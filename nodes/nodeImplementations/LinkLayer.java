package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.SDNMessage.DeleteMessage;
import projects.cbrenet.nodes.messages.SDNMessage.LinkMessage;
import projects.cbrenet.nodes.messages.SDNMessage.StatusChangedMessage;
import projects.cbrenet.nodes.messages.controlMessage.DeleteRequestMessage;
import projects.cbrenet.nodes.messages.controlMessage.LargeInsertMessage;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;


import java.util.*;

/**
 * This layer is used to maintain links used in the network.
 *
 * The large node has only one root node. Note that the root node ' id is not the large node id.
 * But we notice large node to
 * */

public abstract class LinkLayer extends MessageQueueLayer{

    HashMap<Integer, PriorityQueue<LinkMessage>> unsatisfiedLinkMessage;

    // StatusChangedMessage必须按照顺序执行！ TODO　这可能是一个比较复杂度的系统了



    private int receivedStatusChangedId = 0; // record the statusId has been received

    public void init(){
        super.init();
        unsatisfiedLinkMessage = new HashMap<>();
    }


    private void executeLinkMessage(LinkMessage linkMessage){
        ArrayList<Integer> targets = linkMessage.getTargets();
        int largeId = linkMessage.getLargeId();
        int len = targets.size();
        for(int idx=0;idx<len;idx++){
            int id = targets.get(idx);
            if(id < 0){
                continue;
            }
            if(linkMessage.isInsertFlag()) {
                if(largeId == -1){
                    this.addConnectionTo(Tools.getNodeByID(id));
                }
                else{
                    // create ego-tree
                    char relation = linkMessage.getRelationships().get(idx);
                    switch (relation){
                        case 'p':
                            // TODO 这里我们要选择一下建立链接的方式，如果我们只允许从父亲结点建立
                            // TODO 双向链接， 这里则不必要
                            break;
                        case 'l':
                            this.addBidirectionalLinkToLeftChild(largeId, id);
                            break;
                        case 'r':
                            this.addBidirectionalLinkToRightChild(largeId, id);
                            break;
                        case 't':
                            this.addBidirectionalLinkToRootNode(largeId, id);
                    }
                }
            }
            else{
                if(largeId == -1){
                    this.removeSingleLinkTo(Tools.getNodeByID(id));
                }
                else{
                    // todo 小结点向大结点传信息的的过程中，如果遇到大结点变小怎么办呢？
                    char relation = linkMessage.getRelationships().get(idx);
                    switch (relation){
                        case 'p':
                            // TODO 这里我们要选择一下建立链接的方式，如果我们只允许从父亲结点建立
                            // TODO 双向链接， 这里则不必要
                            break;
                        case 'l':
                            this.removeBidirectionalLinkToLeftChild(largeId, id);
                            break;
                        case 'r':
                            this.removeBidirectionalLinkToRightChild(largeId, id);
                            break;
                    }
                }
            }
        }
    }


    @Override
    public void receiveMessage(Message msg){
        super.receiveMessage(msg);
        if(msg instanceof LinkMessage){
            LinkMessage linkMessage = (LinkMessage) msg;
            int globalStatusIdTmp = linkMessage.getUniqueStatusId();
            // TODO 刚加入ego-tree的小结点变成了大结点
            if(globalStatusIdTmp > this.getReceivedStatusChangedId()){
                // indicate that the status has not been changed!
                // TODO the LinkMessage may have to execute later
                PriorityQueue<LinkMessage> unsatisfiedLinkMessagesTmp = unsatisfiedLinkMessage.getOrDefault(globalStatusIdTmp, null);
                if (unsatisfiedLinkMessagesTmp == null) {
                    unsatisfiedLinkMessagesTmp = new PriorityQueue<>();
                }
                unsatisfiedLinkMessagesTmp.add(linkMessage);
                unsatisfiedLinkMessage.replace(globalStatusIdTmp, unsatisfiedLinkMessagesTmp);
            }
            else{
                // This LinkMessage is satisfied to executed!
                this.executeLinkMessage(linkMessage);
            }
        }
        else if(msg instanceof DeleteMessage){
            // TODO　完善DeleteMessage
            DeleteMessage deleteMessage = (DeleteMessage) msg;
            if(deleteMessage.isAllFlag()){
                // 全删的
            }
            else{
                // 只删自己的
            }
        }
        else if(msg instanceof LargeInsertMessage){
            // Here should be the node which is specified in the LargeInsertMessage

            LargeInsertMessage insertMessage = (LargeInsertMessage) msg;
            int parentId = insertMessage.getLeafId();
            int largeNodeId = insertMessage.getLargeId();

            boolean addLinkFlag = true;

            if(this.largeFlag){
               Tools.warning("When a largeInsertMessage received by a node, the node has been changed into a large " +
                       "node.");
               addLinkFlag = false;
            }

            if(this.isNodeSmall(largeNodeId)){
                // This node has got the message that the Large node has been changed into small.
                Tools.warning("The large node of the ego tree has been changed to small! The LargeInsertMessage " +
                        "won't execute in the target node!!");
                addLinkFlag = false;
            }

            if(!addLinkFlag){
                Node parentNode = Tools.getNodeByID(parentId);
                assert parentNode != null;

                // Remove the link, this node don't want to be a part of ego-tree for some reason.
                parentNode.outgoingConnections.remove(parentNode, this);
                return;
            }

            // The node still want to be a node of the ego-tree
            this.setParent(largeNodeId, parentId);
            this.addConnectionTo(Tools.getNodeByID(parentId));

            // add CP
            this.addCommunicationPartner(largeNodeId);

            // set inserted bit in the insertMessage
            insertMessage.setInserted();

            this.sendDirect(insertMessage, Tools.getNodeByID(this.getSDNId()));


        }
        else if(msg instanceof StatusChangedMessage){
            StatusChangedMessage statusChangedMessage = (StatusChangedMessage) msg;
            int changeId = statusChangedMessage.getStatusChangedNodeId();

            boolean smallFlag = statusChangedMessage.isSmallFlag();

            //update global status
            this.changeNodeStatus(changeId, smallFlag);


            int statusTmp = statusChangedMessage.getUniqueStatusId();
            this.receivedStatusChangedId = Math.max(this.receivedStatusChangedId, statusTmp);

            if(changeId == this.ID){
                this.clearPartners(smallFlag);
                if(!smallFlag){
                    // small -> large, The DRM must sent ASAP!
                    for(int largeId : this.getCommunicateLargeNodes().keySet()){
                        // int src, int dst, boolean ego_tree, int wantToDeleteId
                        DeleteRequestMessage drm = new DeleteRequestMessage(this.ID, largeId, true, this.ID);

                        // in following method, the corresponding CP would be removed immediately!
                        this.sendEgoTreeMessage(largeId,largeId,drm);
                    }
                }
            }
            else{
                if(smallFlag){
                    // Target node  large -> small
                    if(this.getCommunicateLargeNodes().containsKey(changeId)){
                        if(!this.largeFlag) {
                            // actually this would not be a node of transition status
                            // since if this node is a transition status node, it would not have a large CP

                            // status changed node is the ego-tree's large node of this node
                            // ego-tree-link
                            DeleteRequestMessage msgToLargeNode = new DeleteRequestMessage(this.ID, changeId, true, this.ID);
                            this.sendEgoTreeMessage(changeId, changeId, msgToLargeNode);

                            // todo 下面这一段，或许可以删除！
                            DeleteRequestMessage msgToSDN = new DeleteRequestMessage(this.ID, changeId, false, this.ID);
                            this.sendDirect(msgToSDN, Tools.getNodeByID(this.getSDNId()));
                        }
                        else{
                            // l-l link
                            this.removeCommunicationPartner(changeId);
                        }
                    }
                    else{
                        // not effected by the SCM.
                        // do nothing here
                    }
                }
                else{
                    // Target node small -> large

                    // no matter this node is small or large, remove CP ASAP
                    this.removeCommunicationPartner(changeId);
                }
            }

            // execute the satisfied link message

            //todo 除了LinkMessage,别的也可能存在这种情况啊

            if(this.unsatisfiedLinkMessage.keySet().size() != 0){
                ArrayList<Integer> statusIdList = new ArrayList<Integer>(unsatisfiedLinkMessage.keySet());
                Collections.sort(statusIdList); // 升序
                for(int statusId:statusIdList){
                    if(statusId > this.getReceivedStatusChangedId()){
                        break;
                    }
                    PriorityQueue<LinkMessage> linkMessages = this.unsatisfiedLinkMessage.get(statusId);
                    while(!linkMessages.isEmpty()){
                        LinkMessage linkMessageTmp = linkMessages.poll(); // 直接出队列
                        this.executeLinkMessage(linkMessageTmp);
                    }
                }
            }

        }
    }


    public void makeLarge(){
        if(this.largeFlag){
            // already large
            return;
        }
        LinkedHashMap<Integer, Integer>  smallNodePairs= this.getCommunicateSmallNodes();
        // remove existing s-s link
        Set<Integer> smallNodeIds = smallNodePairs.keySet();
        for(int smallId : smallNodeIds){
            LinkLayer linkNode = (LinkLayer) Tools.getNodeByID(smallId);
            linkNode.removeLinkTo(this);

            // update communication Partner
//            linkNode.removeCommunicationPartner(this.ID);
//            this.removeCommunicationPartner(smallId);
        }

        // remove existing s-l link
        Set<Integer> largeIds = this.getLargeIds();
        for(int largeId : largeIds){
            LinkLayer linkNode = (LinkLayer) Tools.getNodeByID(largeId);
            linkNode.removeNodeFromEgoTree(this.ID);

            // update communication Partner
//            linkNode.removeCommunicationPartner(this.ID);
//            this.removeCommunicationPartner(largeId);
        }

        // make to large
        this.changeLargeFlag();

        // make ege tree
        this.makeEgoTree();
        this.addLargeLinks();


    }

    private void makeEgoTree(){
        this.getCommunicateSmallNodes();
    }

    private void addLargeLinks(){
        this.getCommunicateLargeNodes();
    }

    public void removeNodeFromEgoTree(int smallId){
        /**
         *@description remove a small node of [smallId] of the ego tree of this
         * large node 's ego tree
         *@parameters  [smallId]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/7
         */
        // todo

    }


    public void makeSmall(){
        if(!this.largeFlag){
            return;
        }
        // remove l-s link
        this.clearEgoTree();

        // remove l-l link;
        Set<Integer> largeIds = this.getLargeIds();
        for(int largeId : largeIds){
            LinkLayer linkNode = (LinkLayer) Tools.getNodeByID(largeId);
            this.removeLinkTo(linkNode);
            this.removeCommunicationPartner(largeId);
        }

    }

    public void clearEgoTree(){
        /** TODO
         *@description clear the ego tree of a large node
         *@parameters  [id]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/7
         */
        if(!this.largeFlag){
            return;
        }
        LinkLayer largeNode = (LinkLayer) Tools.getNodeByID(this.ID);
        LinkedHashMap<Integer, Integer> smallNodes = largeNode.getCommunicateSmallNodes();
        if(smallNodes != null){
            for(int smallId : smallNodes.keySet()){
                CounterBasedBSTLayer bstLayer =  (CounterBasedBSTLayer)Tools.getNodeByID(smallId);
                if(bstLayer != null){
                    bstLayer.removeAllNeighbors(this.ID);
                }
                else{
                    Tools.fatalError("Get a null node in the Link layer of removeNodeFromEgoTree");
                }
            }
        }
        else{
            Tools.fatalError("Get a null small nodes in the Link layer of removeNodeFromEgoTree");
        }


    }

    private void removeSingleLinkTo(Node node){
        if(node == null){
            return;
        }
        if (this.outgoingConnections.contains(this,node)) {
            this.outgoingConnections.remove(this, node);
        }
        else {
            Tools.fatalError("Trying to remove a non-existing single connection in the link layer" +
                    " from" +this.ID + " to node " + ID);
        }
    }

    private void removeLinkTo(Node node) {
        // make sure only the parent remove the link
        if(node == null){
            return;
        }
        if (this.outgoingConnections.contains(this,node)) {
            this.outgoingConnections.remove(this, node);
            node.outgoingConnections.remove(node, this);
        }
        else {
            Tools.fatalError("Trying to remove a non-existing connection in the link layer" +
                    " from" +this.ID + " to node " + ID);
        }
    }

    public int getReceivedStatusChangedId() {
        return receivedStatusChangedId;
    }

}
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

        }
        else if(msg instanceof LargeInsertMessage){
            if(this.largeFlag){
               Tools.fatalError("When a largeInsertMessage received by a node, the node has been changed into a large " +
                       "node.");
                return;
            }
            LargeInsertMessage insertMessage = (LargeInsertMessage) msg;
            int parentId = insertMessage.getLeafId();
            int largeNodeId = insertMessage.getLargeId();
            this.setParent(largeNodeId, parentId);
            this.addConnectionTo(Tools.getNodeByID(parentId));
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
                if(smallFlag){
                    //large -> small all link will be cleared
                    this.clearPartners(smallFlag);
                }
                else{
                    // small -> large all link will also be cleared
                    // but we need to reserve the information about small partner to create tree
                    this.clearPartners(smallFlag);
                }
            }
            else{
                // todo 对于自己的large node 变大，我们还需要尽速发送DeleteRequestMessage 将ego-tree clear掉
                if(smallFlag){
                    // TODO send DeleteRequestMessage to the root
                    if(this.getCommunicateLargeNodes().containsKey(changeId)){
                        DeleteRequestMessage msgToLargeNode = new DeleteRequestMessage(this.ID, changeId, true, this.ID);
                        this.sendToParent(changeId, msgToLargeNode);

                        DeleteRequestMessage msgToSDN = new DeleteRequestMessage(this.ID, changeId, false, this.ID);
                        this.sendDirect(msgToSDN, Tools.getNodeByID(this.getSDNId()));
                    }
                    // tODO

                }
                else{
                    // TODO
                }
            }

            // execute the satisfied link message
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

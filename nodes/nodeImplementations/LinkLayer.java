package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.SDNMessage.*;
import projects.cbrenet.nodes.messages.controlMessage.DeleteRequestMessage;
import projects.cbrenet.nodes.messages.controlMessage.clusterMessage.ClusterRelatedMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteBaseMessage;
import projects.cbrenet.nodes.nodeImplementations.deleteProcedure.DeleteProcess;
import projects.cbrenet.nodes.nodeImplementations.nodeHelper.ClusterHelper;
import projects.cbrenet.nodes.routeEntry.SendEntry;
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

public abstract class LinkLayer extends MessageSendLayer{

    DeleteProcess deleteProcess = new DeleteProcess();

    HashMap<Integer, PriorityQueue<StatusRelatedMessage>> unsatisfiedLinkMessage;

    private int receivedStatusChangedId = 0; // record the statusId has been received

    public void init(){
        super.init();
        unsatisfiedLinkMessage = new HashMap<>();
        this.receivedStatusChangedId = 0;
    }


    private void executeLinkMessage(LinkMessage linkMessage){
        ArrayList<Integer> targets = linkMessage.getTargets();
        int largeId = linkMessage.getLargeId();
        int len = targets.size();
        for(int idx=0;idx<len;idx++){
            int id = targets.get(idx);
            if(id < 0){
                // SDN do not want to use this bit
                continue;
            }
            if(linkMessage.isInsertFlag()) {
                if(largeId < 0){
                    // add cp
                    this.addCommunicationPartner(id);
                    //this.addConnectionTo(Tools.getNodeByID(id));
                }
                else{
                    // create ego-tree
                    // To make the simulation so close to the reality as we could, only
                    // allow the node to create unidirectional link
                    char relation = linkMessage.getRelationships().get(idx);

                    this.addInsertMessageExecuteFlags(largeId);

                    switch (relation){
                        case 'p':
                            this.addLinkToParent(largeId,id, id);
                            this.addCommunicationPartner(largeId,true);
                            if(linkMessage.isParentIsLnFlag()){
                                this.getCorrespondingEntry(this.ID,largeId).setEgoTreeRoot(true);
                            }
                            break;
                        case 'l':
                            this.addLinkToLeftChild(largeId, id, id);
                            break;
                        case 'r':
                            this.addLinkToRightChild(largeId, id, id);
                            break;
                        case 't':
                            this.addLinkToRootNode(largeId, id);
                            break;
                    }
                }
            }
            else{
                // ToDo 完善LinkMessage 中对 remove link 的需要
                // 这里会用到吗？
                if(largeId == -1){
                    this.removeSingleLinkTo(Tools.getNodeByID(id));
                }
                else{
                    char relation = linkMessage.getRelationships().get(idx);
                    switch (relation){
                        case 'p':
                            this.removeLinkToParent(largeId);
                            break;
                        case 'l':
                            this.removeLinkToLeftChild(largeId);
                            break;
                        case 'r':
                            this.removeLinkToRightChild(largeId);
                            break;
                    }
                }
            }
        }
    }


    private void executeLargeInsertMessage(LargeInsertMessage insertMessage){
        // Here should be the node which is specified in the LargeInsertMessage
        if(insertMessage.isInserted()){
            Tools.warning("LinkLayer: The LIM is inserted, check what's wrong");
        }
        else{
            int parentId = insertMessage.getLeafId();
            int largeNodeId = insertMessage.getLargeId();

            boolean leftFlag = insertMessage.isLeftFlag();

            boolean addLinkFlag = true;

            if(this.largeFlag){
                Tools.warning("When a largeInsertMessage received by a node, the node has been changed into a large " +
                        "node.");
                addLinkFlag = false;
            }

            if(this.isNodeSmall(largeNodeId)){
                // This node has got the message that the Large node has been changed into small.

                // No Message is sending to this node, so no need to send DRM

                Tools.warning("The large node of the ego tree has been changed to small! The LargeInsertMessage " +
                        "won't execute in the target node!!");
                addLinkFlag = false;
            }

            if(!addLinkFlag){
                CounterBasedBSTLayer parentNode = (CounterBasedBSTLayer) Tools.getNodeByID(parentId);
                assert parentNode != null;

                if(parentId == largeNodeId){
                    parentNode.removeRootNode();
                }
                // Remove the link, this node don't want to be a part of ego-tree for some reason.

                // parentNode.outgoingConnections.remove(parentNode, this);

                // notice the parent node to remove one child
                if(leftFlag){
                    parentNode.removeLinkToLeftChild(largeNodeId);
                }
                else{
                    parentNode.removeLinkToRightChild(largeNodeId);
                }
                return;
            }

            // This node can execute LIM immediately
            this.addInsertMessageExecuteFlags(largeNodeId);

            this.addLinkToParent(largeNodeId, parentId, parentId);
            // The node still want to be a node of the ego-tree

            if(parentId == largeNodeId){
                this.getCorrespondingEntry(-1, largeNodeId).setEgoTreeRoot(true);
            }

            // add CP
            this.addCommunicationPartner(largeNodeId);

            // set inserted bit in the insertMessage
            insertMessage.setInserted();

            this.sendDirect(insertMessage, Tools.getNodeByID(this.getSDNId()));
        }
    }


    private void executeEgoTreeMessage(EgoTreeMessage egoTreeMessage){
        List<Integer> egoTreeList = egoTreeMessage.getEgoTreeNodes();

        this.setEgoTreeDeleteMap(egoTreeList);

        this.setSmallNodesCp(egoTreeList);
    }


    private void executeStatusRelatedMessage(StatusRelatedMessage statusRelatedMessage){
        if(statusRelatedMessage instanceof LinkMessage){
            this.executeLinkMessage((LinkMessage) statusRelatedMessage);
        }
        else if(statusRelatedMessage instanceof DeleteMessage){
            this.executeDeleteMessage((DeleteMessage) statusRelatedMessage);
        }
        else if(statusRelatedMessage instanceof LargeInsertMessage){
            this.executeLargeInsertMessage((LargeInsertMessage) statusRelatedMessage);
        }
        else if(statusRelatedMessage instanceof EgoTreeMessage){
            this.executeEgoTreeMessage((EgoTreeMessage) statusRelatedMessage);
        }
        else{
            Tools.warning("LinkLayer: The StatusRelatedMessage in LinkLayer is "+ statusRelatedMessage.getClass());
        }
    }



    //  Delete Message Part
    private void executeDeleteMessage(DeleteMessage deleteMessage){

        int largeId = deleteMessage.getLargeId();
        if(deleteMessage.isAllFlag()){
            // 全删模式，整个Ego-Tree全部清空！
            // 清空就行了！不必担心Ego-Tree中是否有剩余的Message
            this.removeLinkToParent(largeId);
            this.removeLinkToLeftChild(largeId);
            this.removeLinkToRightChild(largeId);
            this.removeSendEntry(-1, largeId);
        }
        else{
            // 只删自己的Entry和Link, 这就需要保证Queue清空
            // DeleteProcess 就是用于保障Queue清空！
            SendEntry correspondingEntry = this.getCorrespondingEntry(-1, largeId);

            correspondingEntry.setDeleteFlag(true);
            correspondingEntry.setAuxiliaryId(deleteMessage);

            deleteProcess.startDelete(this.getCorrespondingEntry(-1, largeId), this, largeId, this.ID);
        }
    }


    private void tryToDeleteInPostRound(){
        Set<Integer> largeIds = this.routeTableInEgoTree.keySet();

        for(int largeId : largeIds){
            SendEntry correspondingEntry = this.getCorrespondingEntry(-1,largeId);
            if(correspondingEntry.isQueueEmpty() && correspondingEntry.isDeleteFlag()){
                this.deleteProcess.startDelete(correspondingEntry, this, largeId, this.ID);
            }

            deleteProcess.getHighestPriorityDeletePrepareMessage(correspondingEntry, this.ID, this);

            clusterHelper.clusterRequest(correspondingEntry, largeId, this.ID, this);

            // cluster
            clusterHelper.acceptClusterRequest(correspondingEntry, largeId, this.ID, this);
        }

    }




    @Override
    protected void doInPostRound(){
        super.doInPostRound();
        this.tryToDeleteInPostRound();
    }



    @Override
    public void receiveMessage(Message msg){
        if(msg instanceof StatusRelatedMessage){
            StatusRelatedMessage statusRelatedMessage = (StatusRelatedMessage) msg;
            int globalStatusIdTmp = statusRelatedMessage.getUniqueStatusId();
            if(globalStatusIdTmp > this.getReceivedStatusChangedId()){
                PriorityQueue<StatusRelatedMessage> unsatisfiedMessages = this.unsatisfiedLinkMessage.
                        getOrDefault(globalStatusIdTmp, null);
                if(unsatisfiedMessages == null){
                    unsatisfiedMessages = new PriorityQueue<>();
                }
                unsatisfiedMessages.add(statusRelatedMessage);
                this.unsatisfiedLinkMessage.put(globalStatusIdTmp, unsatisfiedMessages);
            }
            else{
                this.executeStatusRelatedMessage(statusRelatedMessage);
            }
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
                this.clearSomePartnersSinceSelfLargeFlagChange(smallFlag);
                if(!smallFlag){

                    Set<Integer> keySet =  this.getCommunicateLargeNodes().keySet();
                    List<Integer> keys = new ArrayList<>(keySet);
                    // small -> large, The DRM must sent ASAP!
                    for(int largeId : keys){
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

//                            // todo 下面这一段，或许可以删除！
//                            DeleteRequestMessage msgToSDN = new DeleteRequestMessage(this.ID, changeId, false, this.ID);
//                            this.sendDirect(msgToSDN, Tools.getNodeByID(this.getSDNId()));
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

            // execute the satisfied StatusRelatedMessage message

            if(this.unsatisfiedLinkMessage.keySet().size() != 0){
                ArrayList<Integer> statusIdList = new ArrayList<Integer>(unsatisfiedLinkMessage.keySet());
                Collections.sort(statusIdList); // 升序
                for(int statusId:statusIdList){
                    if(statusId > this.getReceivedStatusChangedId()){
                        break;
                    }
                    PriorityQueue<StatusRelatedMessage> statusRelatedMessages = this.unsatisfiedLinkMessage.get(statusId);
                    while(!statusRelatedMessages.isEmpty()){
                        StatusRelatedMessage statusRelatedMessage = statusRelatedMessages.poll(); // 直接出队列
                        this.executeStatusRelatedMessage(statusRelatedMessage);
                    }
                }
            }

        }
        else if(msg instanceof DeleteBaseMessage){
            DeleteBaseMessage deleteBaseMessage = (DeleteBaseMessage) msg;

            int largeId = deleteBaseMessage.getLargeId();
            int helpedId = -1;

            if(this.deleteProcess.executeDeleteBaseMessage(deleteBaseMessage,
                    this, this, this.ID)){
                // remove entry
                this.removeCorrespondingEntry(helpedId, largeId);
            }
        }
        else if(msg instanceof ClusterRelatedMessage){

            this.clusterHelper.receiveClusterRelatedMessage((ClusterRelatedMessage) msg, this,this, this.ID);

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


    public int getReceivedStatusChangedId() {
        return receivedStatusChangedId;
    }

}

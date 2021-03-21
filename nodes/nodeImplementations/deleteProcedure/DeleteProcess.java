package projects.cbrenet.nodes.nodeImplementations.deleteProcedure;

// 放一些AuxiliaryNode 和 CBBSTNode 都需要的代码

import projects.cbrenet.nodes.messages.SDNMessage.DeleteMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.*;
import projects.cbrenet.nodes.nodeImplementations.AuxiliaryNode;
import projects.cbrenet.nodes.nodeImplementations.AuxiliaryNodeMessageQueueLayer;
import projects.cbrenet.nodes.nodeImplementations.CounterBasedBSTLayer;
import projects.cbrenet.nodes.nodeImplementations.MessageSendLayer;
import projects.cbrenet.nodes.nodeImplementations.nodeHelper.EntryGetter;
import projects.cbrenet.nodes.routeEntry.AuxiliarySendEntry;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

import java.util.List;

public class DeleteProcess {

    private void sendDeletePrepareMessage(SendEntry deleteEntry, DeletePrepareMessage deletePrepareMessage, int largeId, int helpedId, Node node){
        /*
         *@description Send DPM to its neighbors, and set relation in the DPM.
         *@parameters  [deleteEntry, deletePrepareMessage, largeId, helpedId, node]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/21
         */

        // must INIT the Confirm Map !
        // Note that it may change when the ego-tree adjust or one of its neighbors delete itself
        deleteEntry.initGotMap();

        // send DPM to its neighbor.
        List<Integer> egoIdTargetList = deleteEntry.getAllSendIds();
        for(int targetId : egoIdTargetList){
            if(deleteEntry.whetherReceivedConfirmMessageFrom(targetId)){
                continue;
            }
            DeletePrepareMessage deletePrepareMessage1 = new DeletePrepareMessage(deletePrepareMessage);
            char relation = deleteEntry.getRelationShipOf(targetId);
            switch (relation){
                case 'p':
                    deletePrepareMessage1.setRelation(Relation.child);
                case 'l':
                    deletePrepareMessage1.setRelation(Relation.parent);
                case 'r':
                    deletePrepareMessage1.setRelation(Relation.parent);
            }
            this.sendDeleteBaseMessage(deletePrepareMessage1, largeId, deleteEntry, targetId, helpedId, node);
        }

    }


    public void startDelete(SendEntry deleteEntry, Node node, int largeId, int helpedId){
        // when node is node AN, node.id should equal to helpedId
        assert !(node instanceof CounterBasedBSTLayer) || node.ID == helpedId;

        // if deleting, means no need to start delete again.
        if(deleteEntry.isDeletingFlagOfItSelf()){
            return;
        }

        DeletePrepareMessage deletePrepareMessage = new DeletePrepareMessage
                (largeId, helpedId, Tools.getGlobalTime());

        deletePrepareMessage.setRelation(Relation.itself);

        // set deleteEntry 's DPM of itself
        deleteEntry.setDeletePrepareMessageOfMine(deletePrepareMessage);

        // set DPM, add it in Priority Queue to execute after
        deleteEntry.receiveOrSetDeletePrepareMessage(deletePrepareMessage);
        //deleteEntry.setDeletePrepareMessageOfMine(deletePrepareMessage);

        deleteEntry.initGotMap();
    }


    public void getHighestPriorityDeletePrepareMessage(SendEntry deleteEntry, int helpedId, Node node){
        /*
         *@description Execute this method in EVERY post round. Even the node has not received the DeleteMessage yet!
         *@parameters  [deleteEntry, helpedId, node]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/21
         */

        DeletePrepareMessage deletePrepareMessageToDealWith = deleteEntry.getDeletePrepareMessageFromPriorityQueue();

        if(deletePrepareMessageToDealWith == null){
            return;
        }

        // check whether this is itself 's DPM
        if(deletePrepareMessageToDealWith.equals(deleteEntry.getDeletePrepareMessageOfMine())){
            // 执行的时候调试一下看这里是否正确执行
            int largeId = deletePrepareMessageToDealWith.getLargeId();

            if(deleteEntry.checkNeighborDeleting()){
                deleteEntry.setDeletingFlagOfItSelf(true);
                this.sendDeletePrepareMessage(deleteEntry, deletePrepareMessageToDealWith, largeId, helpedId, node);
            }
            else{
                // 还有邻居在做删除，再等等。
                Tools.warning("At least one of neighbors is deleting itself, so can not delete now!");
            }
        }
        else{
            // 想想看，这里需不需要确认一下发送DPM的问题
            this.confirmNeighborsDeleteRequest(deletePrepareMessageToDealWith, deleteEntry, helpedId, node);
        }
    }

    private void confirmNeighborsDeleteRequest(DeletePrepareMessage deletePrepareMessage, SendEntry deleteEntry, int helpedId, Node node){
        /*
         @description Send DeleteConfirmMessage to the deleting one, and it will set
         *              the corresponding deleting flag to true， and Send flag to false;
         *               就算自己在Deleting也没有关系，因为一定不会引发冲突
         @parameters  [sendEntry, deletePrepareMessage, helpedId]
         *              helpedId: In AN, should be helpedId
         *                        In BST, should be BST' id, which is node.ID = helpedId
         @return  void
         @author  Zhang Hongxuan
         @create time  2021/3/17
         */

        int largeId = deletePrepareMessage.getLargeId();
        int deleteTarget = deletePrepareMessage.getDeleteTarget();

        if(deleteTarget == helpedId){
            // means this is it self's DeletePrepareMessage
            Tools.warning("The code let the node to send the DCM according to its own DPM!!");
            return;
        }

        DeleteConfirmMessage confirmMessage = new DeleteConfirmMessage(largeId, deleteTarget, helpedId);

        // 确认并且通过某一个邻居在做deleting
        deleteEntry.targetStartDeleting(deleteTarget);

        this.sendDeleteBaseMessage(confirmMessage, largeId, deleteEntry, deleteTarget, helpedId, node);
    }




    // send DBM
    private void sendDeleteBaseMessage(DeleteBaseMessage message, int largeId, SendEntry deleteEntry, int targetId, int helpedId, Node node){
        boolean upward = false;
        if(deleteEntry.getRelationShipOf(targetId) == 'p'){
            upward = true;
        }
        if(node instanceof MessageSendLayer){
            ((MessageSendLayer)node).sendEgoTreeMessage(largeId, targetId,
                    message, upward);
        }
        else if(node instanceof AuxiliaryNodeMessageQueueLayer){
            ((AuxiliaryNodeMessageQueueLayer)node).sendEgoTreeMessage(largeId, targetId,
                    message, upward, helpedId);
        }
    }




    // receive DBM part
    private void receiveDeletePrepareMessage(SendEntry sendEntry, DeletePrepareMessage deletePrepareMessage){
        /*
         *@description  when receive a DeletePrepareMessage, execute here
         *@parameters  [sendEntry, deletePrepareMessage]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/17
         */
        sendEntry.receiveOrSetDeletePrepareMessage(deletePrepareMessage);
//        sendEntry.setDeletePrepareMessageOfMine(deletePrepareMessage);
    }

    private boolean receiveDeleteConfirmMessage(SendEntry sendEntry, DeleteConfirmMessage deleteConfirmMessage){
        sendEntry.receiveConfirmMessage(deleteConfirmMessage);
        return sendEntry.setDeleteConfirmMessage(deleteConfirmMessage);
    }

    private void receiveAndExecuteDeleteFinishMessage(SendEntry sendEntry, Node node, DeleteFinishMessage deleteFinishMessage, int helpedId){
        int deleteTarget = deleteFinishMessage.getDeleteTarget();
        int largeId = deleteFinishMessage.getLargeId();

        int egoTreeId = deleteFinishMessage.getEgoTreeId();
        int trueId = deleteFinishMessage.getTrueId();

        char relation = sendEntry.getRelationShipOf(deleteTarget);

        // must call unset here, because the follow code would change the ego tree id of its neighbor
        sendEntry.targetDeletingFinish(deleteTarget);

        switch (relation){
            case 'p':
                if(node instanceof MessageSendLayer){
                    MessageSendLayer messageSendLayer = (MessageSendLayer) node;
                    messageSendLayer.changeParentTo(largeId, egoTreeId, trueId);
                }
                else if(node instanceof AuxiliaryNode){
                    AuxiliaryNode auxiliaryNode = (AuxiliaryNode) node;
                    auxiliaryNode.changeParentTo(largeId, egoTreeId, trueId, helpedId);
                }
                break;
            case 'l':
                if(node instanceof MessageSendLayer){
                    MessageSendLayer messageSendLayer = (MessageSendLayer) node;
                    messageSendLayer.changeLeftChildTo(largeId, egoTreeId, trueId);
                }
                else if(node instanceof AuxiliaryNode){
                    AuxiliaryNode auxiliaryNode = (AuxiliaryNode) node;
                    auxiliaryNode.changeLeftChildTo(largeId, egoTreeId, trueId, helpedId);
                }
                break;
            case 'r':
                if(node instanceof MessageSendLayer){
                    MessageSendLayer messageSendLayer = (MessageSendLayer) node;
                    messageSendLayer.changeRightChildTo(largeId, egoTreeId, trueId);
                }
                else if(node instanceof AuxiliaryNode){
                    AuxiliaryNode auxiliaryNode = (AuxiliaryNode) node;
                    auxiliaryNode.changeRightChildTo(largeId, egoTreeId, trueId, helpedId);
                }
                break;
            default:
                Tools.warning("When executing DeleteFinishMessage, can not get relation");
                break;
        }



    }

    private void sendDeleteFinishMessage(SendEntry sendEntry, int largeId, int helpedId, Node node){
        /*
         *@description 清空DCMList, 发送DFM， todo 断链接
         *@parameters  [sendEntry, largeId, helpedId, node]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/21
         */

        if(sendEntry.checkNeighborDeleting()){
            Tools.fatalError("THIS SHOULD NEVER HAPPEN! When the node want to send DeleteConfirmMessage, " +
                    "all its neighbors should return the DCM, but it seems " +
                    "that one of its neighbor it deleting.");
            return;
        }

        // 清空DCM
        sendEntry.clearConfirmMessageList();
        List<Integer> ids = sendEntry.getAllSendIds();

        boolean insertFlag = false;
        if(sendEntry instanceof AuxiliarySendEntry){
            insertFlag = ((AuxiliarySendEntry)sendEntry).isInsertedFlag();
        }
        // if insertFlag, the AN need a similar operation as the BST who has 3 neighbor

        if(ids.size() == 3 || insertFlag){
            // Need AN to help or AN want the node back!
            int auxiliaryId = sendEntry.getAuxiliaryId();

            DeleteFinishMessage deleteFinishMessage = new DeleteFinishMessage(largeId, helpedId, helpedId, auxiliaryId);
            for(int dstId : ids)
            {
                this.sendDeleteBaseMessage(deleteFinishMessage, largeId, sendEntry, dstId, helpedId, node);
            }

        }
        else{
            if(ids.size() == 2){
                // Just Connect them two would be OK
                int neighbor1 = ids.get(0);
                int neighbor2 = ids.get(1);

                int sendIdOfNeighbor1 = sendEntry.getSendIdOf(neighbor1);
                int sendIdOfNeighbor2 = sendEntry.getSendIdOf(neighbor2);

                DeleteFinishMessage deleteFinishMessageTo2 = new DeleteFinishMessage(largeId, helpedId, neighbor1, sendIdOfNeighbor1);
                DeleteFinishMessage deleteFinishMessageTo1 = new DeleteFinishMessage(largeId, helpedId, neighbor2, sendIdOfNeighbor2);

                this.sendDeleteBaseMessage(deleteFinishMessageTo2, largeId, sendEntry, neighbor2, helpedId, node);
                this.sendDeleteBaseMessage(deleteFinishMessageTo1, largeId, sendEntry, neighbor1, helpedId, node);


            }
            else if(ids.size() == 1){
                // no need to create any link
                int neighbor1 = ids.get(0);
                DeleteFinishMessage deleteFinishMessageTo1 = new DeleteFinishMessage(largeId, helpedId, -1, -1);
                this.sendDeleteBaseMessage(deleteFinishMessageTo1, largeId, sendEntry, neighbor1, helpedId, node);
            }
            else{
                Tools.warning("Very Interesting situation， no node connect to it!");
            }
        }

        sendEntry.setDeletingFlagOfItSelf(false);

    }


    public void executeDeleteBaseMessage(DeleteBaseMessage msg, EntryGetter entryGetter, Node node){
        /*
         *@description  Call this method when receive DeleteBaseMessage
         *@parameters  [msg, entryGetter, node]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/21
         */
        int largeId = msg.getLargeId();
        int helpedId = msg.getDeleteTarget();
        if(msg instanceof DeletePrepareMessage){
            SendEntry entry = entryGetter.getCorrespondingEntry(helpedId, largeId);
            this.receiveDeletePrepareMessage(entry,(DeletePrepareMessage) msg);
        }
        else if(msg instanceof DeleteConfirmMessage){
            SendEntry entry = entryGetter.getCorrespondingEntry(helpedId,largeId);
            if(this.receiveDeleteConfirmMessage(entry, (DeleteConfirmMessage)msg)){
                this.sendDeleteFinishMessage(entry,largeId, helpedId, node);
            }
        }
        else if(msg instanceof DeleteFinishMessage){
            SendEntry entry = entryGetter.getCorrespondingEntry(helpedId,largeId);
            this.receiveAndExecuteDeleteFinishMessage(entry, node, (DeleteFinishMessage)msg, helpedId);
        }
    }
    // receive DBM part finish

}

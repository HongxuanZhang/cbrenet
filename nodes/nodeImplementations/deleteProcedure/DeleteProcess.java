package projects.cbrenet.nodes.nodeImplementations.deleteProcedure;

// 放一些AuxiliaryNode 和 CBBSTNode 都需要的代码

import projects.cbrenet.CustomGlobal;
import projects.cbrenet.nodes.messages.deletePhaseMessages.*;
import projects.cbrenet.nodes.nodeImplementations.AuxiliaryNode;
import projects.cbrenet.nodes.nodeImplementations.AuxiliaryNodeMessageQueueLayer;
import projects.cbrenet.nodes.nodeImplementations.CounterBasedBSTLayer;
import projects.cbrenet.nodes.nodeImplementations.MessageSendLayer;
import projects.cbrenet.nodes.nodeImplementations.nodeHelper.EntryGetter;
import projects.cbrenet.nodes.routeEntry.AuxiliarySendEntry;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.nodes.Node;
import sinalgo.runtime.Global;
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
        List<Integer> egoIdTargetList = deleteEntry.getAllEgoTreeIdOfNeighbors();
        for(int targetId : egoIdTargetList){
            if(deleteEntry.whetherReceivedConfirmMessageFrom(targetId)){
                continue;
            }
            DeletePrepareMessage deletePrepareMessage1 = new DeletePrepareMessage(deletePrepareMessage);
            deletePrepareMessage1.setSendTargetEgoTreeId(targetId);
            char relation = deleteEntry.getRelationShipOf(targetId);
            switch (relation){
                case 'p':
                    deletePrepareMessage1.setRelation(Relation.child);
                    break;
                case 'l':
                    deletePrepareMessage1.setRelation(Relation.parent);
                    break;
                case 'r':
                    deletePrepareMessage1.setRelation(Relation.parent);
                    break;
            }
            this.sendDeleteBaseMessage(deletePrepareMessage1, largeId, deleteEntry, targetId, helpedId, node);
        }

    }


    public void startDelete(SendEntry deleteEntry, Node node, int largeId, int helpedId){
        /*
         *@description Only call this method to start a delete, but not call it when want to delete
         *@parameters  [deleteEntry, node, largeId, helpedId]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/29
         */

        // when node is node AN, node.id should equal to helpedId
        assert !(node instanceof CounterBasedBSTLayer) || node.ID == helpedId;

        // if deleting, means no need to start delete again.
        if(deleteEntry.isDeletingFlagOfItSelf()){
            return;
        }

        CustomGlobal.deleteNum ++ ;
        System.out.println("Node "+ node.ID + " corresponding helpedId is "+ helpedId + " start a delete process in " +
                "the egoTree(" + largeId + "), deleteNum is " + CustomGlobal.deleteNum );

        deleteEntry.setDeletingFlagOfItSelf(true);
        DeletePrepareMessage deletePrepareMessage = new DeletePrepareMessage
                (largeId, helpedId, Tools.getGlobalTime());

        deletePrepareMessage.setRelation(Relation.itself);

        // set deleteEntry 's DPM of itself
        deleteEntry.setDeletePrepareMessageOfMine(deletePrepareMessage);

        // set DPM, add it in Priority Queue to execute after
        deleteEntry.receiveOrSetDeletePrepareMessage(deletePrepareMessage);
        //deleteEntry.setDeletePrepareMessageOfMine(deletePrepareMessage);

        deleteEntry.initGotMap();

        System.out.println("Delete Process: Node " + helpedId + " start to delete!");

    }


    public void getHighestPriorityDeletePrepareMessage(SendEntry deleteEntry, int helpedId, Node node){
        /*
         *@description Execute this method in EVERY post round. Even the node has not received the DeleteMessage yet!
         *              这个函数是用于回复DPM的。
         * @parameters  [deleteEntry, helpedId, node]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/21
         */

        assert !(node instanceof CounterBasedBSTLayer) || node.ID == helpedId;

        DeletePrepareMessage deletePrepareMessageToDealWith = deleteEntry.getDeletePrepareMessageFromPriorityQueue();

        if(deletePrepareMessageToDealWith == null){
            return;
        }

        // delete 与 cluster 之间的一次妥协
        if(CustomGlobal.adjustNum != 0){
            System.err.println("A node need to join in delete but some node is clustering!!!");
            deleteEntry.resetCurHighestDPM();
            // 暂不处理，等待
            return;
        }


        int largeId = deletePrepareMessageToDealWith.getLargeId();
        // check whether this is itself 's DPM
        if(deletePrepareMessageToDealWith.equals(deleteEntry.getDeletePrepareMessageOfMine())){
            // 执行的时候调试一下看这里是否正确执行

            if(!deleteEntry.checkNeighborDeleting()){

                this.sendDeletePrepareMessage(deleteEntry, deletePrepareMessageToDealWith, largeId, helpedId, node);
                System.out.println("Delete Process: Send a DPM from " + helpedId + " " +
                        "in the ego-tree of " + largeId);
            }
            else{
                // 还有邻居在做删除，再等等。
                Tools.warning("At least one of neighbors is deleting itself, so can not delete now!");
            }
        }
        else{
            // 为什么可以直接发送DCM而不用担心冲突呢？
            // 因为冲突只可能发生在这条边上，但是按照规定对方先进行删除，那自己需要等待的DCM对方不会发给自己，自己就一直等着。。
            this.confirmNeighborsDeleteRequest(deletePrepareMessageToDealWith, deleteEntry, helpedId, node);
            System.out.println("Delete Process: Send a DCM from " + helpedId + " " +
                    "" + "to " + deletePrepareMessageToDealWith.getDeleteTarget() + " in the ego-tree of " + largeId);
        }
        deleteEntry.receiveOrSetDeletePrepareMessage(deletePrepareMessageToDealWith);
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

        this.sendDeleteBaseMessage(confirmMessage, largeId, deleteEntry, deleteTarget, helpedId, node);

        // 确认并且通过某一个邻居在做deleting
        // corresponding deleting flag & send flag
        deleteEntry.targetStartDeleting(deleteTarget);
    }




    // send DBM
    private boolean sendDeleteBaseMessage(DeleteBaseMessage message, int largeId, SendEntry deleteEntry,
                                          int targetId, int helpedId, Node node){
        boolean upward = false;
        if(deleteEntry.getRelationShipOf(targetId) == 'p'){
            upward = true;
        }
        if(node instanceof MessageSendLayer){
            System.out.println("Delete process: In node "+ helpedId + " a " +
                    "message " + message.getClass().getSimpleName() + " send to " + targetId + " " +
                    "in the ego-tree of " + largeId );
            return ((MessageSendLayer)node).sendEgoTreeMessage(largeId, targetId,
                    message, upward);
        }
        else if(node instanceof AuxiliaryNodeMessageQueueLayer){
            return ((AuxiliaryNodeMessageQueueLayer)node).sendEgoTreeMessage(largeId, targetId,
                    message, upward, helpedId);
        }


        Tools.fatalError("The node call DeleteProcess should be the instance of one Nodes");
        return false;
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

    private void receiveAndExecuteDeleteFinishMessage(SendEntry sendEntry, Node node, DeleteFinishMessage
            deleteFinishMessage, int changingEntryId){
        /*
         *@description
         *@parameters  [sendEntry, node, deleteFinishMessage, changingEntryId]
         *              changingEntryId 就相当于 AN里的 helpedId.
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/4/6
         */
        int deleteTarget = deleteFinishMessage.getDeleteTarget();
        int largeId = deleteFinishMessage.getLargeId();

        int egoTreeId = deleteFinishMessage.getEgoTreeId();
        int trueId = deleteFinishMessage.getTrueId();

        if(node.ID == largeId){
            // which means this is the large node
            assert  sendEntry == null;
            ((CounterBasedBSTLayer)node).setRootSendId(trueId);
            ((CounterBasedBSTLayer)node).setRootEgoTreeId(egoTreeId);
            return;
        }

        char relation = sendEntry.getRelationShipOf(deleteTarget);

        // must call unset here, because the follow code would change the ego tree id of its neighbor
        sendEntry.targetDeletingFinish(largeId, deleteTarget);

        switch (relation){
            case 'p':
                if(node instanceof MessageSendLayer){
                    MessageSendLayer messageSendLayer = (MessageSendLayer) node;
                    messageSendLayer.changeParentTo(largeId, egoTreeId, trueId);
                }
                else if(node instanceof AuxiliaryNode){
                    AuxiliaryNode auxiliaryNode = (AuxiliaryNode) node;
                    auxiliaryNode.changeParentTo(largeId, egoTreeId, trueId, changingEntryId);
                }
                break;
            case 'l':
                if(node instanceof MessageSendLayer){
                    MessageSendLayer messageSendLayer = (MessageSendLayer) node;
                    messageSendLayer.changeLeftChildTo(largeId, egoTreeId, trueId);
                }
                else if(node instanceof AuxiliaryNode){
                    AuxiliaryNode auxiliaryNode = (AuxiliaryNode) node;
                    auxiliaryNode.changeLeftChildTo(largeId, egoTreeId, trueId, changingEntryId);
                }
                break;
            case 'r':
                if(node instanceof MessageSendLayer){
                    MessageSendLayer messageSendLayer = (MessageSendLayer) node;
                    messageSendLayer.changeRightChildTo(largeId, egoTreeId, trueId);
                }
                else if(node instanceof AuxiliaryNode){
                    AuxiliaryNode auxiliaryNode = (AuxiliaryNode) node;
                    auxiliaryNode.changeRightChildTo(largeId, egoTreeId, trueId, changingEntryId);
                }
                break;
            default:
                Tools.warning("When executing DeleteFinishMessage, can not get relation");
                break;
        }



    }


    private void setInsertedEntry(SendEntry sendEntry, int helpedId, int largeId, boolean rootFlag){
        /*
         *@description 该函数用于将Entry设定入结点中 可能是inserted node，也可能是AN
         *@parameters  [sendEntry, helpedId, largeId, rootFlag]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/31
         */
        List<Integer> ids = sendEntry.getAllEgoTreeIdOfNeighbors();
        int auxiliaryId = sendEntry.getAuxiliaryId();
        // 为 inserted node (or an )设定Entry
        int parentEgoTreeId = -1;
        int parentSendId = -1;
        int leftChildEgoTreeId = -1;
        int leftChildSendId = -1;
        int rightChildEgoTreeId = -1;
        int rightChildSendId = -1;

        for(int id : ids){
            char relation = sendEntry.getRelationShipOf(id);
            switch (relation){
                case 'p':
                    parentEgoTreeId = id;
                    parentSendId = sendEntry.getSendIdOfParent();
                    break;
                case 'l':
                    leftChildEgoTreeId = id;
                    leftChildSendId = sendEntry.getSendIdOfLeftChild();
                    break;
                case 'r':
                    rightChildEgoTreeId = id;
                    rightChildSendId = sendEntry.getSendIdOfRightChild();
                    break;
            }
        }

        EntryGetter entryGetter = (EntryGetter)Tools.getNodeByID(auxiliaryId);
        entryGetter.addSendEntry(helpedId, largeId, parentEgoTreeId, leftChildEgoTreeId, rightChildEgoTreeId);

        SendEntry insertedEntry = entryGetter.getCorrespondingEntry(helpedId, largeId);
        insertedEntry.setSendIdOfParent(parentSendId);
        insertedEntry.setSendIdOfLeftChild(leftChildSendId);
        insertedEntry.setSendIdOfRightChild(rightChildSendId);

        insertedEntry.setEgoTreeRoot(rootFlag);

    }


    private boolean sendDeleteFinishMessage(SendEntry sendEntry, int largeId, int helpedId, Node node){
        /*
         *@description 清空DCMList, 发送DFM
         *@parameters  [sendEntry, largeId, helpedId, node]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/21
         */

        if(sendEntry.checkNeighborDeleting()){
            Tools.fatalError("THIS SHOULD NEVER HAPPEN! When the node want to send DeleteConfirmMessage, " +
                    "all its neighbors should return the DCM, but it seems " +
                    "that one of its neighbor it deleting.");
            return false;
        }

        // 清空DCM
        sendEntry.clearConfirmMessageList();
        List<Integer> ids = sendEntry.getAllEgoTreeIdOfNeighbors();

        boolean insertFlag = false;
        if(sendEntry instanceof AuxiliarySendEntry){
            insertFlag = ((AuxiliarySendEntry)sendEntry).isInsertedFlag();
        }
        // if insertFlag, the AN need a similar operation as the BST who has 3 neighbor


        boolean sendFlag =true;
        if(ids.size() == 3 || insertFlag){
            // Need AN to help or AN want the node back!
            int auxiliaryId = sendEntry.getAuxiliaryId();

            // if a LIM call a node back, then the helpedId should equal to auxiliaryId

            for(int dstId : ids)
            {
                DeleteFinishMessage deleteFinishMessage = new DeleteFinishMessage(largeId, helpedId, helpedId
                        , auxiliaryId, dstId);

                if(!this.sendDeleteBaseMessage(deleteFinishMessage, largeId,
                        sendEntry, dstId, helpedId, node)){
                    sendFlag = false;
                }
            }


            // todo 日后考虑用分布式的方式实现一次
            this.setInsertedEntry(sendEntry, helpedId, largeId, sendEntry.isEgoTreeRoot());

        }
        else{
            if(ids.size() == 2){
                // Just Connect them two would be OK
                int neighbor1 = ids.get(0);
                int neighbor2 = ids.get(1);

                int sendIdOfNeighbor1 = sendEntry.getSendIdOf(neighbor1);
                int sendIdOfNeighbor2 = sendEntry.getSendIdOf(neighbor2);

                DeleteFinishMessage deleteFinishMessageTo2 = new DeleteFinishMessage(largeId, helpedId,
                        neighbor1, sendIdOfNeighbor1, neighbor2);
                DeleteFinishMessage deleteFinishMessageTo1 = new DeleteFinishMessage(largeId, helpedId,
                        neighbor2, sendIdOfNeighbor2, neighbor1);

                if(!this.sendDeleteBaseMessage(deleteFinishMessageTo2, largeId,
                        sendEntry, neighbor2, helpedId, node)){
                    sendFlag = false;
                }
                if(!this.sendDeleteBaseMessage(deleteFinishMessageTo1, largeId,
                        sendEntry, neighbor1, helpedId, node)){
                    sendFlag = false;
                }
            }
            else if(ids.size() == 1){
                // no need to create any link
                int neighbor1 = ids.get(0);
                DeleteFinishMessage deleteFinishMessageTo1 = new DeleteFinishMessage(largeId, helpedId,
                        -1, -1, neighbor1);
                if(!this.sendDeleteBaseMessage(deleteFinishMessageTo1, largeId,
                        sendEntry, neighbor1, helpedId, node)){
                    sendFlag = false;
                }
            }
            else{
                Tools.warning("Very Interesting situation， no node connect to it!");
            }
        }

        if(!sendFlag){
            Tools.fatalError("Check what happen cause the DFM not send!");
            return false;
        }
        else {
            sendEntry.setDeletingFlagOfItSelf(false);
            return true;
        }
    }


    public boolean executeDeleteBaseMessage(DeleteBaseMessage msg, EntryGetter entryGetter, Node node, int helpedId){
        /*
         *@description  Call this method when receive DeleteBaseMessage
         *@parameters  [msg, entryGetter, node]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/21
         */
        int largeId = msg.getLargeId();

        if(node.ID == largeId){
            if(msg instanceof DeletePrepareMessage){
                DeletePrepareMessage deletePrepareMessage = (DeletePrepareMessage) msg;
                int deleteTarget = deletePrepareMessage.getDeleteTarget();
                if(deleteTarget == helpedId){
                    // means this is it self's DeletePrepareMessage
                    Tools.warning("The code let the node to send the DCM according to its own DPM!!");
                    return false;
                }


                DeleteConfirmMessage confirmMessage = new DeleteConfirmMessage(largeId, deleteTarget, helpedId);
                //int largeId, int dst, Message msg, boolean upward
                ((MessageSendLayer)node).sendEgoTreeMessage(largeId, deleteTarget, confirmMessage,false);
                ((CounterBasedBSTLayer)node).setRootNodeSendFlag(false);
                return false;

            }
            else if (msg instanceof DeleteConfirmMessage){
                Tools.warning("Delete Process: Large node " + largeId + " receive a DCM");
                return false;
            }
            else if(msg instanceof DeleteFinishMessage){
                DeleteFinishMessage deleteFinishMessage = (DeleteFinishMessage) msg;

                int egoTreeId = deleteFinishMessage.getEgoTreeId();
                int trueId = deleteFinishMessage.getTrueId();

                if(node.ID == largeId){
                    // which means this is the large node
                    ((CounterBasedBSTLayer)node).setRootSendId(trueId);
                    ((CounterBasedBSTLayer)node).setRootEgoTreeId(egoTreeId);
                    ((CounterBasedBSTLayer)node).setRootNodeSendFlag(true);
                    return false;
                }
            }
        }

        if(msg instanceof DeletePrepareMessage){
            SendEntry entry = entryGetter.getCorrespondingEntry(helpedId, largeId);
            int targetEgoTreeId = ((DeletePrepareMessage) msg).getSendTargetEgoTreeId();
            this.receiveDeletePrepareMessage(entry,(DeletePrepareMessage) msg);
            System.out.println("Delete Process : Ego Node " + targetEgoTreeId + " actual node " + node.ID + "" +
                    "  received a DPM " +
                    "from " + msg.getDeleteTarget() + ", in the ego-tree of " + largeId);
        }
        else if(msg instanceof DeleteConfirmMessage){
            SendEntry entry = entryGetter.getCorrespondingEntry(helpedId,largeId);
            if(this.receiveDeleteConfirmMessage(entry, (DeleteConfirmMessage)msg)){
                System.out.println("Delete Process : Ego Node " + helpedId + " got all DCM," +
                        " in the ego-tree of " + largeId);
                CustomGlobal.deleteNum -- ;
                System.out.println("Node "+ node.ID + " corresponding helpedId is "+ helpedId + " want to send DeleteFinishMessage in " +
                        "the egoTree(" + largeId + "), current deleteNum is " + CustomGlobal.deleteNum );

                return this.sendDeleteFinishMessage(entry,largeId, helpedId, node);
                // Remember to remove Entry outside
            }
        }
        else if(msg instanceof DeleteFinishMessage){
            int targetId = ((DeleteFinishMessage) msg).getTargetEgoTreeId();
            SendEntry entry = entryGetter.getCorrespondingEntry(helpedId,largeId);
            this.receiveAndExecuteDeleteFinishMessage(entry, node, (DeleteFinishMessage)msg, targetId);
        }
        return false;
    }
    // receive DBM part finish

}

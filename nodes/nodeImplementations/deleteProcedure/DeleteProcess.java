package projects.cbrenet.nodes.nodeImplementations.deleteProcedure;

// 放一些AuxiliaryNode 和 CBBSTNode 都需要的代码

import projects.cbrenet.nodes.messages.SDNMessage.DeleteMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteBaseMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteConfirmMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeletePrepareMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.FinishDeleteMessage;
import projects.cbrenet.nodes.nodeImplementations.AuxiliaryNodeMessageQueueLayer;
import projects.cbrenet.nodes.nodeImplementations.CounterBasedBSTLayer;
import projects.cbrenet.nodes.nodeImplementations.MessageSendLayer;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

import java.util.List;
import java.util.Random;

public class DeleteProcess {


    private void sendDeleteBaseMessage(DeleteBaseMessage message, int largeId, SendEntry deleteEntry, int targetId, int helpedId, Node node){
        boolean upward = false;
        if(deleteEntry.getRelationShipTo(targetId) == 'p'){
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


    public void startDelete(SendEntry deleteEntry, Node node, int largeId, int helpedId){


        // when node is node AN, node.id should equal to helpedId
        assert !(node instanceof CounterBasedBSTLayer) || node.ID == helpedId;


        if(!deleteEntry.isDeleteFlag()){
            Tools.warning("Want to delete, but can not now, " +
                    "have to wait one neighbor is deleting.");
            return;
        }

        List<Integer> egoIdTargetList = deleteEntry.getAllSendIds();

        DeletePrepareMessage deletePrepareMessage = new DeletePrepareMessage
                (largeId, helpedId, Tools.getGlobalTime() + (new Random()).nextDouble());

        deleteEntry.setDeletePrepareMessage(deletePrepareMessage);
        deleteEntry.initGotMap();

        if(deleteEntry.cheakNeighborDeleting()){
            for(int targetId : egoIdTargetList){
                boolean upward = false;
                if(deleteEntry.getRelationShipTo(targetId) == 'p'){
                    upward = true;
                }
                if(node instanceof MessageSendLayer){
                    ((MessageSendLayer)node).sendEgoTreeMessage(largeId, targetId,
                            deletePrepareMessage, upward);
                }
                else if(node instanceof AuxiliaryNodeMessageQueueLayer){
                    ((AuxiliaryNodeMessageQueueLayer)node).sendEgoTreeMessage(largeId, targetId,
                            deletePrepareMessage, upward, helpedId);
                }
            }
        }
        else{
            Tools.warning("At least one of neighbors is deleting itself, so can not delete now!");
        }

    }


    public void receiveDeletePrepareMessage(SendEntry sendEntry, DeletePrepareMessage deletePrepareMessage){
        /**
         *@description  when receive a DeletePrepareMessage, execute here
         *@parameters  [sendEntry, deletePrepareMessage]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/17
         */
        sendEntry.setDeletePrepareMessage(deletePrepareMessage);
    }


    public void deleteConfirm(SendEntry deleteEntry, int helpedId, Node node){
        /**
         *@description  Delete Confirm Phase
         *@parameters  [sendEntry, deletePrepareMessage, helpedId]
         *              helpedId: In AN, should be helpedId
         *                        In BST, should be BST' id, which is node.ID = helpedId
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/17
         */

        List<DeletePrepareMessage> messageList = deleteEntry.getDeletePrepareMessage();

        for(DeletePrepareMessage deletePrepareMessage : messageList){
            int largeId = deletePrepareMessage.getLargeId();
            int deleteTarget = deletePrepareMessage.getDeleteTarget();
            // 等待
            DeleteConfirmMessage confirmMessage = new DeleteConfirmMessage(largeId, deleteTarget, helpedId);

            this.sendDeleteBaseMessage(confirmMessage, largeId, deleteEntry, deleteTarget, helpedId, node);
        }
    }

    public boolean receiveDeleteConfirmMessage(SendEntry sendEntry, DeleteConfirmMessage deleteConfirmMessage, int helpedId){
        int src = deleteConfirmMessage.getSrcId();
        int largeId = deleteConfirmMessage.getLargeId();
        return sendEntry.setDeleteConfirmMessage(deleteConfirmMessage);
    }

    public void sendFinishDeleteMessage(SendEntry sendEntry, int largeId, int helpedId, Node node){
        List<Integer> ids = sendEntry.getAllSendIds();


        if(ids.size() == 3){
            // Need AN to help
            DeleteMessage correspondingMessage = sendEntry.getCorrespondingDeleteMessage();
            int auxiliaryId = correspondingMessage.getAuxiliaryNodeId();

            FinishDeleteMessage finishDeleteMessage = new FinishDeleteMessage(largeId, helpedId, helpedId, auxiliaryId);
            for(int dstId : ids)
            {
                this.sendDeleteBaseMessage(finishDeleteMessage, largeId, sendEntry, dstId, helpedId, node);
            }

        }
        else{
            if(ids.size() == 2){
                // Just Connect them two would be OK
                int neighbor1 = ids.get(0);
                int neighbor2 = ids.get(1);

                int sendIdOfNeighbor1 = sendEntry.getSendIdOf(neighbor1);
                int sendIdOfNeighbor2 = sendEntry.getSendIdOf(neighbor2);

                FinishDeleteMessage finishDeleteMessageTo2 = new FinishDeleteMessage(largeId, helpedId, neighbor1, sendIdOfNeighbor1);
                FinishDeleteMessage finishDeleteMessageTo1 = new FinishDeleteMessage(largeId, helpedId, neighbor2, sendIdOfNeighbor2);

                this.sendDeleteBaseMessage(finishDeleteMessageTo2, largeId, sendEntry, neighbor2, helpedId, node);
                this.sendDeleteBaseMessage(finishDeleteMessageTo1, largeId, sendEntry, neighbor1, helpedId, node);


            }
            else if(ids.size() == 1){
                // no need to create any link
                int neighbor1 = ids.get(0);
                FinishDeleteMessage finishDeleteMessageTo1 = new FinishDeleteMessage(largeId, helpedId, -1, -1);
                this.sendDeleteBaseMessage(finishDeleteMessageTo1, largeId, sendEntry, neighbor1, helpedId, node);
            }
            else{
                Tools.warning("Very Interesting situation， no node connect to it!");
            }
        }

    }


}

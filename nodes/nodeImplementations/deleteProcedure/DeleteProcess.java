package projects.cbrenet.nodes.nodeImplementations.deleteProcedure;

// 放一些AuxiliaryNode 和 CBBSTNode 都需要的代码

import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteBaseMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteConfirmMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeletePrepareMessage;
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
        // todo 思考什么情况应该发送的问题


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

            // todo 思考这个结点确认了另一个没确认怎么办的问题。
            // 等吧

            DeleteConfirmMessage confirmMessage = new DeleteConfirmMessage(largeId, deleteTarget, helpedId);


            this.sendDeleteBaseMessage(confirmMessage, largeId, deleteEntry, deleteTarget, helpedId, node);

        }

    }


}

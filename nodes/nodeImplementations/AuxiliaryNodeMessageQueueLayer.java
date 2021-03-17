package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.SDNMessage.LargeInsertMessage;
import projects.cbrenet.nodes.messages.controlMessage.DeleteRequestMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteBaseMessage;
import projects.cbrenet.nodes.nodeImplementations.forwardSendHelper.MessageForwardAndSendHelper;
import projects.cbrenet.nodes.routeEntry.AuxiliarySendEntry;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

import java.util.*;

public abstract class AuxiliaryNodeMessageQueueLayer extends AuxiliaryNodeStructureLayer {


    protected void doInPostRound() {

        // clear queue part and delete condition check
        HashMap<Integer, HashMap<Integer, AuxiliarySendEntry>> routeTableTmp = this.getRouteTable();

        Set<Integer> helpedIds = routeTableTmp.keySet();

        for (int helpedId : helpedIds) {
            HashMap<Integer, AuxiliarySendEntry> sendEntryHashMap = routeTableTmp.getOrDefault(helpedId, null);
            if (sendEntryHashMap != null) {
                Set<Integer> largeIds = sendEntryHashMap.keySet();
                for (int largeId : largeIds) {
                    AuxiliarySendEntry entry = this.getCorrespondingEntry(helpedId, largeId);

                    // 这里可能会有bug,,但是Java的数据结构这么做似乎没有问题。
                    Queue<RoutingMessage> routingMessageQueue = entry.getRoutingMessageQueue();
                    Queue<RoutingMessage> sendFailedQueue = new LinkedList<>();
                    while (!routingMessageQueue.isEmpty()) {
                        RoutingMessage msgTmp = routingMessageQueue.poll();
                        if (!this.forwardMessage(msgTmp)) {
                            sendFailedQueue.add(msgTmp);
                        }
                    }
                    routingMessageQueue.addAll(sendFailedQueue);
                    if (entry.getRoutingMessageQueue().isEmpty()) {
                        if (!routingMessageQueue.isEmpty()) {
                            Tools.fatalError("In ANMQLayer, the queue in the entry is not equal to the queue " +
                                    "routingMessageQueue");
                        }
                        entry.setQueueEmpty(true);
                    }

                    // Check whether satisfy the delete condition
                    int egoIdOfLeft = entry.getEgoTreeIdOfLeftChild();
                    int egoIdOfRight = entry.getEgoTreeIdOfRightChild();
                    entry.setDeleteConditionSatisfied(this.checkDeleteCondition(egoIdOfLeft, egoIdOfRight));
                    // Part finished!

                }
            } else {
                Tools.fatalError("");
            }
        }
        // clear queue part and delete condition check


    }


    public void sendEgoTreeMessage(int largeId, int dst, Message msg, boolean upward,int helpedId){
        assert msg instanceof DeleteBaseMessage;

        RoutingMessage routingMessage = new RoutingMessage(this.ID, dst, msg, largeId, upward);

        if(!this.forwardMessage(routingMessage)){
            this.addRoutingMessageToQueue(helpedId, routingMessage);
        }
    }


    public void addRoutingMessageToQueue(int helpedID, RoutingMessage routingMessage) {
        int largeId = routingMessage.getLargeId();
        AuxiliarySendEntry entry = this.getCorrespondingEntry(helpedID, largeId);
        entry.addMessageIntoRoutingMessageQueue(routingMessage);
    }



    public boolean forwardMessage(RoutingMessage routingMessage) {
        /**
         *@description The auxiliary node use this to transfer message
         *@parameters [largeId, msg]
         *@return boolean
         *@author Zhang Hongxuan
         *@create time  2021/3/14
         */


        int helpedId = routingMessage.getNextHop();
        MessageForwardAndSendHelper helper = new MessageForwardAndSendHelper();

        // 为什么是helped id 呢？ 因为forwardMessage需要和第一位参数进行比较的来决定RoutingMessage该去哪的
        return helper.forwardMessage(helpedId, this, routingMessage);

    }
//
//        int destination = routingMessage.getDestination();
//
//        int largeId = routingMessage.getLargeId();
//        int helpedId = routingMessage.getNextHop();
//
//
//        if(largeId != -1){
//            // which means need to transfer in the large id's tree
//
//            // Very Important!!
//            boolean sendFlag = false;
//            // Note that every message can not send in this turn must store in the routingMessageQueue to
//            // send it in the next turn;
//
//
//            if(routingMessage.getSpecialHopFlag()){
//                // when nextHop flag is true, we need a special forward
//                int currentParentId = this.getParentOf(helpedId, largeId);
//                if(currentParentId != -1){
//
//                    if(currentParentId == routingMessage.getSpecialHop()){
//                        routingMessage.resetSpecialHop();
//                        return this.sendTo(currentParentId, routingMessage);
//                    }
//                    else{
//                        Tools.warning("We have to send a message according to the next hop, but the parent" +
//                                " still seems wrong! NextHop ID is not same as the parent ID!");
//                        return false;
//                    }
//                }
//                else{
//                    Tools.warning("We have to send a message according to the next hop, but the current parent" +
//                            "ID still seems wrong!");
//                    return false;
//                }
//            }
//
//
//
//            Message message = routingMessage.getPayLoad();
//
//            if(message instanceof CbRenetMessage){
//                if(!((CbRenetMessage) message).isUpForward()){
//                    // if not upForward, the message would send to the child
//                    if (helpedId < destination) {
//                        int rightChild = this.getRightChildOf(helpedId, largeId);
//                        if(sendTo(rightChild, routingMessage)){
//                            sendFlag = true;
//                        }
//                    } else if (destination < helpedId) {
//                        int leftChild = this.getLeftChildOf(helpedId, largeId);
//                        if(sendTo(leftChild, routingMessage)){
//                            sendFlag = true;
//                        }
//                    }
//                }
//                else{
//                    int parentId = this.getParentOf(helpedId, largeId);
//                    if(sendTo(parentId, routingMessage)){
//                        sendFlag = true;
//                    }
//                }
//            }
//            else if(message instanceof LargeInsertMessage){
//                LargeInsertMessage insertMessageTmp = (LargeInsertMessage) message;
//                int target = insertMessageTmp.getTarget();
//                if(target > this.ID){
//                    int rightChild = this.getRightChildOf(helpedId, largeId);
//                    if(this.sendTo(rightChild, routingMessage)){
//                        sendFlag = true;
//                    }
//                }
//                else{
//                    int leftChild = this.getLeftChildOf(helpedId, largeId);
//                    if(this.sendTo(leftChild, routingMessage)){
//                        sendFlag = true;
//                    }
//                }
//
//            }
//            else if(message instanceof DeleteRequestMessage){
//                DeleteRequestMessage deleteRequestMessageTmp = (DeleteRequestMessage) message;
//                if(this.ID != deleteRequestMessageTmp.getDst()){
//                    int parentId = this.getParentOf(helpedId, largeId);
//                    if(this.sendTo(parentId, routingMessage)){
//                        sendFlag = true;
//                    }
//                }
//            }
//            else if(message instanceof DeleteBaseMessage){
//                if(!routingMessage.isUpForward()){
//                    // if not upForward, the message would send to the child
//                    if (this.ID < destination) {
//                        int rightChild = this.getRightChildOf(helpedId,largeId);
//                        if(sendTo(rightChild, routingMessage)){
//                            sendFlag = true;
//                        }
//                    } else if (destination < ID) {
//                        int leftChild = this.getLeftChildOf(helpedId,largeId);
//                        if(sendTo(leftChild, routingMessage)){
//                            sendFlag = true;
//                        }
//                    }
//                }
//                else{
//                    int parentId = this.getParentOf(helpedId,largeId);
//                    if(sendTo(parentId, routingMessage)){
//                        sendFlag = true;
//                    }
//                }
//            }
//            else{
//                Tools.fatalError("Some message in the RoutingMessage is " + message.getClass());
//            }
//
//
//            if(!sendFlag){
//                // can not send for some reason
//                Tools.warning("A routing message contains " + message.getClass() + " can not send for some reason " +
//                        "has been add into rMQ");
//            }
//            return sendFlag;
//        }
//        else{
//            Tools.fatalError("A RoutingMessage do not have largeId but sent to AuxiliaryNode!");
//            return false;
//        }
//    }
}

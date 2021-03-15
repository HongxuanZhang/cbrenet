package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.SDNMessage.LargeInsertMessage;
import projects.cbrenet.nodes.messages.controlMessage.DeleteRequestMessage;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

import java.util.*;

public abstract class AuxiliaryNodeMessageQueueLayer extends AuxiliaryNodeStructureLayer {


    private HashMap<Integer, Queue<RoutingMessage>> routingMessageQueues;
    // Organized by the Helped Id.


    protected void doInPostRound(){

        // clear queue part

        // todo 是否需要增加有关相关entry中的queue空不空的部分, 用以删除所用？？
        Set<Integer> idKeys = this.routingMessageQueues.keySet();
        for(int id : idKeys){
            Queue<RoutingMessage> routingMessageQueue = this.routingMessageQueues.get(id);
            Queue<RoutingMessage> sendFailedQueue = new LinkedList<>();
            while(!routingMessageQueue.isEmpty()){
                RoutingMessage msgTmp = routingMessageQueue.poll();
                if(!this.forwardMessage(msgTmp)){
                    sendFailedQueue.add(msgTmp);
                }
            }
            routingMessageQueue.addAll(sendFailedQueue);
            routingMessageQueues.put(id, routingMessageQueue);
        }


    }


    public Queue<RoutingMessage> getRoutingMessageQueueOf(int helpedID){
        return this.routingMessageQueues.getOrDefault(helpedID, null);
    }

    public void addRoutingMessageToQueue(int helpedID, RoutingMessage routingMessage){
        Queue<RoutingMessage> msgQueueTmp = this.getRoutingMessageQueueOf(helpedID);
        if(msgQueueTmp == null){
            msgQueueTmp = new LinkedList<>();
        }
        msgQueueTmp.add(routingMessage);
        this.routingMessageQueues.put(helpedID, msgQueueTmp);
    }


    public boolean forwardMessage(RoutingMessage routingMessage) {
        /**
         *@description The auxiliary node use this to transfer message
         *@parameters  [largeId, msg]
         *@return  boolean
         *@author  Zhang Hongxuan
         *@create time  2021/3/14
         */


        int destination = routingMessage.getDestination();

        int largeId = routingMessage.getLargeId();
        int helpedId = routingMessage.getNextHop();


        if(largeId != -1){
            // which means need to transfer in the large id's tree

            // Very Important!!
            boolean sendFlag = false;
            // Note that every message can not send in this turn must store in the routingMessageQueue to
            // send it in the next turn;


            if(routingMessage.getSpecialHopFlag()){
                // when nextHop flag is true, we need a special forward
                int currentParentId = this.getParentOf(helpedId, largeId);
                if(currentParentId != -1){

                    if(currentParentId == routingMessage.getSpecialHop()){
                        routingMessage.resetSpecialHop();
                        return this.sendTo(currentParentId, routingMessage);
                    }
                    else{
                        Tools.warning("We have to send a message according to the next hop, but the parent" +
                                " still seems wrong! NextHop ID is not same as the parent ID!");
                        return false;
                    }
                }
                else{
                    Tools.warning("We have to send a message according to the next hop, but the current parent" +
                            "ID still seems wrong!");
                    return false;
                }
            }



            Message message = routingMessage.getPayLoad();

            if(message instanceof CbRenetMessage){
                if(!((CbRenetMessage) message).isUpForward()){
                    // if not upForward, the message would send to the child
                    if (helpedId < destination) {
                        int rightChild = this.getRightChildOf(helpedId, largeId);
                        if(sendTo(rightChild, routingMessage)){
                            sendFlag = true;
                        }
                    } else if (destination < helpedId) {
                        int leftChild = this.getLeftChildOf(helpedId, largeId);
                        if(sendTo(leftChild, routingMessage)){
                            sendFlag = true;
                        }
                    }
                }
                else{
                    int parentId = this.getParentOf(helpedId, largeId);
                    if(sendTo(parentId, routingMessage)){
                        sendFlag = true;
                    }
                }
            }
            else if(message instanceof LargeInsertMessage){
                LargeInsertMessage insertMessageTmp = (LargeInsertMessage) message;
                int target = insertMessageTmp.getTarget();
                if(target > this.ID){
                    int rightChild = this.getRightChildOf(helpedId, largeId);
                    if(this.sendTo(rightChild, routingMessage)){
                        sendFlag = true;
                    }
                }
                else{
                    int leftChild = this.getLeftChildOf(helpedId, largeId);
                    if(this.sendTo(leftChild, routingMessage)){
                        sendFlag = true;
                    }
                }

            }
            else if(message instanceof DeleteRequestMessage){
                DeleteRequestMessage deleteRequestMessageTmp = (DeleteRequestMessage) message;
                if(this.ID != deleteRequestMessageTmp.getDst()){
                    int parentId = this.getParentOf(helpedId, largeId);
                    if(this.sendTo(parentId, routingMessage)){
                        sendFlag = true;
                    }
                }
            }
            else{
                Tools.fatalError("Some message in the RoutingMessage is " + message.getClass());
            }


            if(!sendFlag){
                // can not send for some reason
                Tools.warning("A routing message contains " + message.getClass() + " can not send for some reason " +
                        "has been add into rMQ");
            }
            return sendFlag;
        }
        else{
            Tools.fatalError("A RoutingMessage do not have largeId but sent to AuxiliaryNode!");
            return false;
        }
    }
}

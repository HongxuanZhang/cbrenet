package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.SDNMessage.LargeInsertMessage;
import projects.cbrenet.nodes.messages.controlMessage.DeleteRequestMessage;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public abstract class AuxiliaryNodeMessageQueueLayer extends AuxiliaryNodeStructureLayer {


    protected boolean forwardMessage(RoutingMessage routingMessage) {
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
                        this.sendTo(currentParentId, routingMessage);
                        return true;
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
                    if(sendTo(leftChild, routingMessage)){
                        sendFlag = true;
                    }
                }

            }
            else if(message instanceof DeleteRequestMessage){
                DeleteRequestMessage deleteRequestMessageTmp = (DeleteRequestMessage) message;
                if(this.ID != deleteRequestMessageTmp.getDst()){
                    int parentId = this.getParentOf(helpedId, largeId);
                    if(sendTo(parentId, routingMessage)){
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
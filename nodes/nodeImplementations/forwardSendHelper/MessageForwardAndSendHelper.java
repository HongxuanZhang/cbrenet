package projects.cbrenet.nodes.nodeImplementations.forwardSendHelper;

import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.SDNMessage.LargeInsertMessage;
import projects.cbrenet.nodes.messages.controlMessage.DeleteRequestMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteBaseMessage;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class MessageForwardAndSendHelper  {


    // Call different EntryGetter instance to execute in AN and BST N differently.
    public boolean forwardMessage(int ID, EntryGetter entryGetter, RoutingMessage routingMessage) {

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
                int currentParentId = entryGetter.getParentOf(helpedId, largeId);
                if(currentParentId != -1){

                    if(currentParentId == routingMessage.getSpecialHop()){
                        routingMessage.resetSpecialHop();
                        return entryGetter.sendTo(currentParentId, routingMessage);
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
                if(!routingMessage.isUpForward()){
                    // if not upForward, the message would send to the child
                    if (helpedId < destination) {
                        int rightChild = entryGetter.getRightChildOf(helpedId, largeId);
                        if(entryGetter.sendTo(rightChild, routingMessage)){
                            sendFlag = true;
                        }
                    } else if (destination < helpedId) {
                        int leftChild = entryGetter.getLeftChildOf(helpedId, largeId);
                        if(entryGetter.sendTo(leftChild, routingMessage)){
                            sendFlag = true;
                        }
                    }
                }
                else{
                    int parentId = entryGetter.getParentOf(helpedId, largeId);
                    if(entryGetter.sendTo(parentId, routingMessage)){
                        sendFlag = true;
                    }
                }
            }
            else if(message instanceof LargeInsertMessage){
                LargeInsertMessage insertMessageTmp = (LargeInsertMessage) message;
                int target = insertMessageTmp.getTarget();
                if(target > ID){
                    int rightChild = entryGetter.getRightChildOf(helpedId, largeId);
                    if(entryGetter.sendTo(rightChild, routingMessage)){
                        sendFlag = true;
                    }
                }
                else{
                    int leftChild = entryGetter.getLeftChildOf(helpedId, largeId);
                    if(entryGetter.sendTo(leftChild, routingMessage)){
                        sendFlag = true;
                    }
                }

            }
            else if(message instanceof DeleteRequestMessage){
                DeleteRequestMessage deleteRequestMessageTmp = (DeleteRequestMessage) message;
                if(ID != deleteRequestMessageTmp.getDst()){
                    int parentId = entryGetter.getParentOf(helpedId, largeId);
                    if(entryGetter.sendTo(parentId, routingMessage)){
                        sendFlag = true;
                    }
                }
            }
            else if(message instanceof DeleteBaseMessage){
                if(!routingMessage.isUpForward()){
                    // if not upForward, the message would send to the child
                    if (ID < destination) {
                        int rightChild = entryGetter.getRightChildOf(helpedId,largeId);
                        if(entryGetter.sendTo(rightChild, routingMessage)){
                            sendFlag = true;
                        }
                    } else if (destination < ID) {
                        int leftChild = entryGetter.getLeftChildOf(helpedId,largeId);
                        if(entryGetter.sendTo(leftChild, routingMessage)){
                            sendFlag = true;
                        }
                    }
                }
                else{
                    int parentId = entryGetter.getParentOf(helpedId,largeId);
                    if(entryGetter.sendTo(parentId, routingMessage)){
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

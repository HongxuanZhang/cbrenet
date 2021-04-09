package projects.cbrenet.nodes.nodeImplementations.nodeHelper;

import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.SDNMessage.LargeInsertMessage;
import projects.cbrenet.nodes.messages.controlMessage.DeleteRequestMessage;
import projects.cbrenet.nodes.messages.controlMessage.clusterMessage.AcceptOrRejectBaseMessage;
import projects.cbrenet.nodes.messages.controlMessage.clusterMessage.ClusterRelatedMessage;
import projects.cbrenet.nodes.messages.controlMessage.clusterMessage.RequestClusterMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteBaseMessage;
import projects.cbrenet.nodes.routeEntry.AuxiliarySendEntry;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class MessageForwardHelper {

    // Call different EntryGetter instance to execute in AN and BST N differently.
    public boolean forwardMessage(int ID, EntryGetter entryGetter, RoutingMessage routingMessage) {

        int destination = routingMessage.getDestination();
        int largeId = routingMessage.getLargeId();
        int helpedId = routingMessage.getNextHop();




        if(largeId >=0 ){
            // which means need to transfer in the large id's tre


            // Very Important!!
            boolean alreadySendFlag = false;
            // Note that every message can not send in this turn must store in the routingMessageQueue to
            // send it in the next turn;


            if(ID == largeId){
                // the sender must set Next Hop already
                return entryGetter.sendTo(helpedId, routingMessage);
            }


            // Special hop do not add weight
            if(routingMessage.getSpecialHopFlag()){
                // when nextHop flag is true, we need a special forward
                // special forward: direct send it to the parent and reset special hop flag
                int currentParentId = entryGetter.getEgoTreeIdOfParent(helpedId, largeId);
                if(currentParentId != -1){

                    if(currentParentId == routingMessage.getSpecialHop()){
                        routingMessage.resetSpecialHop();

                        System.out.println("Node " + ID + " send a routing message to " + currentParentId);

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
                        int rightChild = entryGetter.getEgoTreeIdOfRightChild(helpedId, largeId);
                        System.out.println("Node " + ID + " send a routing message to right child " + rightChild + " in" +
                                " egoTree(" + largeId + ") to help " + helpedId);

                        if(entryGetter.sendTo(rightChild, routingMessage)){
                            alreadySendFlag = true;
                        }
                        if(alreadySendFlag){
                            SendEntry entry = entryGetter.getCorrespondingEntry(helpedId, largeId);
                            if(entry instanceof AuxiliarySendEntry){
                                 // not weight change
                            }
                            else{
                                entry.incrementWeightOfRight();
                            }
                        }
                    } else if (destination < helpedId) {
                        int leftChild = entryGetter.getEgoTreeIdOfLeftChild(helpedId, largeId);
                        System.out.println("Node " + ID + " send a routing message to left child " + leftChild + " in" +
                                " egoTree(" + largeId + ") to help " + helpedId);

                        if(entryGetter.sendTo(leftChild, routingMessage)){
                            alreadySendFlag = true;
                        }

                        if(alreadySendFlag){
                            SendEntry entry = entryGetter.getCorrespondingEntry(helpedId, largeId);
                            if(entry instanceof AuxiliarySendEntry){
                                // not weight change
                            }
                            else{
                                entry.incrementWeightOfLeft();
                            }
                        }

                    }
                }
                else{
                    // 从下向上发来的消息，自然是Receive时统计啦
                    int parentId = entryGetter.getEgoTreeIdOfParent(helpedId, largeId);
                    System.out.println("Node " + ID + " send a routing message to parent " + parentId + " in" +
                            " egoTree(" + largeId + ") to help "+ helpedId);
                    if(entryGetter.sendTo(parentId, routingMessage)){
                        alreadySendFlag = true;
                    }
                }

                System.out.println(routingMessage.toString());

            }
            else if(message instanceof LargeInsertMessage){
                LargeInsertMessage insertMessageTmp = (LargeInsertMessage) message;
                int target = insertMessageTmp.getTarget();
                if(target > helpedId){
                    int rightChild = entryGetter.getEgoTreeIdOfRightChild(helpedId, largeId);
                    if(entryGetter.sendTo(rightChild, routingMessage)){
                        alreadySendFlag = true;
                    }
                }
                else{
                    int leftChild = entryGetter.getEgoTreeIdOfLeftChild(helpedId, largeId);
                    if(entryGetter.sendTo(leftChild, routingMessage)){
                        alreadySendFlag = true;
                    }
                }

            }
            else if(message instanceof DeleteRequestMessage){
                DeleteRequestMessage deleteRequestMessageTmp = (DeleteRequestMessage) message;
                if(ID != deleteRequestMessageTmp.getDst()){
                    int parentId = entryGetter.getEgoTreeIdOfParent(helpedId, largeId);
                    if(entryGetter.sendTo(parentId, routingMessage)){
                        alreadySendFlag = true;
                    }
                }
            }
            else if(message instanceof DeleteBaseMessage){
                if(!routingMessage.isUpForward()){
                    // if not upForward, the message would send to the child
                    if (helpedId < destination) {
                        int rightChild = entryGetter.getEgoTreeIdOfRightChild(helpedId,largeId);
                        if(entryGetter.sendTo(rightChild, routingMessage)){
                            alreadySendFlag = true;
                        }
                    } else if (destination < helpedId) {
                        int leftChild = entryGetter.getEgoTreeIdOfLeftChild(helpedId,largeId);
                        if(entryGetter.sendTo(leftChild, routingMessage)){
                            alreadySendFlag = true;
                        }
                    }
                }
                else{
                    int parentId = entryGetter.getEgoTreeIdOfParent(helpedId,largeId);
                    if(entryGetter.sendTo(parentId, routingMessage)){
                        alreadySendFlag = true;
                    }
                }
            }
            else if(message instanceof ClusterRelatedMessage){
                if(message instanceof RequestClusterMessage){
                    RequestClusterMessage requestClusterMessageTmp = (RequestClusterMessage) message;

                    boolean upForward = routingMessage.isUpForward();
                    if(!upForward){
                        Tools.fatalError("RequestClusterMessage is not up forward !");
                    }
                    else{
                        int parentId = entryGetter.getEgoTreeIdOfParent(helpedId, largeId);
                        if(entryGetter.sendTo(parentId, routingMessage)){
                            alreadySendFlag = true;
                        }
                    }

                }
                else if(message instanceof AcceptOrRejectBaseMessage){
                    AcceptOrRejectBaseMessage acceptOrRejectBaseMessage = (AcceptOrRejectBaseMessage) message;

                    boolean upForward = routingMessage.isUpForward();
                    if(upForward){
                        int parentId = entryGetter.getEgoTreeIdOfParent(helpedId, largeId);
                        if(entryGetter.sendTo(parentId, routingMessage)){
                            alreadySendFlag = true;
                        }
                    }
                    else{

                        if (helpedId < destination) {
                            int rightChild = entryGetter.getEgoTreeIdOfRightChild(helpedId, largeId);
                            System.out.println("Node " + ID + " send a routing message contains" +
                                    " ClusterRelatedMessage to right child " + rightChild + " in" +
                                    " egoTree(" + largeId + ") to help " + helpedId);

                            if(entryGetter.sendTo(rightChild, routingMessage)){
                                alreadySendFlag = true;
                            }
                        } else if (destination < helpedId) {
                            int leftChild = entryGetter.getEgoTreeIdOfLeftChild(helpedId, largeId);
                            System.out.println("Node " + ID + " send a routing message contains" +
                                    " ClusterRelatedMessage to left child " + leftChild + " in" +
                                    " egoTree(" + largeId + ") to help " + helpedId);

                            if(entryGetter.sendTo(leftChild, routingMessage)){
                                alreadySendFlag = true;
                            }
                        }
                    }
                }
                else{
                    Tools.warning("Message Forwarder: Another type of ClusterRelatedMessage's " + message.getClass().getSimpleName());
                }


            }

            else{
                Tools.fatalError("MessageForwarder: Some message in the RoutingMessage is " + message.getClass());
            }


            if(!alreadySendFlag){
                // can not send for some reason
                Tools.warning("A routing message contains " + message.getClass() + " can not send for some reason " +
                        "has been add into rMQ");
            }
            return alreadySendFlag;
        }
        else{
            Tools.fatalError("A RoutingMessage do not have largeId but sent to AuxiliaryNode!");
            return false;
        }
    }

}

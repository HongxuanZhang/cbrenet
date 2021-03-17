package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.controlMessage.DeleteRequestMessage;
import projects.cbrenet.nodes.messages.SDNMessage.LargeInsertMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteBaseMessage;
import projects.cbrenet.nodes.nodeImplementations.forwardSendHelper.MessageForwardAndSendHelper;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Only used to deal with the message */

public abstract class MessageQueueLayer extends CounterBasedBSTLayer{

    private Queue<Message> messageQueue; // used by CbRenetMessage or other that do not need Ego tree

    public void addInRoutingMessageQueue(RoutingMessage routingMessage){
        /**
         *@description called it when forwardMessage return false;
         *@parameters  [routingMessage]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/11
         */
        int largeId = routingMessage.getLargeId();
        SendEntry entry = this.getSendEntryOf(largeId);
        if(entry == null){
            Tools.fatalError("Something terrible happened! Want to add Routing message into the queue but " +
                    "the corresponding entry is NULL!!!");
            return;
        }
        entry.addMessageIntoRoutingMessageQueue(routingMessage);
    }


    @Override
    public void init(){
        super.init();

        messageQueue = new LinkedList<>();
    }


    protected void doInPostRound(){
        super.doInPostRound();
        this.sendMessageInQueue();
    }


    // Send message which in the Queue
    private void sendMessageInQueue(){
        /**
         *@description //todo call this in the EVERY postRound
         *@parameters  []
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/1
         */
        this.sendMessageWaitingInMessageQueue();
        this.sendMessageWaitingInRoutingQueue();
    }

    private void sendMessageWaitingInMessageQueue(){
        Queue<Message> messageQueueTmp = new LinkedList<>();
        while(!this.messageQueue.isEmpty()){
            Message message = this.messageQueue.poll();

            // remove CP test here.
            if(message instanceof CbRenetMessage){
                int dst = ((CbRenetMessage)message).getDst();
                if(this.outgoingConnections.contains(this, Tools.getNodeByID(dst))){
                    this.send(message, Tools.getNodeByID(dst));
                }
                else{
                    messageQueueTmp.add(message);
                }
            }
            else{
                Tools.fatalError("Message class MISSING! Add " + message.getClass() + " into MessageQueueLayer" +
                        " sendMessageInMessageQueue" );
            }

        }
        this.messageQueue.addAll(messageQueueTmp);
    }

    private void sendMessageWaitingInRoutingQueue(){
        /**
         *@description
         *@parameters  []
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/1
         */

        Set<Integer> largeIds = this.routeTable.keySet();

        for(int largeId : largeIds){

            SendEntry correspondingEntry = this.getSendEntryOf(largeId);
            Queue<RoutingMessage> routingMessageQueue = correspondingEntry.getRoutingMessageQueue();

            Queue<RoutingMessage> routingMessageQueueTmp = new LinkedList<>();
            while(!routingMessageQueue.isEmpty())
            {
                RoutingMessage routingMessage = routingMessageQueue.poll();
                assert routingMessage != null;
                if(!this.forwardMessage(routingMessage)){
                    routingMessageQueueTmp.add(routingMessage);
                }
            }
            routingMessageQueue.addAll(routingMessageQueueTmp);


        }



    }


    @Override
    protected boolean forwardMessage(RoutingMessage routingMessage) {
        /**
         *@description this method only use to transfer message in the ego-tree of the large node,
         * called when the node want to transfer a message
         *@parameters [largeId, routingMessage]
         *@return void
         *@author Zhang Hongxuan
         *@create time  2021/2/7
         */
        MessageForwardAndSendHelper helper = new MessageForwardAndSendHelper();
        return helper.forwardMessage(this.ID, this, routingMessage);
    }

//        int largeId = routingMessage.getLargeId();
//        int destination = routingMessage.getDestination();
//
//        if(this.ID == destination )
//        {
//            Tools.warning("A message received in forwardMessage : " + routingMessage.getPayLoad().getClass() );
//            this.receiveMessage(routingMessage.getPayLoad());
//            return true;
//        }
//
//        if(this.ID == largeId){
//            // if the sender is LN, it should transfer the rt routingMessage directly to the root of the ego-tree
//            int rootId = this.getRootNodeId();
//            if(rootId != -1){
//                if(this.sendTo(rootId, routingMessage)){
//                    return true;
//                }
//            }
//            Tools.warning("A routing message can not send from the LN to its root for some reason " +
//                        "has been add into rMQ");
//            return false;
//        }
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
//
//                int currentParentId = this.getParentOf(-1,largeId);
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
//            Message message = routingMessage.getPayLoad();
//
//            if(message instanceof CbRenetMessage){
//                if(!((CbRenetMessage) message).isUpForward()){
//                    // if not upForward, the message would send to the child
//                    if (this.ID < destination) {
//                        int rightChild = this.getRightChildOf(-1,largeId);
//                        if(sendTo(rightChild, routingMessage)){
//                            sendFlag = true;
//                        }
//                    } else if (destination < ID) {
//                        int leftChild = this.getLeftChildOf(-1,largeId);
//                        if(sendTo(leftChild, routingMessage)){
//                            sendFlag = true;
//                        }
//                    }
//                }
//                else{
//                    int parentId = this.getParentOf(-1,largeId);
//                    if(sendTo(parentId, routingMessage)){
//                        sendFlag = true;
//                    }
//                }
//            }
//            else if(message instanceof LargeInsertMessage){
//                LargeInsertMessage insertMessageTmp = (LargeInsertMessage) message;
//                int target = insertMessageTmp.getTarget();
//                if(target > this.ID){
//                    int rightChild = this.getRightChildOf(-1,largeId);
//                    if(this.sendTo(rightChild, routingMessage)){
//                        sendFlag = true;
//                    }
//                }
//                else{
//                    int leftChild = this.getLeftChildOf(-1,largeId);
//                    if(this.sendTo(leftChild, routingMessage)){
//                        sendFlag = true;
//                    }
//                }
//            }
//            else if(message instanceof DeleteRequestMessage){
//                DeleteRequestMessage deleteRequestMessageTmp = (DeleteRequestMessage) message;
//                if(this.ID != deleteRequestMessageTmp.getDst()){
//                    int parentId = this.getParentOf(-1,largeId);
//                    if(this.sendTo(parentId, routingMessage)){
//                        sendFlag = true;
//                    }
//                }
//            }
//            else if(message instanceof DeleteBaseMessage){
//                if(!routingMessage.isUpForward()){
//                    // if not upForward, the message would send to the child
//                    if (this.ID < destination) {
//                        int rightChild = this.getRightChildOf(-1,largeId);
//                        if(sendTo(rightChild, routingMessage)){
//                            sendFlag = true;
//                        }
//                    } else if (destination < ID) {
//                        int leftChild = this.getLeftChildOf(-1,largeId);
//                        if(sendTo(leftChild, routingMessage)){
//                            sendFlag = true;
//                        }
//                    }
//                }
//                else{
//                    int parentId = this.getParentOf(-1,largeId);
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
//            Tools.warning("A routing message not go in the ego tree, but send to the destination!");
//            return false;
//        }
//    }




    // getter
    public Queue<Message> getMessageQueue(){
        return this.messageQueue;
    }


}

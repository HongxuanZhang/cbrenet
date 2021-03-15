package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.controlMessage.DeleteRequestMessage;
import projects.cbrenet.nodes.messages.SDNMessage.LargeInsertMessage;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Only used to deal with the message */

public abstract class MessageQueueLayer extends CounterBasedBSTLayer{

    private Queue<RoutingMessage> routingMessageQueue; // Only used for RoutingMessage in the Ego-Tree

    private Queue<Message> messageQueue; // used by CbRenetMessage or other that do not need Ego tree


    public void addInRoutingMessageQueue(RoutingMessage routingMessage){
        /**
         *@description called it when forwardMessage return false;
         *@parameters  [routingMessage]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/11
         */
        this.routingMessageQueue.add(routingMessage);
    }


    @Override
    public void init(){
        super.init();
        routingMessageQueue = new LinkedList<>();
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
        this.sendMessageInMessageQueue();
        this.sendMessageInWaitingRoutingQueue();
    }

    private void sendMessageInMessageQueue(){
        Queue<Message> messageQueueTmp = new LinkedList<>();
        while(!this.messageQueue.isEmpty()){
            Message message = this.messageQueue.poll();

            // if a node has message to send but can not pass CP test,
            // Then there may be bug in the code

//            if(this.checkCommunicateSatisfaction(message)){
//                if(message instanceof CbRenetMessage){
//                    int dst = ((CbRenetMessage)message).getDst();
//                    this.send(message, Tools.getNodeByID(dst));
//                }
//                else{
//                    Tools.fatalError("Message class MISSING! Add " + message.getClass() + " into MessageQueueLayer" +
//                            " sendMessageInMessageQueue" );
//                }
//            }
//            else{
//                messageQueueTmp.add(message);
//            }

            // remove CP test here.
            if(message instanceof CbRenetMessage){
                int dst = ((CbRenetMessage)message).getDst();
                this.send(message, Tools.getNodeByID(dst));
            }
            else{
                Tools.fatalError("Message class MISSING! Add " + message.getClass() + " into MessageQueueLayer" +
                        " sendMessageInMessageQueue" );
            }

        }
        this.messageQueue.addAll(messageQueueTmp);
    }

    private void sendMessageInWaitingRoutingQueue(){
        /**
         *@description
         *@parameters  []
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/1
         */
        Queue<RoutingMessage> routingMessageQueueTmp = new LinkedList<>();
        while(!routingMessageQueue.isEmpty())
        {
            RoutingMessage message = this.routingMessageQueue.poll();
            int dst = message.getDestination();
            int largeId = message.getLargeId();
            if(!this.forwardMessage(largeId, message)){
                routingMessageQueueTmp.add(message);
            }
        }

        routingMessageQueue.addAll(routingMessageQueueTmp);

    }


    // Todo 从ForwardMessage中删除LargeId.
    @Override
    protected boolean forwardMessage(int largeId, RoutingMessage routingMessage) {
        /**
         *@description this method only use to transfer message in the ego-tree of the large node,
         * called when the node want to transfer a message
         *@parameters  [largeId, routingMessage]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/7
         */
        int destination = routingMessage.getDestination();
        if(this.ID == destination )
        {
            Tools.warning("A message received in forwardMessage : " + routingMessage.getPayLoad().getClass() );
            this.receiveMessage(routingMessage.getPayLoad());
            return true;
        }

        if(this.ID == largeId){
            // if the sender is LN, it should transfer the rt routingMessage directly to the root of the ego-tree
            int rootId = this.getRootNodeId();
            if(rootId != -1){
                if(this.outgoingConnections.contains(this, Tools.getNodeByID(rootId))){
                    this.send(routingMessage, Tools.getNodeByID(rootId));
                    return true;
                }
            }

            Tools.warning("A routing message can not send from the LN to its root for some reason " +
                        "has been add into rMQ");
            return false;
        }

        if(largeId != -1){
            // which means need to transfer in the large id's tree

            // Very Important!!
            boolean sendFlag = false;
            // Note that every message can not send in this turn must store in the routingMessageQueue to
            // send it in the next turn;

            // Todo 适配 sendTo  IMPORTANT !!!!!

            if(routingMessage.getSpecialHopFlag()){
                // when nextHop flag is true, we need a special forward

                int currentParentId = this.getParent(largeId);
                if(currentParentId != -1){
                    // 上面这里最好不要用到，因为会导致灾难性的后果。旋转后，还请阻塞一下！！
                    // 不要用！！ 也没必要用，阻塞住即可！ 阻塞一个round就行了！

                    if(currentParentId == routingMessage.getSpecialHop()){
                        routingMessage.resetSpecialHop();
                        return this.sendToParent(largeId, routingMessage);
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
                    if (this.ID < destination) {
                        if(sendToRightChild(largeId, routingMessage)){
                            sendFlag = true;
                        }
                    } else if (destination < ID) {
                        if(sendToLeftChild(largeId, routingMessage)){
                            sendFlag = true;
                        }
                    }
                }
                else{
                    if(sendToParent(largeId, routingMessage)){
                        sendFlag = true;
                    }
                }
            }
            else if(message instanceof LargeInsertMessage){
                LargeInsertMessage insertMessageTmp = (LargeInsertMessage) message;
                int target = insertMessageTmp.getTarget();
                if(target > this.ID){
                    if(this.sendToRightChild(largeId, routingMessage)){
                        sendFlag = true;
                    }
                }
                else{
                    if(this.sendToLeftChild(largeId, routingMessage)){
                        sendFlag = true;
                    }
                }

            }
            else if(message instanceof DeleteRequestMessage){
                DeleteRequestMessage deleteRequestMessageTmp = (DeleteRequestMessage) message;
                if(this.ID != deleteRequestMessageTmp.getDst()){
                    if(sendToParent(largeId, routingMessage)){
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
            Tools.warning("A routing message not go in the ego tree, but send to the destination!");
            send(routingMessage, Tools.getNodeByID(destination));
            return true;
        }
    }




    // getter
    public Queue<RoutingMessage> getRoutingMessageQueue(){
        return this.routingMessageQueue;
    }

    public Queue<Message> getMessageQueue(){
        return this.messageQueue;
    }


}

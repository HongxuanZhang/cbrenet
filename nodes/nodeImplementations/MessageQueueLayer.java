package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.tableEntry.Request;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Only used in the ego-tree. */

public abstract class MessageQueueLayer extends CounterBasedBSTLayer{

    private Queue<RoutingMessage> routingMessageQueue;

    private Queue<Message> messageQueue; // used by CbRenetMessage or other that do not need Ego tree

    @Override
    public void init(){
        super.init();
        routingMessageQueue = new LinkedList<>();
        messageQueue = new LinkedList<>();
    }


    // Send message which in the Queue
    public void sendMessageInQueue(){
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
            if(this.checkCommunicateSatisfaction(message)){
                if(message instanceof CbRenetMessage){
                    int dst = ((CbRenetMessage)message).getDst();
                    this.send(message, Tools.getNodeByID(dst));
                }
                else{
                    Tools.fatalError("Message class MISSING! Add " + message.getClass() + " into MessageQueueLayer" +
                            " sendMessageInMessageQueue" );
                }
            }
            else{
                messageQueueTmp.add(message);
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
            if(this.checkCommunicateSatisfaction(this.ID, dst)){
                this.forwardMessage(largeId, message);
            }
            else{
                routingMessageQueueTmp.add(message);
            }
        }

        routingMessageQueue.addAll(routingMessageQueueTmp);

    }


    // 变成Message之后再进行Check 是绝对可行的！

    // send message part,
    // The first one : s-s l-l
    // The second one : l-s s-l

    public void sendNonEgoTreeMessage(int dst, Message msg){
        if(this.checkCommunicateSatisfaction(this.ID, dst)){
            this.send(msg, Tools.getNodeByID(dst));
        }
        else{
            this.messageQueue.add(msg);
        }
    }

    @Override
    public void sendEgoTreeMessage(int largeId, int dst, Message msg){
        /**
         *@description The method only used to send a message in the ego-tree
         *          *  The method would generate a routing message to wrap up the msg
         *
         *          IMPORTANT!!! DO NOT use it to forward message
         *
         *          Only called by the sender!
         *
         *          would not cause RoutingMessage blocked in the Ego-Tree.
         *
         *@parameters  [largeId, dst, msg]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/1
         */
        if (dst == ID) {
            this.receiveMessage(msg);
            return;
        }
        boolean upForward = false;
        if(!isNodeSmall(dst)){
            upForward = true;
        }



        RoutingMessage routingMessage = new RoutingMessage(ID, dst, msg, largeId, upForward);

        //  check 是否满足发送
        if(this.checkCommunicateSatisfaction(this.ID, dst)){
            this.forwardMessage(largeId, routingMessage);
        }
        else{
            this.routingMessageQueue.add(routingMessage);
        }
    }




    public boolean checkCommunicateSatisfaction(int src, int dst){
        if(src != this.ID) {
            Tools.fatalError("This method must be called in a wrong way, the parameter src must equal to the ID of the " +
                    "node");
            return false;
        }
        else return this.getCommunicateSmallNodes().containsKey(dst) || this.getCommunicateLargeNodes().containsKey(dst);
    }

    public boolean checkCommunicateSatisfaction(Request request){
        int src = request.srcId;
        int dst = request.dstId;
        return checkCommunicateSatisfaction(src,dst);
    }

    public boolean checkCommunicateSatisfaction(Message message)
    {
        int src;
        int dst;
        if(message instanceof CbRenetMessage){
            CbRenetMessage messageTmp = (CbRenetMessage) message;
            src = messageTmp.getSrc();
            dst = messageTmp.getDst();
        }
        else{
            Tools.fatalError("Message class MISSING! Add " + message.getClass() + " into MessageQueueLayer" );
            src = -100; // Whatever, make sure check won't return true;
            dst = -100;
        }
        return checkCommunicateSatisfaction(src,dst);
    }

}

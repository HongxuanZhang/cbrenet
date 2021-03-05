package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.SDNMessage.RequestMessage;
import projects.cbrenet.nodes.messages.controlMessage.DeleteRequestMessage;
import projects.cbrenet.nodes.messages.controlMessage.LargeInsertMessage;
import projects.cbrenet.nodes.tableEntry.Request;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Only used in the ego-tree. */

public abstract class MessageQueueLayer extends CounterBasedBSTLayer{

    private Random rand = Tools.getRandomNumberGenerator(); // used to generate priority of the CbRenetMessage

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
    public boolean sendMessageAccordingToRequest(Request request)
    {
        /**
         *@description This method is used to deal with request.
         *               It call sendEgoTreeMessage and sendNonEgoTreeMessage
         *@parameters  [request]
         *@return  boolean
         *@author  Zhang Hongxuan
         *@create time  2021/3/5
         */
        // 过渡态中的结点是不可能通过CP检测的
        if(!this.checkCommunicateSatisfaction(request)){
            // todo，现在是一直会告诉SDN，这样真的好吗？
            this.tellSDNUnsatisfiedRequest(request);
            return false;
        }

        boolean dstLargeCPFlag = this.getCommunicateLargeNodes().containsKey(request.dstId);

        double priority = Global.currentTime + rand.nextDouble();

        if(dstLargeCPFlag){
            if(this.largeFlag){
                // l - l
                CbRenetMessage message = new CbRenetMessage(request.srcId, request.dstId,
                        priority);
                this.sendNonEgoTreeMessage(request.dstId, message);
            }
            else{
                // s - l
                //int src, int dst, boolean largeIdFlag, int largeId, double priority,
                //                          boolean upForward
                int largeId = request.dstId;
                CbRenetMessage message = new CbRenetMessage(request.srcId, request.dstId, true, largeId,
                        priority, true);
                this.sendEgoTreeMessage(largeId, request.dstId,message);
            }
        }
        else{
            if(this.largeFlag){
                // l - s
                int largeId = request.srcId;
                CbRenetMessage message = new CbRenetMessage(request.srcId, request.dstId, true, largeId,
                        priority, false);
                this.sendEgoTreeMessage(largeId, request.dstId,message);
            }
            else{
                // s - s
                CbRenetMessage message = new CbRenetMessage(request.srcId, request.dstId,
                        priority);
                this.sendNonEgoTreeMessage(request.dstId, message);
            }
        }
        return true;

    }



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
         *              msg could be DeleteRequestMessage
         *                           LargeInsertMessage
         *                           CbRenetMessage  这个比较特殊，这个在Request层面就已经判断过了
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/1
         */
        super.sendEgoTreeMessage(largeId, dst, msg);

        if (dst == ID) {
            this.receiveMessage(msg);
            return;
        }
        boolean upForward = false;

        // 下面的写法是标准的错误写法，因为node的大小根本不能用于判断对方，必须考虑到过渡态的存在！！！！
//        if(!isNodeSmall(dst)){
//            upForward = true;
//        }

        if(msg instanceof DeleteRequestMessage)
        {
            upForward = true;
        }
        else if(msg instanceof LargeInsertMessage){
            upForward = false;
        }
        else if (msg instanceof CbRenetMessage){
            if(this.getCommunicateLargeNodes().containsKey(dst)){
                upForward = true;
            }
            else if(this.getCommunicateSmallNodes().containsKey(dst)){
                upForward = false;
            }
            else{
                Tools.fatalError("Please check communication request before send a Message!!!");
            }

            if(upForward != ((CbRenetMessage)msg).isUpForward()){
                Tools.fatalError("The upForward in routing message " +
                        "should equal to the cbrenetmessage!!!");
            }
        }
        else{
            Tools.warning("In sendEgoTreeMessage, the msg Type is " + msg.getClass() );
        }

        RoutingMessage routingMessage = new RoutingMessage(this.ID, dst, msg, largeId, upForward);

        //  check 是否满足发送
        // TODO 这里说不定可以不用再检测，看情况吧
        if(this.checkCommunicateSatisfaction(this.ID, dst)){
            this.forwardMessage(largeId, routingMessage);

            // When DRM sent, the corresponding CP should be removed! No message would be sent after this!
            if(msg instanceof DeleteRequestMessage){
                DeleteRequestMessage tmpMsg = (DeleteRequestMessage) msg;
                int largeCp = tmpMsg.getDst();
                this.removeCommunicationPartner(largeCp);
            }

        }
        else{
            this.routingMessageQueue.add(routingMessage);
        }
    }

    @Override
    // TODO 在这里加一个等待的队列
    protected void forwardMessage(int largeId, RoutingMessage msg) {
        /**
         *@description this method only use to transfer message in the ego-tree of the large node,
         * called when the node want to transfer a message
         *@parameters  [largeId, msg]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/7
         */
        int destination = msg.getDestination();
        if(this.ID == destination )
        {
            this.receiveMessage(msg.getPayLoad());
            return;
        }

        if(this.ID == largeId){
            // if the sender is LN, it should transfer the rt msg directly to the root of the ego-tree
            this.send(msg, Tools.getNodeByID(this.getRootNodeId()));
            return;
        }

        if(largeId != -1){
            // which means need to transfer in the large id's tree
            Message message = msg.getPayLoad();
            if(message instanceof CbRenetMessage){
                if(!((CbRenetMessage) message).isUpForward()){
                    // if not upForward, the message would send to the child
                    if (this.ID < destination) {
                        sendToRightChild(largeId, msg);
                    } else if (destination < ID) {
                        sendToLeftChild(largeId, msg);
                    }
                }
                else{
                    sendToParent(largeId, msg);
                }
            }
            else if(message instanceof LargeInsertMessage){
                LargeInsertMessage insertMessageTmp = (LargeInsertMessage) message;
                int target = insertMessageTmp.getTarget();
                if(target > this.ID){
                    this.sendToRightChild(largeId, message);
                }
                else{
                    this.sendToLeftChild(largeId, message);
                }
            }
            else if(message instanceof DeleteRequestMessage){
                DeleteRequestMessage deleteRequestMessageTmp = (DeleteRequestMessage) message;
                if(this.ID != deleteRequestMessageTmp.getDst()){
                    sendToParent(largeId, message);
                }
            }
            else{
                Tools.fatalError("Some message in the RoutingMessage is " + message.getClass());
            }
        }
        else{
            send(msg, Tools.getNodeByID(destination));
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

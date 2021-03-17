package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.SDNMessage.LargeInsertMessage;
import projects.cbrenet.nodes.messages.controlMessage.DeleteRequestMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteBaseMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeletePrepareMessage;
import projects.cbrenet.nodes.tableEntry.Request;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;

import java.util.Random;

public abstract class MessageSendLayer extends MessageQueueLayer{

    private Random rand = Tools.getRandomNumberGenerator(); // used to generate priority of the CbRenetMessage

    // send message part,
    // The first one : s-s l-l
    // The second one : l-s s-l
    public boolean sendMessageAccordingToRequest(Request request) {
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
            this.getMessageQueue().add(msg);
        }
    }



    @Override
    public void sendEgoTreeMessage(int largeId, int dst, Message msg){
        /**
         *@description The method only used to send a message in the ego-tree
         *          *  The method would generate a routing message to wrap up the msg
         *             Not only send the Request Message , but also other message can be sent
         *
         *          IMPORTANT!!! DO NOT use it to forward message
         *
         *          Only called by the sender!
         *
         *          would not cause RoutingMessage blocked in the Ego-Tree.
         *
         *@parameters  [largeId, dst, msg]
         *              msg could be DeleteRequestMessage (Send from the node in ego-tree)
         *                           LargeInsertMessage
         *                           CbRenetMessage  这个比较特殊，这个在Request层面就已经判断过了
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/1
         */

        if (dst == ID) {
            this.receiveMessage(msg);
            return;
        }
        boolean upForward = false;


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


        if(this.forwardMessage(routingMessage)){
            // When DRM sent, the corresponding CP should be removed! No message would be sent after this!
            if(msg instanceof DeleteRequestMessage){
                DeleteRequestMessage tmpMsg = (DeleteRequestMessage) msg;
                int largeCp = tmpMsg.getDst();
                this.removeCommunicationPartner(largeCp);
            }
        }
        else{
            this.addInRoutingMessageQueue(routingMessage);
        }

    }

    public void sendEgoTreeMessage(int largeId, int dst, Message msg, boolean upward){
        assert msg instanceof DeleteBaseMessage;

        RoutingMessage routingMessage = new RoutingMessage(this.ID, dst, msg, largeId, upward);

        if(!this.forwardMessage(routingMessage)){
            this.addInRoutingMessageQueue(routingMessage);
        }
    }


}

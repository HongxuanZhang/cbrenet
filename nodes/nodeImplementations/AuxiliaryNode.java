package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.AuxiliaryNodeMessage.AuxiliaryRequestMessage;
import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.SDNMessage.LargeInsertMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteBaseMessage;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;


/**
 * 只用于Ego-Tree中结点的辅助，只用于转发Message
 * 当满足条件时进行删除： 1. 只有一个孩子
 *                    2. 全Ego-Tree清除 （不需要来自SDN的DeleteMessage, 当其plr都断后自己即可清除）
 *
 * 特别注意，不处理LIM, 遇到LIM发现居然要执行的，说明自身已经可以删除了，把LIM返回给上一级结点。
 * */

public class AuxiliaryNode extends AuxiliaryNodeMessageQueueLayer{


    private void executeRoutingMessage(RoutingMessage routingMessage){
        Message payload = routingMessage.getPayload();
        if(payload instanceof LargeInsertMessage){
            // check whether this need to make the node return.
            int largeId = routingMessage.getLargeId();
            int helpedId = ((LargeInsertMessage) payload).getTarget();
            SendEntry entry = this.getCorrespondingEntry(helpedId, largeId);
            if(entry != null){
                // todo make the corresponding node return;
                return; // no need to forward.
            }
        }
        if(!this.forwardMessage( routingMessage )){
            this.addRoutingMessageToQueue(routingMessage.getNextHop(), routingMessage);
        }
    }

    @Override
    public void handleMessages(Inbox inbox) {
        // AuxiliaryRequestMessage
        // RoutingMessage ( 除了转发之外要特殊处理  1. LIM  2. )

        while (inbox.hasNext()) {
            Message msg = inbox.next();

            if(msg instanceof RoutingMessage){
                RoutingMessage routingMessage = (RoutingMessage) msg;

                this.executeRoutingMessage(routingMessage);

            }
            else if(msg instanceof AuxiliaryRequestMessage){

            }
            else if(msg instanceof DeleteBaseMessage){

            }
            //else if() 聚类和旋转的部分
            else{

            }

        }

        while(!this.cycleRoutingMessage.isEmpty()){
            // A routing message could cycle multiple times here.
            // Could make a routing message pass through a lot of nodes
            RoutingMessage routingMessage = this.cycleRoutingMessage.poll();
            this.executeRoutingMessage(routingMessage);
        }

    }

    private void tryToDelete(){
        // 在handleMessages后执行，删除已经被满足的entry
        // 确保其MessageQueue中没有对应的Message。
    }


    private void startDelete(){

    }


    @Override
    public void preStep() {

    }

    @Override
    public void init() {

    }

    @Override
    public void neighborhoodChange() {

    }

    @Override
    public void postStep() {
        this.doInPostRound();
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {

    }
}

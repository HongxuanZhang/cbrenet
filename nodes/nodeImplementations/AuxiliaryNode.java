package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.AuxiliaryNodeMessage.AuxiliaryRequestMessage;
import projects.cbrenet.nodes.messages.RoutingMessage;
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




    @Override
    public void handleMessages(Inbox inbox) {
        // AuxiliaryRequestMessage
        // RoutingMessage ( 除了转发之外要特殊处理  1. LIM  2. )

        while (inbox.hasNext()) {
            Message msg = inbox.next();

            if(msg instanceof RoutingMessage){
                RoutingMessage routingMessage = (RoutingMessage) msg;
                if(!this.forwardMessage( routingMessage )){
                    this.addRoutingMessageToQueue(routingMessage.getNextHop(), routingMessage);
                }
            }
            else if(msg instanceof AuxiliaryRequestMessage){

            }
            else{

            }

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

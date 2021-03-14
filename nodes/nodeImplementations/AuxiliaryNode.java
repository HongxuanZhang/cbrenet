package projects.cbrenet.nodes.nodeImplementations;

import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;


/**
 * 只用于Ego-Tree中结点的辅助，只用于转发Message
 * 当满足条件时进行删除： 1. 只有一个孩子
 *                    2. 全Ego-Tree清除 （不需要来自SDN的DeleteMessage, 当其plr都断后自己即可清除）
 * */

public class AuxiliaryNode extends AuxiliaryNodeMessageQueueLayer{




    @Override
    public void handleMessages(Inbox inbox) {
        // AuxiliaryRequestMessage
        // RoutingMessage ( 除了转发之外要特殊处理  1. LIM  2. )
    }

    private void tryToDelete(){
        // 在handleMessages后执行，删除已经被满足的entry
        // 确保其MessageQueue中没有对应的Message。
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

    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {

    }
}

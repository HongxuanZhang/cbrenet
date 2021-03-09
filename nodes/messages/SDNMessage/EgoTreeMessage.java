package projects.cbrenet.nodes.messages.SDNMessage;

import sinalgo.nodes.messages.Message;

import java.util.List;

// 该Message用于处理因为SCM到达顺序先后混乱的情形下的Create-Ego-Tree过程
// 用于防止LN误将一部分结点错认为Ego-Tree的结点

// only use: SDN -> LN

public class EgoTreeMessage extends StatusRelatedMessage {

    private List<Integer> egoTreeNodes;

    public EgoTreeMessage(List<Integer> egoTreeNodes,int uniqueStatusId){
        super(uniqueStatusId);
        this.egoTreeNodes = egoTreeNodes;
    }

    public List<Integer> getEgoTreeNodes() {
        return egoTreeNodes;
    }

    @Override
    public Message clone() {
        return null;
    }
}

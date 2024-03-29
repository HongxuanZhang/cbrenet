package projects.cbrenet.nodes.messages.SDNMessage;

import sinalgo.nodes.messages.Message;

/**
 * Send by the SDN to delete a small node in the ego-tree
 *
 * if allFlag = true, then the small node should remove itself from all ego-tree.
 *
 * */

public class DeleteMessage extends StatusRelatedMessage {

    private final int dst;
    private final int largeId;

    private final boolean allFlag;

    private final int auxiliaryNodeId;



    public DeleteMessage(int dst, int largeId, int uniqueStatusId, int auxiliaryNodeId){
        super(uniqueStatusId);
        this.dst = dst;
        this.largeId = largeId;
        this.allFlag = false;
        this.auxiliaryNodeId = auxiliaryNodeId;
    }

    public DeleteMessage(int dst, boolean allFlag, int largeId, int uniqueStatusId, int auxiliaryNodeId){
        super(uniqueStatusId);
        this.dst = dst;
        this.largeId = largeId;
        this.allFlag = allFlag;
        this.auxiliaryNodeId = auxiliaryNodeId;
    }

    public int getDst() {
        return dst;
    }

    public int getLargeId() {
        return largeId;
    }

    public boolean isAllFlag() {
        return allFlag;
    }

    public int getAuxiliaryNodeId() {
        return auxiliaryNodeId;
    }

    @Override
    public Message clone() {
        return this;
    }

    @Override
    public String toString() {
        return "DeleteMessage{" +
                "dst=" + dst +
                ", largeId=" + largeId +
                ", allFlag=" + allFlag +
                ", auxiliaryNodeId=" + auxiliaryNodeId +
                ", uniqueStatusId=" + uniqueStatusId +
                '}';
    }
}

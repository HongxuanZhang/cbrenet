package projects.cbrenet.nodes.messages.SDNMessage;

import sinalgo.nodes.messages.Message;

/**
 * this message only generate by the SDN node and send to the large node, large node send it
 * to its ego-tree.
 *
 * every node in the ego-tree will try to transfer the message to the leaf which should add the
 * link the the target.
 *
 * When the leaf add the link to the target, this message will transfer to the target too.
 * And the leaf id would be the parent of the target
 * */

public class LargeInsertMessage extends StatusRelatedMessage {

    final private int auxiliaryNodeId; // the auxiliary node which SDN allocate

    final private int target;          // which would be inserted to the ego-Tree(largeId)
    final private int largeId;

    private int leafId; // This will be the parent of the target.

    private boolean inserted = false; // set inserted when this message is received by the target

    private boolean leftFlag = false; // set by the parent , indicate that whether the target node is leftChild

    public LargeInsertMessage(int target, int largeID, int auxiliaryNodeId, int uniqueStatusId){
        super(uniqueStatusId);
        this.largeId = largeID;
        this.target = target;
        this.auxiliaryNodeId = auxiliaryNodeId;
    }

    public void setLeftFlag(boolean leftFlag) {
        this.leftFlag = leftFlag;
    }

    public boolean isLeftFlag() {
        return leftFlag;
    }

    public int getTarget() {
        return target;
    }

    public int getAuxiliaryNodeId() {
        return auxiliaryNodeId;
    }

    public int getLargeId() {
        return largeId;
    }

    public void setLeafId(int leafId) {
        this.leafId = leafId;
    }

    public int getLeafId() {
        return leafId;
    }

    @Override
    public Message clone() {
        return this;
    }

    public boolean isInserted() {
        return inserted;
    }

    public void setInserted() {
        this.inserted = true;
    }
}

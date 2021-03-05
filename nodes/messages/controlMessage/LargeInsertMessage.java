package projects.cbrenet.nodes.messages.controlMessage;

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

public class LargeInsertMessage extends Message {

    final private int target;
    final private int largeId;

    private int leafId; // This will be the parent of the target.

    private boolean inserted = false; // set inserted when this message is received by the target


    public LargeInsertMessage(int target, int largeID){
        this.largeId = largeID;
        this.target = target;
    }

    public int getTarget() {
        return target;
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

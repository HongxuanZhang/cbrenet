package projects.cbrenet.nodes.messages.deletePhaseMessages;

import sinalgo.nodes.messages.Message;

public class DeleteBaseMessage extends Message {

    final private int largeId;
    // This is necessary, since a node could be in many ego-tree.

    final private int deleteTarget;

    protected DeleteBaseMessage(int largeId, int deleteTarget){
        this.largeId = largeId;
        this.deleteTarget = deleteTarget;
    }


    @Override
    public Message clone() {
        return null;
    }

    public int getLargeId() {
        return largeId;
    }

    public int getDeleteTarget() {
        return deleteTarget;
    }
}

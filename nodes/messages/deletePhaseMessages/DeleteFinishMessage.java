package projects.cbrenet.nodes.messages.deletePhaseMessages;


/*
* Indicate the node to change the link to which node */

public class DeleteFinishMessage extends DeleteBaseMessage{

    final private int egoTreeId;

    final private int trueId;

    public DeleteFinishMessage(int largeId, int deleteTarget, int egoTreeId, int trueId) {
        super(largeId, deleteTarget);
        this.egoTreeId = egoTreeId;
        this.trueId = trueId;
    }

    public int getEgoTreeId() {
        return egoTreeId;
    }

    public int getTrueId() {
        return trueId;
    }

}

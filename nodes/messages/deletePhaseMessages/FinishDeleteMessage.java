package projects.cbrenet.nodes.messages.deletePhaseMessages;


/*
* Indicate the node to change the link to which node */

public class FinishDeleteMessage extends DeleteBaseMessage{
    
    final private int egoTreeId;

    final private int trueId;

    public FinishDeleteMessage(int largeId, int deleteTarget, int egoTreeId, int trueId) {
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

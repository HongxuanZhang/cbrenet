package projects.cbrenet.nodes.messages.deletePhaseMessages;

public class DeleteConfirmMessage extends DeleteBaseMessage{

    final private int srcEgoTreeId;

    public DeleteConfirmMessage(int largeId, int targetId, int srcEgoTreeId){
        super(largeId, targetId);
        this.srcEgoTreeId = srcEgoTreeId;
    }

    public int getSrcEgoTreeId() {
        return srcEgoTreeId;
    }
}

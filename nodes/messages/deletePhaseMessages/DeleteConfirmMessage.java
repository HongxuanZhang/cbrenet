package projects.cbrenet.nodes.messages.deletePhaseMessages;

public class DeleteConfirmMessage extends DeleteBaseMessage{

    final private int srcId;

    public DeleteConfirmMessage(int largeId, int targetId, int srcId){
        super(largeId, targetId);
        this.srcId = srcId;
    }

    public int getSrcId() {
        return srcId;
    }
}

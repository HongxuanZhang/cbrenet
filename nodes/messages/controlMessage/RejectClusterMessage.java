package projects.cbrenet.nodes.messages.controlMessage;

public class RejectClusterMessage extends AcceptOrRejectBaseMessage {

    boolean upward;

    public RejectClusterMessage(int masterId, int largeId, int clusterId, boolean upward){
        super(masterId, largeId,clusterId);
        this.upward = upward;
    }

    public boolean isUpward() {
        return upward;
    }
}

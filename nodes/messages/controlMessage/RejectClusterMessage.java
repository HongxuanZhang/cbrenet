package projects.cbrenet.nodes.messages.controlMessage;

import sinalgo.nodes.messages.Message;

public class RejectClusterMessage extends AckBaseMessage {

    boolean upward;

    public RejectClusterMessage(int masterId, int largeId, int clusterId, boolean upward){
        super(masterId, largeId,clusterId);
        this.upward = upward;
    }

    public boolean isUpward() {
        return upward;
    }
}

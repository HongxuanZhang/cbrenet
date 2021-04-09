package projects.cbrenet.nodes.messages.controlMessage.clusterMessage;

import sinalgo.nodes.messages.Message;

public class ClusterRelatedMessage extends Message {

    protected int largeId;

    protected ClusterRelatedMessage(int largeId){
        this.largeId = largeId;
    }

    @Override
    public Message clone() {
        return null;
    }

    public int getLargeId() {
        return largeId;
    }
}

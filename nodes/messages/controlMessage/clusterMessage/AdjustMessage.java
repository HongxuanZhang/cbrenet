package projects.cbrenet.nodes.messages.controlMessage.clusterMessage;

import projects.cbrenet.nodes.messages.controlMessage.clusterMessage.RequestClusterMessage;
import sinalgo.nodes.messages.Message;

public class AdjustMessage extends ClusterRelatedMessage {

    RequestClusterMessage requestClusterMessage;

    public AdjustMessage(int largeId, RequestClusterMessage requestClusterMessage){
        super(largeId);
        this.requestClusterMessage = requestClusterMessage;
    }


    @Override
    public Message clone() {
        return null;
    }
}

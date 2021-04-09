package projects.cbrenet.nodes.messages.controlMessage.clusterMessage;

import sinalgo.nodes.messages.Message;

public class AcceptOrRejectBaseMessage extends ClusterRelatedMessage {

    int masterId;
    int clusterId;  // which is also the requester 's ID

    protected AcceptOrRejectBaseMessage(int masterId, int largeId, int clusterId){
        super(largeId);

        this.masterId = masterId;
        this.clusterId = clusterId;
    }



    @Override
    public Message clone() {
        return this;
    }

    public int getClusterId() {
        return clusterId;
    }

    public int getMasterId() {
        return masterId;
    }
}

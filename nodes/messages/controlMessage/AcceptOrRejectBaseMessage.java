package projects.cbrenet.nodes.messages.controlMessage;

import sinalgo.nodes.messages.Message;

public class AcceptOrRejectBaseMessage extends Message {

    int masterId;

    int largeId;    // in which ego-tree
    int clusterId;  // which is also the requester 's ID

    protected AcceptOrRejectBaseMessage(int masterId, int largeId, int clusterId){
        this.masterId = masterId;
        this.largeId = largeId;
        this.clusterId = clusterId;
    }



    @Override
    public Message clone() {
        return this;
    }

    public int getLargeId() {
        return largeId;
    }

    public int getClusterId() {
        return clusterId;
    }

    public int getMasterId() {
        return masterId;
    }
}

package projects.cbrenet.nodes.messages.controlMessage;

import projects.cbrenet.nodes.tableEntry.CBInfo;
//import projects.cbrenet.nodes.tableEntry.CBRenetNodeInfo;


/**
 * AckCluster
 */
public class AcceptClusterMessage extends AcceptOrRejectBaseMessage {

    private double priority;
    private CBInfo info;

    RequestClusterMessage requestClusterMessage;


    private int position;
    private boolean isFinalNode; // keep track if this node is final node in current request

    public AcceptClusterMessage(int masterId, int largeId, int clusterId, double priority,
                                RequestClusterMessage requestClusterMessage) {
        super(masterId, largeId, clusterId);

        this.priority = priority;
        this.requestClusterMessage = requestClusterMessage;

    }

    public double getPriority() {
        return priority;
    }

    public CBInfo getInfo() {
        return info;
    }

    public int getPosition() {
        return position;
    }

    public boolean isFinalNode() {
        return isFinalNode;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public void setInfo(CBInfo info) {
        this.info = info;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setFinalNode() {
        this.isFinalNode = true;
    }

    public RequestClusterMessage getRequestClusterMessage() {
        return requestClusterMessage;
    }
}
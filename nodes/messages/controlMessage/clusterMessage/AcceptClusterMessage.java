package projects.cbrenet.nodes.messages.controlMessage.clusterMessage;

//import projects.cbrenet.nodes.tableEntry.CBRenetNodeInfo;


/**
 * AckCluster
 */
public class AcceptClusterMessage extends AcceptOrRejectBaseMessage {

    private double priority;

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

    public int getPosition() {
        return position;
    }

    public boolean isFinalNode() {
        return isFinalNode;
    }

    public void setPriority(double priority) {
        this.priority = priority;
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
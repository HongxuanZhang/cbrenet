package projects.cbrenet.nodes.messages.controlMessage;

import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.tableEntry.CBInfo;
//import projects.cbrenet.nodes.tableEntry.CBRenetNodeInfo;
import sinalgo.nodes.messages.Message;

/**
 * AckCluster
 */
public class AckClusterMessage extends AckBaseMessage{

    private double priority;
    private CBInfo info;



    private int position;
    private boolean isFinalNode; // keep track if this node is final node in current request

    public AckClusterMessage(int masterId, int largeId, int clusterId, double priority, int position,CBInfo info) {
        super(masterId, largeId, clusterId);

        this.priority = priority;
        this.info = info;
        this.position = position;

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

}
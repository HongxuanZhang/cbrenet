package projects.cbrenet.nodes.messages.controlMessage;

import sinalgo.nodes.messages.Message;

/**
 * RequestCluster
 */
public class RequestClusterMessage extends Message implements Comparable<RequestClusterMessage> {

    private final int largeId;
    private int currentNode;
    private int requesterId;

    private double generateTime; // used as priority

    private int position;
    private boolean isFinalNode; // keep track if this node is final node in current request
    
    public RequestClusterMessage(RequestClusterMessage msg) {
        this.largeId = msg.largeId;
        this.currentNode = msg.getCurrentNode();
        this.requesterId = msg.getRequesterId();

        this.generateTime = msg.getGenerateTime();
        this.position = msg.getPosition();
        this.isFinalNode = false;
    }

    public RequestClusterMessage(int largeId, int currentNode, int requesterId, int position, double generateTime) {
        this.largeId = largeId;
        this.currentNode = currentNode;
        this.requesterId = requesterId;

        this.generateTime = generateTime;
        this.position = position;
        this.isFinalNode = false;
    }

    public int getCurrentNode() {
        return currentNode;
    }

    public int getRequesterId() {
        return requesterId;
    }


    public double getGenerateTime() {
        return generateTime;
    }

    public int getPosition() {
        return position;
    }

    public boolean isFinalNode() {
        return isFinalNode;
    }

    public void setCurrentNode(int currentNode) {
        this.currentNode = currentNode;
    }

    public void setRequesterId(int requesterId) {
        this.requesterId = requesterId;
    }


    public void setGenerateTime(double generateTime) {
        this.generateTime = generateTime;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void shiftPosition() {
        this.position++;
    }

    public void setFinalNode() {
        this.isFinalNode = true;
    }

    public int getLargeId() {
        return largeId;
    }

    @Override
    public Message clone() {
        return this;
    }

    @Override
	public int compareTo(RequestClusterMessage o) {
		int value = Double.compare(this.generateTime, o.generateTime);
		if (value == 0) { // In case tie, compare the id of the source node
			return this.requesterId - o.requesterId;
		} else {
			return value;
		}
	}
    
}
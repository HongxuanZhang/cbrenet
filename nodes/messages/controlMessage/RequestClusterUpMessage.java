package projects.cbrenet.nodes.messages.controlMessage;

/**
 * RequestCluster
 */
public class RequestClusterUpMessage extends RequestClusterMessage {

    public RequestClusterUpMessage(RequestClusterMessage m) {
        super(m);
    }

    public RequestClusterUpMessage(int largeId, int currentNode, int src, int dst, int position, double priority) {
        super(largeId, currentNode, src, dst, position, priority);
    }

}
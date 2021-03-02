package projects.cbrenet.nodes.messages.controlMessage;

public class RequestClusterDownMessage extends RequestClusterMessage {

    public RequestClusterDownMessage(RequestClusterMessage m) {
        super(m);
    }

    public RequestClusterDownMessage(int largeId, int currentNode, int src, int dst, int position, double priority) {
        super(largeId,currentNode, src, dst, position, priority);
    }
    
}
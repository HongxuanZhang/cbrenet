package projects.cbrenet.nodes.nodeImplementations.forwardSendHelper;

import projects.cbrenet.nodes.messages.RoutingMessage;

public interface EntryGetter {

    boolean sendTo(int egoTreeTargetID, RoutingMessage routingMessage);

    int getParentOf(int helpedId, int largeId);

    int getLeftChildOf(int helpedId, int largeId);

    int getRightChildOf(int helpedId, int largeId);

}

package projects.cbrenet.nodes.nodeImplementations.nodeHelper;

import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.routeEntry.SendEntry;

public interface EntryGetter {

    boolean sendTo(int egoTreeTargetID, RoutingMessage routingMessage);

    void addSendEntry(int helpedId, int largeId, int parent, int leftChild, int rightChild);

    SendEntry getCorrespondingEntry(int helpedId, int largeId);

    int getParentOf(int helpedId, int largeId);

    int getLeftChildOf(int helpedId, int largeId);

    int getRightChildOf(int helpedId, int largeId);

}

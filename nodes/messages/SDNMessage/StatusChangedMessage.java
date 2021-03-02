package projects.cbrenet.nodes.messages.SDNMessage;

import sinalgo.nodes.messages.Message;

/**
 * Only means a node's status has been changed by the SDN , the link change should be LinkMessage
 * 's concern
 *  This should never effect largeIds and links in the node which receive this message
 *
 *  But THIS MESSAGE WOULD EFFECT the partner part, which means the new request may not satisfied!
 * */

public class StatusChangedMessage extends Message
{
    final private int statusChangedNodeId;

    final private int uniqueStatusId;

    final private boolean smallFlag;

    public StatusChangedMessage(int statusChangedNodeId, boolean smallFlag,int uniqueStatusId){
        this.statusChangedNodeId = statusChangedNodeId;
        this.smallFlag = smallFlag;
        this.uniqueStatusId = uniqueStatusId;
    }

    public int getStatusChangedNodeId() {
        return statusChangedNodeId;
    }

    public int getUniqueStatusId() {
        return uniqueStatusId;
    }

    public boolean isSmallFlag() {
        return smallFlag;
    }

    @Override
    public Message clone() {
        return null;
    }
}

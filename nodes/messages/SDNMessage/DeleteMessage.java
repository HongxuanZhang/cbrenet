package projects.cbrenet.nodes.messages.SDNMessage;

import sinalgo.nodes.messages.Message;

/**
 * Send by the SDN to delete a small node in the ego-tree
 *
 * if allFlag = true, then the small node should remove itself from all ego-tree.
 *
 * */

public class DeleteMessage extends Message {

    private final int dst;
    private final int largeId;

    private final boolean allFlag;

    public DeleteMessage(int dst, int largeId){
        this.dst = dst;
        this.largeId = largeId;
        this.allFlag = false;
    }

    public DeleteMessage(int dst, boolean allFlag, int largeId){
        this.dst = dst;
        this.largeId = largeId;
        this.allFlag = allFlag;
    }


    @Override
    public Message clone() {
        return this;
    }
}

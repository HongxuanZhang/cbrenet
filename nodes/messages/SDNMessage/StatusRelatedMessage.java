package projects.cbrenet.nodes.messages.SDNMessage;

import sinalgo.nodes.messages.Message;

/*
* This is the base class of some messages which related to the SCM.
* They may wait until the corresponding SCM received by the node.
* */

public class StatusRelatedMessage extends Message {

    final protected int uniqueStatusId;

    public StatusRelatedMessage(int uniqueStatusId){
        this.uniqueStatusId = uniqueStatusId;
    }

    public int getUniqueStatusId() {
        return this.uniqueStatusId;
    }

    @Override
    public Message clone() {
        return null;
    }
}

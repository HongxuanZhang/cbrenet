package projects.cbrenet.nodes.messages.SDNMessage;

import projects.cbrenet.nodes.nodeImplementations.CbRenetBinarySearchTreeLayer;

public class RPCSdnMessage extends RPCMessage{

    private int source;

    public RPCSdnMessage(String command, CbRenetBinarySearchTreeLayer node, int source) {
        super(command, node, -1);
        this.source = source;
    }

    public int getSource() {
        return source;
    }
}

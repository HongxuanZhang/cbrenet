package projects.cbrenet.nodes.messages.SDNMessage;

import projects.cbrenet.nodes.nodeImplementations.CbRenetBinarySearchTreeLayer;
import sinalgo.nodes.messages.Message;

/**
 * RPCMessage
 */
public class RPCMessage extends Message {
    private String command;
    private CbRenetBinarySearchTreeLayer node;
    private long value;
    private int largeId = -1;


    public RPCMessage(String command, CbRenetBinarySearchTreeLayer node, int largeId) {
        this.command = command;
        this.node = node;
        this.value = -1;
        this.largeId = largeId;
    }

    public RPCMessage(String command, long value, int largeId) {
        this.command = command;
        this.node = null;
        this.value = value;
        this.largeId = largeId;
    }

    public String getCommand() {
        return this.command;
    }

    public CbRenetBinarySearchTreeLayer getNode() {
        return this.node;
    }

    public long getValue() {
        return this.value;
    }

    public void setCommand(String cmd) {
        this.command = cmd;
    }

    public void setNode(CbRenetBinarySearchTreeLayer node) {
        this.node = node;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public Message clone() {
        return this;
    }
    
}
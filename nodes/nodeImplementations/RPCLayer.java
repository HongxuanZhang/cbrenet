package projects.cbrenet.nodes.nodeImplementations;

import java.util.LinkedList;
import java.util.Queue;

import projects.cbrenet.nodes.messages.SDNMessage.RPCMessage;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

/**
 * RPCLayer // todo 最后再改吧
 */
public abstract class RPCLayer extends SynchronizerLayer {

    private Queue<RPCMessage> rpcQueue;

    @Override
    public void init() {
        super.init();
        this.rpcQueue = new LinkedList<>();
    }

    public void clearRPCQueue() {
        this.rpcQueue.clear();
    }

    public void execute(RPCMessage rpc) {
        int largeId = 0;
        switch (rpc.getCommand()) {
        case "setParent":
            this.setParent(largeId, (CBTreeWeightLayer) rpc.getNode());
            break;

        case "setLeftChild":
            this.setLeftChild(largeId, (CBTreeWeightLayer) rpc.getNode());
            break;

        case "setRightChild":
            this.setRightChild(largeId, (CBTreeWeightLayer) rpc.getNode());
            break;

        case "setMinIdInSubtree":
            this.setMinIdInSubtree(largeId, (int) rpc.getValue());
            break;

        case "setMaxIdInSubtree":
            this.setMaxIdInSubtree(largeId, (int) rpc.getValue());
            break;

        case "changeLeftChildTo":
            this.changeLeftChildTo(largeId, (CBTreeWeightLayer) rpc.getNode());
            break;

        case "changeRightChildTo":
            this.changeRightChildTo(largeId, (CBTreeWeightLayer) rpc.getNode());
            break;

        case "setWeight":
            this.setWeight(rpc.getValue());
            break;

        case "incrementWeight":
            this.incrementWeight();
            break;

        default:
            Tools.fatalError("Wrong procedure called " + rpc.getCommand());
            break;
        }
    }

    public void executeAllRPC() {
        while (!this.rpcQueue.isEmpty()) {
            RPCMessage msg = this.rpcQueue.poll();
            this.execute(msg);
        }
    }

    public void requestRPCTo(int id, String command, CBTreeWeightLayer node, int largeId) {
        RPCMessage msg = new RPCMessage(command, node, largeId);
        this.sendForwardMessage(id, msg);
    }

    public void requestRPCTo(int id, String command, long n, int largeId) {
        RPCMessage msg = new RPCMessage(command, n, largeId);
        this.sendForwardMessage(id, msg);
    }

    @Override
    public void receiveMessage(Message msg) {
        if (msg instanceof RPCMessage) {
            this.rpcQueue.add((RPCMessage) msg);
            return;
        }
    }

}
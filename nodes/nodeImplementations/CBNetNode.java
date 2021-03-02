package projects.cbrenet.nodes.nodeImplementations;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.tableEntry.Request;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;

import projects.defaultProject.DataCollection;

/**
 * CBNetNode
 */
public class CBNetNode extends RotationLayer {

    private DataCollection data = DataCollection.getInstance();

    // to break ties in priority
    private Random rand = Tools.getRandomNumberGenerator();

    private Queue<Request> bufferRequest;

    @Override
    public void init() {
        super.init();
        this.bufferRequest = new LinkedList<>();
    }

    @Override
    public void updateState() {
        super.updateState();
        Queue<Request> unsatisfiedRequest = new LinkedList<>();
        while(!this.bufferRequest.isEmpty()) {
            Request rq = this.bufferRequest.poll();

            CbRenetMessage msg = new CbRenetMessage(rq.srcId, rq.dstId, bigFlag,largeId,Global.currentTime + rand.nextDouble());

            boolean src = this.isNodeSmall(rq.srcId);
            boolean dst = this.isNodeSmall(rq.dstId);
            if(this.checkCommunicationRequest(rq)){
                if(src && dst){
                    // small & small
                    this.sendCBNetMessage(-1, false,
                            rq.dstId,Global.currentTime + rand.nextDouble());
                }
                else if(!src && !dst){
                    // big & big
                    this.sendCBNetMessage(-1, false,
                            rq.dstId,Global.currentTime + rand.nextDouble());
                }
                else{
                    if(!src){
                        // src node is big
                        this.sendCBNetMessage(rq.srcId, true,
                                rq.dstId,Global.currentTime + rand.nextDouble());
                    }
                    else{
                        // dst node is big
                        this.sendCBNetMessage(rq.dstId, true,
                                rq.dstId,Global.currentTime + rand.nextDouble());
                    }
                }
                this.newMessageSent();
                this.data.addSequence(rq.srcId - 1, rq.dstId - 1);
            }
            else{
                unsatisfiedRequest.add(rq);
                // send message to SDN to solve this question
                this.sendDirect(new , Tools.getNodeByID(this.sdnId));
            }
        }
        this.bufferRequest.addAll(unsatisfiedRequest);
    }

    public void newMessage(int dst) {
        Request splay = new Request(ID, dst);
        this.bufferRequest.add(splay);
    }

    @Override
    public void ackCBNetMessageReceived(CbRenetMessage msg) {
//        this.state = States.PASSIVE;
        msg.finalTime = this.getCurrentRound();
        this.communicationCompleted(msg);
    }

    public void newMessageSent() {
        
    }

    public void communicationCompleted(CbRenetMessage msg) {

    }

    @Override
    public void receivedCBNetMessage(CbRenetMessage msg) {
        // System.out.println("Node " + ID + ": message received from " + msg.getSrc());
    }
}
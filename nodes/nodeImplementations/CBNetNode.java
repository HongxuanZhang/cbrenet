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
public class CBNetNode extends CounterBasedNetLayer {

    private DataCollection data = DataCollection.getInstance();

    private Queue<Request> bufferRequest;

    @Override
    public void preStep() {
        super.preStep();
        this.communicateAccordingToRequests();
    }

    @Override
    public void init() {
        super.init();
        this.bufferRequest = new LinkedList<>();
    }

    @Override
    public void postStep() {
        super.postStep();
    }

    // 加个接口，这里每一回合都要调用的
    public void communicateAccordingToRequests() {
        Queue<Request> unsatisfiedRequest = new LinkedList<>();
        while(!this.bufferRequest.isEmpty()) {
            Request request = this.bufferRequest.poll();
            if(this.sendMessageAccordingToRequest(request)){
                unsatisfiedRequest.add(request);
            }
            else{
                this.updatePartnerOrder(request);
            }
        }
        this.bufferRequest.addAll(unsatisfiedRequest);
    }




    public void newRequestCome(int dst) {
        /*
         *@description  A global timer trigger will tell the node when a new
         *              request come, and add it to the buffer
         *@parameters  [dst]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/4/4
         */
        Request request = new Request(ID, dst);
        this.bufferRequest.add(request);
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
package projects.cbrenet.nodes.nodeImplementations;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import projects.cbrenet.nodes.tableEntry.Request;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;


/**
 * CBNetNode
 */
public class CBNetNode extends LinkLayer {

    private Queue<Request> unknownRequestsToSDN;
    private Queue<Request> bufferRequest;

    @Override
    public void preStep() {
        this.communicateAccordingToRequests();
    }

    @Override
    public void init() {
        super.init();
        this.bufferRequest = new LinkedList<>();
        this.unknownRequestsToSDN = new LinkedList<>();
    }

    @Override
    public void postStep() {
        super.postStep();
    }

    // 加个接口，这里每一回合都要调用的
    public void communicateAccordingToRequests() {
        Queue<Request> unsatisfiedRequest = new LinkedList<>();

        // only the first time request would be sent to SDN
        while(!this.unknownRequestsToSDN.isEmpty()){
            Request request = this.unknownRequestsToSDN.poll();
            if(this.sendMessageAccordingToRequest(request, true)){
                this.updatePartnerOrder(request);
            }
            else{
                unsatisfiedRequest.add(request);
            }
        }

        while(!this.bufferRequest.isEmpty()) {
            Request request = this.bufferRequest.poll();
            if(this.sendMessageAccordingToRequest(request,false)){
                this.updatePartnerOrder(request);
            }
            else{
                unsatisfiedRequest.add(request);
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
        this.unknownRequestsToSDN.add(request);
    }


    public void newMessageSent() {
        
    }

    public void communicationCompleted(CbRenetMessage msg) {

    }

    @Override
    public void receiveMessage(Message msg){
        System.out.println("Node " + this.ID + " receive a message " + msg.getClass().getName() + "" +
                " " + msg.toString());
        super.receiveMessage(msg);
        if(msg instanceof CbRenetMessage){
            CbRenetMessage message = (CbRenetMessage)msg;
            message.incrementRouting();
            System.out.println("Node " + ID + ": message received from " + message.getSrc() + " routing" +
                    " " + message.getRouting());
            this.communicationCompleted(message);
        }
    }

}
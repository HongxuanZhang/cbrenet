package projects.cbrenet.nodes.nodeImplementations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.messages.CompletionMessage;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

/**
 * CBNetLayer
 * This layer is used to send the message
 */
public abstract class CounterBasedNetLayer extends LinkLayer  {
	
	private boolean recvCBNetMessage;
	private int sourceID;
	private boolean recvAckCBNetMessage;
	private ArrayList<CbRenetMessage> ackMessageReceived;

	// The message it has to send
    private PriorityQueue<CbRenetMessage> cbnetQueue;

    @Override
    public void init() {
        super.init();


        this.recvCBNetMessage = false;
        this.sourceID = -1;
        this.recvAckCBNetMessage = false;
        this.ackMessageReceived = new ArrayList<CbRenetMessage>();


        this.cbnetQueue = new PriorityQueue<>();
    }

    public boolean hasCBNetMessage() {
        return !this.cbnetQueue.isEmpty();
    }

    public CbRenetMessage getTopCBNetMessage() {
        return this.cbnetQueue.peek();
    }

    public void removeTopCBNetMessage() {
        this.cbnetQueue.poll();
    }

    public void sendCBNetMessage(int largeId,boolean bigFlag,int dst, double priority) {
    	this.incrementCounter(); // increment local counter
        // todo make sure the largeId and bigFlag is used correctly
        CbRenetMessage msg = new CbRenetMessage(ID, dst, bigFlag,largeId,priority);
        msg.initialTime = this.getCurrentRound();
        this.cbnetQueue.add(msg);
    }

    public void forwardCBNetMessage(int dst, CbRenetMessage msg) {
        // todo need largeId
        this.sendForwardMessage(dst, msg);
    }

    @Override
    public void receiveMessage(Message msg) {
        super.receiveMessage(msg);

        if (msg instanceof CbRenetMessage) {
            CbRenetMessage cbmsg = (CbRenetMessage) msg;

            if (ID == cbmsg.getDst()) {
                this.receivedCBNetMessage(cbmsg);
                this.sourceID = cbmsg.getSrc();
                this.recvCBNetMessage = true;

                // send ack message
                this.sendDirect(new CompletionMessage(cbmsg), Tools.getNodeByID(cbmsg.getSrc()));

            } else {
                this.cbnetQueue.add(cbmsg);
            }
            return;
        } else if (msg instanceof CompletionMessage) {
            CompletionMessage completionMessage = (CompletionMessage) msg;
            this.recvAckCBNetMessage = true;
            this.ackMessageReceived.add(completionMessage.getCbnetMessage());
            return;
        }
    }
    
    @Override
    public void timeslot11() {
    	super.timeslot11();
    	//if future improvements allow to 
    	//receive multiples messages at same time
    	//this procedure must be changed 
    	if (this.recvCBNetMessage == true) {
    	    int largeId = -1; // todo
    		this.recvCBNetMessage = false;
    		this.incrementCounter(); // increment local counter
    		this.incrementWeight();
    		this.updateWeights(largeId, ID, this.sourceID);
    	}
    	
    	if (this.recvAckCBNetMessage == true) {
    		this.recvAckCBNetMessage = false;
    		Iterator<CbRenetMessage> it = this.ackMessageReceived.iterator();
    		while (it.hasNext()) {
    			this.ackCBNetMessageReceived(it.next());
    		}
    	}
    	
    	this.ackMessageReceived.clear();
    }

    public abstract void receivedCBNetMessage(CbRenetMessage msg);

    
    public abstract void ackCBNetMessageReceived(CbRenetMessage msg);
    
}
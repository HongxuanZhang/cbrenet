package projects.cbrenet.nodes.nodeImplementations;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import projects.cbrenet.nodes.messages.controlMessage.AckClusterMessage;
import projects.cbrenet.nodes.messages.controlMessage.RequestClusterUpMessage;
import projects.cbrenet.nodes.messages.controlMessage.RequestClusterDownMessage;
import projects.cbrenet.nodes.messages.controlMessage.RequestClusterMessage;
import projects.cbrenet.nodes.tableEntry.CBInfo;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

/**
 * ClusterLayer
 * 本layer在做cluster的工作
 *  一个很重要的概念，就是position，对应于：
 *              /**
 *                                   3- w
 *                                     /
 *                                 2- z <ID == dst then set as final node; set zig operation>
 *                                   /
 *                               1- y <if is LCA the set as final node; set zig operation>
 *                                 / \
 *                current node-0- x   c
 *                               / \
 *                              a   b
 *
 *
 **/
public abstract class ClusterLayer extends CounterBasedNetLayer {

    //ToDo 删掉这里面的单树所用的东西

    // this priority queue store all request cluster message received
    private PriorityQueue<RequestClusterMessage> queueClusterRequest;
    private HashMap<Integer, PriorityQueue<RequestClusterMessage>> queueClusterRequests;

    // this queue keeps all acks received due to a request cluster operation
    private Queue<AckClusterMessage> queueAckCluster;
    private HashMap<Integer, Queue<AckClusterMessage>> queueAckClusters;



//    private boolean isClusterUp;
//    private boolean isClusterDown;
    private HashMap<Integer, Boolean> isClusterUps;
    private HashMap<Integer, Boolean> isClusterDowns;



    private void addQueueClusterRequests(int largeId, RequestClusterMessage msg){
        if(this.queueClusterRequests.containsKey(largeId)){
            PriorityQueue<RequestClusterMessage> tmp = this.queueClusterRequests.getOrDefault(largeId, null);
            assert tmp != null;
            tmp.add(msg);
            this.queueClusterRequests.replace(largeId,tmp);
        }
        else{
            PriorityQueue<RequestClusterMessage> tmp = new PriorityQueue<>();
            tmp.add(msg);
            this.queueClusterRequests.put(largeId, tmp);
        }
    }


    private void addQueueAckClusters(int largeId, AckClusterMessage msg){
        if(this.queueAckClusters.containsKey(largeId)){
            Queue<AckClusterMessage> tmp = this.queueAckClusters.getOrDefault(largeId, null);
            assert tmp != null;
            tmp.add(msg);
            this.queueAckClusters.replace(largeId,tmp);
        }
        else{
            Queue<AckClusterMessage> tmp = new LinkedList<>();
            tmp.add(msg);
            this.queueAckClusters.put(largeId, tmp);
        }
    }




    @Override
    public void init() {
        super.init();

        this.queueClusterRequest = new PriorityQueue<>();
        this.queueAckCluster = new LinkedList<>();
//        this.isClusterUp = false;
//        this.isClusterDown = false;

        this.queueClusterRequests = new HashMap<>();
        this.queueAckClusters = new HashMap<>();
        this.isClusterDowns = new HashMap<>();
        this.isClusterUps = new HashMap<>();
    }



    public void clearClusterRequestQueue() {
        this.queueClusterRequest.clear();
        this.queueClusterRequests.clear();
    }

    public void clearAckClusterQueue() {
        this.queueAckCluster.clear();
        this.queueAckClusters.clear();
    }

    /**
     * This method send a cluster request and put the request into the buffer of
     * current node. The position means how many times the message should be routed.
     * 
     * @param src
     * @param dst
     * @param priority
     */
    public void sendRequestClusterUp(int largeId, int currentNode, int src, int dst, double priority) {
        // System.out.println("Node " + ID + " sending cluster up");
//        this.isClusterUp = true;
        this.isClusterUps.put(largeId,true);

        RequestClusterUpMessage msg = new RequestClusterUpMessage(largeId, currentNode, src, dst, 0, priority);

        // add cluster request to buffer
        //this.queueClusterRequest.add(msg);
        this.addQueueClusterRequests(largeId, msg);

        // shift the position one hop
        RequestClusterUpMessage m = new RequestClusterUpMessage(msg);
        m.shiftPosition();

        this.sendToParent(largeId,m);
    }

    // sendRequestClusterUp & sendRequestClusterDown only invoked in the CBNetLayer

    /**
     * 该函数向上向下都发了message*/
    public void sendRequestClusterDown(int largeId, int currentNode, int src, int dst, double priority) {
        // System.out.println("Node " + ID + " sending cluster down");
        // this.isClusterDown = true;
        this.isClusterDowns.put(largeId, true);

        RequestClusterDownMessage msg = new RequestClusterDownMessage(largeId, currentNode, src, dst, 1, priority);

        //this.queueClusterRequest.add(msg);
        this.addQueueClusterRequests(largeId, msg);

        RequestClusterDownMessage toParent = new RequestClusterDownMessage(msg);
        toParent.setPosition(0);
        this.sendToParent(largeId, toParent);

        RequestClusterDownMessage downward = new RequestClusterDownMessage(msg);
        downward.shiftPosition();
        
        if (ID < msg.getDst() && msg.getDst() <= this.getMaxIdInSubtree(largeId)) {
            this.sendToRightChild(largeId, downward);
        } else if (this.getMinIdInSubtree(largeId) <= msg.getDst() && msg.getDst() < ID) {
            this.sendToLeftChild(largeId, downward);
        }
    }

    @Override
    public void receiveMessage(Message msg) {
        super.receiveMessage(msg);

        if (msg instanceof RequestClusterUpMessage) {

            RequestClusterUpMessage requestMessage = (RequestClusterUpMessage) msg;
            int position = requestMessage.getPosition();

            /**
                                  3- w
                                    /
                                2- z <ID == dst then set as final node; set zig operation>
                                  /            
                              1- y <if is LCA the set as final node; set zig operation>            
                                / \            
               current node-0- x   c     
                              / \              
                             a   b              
             
             */
            /* just a zig because 2 is the dst node*/
            //  || (position == 2 && ID == requestMessage.getDst())
            if (position == 3 /*simple double operation*/
            		/* a single cluster to send the message*/
            		|| (position == 1 && ID == requestMessage.getDst())) {

                requestMessage.setFinalNode();

            } else if (!requestMessage.isFinalNode()) {
                int largeId = requestMessage.getLargeId();
                RequestClusterUpMessage newRequestMessage = new RequestClusterUpMessage(requestMessage);
                newRequestMessage.shiftPosition();

                if (position == 1 && this.isAncestorOf(largeId, requestMessage.getDst())) {
                    newRequestMessage.setFinalNode();
                }

                this.sendToParent(largeId, newRequestMessage);

            }

            // add current request to queue
//            this.queueClusterRequest.add(requestMessage);
            this.addQueueClusterRequests(requestMessage.getLargeId(), requestMessage);
            return;
        } else if (msg instanceof RequestClusterDownMessage) {

            RequestClusterDownMessage requestMessage = (RequestClusterDownMessage) msg;
            int position = requestMessage.getPosition();

            /**
            
                                  0- w
                                    /
                   current node-1- z
                                  /            
                              2- y <ID == dst then set as final node; set zig operation>            
                                / \            
                            3- x   c     
                              / \              
                             a   b              
             
            */
            if (position != 0) { /* node 0 just add request to queue*/ 
	            if (position == 3 /*simple flow, last node*/
	            		|| (position == 2 && ID == requestMessage.getDst()) /*dst node found*/) {
	
	                requestMessage.setFinalNode();
	
	            } else {
	                int largeId = requestMessage.getLargeId();
	                RequestClusterDownMessage newRequestMessage = new RequestClusterDownMessage(requestMessage);
	                newRequestMessage.shiftPosition();
	
	                if (ID < newRequestMessage.getDst() 
	                		&& newRequestMessage.getDst() <= this.getMaxIdInSubtree(largeId)) {
	                    if (this.hasRightChild(largeId)) {
	                        // System.out.println("node " + ID + " forwarding cluster msg down left");
	                        this.sendToRightChild(largeId, newRequestMessage);
	                    } else {
	                        requestMessage.setFinalNode();
	                    }
	                } else if (this.getMinIdInSubtree(largeId) <= newRequestMessage.getDst()
	                		&& newRequestMessage.getDst() < ID) {
	                    if (this.hasLeftChild(largeId)) {
	                        // System.out.println("node " + ID + " forwarding cluster msg down right");
	                        this.sendToLeftChild(largeId, newRequestMessage);
	                    } else {
	                        requestMessage.setFinalNode();
	                    }
	                }
	            }
            }

            // add current request to queue
//            this.queueClusterRequest.add(requestMessage);
            this.addQueueClusterRequests(requestMessage.getLargeId(), requestMessage);

            return;

        } else if (msg instanceof AckClusterMessage) {

            AckClusterMessage ackMessage = (AckClusterMessage) msg;
            this.addQueueAckClusters(ackMessage.getLargeId(),ackMessage);

            return;
        }
    }

    /**
     * In this time slot all request message will have arrived
     */
    @Override
    public void timeslot3() {
        for(int largeId:this.getLargeIds()){
            // todo may traverse all largeId
            PriorityQueue<RequestClusterMessage> queueClusterRequestTmp =
                    this.queueClusterRequests.getOrDefault(largeId, null);
            if(queueClusterRequestTmp == null ){
                Tools.fatalError("Wrong things happen in the " + this.ID+", the large node " + largeId +
                        "do not have corresponding queueClusterRequest");
                continue;
            }
            else if (!queueClusterRequestTmp.isEmpty()) {
                RequestClusterMessage rq = queueClusterRequestTmp.poll();
                CBInfo info = this.getNodeInfo();
                AckClusterMessage ack = new AckClusterMessage(-1, rq.getRequesterId(), rq.getDst(), rq.getGenerateTime(), rq.getPosition(),
                        rq.getLargeId(), info);

                if (rq.isFinalNode()) {
                    ack.setFinalNode();
                }

                this.sendForwardMessage(rq.getCurrentNode(), ack);
            }
        }
    }

    private AckClusterMessage findAckMessageInBufferByPosition(int largeId, int pos) {
        Queue<AckClusterMessage> q = this.queueAckClusters.getOrDefault(largeId, null);
        if(q == null){
            Tools.fatalError("Wrong things happen in the " + this.ID+", the large node " + largeId +
                    "do not have corresponding queueAckCluster");
            return null;
        }
        for (AckClusterMessage m : q) {
            if (m.getPosition() == pos) {
                return m;
            }
        }
        return null;
    }

    /**
     * This function checks ack buffer to see if ack message form a linear sequence
     * to final node. In case missing one ack message the function return false.
     * 
     * @return
     */
    private boolean isClusterGranted(int largeId) {
        AckClusterMessage m;
        // 这里的3应该是检测是否有3个结点可以参与cluster
        // 我们需要的是，把这个buffer按照largeId分开
        for (int pos = 0; pos <= 3; pos++) {
            m = findAckMessageInBufferByPosition(largeId, pos);
            if (m == null) {
                return false;
            } else if (m.isFinalNode()) {
                return true;
            }
        }

        return false;
    }

    private HashMap<String, CBInfo> getClusterSequenceFromAckBufferBottomUp(int largeId) {
        HashMap<String, CBInfo> table = new HashMap<>();
        CBInfo info;

        info = findAckMessageInBufferByPosition(largeId,0).getInfo();
        table.put("x", info);

        info = findAckMessageInBufferByPosition(largeId,1).getInfo();
        table.put("y", info);

        info = findAckMessageInBufferByPosition(largeId,2).getInfo();
        table.put("z", info);

        if (this.queueAckClusters.get(largeId).size() == 4) {
            info = findAckMessageInBufferByPosition(largeId,3).getInfo();
            table.put("w", info);
        }

        return table;
    }

    private HashMap<String, CBInfo> getClusterSequenceFromAckBufferTopDown(int largeId) {
        HashMap<String, CBInfo> table = new HashMap<>();
        CBInfo info;

        info = findAckMessageInBufferByPosition(largeId,0).getInfo();
        table.put("w", info);

        info = findAckMessageInBufferByPosition(largeId,1).getInfo();
        table.put("z", info);

        info = findAckMessageInBufferByPosition(largeId,2).getInfo();
        table.put("y", info);

        if (this.queueAckClusters.get(largeId).size() == 4) {
            info = findAckMessageInBufferByPosition(largeId,3).getInfo();
            table.put("x", info);
        }

        return table;
    }

    private CBInfo findTarget(int largeId) {
        Queue<AckClusterMessage> queueTmp = this.queueAckClusters.getOrDefault(largeId, null);
        if(queueTmp == null){
            Tools.fatalError("Wrong things happen in the " + this.ID+", the large node " + largeId +
                    "do not have corresponding queueAckCluster in findTarget");
            return null;
        }
        for (AckClusterMessage m : queueTmp) {
            if (m.getDst() == m.getInfo().getNode().ID 
            		&& this.isNeighbor(largeId, m.getInfo().getNode())) {
//            	System.out.println(ID + " Node found");
                return m.getInfo();
            }
        }

        return null;
    }

    /**
     * In this time slot all ack message will have arrived If the node has sent
     * message requesting cluster formation verify if the permission was granted.
     */
    @Override
    public void timeslot6() {
        super.timeslot6();
        for(int largeId : this.getLargeIds()){
            Queue<AckClusterMessage> qTmp = this.queueAckClusters.getOrDefault(largeId, null);
            if(qTmp == null){
                continue;
            }
            if (!qTmp.isEmpty() && this.isClusterGranted(largeId)) {
                CBInfo target = findTarget(largeId);
                if (target != null) {
                    this.targetNodeFound(target);
                } else if (this.isClusterUps.getOrDefault(largeId, false)) {
                    this.clusterCompletedBottomUp(largeId, this.getClusterSequenceFromAckBufferBottomUp(largeId));
                } else if (this.isClusterDowns.getOrDefault(largeId, false)) {
                    this.clusterCompletedTopDown(largeId, this.getClusterSequenceFromAckBufferTopDown(largeId));
                }
            }
        }

        // reset queue
        this.clearClusterRequestQueue();
        this.clearAckClusterQueue();
        this.isClusterDowns.clear();
        this.isClusterUps.clear();

//        this.isClusterDown = false;
//        this.isClusterUp = false;
    }

    public abstract void clusterCompletedBottomUp(int largeId, HashMap<String, CBInfo> cluster);

    public abstract void clusterCompletedTopDown(int largeId, HashMap<String, CBInfo> cluster);

    public abstract void targetNodeFound(CBInfo target);

}

package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.SDNMessage.DeleteMessage;
import projects.cbrenet.nodes.messages.SDNMessage.EgoTreeMessage;
import projects.cbrenet.nodes.messages.SDNMessage.LinkMessage;
import projects.cbrenet.nodes.messages.SDNMessage.StatusChangedMessage;
import projects.cbrenet.nodes.messages.controlMessage.DeleteEgoTreeRequestMessage;
import projects.cbrenet.nodes.messages.controlMessage.DeleteRequestMessage;
import projects.cbrenet.nodes.messages.SDNMessage.LargeInsertMessage;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

import java.util.*;

public abstract class CounterBasedBSTLayer extends CounterBasedBSTLinkLayer implements Comparable<CounterBasedBSTLayer>{

    // basic field part


    // only use in the root node of Ego-Tree(large node): largeParent is the largeNode of the ego-tree
    private HashSet<Integer> largeParent;

    // only used in the small nodes and the small node that changing into large.
    private HashMap<Integer, Boolean> isRoots; // indicate whether the node is the root of ego tree under the large Node

    // The insertMessageExecuteFlags use to direct what to do when receive a LIM.
    private HashMap<Integer, Boolean> insertMessageExecuteFlags;





    // Getter & Setter
    public HashSet<Integer> getLargeParent() {
        return largeParent;
    }
    public void addLargeParent(int largeParent) {
        this.isRoots.put(largeParent, true);
        this.largeParent.add(largeParent);
    }


    public boolean isRoot(int largeId) {
        return this.isRoots.getOrDefault(largeId, false);
    }
    public void setRoot(int largeId, boolean isRoot) {
        this.isRoots.put(largeId, isRoot);
    }

    // call unsetRoot in make Large
    public void unsetRoot() {
        this.isRoots = new HashMap<>();
    }





    // LIM execution part
    public boolean checkInsertMessageExecuteFlags(int largeId){
        return this.insertMessageExecuteFlags.getOrDefault(largeId, false);
    }

    public void addInsertMessageExecuteFlags(int largeId){
        /**
         *@description Only use this method when LIM or LinkMessage received by a ego-tree node.
         *@parameters  [largeId]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/8
         */
        // Why True?
        // Check the method checkInsertMessageExecuteFlags, the default value is false.
        // When a node inserted into a tree completely (both p and children), execute this method!
        this.insertMessageExecuteFlags.put(largeId, true);
    }

    // Only use for the node in the ego-tree. Only use for LIM
    // Why Routing Message ? We have to satisfy the need of forwarding in LIM.
    private Queue<RoutingMessage> insertMessageQueue;

    // use this method when LIM can not execute
    private void addLargeInsertMessage(RoutingMessage message){
        assert message.getPayLoad() instanceof LargeInsertMessage;
        this.insertMessageQueue.add(message);
    }
    // LIM part finished!





    // todo
    // leaf part, check whether have l or r child

//    public boolean isLeaf(int largeId) {
//        // judge whether it is a leaf under the tree of largeId
//        return !(this.hasLeftChild(largeId) || this.hasRightChild(largeId));
//    }
//
//    public boolean hasLeftChild(int largeId) {
//        // we may have to change them since some large nodes may exist in the ego-tree
//        int left = -1;
//        if(!this.largeFlag){
//            left = this.leftChildren.getOrDefault(largeId, null);
//        }
//        return left != -1;
//    }
//
//    public boolean hasRightChild(int largeId) {
//        int right = -1;
//        if(!this.largeFlag){
//            right = this.rightChildren.getOrDefault(largeId, null);
//        }
//        return right != -1;
//    }

    // leaf part finish


    private boolean isConnectedTo(CounterBasedBSTLayer node) {
        return this.outgoingConnections.contains(this, node) && node.outgoingConnections
                .contains(node, this);
    }



    // Add Link Part

    private void addBidirectionalLinkTo(CounterBasedBSTLayer node) {
        if (node != null) {
            this.outgoingConnections.add(this, node, false);
            node.outgoingConnections.add(node, this, false);
        }
    }


    // todo 这些 addLinkTo 的函数们，似乎是用于rotate, check 是否满足我们的要求
    /**
     * Set the link to node and update reference to left child
     */
//    public void addBidirectionalLinkToLeftChild(int largeId, int id) {
//        // update current left child and create edge
//        CounterBasedBSTLayer node = (CounterBasedBSTLayer) Tools.getNodeByID(id);
//        this.addBidirectionalLinkTo(node);
//        this.setLeftChild(largeId,id);
//        if (node != null) {
//            node.setParent(largeId, this.ID);
//        }
//    }
//
//    /**
//     * Set the link to node and update reference to right child
//     */
//    public void addBidirectionalLinkToRightChild(int largeId, int id) {
//        CounterBasedBSTLayer node = (CounterBasedBSTLayer) Tools.getNodeByID(id);
//        this.addBidirectionalLinkTo(node);
//        this.setRightChild(largeId, id);
//        if (node != null) {
//            node.setParent(largeId, this.ID);
//        }
//    }

    public void addBidirectionalLinkToRootNode(int largeId, int id){
        CounterBasedBSTLayer node = (CounterBasedBSTLayer) Tools.getNodeByID(id);
        this.addBidirectionalLinkTo(node);
        this.setRootNodeId(id);
        if(node != null){
            node.addLargeParent(largeId);
        }
        else{
            Tools.fatalError("The ego tree's root of the large node " + largeId + " is empty!!");
        }
    }

//
//    // Add Link Part Finished!
//
//
//
//
//
//    public void removeBidirectionalLinkToLeftChild(int largeId, int id){
//        CounterBasedBSTLayer node = (CounterBasedBSTLayer) Tools.getNodeByID(id);
//        if(!this.removeLeftChildInRouteTable(largeId)){
//            return;
//        }
//        this.removeBidirectionalLinkTo(id);
//        if (node != null) {
//            node.removeParentInRouteTable(largeId);
//        }
//    }
//
//
//    public void removeBidirectionalLinkToRightChild(int largeId, int id){
//        CounterBasedBSTLayer node = (CounterBasedBSTLayer) Tools.getNodeByID(id);
//        // NOTE that this check can not replaced by the link connected!
//        if(!this.removeRightChildInRouteTable(largeId)){
//            return;
//        }
//        this.removeBidirectionalLinkTo(id);
//        if (node != null) {
//            node.removeParentInRouteTable(largeId);
//        }
//    }
//
//
//    /**
//     * Remove a link to a node that is not null.
//     *
//     */
//    private void removeBidirectionalLinkTo(int nodeId) {
//        /**
//         *@description This method remove bidirectional edge!!!
//         *             And LinkMessage only remove one side of the edge
//         *@parameters  [nodeId]
//         *@return  void
//         *@author  Zhang Hongxuan
//         *@create time  2021/2/20
//         */
//        // make sure only the parent remove the link
//        if(nodeId == -1){
//            return;
//        }
//        CounterBasedBSTLayer node = (CounterBasedBSTLayer) Tools.getNodeByID(nodeId);
//        if(node == null){
//            return;
//        }
//        if (this.isConnectedTo(node)) {
//            this.outgoingConnections.remove(this, node);
//            node.outgoingConnections.remove(node, this);
//        }
//        else {
//            Tools.fatalError("Trying to remove a non-existing connection to node " + ID);
//        }
//    }



    // send message part
    public abstract boolean sendToParent(int largeId, RoutingMessage msg);

    public abstract boolean sendToLeftChild(int largeId, RoutingMessage msg);

    public abstract boolean sendToRightChild(int largeId, RoutingMessage msg);
    // send message part finish


    @Override
    public void init() {

        // Insert Message Queue
        this.insertMessageQueue = new LinkedList<RoutingMessage>();
        this.insertMessageExecuteFlags = new HashMap<>();

    }



    private boolean executeLargeInsertMessage(RoutingMessage routingMessage){
        /**
         *@description This method is used to forward routingMessage contains LIM, do not need to
         *  put it in the LinkLayer. The target node of the LIM should execute LIM when globalStatusId
         *  satisfied!
         *@parameters  [routingMessage]
         *@return  boolean
         *@author  Zhang Hongxuan
         *@create time  2021/3/9
         */
        // Insert message is a special message which can not send directly to the true target
        assert routingMessage.getPayLoad() instanceof LargeInsertMessage;


        LargeInsertMessage insertMessage = (LargeInsertMessage) routingMessage.getPayLoad();
        int largeId = insertMessage.getLargeId();


        if(!this.checkInsertMessageExecuteFlags(largeId)){
            Tools.warning("The node " + this.ID + " is not prepared to execute LIM");
            this.addLargeInsertMessage(routingMessage);
            return false;
        }

        int target = insertMessage.getTarget();
        boolean forwardedFlag = true;
        boolean leftFlag = false;

        if(target < this.ID){
            // helpedId is useless, just used to fulfill the need of the interface
            if(this.getLeftChildOf(-1, largeId) != -1){
                if(!this.forwardMessage(routingMessage)){
                    ((MessageQueueLayer)this).addInRoutingMessageQueue(routingMessage);
                }
                forwardedFlag = true;
            }
            else{
                // InsertMessage has achieve the corresponding leaf of the ego-tree
                forwardedFlag = false;
                leftFlag = true;
            }
        }
        else{
            if(this.getRightChildOf(-1,largeId) != -1){
                if(!this.forwardMessage(routingMessage)){
                    ((MessageQueueLayer)this).addInRoutingMessageQueue(routingMessage);
                }
                forwardedFlag = true;
            }
            else{
                // InsertMessage has achieve the corresponding leaf of the ego-tree
                forwardedFlag = false;
                leftFlag = false;
            }
        }
        if(!forwardedFlag) {
            // has reached the leaf, create link to the target
            this.addConnectionTo(Tools.getNodeByID(target));
            if (leftFlag) {
                this.setLeftChild(largeId, target);
                insertMessage.setLeftFlag(true);
            }
            else{
                this.setRightChild(largeId, target);
                insertMessage.setLeftFlag(false);
            }
            insertMessage.setLeafId(this.ID);
            this.send(insertMessage, Tools.getNodeByID(target));
        }
        return true;
    }



    /**
     * The snoopingMessage method allow to intercept one forward message and decide if the message
     * should be routed or not.
     */
    @Override
    public void handleMessages(Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();
            if (msg instanceof RoutingMessage) {
                RoutingMessage rt = (RoutingMessage) msg;
                Message payload = rt.getPayLoad();
                int largeId = rt.getLargeId();
                if(rt.getDestination() == this.ID){
                    // have achieve the destination
                    if(payload instanceof CbRenetMessage) {
                        this.receiveMessage(payload);

                    }
                    else if(payload instanceof DeleteRequestMessage){
                        DeleteRequestMessage msgTmp = (DeleteRequestMessage) payload;
                        if(msgTmp.getDst() == this.ID){
                            if(!msgTmp.isEgo_tree()){
                                // todo 这条路径是否有必要保留？？
                                // SN 因为scm所以会从Ego-tree删除，但是CP也会同时删除，不需要这样一条DRM通知LN删除CP
                                // s_node -> SDN -> L_node
                                this.removeCommunicationPartner(msgTmp.getWantToDeleteId());
                                // Note that: 这个地方或许会被多次执行（即多次删除），因为StatusChangedMessage 也会对此进行修改
                            }
                            else{
                                // s_node -> ego-tree -> L_node
                                // LN tell SDN the node is prepared to delete!
                                int deleteId = msgTmp.getWantToDeleteId();
                                DeleteRequestMessage msgToSDN = new DeleteRequestMessage(this.ID, this.getSDNId(),
                                        true, deleteId);

                                this.nodeInEgoTreeArePreparedToDelete(deleteId);

                                if(this.isWaitAllDeleteRequestMessage()){
                                    if(this.checkWhetherAllNodePrepareToDelete()){
                                        // All node in the ego tree are prepared to delete
                                        DeleteEgoTreeRequestMessage deleteEgoTreeMessage = new DeleteEgoTreeRequestMessage(this.ID,
                                                new HashSet<Integer>(this.getEgoTreeDeleteMap().keySet()));
                                        this.send(deleteEgoTreeMessage, Tools.getNodeByID(this.getSDNId()));
                                    }
                                    else{
                                        // Continue waiting
                                    }
                                }
                                else{
                                    //  then send to SDN node
                                    this.sendDirect(msgToSDN, Tools.getNodeByID(this.getSDNId()));
                                }

                            }
                        }
                        else{
                            Tools.fatalError("DeleteRequestMessage has been sent to a wrong node!!!");
                        }
                    }

                    // todo 这里或许还需要几种message
                    else{
                        Tools.fatalError("Some message in the RoutingMessage is " + msg.getClass() + " and being received");
                    }
                }
                else{
                    // not get destination
                    if(payload instanceof LargeInsertMessage){
                        // LargeInsertMessage need a special forward procedure
                        this.executeLargeInsertMessage(rt);
                    }
                    else{
                        // not InsertMessage and not get destination
                        // only need to forward
                        if(!this.forwardMessage(rt)){
                            ((MessageQueueLayer)this).addInRoutingMessageQueue(rt);
                        }
                    }
                }
            }
            else if(msg instanceof CbRenetMessage){
                CbRenetMessage cm = (CbRenetMessage) msg;
                int destination = cm.getDestination();
                if(destination == this.ID){
                    this.receiveMessage(msg);
                }
                else{
                    Tools.fatalError("A strange CbRenetMessage has received by the node " + this.ID + " " +
                            ",please check what happened");
                }
            }
            else if(msg instanceof StatusChangedMessage){
                this.receiveMessage(msg);
            }
            else if(msg instanceof EgoTreeMessage){
                this.receiveMessage(msg);
            }
            else if(msg instanceof LargeInsertMessage){
                LargeInsertMessage insertMessage = (LargeInsertMessage) msg;
                int largeId = insertMessage.getLargeId();
                int target = insertMessage.getTarget();

                if(this.ID == largeId){
                    // Means this node is the large node that receive the LargeInsertMessage from the SDN node
                    if(insertMessage.isInserted()){
                        // means the node has been inserted into the ego-tree

                        this.addEgoTreeNodeToDeleteMap(target);
                        // first , check whether it is small
                        if(this.isNodeSmall(target)){
                            this.addCommunicationPartner(target);
                        }
                    }
                    else {
                        this.sendEgoTreeMessage(this.ID, target, msg);
                    }
                }
                else if(target == this.ID){
                    this.receiveMessage(msg);
                }
                else{
                    Tools.fatalError("A LargeInsertMessage has been sent to a wrong node. Please Check what happened!");
                }
            }
            else if(msg instanceof LinkMessage){
                this.receiveMessage(msg);
            }
            else if(msg instanceof DeleteMessage){
                this.receiveMessage(msg);
            }
            else {
                this.receiveMessage(msg);
            }
        }
    }


    protected void sendForwardMessage(int dst, Message msg) {
        // TODO 这是cbent中的，因为似乎关系到Rotation Layer故在此保留 反正后面要改先留着后面再删除
        if (dst == this.ID) {
            this.receiveMessage(msg);
            return;
        }
        int largeId = -1; // todo 我们要在这里， 得到largeId
        RoutingMessage rt = new RoutingMessage(this.ID, dst, msg, largeId, );
        forwardMessage(rt);
    }



    // abstract method
    public abstract void sendEgoTreeMessage(int largeId, int dst, Message msg) ;

    protected abstract boolean forwardMessage(RoutingMessage msg);

    // use this to give message received to other layers.
    public abstract void receiveMessage(Message msg);

    // abstract method part finish


    //  override method in the Node

    /**
     * if the function return false the message will not be forward to next node
     */
    public boolean snoopingMessage(Message msg) {
        return true;
    }

    @Override
    public void neighborhoodChange() {
        // nothing to do
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {
        // nothing to do
    }

    @Override
    public int compareTo(CounterBasedBSTLayer o) {
        return ID - o.ID;
    }




    protected void doInPostRound(){

        // execute the LIM in the queue

        Queue<RoutingMessage> unexecutedQueue = new LinkedList<>();

        while(!this.insertMessageQueue.isEmpty()){
            RoutingMessage routingMessage = insertMessageQueue.poll();

            if(! this.executeLargeInsertMessage(routingMessage)){
                unexecutedQueue.add(routingMessage);
            }

        }
        this.insertMessageQueue.addAll(unexecutedQueue);

        // LIM part finished!

    }


}

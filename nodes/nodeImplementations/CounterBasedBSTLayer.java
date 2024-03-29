package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.CustomGlobal;
import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.SDNMessage.*;
import projects.cbrenet.nodes.messages.controlMessage.DeleteEgoTreeRequestMessage;
import projects.cbrenet.nodes.messages.controlMessage.DeleteRequestMessage;
import projects.cbrenet.nodes.messages.controlMessage.clusterMessage.AcceptOrRejectBaseMessage;
import projects.cbrenet.nodes.messages.controlMessage.clusterMessage.ClusterRelatedMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteBaseMessage;
import projects.cbrenet.nodes.nodeImplementations.nodeHelper.ClusterHelper;
import projects.cbrenet.nodes.nodeImplementations.nodeHelper.SimpleClusterHelper;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

import java.util.*;

public abstract class CounterBasedBSTLayer extends CounterBasedBSTLinkLayer implements Comparable<CounterBasedBSTLayer>{

    // basic field part

    // The insertMessageExecuteFlags use to direct what to do when receive a LIM.
    private HashMap<Integer, Boolean> insertMessageExecuteFlags;

    //ClusterHelper clusterHelper = ClusterHelper.getInstance();
    SimpleClusterHelper clusterHelper = SimpleClusterHelper.getInstance();


    @Override
    public void init() {

        super.init();

        // Insert Message Queue
        this.insertMessageQueue = new LinkedList<RoutingMessage>();
        this.insertMessageExecuteFlags = new HashMap<>();

    }





    // LIM execution part
    public boolean checkInsertMessageExecuteFlags(int largeId){
        if(!this.insertMessageExecuteFlags.getOrDefault(largeId, false)){
            return false;
        }
        else{
            // pass the LIM test, but the node may involve in deleting or rotation.
            SendEntry entry = this.getCorrespondingEntry(-1, largeId);
            if(entry == null){
                Tools.fatalError("We are checking whether should execute LIM, Entry should not be null!");
                return false;
            }
            if(entry.isDeleteFlag()){
                return false;
            }
            return true;
        }
    }

    public void addInsertMessageExecuteFlags(int largeId){
        /**
         *@description Only use this method when LIM or LinkMessage received by a ego-tree node.
         *              收到LIM or LinkMessage时，做这件事
         *@parameters  [largeId]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/8
         */
        // Why default value is True?
        // Check the method checkInsertMessageExecuteFlags, the default value is false.
        // When a node inserted into a tree completely (both p and children), execute this method!
        if(this.insertMessageExecuteFlags.containsKey(largeId)){
            if(!this.insertMessageExecuteFlags.get(largeId)){
                this.insertMessageExecuteFlags.put(largeId, true);
            }
        }
        else{
            this.insertMessageExecuteFlags.put(largeId, true);
        }
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






    private boolean executeLargeInsertMessage(RoutingMessage routingMessage){
        /**
         *@description This method is used to forward routingMessage contains LIM, do not need to
         *  put it in the LinkLayer. The target node of the LIM should execute LIM when globalStatusId
         *  satisfied!
         *@parameters  [routingMessage]
         *@return  boolean
         *             false when : not satisfied to execute yet
         *             true when : sent or add to the node
         *@author  Zhang Hongxuan
         *@create time  2021/3/9
         */
        // Insert message is a special message which can not send directly to the true target
        assert routingMessage.getPayLoad() instanceof LargeInsertMessage;


        LargeInsertMessage insertMessage = (LargeInsertMessage) routingMessage.getPayLoad();
        int largeId = insertMessage.getLargeId();


        if(!this.checkInsertMessageExecuteFlags(largeId)){
            Tools.warning("The node " + this.ID + " is not prepared to execute LIM");

            // should not add not executed LIM here
            // this.addLargeInsertMessage(routingMessage);
            return false;
        }

        int target = insertMessage.getTarget();
        boolean forwardedFlag = true;
        boolean leftFlag = false;

        if(target < this.ID){
            // helpedId is useless, just used to fulfill the need of the interface
            if(this.getEgoTreeIdOfLeftChild(-1, largeId) != -1){
                if(!this.forwardMessage(routingMessage)){
                    return false;
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
            if(this.getEgoTreeIdOfRightChild(-1,largeId) != -1){
                if(!this.forwardMessage(routingMessage)){
                    return false;
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
            if (leftFlag) {
                this.addLinkToLeftChild(largeId, target, target);
                insertMessage.setLeftFlag(true);
            }
            else{
                this.addLinkToRightChild(largeId, target, target);
                insertMessage.setLeftFlag(false);
            }
            insertMessage.setLeafId(this.ID);
            // fixme bug. 如果当前结点要删除或者调整呢？？
            this.send(insertMessage, Tools.getNodeByID(target));
        }
        return true;
    }

    private void executeDeleteRequestMessage(DeleteRequestMessage deleteRequestMessage){
        if(deleteRequestMessage.getDst() == this.ID){
            if(!deleteRequestMessage.isEgo_tree()){
                // todo 这条路径是否有必要保留？？
                // SN 因为scm所以会从Ego-tree删除，但是CP也会同时删除，不需要这样一条DRM通知LN删除CP
                // s_node -> SDN -> L_node
                this.removeCommunicationPartner(deleteRequestMessage.getWantToDeleteId());
                // Note that: 这个地方或许会被多次执行（即多次删除），因为StatusChangedMessage 也会对此进行修改
            }
            else{
                // s_node -> ego-tree -> L_node
                // LN tell SDN the node is prepared to delete!
                int deleteId = deleteRequestMessage.getWantToDeleteId();
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


    /**
     * The snoopingMessage method allow to intercept one forward message and decide if the message
     * should be routed or not.
     */
    @Override
    public void handleMessages(Inbox inbox) {

        if(this.ID == 530)
        {
            int saidjo =2 ;
        }

        while (inbox.hasNext()) {
            Message msg = inbox.next();
            if (msg instanceof RoutingMessage) {
                RoutingMessage routingMessageTmp = (RoutingMessage) msg;
                Message payload = routingMessageTmp.getPayLoad();
                int largeId = routingMessageTmp.getLargeId();
                if(routingMessageTmp.getDestination() == this.ID){
                    // have achieve the destination
                    if(payload instanceof CbRenetMessage) {

                        SendEntry entry = this.getCorrespondingEntry(-1, largeId);
                        // counter ++;
                        if(entry != null) {
                            entry.incrementCounter();
                            entry.setRotationAbleFlag(true);
                        }
                        this.receiveMessage(payload);
                    }
                    else if(payload instanceof DeleteRequestMessage){
                        this.executeDeleteRequestMessage((DeleteRequestMessage) payload);
                    }
                    else if(payload instanceof DeleteBaseMessage){
                        this.receiveMessage(payload);
                    }
                    else if(payload instanceof ClusterRelatedMessage){
                        this.receiveMessage(payload);
                    }
                    else if(payload instanceof LargeInsertMessage){
                        this.receiveMessage(payload);
                    }
                    else{
                        Tools.warning("CBBSTLayer: Some messages in the RoutingMessage is " + msg.getClass().getSimpleName() + " and being received");
                    }
                }
                else{
                    // not get destination
                    if(payload instanceof LargeInsertMessage){
                        // LargeInsertMessage need a special forward procedure
                        if(!this.executeLargeInsertMessage(routingMessageTmp)){
                            this.addLargeInsertMessage(routingMessageTmp);
                        }
                    }
                    else if(payload instanceof AcceptOrRejectBaseMessage){
                        // 如果是RejectMessage的话，也需要关心一下
                        this.receiveMessage(payload);
                    }
                    else{
                        // not InsertMessage and not get destination
                        // only need to forward
                        if(payload instanceof CbRenetMessage){
                            ((CbRenetMessage) payload).incrementRouting();

                            if(routingMessageTmp.isUpForward()){
                                // come from child, so i need to increment the weight
                                int src =  ((CbRenetMessage) payload).getSrc();

                                SendEntry entry = this.getCorrespondingEntry(-1, largeId);
                                if(src < this.ID){
                                    // left child
                                    entry.incrementWeightOfLeft();
                                }
                                else{
                                    entry.incrementWeightOfRight();
                                }
                            }
                            //自上而下的消息，weight在转发时增加
                        }


                        if(!this.forwardMessage(routingMessageTmp)){
                            ((MessageQueueLayer)this).addInRoutingMessageQueue(routingMessageTmp);
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

                        if(this.getRootSendId() > 0){
                            this.sendEgoTreeMessage(this.ID, target, msg);
                        }
                        else{
                            this.addLinkToRootNode(largeId, target);
                            insertMessage.setLeafId(this.ID);
                            this.sendEgoTreeMessage(this.ID, target, msg);
                        }

                    }
                }
                else if(target == this.ID){
                    this.receiveMessage(msg);
                }
                else{
                    Tools.fatalError("A LargeInsertMessage has been sent to a wrong node. Please Check what happened!");
                }
            }
            else if(msg instanceof StatusRelatedMessage){
                this.receiveMessage(msg);
            }
            else {
                this.receiveMessage(msg);
            }
        }
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
    public void postStep() {
        this.doInPostRound();
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
                int largeId = routingMessage.getLargeId();
                SendEntry entry = this.getCorrespondingEntry(-1, largeId);
                if(entry.isDeleteFlag()){
                    this.sendTo(entry.getEgoTreeIdOfParent(), routingMessage);
                }
                unexecutedQueue.add(routingMessage);
            }
        }
        this.insertMessageQueue.addAll(unexecutedQueue);

        // LIM part finished!



    }


}

package projects.cbrenet.nodes.nodeImplementations;



/*
* In this layer, I will rebuild the data structure of the CBBST in the ego-tree. */

import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.nodeImplementations.forwardSendHelper.EntryGetter;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.tools.Tools;

import java.util.HashMap;

/* Use SendEntry to provide a routeTable and sendTo method */

public abstract class CounterBasedBSTStructureLayer extends CommunicatePartnerLayer implements EntryGetter {

    // only used in the large node. root node id is the id of the node which connect to the large node directly!
    // Even we have SendEntry, we still need this field as the link to the Ego-Tree(LN).
    private int rootNodeId = -1;
    // rootNodeId can be used as SendID actually, since LN do not need a egoTreeID.

    private boolean rootNodeSendFlag = true;

    // root node id & send Flag getter & setter
    public void setRootNodeSendFlag(boolean rootNodeSendFlag) {
        this.rootNodeSendFlag = rootNodeSendFlag;
    }

    public boolean isRootNodeSendFlag() {
        return rootNodeSendFlag;
    }

    public int getRootNodeId() {
        return this.rootNodeId;
    }

    public void setRootNodeId(int rootNodeId) {
        this.rootNodeId = rootNodeId;
    }




    HashMap<Integer, SendEntry> routeTable;  // 指示着当前的结点在 Ego-Tree(largeId)中的情况




    public SendEntry getSendEntryOf(int largeId){
        return this.routeTable.getOrDefault(largeId,null);
    }

    public void addSendEntry(int largeId, int parent, int leftChild, int rightChild){
        SendEntry entry = new SendEntry(parent, leftChild, rightChild);
        this.routeTable.put(largeId, entry);
    }

    // May use it in cluster layer or rotation layer
    public char getRelationShipTo(int largeId, int targetId){
        SendEntry entry = this.getSendEntryOf(largeId);
        if(entry == null){
            return 'w';
        }
        else{
            return entry.getRelationShipTo(targetId);
        }
    }


    public boolean sendTo(int egoTreeTargetID, RoutingMessage routingMessage){
        /**
         *@description  调用这个method的时候，只需要提供routingMessage 要去的 egoTreeId 即可
         *              Ego-Tree 的根结点请提供rootID
         *@parameters  [egoTreeTargetID, routingMessage]
         *              Here must be egoTreeTargetID, since the auxiliary node use this to forward message!
         *@return  boolean
         *@author  Zhang Hongxuan
         *@create time  2021/3/15
         */

        if(egoTreeTargetID < 0){
            Tools.fatalError("In sendTo method of CBBST node" + this.ID
                    + ", the egoTreeTargetID < 0 !");
            return false;
        }

        int largeId = routingMessage.getLargeId();

        if(this.ID == largeId){
            // Which indicate that this node is the node that send the rt message, it need
            // a special forwarding mechanism

            int rootID = this.getRootNodeId();

            if(rootID < 0){
                Tools.warning("LN" + this.ID + " need to send a forwarding message, " +
                        "but the rootNodeId is wrong:" + rootID );
                return false;
            }

            if(rootID != egoTreeTargetID){
                Tools.fatalError("LN" + this.ID + " need to send a forwarding message, " +
                        "but the rootNodeId is not equal to the egoTreeId provided by the node " + rootID + " &" +
                        " " + egoTreeTargetID );
                return false;
            }

            if(this.outgoingConnections.contains(this, Tools.getNodeByID(rootID))){
                boolean sendFlag = this.rootNodeSendFlag;
                if(sendFlag){
                    routingMessage.setNextHop(rootID); // when the node is sure that the message would be sent, change it !
                    this.send(routingMessage,Tools.getNodeByID(rootID));
                    return true;
                }
                else{
                    Tools.warning("Can not send a message since the send Flag " +
                            "is False: from " + this.ID +" to the root node in ego-tree : " + largeId);
                    return false;
                }
            }
            else{
                Tools.warning("Node " + this.ID +" want to send a Message to " + rootID + ", but the" +
                        " corresponding link not exist by auxiliary node.");
                return false;
            }
        }

        SendEntry entry = this.getSendEntryOf(largeId);


        if(entry != null){
            int targetID = entry.getSendIdOf(egoTreeTargetID);
            if(this.outgoingConnections.contains(this, Tools.getNodeByID(targetID))){
                boolean sendFlag = entry.getSendFlag(egoTreeTargetID);
                if(sendFlag){
                    routingMessage.setNextHop(egoTreeTargetID); // when the node is sure that the message would be sent, change it !
                    this.send(routingMessage,Tools.getNodeByID(targetID));
                    return true;
                }
                else{
                    Tools.warning("Can not send a message since the send Flag " +
                            "is False: from " + this.ID +" to " + targetID + " in " + largeId);
                    return false;
                }
            }
            else{
                Tools.fatalError("Node " + this.ID +" want to send a Message to " + targetID + ", but the" +
                        " corresponding link not exist by auxiliary node.");
                return false;
            }
        }
        else{
            Tools.warning("Node " + this.ID +" want to send a Message to " + egoTreeTargetID + ", but the corresponding entry in CBBST node" +
                    " is null");
            return false;
        }

    }




    // Getter & Setter

    private int getNeighbor(int largeId, char relation){
        /**
         *@description This method only get egoTreeID, remember that use egoTreeID to get SendID!!
         *@parameters  [largeId, relation]
         *@return  int
         *@author  Zhang Hongxuan
         *@create time  2021/3/15
         */
        SendEntry entry = this.getSendEntryOf(largeId);
        if(entry != null){
            switch (relation){
                case 'p':
                    return entry.getEgoTreeIdOfParent();
                case 'l':
                    return entry.getEgoTreeIdOfLeftChild();
                case 'r':
                    return entry.getEgoTreeIdOfRightChild();
                default:
                    return -1;
            }
        }
        else{
            return -1;
        }
    }

    private void setNeighbor(int largeId, char relation, int nodeId, boolean egoTreeFlag){
        /**
         *@description
         *@parameters  [largeId, relation, nodeId, egoTreeFlag]
         *          // nodeId : value
         *          // egoTreeFlag : indicate that whether the node id is egoTreeID or sendID
         *          // T : egoTreeID
         *          // F : sendID
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/15
         */
        SendEntry entry = this.getSendEntryOf(largeId);
        if(entry != null){
            if(egoTreeFlag){
                switch (relation){
                    case 'p':
                        entry.setEgoTreeIdOfParent(nodeId);
                        break;
                    case 'l':
                        entry.setEgoTreeIdOfLeftChild(nodeId);
                        break;
                    case 'r':
                        entry.setEgoTreeIdOfRightChild(nodeId);
                        break;
                    default:
                        break;
                }
            }
            else{
                switch (relation){
                    case 'p':
                        entry.setSendIdOfParent(nodeId);
                        break;
                    case 'l':
                        entry.setSendIdOfLeftChild(nodeId);
                        break;
                    case 'r':
                        entry.setSendIdOfRightChild(nodeId);
                        break;
                    default:
                        break;
                }
            }
            this.routeTable.put(largeId, entry);
        }
    }

    // These three methods only set egoTreeId
    public void setParent(int largeId ,int parent) {
        this.setNeighbor(largeId, 'p',parent, true);
    }

    public void setLeftChild(int largeId,int leftChild) {
        this.setNeighbor(largeId, 'l',leftChild, true);
    }

    public void setRightChild(int largeId, int rightChild) {
        this.setNeighbor(largeId, 'r',rightChild, true);
    }

    public int getParentOf(int helpedId, int largeId) {
        return this.getNeighbor(largeId,'p');
    }

    public int getLeftChildOf(int helpedId, int largeId) {
        return this.getNeighbor(largeId,'l');
    }

    public int getRightChildOf(int helpedId, int largeId) {
        return this.getNeighbor(largeId, 'r');
    }

}

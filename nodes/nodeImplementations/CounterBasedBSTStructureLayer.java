package projects.cbrenet.nodes.nodeImplementations;



/*
* In this layer, I will rebuild the data structure of the CBBST in the ego-tree. */

import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.tools.Tools;

import java.util.HashMap;

public abstract class CounterBasedBSTStructureLayer extends CommunicatePartnerLayer{

    HashMap<Integer, SendEntry> routeTable;  // 指示着当前的结点在 Ego-Tree(largeId)中的情况

    private SendEntry getSendEntryOf(int largeId){
        return this.routeTable.getOrDefault(largeId,null);
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
         *@description
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


}

package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.nodeImplementations.nodeHelper.EntryGetter;
import projects.cbrenet.nodes.nodeImplementations.nodeHelper.LinkHelper;
import projects.cbrenet.nodes.routeEntry.AuxiliarySendEntry;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public abstract class AuxiliaryNodeStructureLayer extends Node implements EntryGetter {

    LinkHelper linkHelper = new LinkHelper();


    public Queue<RoutingMessage> cycleRoutingMessage = new LinkedList<>();

    private HashMap<Integer, HashMap<Integer, AuxiliarySendEntry>> routeTable;
    // Note that the key is helpedId, and the inner key is largeId.
    //  helpedId 15,   LN 3,    p, l, r  ;

    public HashMap<Integer, HashMap<Integer, AuxiliarySendEntry>> getRouteTable(){
        return this.routeTable;
    }


    public boolean sendTo(int egoTreeTargetID, RoutingMessage routingMessage){
        /**
         *@description Used to send routing message in helped ego-tree.
         *             This method also set nextHop bit in the RoutingMessage!
         *@parameters  [egoTreeTargetID, routingMessage]
         *              // Only accept egoTreeID is enough !
         *              // IMPORTANT !!!
         *@return  boolean
         *@author  Zhang Hongxuan
         *@create time  2021/3/14
         */

        if(egoTreeTargetID < 0){
            Tools.fatalError("In sendTo method of auxiliary node, the egoTreeTargetID < 0 !");
            return false;
        }

        int largeId = routingMessage.getLargeId();
        int helpedId = routingMessage.getNextHop(); // still unchanged!!!

        AuxiliarySendEntry entry = this.getCorrespondingEntry(helpedId, largeId);

        if(entry != null){

            int targetID = entry.getSendIdOf(egoTreeTargetID);

            if(targetID == this.ID){
                boolean sendFlag = entry.getSendFlag(egoTreeTargetID);
                if(sendFlag){
                    routingMessage.setNextHop(egoTreeTargetID); // when the node is sure that the message would be sent, change it !
                    this.cycleRoutingMessage.add(routingMessage);
                    return true;
                }
                else{
                    Tools.warning("Can not send a message since the send Flag " +
                            "is False: " + helpedId +" " + largeId);
                    return false;
                }
            }


            if(this.outgoingConnections.contains(this, Tools.getNodeByID(targetID))){
                boolean sendFlag = entry.getSendFlag(egoTreeTargetID);
                if(sendFlag){
                    routingMessage.setNextHop(egoTreeTargetID); // when the node is sure that the message would be sent, change it !
                    this.send(routingMessage,Tools.getNodeByID(egoTreeTargetID));
                    return true;
                }
                else{
                    Tools.warning("Can not send a message since the send Flag " +
                            "is False: " + helpedId +" " + largeId);
                    return false;
                }
            }
            else{
                Tools.fatalError("Want to send a Message to " + egoTreeTargetID + ", but the" +
                        " corresponding link not exist" + helpedId + " by auxiliary node.");
                return false;
            }
        }
        else{
            Tools.warning("Want to send a Message to " + egoTreeTargetID + ", but the corresponding entry in auxiliary node" +
                    " is null");
            return false;
        }
    }

    public void addSendEntry(int helpedId, int largeId, int parent, int leftChild, int rightChild){
        assert leftChild != -1 && rightChild != -1;
        // if one of them are -1, means the node can be delete!
        if(this.routeTable.containsKey(helpedId)){
            HashMap<Integer, AuxiliarySendEntry> entriesTmp = this.routeTable.get(helpedId);
            if(entriesTmp.containsKey(largeId)){
                Tools.fatalError("The Auxiliary node can not help a node in the same ego-tree, please" +
                        "check why not the LargeInsertMessage insert the corresponding node into the Ego-Tree");
                return;
            }
            entriesTmp.put(largeId, new AuxiliarySendEntry(parent, leftChild, rightChild));
        }
        else{
            AuxiliarySendEntry auxiliarySendEntry = new AuxiliarySendEntry(parent, leftChild, rightChild);
            HashMap<Integer, AuxiliarySendEntry> entryHashMap = new HashMap<>();
            entryHashMap.put(largeId, auxiliarySendEntry);
            this.routeTable.put(helpedId, entryHashMap);
        }
    }


    // delete node 时调用
    public void removeCorrespondingEntry(int helpedId, int largeId){
        /**
         *@description Call this method when the node is satisfied to truly delete from the
         *             Ego-Tree(largeId) (In Complete Delete Phase)
         *             Or when the whole tree is deleted!
         *@parameters  [helpedId, largeId]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/11
         */
        if(this.routeTable.containsKey(helpedId)){
            HashMap<Integer, AuxiliarySendEntry> entriesTmp = this.routeTable.get(helpedId);
            if(entriesTmp.containsKey(largeId)){
                entriesTmp.remove(largeId);
                this.routeTable.replace(helpedId, entriesTmp);
            }
            else{
                Tools.warning("The auxiliary node want to delete helped entry helpedId: " + helpedId + ", largeId: " +
                        largeId+ " but not exist! LargeId Part!");
            }
        }
        else{
            Tools.warning("The auxiliary node want to delete helped entry helpedId: " + helpedId + ", largeId: " +
                    largeId+ " but not exist! HelpedId Part!");
        }
    }

    public int getEgoTreeIdOfLeftChild(int helpedId, int largeId){
        AuxiliarySendEntry entry = this.getCorrespondingEntry(helpedId, largeId);
        if(entry != null){
            return entry.getEgoTreeIdOfLeftChild();
        }
        else{
            return -1;
        }
    }

    public int getEgoTreeIdOfRightChild(int helpedId, int largeId){
        AuxiliarySendEntry entry = this.getCorrespondingEntry(helpedId, largeId);
        if(entry != null){
            return entry.getEgoTreeIdOfRightChild();
        }
        else{
            return -1;
        }
    }

    public int getEgoTreeIdOfParent(int helpedId, int largeId){
        AuxiliarySendEntry entry = this.getCorrespondingEntry(helpedId, largeId);
        if(entry != null){
            return entry.getEgoTreeIdOfParent();
        }
        else{
            return -1;
        }
    }

    public AuxiliarySendEntry getCorrespondingEntry(int helpedId, int largeId){
        if(this.routeTable.containsKey(helpedId)){
            HashMap<Integer, AuxiliarySendEntry> entryHashMap = this.routeTable.get(helpedId);
            return entryHashMap.getOrDefault(largeId, null);
        }
        else{
            return null;
        }
    }

    public boolean checkRemoveEntryCondition(int leftEgoId, int rightEgoId){
        return (leftEgoId<0 || rightEgoId < 0);
    }




    public boolean addLinkToLeftChild(int largeId, int egoTreeId, int sendId, int helpedId){
        return this.linkHelper.addLinkToLeftChild(largeId, egoTreeId, sendId, helpedId,this, this);
    }
    public boolean addLinkToRightChild(int largeId, int egoTreeId, int sendId, int helpedId){
        return this.linkHelper.addLinkToRightChild(largeId, egoTreeId, sendId, helpedId, this, this);
    }
    public boolean addLinkToParent(int largeId, int egoTreeId, int sendId, int helpedId){
        return this.linkHelper.addLinkToParent(largeId, egoTreeId, sendId, helpedId, this, this);
    }


    public boolean changeLeftChildTo(int largeId, int egoTreeId, int sendId, int helpedId){
        return this.linkHelper.changeLeftChildTo(largeId,egoTreeId,sendId, helpedId, this, this);
    }
    public boolean changeRightChildTo(int largeId, int egoTreeId, int sendId, int helpedId){
        return this.linkHelper.changeRightChildTo(largeId,egoTreeId,sendId, helpedId, this, this);
    }
    public boolean changeParentTo(int largeId, int egoTreeId, int sendId, int helpedId){
        return this.linkHelper.changeParentTo(largeId,egoTreeId,sendId, helpedId, this, this);
    }

}

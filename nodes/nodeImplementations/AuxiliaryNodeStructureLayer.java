package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.RoutingMessage;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

import java.util.HashMap;
import java.util.Queue;

public abstract class AuxiliaryNodeStructureLayer extends Node {

    private HashMap<Integer, HashMap<Integer, HelpIdEntry>> routeTable;
    // Note that the key is helpedId, and the inner key is largeId.
    //  helpedId 15,   LN 3,    p, l, r  ;

    private class HelpIdEntry {
        int parent;
        int leftChild;
        int rightChild;

        HelpIdEntry(int parent, int leftChild, int rightChild){
            this.parent = parent;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
        }

        boolean sendFlagOfParent = true;
        boolean sendFlagOfLeftChild = true;
        boolean sendFlagOfRightChild = true;

        public boolean getSendFlag(int id){
            if(id == this.parent){
                return this.sendFlagOfParent;
            }
            else if(id == this.leftChild){
                return this.sendFlagOfLeftChild;
            }
            else if(id == this.rightChild){
                return this.sendFlagOfRightChild;
            }
            else{
                return false;
            }
        }



    }

    public boolean sendTo(int targetID, RoutingMessage routingMessage){
        /**
         *@description Used to send routing message in helped ego-tree.
         *             This method also set nextHop bit in the RoutingMessage!
         *@parameters  [targetID, routingMessage]
         *@return  boolean
         *@author  Zhang Hongxuan
         *@create time  2021/3/14
         */

        int largeId = routingMessage.getLargeId();
        int helpedId = routingMessage.getNextHop(); // still unchanged!!!
        if(this.outgoingConnections.contains(this, Tools.getNodeByID(targetID))){
            HelpIdEntry entry = this.getCorrespondingEntry(helpedId, largeId);
            if(entry != null){
                boolean sendFlag = entry.getSendFlag(targetID);
                if(sendFlag){
                    routingMessage.setNextHop(targetID); // when the node is sure that the message would be sent, change it !
                    this.send(routingMessage,Tools.getNodeByID(targetID));
                    return true;
                }
                else{
                    Tools.warning("Can not send a message since the send Flag " +
                            "is False: " + helpedId +" " + largeId);
                    return false;
                }
            }
            else{
                Tools.warning("Want to send a Message to " + targetID + ", but the corresponding entry in auxiliary node" +
                        " is null");
                return false;
            }
        }
        else{
            Tools.fatalError("Want to send a Message to " + targetID + ", but the" +
                    " corresponding link not exist" + helpedId + " by auxiliary node.");
            return false;
        }
    }

    public void addHelpIdEntry(int helpedId, int largeId, int parent, int leftChild, int rightChild){
        assert leftChild != -1 && rightChild != -1;
        // if one of them are -1, means the node can be delete!
        if(this.routeTable.containsKey(helpedId)){
            HashMap<Integer, HelpIdEntry> entriesTmp = this.routeTable.get(helpedId);
            if(entriesTmp.containsKey(largeId)){
                Tools.fatalError("The Auxiliary node can not help a node in the same ego-tree, please" +
                        "check why not the LargeInsertMessage insert the corresponding node into the Ego-Tree");
                return;
            }
            entriesTmp.put(largeId, new HelpIdEntry(parent, leftChild, rightChild));
        }
    }

    public void removeHelpIdEntry(int helpedId, int largeId){
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
            HashMap<Integer, HelpIdEntry> entriesTmp = this.routeTable.get(helpedId);
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

    public int getLeftChildOf(int helpedId, int largeId){
        HelpIdEntry entry = this.getCorrespondingEntry(helpedId, largeId);
        if(entry != null){
            return entry.leftChild;
        }
        else{
            return -1;
        }
    }

    public int getRightChildOf(int helpedId, int largeId){
        HelpIdEntry entry = this.getCorrespondingEntry(helpedId, largeId);
        if(entry != null){
            return entry.rightChild;
        }
        else{
            return -1;
        }
    }

    public int getParentOf(int helpedId, int largeId){
        HelpIdEntry entry = this.getCorrespondingEntry(helpedId, largeId);
        if(entry != null){
            return entry.parent;
        }
        else{
            return -1;
        }
    }

    public HelpIdEntry getCorrespondingEntry(int helpedId, int largeId){
        if(this.routeTable.containsKey(helpedId)){
            HashMap<Integer, HelpIdEntry> entryHashMap = this.routeTable.get(helpedId);
            return entryHashMap.getOrDefault(largeId, null);
        }
        else{
            return null;
        }
    }



}

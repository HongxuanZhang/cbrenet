package projects.cbrenet.nodes.nodeImplementations;

import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class AuxiliaryNodeStructureLayer extends Node {

    private class helpIdEntry{
        int parent;
        int leftChild;
        int rightChild;

        helpIdEntry(int parent, int leftChild, int rightChild){
            this.parent = parent;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
        }

    }

    public HashMap<Integer, HashMap<Integer, helpIdEntry>> routeTable;
    // Note that the key is helpedId, and the inner key is largeId.
    //  helpedId 15,   LN 3,    p, l, r  ;

    public void addHelpIdEntry(int helpedId, int largeId, int parent, int leftChild, int rightChild){
        assert leftChild != -1 && rightChild != -1;
        // if one of them are -1, means the node can be delete!
        if(this.routeTable.containsKey(helpedId)){
            HashMap<Integer, helpIdEntry> entriesTmp = this.routeTable.get(helpedId);
            if(entriesTmp.containsKey(largeId)){
                Tools.fatalError("The Auxiliary node can not help a node in the same ego-tree, please" +
                        "check why not the LargeInsertMessage insert the corresponding node into the Ego-Tree");
                return;
            }
            entriesTmp.put(largeId, new helpIdEntry(parent, leftChild, rightChild));
        }
    }

    public void removeHelpIdEntry(int helpedId, int largeId){
        /**
         *@description Call this method when the node is satisfied to truly delete from the
         *             Ego-Tree(largeId)
         *             Or when the whole tree is deleted!
         *@parameters  [helpedId, largeId]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/11
         */
        if(this.routeTable.containsKey(helpedId)){
            HashMap<Integer, helpIdEntry> entriesTmp = this.routeTable.get(helpedId);
            if(entriesTmp.containsKey(largeId)){
                entriesTmp.remove(largeId);
                this.routeTable.replace(helpedId, entriesTmp);
            }
            else{
                Tools.warning("The auxiliary node want to delete helped entry helpedId: " + helpedId + ", largeId: " +
                        largeId+ " but not exist!");
            }
        }
    }

}

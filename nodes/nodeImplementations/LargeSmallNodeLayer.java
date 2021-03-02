package projects.cbrenet.nodes.nodeImplementations;

import sinalgo.nodes.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This layer store the status of other nodes and the large id concern to this node */

public abstract class LargeSmallNodeLayer extends Node {
    public boolean largeFlag; // 唯一用于指定结点是大还是小的变量

    // large:



    // small:
    // the small node belong to which large node 's tree
    private HashSet<Integer> largeIds;


    private HashMap<Integer, Boolean> globalSmallNodesMap;
    // record the status of a node, true means the node is small.

    @Override
    public void init() {
        this.largeIds = new HashSet<>();
        this.globalSmallNodesMap = new HashMap<>();
        this.largeFlag = false;
    }

    public void changeNodeStatus(int id, boolean smallStatus){
        this.globalSmallNodesMap.replace(id, smallStatus);
//        if(smallStatus){
//            if(this.largeIds.contains(id)){
//                this.largeIds.remove
//            }
//        }
    }

    public boolean isNodeSmall(int id){
        return this.globalSmallNodesMap.get(id);
    }

    public HashSet<Integer> getLargeIds() {
        if(this.largeFlag){
            return largeIds;
        }
        else{
            return null;
        }
    }

    public void addLargeIds(Set<Integer> ids){
        for(int id : ids){
            this.largeIds.add(id);
        }
    }

    public void addLargeIds(int id){
        this.largeIds.add(id);
    }

    public void clearLargeIds(){
        this.largeIds.clear();
    }
}

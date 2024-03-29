package projects.cbrenet.nodes.messages.SDNMessage;

import sinalgo.nodes.messages.Message;

import java.util.ArrayList;

/**
 *  Only send by the SDN node, used to tell the dst node to insert or remove
 *  link to the target nodes;
 *
 * */

public class LinkMessage extends StatusRelatedMessage {

    private int largeId;
    final private int dst;

    final private boolean insertFlag;
    /*
    * T : insert link
    * F : remove link
    * */

    private boolean parentIsLnFlag = false;

    public void setParentIsLnFlag() {
        this.parentIsLnFlag = true;
    }

    public boolean isParentIsLnFlag() {
        return parentIsLnFlag;
    }


    final private ArrayList<Integer> targets;
    final private ArrayList<Character> relationships; // use with targets to indicate the relationship between node
    // and target
    // p parent
    // l leftChild
    // r rightChild
    // t root of the ego tree, it has special link with the large node


    public LinkMessage(int dst, boolean insertFlag, ArrayList<Integer> targets, int uniqueStatusId){
        /**
         *@description
         *@parameters  [dst, insertFlag, targets, uniqueStatusId]
         *@return
         *@author  Zhang Hongxuan
         *@create time  2021/2/28
         */
        super(uniqueStatusId);
        this.dst = dst;
        this.insertFlag = insertFlag;
        this.targets = targets;

        this.largeId = -1;
        this.relationships = null;
    }

    public LinkMessage(int dst, boolean insertFlag, int largeId, ArrayList<Integer> targets, ArrayList<Character> relationships, int uniqueStatusId){
        /**
         *@description This message is used to create ego-tree
         *@parameters  [dst, insertFlag, largeId, targets, relationships,uniqueStatusId]
         *@return
         *@author  Zhang Hongxuan
         *@create time  2021/2/22
         */
        super(uniqueStatusId);
        this.dst = dst;
        this.insertFlag = insertFlag;
        this.targets = targets;


        this.largeId = largeId;
        this.relationships = relationships;
    }

    public void setLargeId(int largeId) {
        this.largeId = largeId;
    }

    public int getLargeId() {
        return largeId;
    }

    public int getDst() {
        return dst;
    }

    public boolean isInsertFlag() {
        return insertFlag;
    }

    public ArrayList<Integer> getTargets() {
        return targets;
    }

    public ArrayList<Character> getRelationships() {
        return relationships;
    }

    public int getUniqueStatusId() {
        return uniqueStatusId;
    }

    @Override
    public Message clone() {
        return this;
    }

    @Override
    public String toString(){
        if(relationships != null){
            return "The largeId is " + this.largeId + " " +
                    "" + this.relationships.toString() + " " +
                    "" + ", link to" + this.targets.toString();
        }
        else{
            return "The largeId is " + this.largeId + " Link to " + this.targets.toString();
        }
    }
}

package projects.cbrenet.nodes.nodeImplementations;



/*
* In this layer, I will rebuild the data structure of the CBBST in the ego-tree. */

import projects.cbrenet.nodes.messages.RoutingMessage;

import java.util.HashMap;

public abstract class CounterBasedBSTStructureLayer extends CommunicatePartnerLayer{


    private class SendEntry{
        int egoTreeIdOfParent;
        int trueIdOfParent;  // Used for Forward Message 这个true Id 确实用得着嘛。。

        int egoTreeIdOfLeftChild;
        int trueIdOfLeftChild;

        int egoTreeIdOfRightChild;
        int trueIdOfRightChild;

        public SendEntry(int egoTreeIdOfParent, int egoTreeIdOfLeftChild, int egoTreeIdOfRightChild){
            this.egoTreeIdOfParent = egoTreeIdOfParent;
            this.egoTreeIdOfLeftChild = egoTreeIdOfLeftChild;
            this.egoTreeIdOfRightChild = egoTreeIdOfRightChild;

            this.trueIdOfParent = -10;
            this.trueIdOfLeftChild = -8;
            this.trueIdOfRightChild = -7;

        }

        boolean sendFlagOfParent = true;
        boolean sendFlagOfLeftChild = true;
        boolean sendFlagOfRightChild = true;

        public boolean getSendFlag(int id){
            if(id == this.egoTreeIdOfParent || id == this.trueIdOfParent){
                return this.sendFlagOfParent;
            }
            else if(id == this.egoTreeIdOfLeftChild || id == this.trueIdOfLeftChild){
                return this.sendFlagOfLeftChild;
            }
            else if(id == this.egoTreeIdOfRightChild || id ==this.trueIdOfRightChild){
                return this.sendFlagOfRightChild;
            }
            else{
                return false;
            }
        }


        public char getRelationShipTo(int id){
            if(id == this.egoTreeIdOfParent || id == this.trueIdOfParent){
                return 'p';
            }
            else if(id == this.egoTreeIdOfLeftChild || id == this.trueIdOfLeftChild){
                return 'l';
            }
            else if(id == this.egoTreeIdOfRightChild || id ==this.trueIdOfRightChild){
                return 'r';
            }
            else{
                return 'w'; // means wrong
            }
        }


        // Getter & Setter
        public void setTrueIdOfParent(int trueIdOfParent) {
            this.trueIdOfParent = trueIdOfParent;
        }

        public void setTrueIdOfLeftChild(int trueIdOfLeftChild) {
            this.trueIdOfLeftChild = trueIdOfLeftChild;
        }

        public void setTrueIdOfRightChild(int trueIdOfRightChild) {
            this.trueIdOfRightChild = trueIdOfRightChild;
        }

        public void setEgoTreeIdOfParent(int egoTreeIdOfParent) {
            this.egoTreeIdOfParent = egoTreeIdOfParent;
        }

        public void setEgoTreeIdOfLeftChild(int egoTreeIdOfLeftChild) {
            this.egoTreeIdOfLeftChild = egoTreeIdOfLeftChild;
        }

        public void setEgoTreeIdOfRightChild(int egoTreeIdOfRightChild) {
            this.egoTreeIdOfRightChild = egoTreeIdOfRightChild;
        }

        public void setSendFlagOfParent(boolean sendFlagOfParent) {
            this.sendFlagOfParent = sendFlagOfParent;
        }

        public void setSendFlagOfLeftChild(boolean sendFlagOfLeftChild) {
            this.sendFlagOfLeftChild = sendFlagOfLeftChild;
        }

        public void setSendFlagOfRightChild(boolean sendFlagOfRightChild) {
            this.sendFlagOfRightChild = sendFlagOfRightChild;
        }

    };


    HashMap<Integer, SendEntry> routeTable;  // 指示着当前的结点在 Ego-Tree(largeId)中的情况

    private SendEntry getSendEntryOf(int largeId){
        return this.routeTable.getOrDefault(largeId,null);
    }

    public char getRelationShipTo(int largeId, int targetId){
        SendEntry entry = this.getSendEntryOf(largeId);
        if(entry == null){
            return 'w';
        }
        else{
            return entry.getRelationShipTo(targetId);
        }

    }



}

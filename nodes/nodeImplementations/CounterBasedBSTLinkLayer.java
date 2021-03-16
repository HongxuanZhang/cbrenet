package projects.cbrenet.nodes.nodeImplementations;


/*
* 这里只负责Ego-Tree中结点的相连和移除，包括处理在StructureLayer的部分，但是不处理类似旋转位，发送位的问题！！*/

import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

public abstract class CounterBasedBSTLinkLayer extends CounterBasedBSTStructureLayer{
     
    // change link p l r, 这里就要考虑Auxiliary Node的问题了
    // 调用这个函数的时候，基本上也就是Delete过程或者Rotation过程了

    // may mainly used in rotation
    public boolean changeLeftChildTo(int largeId, int egoTreeId){

        // 原来的思路是先remove再add
        // remove the previous connection
        if(this.removeLinkToLeftChild(largeId)){
            // update current left child and create edge
            return this.addLinkToLeftChild(largeId, egoTreeId);
        }
        return false;
    }
    public boolean changeRightChildTo(int largeId, int egoTreeId){
        // remove the previous connection
        if(this.removeLinkToRightChild(largeId)){
            // update current left child and create edge
            return this.addLinkToRightChild(largeId, egoTreeId);
        }
        return false;
    }
    public boolean changeParentTo(int largeId, int egoTreeId){
        // remove the previous connection
        if(this.removeLinkToParent(largeId)){
            // update current left child and create edge
            return this.addLinkToParent(largeId, egoTreeId);
        }
        return false;
    }



    private boolean changeSendIdInRouteTable(int largeId, char relation, int id){
        assert (relation =='p' || relation=='r' || relation == 'l') ;

        SendEntry entryTmp = this.getSendEntryOf(largeId);
        if(entryTmp == null){
            Tools.fatalError("In change Send ID of CBBSTLinkLayer, the entry got is null!!!");
            return false;
        }

        switch (relation){
            case 'p':
                entryTmp.setSendIdOfParent(id);
                break;
            case 'l':
                entryTmp.setSendIdOfLeftChild(id);
                break;
            case 'r':
                entryTmp.setSendIdOfRightChild(id);
                break;
            default:
                break;
        }

        return true;
    }

    private boolean changeLinkTo(int largeId, int egoTreeId, int trueId, char relation){
        assert (relation =='p' || relation=='r' || relation == 'l') ;

        if(trueId < 0){
            return this.changeLeftChildTo(largeId, egoTreeId);
        }
        else{
            boolean removeFlag = false;
            switch (relation){
                case 'p':
                    removeFlag = this.removeLinkToParent(largeId);
                    break;
                case 'l':
                    removeFlag = this.removeLinkToLeftChild(largeId);
                    break;
                case 'r':
                    removeFlag = this.removeLinkToRightChild(largeId);
                    break;
                default:
                    break;
            }


            if(removeFlag){
                // update current left child and create edge
                Node node = Tools.getNodeByID(trueId);
                if(node != null){
                    if(this.changeSendIdInRouteTable(largeId, relation, trueId)){
                        this.addConnectionTo(node);
                        return true;
                    }
                }
                else{
                    Tools.fatalError("Want to change link to a node not exist!");
                    return false;
                }
            }
            return false;
        }

    }

    public boolean changeLeftChildTo(int largeId, int egoTreeId, int trueId){
        return this.changeLinkTo(largeId, egoTreeId, trueId, 'l');
    }
    public boolean changeRightChildTo(int largeId, int egoTreeId, int trueId){
        return this.changeLinkTo(largeId, egoTreeId, trueId, 'r');
    }
    public boolean changeParentTo(int largeId, int egoTreeId, int trueId){
        return this.changeLinkTo(largeId, egoTreeId, trueId, 'p');
    }



    // add link p l r
    // These part's code only used to add link, and only in ego-tree.
    // DO NOT use it to create link to auxiliary node!

    private boolean addNeighborInRouteTable(int largeId, char relation, int id){
        assert (relation =='p' || relation=='r' || relation == 'l') ;

        SendEntry entryTmp = this.getSendEntryOf(largeId);
        if(entryTmp == null){
            this.addSendEntry(largeId, -1,-1,-1);
        }
        entryTmp = this.getSendEntryOf(largeId);

        assert entryTmp != null;

        switch (relation){
            case 'p':
                entryTmp.setEgoTreeIdOfParent(id);
                entryTmp.setSendIdOfParent(id);
                break;
            case 'l':
                entryTmp.setEgoTreeIdOfLeftChild(id);
                entryTmp.setSendIdOfLeftChild(id);
                break;
            case 'r':
                entryTmp.setEgoTreeIdOfRightChild(id);
                entryTmp.setSendIdOfRightChild(id);
                break;
            default:
                break;
        }

        return true;
    }

    // todo 视情况更改为private
    public boolean addParentInRouteTable(int largeId, int id){
        return this.addNeighborInRouteTable(largeId,'p', id);
    }

    public boolean addRightChildInRouteTable(int largeId, int id){
        return this.addNeighborInRouteTable(largeId,'r', id);
    }

    public boolean addLeftChildInRouteTable(int largeId, int id){
        return this.addNeighborInRouteTable(largeId, 'l',id);
    }

    private boolean addLinkTo(int nodeId){
        /**
         *@description  Add a link from this node to the id node, in the ego-tree(largeId). Do
         *              NOT use this to add link to a node not belong to Ego-Tree.
         *@parameters  [id]
         *@return  boolean
         *@author  Zhang Hongxuan
         *@create time  2021/3/16
         */
        if(nodeId < 0){
            return false;
        }
        Node node = Tools.getNodeByID(nodeId);
        if(node == null){
            Tools.fatalError("SDN want to create a link between " + this.ID + " and " + nodeId + " , but" +
                    " the corresponding node is NULL!!!!");
            return false;
        }
        this.addConnectionTo(node);
        return true;
    }

    public boolean addLinkToLeftChild(int largeId, int egoTreeId){
        if(this.addLeftChildInRouteTable(largeId, egoTreeId)){
            return this.addLinkTo(egoTreeId);
        }
        else{
            Tools.fatalError("SDN want to add a link to "+egoTreeId + " in the Ego-Tree(largeId), " +
                    "but failed!!!!");
            return false;
        }
    }

    public boolean addLinkToRightChild(int largeId, int egoTreeId){
        if(this.addRightChildInRouteTable(largeId, egoTreeId)){
            return this.addLinkTo(egoTreeId);
        }
        else{
            Tools.fatalError("SDN want to add a link to "+egoTreeId + " in the Ego-Tree(largeId), " +
                    "but failed!!!!");
            return false;
        }
    }

    public boolean addLinkToParent(int largeId, int egoTreeId){
        if(this.addParentInRouteTable(largeId, egoTreeId)){
            return this.addLinkTo(egoTreeId);
        }
        else{
            Tools.fatalError("SDN want to add a link to "+egoTreeId + " in the Ego-Tree(largeId), " +
                    "but failed!!!!");
            return false;
        }
    }


    // remove link p l r


    /* Todo 下面这两个函数，需要小心多次调用以及误删的问题！*/

    // remove : delete corresponding link and set corresponding entry into -1;

    public boolean removeLinkToParent(int largeId){
        int id = this.getSendEntryOf(largeId).getEgoTreeIdOfParent();
        if(!this.removeParentInRouteTable(largeId)){
            return false;
        }
        return this.removeLinkTo(id);
    }

    public boolean removeLinkToRightChild(int largeId){
        // NOTE that this check can not replaced by the link connected! 因为可能两个结点之间不可能只存在一个边
        int id = this.getSendEntryOf(largeId).getEgoTreeIdOfRightChild();
        // this code must put here, since removeRightChildInRouteTable would change it into -1

        if(!this.removeRightChildInRouteTable(largeId)){
            return false;
        }
        return this.removeLinkTo(id);
    }

    public boolean removeLinkToLeftChild(int largeId){
        int id = this.getSendEntryOf(largeId).getEgoTreeIdOfLeftChild();
        if(!this.removeLeftChildInRouteTable(largeId)){
            return false;
        }
        return this.removeLinkTo(id);
    }


    public boolean removeParentInRouteTable(int largeId){
        return this.removeNeighborInRouteTable(largeId,'p');
    }

    public boolean removeRightChildInRouteTable(int largeId){
        return this.removeNeighborInRouteTable(largeId,'r');
    }

    public boolean removeLeftChildInRouteTable(int largeId){
        return this.removeNeighborInRouteTable(largeId, 'l');
    }


    private boolean removeNeighborInRouteTable(int largeId, char relation){
        /**
         *@description both send id and ego-tree id would be set to -1;
         *@parameters  [largeId, relation]
         *@return  boolean
         *@author  Zhang Hongxuan
         *@create time  2021/3/16
         */
        assert (relation =='p' || relation=='r' || relation == 'l') ;

        SendEntry entryTmp = this.getSendEntryOf(largeId);
        if(entryTmp != null){

            int egoTreeId = -1;
            switch (relation){
                case 'p':
                    egoTreeId = entryTmp.getEgoTreeIdOfParent();
                    break;
                case 'l':
                    egoTreeId = entryTmp.getEgoTreeIdOfLeftChild();
                    break;
                case 'r':
                    egoTreeId = entryTmp.getEgoTreeIdOfRightChild();
                    break;
                default:
                    break;
            }


            if(egoTreeId < 0){
                Tools.warning("Want to remove neighbor, but seems removed already");
                // indicate that the child has been removed somehow
                return false;
            }


            switch (relation){
                case 'p':
                    entryTmp.setEgoTreeIdOfParent(-1);
                    entryTmp.setSendIdOfParent(-1);
                    break;
                case 'l':
                    entryTmp.setEgoTreeIdOfLeftChild(-1);
                    entryTmp.setSendIdOfLeftChild(-1);
                    break;
                case 'r':
                    entryTmp.setEgoTreeIdOfRightChild(-1);
                    entryTmp.setSendIdOfRightChild(-1);
                    break;
                default:
                    break;
            }
            return true;
        }
        else{
            Tools.warning("Want to remove neighbor, but can not get the corresponding entry, " +
                    "in CBBSTLink Layer.");
            return false;
        }
    }

    private boolean removeLinkTo(int nodeId) {
        /**
         *@description This method remove only one side of edge
         *@parameters  [nodeId]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/20
         */
        // make sure only the parent remove the link
        if(nodeId < 0){
            return false;
        }
        CounterBasedBSTLayer node = (CounterBasedBSTLayer) Tools.getNodeByID(nodeId);
        if(node == null){
            return false;
        }
        this.outgoingConnections.remove(this, node);
        return true;
    }


}

package projects.cbrenet.nodes.nodeImplementations.nodeHelper;

import projects.cbrenet.nodes.nodeImplementations.CounterBasedBSTLayer;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

public class LinkHelper {

    boolean realLink = false;


    // change link p l r, 这里就要考虑Auxiliary Node的问题了
    // 调用这个函数的时候，基本上也就是Delete过程或者Rotation过程了

    // may mainly used in rotation

    // delete 的时候如果用到AN，就调用这个method
    public boolean changeLeftChildTo(int largeId, int egoTreeId, int sendId, int helpedId, EntryGetter entryGetter, Node helpedNode){
        boolean result = this.changeLinkTo(largeId, egoTreeId, sendId, 'l',
                helpedId, entryGetter, helpedNode);
        if(result == true){
            System.out.println("Node " + helpedNode.ID + " change " + helpedId + "'s left child in egoTree(" + largeId + ") to" +
                    " EgoTreeId: " + egoTreeId + " and SendId:" + sendId);
        }
        else{
            System.out.println("Node " + helpedNode.ID + " want to  change " + helpedId + "'s left child in egoTree(" + largeId + ")to" +
                    " EgoTreeId: " + egoTreeId + " and SendId:" + sendId + " but failed!!!");
        }
        return result;

    }
    public boolean changeRightChildTo(int largeId, int egoTreeId, int sendId, int helpedId, EntryGetter entryGetter, Node helpedNode){

        boolean result = this.changeLinkTo(largeId, egoTreeId, sendId, 'r',
                helpedId, entryGetter, helpedNode);
        if(result == true){
            System.out.println("Node " + helpedNode.ID + " change " + helpedId + "'s right child in egoTree(" + largeId + ") to" +
                    " EgoTreeId: " + egoTreeId + " and SendId:" + sendId);
        }
        else{
            System.out.println("Node " + helpedNode.ID + " want to  change " + helpedId + "'s right child in egoTree(" + largeId + ") to" +
                    " EgoTreeId: " + egoTreeId + " and SendId:" + sendId + " but failed!!!");
        }
        return result;

    }
    public boolean changeParentTo(int largeId, int egoTreeId, int sendId, int helpedId, EntryGetter entryGetter, Node helpedNode){
        boolean result = this.changeLinkTo(largeId, egoTreeId, sendId, 'p',
                helpedId, entryGetter, helpedNode);
        if(result == true){
            System.out.println("Node " + helpedNode.ID + " change " + helpedId + "'s parent in egoTree(" + largeId + ") to" +
                    " EgoTreeId: " + egoTreeId + " and SendId:" + sendId);
        }
        else{
            System.out.println("Node " + helpedNode.ID + " want to  change " + helpedId + "'s parent in egoTree(" + largeId + ") to" +
                    " EgoTreeId: " + egoTreeId + " and SendId:" + sendId + " but failed!!!");
        }
        return result;
    }


    // todo 这三个可能没有必要继续保留
    private boolean changeLeftChildTo(int largeId, int egoTreeId, int helpedId, EntryGetter entryGetter, Node node){

        // 原来的思路是先remove再add
        // remove the previous connection
        if(this.removeLinkToLeftChild(largeId, helpedId, entryGetter, node)){
            // update current left child and create edge
            return this.addLinkToLeftChild(largeId, egoTreeId, egoTreeId, helpedId, entryGetter, node);
        }
        return false;
    }
    private boolean changeRightChildTo(int largeId, int egoTreeId, int helpedId, EntryGetter entryGetter, Node node){
        // remove the previous connection
        if(this.removeLinkToRightChild(largeId, helpedId, entryGetter, node)){
            // update current left child and create edge
            return this.addLinkToRightChild(largeId, egoTreeId, egoTreeId,helpedId, entryGetter, node);
        }
        return false;
    }
    private boolean changeParentTo(int largeId, int egoTreeId, int helpedId, EntryGetter entryGetter, Node node){
        // remove the previous connection
        //int largeId,int helpedId, EntryGetter entryGetter, Node node
        if(this.removeLinkToParent(largeId, helpedId, entryGetter, node)){
            // update current left child and create edge
            return this.addLinkToParent(largeId, egoTreeId, egoTreeId, helpedId, entryGetter, node);
        }
        return false;
    }



    private boolean changeSendIdInRouteTable(int largeId, char relation, int sendId, int helpedId, EntryGetter entryGetter){
        /**
         *@description  This method is used to set send id in the Send Entry
         *@parameters  [largeId, relation, sendId, helpedId, entryGetter]
         *@return  boolean
         *@author  Zhang Hongxuan
         *@create time  2021/3/18
         */
        assert (relation =='p' || relation=='r' || relation == 'l') ;

        SendEntry entryTmp = entryGetter.getCorrespondingEntry(helpedId, largeId);
        if(entryTmp == null){
            Tools.fatalError("In change Send ID of CBBSTLinkLayer, the entry got is null!!!");
            return false;
        }
        switch (relation){
            case 'p':
                entryTmp.setSendIdOfParent(sendId);
                break;
            case 'l':
                entryTmp.setSendIdOfLeftChild(sendId);
                break;
            case 'r':
                entryTmp.setSendIdOfRightChild(sendId);
                break;
            default:
                break;
        }
        return true;
    }


    private boolean changeLinkTo(int largeId, int egoTreeId, int trueId, char relation,
                                 int helpedId, EntryGetter entryGetter, Node helpedNode){
        assert (relation =='p' || relation=='r' || relation == 'l') ;

        if(trueId < 0){
            switch (relation){
                case 'p':
                    return this.changeParentTo(largeId, egoTreeId, helpedId, entryGetter, helpedNode);
                case 'l':
                    return this.changeLeftChildTo(largeId, egoTreeId, helpedId, entryGetter, helpedNode);
                case 'r':
                    return this.changeRightChildTo(largeId, egoTreeId, helpedId, entryGetter, helpedNode);
                default:
                    break;
            }
            return false;
        }
        else{
            boolean removeFlag = false;
            switch (relation){
                case 'p':
                    removeFlag = this.removeLinkToParent(largeId, helpedId, entryGetter, helpedNode);
                    break;
                case 'l':
                    removeFlag = this.removeLinkToLeftChild(largeId, helpedId, entryGetter, helpedNode);
                    break;
                case 'r':
                    removeFlag = this.removeLinkToRightChild(largeId, helpedId, entryGetter, helpedNode);
                    break;
                default:
                    break;
            }

            if(removeFlag){
                // update current left child and create edge
                Node node = Tools.getNodeByID(trueId);
                if(node != null){
                    // 先change了sendId 然后又change了egoTreeID
                    if(this.changeSendIdInRouteTable(largeId, relation, trueId, helpedId, entryGetter)){
                        if(realLink){
                            helpedNode.addConnectionTo(node);
                        }

                        SendEntry sendEntry = entryGetter.getCorrespondingEntry(helpedId, largeId);

                        switch (relation){
                            case 'p':
                                sendEntry.setEgoTreeIdOfParent(egoTreeId);
                                break;
                            case 'l':
                                sendEntry.setEgoTreeIdOfLeftChild(egoTreeId);
                                break;
                            case 'r':
                                sendEntry.setEgoTreeIdOfRightChild(egoTreeId);
                                break;
                            default:
                                break;
                        }
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



    // add link p l r
    // These part's code only used to add link, and only in ego-tree.
    // DO NOT use it to create link to auxiliary node!

    public boolean addLinkToLeftChild(int largeId, int egoTreeId, int sendId, int helpedId, EntryGetter entryGetter, Node node){
        if(this.addLeftChildInRouteTable(largeId, egoTreeId, sendId ,helpedId, entryGetter)){
            System.out.println("Node " + node.ID + " add " + helpedId + "'s left child in egoTree(" + largeId + ") to" +
                    " EgoTreeId: " + egoTreeId + " and SendId:" + sendId);
            return this.addLinkTo(egoTreeId, node);
        }
        else{
            Tools.fatalError("SDN want to add a link to "+egoTreeId + " in the Ego-Tree(largeId), " +
                    "but failed!!!!");
            return false;
        }
    }

    public boolean addLinkToRightChild(int largeId, int egoTreeId, int sendId, int helpedId, EntryGetter entryGetter, Node node){
        if(this.addRightChildInRouteTable(largeId, egoTreeId, sendId, helpedId, entryGetter)){
            System.out.println("Node " + node.ID + " add " + helpedId + "'s right child in egoTree(" + largeId + ") to" +
                    " EgoTreeId: " + egoTreeId + " and SendId:" + sendId);
            return this.addLinkTo(egoTreeId, node);
        }
        else{
            Tools.fatalError("SDN want to add a link to "+egoTreeId + " in the Ego-Tree(largeId), " +
                    "but failed!!!!");
            return false;
        }
    }

    public boolean addLinkToParent(int largeId, int egoTreeId, int sendId ,int helpedId, EntryGetter entryGetter, Node node){
        if(this.addParentInRouteTable(largeId, egoTreeId, sendId, helpedId, entryGetter)){
            System.out.println("Node " + node.ID + " add " + helpedId + "'s parent in egoTree(" + largeId + ") to" +
                    " EgoTreeId: " + egoTreeId + " and SendId:" + sendId);
            return this.addLinkTo(egoTreeId, node);
        }
        else{
            Tools.fatalError("SDN want to add a link to "+egoTreeId + " in the Ego-Tree(largeId), " +
                    "but failed!!!!");
            return false;
        }
    }


    private boolean addParentInRouteTable(int largeId, int targetId, int sendId, int helpedId , EntryGetter entryGetter){
        return this.addNeighborInRouteTable(largeId,'p', targetId, sendId, helpedId, entryGetter);
    }

    private boolean addRightChildInRouteTable(int largeId, int targetId, int sendId, int helpedId , EntryGetter entryGetter){
        return this.addNeighborInRouteTable(largeId,'r', targetId, sendId, helpedId, entryGetter);
    }

    private boolean addLeftChildInRouteTable(int largeId, int targetId, int sendId , int helpedId , EntryGetter entryGetter){
        return this.addNeighborInRouteTable(largeId, 'l',targetId, sendId, helpedId, entryGetter);
    }

    private boolean addNeighborInRouteTable(int largeId, char relation, int targetId, int sendId, int helpedId , EntryGetter entryGetter){
        assert (relation =='p' || relation=='r' || relation == 'l') ;

        SendEntry entryTmp = entryGetter.getCorrespondingEntry(helpedId, largeId);
        if(entryTmp == null){
            entryGetter.addSendEntry(helpedId, largeId, -1,-1,-1);
        }
        entryTmp = entryGetter.getCorrespondingEntry(helpedId,largeId);

        assert entryTmp != null;

        switch (relation){
            case 'p':
                entryTmp.setEgoTreeIdOfParent(targetId);
                entryTmp.setSendIdOfParent(sendId);
                break;
            case 'l':
                entryTmp.setEgoTreeIdOfLeftChild(targetId);
                entryTmp.setSendIdOfLeftChild(sendId);
                break;
            case 'r':
                entryTmp.setEgoTreeIdOfRightChild(targetId);
                entryTmp.setSendIdOfRightChild(sendId);
                break;
            default:
                break;
        }

        return true;
    }

    private boolean addLinkTo(int nodeId, Node helpedNode){
        /**
         *@description  Add a link from this node to the id node, in the ego-tree(largeId). Do
         *              NOT use this to add link to a node not belong to Ego-Tree.
         *@parameters  [id]
         *@return  boolean
         *@author  Zhang Hongxuan
         *@create time  2021/3/16
         */
        if(!realLink){
            return true;
        }

        if(nodeId < 0){
            return false;
        }
        Node node = Tools.getNodeByID(nodeId);
        if(node == null){
            Tools.fatalError("SDN want to create a link between " + helpedNode.ID + " and " + nodeId + " , but" +
                    " the corresponding node is NULL!!!!");
            return false;
        }
        helpedNode.addConnectionTo(node);
        return true;
    }



    // remove link p l r

    /*    下面这两个函数，需要小心多次调用以及误删的问题！*/

    // remove : delete corresponding link and set corresponding entry into -1;

    public boolean removeLinkToParent(int largeId,int helpedId, EntryGetter entryGetter, Node node){
        // NOTE that this check can not replaced by the link connected! 因为可能两个结点之间不可能只存在一个边
        int id = entryGetter.getCorrespondingEntry(helpedId, largeId).getEgoTreeIdOfParent();
        // this code must put here, since removeRightChildInRouteTable would change it into -1

        if(!this.removeParentInRouteTable(largeId, helpedId, entryGetter)){
            return false;
        }
        System.out.println("Node " + node.ID + " remove " + helpedId + "'s parent" +
                " in egoTree(" + largeId + ")" );

        return this.removeLinkTo(id, node);
    }

    public boolean removeLinkToRightChild(int largeId, int helpedId, EntryGetter entryGetter, Node node){
        int id = entryGetter.getCorrespondingEntry(helpedId, largeId).getEgoTreeIdOfRightChild();

        if(!this.removeRightChildInRouteTable(largeId, helpedId, entryGetter)){
            return false;
        }
        System.out.println("Node " + node.ID + " remove " + helpedId + "'s right child" +
                " in egoTree(" + largeId + ")" );
        return this.removeLinkTo(id, node);
    }

    public boolean removeLinkToLeftChild(int largeId, int helpedId, EntryGetter entryGetter, Node node){
        int id = entryGetter.getCorrespondingEntry(helpedId, largeId).getEgoTreeIdOfLeftChild();
        if(!this.removeLeftChildInRouteTable(largeId, helpedId, entryGetter)){
            return false;
        }
        System.out.println("Node " + node.ID + " remove " + helpedId + "'s left child" +
                " in egoTree(" + largeId + ")" );
        return this.removeLinkTo(id,node);
    }


    private boolean removeParentInRouteTable(int largeId, int helpedId, EntryGetter entryGetter){
        return this.removeNeighborInRouteTable(largeId,'p', helpedId, entryGetter);
    }

    private boolean removeRightChildInRouteTable(int largeId,int helpedId, EntryGetter entryGetter){
        return this.removeNeighborInRouteTable(largeId,'r', helpedId, entryGetter);
    }

    private boolean removeLeftChildInRouteTable(int largeId, int helpedId, EntryGetter entryGetter){
        return this.removeNeighborInRouteTable(largeId, 'l', helpedId, entryGetter);
    }

    private boolean removeNeighborInRouteTable(int largeId, char relation, int helpedId, EntryGetter entryGetter){
        /**
         *@description both send id and ego-tree id would be set to -1;
         *@parameters  [largeId, relation]
         *@return  boolean
         *@author  Zhang Hongxuan
         *@create time  2021/3/16
         */
        assert (relation =='p' || relation=='r' || relation == 'l') ;

        SendEntry entryTmp = entryGetter.getCorrespondingEntry(helpedId, largeId);
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

    private boolean removeLinkTo(int nodeId, Node helpedNode) {
        /**
         *@description This method remove only one side of edge
         *@parameters  [nodeId]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/20
         */
        // make sure only the parent remove the link
        if(!realLink)
        {
            return true;
        }

        if(nodeId < 0){
            return false;
        }
        CounterBasedBSTLayer node = (CounterBasedBSTLayer) Tools.getNodeByID(nodeId);
        if(node == null){
            return false;
        }
        helpedNode.outgoingConnections.remove(helpedNode, node);
        return true;
    }


}

package projects.cbrenet.nodes.nodeImplementations.nodeHelper;

import projects.cbrenet.nodes.messages.controlMessage.AcceptClusterMessage;
import projects.cbrenet.nodes.messages.controlMessage.NodeInfo;
import projects.cbrenet.nodes.messages.controlMessage.RequestClusterMessage;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.tools.Tools;

public class RotationHelper {

    private double log2(long value) {
        if(value == 0){
            return 0;
        }
        return Math.log(value) / Math.log(2);
    }


    public double diffPotential(RequestClusterMessage requestClusterMessage){

        NodeInfo info0 = requestClusterMessage.getNodeInfoOf(0);
        NodeInfo info1 = requestClusterMessage.getNodeInfoOf(1);
        NodeInfo info2 = requestClusterMessage.getNodeInfoOf(2);


        double result = 0;

        if(info2 != null){
            char relation1 = requestClusterMessage.getRelationFromNode0ToNode1();
            char relation2 = requestClusterMessage.getRelationFromNode1ToNode2();

            // relation2 可能是空的



            if(relation1 == 'l' && relation2 == 'l'){
                // weight , integer is enough
                int wYAfter = info1.getCounterOfCurNode() + info0.getWeightOfCurNode()
                        + info2.getCounterOfCurNode() + info1.getWeightOfRightChild() + info2.getWeightOfRightChild();

                int wZBefore = info2.getWeightOfCurNode();

                result = this.log2(wYAfter) - this.log2(wZBefore);

            }
            else if( relation1 == 'r' && relation2 == 'r' ){
                int wYAfter = info1.getCounterOfCurNode() + info0.getWeightOfCurNode()
                        + info2.getCounterOfCurNode() + info1.getWeightOfLeftChild() + info2.getWeightOfLeftChild();

                int wZBefore = info2.getWeightOfCurNode();

                result = this.log2(wYAfter) - this.log2(wZBefore);

            }
            else if( relation1 == 'l' && relation2 == 'r'){
                int wYAfter = info1.getCounterOfCurNode() + info0.getWeightOfRightChild() +
                        info1.getWeightOfRightChild();

                int wZAfter = info2.getCounterOfCurNode() + info2.getWeightOfLeftChild() +
                        info0.getWeightOfLeftChild();

                int wYBefore = info1.getWeightOfCurNode();

                int wXBefore = info0.getWeightOfCurNode();


                result = this.log2(wYAfter) + this.log2(wZAfter) - this.log2(wYBefore) - this.log2(wXBefore);
            }
            else if( relation1 == 'r' && relation2 == 'l'){
                int wYAfter = info1.getCounterOfCurNode() + info0.getWeightOfLeftChild() +
                        info1.getWeightOfLeftChild();

                int wZAfter = info2.getCounterOfCurNode() + info2.getWeightOfRightChild() +
                        info0.getWeightOfRightChild();

                int wYBefore = info1.getWeightOfCurNode();

                int wXBefore = info0.getWeightOfCurNode();

                result = this.log2(wYAfter) + this.log2(wZAfter) - this.log2(wYBefore) - this.log2(wXBefore);
            }
            System.out.println(result);
            return result;


        }
        else{
            char relation1 = requestClusterMessage.getRelationFromNode0ToNode1();


            if(relation1 == 'l'){
                int wYAfter = info1.getCounterOfCurNode() + info0.getWeightOfRightChild() + info1.getWeightOfRightChild() ;
                int wXBefore = info0.getWeightOfCurNode();
                result = this.log2(wYAfter) - this.log2(wXBefore);
            }
            else{
                int wYAfter = info1.getCounterOfCurNode() + info0.getWeightOfLeftChild() + info1.getWeightOfLeftChild();
                int wXBefore = info0.getWeightOfCurNode();
                result = this.log2(wYAfter) - this.log2(wXBefore);
            }

            return result;
        }


    }

    public void rotation(AcceptClusterMessage acceptClusterMessage, int largeId){

        RequestClusterMessage requestClusterMessage =
                acceptClusterMessage.getRequestClusterMessage();



        int position = requestClusterMessage.getPosition();

        if(position == 3 || position == 2){
            //这两实际上是一种，只不过2的时候没有传递到LN去罢了
            char relation1 = requestClusterMessage.getRelationFromNode0ToNode1();
            char relation2 = requestClusterMessage.getRelationFromNode1ToNode2();


            if( relation1 == 'l' && relation2 == 'l' ){

            }
            else if(relation1 == 'r' && relation2 == 'r'){

            }
            else if(relation1 == 'l' && relation2 == 'r'){

            }
            else if(relation1 == 'r' && relation2 == 'l'){

            }
            else{

            }



        }
        else if(position == 1){
            char relation = requestClusterMessage.getRelationFromNode0ToNode1();

            boolean lNFlag = requestClusterMessage.isLnFlag();

            if(!lNFlag){
                Tools.fatalError("lNFlag should be True! Or should be bug");
            }

            NodeInfo info0 = requestClusterMessage.getNodeInfoOf(0);
            NodeInfo info1 = requestClusterMessage.getNodeInfoOf(1);

            int xSendId = info0.getCurNodeTrueId();
            int xEgoTreeId = info0.getCurNodeEgoId();

            int ySendId = info1.getCurNodeTrueId();
            int yEgoTreeId = info1.getCurNodeEgoId();



            int pEgoTreeId = requestClusterMessage.getTheMostUpperEgoTreeId();
            int pSendId = requestClusterMessage.getTheMostUpperSendId();
            // X
            EntryGetter entryGetterForX = (EntryGetter)Tools.getNodeByID(xSendId);
            assert entryGetterForX != null;
            SendEntry entryForX = entryGetterForX.getCorrespondingEntry(xEgoTreeId, largeId);
            // Y
            EntryGetter entryGetterForY = (EntryGetter)Tools.getNodeByID(ySendId);
            assert entryGetterForY != null;
            SendEntry entryForY = entryGetterForY.getCorrespondingEntry(yEgoTreeId, largeId);

            if(relation == 'l'){
                /*
                          /                 /
                         y                *x
                        / \               / \
                      *x   C     -->     A   y
                      / \                   / \
                     A   B                 B   C
              */

                // set x parent as p
                entryForX.setEgoTreeIdOfParent(pEgoTreeId);
                entryForX.setSendIdOfParent(pSendId);
                // set x right child as y
                entryForX.setEgoTreeIdOfRightChild(yEgoTreeId);
                entryForX.setSendIdOfRightChild(ySendId);


                // set y parent as x
                entryForY.setSendIdOfParent(xSendId);
                entryForY.setEgoTreeIdOfParent(xEgoTreeId);

                // set y left child as b
                int bEgoTreeId = info0.getEgoTreeIdOfRightChild();
                int bSendId = info0.getSendIdOfRightChild();

                entryForY.setSendIdOfLeftChild(bSendId);
                entryForY.setEgoTreeIdOfLeftChild(bEgoTreeId);

                // update weight:
                entryForY.setWeightOfLeft(info0.getWeightOfRightChild());
                entryForX.setWeightOfRight(entryForY.getWeight());
            }
            else{
                // r
                /*
                          /                 /
                         y                *x
                        / \               / \
                       C  x     -->      y   B
                         / \            / \
                        A   B          C   A
              */

                // set x parent as p
                entryForX.setEgoTreeIdOfParent(pEgoTreeId);
                entryForX.setSendIdOfParent(pSendId);
                // set x right child as y
                entryForX.setEgoTreeIdOfLeftChild(yEgoTreeId);
                entryForX.setSendIdOfLeftChild(ySendId);


                // set y parent as x
                entryForY.setSendIdOfParent(xSendId);
                entryForY.setEgoTreeIdOfParent(xEgoTreeId);

                // set y left child as b
                int aEgoTreeId = info0.getEgoTreeIdOfLeftChild();
                int aSendId = info0.getSendIdOfLeftChild();

                entryForY.setSendIdOfRightChild(aSendId);
                entryForY.setEgoTreeIdOfRightChild(aEgoTreeId);

                // update weight:
                entryForY.setWeightOfRight(info0.getWeightOfLeftChild());
                entryForX.setWeightOfLeft(entryForY.getWeight());
            }
        }



    }


    public void receiveAdjustMessage(){




    }

}

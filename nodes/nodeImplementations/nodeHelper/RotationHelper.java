package projects.cbrenet.nodes.nodeImplementations.nodeHelper;

import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.controlMessage.AcceptClusterMessage;
import projects.cbrenet.nodes.messages.controlMessage.NodeInfo;
import projects.cbrenet.nodes.messages.controlMessage.RequestClusterMessage;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.tools.Tools;

import java.util.LinkedList;
import java.util.Queue;

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

    public void rotation(AcceptClusterMessage acceptClusterMessage){

        RequestClusterMessage requestClusterMessage =
                acceptClusterMessage.getRequestClusterMessage();

        int largeId = acceptClusterMessage.getLargeId();


        int position = requestClusterMessage.getPosition();

        if(position == 3 || position == 2){
            //这两实际上是一种，只不过2的时候没有传递到LN去罢了
            char relation1 = requestClusterMessage.getRelationFromNode0ToNode1();
            char relation2 = requestClusterMessage.getRelationFromNode1ToNode2();

            NodeInfo info0 = requestClusterMessage.getNodeInfoOf(0);
            NodeInfo info1 = requestClusterMessage.getNodeInfoOf(1);
            NodeInfo info2 = requestClusterMessage.getNodeInfoOf(2);

            int pEgoTreeId = requestClusterMessage.getTheMostUpperEgoTreeId();
            int pSendId = requestClusterMessage.getTheMostUpperSendId();
            boolean lNFlag = requestClusterMessage.isLnFlag();

            assert position == 3 || lNFlag;

            int xSendId = info0.getCurNodeTrueId();
            int xEgoTreeId = info0.getCurNodeEgoId();

            int ySendId = info1.getCurNodeTrueId();
            int yEgoTreeId = info1.getCurNodeEgoId();

            int zSendId = info2.getCurNodeTrueId();
            int zEgoTreeId = info2.getCurNodeEgoId();

            // X
            EntryGetter entryGetterForX = (EntryGetter)Tools.getNodeByID(xSendId);
            assert entryGetterForX != null;
            SendEntry entryForX = entryGetterForX.getCorrespondingEntry(xEgoTreeId, largeId);
            // Y
            EntryGetter entryGetterForY = (EntryGetter)Tools.getNodeByID(ySendId);
            assert entryGetterForY != null;
            SendEntry entryForY = entryGetterForY.getCorrespondingEntry(yEgoTreeId, largeId);
            // Z
            EntryGetter entryGetterForZ = (EntryGetter)Tools.getNodeByID(zSendId);
            assert entryGetterForZ != null;
            SendEntry entryForZ = entryGetterForZ.getCorrespondingEntry(zEgoTreeId, largeId);




            if( relation1 == 'l' && relation2 == 'l' ){
                /*
                            /                   /
                    2      z                   y
                          / \                /   \
                   1     y   D              x     z
                        / \                / \   / \
                  0    x   C     -->      A  B  C  D
                      / \
                     A   B
              */
                int cSendId = info1.getSendIdOfRightChild();
                int cEgoTreeId = info1.getEgoTreeIdOfRightChild();
                int cWeight = info1.getWeightOfRightChild();

                // y set right child to z
                entryForY.setEgoTreeIdOfRightChild(zEgoTreeId);
                entryForY.setSendIdOfRightChild(zSendId);
                // y set parent to p
                entryForY.setEgoTreeIdOfParent(pEgoTreeId);
                entryForY.setSendIdOfParent(pSendId);

                // z set left child to C
                entryForZ.setEgoTreeIdOfLeftChild(cEgoTreeId);
                entryForZ.setSendIdOfLeftChild(cSendId);
                // z set parent to y
                entryForZ.setEgoTreeIdOfParent(yEgoTreeId);
                entryForZ.setSendIdOfParent(ySendId);

                // update weight of z and y
                entryForZ.setWeightOfLeft(cWeight);
                entryForY.setWeightOfRight(entryForZ.getWeight());


                // set C parent to z
                if(cEgoTreeId>0){
                    SendEntry entryForC = ((EntryGetter)Tools.getNodeByID(cSendId))
                            .getCorrespondingEntry(cEgoTreeId, largeId);
                    if(entryForC == null){
                        Tools.warning("Check what happen in  Rotation Helper whey c have ego tree id but no entry");
                    }
                    else{
                        entryForC.setEgoTreeIdOfParent(zEgoTreeId);
                        entryForC.setSendIdOfParent(zSendId);
                    }
                }


            }
            else if(relation1 == 'r' && relation2 == 'r'){
                /*

                    2      z                   y
                          / \                /   \
                   1     A   y              z     x
                            / \            / \   / \
                  0        B   x    -->   A  B  C  D
                              / \
                             C   D
              */
                int bEgoTreeId = info1.getEgoTreeIdOfLeftChild();
                int bSendId = info1.getSendIdOfLeftChild();
                int bWeight = info1.getWeightOfLeftChild();


                // y set left child to z
                entryForY.setEgoTreeIdOfLeftChild(zEgoTreeId);
                entryForY.setSendIdOfLeftChild(zSendId);
                // y set parent to p
                entryForY.setEgoTreeIdOfParent(pEgoTreeId);
                entryForY.setSendIdOfParent(pSendId);

                // z set right child to B
                entryForZ.setEgoTreeIdOfRightChild(bEgoTreeId);
                entryForZ.setSendIdOfRightChild(bSendId);
                // z set parent to y
                entryForZ.setEgoTreeIdOfParent(yEgoTreeId);
                entryForZ.setSendIdOfParent(ySendId);

                // update weight of z and y
                entryForZ.setWeightOfRight(bWeight);
                entryForY.setWeightOfLeft(entryForZ.getWeight());


                // set B 's parent to z
                if(bEgoTreeId>0){
                    SendEntry entryForB = ((EntryGetter)Tools.getNodeByID(bSendId))
                            .getCorrespondingEntry(bEgoTreeId, largeId);
                    if(entryForB == null){
                        Tools.warning("Check what happen in  Rotation Helper whey c have ego tree id but no entry");
                    }
                    else{
                        entryForB.setEgoTreeIdOfParent(zEgoTreeId);
                        entryForB.setSendIdOfParent(zSendId);
                    }
                }

            }
            else if(relation1 == 'l' && relation2 == 'r'){
                /*
                    2      z                   x
                          / \                /   \
                   1     A   y              z     y
                            / \            / \   / \
                  0        x   B    -->   A  C  D  B
                          / \
                         C   D
              */
                int cEgoTreeId = info0.getEgoTreeIdOfLeftChild();
                int cSendId = info0.getSendIdOfLeftChild();
                int cWeight = info0.getWeightOfLeftChild();

                int dEgoTreeId = info0.getEgoTreeIdOfRightChild();
                int dSendId = info0.getSendIdOfRightChild();
                int dWeight = info0.getWeightOfRightChild();

                // z set right child to C
                entryForZ.setEgoTreeIdOfRightChild(cEgoTreeId);
                entryForZ.setSendIdOfRightChild(cSendId);
                // z set parent to x
                entryForZ.setEgoTreeIdOfParent(xEgoTreeId);
                entryForZ.setSendIdOfParent(xSendId);

                // x set left child to z
                entryForX.setEgoTreeIdOfLeftChild(zEgoTreeId);
                entryForX.setSendIdOfLeftChild(zSendId);
                // x set right child to y
                entryForX.setEgoTreeIdOfRightChild(yEgoTreeId);
                entryForX.setSendIdOfRightChild(ySendId);
                // x set parent to p
                entryForX.setEgoTreeIdOfParent(pEgoTreeId);
                entryForX.setSendIdOfParent(pSendId);

                //y set left child to D
                entryForY.setEgoTreeIdOfLeftChild(dEgoTreeId);
                entryForY.setSendIdOfLeftChild(dSendId);
                //y set parent to x
                entryForY.setEgoTreeIdOfParent(xEgoTreeId);
                entryForY.setSendIdOfParent(xSendId);

                // update weight
                entryForZ.setWeightOfRight(cWeight);
                entryForY.setWeightOfLeft(dWeight);
                entryForX.setWeightOfLeft(entryForZ.getWeight());
                entryForX.setWeightOfRight(entryForY.getWeight());


                // set c ' parent to Z
                if(cEgoTreeId>0){
                    SendEntry entryForC = ((EntryGetter)Tools.getNodeByID(cSendId))
                            .getCorrespondingEntry(cEgoTreeId, largeId);
                    if(entryForC == null){
                        Tools.warning("Check what happen in  Rotation Helper whey c have ego tree id but no entry");
                    }
                    else{
                        entryForC.setEgoTreeIdOfParent(zEgoTreeId);
                        entryForC.setSendIdOfParent(zSendId);
                    }
                }

                // set D's parent to y
                if(dEgoTreeId>0){
                    SendEntry entryForD = ((EntryGetter)Tools.getNodeByID(dSendId))
                            .getCorrespondingEntry(dEgoTreeId, largeId);
                    if(entryForD == null){
                        Tools.warning("Check what happen in  Rotation Helper whey d have ego tree id but no entry");
                    }
                    else{
                        entryForD.setEgoTreeIdOfParent(yEgoTreeId);
                        entryForD.setSendIdOfParent(ySendId);
                    }
                }

            }
            else if(relation1 == 'r' && relation2 == 'l'){
                /*
                    2      z                   x
                          / \                /   \
                   1     y   D              y     z
                        / \                / \   / \
                  0    A   x       -->    A  B  C  D
                          / \
                         B   C
              */
                int bEgoTreeId = info0.getEgoTreeIdOfLeftChild();
                int bSendId = info0.getSendIdOfLeftChild();
                int bWeight = info0.getWeightOfLeftChild();

                int cEgoTreeId = info0.getEgoTreeIdOfRightChild();
                int cSendId = info0.getSendIdOfRightChild();
                int cWeight = info0.getWeightOfRightChild();

                // z set left child to C
                entryForZ.setEgoTreeIdOfLeftChild(cEgoTreeId);
                entryForZ.setSendIdOfLeftChild(cSendId);
                // z set parent to x
                entryForZ.setEgoTreeIdOfParent(xEgoTreeId);
                entryForZ.setSendIdOfParent(xSendId);

                // x set right child to z
                entryForX.setEgoTreeIdOfRightChild(zEgoTreeId);
                entryForX.setSendIdOfRightChild(zSendId);
                // x set left child to y
                entryForX.setEgoTreeIdOfLeftChild(yEgoTreeId);
                entryForX.setSendIdOfLeftChild(ySendId);
                // x set parent to p
                entryForX.setEgoTreeIdOfParent(pEgoTreeId);
                entryForX.setSendIdOfParent(pSendId);

                //y set right child to B
                entryForY.setEgoTreeIdOfRightChild(bEgoTreeId);
                entryForY.setSendIdOfRightChild(bSendId);
                //y set parent to x
                entryForY.setEgoTreeIdOfParent(xEgoTreeId);
                entryForY.setSendIdOfParent(xSendId);

                // update weight
                entryForZ.setWeightOfLeft(cWeight);
                entryForY.setWeightOfRight(bWeight);
                entryForX.setWeightOfRight(entryForZ.getWeight());
                entryForX.setWeightOfLeft(entryForY.getWeight());


                // set B's parent to y
                if(bEgoTreeId>0){
                    SendEntry entryForB = ((EntryGetter)Tools.getNodeByID(bSendId))
                            .getCorrespondingEntry(bEgoTreeId, largeId);
                    if(entryForB == null){
                        Tools.warning("Check what happen in  Rotation Helper whey c have ego tree id but no entry");
                    }
                    else{
                        entryForB.setEgoTreeIdOfParent(yEgoTreeId);
                        entryForB.setSendIdOfParent(ySendId);
                    }
                }

                // set C's parent to z
                if(cEgoTreeId>0){
                    SendEntry entryForC = ((EntryGetter)Tools.getNodeByID(cSendId))
                            .getCorrespondingEntry(cEgoTreeId, largeId);
                    if(entryForC == null){
                        Tools.warning("Check what happen in  Rotation Helper whey c have ego tree id but no entry");
                    }
                    else{
                        entryForC.setEgoTreeIdOfParent(zEgoTreeId);
                        entryForC.setSendIdOfParent(zSendId);
                    }
                }

            }
            else{
                Tools.fatalError("At least one of relations is wrong!!!!!!!!!!");
            }

            // set and unset root, and change most upper node's child
            // todo parent可以最后设置，
            if(lNFlag){
                // means the upper node is root
                if(relation1 == 'l' && relation2 == 'l'){
                    entryForZ.setEgoTreeRoot(false);
                    entryForY.setEgoTreeRoot(true);
                }
                else if(relation1 == 'r' && relation2 == 'r'){
                    entryForZ.setEgoTreeRoot(false);
                    entryForY.setEgoTreeRoot(true);
                }
                else if(relation1 == 'l' && relation2 == 'r'){
                    entryForZ.setEgoTreeRoot(false);
                    entryForX.setEgoTreeRoot(true);
                }
                else if(relation1 == 'r' && relation2 == 'l'){
                    entryForZ.setEgoTreeRoot(false);
                    entryForX.setEgoTreeRoot(true);
                }
            }

            // set most upper node 's child
            if(position == 3){
                // set B's parent to y
                if(pEgoTreeId>0){
                    SendEntry entryForP = ((EntryGetter)Tools.getNodeByID(pSendId))
                            .getCorrespondingEntry(pEgoTreeId, largeId);
                    if(entryForP == null){
                        Tools.warning("Check what happen in  Rotation Helper whey c have ego tree id but no entry");
                    }
                    else{

                        if(relation1 == 'l' && relation2 == 'l'){
                            //set from z to y
                            entryForP.changeChildFromOldToNew
                                    (zEgoTreeId, yEgoTreeId, ySendId);
                        }
                        else if(relation1 == 'r' && relation2 == 'r'){
                            entryForP.changeChildFromOldToNew
                                    (zEgoTreeId, yEgoTreeId, ySendId);
                        }
                        else if(relation1 == 'l' && relation2 == 'r'){
                            //from z to x
                            entryForP.changeChildFromOldToNew
                                    (zEgoTreeId, xEgoTreeId,xSendId);
                        }
                        else if(relation1 == 'r' && relation2 == 'l'){
                            entryForP.changeChildFromOldToNew
                                    (zEgoTreeId, xEgoTreeId,xSendId);
                        }
                    }
                }
                else{
                    Tools.warning("The egoTree id of the parent < 0");
                }
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

                int bEgoTreeId = info0.getEgoTreeIdOfRightChild();
                int bSendId = info0.getSendIdOfRightChild();


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
                entryForY.setSendIdOfLeftChild(bSendId);
                entryForY.setEgoTreeIdOfLeftChild(bEgoTreeId);


                // update weight:
                entryForY.setWeightOfLeft(info0.getWeightOfRightChild());
                entryForX.setWeightOfRight(entryForY.getWeight());

                // set B's parent to y
                if(bEgoTreeId>0){
                    SendEntry entryForB = ((EntryGetter)Tools.getNodeByID(bSendId))
                            .getCorrespondingEntry(bEgoTreeId, largeId);
                    if(entryForB == null){
                        Tools.warning("Check what happen in  Rotation Helper whey c have ego tree id but no entry");
                    }
                    else{
                        entryForB.setEgoTreeIdOfParent(yEgoTreeId);
                        entryForB.setSendIdOfParent(ySendId);
                    }
                }

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

                int aEgoTreeId = info0.getEgoTreeIdOfLeftChild();
                int aSendId = info0.getSendIdOfLeftChild();

                // set x parent as p
                entryForX.setEgoTreeIdOfParent(pEgoTreeId);
                entryForX.setSendIdOfParent(pSendId);
                // set x left child as y
                entryForX.setEgoTreeIdOfLeftChild(yEgoTreeId);
                entryForX.setSendIdOfLeftChild(ySendId);


                // set y parent as x
                entryForY.setSendIdOfParent(xSendId);
                entryForY.setEgoTreeIdOfParent(xEgoTreeId);

                // set y right child as a
                entryForY.setSendIdOfRightChild(aSendId);
                entryForY.setEgoTreeIdOfRightChild(aEgoTreeId);


                // update weight:
                entryForY.setWeightOfRight(info0.getWeightOfLeftChild());
                entryForX.setWeightOfLeft(entryForY.getWeight());

                // set A's parent to y
                if(aEgoTreeId>0){
                    SendEntry entryForA = ((EntryGetter)Tools.getNodeByID(aSendId))
                            .getCorrespondingEntry(aEgoTreeId, largeId);
                    if(entryForA == null){
                        Tools.warning("Check what happen in  Rotation Helper whey c have ego tree id but no entry");
                    }
                    else{
                        entryForA.setEgoTreeIdOfParent(yEgoTreeId);
                        entryForA.setSendIdOfParent(ySendId);
                    }
                }

            }

            // set most upper node's child
            // todo 设置父亲的

            // set root change
            if(requestClusterMessage.isLnFlag()){
                entryForX.setEgoTreeRoot(true);
                entryForY.setEgoTreeRoot(false);
            }
            else{
                Tools.warning("In position 1 rotation, it must have the Ln node as upper node ");
            }

        }



    }


    private void changeRoutingMessageDestination(AcceptClusterMessage acceptClusterMessage){

        int largeId = acceptClusterMessage.getLargeId();

        RequestClusterMessage requestClusterMessage =
                acceptClusterMessage.getRequestClusterMessage();

        int position = requestClusterMessage.getPosition();

        if(position == 3 || position == 2){
            //这两实际上是一种，只不过2的时候没有传递到LN去罢了
            char relation1 = requestClusterMessage.getRelationFromNode0ToNode1();
            char relation2 = requestClusterMessage.getRelationFromNode1ToNode2();

            NodeInfo info0 = requestClusterMessage.getNodeInfoOf(0);
            NodeInfo info1 = requestClusterMessage.getNodeInfoOf(1);
            NodeInfo info2 = requestClusterMessage.getNodeInfoOf(2);

            int xSendId = info0.getCurNodeTrueId();
            int xEgoTreeId = info0.getCurNodeEgoId();

            int ySendId = info1.getCurNodeTrueId();
            int yEgoTreeId = info1.getCurNodeEgoId();

            int zSendId = info2.getCurNodeTrueId();
            int zEgoTreeId = info2.getCurNodeEgoId();

            // X
            EntryGetter entryGetterForX = (EntryGetter)Tools.getNodeByID(xSendId);
            assert entryGetterForX != null;
            SendEntry entryForX = entryGetterForX.getCorrespondingEntry(xEgoTreeId, largeId);
            // Y
            EntryGetter entryGetterForY = (EntryGetter)Tools.getNodeByID(ySendId);
            assert entryGetterForY != null;
            SendEntry entryForY = entryGetterForY.getCorrespondingEntry(yEgoTreeId, largeId);
            // Z
            EntryGetter entryGetterForZ = (EntryGetter)Tools.getNodeByID(zSendId);
            assert entryGetterForZ != null;
            SendEntry entryForZ = entryGetterForZ.getCorrespondingEntry(zEgoTreeId, largeId);


            if( (relation1 == 'l' && relation2 == 'l') ||
                    (relation1 == 'r' && relation2 == 'r')){
                /*

                    2      z                   y
                          / \                /   \
                   1     y   D              x     z
                        / \                / \   / \
                  0    x   C     -->      A  B  C  D
                      / \
                     A   B


                    2      z                   y
                          / \                /   \
                   1     A   y              z     x
                            / \            / \   / \
                  0        B   x    -->   A  B  C  D
                              / \
                             C   D
              */
                boolean conditionOneFlag = (relation1 == 'l' && relation2 == 'l' );


                Queue<RoutingMessage> messageQueue = entryForZ.getRoutingMessageQueue();
                Queue<RoutingMessage> messageQueueTmp = new LinkedList<>();

                while(!messageQueue.isEmpty()){
                    RoutingMessage message = messageQueue.poll();
                    if(!message.isUpForward())
                    {
                        if(conditionOneFlag){
                            if(message.getDestination() <= yEgoTreeId){
                                message.setSpecialHop(yEgoTreeId);
                            }
                        }
                        else{
                            if(message.getDestination() >= yEgoTreeId){
                                message.setSpecialHop(yEgoTreeId);
                            }
                        }
                    }
                    messageQueueTmp.add(message);
                }

                messageQueue.addAll(messageQueueTmp);
            }
            else if( (relation1 == 'l' && relation2 == 'r') ||
                    ( relation1 == 'r' && relation2 == 'l' ) )
            {
                /*
                    2      z                   x
                          / \                /   \
                   1     A   y              z     y
                            / \            / \   / \
                  0        x   B    -->   A  C  D  B
                          / \
                         C   D
              */
               /*
                    2      z                   x
                          / \                /   \
                   1     y   D              y     z
                        / \                / \   / \
                  0    A   x       -->    A  B  C  D
                          / \
                         B   C
              */
                boolean conditionOneFlag =  (relation1 == 'l' && relation2 == 'r');

                // for z
                Queue<RoutingMessage> messageQueueForZ = entryForZ.getRoutingMessageQueue();
                Queue<RoutingMessage> messageQueueTmp = new LinkedList<>();

                while(!messageQueueForZ.isEmpty()){
                    RoutingMessage message = messageQueueForZ.poll();
                    if(!message.isUpForward())
                    {
                        if(conditionOneFlag){
                            if(message.getDestination() >= xEgoTreeId){
                                message.setSpecialHop(xEgoTreeId);
                            }
                        }
                        else{
                            if(message.getDestination() <= xEgoTreeId){
                                message.setSpecialHop(xEgoTreeId);
                            }
                        }
                    }
                    messageQueueTmp.add(message);
                }

                messageQueueForZ.addAll(messageQueueTmp);

                // for y
                Queue<RoutingMessage> messageQueueForY = entryForY.getRoutingMessageQueue();
                Queue<RoutingMessage> messageQueueTmp2 = new LinkedList<>();

                while(!messageQueueForY.isEmpty()){
                    RoutingMessage message = messageQueueForY.poll();
                    if(!message.isUpForward())
                    {
                        if(conditionOneFlag){
                            if(message.getDestination() <= xEgoTreeId){
                                message.setSpecialHop(xEgoTreeId);
                            }
                        }
                        else{
                            if(message.getDestination() >= xEgoTreeId){
                                message.setSpecialHop(xEgoTreeId);
                            }
                        }
                    }
                    messageQueueTmp2.add(message);
                }
                messageQueueForY.addAll(messageQueueTmp2);
            }
            else{
                Tools.fatalError("At least one of relations is wrong!!!!!!!!!!");
            }

        }
        else if(position == 1){
            char relation = requestClusterMessage.getRelationFromNode0ToNode1();

            NodeInfo info0 = requestClusterMessage.getNodeInfoOf(0);
            NodeInfo info1 = requestClusterMessage.getNodeInfoOf(1);

            int xSendId = info0.getCurNodeTrueId();
            int xEgoTreeId = info0.getCurNodeEgoId();

            int ySendId = info1.getCurNodeTrueId();
            int yEgoTreeId = info1.getCurNodeEgoId();

            // X
            EntryGetter entryGetterForX = (EntryGetter)Tools.getNodeByID(xSendId);
            assert entryGetterForX != null;
            SendEntry entryForX = entryGetterForX.getCorrespondingEntry(xEgoTreeId, largeId);
            // Y
            EntryGetter entryGetterForY = (EntryGetter)Tools.getNodeByID(ySendId);
            assert entryGetterForY != null;
            SendEntry entryForY = entryGetterForY.getCorrespondingEntry(yEgoTreeId, largeId);


            /*
                          /                 /
                         y                *x
                        / \               / \
                      *x   C     -->     A   y
                      / \                   / \
                     A   B                 B   C

                          /                 /
                         y                *x
                        / \               / \
                       C   x     -->     y   B
                          / \           / \
                         A   B         C   A
              */

            boolean conditionOneFlag = (relation == 'l');


            Queue<RoutingMessage> messageQueue = entryForY.getRoutingMessageQueue();
            Queue<RoutingMessage> messageQueueTmp = new LinkedList<>();

            while(!messageQueue.isEmpty()){
                RoutingMessage message = messageQueue.poll();
                if(!message.isUpForward())
                {
                    if(conditionOneFlag){
                        if(message.getDestination() <= xEgoTreeId){
                            message.setSpecialHop(xEgoTreeId);
                        }
                    }
                    else{
                        if(message.getDestination() >= xEgoTreeId){
                            message.setSpecialHop(xEgoTreeId);
                        }
                    }
                }
                messageQueueTmp.add(message);
            }
            messageQueue.addAll(messageQueueTmp);
        }
    }


}

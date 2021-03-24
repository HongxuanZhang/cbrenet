package projects.cbrenet.nodes.nodeImplementations.nodeHelper;

import projects.cbrenet.nodes.messages.controlMessage.NodeInfo;
import projects.cbrenet.nodes.messages.controlMessage.RequestClusterMessage;
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





}

package projects.cbrenet.nodes.tableEntry;

public class NodeInfo {

    int curNodeTrueId;
    int curNodeEgoId; // 在AN中也就是Helped Id

    int egoTreeIdOfLeftChild;
    int egoTreeIdOfRightChild;

    int sendIdOfLeftChild;
    int sendIdOfRightChild;


    int counterOfCurNode;
    int weightOfLeftChild;
    int weightOfRightChild;


    public NodeInfo(int curNodeTrueId, int curNodeEgoId,
                    int egoTreeIdOfLeftChild, int egoTreeIdOfRightChild,
                    int sendIdOfLeftChild, int sendIdOfRightChild,
                    int counterOfCurNode, int weightOfLeftChild, int weightOfRightChild){
        this.curNodeTrueId = curNodeTrueId;
        this.curNodeEgoId = curNodeEgoId;

        this.egoTreeIdOfLeftChild = egoTreeIdOfLeftChild;
        this.egoTreeIdOfRightChild = egoTreeIdOfRightChild;

        this.sendIdOfLeftChild = sendIdOfLeftChild;
        this.sendIdOfRightChild = sendIdOfRightChild;

        this.counterOfCurNode = counterOfCurNode;

        this.weightOfLeftChild = weightOfLeftChild;
        this.weightOfRightChild = weightOfRightChild;
    }

    public int getCurNodeTrueId() {
        return curNodeTrueId;
    }

    public int getEgoTreeIdOfLeftChild() {
        return egoTreeIdOfLeftChild;
    }

    public int getEgoTreeIdOfRightChild() {
        return egoTreeIdOfRightChild;
    }

    public int getSendIdOfLeftChild() {
        return sendIdOfLeftChild;
    }

    public int getSendIdOfRightChild() {
        return sendIdOfRightChild;
    }

    public int getCounterOfCurNode() {
        return counterOfCurNode;
    }

    public int getWeightOfCurNode(){
        return this.counterOfCurNode + this.weightOfLeftChild + this.weightOfRightChild;
    }

    public int getWeightOfLeftChild() {
        return weightOfLeftChild;
    }

    public int getWeightOfRightChild() {
        return weightOfRightChild;
    }

    public int getCurNodeEgoId() {
        return curNodeEgoId;
    }
}

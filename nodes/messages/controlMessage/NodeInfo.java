package projects.cbrenet.nodes.messages.controlMessage;

public class NodeInfo {

    int curNodeId;

    int egoTreeIdOfLeftChild;
    int egoTreeIdOfRightChild;

    int sendIdOfLeftChild;
    int sendIdOfRightChild;


    int counterOfCurNode;
    int weightOfLeftChild;
    int weightOfRightChild;


    public NodeInfo(int curNodeId, int egoTreeIdOfLeftChild, int egoTreeIdOfRightChild,
                    int sendIdOfLeftChild, int sendIdOfRightChild,
                    int counterOfCurNode, int weightOfLeftChild, int weightOfRightChild){
        this.curNodeId = curNodeId;
        this.egoTreeIdOfLeftChild = egoTreeIdOfLeftChild;
        this.egoTreeIdOfRightChild = egoTreeIdOfRightChild;

        this.sendIdOfLeftChild = sendIdOfLeftChild;
        this.sendIdOfRightChild = sendIdOfRightChild;

        this.counterOfCurNode = counterOfCurNode;

        this.weightOfLeftChild = weightOfLeftChild;
        this.weightOfRightChild = weightOfRightChild;
    }

    public int getCurNodeId() {
        return curNodeId;
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
}

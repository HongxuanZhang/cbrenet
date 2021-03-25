package projects.cbrenet.nodes.messages.controlMessage;

import sinalgo.nodes.messages.Message;

import java.util.HashMap;

/**
 * RequestCluster
 */
public class RequestClusterMessage extends Message implements Comparable<RequestClusterMessage> {

    private final int largeId;
    private int currentNode;
    private int requesterId;

    private double generateTime; // used as priority

    private int position;
    private boolean isFinalNode; // keep track if this node is final node in current request


    public RequestClusterMessage(int largeId, int currentNode, int requesterId, int position, double generateTime) {
        this.largeId = largeId;
        this.currentNode = currentNode;
        this.requesterId = requesterId;

        this.generateTime = generateTime;
        this.position = position;
        this.isFinalNode = false;

        this.nodeInfoMap = new HashMap<>();

    }



    // 这个用用于指示 reject 信息传播的
    private int theEgoTreeMasterOfCluster; // the 3 node, would be used in adjust，
    // when 2 node or 3 node is the root of the ego-tree. it would equal to the most upper ego tree id.
    private int theSendIdOfCluster;


    // 用于指示整个子树旋转后的父亲结点。。。
    private int theMostUpperEgoTreeId;     // 这几个如果是3结点，那他们跟上面的是一样的。
    private int theMostUpperSendId;        // 毕竟还是要相连的嘛//
    private boolean isLnFlag;              // 用于指示cluster最上层的那个结点是否就是LN




    private HashMap<Integer, NodeInfo> nodeInfoMap;

    public void addNodeInfoPair(int position, NodeInfo info){
        this.nodeInfoMap.put(position,info);
    }

    public NodeInfo getNodeInfoOf(int position){
        return this.nodeInfoMap.getOrDefault(position, null);
    }



    char relationFromNode0ToNode1;
    char relationFromNode1ToNode2;

    public char getRelationFromNode0ToNode1() {
        return relationFromNode0ToNode1;
    }

    public char getRelationFromNode1ToNode2() {
        return relationFromNode1ToNode2;
    }

    public void setRelationFromNode0ToNode1(char relationFromNode0ToNode1) {
        assert relationFromNode0ToNode1 == 'l' || relationFromNode0ToNode1 == 'r';
        this.relationFromNode0ToNode1 = relationFromNode0ToNode1;
    }

    public void setRelationFromNode1ToNode2(char relationFromNode1ToNode2) {
        assert relationFromNode1ToNode2 == 'l' || relationFromNode1ToNode2 == 'r';
        this.relationFromNode1ToNode2 = relationFromNode1ToNode2;
    }




    public void setTheEgoTreeMasterOfCluster(int theEgoTreeMasterOfCluster) {
        this.theEgoTreeMasterOfCluster = theEgoTreeMasterOfCluster;
    }

    public int getTheEgoTreeMasterOfCluster() {
        return theEgoTreeMasterOfCluster;
    }

    // getter

    public int getCurrentNode() {
        return currentNode;
    }

    public int getRequesterId() {
        return requesterId;
    }


    public double getGenerateTime() {
        return generateTime;
    }

    public int getPosition() {
        return position;
    }

    public boolean isFinalNode() {
        return isFinalNode;
    }

    public void setCurrentNode(int currentNode) {
        this.currentNode = currentNode;
    }

    public void setRequesterId(int requesterId) {
        this.requesterId = requesterId;
    }


    public void setGenerateTime(double generateTime) {
        this.generateTime = generateTime;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void shiftPosition() {
        this.position++;
    }

    public void setFinalNode() {
        this.isFinalNode = true;
    }

    public int getLargeId() {
        return largeId;
    }

    public int getTheSendIdOfCluster() {
        return theSendIdOfCluster;
    }

    public int getTheMostUpperEgoTreeId() {
        return theMostUpperEgoTreeId;
    }

    public int getTheMostUpperSendId() {
        return theMostUpperSendId;
    }

    public boolean isLnFlag() {
        return isLnFlag;
    }

    public void setTheSendIdOfCluster(int theSendIdOfCluster) {
        this.theSendIdOfCluster = theSendIdOfCluster;
    }

    public void setTheMostUpperEgoTreeId(int theMostUpperEgoTreeId) {
        this.theMostUpperEgoTreeId = theMostUpperEgoTreeId;
    }

    public void setTheMostUpperSendId(int theMostUpperSendId) {
        this.theMostUpperSendId = theMostUpperSendId;
    }

    public void setLnFlag(boolean lnFlag) {
        isLnFlag = lnFlag;
    }

    @Override
    public Message clone() {
        return this;
    }

    @Override
	public int compareTo(RequestClusterMessage o) {
		int value = Double.compare(this.generateTime, o.generateTime);
		if (value == 0) { // In case tie, compare the id of the source node
			return this.requesterId - o.requesterId;
		} else {
			return value;
		}
	}
    
}
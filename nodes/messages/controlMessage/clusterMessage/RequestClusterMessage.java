package projects.cbrenet.nodes.messages.controlMessage.clusterMessage;

import projects.cbrenet.nodes.tableEntry.NodeInfo;
import sinalgo.nodes.messages.Message;

import java.util.HashMap;

/**
 * RequestCluster
 */
public class RequestClusterMessage extends ClusterRelatedMessage implements Comparable<RequestClusterMessage> {

    private int currentNode;
    private int requesterId;

    private double generateTime; // used as priority

    private int position;
    private boolean isFinalNode; // keep track if this node is final node in current request

    public int ddl = 50;

    public boolean ddlMinus(){
        this.ddl --;
        return ddl <= 0;
    }


    public RequestClusterMessage(int largeId, int currentNode, int requesterId, int position, double generateTime) {
        super(largeId);
        this.currentNode = currentNode;
        this.requesterId = requesterId;

        this.generateTime = generateTime;
        this.position = position;
        this.isFinalNode = false;

        this.nodeInfoMap = new HashMap<>();

        this.theEgoTreeIdOfClusterMaster = -1;
        this.theSendIdOfClusterMaster = -1;
    }




    // master id , 是cluster的master,与 theMostUpperEgoTreId不一定一样，下面那个可以是LN
    // 这个用用于指示 reject 信息传播的
    private int theEgoTreeIdOfClusterMaster; // the 3 node, would be used in adjust，
    // when 2 node or 3 node is the root of the ego-tree. it would equal to the most upper ego tree id.
    private int theSendIdOfClusterMaster;


    // 用于指示整个子树旋转后的父亲结点。。。
    private int theMostUpperNodeEgoTreeId;     // 这几个如果是3结点，那他们跟上面的是一样的。
    private int theMostUpperNodeSendId;        // 毕竟还是要相连的嘛//
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




    public void setTheEgoTreeIdOfClusterMaster(int theEgoTreeIdOfClusterMaster) {
        this.theEgoTreeIdOfClusterMaster = theEgoTreeIdOfClusterMaster;
    }

    public int getTheEgoTreeIdOfClusterMaster() {
        return theEgoTreeIdOfClusterMaster;
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

    public int getTheSendIdOfClusterMaster() {
        return theSendIdOfClusterMaster;
    }

    public int getTheMostUpperNodeEgoTreeId() {
        return theMostUpperNodeEgoTreeId;
    }

    public int getTheMostUpperNodeSendId() {
        return theMostUpperNodeSendId;
    }

    public boolean isLnFlag() {
        return isLnFlag;
    }

    public void setTheSendIdOfClusterMaster(int theSendIdOfClusterMaster) {
        this.theSendIdOfClusterMaster = theSendIdOfClusterMaster;
    }

    public void setTheMostUpperNodeEgoTreeId(int theMostUpperNodeEgoTreeId) {
        this.theMostUpperNodeEgoTreeId = theMostUpperNodeEgoTreeId;
    }

    public void setTheMostUpperNodeSendId(int theMostUpperNodeSendId) {
        this.theMostUpperNodeSendId = theMostUpperNodeSendId;
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
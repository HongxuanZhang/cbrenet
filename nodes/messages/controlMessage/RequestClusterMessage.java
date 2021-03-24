package projects.cbrenet.nodes.messages.controlMessage;

import projects.cbrenet.nodes.messages.deletePhaseMessages.Relation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

import java.util.ArrayList;
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
    
    public RequestClusterMessage(RequestClusterMessage msg) {
        this.largeId = msg.largeId;
        this.currentNode = msg.getCurrentNode();
        this.requesterId = msg.getRequesterId();

        this.generateTime = msg.getGenerateTime();
        this.position = msg.getPosition();
        this.isFinalNode = false;
    }

    public RequestClusterMessage(int largeId, int currentNode, int requesterId, int position, double generateTime) {
        this.largeId = largeId;
        this.currentNode = currentNode;
        this.requesterId = requesterId;

        this.generateTime = generateTime;
        this.position = position;
        this.isFinalNode = false;
    }



    private int theMasterOfCluster; // the 3 node, would be used in adjust

    private HashMap<Integer, NodeInfo> nodeInfoMap = new HashMap<>();

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

    // 0结点左、右
    // 1结点左、右
    // 2结点左、右
    // 2结点即可计算调整前后的势变
    private ArrayList<Integer> weights = new ArrayList<>(6);

    private ArrayList<Integer> childs = new ArrayList<>(6);

    private ArrayList<Integer> linkIds = new ArrayList<>(6);




    public void setWeightAt(int index, int weight){
        this.weights.set(index, weight);
    }

    public int getWeightAt(int index){
        return this.weights.get(index);
    }

    public void setChildAt(int index, int childId){
        this.childs.set(index, childId);
    }

    public int getChildAt(int index){
        return this.childs.get(index);
    }

    public void setLinkIdAt(int index, int weight){
        this.linkIds.set(index, weight);
    }

    public int getLinkIdAt(int index){
        return this.linkIds.get(index);
    }

    public void setTheMasterOfCluster(int theMasterOfCluster) {
        this.theMasterOfCluster = theMasterOfCluster;
    }

    public int getTheMasterOfCluster() {
        return theMasterOfCluster;
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
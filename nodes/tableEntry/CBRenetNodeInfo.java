package projects.cbrenet.nodes.tableEntry;

import projects.cbrenet.nodes.nodeImplementations.CbRenetBinarySearchTreeLayer;
import projects.defaultProject.nodes.nodeImplementations.BinarySearchTreeLayer;
import projects.defaultProject.nodes.tableEntry.NodeInfo;
import sinalgo.tools.Tuple;

import java.util.HashMap;
import java.util.HashSet;

public class CBRenetNodeInfo{

    private CbRenetBinarySearchTreeLayer node;
    // parent & left right child
    private HashMap<Integer, CbRenetBinarySearchTreeLayer> parents;
    private HashMap<Integer, CbRenetBinarySearchTreeLayer> leftChildren;
    private HashMap<Integer, CbRenetBinarySearchTreeLayer> rightChildren;

    private HashMap<Integer, Integer> minIdInSubtrees;
    private HashMap<Integer, Integer> maxIdInSubtrees;

    private boolean bigFlag;
    private long weight;


    public CBRenetNodeInfo() {
        this.node = null;
        this.parents = null;
        this.leftChildren = null;
        this.rightChildren = null;
        this.minIdInSubtrees = null;
        this.maxIdInSubtrees = null;
    }

    public CBRenetNodeInfo(
            CbRenetBinarySearchTreeLayer node,
            HashMap<Integer, CbRenetBinarySearchTreeLayer> parents,
            HashMap<Integer, CbRenetBinarySearchTreeLayer> leftChildren,
            HashMap<Integer, CbRenetBinarySearchTreeLayer> rightChildren,
            HashMap<Integer, Integer> minIdInSubtrees,
            HashMap<Integer, Integer> maxIdInSubtrees,
            long weight) {
        this.node = node;
        this.parents = parents;
        this.leftChildren = leftChildren;
        this.rightChildren = rightChildren;
        this.minIdInSubtrees = minIdInSubtrees;
        this.maxIdInSubtrees = maxIdInSubtrees;
        this.weight = weight;
    }

    public CbRenetBinarySearchTreeLayer getNode() {
        return this.node;
    }

    public CbRenetBinarySearchTreeLayer getParent(int largeId) {
        return this.parents.getOrDefault(largeId, null);
    }

    public CbRenetBinarySearchTreeLayer getLeftChild(int largeId) {
        return this.leftChildren.getOrDefault(largeId, null);
    }

    public CbRenetBinarySearchTreeLayer getRightChild(int largeId) {
        return this.rightChildren.getOrDefault(largeId, null);
    }

    public int getMinIdInSubtree(int largeId) {
        return this.minIdInSubtrees.getOrDefault(largeId, -1);
    }

    public int getMaxIdInSubtree(int largeId) {
        return this.maxIdInSubtrees.getOrDefault(largeId,-1);
    }

    public void setNode(CbRenetBinarySearchTreeLayer node) {
        this.node = node;
    }

    public void setParent(int largeId,CbRenetBinarySearchTreeLayer parent) {
        this.parents.put(largeId,parent);
    }

    public void setLeftChild(int largeId,CbRenetBinarySearchTreeLayer leftChild) {
        this.leftChildren.put(largeId,leftChild);
    }

    public void setRightChild(int largeId,CbRenetBinarySearchTreeLayer rightChild) {
        this.rightChildren.put(largeId,rightChild);
    }

    public void setMinIdInSubtree(int largeId, int minIdInSubtree) {
        this.minIdInSubtrees.put(largeId, minIdInSubtree);
    }

    public void setMaxIdInSubtree(int largeId, int maxIdInSubtree) {
        this.maxIdInSubtrees.put(largeId, maxIdInSubtree);;
    }

    void initForBaseInfo(){
        this.parents = new HashMap<>();
        this.leftChildren = new HashMap<>();
        this.rightChildren = new HashMap<>();
        this.minIdInSubtrees = new HashMap<>();
        this.maxIdInSubtrees = new HashMap<>();
    }

    void initInfoOfNodes(){
        // for large node
        this.largeSmallNodes = new HashSet<>();
        this.largeSmallNodeIds = new HashSet<>();
        this.largeLargeNodes = new HashSet<>();
        this.largeLargeNodeIds = new HashSet<>();

        // for small node
        this.smallHelpedNodes = new HashSet<>();
        this.smallSmallNodes = new HashSet<>();
        this.smallSmallNodeIds = new HashSet<>();
        this.smallLargeNodes = new HashSet<>();
        this.smallLargeNodeIds = new HashSet<>();
    }

    //for big node
    private CBRenetNodeInfo rootNodeForBigNode;
    // the small node connected to this large Node
    private HashSet<CbRenetBinarySearchTreeLayer> largeSmallNodes;
    private HashSet<Integer> largeSmallNodeIds;
    // the large node connected to this large Node
    private HashSet<CbRenetBinarySearchTreeLayer> largeLargeNodes;
    private HashSet<Integer> largeLargeNodeIds;


    //for small node
    // S: Small node
    private HashSet<CbRenetBinarySearchTreeLayer> smallSmallNodes;
    private HashSet<Integer> smallSmallNodeIds;
    // L: Large node
    private HashSet<CbRenetBinarySearchTreeLayer> smallLargeNodes;
    private HashSet<Integer> smallLargeNodeIds;
    // H: Helper node
    private HashSet<Tuple<Integer, Integer>> smallHelpedNodes; // from integer one to integer two large node

    private void init(){
        this.bigFlag = false; // default small node
        this.weight = Long.MIN_VALUE;
        this.initInfoOfNodes();
    }

    private void changeNodeMode(){

    }


}

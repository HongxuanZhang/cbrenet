package projects.cbrenet.nodes.nodeImplementations;

import java.util.HashMap;


import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.tableEntry.CBInfo;
import projects.cbrenet.nodes.tableEntry.CBRenetNodeInfo;

/**
 * RotationLayer
 */
public abstract class RotationLayer extends ClusterLayer {

  private boolean rotating;
  private boolean routing;

  private CbRenetMessage cbnetMessage;

  private double epsilon = -1.5;   //判断用的标准

  @Override
  public void init() {
    super.init();

    this.rotating = false;
    this.routing = false;
    this.cbnetMessage = null;
  }

  @Override
  public void timeslot0() {
    this.tryOperation();
  }

  public void tryOperation() {
    for(int largeId : this.getLargeIds()) {

    }
    if (this.hasCBNetMessage()) {
      // System.out.println("Node " + ID + "has cbnet message");
      this.cbnetMessage = this.getTopCBNetMessage();
      int largeId = this.cbnetMessage.getLargeId();
      if(largeId == -1){
        //todo
      }
      else{
        if (this.getMinIdInSubtree(largeId) <= cbnetMessage.getDst()
                && cbnetMessage.getDst() <= this.getMaxIdInSubtree(largeId)) {
          this.sendRequestClusterDown(largeId,ID, cbnetMessage.getSrc(), cbnetMessage.getDst(),
                  cbnetMessage.getPriority());
        } else {
          this.sendRequestClusterUp(largeId, ID, cbnetMessage.getSrc(), cbnetMessage.getDst(),
                  cbnetMessage.getPriority());
        }
      }
    }
  }

  @Override
  public void clusterCompletedBottomUp(int largeId, HashMap<String, CBInfo> cluster) {
    this.removeTopCBNetMessage(); // 这有什么用呢？
    this.rotateBottomUp(largeId, cluster);
    // System.out.println("Cluster formed bottom up at node " + ID);
  }

  @Override
  public void clusterCompletedTopDown(int largeId,HashMap<String, CBInfo> cluster) {

    this.removeTopCBNetMessage();
    this.rotateTopDown(largeId, cluster);
    // System.out.println("Cluster formed top down at node " + ID);
  }

  @Override
  public void targetNodeFound(CBInfo target) {

    this.removeTopCBNetMessage();
    this.cbnetMessage.incrementRouting(); // DATA LOG
    
    this.incrementWeight();
    this.forwardCBNetMessage(target.getNode().ID, this.cbnetMessage);
    // System.out.println("Cluster target found at node " + ID);
  }

  private void rotateBottomUp(int largeId, HashMap<String, CBInfo> cluster) {
    CBInfo xInfo = cluster.get("x");
    CBInfo yInfo = cluster.get("y");
    CBInfo zInfo = cluster.get("z");

    if (cluster.size() == 3) {
      this.zigBottomUp(largeId,xInfo, yInfo, zInfo);
    }
    else {
      CBInfo wInfo = cluster.get("w");
      CBTreeWeightLayer x = (CBTreeWeightLayer) xInfo.getNode();
      CBTreeWeightLayer y = (CBTreeWeightLayer) yInfo.getNode();

      if ((x == yInfo.getLeftChild(largeId) && y == zInfo.getLeftChild(largeId))
          || (x == yInfo.getRightChild(largeId) && y == zInfo.getRightChild(largeId))) {
        this.zigZigBottomUp(largeId, xInfo, yInfo, zInfo, wInfo);
      } else {
        this.zigZagBottomUp(largeId, xInfo, yInfo, zInfo, wInfo);
      }
    }

  }

  /*
               z                 z
              /                 /
             y                *x
            / \               / \
          *x   c     -->     a   y
          / \                   / \
         a   b                 b   c
  */
  private void zigBottomUp(int largeId,CBInfo xInfo, CBInfo yInfo, CBInfo zInfo) {
    /**
     * 这个函数没有做任何link的调整，但是调整了weight*/
//     System.out.println("zig bottom up operation: " + ID);

    CBTreeWeightLayer x = (CBTreeWeightLayer) xInfo.getNode();
    CBTreeWeightLayer y = (CBTreeWeightLayer) yInfo.getNode();
    this.routing = true;

    // DATA LOG
    this.cbnetMessage.incrementRouting();
    // increment counters
    this.requestRPCTo(x.ID, "setWeight", xInfo.getWeight() + 1, largeId);
    if (y.ID == cbnetMessage.getDst()) {
  	  this.requestRPCTo(y.ID, "setWeight", yInfo.getWeight() + 1, largeId);
    }
    // forward here
    this.forwardCBNetMessage(y.ID, this.cbnetMessage);
//    }
  }

  /*
              z                 *y
             / \               /   \
            y   d             x     z
           / \      -->      / \   / \
         *x   c             a   b c   d
         / \
        a   b
  */
  private void zigZigBottomUp(int largeId,CBInfo xInfo, CBInfo yInfo, CBInfo zInfo, CBInfo wInfo) {
//     System.out.println("zig zig bottom up operation: " + ID);

    CBTreeWeightLayer x = (CBTreeWeightLayer) xInfo.getNode();
    CBTreeWeightLayer y = (CBTreeWeightLayer) yInfo.getNode();
    CBTreeWeightLayer z = (CBTreeWeightLayer) zInfo.getNode();
    CBTreeWeightLayer w = (CBTreeWeightLayer) wInfo.getNode();

    double deltaRank = this.zigDiffRank(largeId, yInfo, zInfo);

//     if (true) {
//     if (false) {
    if (deltaRank < this.epsilon) {
      this.rotating = true;

      // DATA LOG
      this.cbnetMessage.incrementRotations();
      this.cbnetMessage.incrementRouting();
      // forward message
      this.forwardCBNetMessage(y.ID, this.cbnetMessage);

      // set new child of node z
      if (wInfo.getLeftChild(largeId) == z) {
        this.requestRPCTo(w.ID, "changeLeftChildTo", y, largeId);
      } else {
        this.requestRPCTo(w.ID, "changeRightChildTo", y, largeId);
      }

      // calculate the new rank of nodes
      // type of operation----------------------------------------------------
      boolean leftZig = y == zInfo.getLeftChild(largeId);

      RotationLayer b = (RotationLayer) ((leftZig) ? yInfo.getRightChild(largeId) : yInfo.getLeftChild(largeId));

      long yOldWeight = yInfo.getWeight();
      long zOldWeight = zInfo.getWeight();

      long bWeight = (b != null) ? b.getWeight() : 0;

      long zNewWeight = zOldWeight - yOldWeight + bWeight;
      long yNewWeight = yOldWeight - bWeight + zNewWeight;
      // ---------------------------------------------------------------------

      //increment x counter
      this.requestRPCTo(x.ID, "setWeight", xInfo.getWeight() + 1, largeId);
      
      // left zig operetion on node y
      if (zInfo.getLeftChild(largeId) == y) {
        // change node z
        this.requestRPCTo(z.ID, "changeLeftChildTo", (CBTreeWeightLayer) yInfo.getRightChild(largeId), largeId);
        int min = z.ID;
        if (yInfo.getRightChild(largeId) != null) {
          min = yInfo.getRightChild(largeId).getMinIdInSubtree(largeId);
        }
        this.requestRPCTo(z.ID, "setMinIdInSubtree", min, largeId);
        this.requestRPCTo(z.ID, "setWeight", zNewWeight, largeId);

        // change node y
        this.requestRPCTo(y.ID, "changeRightChildTo", z, largeId);
        this.requestRPCTo(y.ID, "setMaxIdInSubtree", zInfo.getMaxIdInSubtree(largeId), largeId);
        this.requestRPCTo(y.ID, "setWeight", yNewWeight, largeId);
      } else {
        // change node z
        this.requestRPCTo(z.ID, "changeRightChildTo", (CBTreeWeightLayer) yInfo.getLeftChild(largeId), largeId);
        int max = z.ID;
        if (yInfo.getLeftChild(largeId) != null) {
          max = yInfo.getLeftChild(largeId).getMaxIdInSubtree(largeId);
        }
        this.requestRPCTo(z.ID, "setMaxIdInSubtree", max, largeId);
        this.requestRPCTo(z.ID, "setWeight", zNewWeight, largeId);

        // change node y
        this.requestRPCTo(y.ID, "changeLeftChildTo", z, largeId);
        this.requestRPCTo(y.ID, "setMinIdInSubtree", zInfo.getMinIdInSubtree(largeId), largeId);
        this.requestRPCTo(y.ID, "setWeight", yNewWeight, largeId);
      }
    }
    else {
      this.routing = true;

      // DATA LOG
      this.cbnetMessage.incrementRouting();
      this.cbnetMessage.incrementRouting();
      // increment counters
      this.requestRPCTo(x.ID, "setWeight", xInfo.getWeight() + 1, largeId);
      this.requestRPCTo(y.ID, "setWeight", yInfo.getWeight() + 1, largeId);
      if (z.ID == cbnetMessage.getDst()) {
    	  this.requestRPCTo(z.ID, "setWeight", zInfo.getWeight() + 1, largeId);
      }
      // forward
      this.forwardCBNetMessage(z.ID, this.cbnetMessage);
    }

  }

  /*
                 w                  w
                /                  /
               z				 *x
              / \               /   \
             y   d             y     z
            / \		  -->     / \   / \
           a   x*            a   b c   d
              / \
             b   c
 */
  private void zigZagBottomUp(int largeId, CBInfo xInfo, CBInfo yInfo, CBInfo zInfo, CBInfo wInfo) {
//     System.out.println("zig zag bottom up operation: " + ID);

    CBTreeWeightLayer x = (CBTreeWeightLayer) xInfo.getNode();
    CBTreeWeightLayer y = (CBTreeWeightLayer) yInfo.getNode();
    CBTreeWeightLayer z = (CBTreeWeightLayer) zInfo.getNode();
    CBTreeWeightLayer w = (CBTreeWeightLayer) wInfo.getNode();

    double deltaRank = this.zigZagDiffRank(largeId, xInfo, yInfo, zInfo);

//    if (true) {
//     if (false) {
    if (deltaRank < this.epsilon) {
      this.rotating = true;

      // DATA LOG
      this.cbnetMessage.incrementRotations();
      this.cbnetMessage.incrementRotations();
      // forward message
      this.forwardCBNetMessage(x.ID, this.cbnetMessage);

      // set new child of node z
      if (wInfo.getLeftChild(largeId) == z) {
        this.requestRPCTo(w.ID, "changeLeftChildTo", x, largeId);
      } else {
        this.requestRPCTo(w.ID, "changeRightChildTo", x, largeId);
      }

      // new weights------------------------------------------------------
      boolean lefZigZag = y == zInfo.getLeftChild(largeId);

      RotationLayer b = (RotationLayer) (lefZigZag ? xInfo.getLeftChild(largeId) : xInfo.getRightChild(largeId));
      RotationLayer c = (RotationLayer) (lefZigZag ? xInfo.getRightChild(largeId) : xInfo.getLeftChild(largeId));

      long xOldWeight = xInfo.getWeight();
      long yOldWeight = yInfo.getWeight();
      long zOldWeight = zInfo.getWeight();

      long bWeight = (b != null) ? b.getWeight() : 0;
      long cWeight = (c != null) ? c.getWeight() : 0;

      long yNewWeight = yOldWeight - xOldWeight + bWeight;
      long zNewWeight = zOldWeight - yOldWeight + cWeight;
      long xNewWeight = xOldWeight - bWeight - cWeight + yNewWeight + zNewWeight;
      // ---------------------------------------------------------------

      // deciding between left or right zigzag operation
      if (y == zInfo.getLeftChild(largeId)) {
        // change node z
        this.requestRPCTo(z.ID, "changeLeftChildTo", (CBTreeWeightLayer) xInfo.getRightChild(largeId), largeId);
        int min = z.ID;
        if (xInfo.getRightChild(largeId) != null) {
          min = xInfo.getRightChild(largeId).getMinIdInSubtree(largeId);
        }
        this.requestRPCTo(z.ID, "setMinIdInSubtree", min, largeId);

        // change node y
        this.requestRPCTo(y.ID, "changeRightChildTo", (CBTreeWeightLayer) xInfo.getLeftChild(largeId), largeId);
        int max = y.ID;
        if (xInfo.getLeftChild(largeId) != null) {
          max = xInfo.getLeftChild(largeId).getMaxIdInSubtree(largeId);
        }
        this.requestRPCTo(y.ID, "setMaxIdInSubtree", max, largeId);

        // change node x
        this.requestRPCTo(x.ID, "changeLeftChildTo", y, largeId);
        this.requestRPCTo(x.ID, "setMinIdInSubtree", yInfo.getMinIdInSubtree(largeId), largeId);
        this.requestRPCTo(x.ID, "changeRightChildTo", z, largeId);
        this.requestRPCTo(x.ID, "setMaxIdInSubtree", zInfo.getMaxIdInSubtree(largeId), largeId);
        this.requestRPCTo(x.ID, "setWeight", xNewWeight, largeId);
        
        //increment counters based on nodes position
        if (x.ID == cbnetMessage.getSrc()) {
        	this.requestRPCTo(z.ID, "setWeight", zNewWeight, largeId);
        	this.requestRPCTo(y.ID, "setWeight", yNewWeight, largeId);
        } else if (x.ID < cbnetMessage.getSrc()
                && cbnetMessage.getSrc() <= this.getMaxIdInSubtree(largeId)) {
        	this.requestRPCTo(z.ID, "setWeight", zNewWeight + 1, largeId);
        } else {
        	this.requestRPCTo(y.ID, "setWeight", yNewWeight + 1, largeId);
        }

      } else {

        // change node z
        this.requestRPCTo(z.ID, "changeRightChildTo", (CBTreeWeightLayer) x.getLeftChild(largeId), largeId);
        int max = z.ID;
        if (x.getLeftChild(largeId) != null) {
          max = x.getLeftChild(largeId).getMaxIdInSubtree(largeId);
        }
        this.requestRPCTo(z.ID, "setMaxIdInSubtree", max, largeId);

        // change node y
        this.requestRPCTo(y.ID, "changeLeftChildTo", (CBTreeWeightLayer) xInfo.getRightChild(largeId), largeId);
        int min = y.ID;
        if (xInfo.getRightChild(largeId) != null) {
          min = xInfo.getRightChild(largeId).getMinIdInSubtree(largeId);
        }
        this.requestRPCTo(y.ID, "setMinIdInSubtree", min, largeId);

        // change node x
        this.requestRPCTo(x.ID, "changeRightChildTo", y, largeId);
        this.requestRPCTo(x.ID, "setMaxIdInSubtree", yInfo.getMaxIdInSubtree(largeId), largeId);
        this.requestRPCTo(x.ID, "changeLeftChildTo", z, largeId);
        this.requestRPCTo(x.ID, "setMinIdInSubtree", zInfo.getMinIdInSubtree(largeId), largeId);
        this.requestRPCTo(x.ID, "setWeight", xNewWeight, largeId);
        
        //increment counters based on nodes position
        if (x.ID == cbnetMessage.getSrc()) {
        	this.requestRPCTo(z.ID, "setWeight", zNewWeight, largeId);
        	this.requestRPCTo(y.ID, "setWeight", yNewWeight, largeId);
        } else if (x.ID < cbnetMessage.getSrc()
                && cbnetMessage.getSrc() <= this.getMaxIdInSubtree(largeId)) {
        	this.requestRPCTo(y.ID, "setWeight", yNewWeight + 1, largeId);
        } else {
        	this.requestRPCTo(z.ID, "setWeight", zNewWeight + 1, largeId);
        }
        
      }
    } else {
      this.routing = true;

      // DATA LOG
      this.cbnetMessage.incrementRouting();
      this.cbnetMessage.incrementRouting();
      // increment counters
      this.requestRPCTo(x.ID, "setWeight", xInfo.getWeight() + 1, largeId);
      this.requestRPCTo(y.ID, "setWeight", yInfo.getWeight() + 1, largeId);
      if (z.ID == cbnetMessage.getDst()) {
    	  this.requestRPCTo(z.ID, "setWeight", zInfo.getWeight() + 1, largeId);
      }
      // forward
      this.forwardCBNetMessage(z.ID, this.cbnetMessage);
    }
  }


  // clusterCompletedTopDown

  private void rotateTopDown(int largeId, HashMap<String, CBInfo> cluster) {
    this.rotating = true;

    CBInfo xInfo = cluster.get("x");
    CBInfo yInfo = cluster.get("y");
    CBInfo zInfo = cluster.get("z");
    CBInfo wInfo = cluster.get("w");

    CBTreeWeightLayer x = (CBTreeWeightLayer) xInfo.getNode();
    CBTreeWeightLayer y = (CBTreeWeightLayer) yInfo.getNode();

    if ((x == yInfo.getLeftChild(largeId) && y == zInfo.getLeftChild(largeId))
        || (x == yInfo.getRightChild(largeId) && y == zInfo.getRightChild(largeId))) {
      this.zigZigTopDown(largeId,xInfo, yInfo, zInfo, wInfo);
    } else {
      this.zigZagTopDown(largeId, xInfo, yInfo, zInfo, wInfo);
    }
  }


  /*
             *z                   y
            / \                 /   \
           y   d     -->      *x     z
          / \                 / \   / \
         x   c               a   b c   d
        / \
       a   b
  */
  private void zigZigTopDown(int largeId, CBInfo xInfo, CBInfo yInfo, CBInfo zInfo, CBInfo wInfo) {
//     System.out.println("zig zig top down operation: " + ID);

    CBTreeWeightLayer x = (CBTreeWeightLayer) xInfo.getNode();
    CBTreeWeightLayer y = (CBTreeWeightLayer) yInfo.getNode();
    CBTreeWeightLayer z = (CBTreeWeightLayer) zInfo.getNode();
    CBTreeWeightLayer w = (CBTreeWeightLayer) wInfo.getNode();

    double deltaRank = this.zigDiffRank(largeId, yInfo, zInfo);

//    if (true) {
//     if (false) {
    if (deltaRank < this.epsilon) {
      this.rotating = true;

      // DATA LOG
      this.cbnetMessage.incrementRotations();
      this.cbnetMessage.incrementRouting();
      // forward message
      this.forwardCBNetMessage(x.ID, this.cbnetMessage);

      // set new child of node z
      if (wInfo.getLeftChild(largeId) == z) {
        this.requestRPCTo(w.ID, "changeLeftChildTo", y, largeId);
      } else {
        this.requestRPCTo(w.ID, "changeRightChildTo", y, largeId);
      }

      // calculate the new rank of nodes
      // type of operation----------------------------------------------------
      boolean leftZig = y == zInfo.getLeftChild(largeId);

      RotationLayer b = (RotationLayer) ((leftZig) ? yInfo.getRightChild(largeId) : yInfo.getLeftChild(largeId));

      long yOldWeight = yInfo.getWeight();
      long zOldWeight = zInfo.getWeight();

      long bWeight = (b != null) ? b.getWeight() : 0;

      long zNewWeight = zOldWeight - yOldWeight + bWeight;
      long yNewWeight = yOldWeight - bWeight + zNewWeight;
      // ---------------------------------------------------------------------

      // left zig operetion on node y
      if (zInfo.getLeftChild(largeId) == y) {
        // change node z
        this.requestRPCTo(z.ID, "changeLeftChildTo", (CBTreeWeightLayer) yInfo.getRightChild(largeId), largeId);
        int min = z.ID;
        if (yInfo.getRightChild(largeId) != null) {
          min = yInfo.getRightChild(largeId).getMinIdInSubtree(largeId);
        }
        this.requestRPCTo(z.ID, "setMinIdInSubtree", min, largeId);
        // increment z counter
        if (z.ID <= cbnetMessage.getSrc()
                && cbnetMessage.getSrc() <= this.getMaxIdInSubtree(largeId)) {
        	this.requestRPCTo(z.ID, "setWeight", zNewWeight + 1, largeId);
        } else {
        	this.requestRPCTo(z.ID, "setWeight", zNewWeight, largeId);
        }

        // change node y
        this.requestRPCTo(y.ID, "changeRightChildTo", z, largeId);
        this.requestRPCTo(y.ID, "setMaxIdInSubtree", zInfo.getMaxIdInSubtree(largeId), largeId);
        // increment y counter
        this.requestRPCTo(y.ID, "setWeight", yNewWeight + 1, largeId);
        if (x.ID == cbnetMessage.getDst()) {
      	  this.requestRPCTo(x.ID, "setWeight", xInfo.getWeight() + 1, largeId);
        }
        
      } else {
        // change node z
        this.requestRPCTo(z.ID, "changeRightChildTo", (CBTreeWeightLayer) yInfo.getLeftChild(largeId), largeId);
        int max = z.ID;
        if (yInfo.getLeftChild(largeId) != null) {
          max = yInfo.getLeftChild(largeId).getMaxIdInSubtree(largeId);
        }
        this.requestRPCTo(z.ID, "setMaxIdInSubtree", max, largeId);
        // increment z counter
        if (this.getMinIdInSubtree(largeId) <= cbnetMessage.getSrc()
                && cbnetMessage.getSrc() <= z.ID) {
        	this.requestRPCTo(z.ID, "setWeight", zNewWeight + 1, largeId);
        } else {
        	this.requestRPCTo(z.ID, "setWeight", zNewWeight, largeId);
        }

        // change node y
        this.requestRPCTo(y.ID, "changeLeftChildTo", z, largeId);
        this.requestRPCTo(y.ID, "setMinIdInSubtree", zInfo.getMinIdInSubtree(largeId), largeId);
        // increment y counter
        this.requestRPCTo(y.ID, "setWeight", yNewWeight + 1, largeId);
        if (x.ID == cbnetMessage.getDst()) {
      	  this.requestRPCTo(x.ID, "setWeight", xInfo.getWeight() + 1, largeId);
        }
        
      }

    }
    else {
      this.routing = true;

      // DATA LOG
      this.cbnetMessage.incrementRouting();
      this.cbnetMessage.incrementRouting();
      // increment counters
      this.requestRPCTo(z.ID, "setWeight", zInfo.getWeight() + 1, largeId);
      this.requestRPCTo(y.ID, "setWeight", yInfo.getWeight() + 1, largeId);
      if (x.ID == cbnetMessage.getDst()) {
    	  this.requestRPCTo(x.ID, "setWeight", xInfo.getWeight() + 1, largeId);
      }
      // forward
      this.forwardCBNetMessage(x.ID, this.cbnetMessage);
    }
  }


  /*
         *z                     x
         / \        -->       /   \
        y   d                y     z
       / \                  / \   / \
      a   x                a  *b *c  d
         / \
        b   c
  */
  private void zigZagTopDown(int largeId, CBInfo xInfo, CBInfo yInfo, CBInfo zInfo, CBInfo wInfo) {
//     System.out.println("zig zag top down operation: " + ID);

    CBTreeWeightLayer x = (CBTreeWeightLayer) xInfo.getNode();
    CBTreeWeightLayer y = (CBTreeWeightLayer) yInfo.getNode();
    CBTreeWeightLayer z = (CBTreeWeightLayer) zInfo.getNode();
    CBTreeWeightLayer w = (CBTreeWeightLayer) wInfo.getNode();

    double deltaRank = this.zigZagDiffRank(largeId,xInfo, yInfo, zInfo);

//    if (true) {
//     if (false) {
    if (deltaRank < this.epsilon) {
      this.rotating = true;

      // DATA LOG
      this.cbnetMessage.incrementRotations();
      this.cbnetMessage.incrementRotations();
      // forward message
      if (x.ID == this.cbnetMessage.getDst()) {
    	  this.forwardCBNetMessage(xInfo.getNode().ID, this.cbnetMessage);
  	 } else if (xInfo.getMinIdInSubtree(largeId) <= this.cbnetMessage.getDst()
          && this.cbnetMessage.getDst() < x.ID) {
        this.forwardCBNetMessage(xInfo.getLeftChild(largeId).ID, this.cbnetMessage);
      } else {
        this.forwardCBNetMessage(xInfo.getRightChild(largeId).ID, this.cbnetMessage);
      }

      // set new child of node z
      if (wInfo.getLeftChild(largeId) == z) {
        this.requestRPCTo(w.ID, "changeLeftChildTo", x, largeId);
      } else {
        this.requestRPCTo(w.ID, "changeRightChildTo", x, largeId);
      }

      // new weights------------------------------------------------------
      boolean lefZigZag = y == zInfo.getLeftChild(largeId);

      RotationLayer b = (RotationLayer) (lefZigZag ? xInfo.getLeftChild(largeId) : xInfo.getRightChild(largeId));
      RotationLayer c = (RotationLayer) (lefZigZag ? xInfo.getRightChild(largeId) : xInfo.getLeftChild(largeId));

      long xOldWeight = xInfo.getWeight();
      long yOldWeight = yInfo.getWeight();
      long zOldWeight = zInfo.getWeight();

      long bWeight = (b != null) ? b.getWeight() : 0;
      long cWeight = (c != null) ? c.getWeight() : 0;

      long yNewWeight = yOldWeight - xOldWeight + bWeight;
      long zNewWeight = zOldWeight - yOldWeight + cWeight;
      long xNewWeight = xOldWeight - bWeight - cWeight + yNewWeight + zNewWeight;
      // ---------------------------------------------------------------

      // deciding between lef or right zigzag operation
      if (y == zInfo.getLeftChild(largeId)) {
        // change node z
        this.requestRPCTo(z.ID, "changeLeftChildTo", (CBTreeWeightLayer) xInfo.getRightChild(largeId), largeId);
        int min = z.ID;
        if (xInfo.getRightChild(largeId) != null) {
          min = xInfo.getRightChild(largeId).getMinIdInSubtree(largeId);
        }
        this.requestRPCTo(z.ID, "setMinIdInSubtree", min, largeId);

        // change node y
        this.requestRPCTo(y.ID, "changeRightChildTo", (CBTreeWeightLayer) xInfo.getLeftChild(largeId), largeId);
        int max = y.ID;
        if (xInfo.getLeftChild(largeId) != null) {
          max = xInfo.getLeftChild(largeId).getMaxIdInSubtree(largeId);
        }
        this.requestRPCTo(y.ID, "setMaxIdInSubtree", max, largeId);

        // change node x
        this.requestRPCTo(x.ID, "changeLeftChildTo", y, largeId);
        this.requestRPCTo(x.ID, "setMinIdInSubtree", yInfo.getMinIdInSubtree(largeId), largeId);
        this.requestRPCTo(x.ID, "changeRightChildTo", z, largeId);
        this.requestRPCTo(x.ID, "setMaxIdInSubtree", zInfo.getMaxIdInSubtree(largeId), largeId);
        
        // increment counters
        if (z.ID <= cbnetMessage.getSrc()
                && cbnetMessage.getSrc() <= this.getMaxIdInSubtree(largeId)) {
        	if (x.ID == this.cbnetMessage.getDst()) {
        		this.requestRPCTo(x.ID, "setWeight", xNewWeight + 1, largeId);
        		this.requestRPCTo(y.ID, "setWeight", yNewWeight, largeId);
        		this.requestRPCTo(z.ID, "setWeight", zNewWeight + 1, largeId);
        	} else if (xInfo.getMinIdInSubtree(largeId) <= this.cbnetMessage.getDst()
        	          && this.cbnetMessage.getDst() < x.ID) {
        		//y+1, x+1, z+1
        		this.requestRPCTo(x.ID, "setWeight", xNewWeight + 1, largeId);
        		this.requestRPCTo(y.ID, "setWeight", yNewWeight + 1, largeId);
        		this.requestRPCTo(z.ID, "setWeight", zNewWeight + 1, largeId);
        	} else {
        		//z+1
        		this.requestRPCTo(x.ID, "setWeight", xNewWeight, largeId);
        		this.requestRPCTo(y.ID, "setWeight", yNewWeight, largeId);
        		this.requestRPCTo(z.ID, "setWeight", zNewWeight + 1, largeId);
        	}
        } else {
        	if (x.ID == this.cbnetMessage.getDst()) {
        		this.requestRPCTo(x.ID, "setWeight", xNewWeight + 1, largeId);
        		this.requestRPCTo(y.ID, "setWeight", yNewWeight, largeId);
        		this.requestRPCTo(z.ID, "setWeight", zNewWeight, largeId);
        	} else if (xInfo.getMinIdInSubtree(largeId) <= this.cbnetMessage.getDst()
      	          && this.cbnetMessage.getDst() < x.ID) {
        		//y+1, x+1
        		this.requestRPCTo(x.ID, "setWeight", xNewWeight + 1, largeId);
        		this.requestRPCTo(y.ID, "setWeight", yNewWeight + 1, largeId);
        		this.requestRPCTo(z.ID, "setWeight", zNewWeight, largeId);
        	} else {
        		//z+1, x+1
        		this.requestRPCTo(x.ID, "setWeight", xNewWeight + 1, largeId);
        		this.requestRPCTo(y.ID, "setWeight", yNewWeight, largeId);
        		this.requestRPCTo(z.ID, "setWeight", zNewWeight + 1, largeId);
        	}
        }

      } else {

        // change node z
        this.requestRPCTo(z.ID, "changeRightChildTo", (CBTreeWeightLayer) x.getLeftChild(largeId),largeId);
        int max = z.ID;
        if (x.getLeftChild(largeId) != null) {
          max = x.getLeftChild(largeId).getMaxIdInSubtree(largeId);
        }
        this.requestRPCTo(z.ID, "setMaxIdInSubtree", max, largeId);
        this.requestRPCTo(z.ID, "setWeight", zNewWeight, largeId);

        // change node y
        this.requestRPCTo(y.ID, "changeLeftChildTo", (CBTreeWeightLayer) xInfo.getRightChild(largeId), largeId);
        int min = y.ID;
        if (xInfo.getRightChild(largeId) != null) {
          min = xInfo.getRightChild(largeId).getMinIdInSubtree(largeId);
        }
        this.requestRPCTo(y.ID, "setMinIdInSubtree", min, largeId);
        this.requestRPCTo(y.ID, "setWeight", yNewWeight, largeId);

        // change node x
        this.requestRPCTo(x.ID, "changeRightChildTo", y, largeId);
        this.requestRPCTo(x.ID, "setMaxIdInSubtree", yInfo.getMaxIdInSubtree(largeId), largeId);
        this.requestRPCTo(x.ID, "changeLeftChildTo", z, largeId);
        this.requestRPCTo(x.ID, "setMinIdInSubtree", zInfo.getMinIdInSubtree(largeId), largeId);
        this.requestRPCTo(x.ID, "setWeight", xNewWeight, largeId);
        
        // increment counters
        if (this.getMaxIdInSubtree(largeId) <= cbnetMessage.getSrc()
                && cbnetMessage.getSrc() <= z.ID) {
        	if (x.ID == this.cbnetMessage.getDst()) {
        		this.requestRPCTo(x.ID, "setWeight", xNewWeight + 1, largeId);
        		this.requestRPCTo(y.ID, "setWeight", yNewWeight, largeId);
        		this.requestRPCTo(z.ID, "setWeight", zNewWeight + 1, largeId);
        	} else if (xInfo.getMinIdInSubtree(largeId) <= this.cbnetMessage.getDst()
        	          && this.cbnetMessage.getDst() < x.ID) {
        		//z+1
        		this.requestRPCTo(x.ID, "setWeight", xNewWeight, largeId);
        		this.requestRPCTo(y.ID, "setWeight", yNewWeight, largeId);
        		this.requestRPCTo(z.ID, "setWeight", zNewWeight + 1, largeId);
        	} else {
        		//y+1, x+1, z+1
        		this.requestRPCTo(x.ID, "setWeight", xNewWeight + 1, largeId);
        		this.requestRPCTo(y.ID, "setWeight", yNewWeight + 1, largeId);
        		this.requestRPCTo(z.ID, "setWeight", zNewWeight + 1, largeId);
        	}
        } else {
        	if (x.ID == this.cbnetMessage.getDst()) { //TODO
        		this.requestRPCTo(x.ID, "setWeight", xNewWeight + 1, largeId);
        		this.requestRPCTo(y.ID, "setWeight", yNewWeight, largeId);
        		this.requestRPCTo(z.ID, "setWeight", zNewWeight, largeId);
        	} else if (xInfo.getMinIdInSubtree(largeId) <= this.cbnetMessage.getDst()
      	          && this.cbnetMessage.getDst() < x.ID) {
        		//x+1, z+1
        		this.requestRPCTo(x.ID, "setWeight", xNewWeight + 1, largeId);
        		this.requestRPCTo(y.ID, "setWeight", yNewWeight, largeId);
        		this.requestRPCTo(z.ID, "setWeight", zNewWeight + 1, largeId);
        	} else {
        		//y+1, x+1
        		this.requestRPCTo(x.ID, "setWeight", xNewWeight + 1, largeId);
        		this.requestRPCTo(y.ID, "setWeight", yNewWeight + 1, largeId);
        		this.requestRPCTo(z.ID, "setWeight", zNewWeight, largeId);
        	}
        }
      }
    } else {
      this.routing = true;

      // DATA LOG
      this.cbnetMessage.incrementRouting();
      this.cbnetMessage.incrementRouting();
      // increment counters
      this.requestRPCTo(z.ID, "setWeight", zInfo.getWeight() + 1, largeId);
      this.requestRPCTo(y.ID, "setWeight", yInfo.getWeight() + 1, largeId);
      if (x.ID == cbnetMessage.getDst()) {
    	  this.requestRPCTo(x.ID, "setWeight", xInfo.getWeight() + 1, largeId);
      }
      // forward
      this.forwardCBNetMessage(x.ID, this.cbnetMessage);
    }
  }

  private double log2(long value) {
    return Math.log(value) / Math.log(2);
  }

  /*
            y                   x
          /   \               /   \
         x     c     -->     a     y
        / \                       / \
       a   b                     b   c
  */
  public double zigDiffRank(int largeId, CBInfo xInfo, CBInfo yInfo) {
    RotationLayer x = (RotationLayer) xInfo.getNode();
    // RotationLayer y = (RotationLayer) yInfo.getNode();

    // type of operation
    boolean leftZig = x == yInfo.getLeftChild(largeId);

    RotationLayer b = (RotationLayer) ((leftZig) ? xInfo.getRightChild(largeId) : xInfo.getLeftChild(largeId));

    long xOldWeight = xInfo.getWeight();
    long yOldWeight = yInfo.getWeight();

    long bWeight = (b != null) ? b.getWeight() : 0;

    long yNewWeight = yOldWeight - xOldWeight + bWeight;
    long xNewWeight = xOldWeight - bWeight + yNewWeight;

    double xOldRank = (xOldWeight == 0) ? 0 : log2(xOldWeight);
    double yOldRank = (yOldWeight == 0) ? 0 : log2(yOldWeight);
    double xNewRank = (xNewWeight == 0) ? 0 : log2(xNewWeight);
    double yNewRank = (yNewWeight == 0) ? 0 : log2(yNewWeight);

    double deltaRank = yNewRank + xNewRank - yOldRank - xOldRank;

    return deltaRank;
  }

  /*
         z					   *x
        / \                   /   \
       y   d                 y     z
          / \		 -->    / \   / \
         a  *x             a   b c   d
            / \
           b   c
  */
  private double zigZagDiffRank(int largeId,CBInfo xInfo, CBInfo yInfo, CBInfo zInfo) {
    // RotationLayer x = (RotationLayer) xInfo.getNode();
    RotationLayer y = (RotationLayer) yInfo.getNode();
    // RotationLayer z = (RotationLayer) zInfo.getNode();

    boolean lefZigZag = y == zInfo.getLeftChild(largeId);

    RotationLayer b = (RotationLayer) (lefZigZag ? xInfo.getLeftChild(largeId) : xInfo.getRightChild(largeId));
    RotationLayer c = (RotationLayer) (lefZigZag ? xInfo.getRightChild(largeId) : xInfo.getLeftChild(largeId));

    long xOldWeight = xInfo.getWeight();
    long yOldWeight = yInfo.getWeight();
    long zOldWeight = zInfo.getWeight();

    long bWeight = (b != null) ? b.getWeight() : 0;
    long cWeight = (c != null) ? c.getWeight() : 0;

    long yNewWeight = yOldWeight - xOldWeight + bWeight;
    long zNewWeight = zOldWeight - yOldWeight + cWeight;
    long xNewWeight = xOldWeight - bWeight - cWeight + yNewWeight + zNewWeight;

    double xOldRank = (xOldWeight == 0) ? 0 : log2(xOldWeight);
    double yOldRank = (yOldWeight == 0) ? 0 : log2(yOldWeight);
    double zOldRank = (zOldWeight == 0) ? 0 : log2(zOldWeight);
    double xNewRank = (xNewWeight == 0) ? 0 : log2(xNewWeight);
    double yNewRank = (yNewWeight == 0) ? 0 : log2(yNewWeight);
    double zNewRank = (zNewWeight == 0) ? 0 : log2(zNewWeight);

    double deltaRank = xNewRank + yNewRank + zNewRank - xOldRank - yOldRank - zOldRank;

    return deltaRank;
  }

  @Override
  public void timeslot10() {

    this.executeAllRPC();
    this.clearRPCQueue();
    this.cbnetMessage = null;

    if (this.rotating) {

      this.rotationCompleted();
      this.rotating = false;

    } else if (this.routing) {

      this.forwardCompleted();
      this.routing = false;

    }
    
    super.timeslot10();
  }

  public void rotationCompleted() {
	  
  }

  public void forwardCompleted() {
	  
  }

}
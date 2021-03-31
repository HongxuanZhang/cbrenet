package projects.cbrenet;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import projects.cbrenet.nodes.nodeImplementations.AuxiliaryNode;
import projects.cbrenet.nodes.nodeImplementations.CBReNetApp;
import projects.cbrenet.nodes.nodeImplementations.CbRenetBinarySearchTreeLayer;
import projects.cbrenet.nodes.nodeImplementations.SDNApp;
import projects.cbrenet.nodes.timers.TriggerNodeOperation;
import projects.defaultProject.BalancedTreeTopology;
import projects.defaultProject.DataCollection;
import projects.defaultProject.RequestQueue;
import projects.defaultProject.TreeConstructor;
import sinalgo.configuration.Configuration;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.tools.Tools;
import sinalgo.tools.Tuple;

public class CustomGlobal extends AbstractCustomGlobal {

  // final condition
  public long MAX_REQ; // MAX request number, may be the request sequence

  // simulation
  public int numNodes = 30;
  public ArrayList<CBReNetApp> tree = null;
  public ArrayList<CBReNetApp> allNodes = null;
  public CBReNetApp controller = null;
  public TreeConstructor treeTopology = null;
  public RequestQueue requestQueue;

  public int parac = 20;

  // control execution
  public static boolean isSequencial = true;
  public static boolean mustGenerateSplay = true;  //这个有什么用呢?

  public Random random = Tools.getRandomNumberGenerator();
  public double lambda = 0.05;

  // LOG
  DataCollection data = DataCollection.getInstance();

  @Override
  public boolean hasTerminated() {
    if (this.data.getCompletedRequests() >= MAX_REQ) {
      CBReNetApp node = (CBReNetApp) Tools.getNodeByID(1);
      this.data.addTotalTime(node.getCurrentRound());
      this.data.printRotationData();
      this.data.printRoutingData();
      return true;
    }
    return false;
  }

  @Override
  public void preRun() {

    String input = "";
    String output = "";

    try {

      if (Configuration.hasParameter("input")) {
        input = Configuration.getStringParameter("input");
      }

      if (Configuration.hasParameter("output")) {
        output = Configuration.getStringParameter("output");
      }

      if (Configuration.hasParameter("mu")) {
        double mu = (double) Configuration.getIntegerParameter("mu");
        lambda = (double) (1 / mu);
      }

      if (Configuration.hasParameter("parac")) {
        parac = Configuration.getIntegerParameter("parac");
      }

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Missing configuration parameters");
    }

    // Set Log Path
    this.data.setPath(output);

    /*
     * read input data and configure the simulation
     */

    this.requestQueue = new RequestQueue(input);
    this.numNodes = this.requestQueue.getNumberOfNodes();
    MAX_REQ = this.requestQueue.getNumberOfRequests();

    /*
     * create the nodes and constructs the tree topology
     */
    this.tree = new ArrayList<CBReNetApp>();
    this.allNodes = new ArrayList<>();

    SDNApp sdn = new SDNApp(10);
    sdn.finishInitializationWithDefaultModels(true);


    AuxiliaryNode auxiliaryNode = new AuxiliaryNode();
    auxiliaryNode.finishInitializationWithDefaultModels(true);
    sdn.setAuxiliaryNodeId(auxiliaryNode.ID);


    List<Node> nodes = new ArrayList<>();


    for (int i = 0; i < numNodes; i++) {
        CBReNetApp n = new CBReNetApp();
        n.finishInitializationWithDefaultModels(true);
        this.allNodes.add(n);
        n.setSDNId(sdn.ID);
        sdn.addNodeId(n.ID);
        nodes.add(n);
    }


    // 整个完全图进来吧

    // node <-> an
    for(Node n : nodes){
        n.addBidirectionalConnectionTo(auxiliaryNode);
    }

    // node <-> node
    int len = nodes.size();
    for(int i = 0; i < len; i++){
        for(int j = i + 1; j < len; j++){
            nodes.get(i).addBidirectionalConnectionTo(nodes.get(j));
        }
    }


  }

  @Override
  public void preRound() {

      if (mustGenerateSplay && this.requestQueue.hasNextRequest()) {
        // 使用点阴的，，我们也这么做吧。。。 todo 遏制新Request产生。可以帮助我们跑出实验数据
        mustGenerateSplay = false;

        double u = random.nextDouble();
        double x = Math.log(1 - u) / (-lambda);
        x = (int) x;
        if (x <= 0) {
          x = 1;
        }

        Tuple<Integer, Integer> r = this.requestQueue.getNextRequest();
        TriggerNodeOperation ted = new TriggerNodeOperation(r.first, r.second);   //此处设置了一条信息，是request的source 和 destination间的
        ted.startGlobalTimer(x);  // 设置一个全局触发的timer, 在x后request r会从r.first结点发出

      }
  }

}
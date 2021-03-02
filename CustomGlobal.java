package projects.cbrenet;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;

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


    for (int i = 0; i < numNodes; i++) {
      CBReNetApp n = new CBReNetApp();
      n.finishInitializationWithDefaultModels(true);
      this.allNodes.add(n);
      n.setSDNId(sdn.ID);
      sdn.addNodeId(n.ID);
    }

    this.controller = new CBReNetApp() {
      public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        String text = "SDN Node";
        super.drawNodeAsDiskWithText(g, pt, highlight, text, 10, Color.BLUE);
      }
    };
    this.controller.finishInitializationWithDefaultModels(true);

//    this.treeTopology = new BalancedTreeTopology(controlNode, this.tree);
    this.treeTopology = new BalancedTreeTopology(null, null);
    this.treeTopology.buildTree();

//    this.treeTopology.linearTree();
    this.treeTopology.setPositions();

  }

  @Override
  public void preRound() {
    this.treeTopology.setPositions();

    if (mustGenerateSplay && this.requestQueue.hasNextRequest()) {
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
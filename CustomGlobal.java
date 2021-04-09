package projects.cbrenet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import projects.cbrenet.nodes.nodeImplementations.AuxiliaryNode;
import projects.cbrenet.nodes.nodeImplementations.CBReNetApp;
import projects.cbrenet.nodes.nodeImplementations.SDNApp;
import projects.cbrenet.nodes.nodeImplementations.nodeHelper.ClusterHelper;
import projects.cbrenet.nodes.nodeImplementations.nodeHelper.SimpleClusterHelper;
import projects.cbrenet.nodes.timers.TriggerNodeOperation;
import projects.cbrenet.DataCollection;
import projects.defaultProject.RequestQueue;
import projects.defaultProject.TreeConstructor;
import sinalgo.configuration.Configuration;
import sinalgo.nodes.Node;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.tools.Tools;
import sinalgo.tools.Tuple;

public class CustomGlobal extends AbstractCustomGlobal {

  // final condition
  public long MAX_REQ; // MAX request number, may be the request sequence

  // simulation
  public int numNodes = 30;
  public CBReNetApp controller = null;
  public TreeConstructor treeTopology = null;
  public RequestQueue requestQueue;

  public int parac = 20;

  // control execution

  public static int deleteNum = 0;
  public static int adjustNum = 0;

  public static boolean noWaitForAdjust = true;
  public static boolean mustGenerateSplay = true;



  public Random random = Tools.getRandomNumberGenerator();
  public double lambda = 0.05;

  public double epsilon = -1.5;
  public boolean adjust = false;

  private SDNApp sdn;

  // LOG
  DataCollection data = DataCollection.getInstance();

  @Override
  public boolean hasTerminated() {
    if (this.data.getCompletedRequests() >= MAX_REQ) {
      this.data.printRotationData();
      this.data.printRoutingData();
      this.data.outputHopNum();
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

      if(Configuration.hasParameter("epsilon")){
        epsilon = Configuration.getDoubleParameter("epsilon");
        System.out.println("The configuration of epsilon is "+ epsilon);
      }

      if(Configuration.hasParameter("epsilon")){
        adjust = Configuration.getBooleanParameter("adjust");
        System.out.println("The configuration of adjust is "+ adjust);
      }

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Missing configuration parameters");
    }

    System.out.println(input);
    System.out.println(output);
    System.out.println(lambda);
    System.out.println(parac);

    String dataName = input.substring(input.lastIndexOf("\\")+1, input.lastIndexOf("."));
    System.out.println(dataName);

    String filename = "parac_"+((Integer)parac).toString() + "_epsilon" + ((Double)epsilon).toString() + "_adjust_" +
            ((Boolean)adjust).toString() + "_" + dataName;

    // Set Log Path
    this.data.setPath(output, filename);

    /*
     * read input data and configure the simulation
     */

    this.requestQueue = new RequestQueue(input);
    this.numNodes = this.requestQueue.getNumberOfNodes();
    MAX_REQ = this.requestQueue.getNumberOfRequests();

    /*
     * create the nodes and constructs the tree topology
     */




    List<CBReNetApp> nodes = new ArrayList<>();


    for (int i = 0; i < numNodes; i++) {
        CBReNetApp n = new CBReNetApp();
        n.finishInitializationWithDefaultModels(true);
        nodes.add(n);
        System.out.println("Create communicate node " + n.ID);
    }

    SDNApp sdn = new SDNApp(parac);
    sdn.finishInitializationWithDefaultModels(true);
    sdn.setConstCAndThreshold(parac);

    this.sdn = sdn;

    AuxiliaryNode auxiliaryNode = new AuxiliaryNode();
    auxiliaryNode.finishInitializationWithDefaultModels(true);
    sdn.setAuxiliaryNodeId(auxiliaryNode.ID);

    for(CBReNetApp n: nodes){
      n.setSDNId(sdn.ID);
      sdn.addCommunicateNodeId(n.ID);
    }



    // 整个完全图进来吧

    // node <-> an
    for(Node n : nodes){
        n.addConnectionTo(auxiliaryNode);
    }

    // node <-> node
    int len = nodes.size();
    for(int i = 0; i < len; i++){
        for(int j = i + 1; j < len; j++){
            nodes.get(i).addConnectionTo(nodes.get(j));
        }
    }

    // cluster and rotation

//    ClusterHelper clusterHelper = ClusterHelper.getInstance();
//    clusterHelper.setEpsilon(epsilon);
//    clusterHelper.setAdjustFlag(adjust);

    SimpleClusterHelper simpleClusterHelper = SimpleClusterHelper.getInstance();
    simpleClusterHelper.setEpsilon(epsilon);
    simpleClusterHelper.setAdjustFlag(adjust);

  }

  private static int requestNo = 0;

  @Override
  public void preRound() {

      if (mustGenerateSplay && noWaitForAdjust && deleteNum == 0  && adjustNum == 0 && this.requestQueue.hasNextRequest()) {

        mustGenerateSplay = false;

        if(this.sdn != null){
          this.sdn.outputGlobalSituationInSDN();
        }
        double u = random.nextDouble();
        double x = Math.log(1 - u) / (-lambda);
        x = (int) x;
        if (x <= 0) {
          x = 1;
        }

        System.out.println("------------------------ A NEW REQUEST : " + ++requestNo + " --------------------------");

        Tuple<Integer, Integer> r = this.requestQueue.getNextRequest();
        TriggerNodeOperation ted = new TriggerNodeOperation(r.first, r.second);   //此处设置了一条信息，是request的source 和 destination间的

        if(r.first == 219 && r.second == 48){
          int jdasd =3;
        }

        ted.startGlobalTimer(x);  // 设置一个全局触发的timer, 在x后request r会从r.first结点发出

      }
  }

}
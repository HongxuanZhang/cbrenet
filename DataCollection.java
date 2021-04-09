package projects.cbrenet;

import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.DataSeries;

import java.util.ArrayList;

public class DataCollection {

  private static DataCollection single_instance = null;

  private DataSeries rotationData = new DataSeries();
  private DataSeries routingData = new DataSeries();

  private ArrayList<Long> hopNumForEveryRequest = new ArrayList<>();


  private long activeSplays = 0;
  private long activeClusters = 0;

  private long completedRequests = 0;

  // LOGS

  private Logging routing_hop_log; // for CB ReNet

  private DataCollection() {

  }

  public void setPath(String path, String filename) {
    routing_hop_log = Logging.getLogger(path + "/" + filename + ".txt");
  }

  public static DataCollection getInstance() {
    if (single_instance == null) {
      single_instance = new DataCollection();
    }

    return single_instance;
  }

  public void initCollection() {
    this.activeSplays = 0;
    this.activeClusters = 0;
  }

  public void addRotations(long num) {
    this.rotationData.addSample(num);
  }

  public void addRouting(long num) {
    this.routingData.addSample(num);
    this.hopNumForEveryRequest.add(num);
  }

  public void resetCollection() {
    this.rotationData.reset();
    this.routingData.reset();
  }

  public void incrementActiveSplays() {
    this.activeSplays++;
  }

  public void decrementActiveSplays() {
    this.activeSplays--;
  }

  public long getNumbugerOfActiveSplays() {
    return activeSplays;
  }

  public void incrementActiveClusters() {
    this.activeClusters++;
  }

  public void decrementActiveClusters() {
    this.activeClusters--;
  }

  public void resetActiveClusters() {
    this.activeClusters = 0;
  }

  public long getActiveClusters() {
    return activeClusters;
  }

  public void incrementCompletedRequests() {
    this.completedRequests++;
  }

  public long getCompletedRequests() {
    return completedRequests;
  }


  public void outputHopNum(){
    for(Long hop:this.hopNumForEveryRequest){
      this.routing_hop_log.logln(hop.toString());
    }

    Double route_link = this.routingData.getSum();
    this.routing_hop_log.log("Route Total hops:");
    this.routing_hop_log.logln(route_link.toString());

    Double route_involved_link = this.rotationData.getSum();
    this.routing_hop_log.log("Adjust total Links:");
    this.routing_hop_log.logln(route_involved_link.toString());

  }


  public DataSeries getRotationDataSeries() {
    return this.rotationData;
  }

  public DataSeries getRoutingDataSeries() {
    return this.routingData;
  }

  public void printRotationData() {
    System.out.println("Rotations:");
    System.out.println("Number of request: " + this.rotationData.getNumberOfSamples());
    System.out.println("Mean: " + this.rotationData.getMean());
    System.out.println("Standard Deviation: " + this.rotationData.getStandardDeviation());
    System.out.println("Min: " + this.rotationData.getMinimum());
    System.out.println("Max: " + this.rotationData.getMaximum());

  }

  public void printRoutingData() {
    System.out.println("Routing:");
    System.out.println("Number of request " + this.routingData.getNumberOfSamples());
    System.out.println("Mean: " + this.routingData.getMean());
    System.out.println("Standard Deviation: " + this.routingData.getStandardDeviation());
    System.out.println("Min: " + this.routingData.getMinimum());
    System.out.println("Max: " + this.routingData.getMaximum());

  }
}
package projects.cbrenet.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import projects.cbrenet.CustomGlobal;
import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.defaultProject.DataCollection;
import sinalgo.gui.transformation.PositionTransformation;

/**
 * CBNetApp
 */
public class CBReNetApp extends CBNetNode {

  private DataCollection data = DataCollection.getInstance();

  @Override
  public void newMessageSent() {
    super.newMessageSent();

    this.data.incrementActiveSplays();
  }

  @Override
  public void init(){
      super.init();
  }

  public CBReNetApp(){
      super();
      this.init();
  }



  @Override
  public void communicationCompleted(CbRenetMessage msg) {
      super.communicationCompleted(msg);
      CustomGlobal.mustGenerateSplay = true;
      this.data.addRouting(msg.getRouting());
      this.data.addRoundsPerSplay(msg.finalTime - msg.initialTime);
      this.data.incrementCompletedRequests();
  }

  @Override
  public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
    // String text = ID + " l:" + this.minIdInSubtree + " r:" + this.maxIdInSubtree;
    String text = "" + ID;// + ": " + this.getWeight();

    // draw the node as a circle with the text inside
    super.drawNodeAsDiskWithText(g, pt, highlight, text, 12, Color.YELLOW);
  }
}
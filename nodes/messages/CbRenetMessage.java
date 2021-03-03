package projects.cbrenet.nodes.messages;

import projects.defaultProject.nodes.messages.NetworkMessage;
import sinalgo.nodes.messages.Message;

/**
* 初步认定应该用于Rotation*/

public class CbRenetMessage extends NetworkMessage implements Comparable<CbRenetMessage>{

    final private boolean largeIdFlag; // Refer to whether the largeId should be used
    final private int largeId;  // if the source is not big, then the largeId should be -1;

    private boolean upForward = false; // false by default, true only when the small send to large
    // in which big tree the message transfer.

    private int src;
    private int dst;
    private double priority;

    // collect data variable
    private long rotations;
    private long routing;

    public long initialTime;
    public long finalTime;


    private void init(){
        this.upForward = false;
        // collect
        this.rotations = 0;
        this.routing = 0;
    }

    public CbRenetMessage(int src, int dst, double priority) {
        super(src, dst);
        this.largeIdFlag = false;
        this.largeId = -1;
        this.priority = priority;

        this.init();
    }

    public CbRenetMessage(int src, int dst, boolean largeIdFlag, int largeId, double priority){
        super(src, dst);
        this.largeIdFlag = largeIdFlag;
        this.largeId = largeId;
        this.priority = priority;

        this.init();

    }

    public CbRenetMessage(int src, int dst, boolean largeIdFlag, int largeId, double priority,
                          boolean upForward){
        super(src, dst);
        this.largeIdFlag = largeIdFlag;
        this.largeId = largeId;
        this.priority = priority;

        this.init();
        this.upForward = upForward;
    }

    public boolean isLargeIdFlag() {
        return largeIdFlag;
    }

    public int getLargeId(){
        if(largeIdFlag){
            return largeId;
        }
        else{
            return -1;
        }
    }


    /*
    * Only prepare for helper node*/

    // when helperFlag is true, the dst should be helper node's ID, and
    // the trueDst would be another large node's ID.
    private boolean helperFlag;
    private int trueDst;


    public void setHelpMode(int trueDst)
    {
        // called when a node have to send the msg in help mode
        this.trueDst = trueDst;
        this.helperFlag = true;
    }

    public void changeDstInHelperNode(){
        // called when the msg achieved in the helper node
        this.dst = this.trueDst;
        this.helperFlag = false;
    }



    /**
     * @return the src
     */
    public int getSrc() {
        return src;
    }

    /**
     * @return the dst
     */
    public int getDst() {
        return dst;
    }

    /**
     * @return the priority
     */
    public double getPriority() {
        return priority;
    }

    /**
     * @param src the src to set
     */
    public void setSrc(int src) {
        this.src = src;
    }

    /**
     * @param dst the dst to set
     */
    public void setDst(int dst) {
        this.dst = dst;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(double priority) {
        this.priority = priority;
    }

    @Override
    public Message clone() {
        return this;
    }

    /**
     * @return the rotations
     */
    public long getRotations() {
        return rotations;
    }

    /**
     * @return the routing
     */
    public long getRouting() {
        return routing;
    }

    public void incrementRotations() {
        this.rotations++;
    }

    public void incrementRouting() {
        this.routing++;
    }

    public boolean isUpForward() {
        return upForward;
    }

    @Override
    public int compareTo(CbRenetMessage o) {
        int value = Double.compare(this.priority, o.priority);
        if (value == 0) { // In case tie, compare the id of the source node
            return this.dst - o.dst;
        } else {
            return value;
        }
    }

}

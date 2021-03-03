package projects.cbrenet.nodes.messages;


import projects.defaultProject.nodes.messages.NetworkMessage;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

/**
 * RoutingMessage
 */
public class RoutingMessage extends NetworkMessage {


    private int largeId = -1;
    private boolean upForward = false;


    // these two properties are used to prevent the wrong route as the consequence of the semi-splay
    private int nextHop = -1;
    private boolean nextHopFlag = false;



    public void setNextHop(int id){
        if(id<0){
            Tools.fatalError("The next hop id of routing message can not be less than 0!");
        }
        this.nextHop = id;
        this.nextHopFlag = true;
    }

    public boolean getNextHopFlag(){
        return this.nextHopFlag;
    }

    public int getNextHop(){
        return this.nextHop;
    }

    public void resetNextHop(){
        /**
         *@description This method should be used when the node finish handling the hop.
         *@parameters  []
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/20
         */
        this.nextHopFlag = false;
        this.nextHop = -1;
    }

    private Message payload;


    // 大概率用不到
    public RoutingMessage(int source, int destination, Message payload) {
        super(source, destination);
        this.payload = payload;
    }

    public RoutingMessage(int source, int destination, Message payload, int largeId, boolean upForward) {
        super(source, destination);
        this.payload = payload;
        this.largeId = largeId;
        this.upForward = upForward;
    }

    public int getLargeId() {
        return largeId;
    }

    public boolean isNextHopFlag() {
        return nextHopFlag;
    }

    public Message getPayload() {
        return payload;
    }

    public Message getPayLoad() {
        return this.payload;
    }

    public void setPayLoad(Message payload) {
        this.payload = payload;
    }

    @Override
    public Message clone() {
        return this;
    }

}
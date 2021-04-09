package projects.cbrenet.nodes.messages;


import projects.defaultProject.nodes.messages.NetworkMessage;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

/**
 * RoutingMessage
 */
public class RoutingMessage extends NetworkMessage {

    private int nextHop; // Auxiliary Node need to know which node is helped.
    // nextHop field must set beforeForward!

    public int getNextHop() {
        return nextHop;
    }

    public void setNextHop(int nextHop) {
        this.nextHop = nextHop;
    }

    private int largeId = -1;
    private boolean upForward = false;


    // these two properties are used to prevent the wrong route as the consequence of the semi-splay
    private int specialHop = -1;
    private boolean specialHopFlag = false;
    // Think about what would happen if multiple rotations occurs....


    public void setSpecialHop(int id){
        /*
         *@description  This id should be ego-tree id
         *@parameters  [id]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/4/1
         */
        if(id<0){
            Tools.fatalError("The next hop id of routing message can not be less than 0!");
        }
        this.specialHop = id;
        this.specialHopFlag = true;
    }

    public boolean getSpecialHopFlag(){
        return this.specialHopFlag;
    }

    public int getSpecialHop(){
        return this.specialHop;
    }

    public void resetSpecialHop(){
        /**
         *@description This method should be used when the node finish handling the hop.
         *@parameters  []
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/20
         */
        this.specialHopFlag = false;
        this.specialHop = -1;
    }

    private Message payload;


    public RoutingMessage(int source, int destination, Message payload, int largeId, boolean upForward) {
        super(source, destination);
        this.payload = payload;
        this.largeId = largeId;
        this.upForward = upForward;
    }

    public void beforeForward(int nextHop){
        /**
         *@description  The auxiliary node need to know the current helped id
         *@parameters  [nextHop]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/14
         */
        this.setNextHop(nextHop);
    }

    public int getLargeId() {
        return largeId;
    }

    public boolean isSpecialHopFlag() {
        return specialHopFlag;
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

    public boolean isUpForward() {
        return upForward;
    }

    @Override
    public Message clone() {
        return this;
    }

    @Override
    public String toString() {
        return "RoutingMessage{" +
                "nextHop=" + nextHop +
                ", largeId=" + largeId +
                ", upForward=" + upForward +
                ", specialHop=" + specialHop +
                ", specialHopFlag=" + specialHopFlag +
                ", payload=" + payload.toString() +
                '}';
    }
}
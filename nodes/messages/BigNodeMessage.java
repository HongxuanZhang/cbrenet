package projects.cbrenet.nodes.messages;

import projects.defaultProject.nodes.messages.NetworkMessage;

/**
 * todo 似乎一个结点只能知道自己是不是大结点而并不能知道对方是否是大结点 */

public class BigNodeMessage extends NetworkMessage {

    //final private boolean srcNodeBig; // when the node send message, the node only know whether itself is big.
    final private int helper; // this is used to send message to the small node as helper


    public BigNodeMessage(int src, int dst) {
        super(src, dst);
        this.helper = -1;
        //  this.srcNodeBig = true;
    }

    public BigNodeMessage(int src, int dst, int helper){
        super(src, dst);
        //this.srcNodeBig = true;
        this.helper = helper;
    }
}

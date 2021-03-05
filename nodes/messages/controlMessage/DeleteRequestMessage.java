package projects.cbrenet.nodes.messages.controlMessage;

import sinalgo.nodes.messages.Message;

public class DeleteRequestMessage extends Message {

    private final int src;
    private final int dst; // This is also largeId

    private boolean ego_tree;

    private final int wantToDeleteId;

    public DeleteRequestMessage(int src, int dst, boolean ego_tree, int wantToDeleteId){
        this.src = src;
        this.dst = dst;
        this.ego_tree = ego_tree;

        this.wantToDeleteId = wantToDeleteId;
    }

    public int getSrc() {
        return src;
    }

    public int getDst() {
        return dst;
    }

    public boolean isEgo_tree() {
        return ego_tree;
    }

    public int getWantToDeleteId() {
        return wantToDeleteId;
    }

    @Override
    public Message clone() {
        return null;
    }
}

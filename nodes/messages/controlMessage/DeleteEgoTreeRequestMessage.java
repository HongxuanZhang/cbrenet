package projects.cbrenet.nodes.messages.controlMessage;

import sinalgo.nodes.messages.Message;

import java.util.HashSet;
import java.util.List;

public class DeleteEgoTreeRequestMessage extends Message {

    private final int largeId;

    private final HashSet<Integer> egoTreeIds;

    public DeleteEgoTreeRequestMessage(int largeId, HashSet<Integer> egoTreeIds){
        this.largeId = largeId;
        this.egoTreeIds = egoTreeIds;
    }

    public int getLargeId() {
        return largeId;
    }

    public HashSet<Integer> getEgoTreeIds() {
        return egoTreeIds;
    }

    @Override
    public Message clone() {
        return null;
    }
}

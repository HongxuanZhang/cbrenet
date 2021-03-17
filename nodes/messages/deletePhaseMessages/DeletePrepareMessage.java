package projects.cbrenet.nodes.messages.deletePhaseMessages;

public class DeletePrepareMessage extends DeleteBaseMessage implements Comparable<DeletePrepareMessage>{

    final private double t;

    public DeletePrepareMessage(int largeId, int deleteTarget, double t){
        super(largeId, deleteTarget);
        this.t = t;
    }

    @Override
    public int compareTo(DeletePrepareMessage o) {
        if(o == null){
            return 1;
        }
        double result = this.t - o.t;
        if(result > 0) {
            return 1;
        }
        else if(result < 0){
            return -1;
        }
        else{
            return 0;
        }
    }

    public double getT() {
        return t;
    }
}

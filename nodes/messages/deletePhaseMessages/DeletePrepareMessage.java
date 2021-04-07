package projects.cbrenet.nodes.messages.deletePhaseMessages;

import java.util.Objects;

public class DeletePrepareMessage extends DeleteBaseMessage implements Comparable<DeletePrepareMessage>{

    final private double t;

    private Relation relation = Relation.itself;

    private int sendTargetEgoTreeId = -1;

    public DeletePrepareMessage(int largeId, int deleteTarget, double t){
        super(largeId, deleteTarget);
        this.t = t;
    }

    public DeletePrepareMessage(DeletePrepareMessage deletePrepareMessage){
        /*
         *@description Do not need to copy relation field
         *@parameters  [deletePrepareMessage]
         *@return
         *@author  Zhang Hongxuan
         *@create time  2021/3/21
         */
        super(deletePrepareMessage.getLargeId(), deletePrepareMessage.getDeleteTarget());
        this.t = deletePrepareMessage.t;
        this.relation = deletePrepareMessage.relation; // Remember call setRelation later.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeletePrepareMessage that = (DeletePrepareMessage) o;
        return Double.compare(that.t, t) == 0
                && relation == that.relation
                && this.getLargeId() == that.getLargeId()
                && this.getDeleteTarget() == that.getDeleteTarget();
    }

    @Override
    public int hashCode() {
        return Objects.hash(t, relation);
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
            return this.relation.ordinal() - o.relation.ordinal();
        }
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    public Relation getRelation() {
        return relation;
    }

    public double getT() {
        return t;
    }

    public int getSendTargetEgoTreeId() {
        return sendTargetEgoTreeId;
    }

    public void setSendTargetEgoTreeId(int sendTargetEgoTreeId) {
        this.sendTargetEgoTreeId = sendTargetEgoTreeId;
    }
}

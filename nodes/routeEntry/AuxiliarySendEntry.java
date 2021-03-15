package projects.cbrenet.nodes.routeEntry;

public class AuxiliarySendEntry extends SendEntry{

    private boolean deleteConditionSatisfied = false;

    private boolean queueEmpty = true;

    public AuxiliarySendEntry(int egoTreeIdOfParent, int egoTreeIdOfLeftChild, int egoTreeIdOfRightChild) {
        super(egoTreeIdOfParent, egoTreeIdOfLeftChild, egoTreeIdOfRightChild);
    }


    // Getter & Setter
    public boolean isDeleteConditionSatisfied() {
        return deleteConditionSatisfied;
    }

    public boolean isQueueEmpty() {
        return queueEmpty;
    }

    public void setDeleteConditionSatisfied(boolean deleteConditionSatisfied) {
        this.deleteConditionSatisfied = deleteConditionSatisfied;
    }

    public void setQueueEmpty(boolean queueEmpty) {
        this.queueEmpty = queueEmpty;
    }
}

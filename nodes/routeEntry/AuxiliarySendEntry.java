package projects.cbrenet.nodes.routeEntry;

public class AuxiliarySendEntry extends SendEntry{

    private boolean insertedFlag = false;
    //  when this is true, even the Entry could be delete, we should not delete since it would be inserted!

    private boolean deleteConditionSatisfied = false;


    public AuxiliarySendEntry(int egoTreeIdOfParent, int egoTreeIdOfLeftChild, int egoTreeIdOfRightChild) {
        super(egoTreeIdOfParent, egoTreeIdOfLeftChild, egoTreeIdOfRightChild);
    }


    // Getter & Setter
    public boolean isDeleteConditionSatisfied() {
        return deleteConditionSatisfied;
    }



    public void setDeleteConditionSatisfied(boolean deleteConditionSatisfied) {
        this.deleteConditionSatisfied = deleteConditionSatisfied;
    }

    public void setInsertedFlag(boolean insertedFlag) {
        this.insertedFlag = insertedFlag;
    }

    public boolean isInsertedFlag() {
        return insertedFlag;
    }
}

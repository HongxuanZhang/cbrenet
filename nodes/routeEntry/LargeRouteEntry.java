package projects.cbrenet.nodes.routeEntry;

public class LargeRouteEntry extends BaseRouteEntry{
    private final int largeId;

    private final int parentId;
    private final int leftChildId;
    private final int rightChildId;

    public int getLargeId() {
        return largeId;
    }

    public int getParentId() {
        return parentId;
    }

    public int getLeftChildId() {
        return leftChildId;
    }

    public int getRightChildId() {
        return rightChildId;
    }

    public LargeRouteEntry(int source, int destination, int largeId, int parentId, int leftChildId, int rightChildId){
        super(source, destination);
        this.largeId = largeId;
        this.parentId = parentId;
        this.leftChildId = leftChildId;
        this.rightChildId = rightChildId;
    }

    @Override
    public String getDescription() {
        return "A Route Entry under the Large Node " + this.largeId;
    }

    @Override
    public String toString()
    {
        return "This is " + this.getDescription() + ": from " +this.startPoint + " to left child " + this.leftChildId +
                " or from " +this.startPoint + " to right child " + this.rightChildId;
    }
}

package projects.cbrenet.nodes.routeEntry;


public class HelperRouteEntry extends BaseRouteEntry{

    // the link under the tree of largeId1
    private final int largeId1;
    private final int parentId1;
    private final int leftChildId1;
    private final int rightChildId1;

    // the link under the tree of largeId2
    private final int largeId2;
    private final int parentId2;
    private final int leftChildId2;
    private final int rightChildId2;

    public int getLargeId1() {
        return largeId1;
    }

    public int getParentId1() {
        return parentId1;
    }

    public int getLeftChildId1() {
        return leftChildId1;
    }

    public int getRightChildId1() {
        return rightChildId1;
    }

    public int getLargeId2() {
        return largeId2;
    }

    public int getParentId2() {
        return parentId2;
    }

    public int getLeftChildId2() {
        return leftChildId2;
    }

    public int getRightChildId2() {
        return rightChildId2;
    }

    public HelperRouteEntry(int largeId1, int parentId1, int leftChildId1,
                            int rightChildId1, int largeId2, int parentId2, int leftChildId2, int rightChildId2){
        super(largeId1, largeId2);

        this.largeId1 = largeId1;
        this.parentId1 = parentId1;
        this.leftChildId1 = leftChildId1;
        this.rightChildId1 = rightChildId1;

        this.largeId2 = largeId2;
        this.parentId2 = parentId2;
        this.leftChildId2 = leftChildId2;
        this.rightChildId2 = rightChildId2;
    }

    @Override
    public String getDescription() {
        return "A Helper Route Entry under the Large Node " + this.largeId1 +" and Large Node " + this.largeId2;
    }
}

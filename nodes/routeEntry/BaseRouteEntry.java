package projects.cbrenet.nodes.routeEntry;


abstract class BaseRouteEntry {
    final protected int startPoint; // Node id
    final protected int destination; // Node id

    public abstract String getDescription();

    protected BaseRouteEntry(){
        this.destination = -1;
        this.startPoint = -1;
    }

    protected BaseRouteEntry(int startPoint, int destination){
        this.startPoint = startPoint;
        this.destination = destination;
    }

    public int getDestination(){return this.destination;}

    public int getStartPoint(){return this.startPoint;}

    @Override
    public String toString(){
        return "This is " + this.getDescription() + ": from " +this.startPoint + " to " + this.destination;
    }

}

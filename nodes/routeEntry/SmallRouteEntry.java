package projects.cbrenet.nodes.routeEntry;

public class SmallRouteEntry extends BaseRouteEntry{

    @Override
    public String getDescription() {
        return "Small Link";
    }

    public SmallRouteEntry(int source, int destination){
        super(source,destination);
    }

//    public static void main(String[] args){
//        SmallRouteEntry se = new SmallRouteEntry(0,1);
//        SmallRouteEntry se1 = new SmallRouteEntry(0,0);
//        HelperRouteEntry helperRouteEntry = new HelperRouteEntry(0,0,0,0,0,0,0,0);
//        System.out.println(se.toString());
//        System.out.println(se1.toString());
//        System.out.println(helperRouteEntry.toString());
//        System.out.println(new LargeRouteEntry(0,0,0,0,0,0).toString());
//    }

}

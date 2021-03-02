package projects.cbrenet.nodes.tableEntry;

import projects.cbrenet.nodes.dataStructure.LRULinkedHashMap;
import projects.cbrenet.nodes.routeEntry.HelperRouteEntry;
import projects.cbrenet.nodes.routeEntry.LargeRouteEntry;
import projects.cbrenet.nodes.routeEntry.SmallRouteEntry;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class RouteTableForSmallNode {
    // routing table for small node
    // S: Small node
    LRULinkedHashMap<Integer, SmallRouteEntry> smallTable; //des, used when the message sender is a small node.
    // L: Large node
    // 1: ID for the Large node
    LRULinkedHashMap<Integer, LargeRouteEntry> lagerTable; // src , for we have to decide the link according to which
    // large node send the message.
    // H: Helper node
    LRULinkedHashMap<Integer, HelperRouteEntry> helperTable; // des


    public void moveToFront(){

    }

    public static void main(String[] args){
        LinkedHashMap<String, String> linkedHashMap =
                new LinkedHashMap<String, String>(16, 0.75f, true);
        linkedHashMap.put("111", "111");
        linkedHashMap.put("222", "222");
        linkedHashMap.put("333", "333");
        linkedHashMap.put("444", "444");
        loopLinkedHashMap(linkedHashMap);
        linkedHashMap.get("111");
        loopLinkedHashMap(linkedHashMap);
        linkedHashMap.put("222", "2222");
        loopLinkedHashMap(linkedHashMap);
    }

    public static void loopLinkedHashMap(LinkedHashMap<String, String> linkedHashMap)
    {
        Set<Map.Entry<String, String>> set = linkedHashMap.entrySet();
        Iterator<Map.Entry<String, String>> iterator = set.iterator();

        while (iterator.hasNext())
        {
            System.out.print(iterator.next() + "\t");
        }
        System.out.println();
    }

}

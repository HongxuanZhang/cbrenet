package projects.cbrenet.nodes.nodeImplementations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class SDNApp extends SDNNode{

    private Queue<AuxiliaryNode> auxiliaryNodeQueue = new LinkedList<>();

    public void addAuxiliaryNode(AuxiliaryNode auxiliaryNode){
        this.auxiliaryNodeQueue.add(auxiliaryNode);
    }

    public void removeAuxiliaryNode(int id){
        Queue<AuxiliaryNode> nodesTmp = new LinkedList<>();
        while(!this.auxiliaryNodeQueue.isEmpty()){
            AuxiliaryNode node = this.auxiliaryNodeQueue.poll();
            if(node.ID != id)
                nodesTmp.add(node);
        }
        this.auxiliaryNodeQueue.addAll(nodesTmp);
    }

    public SDNApp(int constC) {
        super(constC);
    }


    public void outputGlobalSituationInSDN(){
        System.out.println("--------SDN Report-------");

        HashMap<Integer, Boolean> map = this.smallStatusBits;
        ArrayList<Integer> smallNode = new ArrayList<>();
        ArrayList<Integer> bigNode = new ArrayList<>();

        ArrayList<Integer> keys = new ArrayList<Integer>(map.keySet());

        for(int keyId : keys){
            if(map.get(keyId)){
                smallNode.add(keyId);
            }
            else{
                bigNode.add(keyId);
            }
        }

        System.out.println("Small Nodes: " + smallNode);
        System.out.println("Big Nodes: " + bigNode);

    }

    // make large

    // make small

    // clear()

    // add route


}

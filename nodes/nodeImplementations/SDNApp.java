package projects.cbrenet.nodes.nodeImplementations;

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



    // make large

    // make small

    // clear()

    // add route


}

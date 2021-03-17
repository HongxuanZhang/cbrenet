package projects.cbrenet.nodes.nodeImplementations.deleteProcedure;

// 放一些AuxiliaryNode 和 CBBSTNode 都需要的代码

import projects.cbrenet.nodes.messages.deletePhaseMessages.DeletePrepareMessage;
import projects.cbrenet.nodes.nodeImplementations.AuxiliaryNodeMessageQueueLayer;
import projects.cbrenet.nodes.nodeImplementations.CounterBasedBSTLayer;
import projects.cbrenet.nodes.nodeImplementations.MessageSendLayer;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

import java.util.List;
import java.util.Random;

public class DeleteProcess {

    public void startDelete(SendEntry deleteEntry, Node node, int largeId, int helpedId){

        // when node is node AN, node.id should equal to helpedId
        assert !(node instanceof CounterBasedBSTLayer) || node.ID == helpedId;


        if(!deleteEntry.isDeleteFlag()){
            Tools.warning("Want to delete, but can not");
            return;
        }

        List<Integer> sendTargeList = deleteEntry.getAllSendIds();

        DeletePrepareMessage deletePrepareMessage = new DeletePrepareMessage
                (largeId, helpedId, Tools.getGlobalTime() + (new Random()).nextDouble());


        for(int targetId : sendTargeList){
            boolean upward = false;
            if(deleteEntry.getRelationShipTo(targetId) == 'p'){
                upward = true;
            }
            if(node instanceof MessageSendLayer){
                ((MessageSendLayer)node).sendEgoTreeMessage(largeId, targetId,
                        deletePrepareMessage, upward);
            }
            else if(node instanceof AuxiliaryNodeMessageQueueLayer){
                ((AuxiliaryNodeMessageQueueLayer)node).sendEgoTreeMessage(largeId, targetId,
                        deletePrepareMessage, upward, helpedId);
            }

        }






    }



}

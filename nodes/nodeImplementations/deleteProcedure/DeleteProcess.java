package projects.cbrenet.nodes.nodeImplementations.deleteProcedure;

// 放一些AuxiliaryNode 和 CBBSTNode 都需要的代码

import projects.cbrenet.nodes.messages.deletePhaseMessages.DeletePrepareMessage;
import projects.cbrenet.nodes.nodeImplementations.AuxiliaryNodeMessageQueueLayer;
import projects.cbrenet.nodes.nodeImplementations.MessageSendLayer;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

import java.util.List;
import java.util.Random;

public class DeleteProcess {

    public void startDelete(SendEntry deleteEntry, Node node, int largeId){

        if(!deleteEntry.isDeleteFlag()){
            Tools.warning("Want to delete, but can not");
            return;
        }

        List<Integer> sendTargeList = deleteEntry.getAllSendIds();

        DeletePrepareMessage deletePrepareMessage = new DeletePrepareMessage
                (largeId, node.ID, Tools.getGlobalTime() + (new Random()).nextDouble());

        for(int targetId : sendTargeList){
            if(node instanceof MessageSendLayer){
                ((MessageSendLayer)node).sendEgoTreeMessage(largeId,targetId, deletePrepareMessage);
            }
            else if(node instanceof AuxiliaryNodeMessageQueueLayer){

            }

        }






    }



}

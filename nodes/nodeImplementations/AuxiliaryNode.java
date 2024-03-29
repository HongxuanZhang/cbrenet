package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.AuxiliaryNodeMessage.AuxiliaryRequestMessage;
import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.SDNMessage.LargeInsertMessage;
import projects.cbrenet.nodes.messages.controlMessage.clusterMessage.ClusterRelatedMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteBaseMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteConfirmMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteFinishMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeletePrepareMessage;
import projects.cbrenet.nodes.nodeImplementations.deleteProcedure.DeleteProcess;
import projects.cbrenet.nodes.routeEntry.AuxiliarySendEntry;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;


/**
 * 只用于Ego-Tree中结点的辅助，只用于转发Message
 * 当满足条件时进行删除： 1. 只有一个孩子
 *                    2. 全Ego-Tree清除 （不需要来自SDN的DeleteMessage, 当其plr都断后自己即可清除）
 *
 * 特别注意，不处理LIM, 遇到LIM发现居然要执行的，说明自身已经可以删除了，把LIM返回给上一级结点。
 * */

public class AuxiliaryNode extends AuxiliaryNodeMessageQueueLayer{

    public AuxiliaryNode(){
        this.init();
    }

    private void executeRoutingMessage(RoutingMessage routingMessage){
        Message payload = routingMessage.getPayload();
        System.out.println("Auxiliary Node " + this.ID + " receive a routing message contains " +
                "" + payload.getClass().getSimpleName() + ", content is " + payload.toString());

        if(payload instanceof LargeInsertMessage){
            // check whether this need to make the node return.
            int largeId = routingMessage.getLargeId();
            ((LargeInsertMessage) payload).setAuxiliaryNodeId(this.ID);
            int helpedId = ((LargeInsertMessage) payload).getTarget();
            AuxiliarySendEntry entry = this.getCorrespondingEntry(helpedId, largeId);
            if(entry != null){
                entry.setAuxiliaryId((LargeInsertMessage) payload);
                this.deleteProcess.startDelete(entry, this, largeId, helpedId);

                // 把Entry加到 InsertedNode中去

                // no need to forward any more.
                return;
            }
            else{
                // means this AN do not have the LIM's target's entry,
                // no need to call the node back
                // continue Forward
            }
        }
        else if(payload instanceof DeleteBaseMessage){
            DeleteBaseMessage deleteBaseMessage = (DeleteBaseMessage) payload;

            int largeId = deleteBaseMessage.getLargeId();
            int helpedId = -1;

            if(deleteBaseMessage instanceof DeletePrepareMessage){
                helpedId = ((DeletePrepareMessage)deleteBaseMessage).getSendTargetEgoTreeId();
            }
            else if(deleteBaseMessage instanceof DeleteConfirmMessage){
                helpedId = ((DeleteConfirmMessage)deleteBaseMessage).getDeleteTarget();
            }
            else if(deleteBaseMessage instanceof DeleteFinishMessage){
                helpedId = ((DeleteFinishMessage)deleteBaseMessage).getTargetEgoTreeId();
            }

            SendEntry entry = this.getCorrespondingEntry(helpedId, largeId);
            if(entry != null){
                // the DBM got the destination
                if(this.deleteProcess.executeDeleteBaseMessage(deleteBaseMessage, this, this, helpedId)){
                    // remove entry.
                    this.removeCorrespondingEntry(helpedId,largeId);
                }
            }
            return;
        }
        else if(payload instanceof CbRenetMessage){
            ((CbRenetMessage) payload).incrementRouting();
        }
        else if(payload instanceof ClusterRelatedMessage){
            this.clusterHelper.receiveClusterRelatedMessage((ClusterRelatedMessage) payload, this,this,
                    routingMessage.getNextHop());
            return;
        }

        if(!this.forwardMessage( routingMessage )){
            this.addRoutingMessageToQueue(routingMessage.getNextHop(), routingMessage);
        }
    }

    @Override
    public void handleMessages(Inbox inbox) {
        // AuxiliaryRequestMessage
        // RoutingMessage ( 除了转发之外要特殊处理  1. LIM  2. )

        while (inbox.hasNext()) {
            Message msg = inbox.next();

            if(msg instanceof RoutingMessage){
                RoutingMessage routingMessage = (RoutingMessage) msg;

                this.executeRoutingMessage(routingMessage);

            }
            else if(msg instanceof AuxiliaryRequestMessage){
                // todo 这里没有用上哦。。
                AuxiliaryRequestMessage auxiliaryRequestMessage = (AuxiliaryRequestMessage) msg;
                int helpedId = auxiliaryRequestMessage.getHelpedId();
                int largeId = auxiliaryRequestMessage.getLargeId();
                int egoTreeId_p = auxiliaryRequestMessage.getEgoTreeId_p();
                int egoTreeId_l = auxiliaryRequestMessage.getEgoTreeId_l();
                int egoTreeId_r = auxiliaryRequestMessage.getEgoTreeId_r();

                int sendId_p = auxiliaryRequestMessage.getSendId_p();
                int sendId_l = auxiliaryRequestMessage.getSendId_l();
                int sendId_r = auxiliaryRequestMessage.getSendId_r();

                // add link
                this.addLinkToParent(largeId, egoTreeId_p, sendId_p, helpedId);
                this.addLinkToLeftChild(largeId, egoTreeId_l, sendId_l, helpedId);
                this.addLinkToRightChild(largeId, egoTreeId_r, sendId_r, helpedId);
            }
            else{

            }
        }

        while(!this.cycleRoutingMessage.isEmpty()){
            // A routing message could cycle multiple times here.
            // Could make a routing message pass through a lot of nodes
            RoutingMessage routingMessage = this.cycleRoutingMessage.poll();
            this.executeRoutingMessage(routingMessage);
        }
    }

    @Override
    public void preStep() {

    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void neighborhoodChange() {

    }

    @Override
    public void postStep() {
        this.doInPostRound();
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {

    }
}

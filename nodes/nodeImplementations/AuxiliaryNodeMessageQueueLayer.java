package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteBaseMessage;
import projects.cbrenet.nodes.nodeImplementations.deleteProcedure.DeleteProcess;
import projects.cbrenet.nodes.nodeImplementations.nodeHelper.MessageForwardHelper;
import projects.cbrenet.nodes.routeEntry.AuxiliarySendEntry;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

import java.util.*;

public abstract class AuxiliaryNodeMessageQueueLayer extends AuxiliaryNodeStructureLayer {


    protected final DeleteProcess deleteProcess = new DeleteProcess();

    protected void doInPostRound() {

        // clear queue part and delete condition check
        HashMap<Integer, HashMap<Integer, AuxiliarySendEntry>> routeTableTmp = this.getRouteTable();

        if(routeTableTmp == null){
            return;
        }

        Set<Integer> helpedIds = routeTableTmp.keySet();

        for (int helpedId : helpedIds) {
            HashMap<Integer, AuxiliarySendEntry> sendEntryHashMap = routeTableTmp.getOrDefault(helpedId, null);
            if (sendEntryHashMap != null) {
                Set<Integer> largeIds = sendEntryHashMap.keySet();
                for (int largeId : largeIds) {
                    AuxiliarySendEntry entry = this.getCorrespondingEntry(helpedId, largeId);

                    // 这里可能会有bug,,但是Java的数据结构这么做似乎没有问题。
                    Queue<RoutingMessage> routingMessageQueue = entry.getRoutingMessageQueue();
                    Queue<RoutingMessage> sendFailedQueue = new LinkedList<>();
                    while (!routingMessageQueue.isEmpty()) {
                        RoutingMessage msgTmp = routingMessageQueue.poll();
                        if (!this.forwardMessage(msgTmp)) {
                            sendFailedQueue.add(msgTmp);
                        }
                    }
                    routingMessageQueue.addAll(sendFailedQueue);
                    if (entry.getRoutingMessageQueue().isEmpty()) {
                        if (!routingMessageQueue.isEmpty()) {
                            Tools.fatalError("In ANMQLayer, the queue in the entry is not equal to the queue " +
                                    "routingMessageQueue");
                        }
                        entry.setQueueEmpty(true);
                    }

                    // Check whether satisfy the delete condition
                    int egoIdOfLeft = entry.getEgoTreeIdOfLeftChild();
                    int egoIdOfRight = entry.getEgoTreeIdOfRightChild();
                    entry.setDeleteConditionSatisfied(this.checkRemoveEntryCondition(egoIdOfLeft, egoIdOfRight));
                    // Part finished!

                    // Start a Delete Process here?
                    if(entry.isDeleteConditionSatisfied()) {
                        this.deleteProcess.startDelete(entry, this, largeId, helpedId);
                    }

                }
            } else {
                Tools.fatalError("");
            }
        }
        // clear queue part and delete condition check
    }


    public boolean sendEgoTreeMessage(int largeId, int dst, Message msg, boolean upward,int helpedId){
        assert msg instanceof DeleteBaseMessage;

        RoutingMessage routingMessage = new RoutingMessage(this.ID, dst, msg, largeId, upward);

        routingMessage.setNextHop(helpedId);

        if(!this.forwardMessage(routingMessage)){
            this.addRoutingMessageToQueue(helpedId, routingMessage);
            return false;
        }
        return true;
    }


    public void addRoutingMessageToQueue(int helpedID, RoutingMessage routingMessage) {
        int largeId = routingMessage.getLargeId();
        AuxiliarySendEntry entry = this.getCorrespondingEntry(helpedID, largeId);
        entry.addMessageIntoRoutingMessageQueue(routingMessage);
    }


    public boolean forwardMessage(RoutingMessage routingMessage) {
        /**
         *@description The auxiliary node use this to transfer message
         *@parameters [largeId, msg]
         *@return boolean
         *@author Zhang Hongxuan
         *@create time  2021/3/14
         */
        MessageForwardHelper helper = new MessageForwardHelper();

        // 为什么是helped id 呢？ 因为forwardMessage需要和第一位参数进行比较的来决定RoutingMessage该去哪的
        return helper.forwardMessage(this.ID, this, routingMessage);
    }
}

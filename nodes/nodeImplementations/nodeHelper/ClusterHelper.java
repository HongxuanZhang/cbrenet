package projects.cbrenet.nodes.nodeImplementations.nodeHelper;

import projects.cbrenet.nodes.messages.controlMessage.*;
import projects.cbrenet.nodes.nodeImplementations.AuxiliaryNodeMessageQueueLayer;
import projects.cbrenet.nodes.nodeImplementations.MessageSendLayer;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

public class ClusterHelper {



    public void clusterRequest(SendEntry entry, int largeId, int helpedId, Node node){

        //int largeId, int currentNode, int requesterId, int position, double generateTime

        if(entry.getClusterMessage() == null){

            RequestClusterMessage clusterMessage = new RequestClusterMessage(largeId, -1,
                    helpedId, 0, Tools.getGlobalTime());

            entry.setClusterMessage(clusterMessage);

        }


        if(entry.checkRotationRequirement()){
            // send cluster Message
            RequestClusterMessage clusterMessage = entry.getClusterMessage();

            NodeInfo nodeInfo = new NodeInfo(helpedId, entry.getEgoTreeIdOfLeftChild(), entry.getSendIdOfRightChild(),
                    entry.getSendIdOfLeftChild(), entry.getSendIdOfRightChild(),
                    entry.getCounter(), entry.getWeightOfLeft(), entry.getWeightOfRight());

            clusterMessage.addNodeInfoPair(clusterMessage.getPosition(), nodeInfo);

            this.sendRequestClusterMessageUp(node, entry, largeId, clusterMessage, helpedId);

            entry.addRequestClusterMessageIntoPriorityQueue(clusterMessage);
        }
    }



    public void receiveRequestClusterMessage(Node node, SendEntry entry, RequestClusterMessage requestClusterMessage,
                                             int helpedId){

        int largeId = requestClusterMessage.getLargeId();
        int clusterId = requestClusterMessage.getRequesterId();

        if(entry.checkRotationRequirement()){

            boolean continueFlag = true;

            entry.addRequestClusterMessageIntoPriorityQueue(requestClusterMessage);

            // process
            requestClusterMessage.shiftPosition();
            int position = requestClusterMessage.getPosition();

            NodeInfo nodeInfo = new NodeInfo(helpedId, entry.getEgoTreeIdOfLeftChild(), entry.getSendIdOfRightChild(),
                    entry.getSendIdOfLeftChild(), entry.getSendIdOfRightChild(),
                    entry.getCounter(), entry.getWeightOfLeft(), entry.getWeightOfRight());

            requestClusterMessage.addNodeInfoPair(position, nodeInfo);


            // todo 完善 处理part


            // process part end

            if(position == 2){
                // 计算势变
                RotationHelper rotationHelper = new RotationHelper();
                double diff = rotationHelper.diffPotential(requestClusterMessage);
            }


            if(position == 3 || entry.isEgoTreeRoot()){
                requestClusterMessage.setFinalNode();

                requestClusterMessage.setTheMasterOfCluster(helpedId);

                entry.updateHighestPriorityRequest();

                RequestClusterMessage highestRequest = entry.getHighestPriorityRequest();

                if(highestRequest.getRequesterId() == clusterId &&
                        highestRequest.getLargeId() == largeId){
                    entry.lockUpdateHighestPriorityRequestPermission();
                    entry.setCurrentClusterRequesterId(clusterId);

                    // remember that masterId equals to helpedId
                    AckClusterMessage ackClusterMessage = new AckClusterMessage(helpedId, largeId, clusterId, requestClusterMessage.getGenerateTime(), );
                    this.sendAckBaseMessage(node, entry, largeId, ackClusterMessage, helpedId);
                }
                else{
                    NonAckClusterMessage nonAckClusterMessage = new NonAckClusterMessage
                            (helpedId, largeId,clusterId, false);
                    this.sendAckBaseMessage(node, entry, largeId, nonAckClusterMessage, helpedId);
                }


            }
            else{
                if(continueFlag){
                    this.sendRequestClusterMessageUp(node, entry, largeId, requestClusterMessage, helpedId);
                }
                else{
                    NonAckClusterMessage nonAckClusterMessage = new NonAckClusterMessage(helpedId, largeId,clusterId, false);
                    this.sendAckBaseMessage(node, entry, largeId, nonAckClusterMessage, helpedId);
                }
            }
        }
        else{
            NonAckClusterMessage nonAckClusterMessage = new NonAckClusterMessage(helpedId, largeId,clusterId, false);
            this.sendAckBaseMessage(node, entry, largeId, nonAckClusterMessage, helpedId);
        }



    }


    public void receiveAckClusterMessage(AckClusterMessage ackClusterMessage, SendEntry entry,
                                         Node node, int helpedId){
        int largeId = ackClusterMessage.getLargeId();
        int clusterId = ackClusterMessage.getClusterId();
        int masterId = ackClusterMessage.getMasterId();


        entry.updateHighestPriorityRequest();

        RequestClusterMessage highestRequest = entry.getHighestPriorityRequest();
        if(clusterId == highestRequest.getRequesterId() && largeId == highestRequest.getLargeId()){

            entry.lockUpdateHighestPriorityRequestPermission();
            entry.setCurrentClusterRequesterId(clusterId);

            this.sendAckBaseMessage(node, entry, largeId, ackClusterMessage, helpedId);

        }
        else{
            // 虽然上面的结点接受聚合，但是本结点处还有更高优先级的请求要同意，所以当前的结点不接受聚合
            // 向上发NonAck
            NonAckClusterMessage nonAckClusterMessage = new NonAckClusterMessage(masterId,largeId, clusterId,true);
            this.sendAckBaseMessage(node, entry, largeId, nonAckClusterMessage, helpedId);
            // 向下发NonAck
            NonAckClusterMessage nonAckClusterMessage2 = new NonAckClusterMessage(masterId,largeId, clusterId,false);
            this.sendAckBaseMessage(node, entry, largeId, nonAckClusterMessage2, helpedId);

            entry.deleteCorrespondingRequestClusterMessageInPriorityQueue(largeId, clusterId);
        }

    }



    public void receiveNonAckClusterMessage(NonAckClusterMessage nonAckClusterMessage, SendEntry entry,
                                            Node node, int helpedId){
        int largeId = nonAckClusterMessage.getLargeId();
        int clusterId = nonAckClusterMessage.getClusterId();
        int masterId = nonAckClusterMessage.getMasterId();
        boolean upward = nonAckClusterMessage.isUpward();
        // 既然被否决，那就移除吧。
        entry.deleteCorrespondingRequestClusterMessageInPriorityQueue(largeId, clusterId);


        if(!upward) {
            // 当前结点是否是requester的判断
            if (node.ID != clusterId) {
                // 这一条 NonAck 还没传递到目的地，需要继续传递
                NonAckClusterMessage nonAckClusterMessage2 = new NonAckClusterMessage
                        (masterId, largeId, clusterId, false);

                this.sendAckBaseMessage(node, entry, largeId, nonAckClusterMessage2, helpedId);
            }
        }
        else{
            // 当前结点是否是master
            if(node.ID != masterId){
                NonAckClusterMessage nonAckClusterMessage2 = new NonAckClusterMessage
                        (masterId, largeId, clusterId, true);

                this.sendAckBaseMessage(node, entry, largeId, nonAckClusterMessage2, helpedId);
            }
        }

        if(clusterId == entry.getCurrentClusterRequesterId()){
            entry.unlockUpdateHighestPriorityRequestPermission();
            entry.setCurrentClusterRequesterId(-1);
        }

    }



    // todo 这两个 sendMessage 部分有一点点问题，一个是一个一个传的，第二个是直接找终点的，不太一样，检查是否会导致bug
    // Send Message Part
    private void sendRequestClusterMessageUp(Node node, SendEntry entry, int largeId,
                                             RequestClusterMessage requestClusterMessage, int helpedId){

        boolean upward = true;
        int egoParentId = entry.getEgoTreeIdOfParent();

        if(node instanceof MessageSendLayer){
            ((MessageSendLayer)node).sendEgoTreeMessage(largeId, egoParentId,
                    requestClusterMessage, upward);
        }
        else if(node instanceof AuxiliaryNodeMessageQueueLayer){
            ((AuxiliaryNodeMessageQueueLayer)node).sendEgoTreeMessage(largeId, egoParentId,
                    requestClusterMessage, upward, helpedId);
        }

    }

    private void sendAckBaseMessage(Node node, SendEntry entry, int largeId,
                                    AckBaseMessage ackBaseMessage, int helpedId){
        // targetId 记得改，可能是 master 也 可能是requester
        int targetId = ackBaseMessage.getClusterId();

        boolean upward = false;
        if(ackBaseMessage instanceof NonAckClusterMessage){
            upward =((NonAckClusterMessage)ackBaseMessage).isUpward();
        }


        if(node instanceof MessageSendLayer){
            ((MessageSendLayer)node).sendEgoTreeMessage(largeId, targetId,
                    ackBaseMessage, upward);
        }
        else if(node instanceof AuxiliaryNodeMessageQueueLayer){
            ((AuxiliaryNodeMessageQueueLayer)node).sendEgoTreeMessage(largeId, targetId,
                    ackBaseMessage, upward, helpedId);
        }


    }

}

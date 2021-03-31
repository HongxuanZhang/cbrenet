package projects.cbrenet.nodes.nodeImplementations.nodeHelper;

import projects.cbrenet.nodes.messages.controlMessage.*;
import projects.cbrenet.nodes.nodeImplementations.AuxiliaryNode;
import projects.cbrenet.nodes.nodeImplementations.AuxiliaryNodeMessageQueueLayer;
import projects.cbrenet.nodes.nodeImplementations.MessageSendLayer;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

import java.util.List;

public class ClusterHelper {

    private final double epsilon = -1.5;

    public void clusterRequest(SendEntry entry, int largeId, int helpedId, Node node){

        //int largeId, int currentNode, int requesterId, int position, double generateTime

        if(entry.getClusterMessageOfMine() == null){

            RequestClusterMessage clusterMessage = new RequestClusterMessage(largeId, -1,
                    helpedId, 0, Tools.getGlobalTime());

            entry.setClusterMessageOfMine(clusterMessage);

        }

        if(entry.checkAdjustRequirement()){
            // send cluster Message
            RequestClusterMessage clusterMessage = entry.getClusterMessageOfMine();

            NodeInfo nodeInfo = new NodeInfo(node.ID, helpedId, entry.getEgoTreeIdOfLeftChild(), entry.getSendIdOfRightChild(),
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

        if(!entry.checkRotationRequirement()){
            RejectClusterMessage rejectClusterMessage = new RejectClusterMessage(helpedId, largeId,clusterId, false);
            this.sendAcceptOrRejectMessage(node, entry, largeId, rejectClusterMessage, helpedId);
        }

        else{

            requestClusterMessage.shiftPosition();
            int position = requestClusterMessage.getPosition();

            if(position == 1 || position == 2){
                // add in node info
                NodeInfo nodeInfo = new NodeInfo(node.ID, helpedId, entry.getEgoTreeIdOfLeftChild(), entry.getSendIdOfRightChild(),
                        entry.getSendIdOfLeftChild(), entry.getSendIdOfRightChild(),
                        entry.getCounter(), entry.getWeightOfLeft(), entry.getWeightOfRight());

                requestClusterMessage.addNodeInfoPair(position, nodeInfo);


                // add relation
                int index = position - 1;
                NodeInfo info = requestClusterMessage.getNodeInfoOf(index);
                if(index == 0){
                    requestClusterMessage.setRelationFromNode0ToNode1(entry.getRelationShipOf(info.getCurNodeTrueId()));
                }
                else {
                    requestClusterMessage.setRelationFromNode1ToNode2(entry.getRelationShipOf(info.getCurNodeTrueId()));
                }


                RotationHelper rotationHelper = new RotationHelper();
                double diff = 0d;

                if(entry.isEgoTreeRoot()){
                    // 到头了
                    // calculate potential difference
                    diff = rotationHelper.diffPotential(requestClusterMessage);

                    if(diff > this.epsilon) {
                        // reject
                        RejectClusterMessage rejectClusterMessage = new RejectClusterMessage(helpedId, largeId,
                                clusterId, false);
                        this.sendAcceptOrRejectMessage(node, entry, largeId,
                                rejectClusterMessage, helpedId);
                    }
                    else{
                        this.setClusterMaster(helpedId, node.ID, requestClusterMessage);
                        // set the upper node of the cluster as LN, which do not need to join in cluster
                        this.setUpperNodeId(requestClusterMessage, entry.getEgoTreeIdOfParent(),
                                entry.getSendIdOfParent(), true);
                        entry.addRequestClusterMessageIntoPriorityQueue(requestClusterMessage);
                    }

                }
                else{
                    if(position == 2){
                        diff = rotationHelper.diffPotential(requestClusterMessage);

                        if(diff > this.epsilon) {
                            // reject
                            RejectClusterMessage rejectClusterMessage = new RejectClusterMessage(helpedId, largeId,
                                    clusterId, false);
                            this.sendAcceptOrRejectMessage(node, entry, largeId,
                                    rejectClusterMessage, helpedId);
                        }
                        else{
                            // 向上继续发送
                            this.sendRequestClusterMessageUp(node, entry, largeId, requestClusterMessage, helpedId);
                            entry.addRequestClusterMessageIntoPriorityQueue(requestClusterMessage);
                        }
                    }
                    else{
                        // position == 1 && root == false
                        // 往后发送后，(浅拷贝？)这里也会变吧，看看怎么处理还是没有影响。
                        this.sendRequestClusterMessageUp(node, entry, largeId, requestClusterMessage, helpedId);
                        entry.addRequestClusterMessageIntoPriorityQueue(requestClusterMessage);
                    }
                }
            }
            else{
                this.setClusterMaster(helpedId, node.ID, requestClusterMessage);
                // 由于这里的3号结点并不真正参与旋转，所以不必检测其是否为root
                this.setUpperNodeId(requestClusterMessage, helpedId, node.ID, false);
                entry.addRequestClusterMessageIntoPriorityQueue(requestClusterMessage);
            }
        }
    }


    public void acceptClusterRequest(SendEntry entry, int largeId ,int helpedId, Node node){
        /*
         *@description Execute this method in the post round
         *@parameters  [entry, largeId, helpedId, node]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/24
         */
        assert node instanceof AuxiliaryNode || node.ID == helpedId;

        if(entry.getCurrentClusterRequesterId() > 0){
            // mean the node is in the cluster already.
            return;
        }

        entry.updateHighestPriorityRequest();

        RequestClusterMessage highestRequest = entry.getHighestPriorityRequest();

        if(highestRequest.getTheEgoTreeMasterOfCluster() == helpedId){
            // if this is the master

            entry.lockUpdateHighestPriorityRequestPermission();

            int clusterId = highestRequest.getRequesterId();

            entry.setCurrentClusterRequesterId(clusterId);
            // remember that masterId equals to helpedId
            AcceptClusterMessage acceptClusterMessage = new AcceptClusterMessage(helpedId, largeId,
                    clusterId, highestRequest.getGenerateTime(), highestRequest);
            this.sendAcceptOrRejectMessage(node, entry, largeId, acceptClusterMessage, helpedId);
        }


        // reject else request;
        // todo 现在只清理了Master里的，这些消息会怎么传播？？？会有哪些跟着被移除？哪些不会被移除呢？
        List<RequestClusterMessage> listTmp = entry.getAllRequestClusterMessage();

        for(RequestClusterMessage clusterMessage : listTmp){
            this.rejectRequest(entry,node, largeId, helpedId, clusterMessage.getRequesterId(),
                    clusterMessage.getTheEgoTreeMasterOfCluster());

        }
    }


    public void receiveAcceptClusterMessage(AcceptClusterMessage acceptClusterMessage, SendEntry entry,
                                            Node node, int helpedId){
        int largeId = acceptClusterMessage.getLargeId();
        int clusterId = acceptClusterMessage.getClusterId(); //also the requester's id
        int masterId = acceptClusterMessage.getMasterId();

        int currentClusterId = entry.getCurrentClusterRequesterId();

        if(currentClusterId> 0 && currentClusterId != clusterId){
            // reject since this node is in a cluster already
            this.rejectRequest(entry, node, largeId, helpedId, clusterId, masterId);
            return;
        }

        if(currentClusterId == clusterId){
             Tools.warning("Should not happen! How a node get the Accept ahead??");
            return;
        }

        entry.updateHighestPriorityRequest();
        RequestClusterMessage highestRequest = entry.getHighestPriorityRequest();
        if(highestRequest == null){
            return;
        }

        // 先判断是不是最高优先级的request
        if(clusterId == highestRequest.getRequesterId() && largeId == highestRequest.getLargeId()){
            entry.lockUpdateHighestPriorityRequestPermission();
            entry.setCurrentClusterRequesterId(clusterId);
            if(helpedId == clusterId){
                // the message reach the requester of the cluster
                // do adjust
                RotationHelper rotationHelper = new RotationHelper();
                rotationHelper.rotation(acceptClusterMessage);
            }
            else{
                this.sendAcceptOrRejectMessage(node, entry, largeId, acceptClusterMessage, helpedId);
            }

            // 这里，我要Accept了！
            // 可是，我手上的请求怎么办？
            // 残忍的抛弃它们吧！
            List<RequestClusterMessage> listTmp = entry.getAllRequestClusterMessage();

            for(RequestClusterMessage clusterMessage : listTmp){
                this.rejectRequest(entry,node, largeId, helpedId, clusterMessage.getRequesterId(),
                        clusterMessage.getTheEgoTreeMasterOfCluster());
            }

        }
        else{
            this.rejectRequest(entry, node, largeId, helpedId, clusterId, masterId);
        }
    }



    public void receiveRejectClusterMessage(RejectClusterMessage rejectClusterMessage, SendEntry entry,
                                            Node node, int helpedId){

        int largeId = rejectClusterMessage.getLargeId();
        int clusterId = rejectClusterMessage.getClusterId();
        int masterId = rejectClusterMessage.getMasterId();
        boolean upward = rejectClusterMessage.isUpward();
        // 既然被否决，那就移除吧。

        boolean continueSend = false;
        if(entry.deleteCorrespondingRequestClusterMessageInPriorityQueue(largeId, clusterId)){
            // 说明自身可能不是这条Reject消息的终点
            continueSend = true;
        }


        if(entry.getCurrentClusterRequesterId() == clusterId){
            // 所在的cluster被否决，退出并允许新的highest Priority Request出现
            entry.setCurrentClusterRequesterId(-3);
            entry.unlockUpdateHighestPriorityRequestPermission();
        }

        // 继续传

        if(continueSend) {
            if (!upward) {
                // 当前结点是否是requester的判断
                if (node.ID != clusterId) {
                    // 这一条 NonAck 还没传递到目的地，需要继续传递
                    RejectClusterMessage rejectClusterMessage2 = new RejectClusterMessage
                            (masterId, largeId, clusterId, false);

                    this.sendAcceptOrRejectMessage(node, entry, largeId, rejectClusterMessage2, helpedId);
                }
            } else {
                // 当前结点是否是master
                if (node.ID != masterId) {
                    // 不是master， 继续向上传
                    RejectClusterMessage rejectClusterMessage2 = new RejectClusterMessage
                            (masterId, largeId, clusterId, true);

                    this.sendAcceptOrRejectMessage(node, entry, largeId, rejectClusterMessage2, helpedId);
                }
            }
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

    private void sendAcceptOrRejectMessage(Node node, SendEntry entry, int largeId,
                                           AcceptOrRejectBaseMessage acceptOrRejectBaseMessage, int helpedId){
        // targetId 记得改，可能是 master 也 可能是requester
        int targetId = acceptOrRejectBaseMessage.getClusterId();

        boolean upward = false;
        if(acceptOrRejectBaseMessage instanceof RejectClusterMessage){
            upward =((RejectClusterMessage) acceptOrRejectBaseMessage).isUpward();
        }


        if(node instanceof MessageSendLayer){
            ((MessageSendLayer)node).sendEgoTreeMessage(largeId, targetId,
                    acceptOrRejectBaseMessage, upward);
        }
        else if(node instanceof AuxiliaryNodeMessageQueueLayer){
            ((AuxiliaryNodeMessageQueueLayer)node).sendEgoTreeMessage(largeId, targetId,
                    acceptOrRejectBaseMessage, upward, helpedId);
        }


    }


    private void rejectRequest(SendEntry entry, Node node, int largeId, int helpedId, int clusterId, int masterId){
        // todo cluster ID 和 masterId或许可以是随便写的？？
        // 虽然上面的结点接受聚合，但是本结点处还有更高优先级的请求要同意，所以当前的结点不接受聚合

        if(masterId != helpedId){
            // 向上发NonAck
            RejectClusterMessage rejectClusterMessage = new RejectClusterMessage(masterId,largeId, clusterId,true);
            this.sendAcceptOrRejectMessage(node, entry, largeId, rejectClusterMessage, helpedId);
        }
        // 向下发NonAck
        RejectClusterMessage rejectClusterMessage2 = new RejectClusterMessage(masterId,largeId, clusterId,false);
        this.sendAcceptOrRejectMessage(node, entry, largeId, rejectClusterMessage2, helpedId);

        entry.deleteCorrespondingRequestClusterMessageInPriorityQueue(largeId, clusterId);
    }


    private void setClusterMaster(int egoTreeId, int sendId, RequestClusterMessage requestClusterMessage){
        requestClusterMessage.setTheEgoTreeMasterOfCluster(egoTreeId);
        requestClusterMessage.setTheSendIdOfClusterMaster(sendId);
    }

    private void setUpperNodeId(RequestClusterMessage requestClusterMessage, int egoTreeId, int sendId,boolean lNFlag){
        requestClusterMessage.setTheMostUpperEgoTreeId(egoTreeId);
        requestClusterMessage.setTheMostUpperSendId(sendId);
        requestClusterMessage.setLnFlag(lNFlag);
    }



}

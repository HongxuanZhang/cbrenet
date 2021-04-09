package projects.cbrenet.nodes.nodeImplementations.nodeHelper;

import projects.cbrenet.CustomGlobal;
import projects.cbrenet.nodes.messages.controlMessage.clusterMessage.*;
import projects.cbrenet.nodes.nodeImplementations.AuxiliaryNode;
import projects.cbrenet.nodes.nodeImplementations.AuxiliaryNodeMessageQueueLayer;
import projects.cbrenet.nodes.nodeImplementations.MessageSendLayer;
import projects.cbrenet.nodes.routeEntry.SendEntry;
import projects.cbrenet.nodes.tableEntry.NodeInfo;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

import java.util.List;

public class SimpleClusterHelper {

    private double epsilon = -1.5;

    private boolean adjustFlag = false;

    private SimpleClusterHelper(){

    }

    private static SimpleClusterHelper clusterHelper = null;

    public static SimpleClusterHelper getInstance() {
        if (clusterHelper == null) {
            clusterHelper = new SimpleClusterHelper();
        }

        return clusterHelper;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public void setAdjustFlag(boolean adjustFlag) {
        this.adjustFlag = adjustFlag;
    }





    public void receiveClusterRelatedMessage(ClusterRelatedMessage msg, EntryGetter entryGetter, Node node, int helpedId){
        int largeId = msg.getLargeId();
        SendEntry entry = entryGetter.getCorrespondingEntry(helpedId, largeId);

        if(entry == null){
            // todo 加一个reject
            return;
        }

        if(msg instanceof RequestClusterMessage){
            this.receiveRequestClusterMessage((RequestClusterMessage) msg, entry, node, helpedId);
        }
        else if(msg instanceof AcceptClusterMessage){
            this.receiveAcceptClusterMessage((AcceptClusterMessage)msg, entry, node, helpedId);
        }
        else if(msg instanceof RejectClusterMessage){
            this.receiveRejectClusterMessage((RejectClusterMessage)msg, entry, node, helpedId);
        }


    }






    public void clusterRequest(SendEntry entry, int largeId, int helpedId, Node node){
        if(!entry.isRotationAbleFlag()){
            return;
        }

        if(!this.adjustFlag){
            CustomGlobal.adjustNum = 0;
            entry.setRotationAbleFlag(false);
            return;
        }

        if(CustomGlobal.deleteNum != 0){
            System.out.println("Cluster Helper : Node " + node.ID + " want to start a delete process" +
                    " corresponding helpedId is "+ helpedId+ " but the delete Num is not 0!");
            return;
        }

        CustomGlobal.adjustNum++;
        System.out.println("Cluster Helper : adjustNum ++ since a node try to rotate, " +
                " node : " +node.ID + " helpedId is " + helpedId + " , adjustNum is" + CustomGlobal.adjustNum);

        if(CustomGlobal.adjustNum > 1){
            CustomGlobal.adjustNum --;
            System.out.println("Cluster Helper : adjustNum --  since adjust num > 1");
            return;
        }


        if(entry.checkAdjustRequirement() && !entry.isEgoTreeRoot()){
            // send cluster Message
            RequestClusterMessage clusterMessage = new RequestClusterMessage(largeId, -1,
                    helpedId, 0, Tools.getGlobalTime());


            NodeInfo nodeInfo = new NodeInfo(node.ID, helpedId, entry.getEgoTreeIdOfLeftChild(), entry.getSendIdOfRightChild(),
                    entry.getSendIdOfLeftChild(), entry.getSendIdOfRightChild(),
                    entry.getCounter(), entry.getWeightOfLeft(), entry.getWeightOfRight());

            clusterMessage.addNodeInfoPair(clusterMessage.getPosition(), nodeInfo);

            this.sendRequestClusterMessageUp(node, entry, largeId, clusterMessage, helpedId);

            entry.setCurrentRequestClusterMessageForSimpleCluster(clusterMessage);

            entry.setRotationAbleFlag(false);
        }
        else{
            entry.setRotationAbleFlag(false);
            CustomGlobal.adjustNum --;
            System.out.println("Cluster Helper : adjustNum --, it can not start rotate, " +
                    " node : " +node.ID + " helpedId is " + helpedId + " , adjustNum is" + CustomGlobal.adjustNum);

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

        RequestClusterMessage highestRequest = entry.getCurrentRequestClusterMessageForSimpleCluster();

        if(highestRequest == null){
            return;
        }

        if(highestRequest.getTheEgoTreeIdOfClusterMaster() == helpedId){
            // if this is the master

            int clusterId = highestRequest.getRequesterId();

            entry.setCurrentClusterRequesterId(clusterId);
            // set current cluster requester id

            // remember that masterId equals to helpedId
            AcceptClusterMessage acceptClusterMessage = new AcceptClusterMessage(helpedId, largeId,
                    clusterId, highestRequest.getGenerateTime(), highestRequest);
            this.sendAcceptOrRejectMessage(node, entry, largeId, acceptClusterMessage, helpedId);
        }

    }



    // receive message part
    private void receiveRequestClusterMessage(RequestClusterMessage requestClusterMessage, SendEntry entry,
                                              Node node,  int helpedId){

        int largeId = requestClusterMessage.getLargeId();
        int clusterId = requestClusterMessage.getRequesterId();

        if(!entry.checkAdjustRequirement()){
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

                    System.out.println("Cluster Helper: Node"+ helpedId + ": The diff is " + diff);
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
                        entry.setCurrentRequestClusterMessageForSimpleCluster(requestClusterMessage);
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
                            entry.setCurrentRequestClusterMessageForSimpleCluster(requestClusterMessage);
                        }
                    }
                    else{
                        // position == 1 && root == false
                        // 往后发送后，(浅拷贝？)这里也会变吧，看看怎么处理还是没有影响。
                        this.sendRequestClusterMessageUp(node, entry, largeId, requestClusterMessage, helpedId);
                        entry.setCurrentRequestClusterMessageForSimpleCluster(requestClusterMessage);
                    }
                }
            }
            else{
                this.setClusterMaster(helpedId, node.ID, requestClusterMessage);
                // 由于这里的3号结点并不真正参与旋转，所以不必检测其是否为root
                this.setUpperNodeId(requestClusterMessage, helpedId, node.ID, false);
                entry.setCurrentRequestClusterMessageForSimpleCluster(requestClusterMessage);
            }
        }
    }


    private void receiveAcceptClusterMessage(AcceptClusterMessage acceptClusterMessage, SendEntry entry,
                                             Node node, int helpedId){

        System.out.println("Node " + node.ID + " receive a Accept! In cluster layer!");

        int largeId = acceptClusterMessage.getLargeId();
        int clusterId = acceptClusterMessage.getClusterId(); //also the requester's id
        int masterId = acceptClusterMessage.getMasterId();

        int currentClusterId = entry.getCurrentClusterRequesterId();

        if(currentClusterId> 0 && currentClusterId != clusterId){
            // reject since this node is in a cluster already
            this.rejectRequest(entry, node, largeId, helpedId, clusterId, masterId);
            return;
        }

        if(!entry.checkAdjustRequirement()){
            this.rejectRequest(entry, node, largeId, helpedId, clusterId, masterId);
            return;
        }

        if(currentClusterId == clusterId){
            // 这里被执行到了 所以确实有隐形bug
            Tools.warning("Should not happen! How a node get the Accept ahead??");
            return;
        }

        RequestClusterMessage highestRequest = entry.getCurrentRequestClusterMessageForSimpleCluster();
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
                System.out.println("Start to rotate!!!!!!");
                RotationHelper rotationHelper = new RotationHelper();
                rotationHelper.rotation(acceptClusterMessage);
                CustomGlobal.adjustNum --;



                System.out.println("Rotate completed! ");

                // these two execute in rotation already
                entry.unlockUpdateHighestPriorityRequestPermission();
                entry.setCurrentClusterRequesterId(-3);



            }
            else{
                this.sendAcceptOrRejectMessage(node, entry, largeId, acceptClusterMessage, helpedId);
            }

            // 这里，我要Accept了！
            // 可是，我手上的请求怎么办？
            // 残忍的抛弃它们吧！
            // 毕设： 去掉他
//            List<RequestClusterMessage> listTmp = entry.getAllRequestClusterMessage();
//
//            for(RequestClusterMessage clusterMessage : listTmp){
//                this.rejectRequest(entry,node, largeId, helpedId, clusterMessage.getRequesterId(),
//                        clusterMessage.getTheEgoTreeIdOfClusterMaster());
//            }

        }
        else{
            this.rejectRequest(entry, node, largeId, helpedId, clusterId, masterId);
        }
    }


    private void receiveRejectClusterMessage(RejectClusterMessage rejectClusterMessage, SendEntry entry,
                                             Node node, int helpedId){


        int largeId = rejectClusterMessage.getLargeId();
        int clusterId = rejectClusterMessage.getClusterId();
        int masterId = rejectClusterMessage.getMasterId();
        boolean upward = rejectClusterMessage.isUpward();
        // 既然被否决，那就移除吧。

        boolean continueSend = false;

        RequestClusterMessage requestClusterMessage = entry.getCurrentRequestClusterMessageForSimpleCluster();

        if(requestClusterMessage == null){
            return;
        }

        if(requestClusterMessage.getLargeId() == largeId && requestClusterMessage.getRequesterId() == clusterId){
            entry.setCurrentRequestClusterMessageForSimpleCluster(null);
            continueSend = true;
        }


        if(helpedId == clusterId){
            CustomGlobal.adjustNum --;
            entry.setRotationAbleFlag(false);
            System.out.println("Delete Process: adjustNum -- since receive a RejectClusterMessage" +
                    " whose clusterId == this.helpedId, adjustNum is" + CustomGlobal.adjustNum);
            continueSend = false;
        }

        if(entry.getCurrentClusterRequesterId() == clusterId){
            // 所在的cluster被否决，退出并允许新的highest Priority Request出现
            entry.setCurrentClusterRequesterId(-3);
            entry.unlockUpdateHighestPriorityRequestPermission();
        }

        // 继续传
        System.out.println("Node " + node.ID + " receive a RejectClusterMessage for " + helpedId + " " +
                "to reject " + clusterId + "'s request, and it will " + (continueSend?" ":" not ") + " continue send");

        if(continueSend) {
            if (!upward) {
                // 当前结点是否是requester的判断
                if (helpedId != clusterId) {
                    // 这一条 NonAck 还没传递到目的地，需要继续传递
                    RejectClusterMessage rejectClusterMessage2 = new RejectClusterMessage
                            (masterId, largeId, clusterId, false);

                    this.sendAcceptOrRejectMessage(node, entry, largeId, rejectClusterMessage2, helpedId);
                }
                else{
                    CustomGlobal.adjustNum -- ;
                    entry.setRotationAbleFlag(false);
                    System.out.println("Cluster Helper: adjustNum -- since receive a RejectClusterMessage2-----+++++ " +
                            " whose clusterId == this.helpedId, adjustNum is" + CustomGlobal.adjustNum);

                }
            } else {
                // 当前结点是否是master
                if (helpedId != masterId) {
                    // 不是master， 继续向上传
                    RejectClusterMessage rejectClusterMessage2 = new RejectClusterMessage
                            (masterId, largeId, clusterId, true);

                    this.sendAcceptOrRejectMessage(node, entry, largeId, rejectClusterMessage2, helpedId);
                }
            }
        }

    }





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

        System.out.println("Cluster Helper: Node " + node.ID + " send a" +
                " " + acceptOrRejectBaseMessage.getClass().getSimpleName() + " to " + targetId);

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
        // 虽然上面的结点接受聚合，但是本结点处还有更高优先级的请求要同意，所以当前的结点不接受聚合
        if(helpedId == clusterId){
            // 自己的，别管了
            return;
        }

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
        requestClusterMessage.setTheEgoTreeIdOfClusterMaster(egoTreeId);
        requestClusterMessage.setTheSendIdOfClusterMaster(sendId);
    }

    private void setUpperNodeId(RequestClusterMessage requestClusterMessage, int egoTreeId, int sendId,boolean lNFlag){
        requestClusterMessage.setTheMostUpperNodeEgoTreeId(egoTreeId);
        requestClusterMessage.setTheMostUpperNodeSendId(sendId);
        requestClusterMessage.setLnFlag(lNFlag);
    }





}

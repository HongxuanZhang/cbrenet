package projects.cbrenet.nodes.routeEntry;

import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.SDNMessage.DeleteMessage;
import projects.cbrenet.nodes.messages.SDNMessage.LargeInsertMessage;
import projects.cbrenet.nodes.messages.controlMessage.RequestClusterMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeleteConfirmMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeletePrepareMessage;
import sinalgo.tools.Tools;

import java.util.*;

public class SendEntry {


    // todo 如果是 root结点被删除的话，看看会不会有什么影响。。
    boolean egoTreeRoot; // when this is true, then the parent is the LN of the Ego-Tree
    public boolean isEgoTreeRoot() {
        return egoTreeRoot;
    }
    public void setEgoTreeRoot(boolean egoTreeRoot) {
        this.egoTreeRoot = egoTreeRoot;
    }


    int egoTreeIdOfParent;
    int sendIdOfParent;  // Used for Forward Message

    int egoTreeIdOfLeftChild;
    int sendIdOfLeftChild;

    int egoTreeIdOfRightChild;
    int sendIdOfRightChild;




    // TODO rotation part
    // 旋转位
    boolean rotationAbleFlag;
    public boolean isRotationAbleFlag() {
        return rotationAbleFlag;
    }
    public void setRotationAbleFlag(boolean rotationAbleFlag) {
        this.rotationAbleFlag = rotationAbleFlag;
    }

    // Count base network weight.
    int weight;  // 包含自身和子树
    int counter; // 以自身为 src or dst 的 Message数目

    // 第一次想要cluster时就出现，直到cluster被满足。
    RequestClusterMessage clusterMessage;
    PriorityQueue<RequestClusterMessage> requestClusterMessagePriorityQueue;


    public void setClusterMessage(RequestClusterMessage clusterMessage) {
        this.clusterMessage = clusterMessage;
    }

    public RequestClusterMessage getClusterMessage() {
        return clusterMessage;
    }

    // 当前自己已经倾心的Cluster， 除非收到NonAck 或者cluster结束才更新的
    int currentClusterRequesterId;

    public int getCurrentClusterRequesterId() {
        return currentClusterRequesterId;
    }

    public void setCurrentClusterRequesterId(int currentClusterRequesterId) {
        this.currentClusterRequesterId = currentClusterRequesterId;
    }

    boolean updateHighestPriorityRequestPermission;

    // 需要保存并且被处理的高优先级request
    // 在收到Ack时进行更新, 或者是自身是一个到来的request的master时允许
    RequestClusterMessage highestPriorityRequest;
    // 如果确认加入某一个cluster，锁定该项
    public void lockUpdateHighestPriorityRequestPermission(){
        this.updateHighestPriorityRequestPermission = false;
    }
    // 如果因为通知或者cluster完毕，解锁
    public void unlockUpdateHighestPriorityRequestPermission(){
        this.updateHighestPriorityRequestPermission = true;
    }



    public boolean checkRotationRequirement(){
        // todo add in delete part,
        // 不仅自己认为自己有希望得到一个cluster， （注意：：：自己可以已经在一个cluster中，只要等到所有都收到就行

        if(this.egoTreeRoot){
            return false; // 结点都是Ego-Tree的root了还想着去哪呢。。
        }



        return true;

    }


    public void addRequestClusterMessageIntoPriorityQueue(RequestClusterMessage requestClusterMessage){
        this.requestClusterMessagePriorityQueue.add(requestClusterMessage);
    }

    public RequestClusterMessage getRequestClusterMessageFromPriorityQueue(){
        if(this.requestClusterMessagePriorityQueue.isEmpty()){
            return null;
        }
        return this.requestClusterMessagePriorityQueue.peek(); // 不删除！
    }

    public void deleteCorrespondingRequestClusterMessageInPriorityQueue(int largeId, int clusterId){
        this.requestClusterMessagePriorityQueue.removeIf(
                requestClusterMessage -> requestClusterMessage.getLargeId() == largeId
                        && requestClusterMessage.getRequesterId() == clusterId);
    }


    public void updateHighestPriorityRequest(){
        /*
         *@description  get the new highest priority request when it may change!
         *@parameters  []
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/23
         */
        if(this.updateHighestPriorityRequestPermission){
            this.highestPriorityRequest = this.getRequestClusterMessageFromPriorityQueue();
        }
    }

    public void setHighestPriorityRequest(RequestClusterMessage highestPriorityRequest) {
        this.highestPriorityRequest = highestPriorityRequest;
    }

    public RequestClusterMessage getHighestPriorityRequest() {
        return highestPriorityRequest;
    }






    // rotation part end






    // Message Queue Part
    Queue<RoutingMessage> routingMessageQueue;

    public Queue<RoutingMessage> getRoutingMessageQueue() {
        return routingMessageQueue;
    }
    public void addMessageIntoRoutingMessageQueue(RoutingMessage routingMessage){
        this.routingMessageQueue.add(routingMessage);
    }
    public void addAllMessageIntoRoutingMessageQueue(Queue<RoutingMessage> routingMessageQueue){
        this.routingMessageQueue.addAll(routingMessageQueue);
    }

    private boolean queueEmpty = true;
    public boolean isQueueEmpty() {
        return queueEmpty;
    }
    public void setQueueEmpty(boolean queueEmpty) {
        this.queueEmpty = queueEmpty;
    }
    // Message Queue Part end







    // Constructor
    public SendEntry(int egoTreeIdOfParent, int egoTreeIdOfLeftChild, int egoTreeIdOfRightChild){
        this.egoTreeIdOfParent = egoTreeIdOfParent;
        this.egoTreeIdOfLeftChild = egoTreeIdOfLeftChild;
        this.egoTreeIdOfRightChild = egoTreeIdOfRightChild;

        this.sendIdOfParent = egoTreeIdOfParent;
        this.sendIdOfLeftChild = egoTreeIdOfLeftChild;
        this.sendIdOfRightChild = egoTreeIdOfRightChild;

        this.egoTreeRoot = false;
        this.rotationAbleFlag = true;

        this.sendFlagOfParent = true;
        this.sendFlagOfLeftChild = true;
        this.sendFlagOfRightChild = true;

        this.routingMessageQueue = new LinkedList<>();

        this.deletePrepareMessagePriorityQueue = new PriorityQueue<>();

        this.gotConfirmMessageMap = new HashMap<>();
        this.confirmMessageList = new ArrayList<>();
        this.auxiliaryId = -1;


        this.deleteFlag = true;
        this.deletingFlagOfItSelf = false;
        this.deletingFlagOfParent = false;
        this.deletingFlagOfLeftChild = false;
        this.deletingFlagOfRightChild = false;

        this.weight = 0;
        this.counter = 0;


        this.clusterMessage = null;
        this.requestClusterMessagePriorityQueue = new PriorityQueue<>();

        this.currentClusterRequesterId = -1;
        this.updateHighestPriorityRequestPermission = true;
        this.highestPriorityRequest = null;
    }






    // Send Flag, may changed in Delete Phase
    boolean sendFlagOfParent;
    boolean sendFlagOfLeftChild;
    boolean sendFlagOfRightChild;
    // Send Flag part end








    // Delete Phase field

    // auxiliaryID, will use the BST need AN or AN need a BST come back to Ego-Tree.
    int auxiliaryId;

    // Set this when receive Delete Message
    public void setAuxiliaryId(DeleteMessage correspondingDeleteMessage){
        this.auxiliaryId = correspondingDeleteMessage.getAuxiliaryNodeId();
    }

    public void setAuxiliaryId(LargeInsertMessage correspondingLargeInsertMessage){
        // only AN can use this to set auxiliary ID
        this.auxiliaryId = correspondingLargeInsertMessage.getTarget();
    }

    public int getAuxiliaryId(){
        return this.auxiliaryId;
    }
    // auxiliary Id part end


    // 控制是否发送deletePrepareMessage.
    HashMap<Integer, Boolean> gotConfirmMessageMap;
    List<DeleteConfirmMessage> confirmMessageList;

    public void receiveConfirmMessage(DeleteConfirmMessage confirmMessage){
        this.confirmMessageList.add(confirmMessage);
    }

    // call it when send DFM
    public void clearConfirmMessageList(){
        this.confirmMessageList.clear();
    }



    public boolean whetherReceivedConfirmMessageFrom(int egoTreeId){
        return this.gotConfirmMessageMap.getOrDefault(egoTreeId, false);
    }

    private boolean whetherReceivedConfirmMessageFromTheList(int egoTreeId){
        for(DeleteConfirmMessage confirmMessage : this.confirmMessageList){
            if(confirmMessage.getSrcId() == egoTreeId){
                return true;
            }
        }
        return false;
    }

    public void initGotMap(){
        /*
         *@description Call this when start delete or try to send DPM
         *@parameters  []
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/21
         */
        this.gotConfirmMessageMap = new HashMap<>();

        List<Integer> ids = this.getAllSendIds();
        for(int id : ids){
            this.gotConfirmMessageMap.put(id, this.whetherReceivedConfirmMessageFromTheList(id));
        }

    }

    public boolean checkNeighborDeleting(){
        /**
         *@description  Call this method when try to send DPM.
         *@parameters  []
         *@return  boolean
         *@author  Zhang Hongxuan
         *@create time  2021/3/21
         */
        return (!this.deletingFlagOfParent) && (!this.deletingFlagOfRightChild) && (!this.deletingFlagOfLeftChild);
    }


    boolean deleteFlag;
    // when delete flag is ture, it means the node would try to delete itself every round
    // and would not accept adjust.


    PriorityQueue<DeletePrepareMessage> deletePrepareMessagePriorityQueue;

    // Call when receive DPM
    public void receiveOrSetDeletePrepareMessage(DeletePrepareMessage deletePrepareMessage){
        //  这个地方可能是不管用的，可能需要自己再做处理，但是这个逻辑是没问题的
        if(this.deletePrepareMessagePriorityQueue.contains(deletePrepareMessage)){
            // contains 里面用的是 equal,应该没有问题！
            return;
        }
        this.deletePrepareMessagePriorityQueue.add(deletePrepareMessage);
    }

    // Every round
    public DeletePrepareMessage getDeletePrepareMessageFromPriorityQueue(){
        if(this.requestClusterMessagePriorityQueue.isEmpty()){
            return null;
        }
        return this.deletePrepareMessagePriorityQueue.poll();
    }

    private void deleteCorrespondingDeletePrepareMessageInPriorityQueue(int largeId, int deleteTarget){
        this.deletePrepareMessagePriorityQueue.removeIf(
                deletePrepareMessage -> deletePrepareMessage.getLargeId() == largeId
                        && deletePrepareMessage.getDeleteTarget() == deleteTarget);
    }



    boolean deletingFlagOfItSelf;
    // 该field 用于 防止重复的启动 startDelete
    // 发送DPM后设置 为 true， 当收到所有期望的DeleteConfirmMessage后置为false
    // 不能用于指示DPM是否需要重新发送！

    DeletePrepareMessage deletePrepareMessageOfMine = null;


    // 这些是结点用于自己告诉自身，自己的邻居在删除，如果自己有删除需求的话，连DPM都不用发的，直接等就是了
    boolean deletingFlagOfParent;
    boolean deletingFlagOfLeftChild;
    boolean deletingFlagOfRightChild;
    // end



    // 下面这个函数，仅在Confirm时调用。不能用于其他目的！
    public DeletePrepareMessage getDeletePrepareMessageOfMine() {
        return this.deletePrepareMessageOfMine;
        // 下面这一部分的代码是用来进行三项并行删除的，但是三项并行删除很难实现，问题有点大，不好做，先放一放
//        List<DeletePrepareMessage> results = new LinkedList<>();
//        if(this.deletePrepareMessageOfMine != null){
//            results.add(this.deletePrepareMessageOfMine);
//            return results;
//        }
//        else{
//            if(this.deletePrepareMessageOfParent != null){
//                results.add(this.deletePrepareMessageOfParent);
//            }
//
//            if(this.deletePrepareMessageOfLeftChild != null){
//                results.add(this.deletePrepareMessageOfLeftChild);
//            }
//
//            if(this.deletePrepareMessageOfRightChild != null){
//                results.add(this.deletePrepareMessageOfRightChild);
//            }
//
//            return results;
//        }
    }


    public void setDeletePrepareMessageOfMine(DeletePrepareMessage deletePrepareMessageOfMine) {
        this.deletePrepareMessageOfMine = deletePrepareMessageOfMine;
        // 下面这一部分的代码是用来进行三项并行删除的，但是三项并行删除很难实现，问题有点大，不好做，先放一放
//        char relation = this.getRelationShipOf(target);
//
//        if(relation == 'w'){
//            // 说明是自己发送的，要设置到自己身上
//            int numTmp = 0;
//
//            DeletePrepareMessage[] deletePrepareMessages = {this.deletePrepareMessageOfParent, this.deletePrepareMessageOfRightChild,
//                    this.deletePrepareMessageOfLeftChild};
//
//
//            for(int i = 0; i<3;i++){
//                // 快速方式，说明之前已经有比不过的了
//                if(numTmp < i){
//                    break;
//                }
//                DeletePrepareMessage message = deletePrepareMessages[i];
//                if(message != null){
//                    if(i == 0){
//                        if(deletePrepareMessageOfMine.compareTo(message) <= 0){
//                            numTmp += 1;
//                        }
//                    }
//                    else{
//                        // 与两个孩子结点的请求比，如果相等，需要优先执行孩子结点的请求
//                        if(deletePrepareMessageOfMine.compareTo(message) < 0){
//                            numTmp += 1;
//                        }
//                    }
//                }
//                else{
//                    numTmp += 1;
//                }
//            }
//
//
//            if(numTmp == 3){
//                this.deletePrepareMessageOfMine = deletePrepareMessageOfMine;
//
//                this.deletePrepareMessageOfParent = null;
//                this.deletePrepareMessageOfLeftChild = null;
//                this.deletePrepareMessageOfRightChild = null;
//            }
//        }
//        else{
//            //'r' 'l' 'p'
//            boolean setFlag = false;
//            if(this.deletePrepareMessageOfMine != null){
//                if(relation == 'p'){
//                    if(deletePrepareMessageOfMine.compareTo(this.deletePrepareMessageOfMine) < 0){
//                        setFlag = true;
//                    }
//                }
//                else{
//                    if(deletePrepareMessageOfMine.compareTo(this.deletePrepareMessageOfMine) < 0){
//                        setFlag = true;
//                    }
//                }
//            }
//            else{
//                setFlag = true;
//            }
//
//            if(setFlag){
//                switch (relation){
//                    case 'p':
//                        this.deletePrepareMessageOfParent = deletePrepareMessageOfMine;
//                        break;
//                    case 'l':
//                        this.deletePrepareMessageOfLeftChild = deletePrepareMessageOfMine;
//                        break;
//                    case 'r':
//                        this.deletePrepareMessageOfRightChild = deletePrepareMessageOfMine;
//                        break;
//                }
//                this.deletePrepareMessageOfMine = null;
//            }
//        }
    }


    public boolean setDeleteConfirmMessage(DeleteConfirmMessage deleteConfirmMessage){
        int src = deleteConfirmMessage.getSrcId();
        if(this.gotConfirmMessageMap.containsKey(src)) {
            this.gotConfirmMessageMap.replace(src, false, true);
        }
        else{
            Tools.warning("The node receive a DeleteConfirmMessage but not from a node expected!");
        }

        // check whether all confirm message has reached the node.
        Set<Integer> ids = this.gotConfirmMessageMap.keySet();
        boolean gotAll = true;
        for(int id: ids){
            if(!this.gotConfirmMessageMap.get(id)){
                gotAll = false;
                break;
            }
        }
        return gotAll;
    }


    public void targetStartDeleting(int targetId){
        char relation = this.getRelationShipOf(targetId);
        switch (relation){
            case 'p':
                this.deletingFlagOfParent = true;
                this.sendFlagOfParent = false;
                break;
            case 'l':
                this.deletingFlagOfLeftChild = true;
                this.sendFlagOfLeftChild = false;
                break;
            case 'r':
                this.deletingFlagOfRightChild = true;
                this.sendFlagOfRightChild = false;
                break;
            default:
                break;
        }
    }

    public void targetDeletingFinish(int largeId, int targetId){
        /*
         *@description Call this method when the neighbor has not removed yet.
         *@parameters  [targetId]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/21
         */

        // 移除那个在Queue中的DPM
        this.deleteCorrespondingDeletePrepareMessageInPriorityQueue(largeId, targetId);

        // set deleting & send flag
        char relation = this.getRelationShipOf(targetId);
        switch (relation){
            case 'p':
                this.deletingFlagOfParent = false;
                this.sendFlagOfParent = true;
                break;
            case 'l':
                this.deletingFlagOfLeftChild = false;
                this.sendFlagOfLeftChild = true;
                break;
            case 'r':
                this.deletingFlagOfRightChild = false;
                this.sendFlagOfRightChild = true;
                break;
            default:
                Tools.warning("Please check what happen in unsetDeletingFlag");
                break;
        }
    }










    public int getSendIdOf(int egoTreeId){
        char relation = this.getRelationShipOf(egoTreeId);
        int result = -1;
        switch (relation){
            case 'w':
                result = -2;
                break;
            case 'p':
                result = this.sendIdOfParent;
                break;
            case 'l':
                result = this.sendIdOfLeftChild;
                break;
            case 'r':
                result = this.sendIdOfRightChild;
                break;
            default:
                result = -3;
                break;
        }
        return result;
    }

    public boolean getSendFlag(int egoTreeId){
        char relation = this.getRelationShipOf(egoTreeId);
        boolean result = false;
        switch (relation){
            case 'p':
                result = this.sendFlagOfParent;
                break;
            case 'l':
                result = this.sendFlagOfLeftChild;
                break;
            case 'r':
                result = this.sendFlagOfRightChild;
                break;
            default:
                break;
        }
        return result;
    }

    public char getRelationShipOf(int egoTreeId){
        /**
         *@description  Use egoTree id to get the relation between this id to the ego-tree-id's node
         *@parameters  [id]
         *             id must be ego-tree id. Auxiliary node's relation could be very complex
         *@return  char
         *@author  Zhang Hongxuan
         *@create time  2021/3/15
         */
        if(egoTreeId == this.egoTreeIdOfParent){
            return 'p';
        }
        else if(egoTreeId == this.egoTreeIdOfLeftChild){
            return 'l';
        }
        else if(egoTreeId == this.egoTreeIdOfRightChild){
            return 'r';
        }
        else{
            return 'w';
        }
    }




    // Getter & Setter

    public List<Integer> getAllSendIds(){
        List<Integer> results = new LinkedList<>();
        if(this.egoTreeIdOfParent > 0){
            results.add(this.egoTreeIdOfParent);
        }
        if(this.egoTreeIdOfLeftChild > 0){
            results.add(this.egoTreeIdOfLeftChild);
        }
        if(this.egoTreeIdOfRightChild > 0){
            results.add(this.egoTreeIdOfRightChild);
        }
        return results;
    }


    public void setSendIdOfParent(int sendIdOfParent) {
        this.sendIdOfParent = sendIdOfParent;
    }

    public void setSendIdOfLeftChild(int sendIdOfLeftChild) {
        this.sendIdOfLeftChild = sendIdOfLeftChild;
    }

    public void setSendIdOfRightChild(int sendIdOfRightChild) {
        this.sendIdOfRightChild = sendIdOfRightChild;
    }

    public void setEgoTreeIdOfParent(int egoTreeIdOfParent) {
        this.egoTreeIdOfParent = egoTreeIdOfParent;
    }

    public void setEgoTreeIdOfLeftChild(int egoTreeIdOfLeftChild) {
        this.egoTreeIdOfLeftChild = egoTreeIdOfLeftChild;
    }

    public void setEgoTreeIdOfRightChild(int egoTreeIdOfRightChild) {
        this.egoTreeIdOfRightChild = egoTreeIdOfRightChild;
    }

    public void setSendFlagOfParent(boolean sendFlagOfParent) {
        this.sendFlagOfParent = sendFlagOfParent;
    }

    public void setSendFlagOfLeftChild(boolean sendFlagOfLeftChild) {
        this.sendFlagOfLeftChild = sendFlagOfLeftChild;
    }

    public void setSendFlagOfRightChild(boolean sendFlagOfRightChild) {
        this.sendFlagOfRightChild = sendFlagOfRightChild;
    }

    public int getEgoTreeIdOfParent() {
        return egoTreeIdOfParent;
    }

    public int getEgoTreeIdOfLeftChild() {
        return egoTreeIdOfLeftChild;
    }

    public int getEgoTreeIdOfRightChild() {
        return egoTreeIdOfRightChild;
    }

    public boolean isDeleteFlag() {
        return deleteFlag;
    }

    public boolean isDeletingFlagOfItSelf() {
        return deletingFlagOfItSelf;
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public void setDeletingFlagOfItSelf(boolean deletingFlagOfItSelf) {
        this.deletingFlagOfItSelf = deletingFlagOfItSelf;
    }
}

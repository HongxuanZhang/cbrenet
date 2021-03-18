package projects.cbrenet.nodes.routeEntry;

import projects.cbrenet.nodes.messages.RoutingMessage;
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


    // TODO
    // 旋转位
    boolean rotationAbleFlag;
    public boolean isRotationAbleFlag() {
        return rotationAbleFlag;
    }
    public void setRotationAbleFlag(boolean rotationAbleFlag) {
        this.rotationAbleFlag = rotationAbleFlag;
    }




    // Queue
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
    // Queue Part.




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

        this.deleteFlag = true;
        this.deletingFlagOfMySelf = false;
        this.deletingFlagOfParent = false;
        this.deletingFlagOfLeftChild = false;
        this.deletingFlagOfRightChild = false;


    }



    // Send Flag, may changed in Delete Phase
    boolean sendFlagOfParent;
    boolean sendFlagOfLeftChild;
    boolean sendFlagOfRightChild;


    // Delete Phase field

    // 控制是否发送deletePrepareMessage.
    HashMap<Integer, Boolean> gotConfirmMessageMap = null;

    // call in start delete
    public void initGotMap(){
        List<Integer> ids = this.getAllSendIds();
        this.gotConfirmMessageMap = new HashMap<>();

        for(int id : ids){
            this.gotConfirmMessageMap.put(id, false);
        }
    }

    // call in start delete
    public boolean cheakNeighborDeleting(){
        return (!this.deletingFlagOfParent) && (!this.deletingFlagOfRightChild) && (!this.deletingFlagOfLeftChild);
    }


    boolean deleteFlag;

    boolean deletingFlagOfMySelf;
    // 发送后设置，当收到所有期望的DeleteConfirmMessage后删除？
    DeletePrepareMessage deletePrepareMessage = null;

    boolean deletingFlagOfParent;
    DeletePrepareMessage deletePrepareMessageOfParent = null;

    boolean deletingFlagOfLeftChild;
    DeletePrepareMessage deletePrepareMessageOfLeftChild = null;

    boolean deletingFlagOfRightChild;
    DeletePrepareMessage deletePrepareMessageOfRightChild = null;

    // 下面这个函数，仅在Confirm时调用。不能用于其他目的！
    public List<DeletePrepareMessage> getDeletePrepareMessage() {
        List<DeletePrepareMessage> results = new LinkedList<>();
        if(this.deletePrepareMessage != null){
            results.add(this.deletePrepareMessage);
            return results;
        }
        else{
            if(this.deletePrepareMessageOfParent != null){
                results.add(this.deletePrepareMessageOfParent);
            }

            if(this.deletePrepareMessageOfLeftChild != null){
                results.add(this.deletePrepareMessageOfLeftChild);
            }

            if(this.deletePrepareMessageOfRightChild != null){
                results.add(this.deletePrepareMessageOfRightChild);
            }

            return results;
        }
    }


    public void setDeletePrepareMessage(DeletePrepareMessage deletePrepareMessage) {
        int target = deletePrepareMessage.getDeleteTarget();

        char relation = this.getRelationShipTo(target);

        if(relation == 'w'){
            // 说明是自己发送的，要设置到自己身上
            int numTmp = 0;

            DeletePrepareMessage[] deletePrepareMessages = {this.deletePrepareMessageOfParent, this.deletePrepareMessageOfRightChild,
                    this.deletePrepareMessageOfLeftChild};


            for(int i = 0; i<3;i++){
                // 快速方式，说明之前已经有比不过的了
                if(numTmp < i){
                    break;
                }
                DeletePrepareMessage message = deletePrepareMessages[i];
                if(message != null){
                    if(i == 0){
                        if(deletePrepareMessage.compareTo(message) <= 0){
                            numTmp += 1;
                        }
                    }
                    else{
                        // 与两个孩子结点的请求比，如果相等，需要优先执行孩子结点的请求
                        if(deletePrepareMessage.compareTo(message) < 0){
                            numTmp += 1;
                        }
                    }
                }
                else{
                    numTmp += 1;
                }
            }


            if(numTmp == 3){
                this.deletePrepareMessage = deletePrepareMessage;

                this.deletePrepareMessageOfParent = null;
                this.deletePrepareMessageOfLeftChild = null;
                this.deletePrepareMessageOfRightChild = null;
            }
        }
        else{
            //'r' 'l' 'p'
            boolean setFlag = false;
            if(this.deletePrepareMessage != null){
                if(relation == 'p'){
                    if(deletePrepareMessage.compareTo(this.deletePrepareMessage) < 0){
                        setFlag = true;
                    }
                }
                else{
                    if(deletePrepareMessage.compareTo(this.deletePrepareMessage) < 0){
                        setFlag = true;
                    }
                }
            }
            else{
                setFlag = true;
            }

            if(setFlag){
                switch (relation){
                    case 'p':
                        this.deletePrepareMessageOfParent = deletePrepareMessage;
                        break;
                    case 'l':
                        this.deletePrepareMessageOfLeftChild = deletePrepareMessage;
                        break;
                    case 'r':
                        this.deletePrepareMessageOfRightChild = deletePrepareMessage;
                        break;
                }

                this.deletePrepareMessage = null;
            }

        }
    }

    public boolean setDeleteConfirmMessage(DeleteConfirmMessage deleteConfirmMessage){
        int src = deleteConfirmMessage.getSrcId();
        if(this.gotConfirmMessageMap.containsKey(src)) {
            this.gotConfirmMessageMap.replace(src, false, true);
        }
        else{
            Tools.warning("The node receive a DeleteConfirmMessage but not from a node expected!");
        }

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

    public void setDeletePrepareMessageOfParent(DeletePrepareMessage deletePrepareMessageOfParent) {
        this.deletePrepareMessageOfParent = deletePrepareMessageOfParent;
    }

    public void setDeletePrepareMessageOfLeftChild(DeletePrepareMessage deletePrepareMessageOfLeftChild) {
        this.deletePrepareMessageOfLeftChild = deletePrepareMessageOfLeftChild;
    }

    public void setDeletePrepareMessageOfRightChild(DeletePrepareMessage deletePrepareMessageOfRightChild) {
        this.deletePrepareMessageOfRightChild = deletePrepareMessageOfRightChild;
    }












    public int getSendIdOf(int egoTreeId){
        char relation = this.getRelationShipTo(egoTreeId);
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
        char relation = this.getRelationShipTo(egoTreeId);
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

    public char getRelationShipTo(int egoTreeId){
        /**
         *@description
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

    public boolean isDeletingFlagOfMySelf() {
        return deletingFlagOfMySelf;
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public void setDeletingFlagOfMySelf(boolean deletingFlagOfMySelf) {
        this.deletingFlagOfMySelf = deletingFlagOfMySelf;
    }

    public void setDeletingFlagOfParent(boolean deletingFlagOfParent) {
        this.deletingFlagOfParent = deletingFlagOfParent;
    }

    public void setDeletingFlagOfLeftChild(boolean deletingFlagOfLeftChild) {
        this.deletingFlagOfLeftChild = deletingFlagOfLeftChild;
    }

    public void setDeletingFlagOfRightChild(boolean deletingFlagOfRightChild) {
        this.deletingFlagOfRightChild = deletingFlagOfRightChild;
    }

    public boolean isDeletingFlagOfParent() {
        return deletingFlagOfParent;
    }

    public boolean isDeletingFlagOfLeftChild() {
        return deletingFlagOfLeftChild;
    }

    public boolean isDeletingFlagOfRightChild() {
        return deletingFlagOfRightChild;
    }

}

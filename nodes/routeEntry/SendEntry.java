package projects.cbrenet.nodes.routeEntry;

import projects.cbrenet.nodes.messages.RoutingMessage;
import projects.cbrenet.nodes.messages.deletePhaseMessages.DeletePrepareMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
            return 'w'; // means wrong
        }
    }




    // Getter & Setter

    public List<Integer> getAllSendIds(){
        List<Integer> results = new LinkedList<>();
        if(this.egoTreeIdOfParent > 0){
            results.add(this.sendIdOfParent);
        }
        if(this.egoTreeIdOfLeftChild > 0){
            results.add(this.sendIdOfLeftChild);
        }
        if(this.egoTreeIdOfRightChild > 0){
            results.add(this.sendIdOfRightChild);
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

    public DeletePrepareMessage getDeletePrepareMessage() {
        return deletePrepareMessage;
    }

    public boolean isDeletingFlagOfParent() {
        return deletingFlagOfParent;
    }

    public DeletePrepareMessage getDeletePrepareMessageOfParent() {
        return deletePrepareMessageOfParent;
    }

    public boolean isDeletingFlagOfLeftChild() {
        return deletingFlagOfLeftChild;
    }

    public DeletePrepareMessage getDeletePrepareMessageOfLeftChild() {
        return deletePrepareMessageOfLeftChild;
    }

    public boolean isDeletingFlagOfRightChild() {
        return deletingFlagOfRightChild;
    }

    public DeletePrepareMessage getDeletePrepareMessageOfRightChild() {
        return deletePrepareMessageOfRightChild;
    }
}

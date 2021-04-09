package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.tableEntry.Request;
import projects.cbrenet.nodes.messages.CbRenetMessage;
import sinalgo.configuration.Configuration;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class CommunicatePartnerLayer extends CommunicationNodeSDNLayer{


    // TODO 运行的时候注意一下这个玩意
    int parac;


    // todo 极低优先级 true access order
    private LinkedHashMap<Integer, Integer> smallNodes;
    private LinkedHashMap<Integer, Integer> largeNodes;


    public void setSmallNodesCp(List<Integer> smallNodeIds){
        /**
         *@description This method only use during create-ego-tree,
         *  when LN receive the EgoTreeMessage, since we can sure that at this time the small cp in
         *  the LN is exactly the small cp in SDN
         *  Actually smallCp & largeCp can be maintained by the SDN message, but this is a easy way.
         *@parameters  [smallNodeIds]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/8
         */
        this.smallNodes.clear();
        for(int id : smallNodeIds) {
            // Some node in the Ego-Tree may not put in the smallCp, since it would be removed very soon.
            if(this.isNodeSmall(id)){
                smallNodes.put(id, id);
            }
            else{
                Tools.warning("CPLayer: 有几个想加入Ego-Tree的结点，还是大的，说不定SDN忘了发送了");
            }
        }
    }


    // only use this when node is a large node of a ego-tree

    private boolean waitAllDeleteRequestMessage = false;

    public boolean isWaitAllDeleteRequestMessage() {
        return waitAllDeleteRequestMessage;
    }

    // 大结点变小时使用到， 当然也可能不会有大结点变小
    public void setWaitAllDeleteRequestMessage(){
        this.waitAllDeleteRequestMessage = true;
    }
    // 大结点删除后重置
    public void resetWaitAllDeleteRequestMessage(){
        this.waitAllDeleteRequestMessage = false;
    }




    // DRM related part
    private HashMap<Integer, Boolean> egoTreeDeleteMap;

    public void setEgoTreeDeleteMap(List<Integer> egoTreeNodeIds){
        this.egoTreeDeleteMap = new HashMap<>();
        for(int id : egoTreeNodeIds){
            this.addEgoTreeNodeToDeleteMap(id);
        }
    }

    public HashMap<Integer, Boolean> getEgoTreeDeleteMap() {
        return egoTreeDeleteMap;
    }

    private HashSet<Integer> unPreparedDeleteNode;
    //  only use in this Layer. only use to check whether all node
    // are prepared

    public void addEgoTreeNodeToDeleteMap(int smallNodeId){
        /**
         *@description Called it when receive LargeInsertMessage(inserted)
         *              It is ok that if a node's id in the Map but not in the CP
         *@parameters  [smallNodeId]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/3
         */
        if(this.egoTreeDeleteMap.containsKey(smallNodeId)){
            Tools.warning("CPLayer: Need to add small node id to " + this.ID + "'s ego-tree, but it already store in the egoTreeDeleteMap, " +
                    " please check whether it has been cleared last time!");
            return;
        }
        this.egoTreeDeleteMap.put(smallNodeId, false);
        this.unPreparedDeleteNode.add(smallNodeId);
    }

    public void removeEgoTreeNodeFromDeleteMap(int smallNodeId){
        /**
         *@description Call this method when the large node of ego-tree receive DRM
         *@parameters  [smallNodeId]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/4
         */
        if(this.egoTreeDeleteMap.containsKey(smallNodeId)){
            boolean ableToDelete = this.egoTreeDeleteMap.get(smallNodeId);
            if(ableToDelete){
                this.egoTreeDeleteMap.remove(smallNodeId);
            }
            else{
                Tools.fatalError("CPLayer: In removeEgoTreeNodeFromDeleteMap, the " +
                        "small node " + smallNodeId +" is not prepared to delete");
            }
        }
        else{
            Tools.fatalError("CPLayer: The small node id " + smallNodeId +" is not in the" +
                    " removeEgoTreeNodeFromDeleteMap");
        }
    }

    public void nodeInEgoTreeArePreparedToDelete(int smallNodeId){
        /**
         *@description Call when LN receive DRM
         *@parameters  [smallNodeId]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/3/4
         */

        if(this.egoTreeDeleteMap.containsKey(smallNodeId)){
            this.egoTreeDeleteMap.put(smallNodeId, true);
            this.unPreparedDeleteNode.remove(smallNodeId);
        }
        else{
            Tools.warning("CPLayer: The " + smallNodeId + " node are prepared to delete in the ego tree of " + this.ID + "" +
                    ", but it not contains in the egoTreeDeleteMap!");
        }
    }

    public boolean checkWhetherAllNodePrepareToDelete(){
        return this.unPreparedDeleteNode.isEmpty() && !this.egoTreeDeleteMap.keySet().isEmpty();
    }

    // DRM related part finished






    @Override
    public void init() {
        super.init();
        try{
            if (Configuration.hasParameter("parac")) {
                parac = Configuration.getIntegerParameter("parac");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Missing configuration parameters");
        }
        this.smallNodes = new LinkedHashMap<>(parac,0.75f,true);
        this.largeNodes = new LinkedHashMap<>(parac,0.75f, true);
        this.egoTreeDeleteMap = new HashMap<>();
        this.unPreparedDeleteNode = new HashSet<>();
    }


    public void removeCommunicationPartner(int id){
        if(smallNodes.containsKey(id)){
            this.smallNodes.remove(id);
        }
        else if(largeNodes.containsKey(id)){
            this.largeNodes.remove(id);
        }
        else{
            System.err.println("CPLayer: Want to remove an non-existing communication partner " + id);
        }
    }

    public void addCommunicationPartner(int id){
        boolean idLargeFlag = !this.isNodeSmall(id);
        if(idLargeFlag){
            if(!this.largeNodes.containsKey(id)) {
                this.largeNodes.put(id, id);
            }
            else{
                System.err.println("CPLayer: Want to add exist cp "+ id +" in to " + this.ID +"'s big cp");
            }
        }
        else{
            if(!this.smallNodes.containsKey(id)){
                this.smallNodes.put(id, id);
            }
            else{
                System.err.println("CPLayer: Want to add exist cp "+ id +" in to " + this.ID +"'s small cp");
            }
        }
    }

    public void addCommunicationPartner(int id, boolean largePartner){
        boolean idLargeFlag = !this.isNodeSmall(id);
        if(idLargeFlag != largePartner){
            Tools.fatalError("CPLayer: In " + this.ID + " CP Layer, " +
                    "the status of " + id + " the node know is different to the expected status");

        }

        if(largePartner){
            if(!this.largeNodes.containsKey(id)) {
                this.largeNodes.put(id, id);
            }
            else{
                System.err.println("CPLayer: Want to add exist cp "+ id +" in to " + this.ID +"'s cp");
            }
        }
        else{
            if(!this.smallNodes.containsKey(id)){
                this.smallNodes.put(id, id);
            }
            else{
                System.err.println("CPLayer: Want to add exist cp "+ id +" in to " + this.ID +"'s cp");
            }
        }

    }


    public void clearSomePartnersSinceSelfLargeFlagChange(boolean changeToSmall)
    {
        /**
         *@description
         *@parameters  [changeToSmall] :  related to delete which partner, equal to the smallStatus message 's small flag
         *             T : means the node would be changed from large to small, all partners should be deleted
         *             F : means the node would be large, so only delete the large partners
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/21
         */
        if(changeToSmall){
            // from large change to small
            this.largeNodes.clear();
            this.smallNodes.clear();
        }
        else{
            // from small change to large
            this.smallNodes.clear();

            // not clear largeNodes since DeleteRequestMessage has to sent after this.
            // remove large CP when DeleteRequestMessage sent. (In MessageQueueLayer)
        }
    }



    // todo 想想要不要删掉
    public void changeLargeFlag(){
        /**
         *@description this function is used to change a large node small or opposite
         *@parameters  []
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/6
         */
        // todo add communication partner part ?
        if(this.largeFlag){
            // make a large node small
            this.largeFlag = false;
            this.clearLargeIds();
            // add all large communicate partner as large ids
            this.addLargeIds(this.getCommunicateLargeNodes().keySet());
//            for(int largeId : this.largeNodes.keySet()){
//                this.addLargeIds(largeId);
//                CommunicatePartnerLayer partner = this.largeNodes.get(largeId);
//            }
        }
        else{
            // make a small node large
            this.largeFlag = true;
            this.clearLargeIds();
        }
    }


    // todo 收到Message的也更新一下哈
    public void updatePartnerOrder(Request request){
        /**
         *@description use this method when the sender send a message or a receiver receives a message
         *@parameters  [request]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/6
         */
        int Id1 = request.dstId;
        int Id2 = request.srcId;
        int p_id = this.ID == Id1 ? Id2 : Id1;
        if(smallNodes.containsKey(p_id)){
            smallNodes.get(p_id);
        }
        else{
            largeNodes.get(p_id);
        }
    }


    public LinkedHashMap<Integer, Integer> getCommunicateSmallNodes() {
        return smallNodes;
    }

    public LinkedHashMap<Integer, Integer> getCommunicateLargeNodes() {
        return largeNodes;
    }

    public boolean checkCommunicateSatisfaction(int src, int dst){
        if(src != this.ID) {
            Tools.fatalError("CPLayer: This method must be called in a wrong way, the parameter src must equal to the ID of the " +
                    "node");
            return false;
        }
        else return this.getCommunicateSmallNodes().containsKey(dst) || this.getCommunicateLargeNodes().containsKey(dst);
    }

    public boolean checkCommunicateSatisfaction(Request request){
        int src = request.srcId;
        int dst = request.dstId;
        return checkCommunicateSatisfaction(src,dst);
    }

    public boolean checkCommunicateSatisfaction(Message message) {
        int src;
        int dst;
        if(message instanceof CbRenetMessage){
            CbRenetMessage messageTmp = (CbRenetMessage) message;
            src = messageTmp.getSrc();
            dst = messageTmp.getDst();
        }
        else{
            Tools.fatalError("CPLayer: Message class MISSING! Add " + message.getClass() + " into MessageQueueLayer" );
            src = -100; // Whatever, make sure check won't return true;
            dst = -100;
        }
        return checkCommunicateSatisfaction(src,dst);
    }


}

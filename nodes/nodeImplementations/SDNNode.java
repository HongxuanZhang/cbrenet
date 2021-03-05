package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.SDNMessage.*;
import projects.cbrenet.nodes.messages.controlMessage.DeleteEgoTreeRequestMessage;
import projects.cbrenet.nodes.messages.controlMessage.DeleteRequestMessage;
import projects.cbrenet.nodes.messages.controlMessage.LargeInsertMessage;
import projects.cbrenet.nodes.tableEntry.Request;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * function of SDN is to control the Node big or small
 * Actually we just use sendDirect,
 * so there is no need to create links between SDN with other nodes
 * */

public class SDNNode extends Node {

    private int globalStatusId;
    // used as id in the StatusChangedMessage, and the LinkMessage would be executed by it.

    public int getGlobalStatusId() {
        return globalStatusId;
    }

    private void incrementGlobalStatusId(){
        this.globalStatusId ++;
    }

    private ArrayList<Integer> allNodeIds;


    // used to present the node of Integer id is big or small
    // true : small
    // false : big
    private HashMap<Integer, Boolean> smallStatusBits;

    // statistic
    private int largeNodeNum;

    private Queue<RPCSdnMessage> rpcQueue;


    private HashMap<Integer, Integer> cpNumber;

    // CP必须是对称的！！
    private HashMap<Integer, HashSet<Integer>> cp_smallNodes;
    private HashMap<Integer, HashSet<Integer>> cp_largeNodes;

    private int constC;
    private int threshold;

    public SDNNode(int constC){
        this.init();
        this.constC = constC;
        this.threshold = 4 * constC;
    }

    @Override
    public void init() {

        this.rpcQueue = new LinkedList<>();

        this.allNodeIds = new ArrayList<>();

        this.cpNumber = new HashMap<>();
        this.cp_smallNodes = new HashMap<>();
        this.cp_largeNodes = new HashMap<>();

        this.constC = 0;
        this.threshold = 0;


        this.largeNodeNum = 0;

        this.globalStatusId = 0;
    }

    public void addNodeId(int id){
        this.allNodeIds.add(id);
    }

    public ArrayList<Integer> getAllNodeIds() {
        return allNodeIds;
    }

    private boolean breakLargeThreshold(int num){
        return this.threshold < num;
    }

    public void receiveMessage(Message msg) {
        if (msg instanceof RPCSdnMessage) {
            this.rpcQueue.add((RPCSdnMessage) msg);
            return;
        }
    }

    @Override
    public void handleMessages(Inbox inbox) {
        for (Message message : inbox) {
            if (message instanceof RPCSdnMessage) {
                RPCSdnMessage sdnMessage = (RPCSdnMessage) message;
                this.receiveMessage(sdnMessage);
                // todo
            }
            else if(message instanceof RequestMessage){
                Request request = ((RequestMessage) message).unsatisfiedRequest;
                Request wantToRemove = ((RequestMessage) message).wantToRemove;

                if(wantToRemove != null){
                    if(this.checkRequest(wantToRemove, false)){

                        this.dealWithRemoveRequest(wantToRemove);
                        // notify both node to add each self
                        // msg to src
                        ArrayList<Integer> targetSrc = new ArrayList<Integer>();
                        targetSrc.add(request.dstId);
                        LinkMessage msgSrc = new LinkMessage(request.srcId, false, targetSrc,
                                this.globalStatusId);
                        sendDirect(msgSrc, Tools.getNodeByID(request.srcId));

                        // msg to dst
                        ArrayList<Integer> targetDst = new ArrayList<Integer>();
                        targetDst.add(request.srcId);
                        LinkMessage msgDst = new LinkMessage(request.dstId, false, targetDst,
                                this.globalStatusId);
                        sendDirect(msgDst, Tools.getNodeByID(request.dstId));
                    }
                    else{
                        // no need to execute
                    }
                }

                if(request != null){

                    if(this.checkRequest(request, true)){
                        int srcId = request.srcId;
                        int dstId = request.dstId;

                        HashSet<Integer> srcCorrespondingCpSet = this.smallStatusBits.get(dstId) ?
                                this.cp_smallNodes.get(srcId) : this.cp_largeNodes.get(srcId);
                        HashSet<Integer> dstCorrespondingCpSet = this.smallStatusBits.get(srcId) ?
                                this.cp_smallNodes.get(dstId) : this.cp_largeNodes.get(dstId);

                        // deal with cp and cp_num
                        this.dealWithRequest(request);

                        // old status message, may contradict to the cp number but it is OK
                        boolean smallFlagForSrc = this.smallStatusBits.get(srcId);
                        boolean smallFlagForDst = this.smallStatusBits.get(dstId);

                        int cp_src = this.cpNumber.getOrDefault(srcId, 0);
                        int cp_dst = this.cpNumber.getOrDefault(dstId, 0);
                        if(smallFlagForDst && smallFlagForSrc){

                            if(this.breakLargeThreshold(cp_src) || this.breakLargeThreshold(cp_dst)) {
                                // at least one of them need to be large

                                int needToChangeLargeNumber = 0;
                                // 这里可能隐藏着Bug,理论上，下面两个会处理好彼此所在的partner set，但是也有可能处理不好
                                if(this.breakLargeThreshold(cp_src)){
                                    this.changeToLarge(srcId);
                                    needToChangeLargeNumber ++;
                                }
                                if(this.breakLargeThreshold(cp_dst)){
                                    this.changeToLarge(dstId);
                                    needToChangeLargeNumber ++;
                                }
                                switch (needToChangeLargeNumber){
                                    case 0:
                                        Tools.fatalError("Some thing wrong happen in the SDN about dealing with Request");
                                        break;
                                    case 1:
                                        if(this.breakLargeThreshold(cp_src)){
                                            // src node need to be large
                                            createEgoTree(srcId);
                                        }
                                        else{
                                            // dst node need to be large
                                            createEgoTree(dstId);
                                        }
                                        break;
                                    case 2:
                                        createEgoTree(srcId);
                                        createEgoTree(dstId);

                                        // 满足一下两个大结点的通讯需要
                                        //dst, insertFlag, targets, uniqueStatusId
                                        ArrayList<Integer> targets1 = new ArrayList<>();
                                        targets1.add(dstId);
                                        LinkMessage linkMessage1 = new LinkMessage(srcId, true, targets1, this.globalStatusId);



                                        ArrayList<Integer> targets2 = new ArrayList<>();
                                        targets2.add(srcId);
                                        LinkMessage linkMessage2 = new LinkMessage(dstId, true, targets2, this.globalStatusId);


                                        sendDirect(linkMessage1, Tools.getNodeByID(srcId));
                                        sendDirect(linkMessage2, Tools.getNodeByID(dstId));
                                        break;
                                }


                            }
                            else{
                                // both nodes are small.
                                // notify both node to add each self
                                // msg to src
                                ArrayList<Integer> targetSrc = new ArrayList<Integer>();
                                targetSrc.add(request.dstId);
                                LinkMessage msgSrc = new LinkMessage(request.srcId, true, targetSrc, this.globalStatusId);
                                sendDirect(msgSrc, Tools.getNodeByID(request.srcId));

                                // msg to dst
                                ArrayList<Integer> targetDst = new ArrayList<Integer>();
                                targetDst.add(request.srcId);
                                LinkMessage msgDst = new LinkMessage(request.dstId, true, targetDst, this.globalStatusId);
                                sendDirect(msgDst, Tools.getNodeByID(request.dstId));
                            }
                        }
                        else{
                            // 在此之前，已经有一个大了，另一个是小，也有可能是大，小的那个也有可能变大！
                            int largeNum = 0;
                            if(!smallFlagForSrc){
                                largeNum ++;
                            }
                            else{
                                if(this.breakLargeThreshold(cp_src)){
                                    this.changeToLarge(srcId);
                                    largeNum++;
                                    this.createEgoTree(srcId);
                                }
                            }

                            if(!smallFlagForDst){
                                largeNum ++;
                            }
                            else{
                                if(this.breakLargeThreshold(cp_dst)){
                                    this.changeToLarge(dstId);
                                    largeNum++;
                                    this.createEgoTree(dstId);
                                }
                            }

                            switch (largeNum){
                                case 2:
                                    ArrayList<Integer> targets1 = new ArrayList<>();
                                    targets1.add(dstId);
                                    LinkMessage linkMessage1 = new LinkMessage(srcId, true, targets1, this.globalStatusId);
                                    sendDirect(linkMessage1, Tools.getNodeByID(srcId));

                                    ArrayList<Integer> targets2 = new ArrayList<>();
                                    targets2.add(srcId);
                                    LinkMessage linkMessage2 = new LinkMessage(dstId, true, targets2, this.globalStatusId);
                                    sendDirect(linkMessage2, Tools.getNodeByID(dstId));
                                    break;
                                case 1:
                                    // Insert Message
                                    LargeInsertMessage largeInsertMessage;
                                    if(!smallFlagForSrc){
                                        // src node is large, and dst node is small
                                        largeInsertMessage = new LargeInsertMessage(dstId, srcId);
                                        sendDirect(largeInsertMessage, Tools.getNodeByID(srcId));
                                    }
                                    else{
                                        // dst node is large, and src node is small
                                        largeInsertMessage = new LargeInsertMessage(srcId, dstId);
                                        sendDirect(largeInsertMessage, Tools.getNodeByID(dstId));
                                    }
                                    break;
                                case 0:
                                    Tools.warning("There should be at least one large node, but do not!");
                                    break;
                            }

                        }
                    }
                    else{
                        // no need to execute
                    }
                }
            }
            else if(message instanceof DeleteRequestMessage){
                DeleteRequestMessage deleteRequestMessage = (DeleteRequestMessage) message;
                if(deleteRequestMessage.isEgo_tree()){
                    // ready to delete
                    int wantToDeleteId = deleteRequestMessage.getWantToDeleteId();
                    int largeId = deleteRequestMessage.getDst();

                    // send DeleteMessage not allFlag to the wantToDelete ego-tree node
                    //int dst, int largeId
                    DeleteMessage deleteMessage = new DeleteMessage(wantToDeleteId, largeId);
                    this.sendDirect(deleteMessage, Tools.getNodeByID(wantToDeleteId));

                }
                else{
                    // not prepared to delete, should send to LN to tell LN remove CP
                    int dst = deleteRequestMessage.getDst(); // which is the large node

                }
            }
            else if(message instanceof LargeInsertMessage){
                // checked!
                LargeInsertMessage largeInsertMessageTmp = (LargeInsertMessage) message;

                int largeNodeId = largeInsertMessageTmp.getLargeId();
                if(!largeInsertMessageTmp.isInserted())
                {
                    Tools.fatalError("The LargeInsertMessage received by SDN node should have inserted bit set!");
                }

                this.sendDirect(largeInsertMessageTmp, Tools.getNodeByID(largeNodeId));
            }
            else if(message instanceof DeleteEgoTreeRequestMessage){
                DeleteEgoTreeRequestMessage deleteEgoTreeRequestMessage = (DeleteEgoTreeRequestMessage) message;
                HashSet<Integer> egoTreeNodeIds = deleteEgoTreeRequestMessage.getEgoTreeIds();

                int largeId = deleteEgoTreeRequestMessage.getLargeId();
                
                for(int wantToDeleteId : egoTreeNodeIds){
                    //int dst, boolean allFlag, int largeId
                    DeleteMessage deleteMessage = new DeleteMessage(wantToDeleteId, true ,largeId);
                    this.sendDirect(deleteMessage, Tools.getNodeByID(wantToDeleteId));
                }
            }

        }
    }


    // add route

    private StatusChangedMessage getNewStatusChangeMessage(int id, boolean smallFlag){
        this.incrementGlobalStatusId();
        StatusChangedMessage changedMessage = new StatusChangedMessage(id, smallFlag, this.globalStatusId);
        this.broadCastToAllNode(changedMessage);
        return changedMessage;
    }

    public void changeToLarge(int id){
        /**
         *@description change all information need to maintain in the SDN node
         *@parameters  [id]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/20
         */
        this.smallStatusBits.replace(id, false);

        this.largeNodeNum++;

//        this.incrementGlobalStatusId();
//        StatusChangedMessage changedMessage = new StatusChangedMessage(id, false, this.globalStatusId);
//        this.broadCastToAllNode(changedMessage);

        this.getNewStatusChangeMessage(id, false);

        // then only notify its partner in SDN
        HashSet<Integer> smallPartner = this.cp_smallNodes.get(id);
        for(int small_id : smallPartner){
            HashSet<Integer> oldSmall = this.cp_smallNodes.get(small_id);
            oldSmall.remove(id);
            this.cp_smallNodes.replace(small_id, oldSmall);

            HashSet<Integer> oldLarge = this.cp_largeNodes.get(small_id);
            oldLarge.add(id);
            this.cp_largeNodes.replace(small_id, oldLarge);
        }

        HashSet<Integer> largePartner = this.cp_largeNodes.get(id);
        for(int large_id : largePartner){
            // remove from large_id node's small partner
            HashSet<Integer> oldSmall = this.cp_smallNodes.get(large_id);
            oldSmall.remove(id);
            this.cp_smallNodes.replace(large_id, oldSmall);

            // add into large_id node's large partner
            HashSet<Integer> oldLarge = this.cp_largeNodes.get(large_id);
            oldLarge.add(id);
            this.cp_largeNodes.replace(large_id, oldLarge);
        }
    }

    public void changeToSmall(int id){
        this.smallStatusBits.replace(id, true);

        this.largeNodeNum--;

        this.getNewStatusChangeMessage(id, true);
//
//        this.incrementGlobalStatusId();
//        StatusChangedMessage changedMessage = new StatusChangedMessage(id,true, this.globalStatusId);
//        this.broadCastToAllNode(changedMessage);


        // then only notify its partner in SDN
        HashSet<Integer> smallPartner = this.cp_smallNodes.get(id);
        for(int small_id : smallPartner){
            // remove from small_id node's large partner
            HashSet<Integer> oldSmall = this.cp_largeNodes.get(small_id);
            oldSmall.remove(id);
            this.cp_largeNodes.replace(small_id, oldSmall);

            // add into small_id node's small partner
            HashSet<Integer> oldLarge = this.cp_smallNodes.get(small_id);
            oldLarge.add(id);
            this.cp_smallNodes.replace(small_id, oldLarge);
        }

        HashSet<Integer> largePartner = this.cp_largeNodes.get(id);
        for(int large_id : largePartner){
            HashSet<Integer> oldSmall = this.cp_largeNodes.get(large_id);
            oldSmall.remove(id);
            this.cp_largeNodes.replace(large_id, oldSmall);

            HashSet<Integer> oldLarge = this.cp_smallNodes.get(large_id);
            oldLarge.add(id);
            this.cp_smallNodes.replace(large_id, oldLarge);
        }
    }


    public Queue<RPCSdnMessage> getRpcQueue() {
        return rpcQueue;
    }


    private boolean checkRequest(Request request, boolean needToSatisfy){
        int srcId = request.srcId;
        int dstId = request.dstId;

        HashSet<Integer> srcCorrespondingCpSet = this.smallStatusBits.get(dstId) ?
                this.cp_smallNodes.get(srcId) : this.cp_largeNodes.get(srcId);
        HashSet<Integer> dstCorrespondingCpSet = this.smallStatusBits.get(srcId) ?
                this.cp_smallNodes.get(dstId) : this.cp_largeNodes.get(dstId);

        // check
        // This is necessary because same request may send twice
        if(needToSatisfy){
            if(srcCorrespondingCpSet.contains(dstId)  && dstCorrespondingCpSet.contains(srcId)){
                Tools.warning("The node send a  request but " + srcId + " and " + dstId + " already exists!");
                return false;
            }
        }
        else{
            // TODO 这里的判断条件或许存在问题，看后续情况决定怎么改吧
            if(!srcCorrespondingCpSet.contains(dstId)  || !dstCorrespondingCpSet.contains(srcId)){
                Tools.fatalError("The node send a wantToRemove request but " + srcId + " and " + dstId + " are " +
                        "not partners!");
                // Todo actually we need to make sure that the node send the message correctly based on the edge connection.
                return false;
            }
        }
        return true;
    }


    private boolean dealWithRequest(Request request){
        /**
         *@description Call this method AFTER the status change finished!!
         *@parameters  [request]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/19
         */
        int srcId = request.srcId;
        int dstId = request.dstId;

        HashSet<Integer> srcCorrespondingCpSet = this.smallStatusBits.get(dstId) ?
                this.cp_smallNodes.get(srcId) : this.cp_largeNodes.get(srcId);
        HashSet<Integer> dstCorrespondingCpSet = this.smallStatusBits.get(srcId) ?
                this.cp_smallNodes.get(dstId) : this.cp_largeNodes.get(dstId);

        // check
        // This is necessary because same request may send twice
        if(srcCorrespondingCpSet.contains(dstId)  && dstCorrespondingCpSet.contains(srcId)){
            Tools.fatalError("The node send a  request but " + srcId + " and " + dstId + " already exists!");
            return false;
        }

        if(srcCorrespondingCpSet.contains(dstId)  || dstCorrespondingCpSet.contains(srcId)){
            Tools.fatalError("The node send a request but one of it " + srcId + " and " + dstId + " already exists! " +
                    "Which should never happen!!!!");
            return false;
        }

        srcCorrespondingCpSet.add(dstId);
        dstCorrespondingCpSet.add(srcId);

        if(this.smallStatusBits.get(dstId)){
            this.cp_smallNodes.replace(srcId, srcCorrespondingCpSet);
        }
        else{
            this.cp_largeNodes.replace(srcId, srcCorrespondingCpSet);
        }

        if(this.smallStatusBits.get(srcId)){
            this.cp_smallNodes.replace(dstId, dstCorrespondingCpSet);
        }
        else{
            this.cp_largeNodes.replace(dstId, dstCorrespondingCpSet);
        }


        // deal with cp num
        int srcCpNum = this.cpNumber.get(srcId);
        int dstCpNum = this.cpNumber.get(dstId);
        srcCpNum++;
        dstCpNum++;
        this.cpNumber.replace(srcId, srcCpNum);
        this.cpNumber.replace(dstId, dstCpNum);

        return true;
    }


    private boolean dealWithRemoveRequest(Request wantToRemove){
        /**
         *@description Call this method BEFORE status change
         *@parameters  [wantToRemove]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/19
         */
        int srcId = wantToRemove.srcId;
        int dstId = wantToRemove.dstId;

        HashSet<Integer> srcCorrespondingCpSet = this.smallStatusBits.get(dstId) ?
                this.cp_smallNodes.get(srcId) : this.cp_largeNodes.get(srcId);
        HashSet<Integer> dstCorrespondingCpSet = this.smallStatusBits.get(srcId) ?
                this.cp_smallNodes.get(dstId) : this.cp_largeNodes.get(dstId);

        // check
        // This is necessary because same request may send twice
        if(!srcCorrespondingCpSet.contains(dstId)  || !dstCorrespondingCpSet.contains(srcId)){
            Tools.fatalError("The node send a wantToRemove request but " + srcId + " and " + dstId + " are " +
                    "not partners!");
            // Todo actually we need to make sure that the node send the message correctly based on the edge connection.
            return false;
        }

        srcCorrespondingCpSet.remove(dstId);
        dstCorrespondingCpSet.remove(srcId);

        if(this.smallStatusBits.get(dstId)){
            this.cp_smallNodes.replace(srcId, srcCorrespondingCpSet);
        }
        else{
            this.cp_largeNodes.replace(srcId, srcCorrespondingCpSet);
        }

        if(this.smallStatusBits.get(srcId)){
            this.cp_smallNodes.replace(dstId, dstCorrespondingCpSet);
        }
        else{
            this.cp_largeNodes.replace(dstId, dstCorrespondingCpSet);
        }


        // deal with cp num
        int srcCpNum = this.cpNumber.get(srcId);
        int dstCpNum = this.cpNumber.get(dstId);
        srcCpNum--;
        dstCpNum--;
        this.cpNumber.replace(srcId, srcCpNum);
        this.cpNumber.replace(dstId, dstCpNum);

        return true;
    }


    private void broadCastToAllNode(Message message){
        for(int id : this.allNodeIds){
            sendDirect(message, Tools.getNodeByID(id));
        }
    }


    // create ego tree part
    private void createEgoTree(int largeNodeId){
        HashSet<Integer> small_cp = this.cp_smallNodes.getOrDefault(largeNodeId, null);
        if(small_cp == null){
            Tools.fatalError("In createEgoTree of SDN, the small partner of " + largeNodeId + " is null" );
            return;
        }

        List<Integer> smallCpList = new ArrayList<>(small_cp);
        smallCpList.sort(Comparator.naturalOrder());


        int len = smallCpList.size();
        if(len == 0){
            return;
        }
        int mid = len/2;

        int egoTreeRoot = smallCpList.get(mid);

        ArrayList<Integer> targets = new ArrayList<>();
        targets.add(egoTreeRoot);

        ArrayList<Character> relationships = new ArrayList<>();
        relationships.add('t');

        LinkMessage linkMessage = new LinkMessage(largeNodeId, true, largeNodeId, targets, relationships,
                this.globalStatusId);
        this.sendDirect(linkMessage, Tools.getNodeByID(largeNodeId));

        createEgoTree(smallCpList, largeNodeId);

    }

    class Pair{
        public int l = -1;
        public int r = -1;
    }


    private void createEgoTree(List<Integer> nodes, int largeNodeId){

        /**
         *@description
         *@parameters  [nodes, largeNodeId], nodes must be sorted in natural order!
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/28
         */

        int len = nodes.size();
        if(len == 0){
            return;
        }
        int mid = len/2;

        int parent = nodes.get(mid);

        List<Integer> nodes1 = nodes.subList(0, mid);
        List<Integer> nodes2 = nodes.subList(mid+1, len);

        Pair p =new Pair();

        getLeftChildAndRightChild(nodes1, nodes2, p);

        System.out.println(parent + "\t" + p.l + "\t" + p.r);
        this.sendEgoTreeLinkMessage(parent, p.l, p.r, largeNodeId);

        createEgoTree(nodes1, largeNodeId);
        createEgoTree(nodes2, largeNodeId);
    }

    private void sendEgoTreeLinkMessage(int parent, int l, int r, int largeNodeId){
        /**
         *@description send LinkMessage to create the ego-tree
         *@parameters  [parent, l, r] parent is parent, which would receive the LinkMessage, l : leftChild, r : rightChild
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/28
         */
        ArrayList<Integer> targets = new ArrayList<>();
        ArrayList<Character> relationships = new ArrayList<>();
        boolean targetsIsNull = true;
        if(l != -1){
            targets.add(l);
            relationships.add('l');
            targetsIsNull = false;
        }
        if(r != -1){
            targets.add(r);
            relationships.add('r');
            targetsIsNull = false;
        }
        if(!targetsIsNull){
            //dst, insertFlag, largeId, targets, relationships, statusId
            LinkMessage linkMessage = new LinkMessage(parent, true, largeNodeId, targets, relationships,
                    this.globalStatusId);
            this.sendDirect(linkMessage, Tools.getNodeByID(parent));
        }
    }

    private void getLeftChildAndRightChild(List<Integer> nodes1, List<Integer> nodes2, Pair pair){
        int len1 = nodes1.size();
        int len2 = nodes2.size();
        if(len1 == 0){
            pair.l = -1;
        }
        else{
            pair.l = nodes1.get(len1/2);
        }

        if(len2 == 0){
            pair.r = -1;
        }
        else{
            pair.r = nodes2.get(len2/2);
        }
    }

    // create ego tree part finished!



    // need not to modify
    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        // String text = ID + " l:" + this.minIdInSubtree + " r:" + this.maxIdInSubtree;
        String text = "SDN Node: " + ID;// + ": " + this.getWeight();

        // draw the node as a circle with the text inside
        super.drawNodeAsDiskWithText(g, pt, highlight, text, 14, Color.magenta);
    }

    @Override
    public void preStep() {

    }


    @Override
    public void neighborhoodChange() {
        // nothing to do since would never change
    }

    @Override
    public void postStep() {

    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {

    }

}
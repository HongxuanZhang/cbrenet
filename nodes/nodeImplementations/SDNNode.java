package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.messages.SDNMessage.*;
import projects.cbrenet.nodes.messages.controlMessage.DeleteEgoTreeRequestMessage;
import projects.cbrenet.nodes.messages.controlMessage.DeleteRequestMessage;
import projects.cbrenet.nodes.messages.SDNMessage.LargeInsertMessage;
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
 * Actually we just use send direct,
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


    private int auxiliaryNodeId;

    public void setAuxiliaryNodeId(int auxiliaryNodeId) {
        this.auxiliaryNodeId = auxiliaryNodeId;
    }

    // used to present the node of Integer id is big or small
    // true : small
    // false : big
    public HashMap<Integer, Boolean> smallStatusBits;

    private HashMap<Integer, Integer> cpNumber;


    // statistic
    private int largeNodeNum;



    // CP必须是对称的！！
    private HashMap<Integer, HashSet<Integer>> cp_smallNodes;
    private HashMap<Integer, HashSet<Integer>> cp_largeNodes;

    private int constC;
    private int threshold;

    public SDNNode(int constC){
        this.init();
        this.constC = constC;
        this.threshold = 4 * constC;

        System.out.println("The SDN's configuration : constC :" +constC+ "," +
                "threshold: " + threshold);

    }

    public void setConstCAndThreshold(int constC){
        this.constC = constC;
        this.threshold = 4*constC;
    }


    @Override
    public void init() {
        // these three need to initialize in the network creating.
        this.smallStatusBits = new HashMap<>();
        this.allNodeIds = new ArrayList<>();
        this.cpNumber = new HashMap<>();


        this.cp_smallNodes = new HashMap<>();
        this.cp_largeNodes = new HashMap<>();

        this.constC = 0;
        this.threshold = 0;


        this.largeNodeNum = 0;

        this.globalStatusId = 0;
    }

    public void addCommunicateNodeId(int id){
        this.allNodeIds.add(id);
        this.smallStatusBits.put(id, true);
        this.cpNumber.put(id,0);

        this.cp_smallNodes.put(id, new HashSet<>());
        this.cp_largeNodes.put(id, new HashSet<>());
    }

    public ArrayList<Integer> getAllNodeIds() {
        return allNodeIds;
    }

    private boolean breakLargeThreshold(int num){
        System.out.println("The test number in breakLargeThreshold: " + num);
        return num > this.threshold ;
    }

    public void receiveMessage(Message msg) {

    }

    public void sendMessage(int id, Message message){
        this.sendDirect(message, Tools.getNodeByID(id));
        System.out.println("SDN: Send a " + message.getClass() + " to" +
                " " + id + ", the content is : " + message.toString());
    }

    @Override
    public void handleMessages(Inbox inbox) {
        for (Message message : inbox) {
            if(message instanceof RequestMessage){
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
                        this.sendMessage(request.srcId, msgSrc);

                        // msg to dst
                        ArrayList<Integer> targetDst = new ArrayList<Integer>();
                        targetDst.add(request.srcId);
                        LinkMessage msgDst = new LinkMessage(request.dstId, false, targetDst,
                                this.globalStatusId);
                        this.sendMessage(request.dstId, msgSrc);
                    }
                    else{
                        // no need to execute
                        System.out.println("SDN: A request has been satisfied ! ");
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
                                        Tools.fatalError("SDN: Some thing wrong happen in the SDN about dealing with Request");
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

                                        this.sendMessage(srcId, linkMessage1);
                                        this.sendMessage(dstId, linkMessage2);

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


                                // msg to dst
                                ArrayList<Integer> targetDst = new ArrayList<>();
                                targetDst.add(request.srcId);
                                LinkMessage msgDst = new LinkMessage(request.dstId, true, targetDst, this.globalStatusId);

                                this.sendMessage(srcId, msgSrc);
                                this.sendMessage(dstId, msgDst);

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

                                    ArrayList<Integer> targets2 = new ArrayList<>();
                                    targets2.add(srcId);
                                    LinkMessage linkMessage2 = new LinkMessage(dstId, true, targets2, this.globalStatusId);

                                    this.sendMessage(srcId, linkMessage1);
                                    this.sendMessage(dstId, linkMessage2);

                                    break;
                                case 1:
                                    // Insert Message
                                    LargeInsertMessage largeInsertMessage;
                                    if(!smallFlagForSrc){
                                        // src node is large, and dst node is small
                                        largeInsertMessage = new LargeInsertMessage(dstId, srcId, this.globalStatusId);
                                        this.sendMessage(srcId, largeInsertMessage);

                                    }
                                    else{
                                        // dst node is large, and src node is small
                                        largeInsertMessage = new LargeInsertMessage(srcId, dstId, this.globalStatusId);
                                        this.sendMessage(dstId, largeInsertMessage);
                                    }
                                    break;
                                case 0:
                                    Tools.warning("SDN: There should be at least one large node, but do not!");
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
                    int largeId = deleteRequestMessage.getSrc();

                    // send DeleteMessage not allFlag to the wantToDelete ego-tree node
                    //int dst, int largeId
                    DeleteMessage deleteMessage = new DeleteMessage(wantToDeleteId, largeId, this.globalStatusId, this.auxiliaryNodeId);
                    this.sendMessage(wantToDeleteId, deleteMessage);
                }
                else{
                    // not prepared to delete, should send to LN to tell LN remove CP
                    // this could remove actually
                }
            }
            else if(message instanceof LargeInsertMessage){
                // checked!
                LargeInsertMessage largeInsertMessageTmp = (LargeInsertMessage) message;

                int largeNodeId = largeInsertMessageTmp.getLargeId();
                if(!largeInsertMessageTmp.isInserted())
                {
                    Tools.fatalError("SDN: The LargeInsertMessage received by SDN node should have inserted bit set!");
                }
                this.sendMessage(largeNodeId, largeInsertMessageTmp);
            }
            else if(message instanceof DeleteEgoTreeRequestMessage){
                DeleteEgoTreeRequestMessage deleteEgoTreeRequestMessage = (DeleteEgoTreeRequestMessage) message;
                HashSet<Integer> egoTreeNodeIds = deleteEgoTreeRequestMessage.getEgoTreeIds();

                int largeId = deleteEgoTreeRequestMessage.getLargeId();
                
                for(int wantToDeleteId : egoTreeNodeIds){
                    //int dst, boolean allFlag, int largeId
                    DeleteMessage deleteMessage = new DeleteMessage(wantToDeleteId, true ,largeId,
                            this.globalStatusId, this.auxiliaryNodeId);
                    this.sendMessage(wantToDeleteId, deleteMessage);
                }
            }

        }
    }


    // add route

    private StatusChangedMessage getNewStatusChangeMessage(int id, boolean smallFlag){
        /*
         *@description Generate a new SCM and broadcast it !
         *@parameters  [id, smallFlag]
         *@return  projects.cbrenet.nodes.messages.SDNMessage.StatusChangedMessage
         *@author  Zhang Hongxuan
         *@create time  2021/4/5
         */

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

        //this.incrementGlobalStatusId();
        //StatusChangedMessage changedMessage = new StatusChangedMessage(id, false, this.globalStatusId);
        //this.broadCastToAllNode(changedMessage);

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

        // Also add L-L link here
        HashSet<Integer> largePartner = this.cp_largeNodes.get(id);
        for(int large_id : largePartner){
            // remove from large_id node's small partner, fixme but we did not send link message to create link!
            HashSet<Integer> oldSmall = this.cp_smallNodes.get(large_id);
            oldSmall.remove(id);
            this.cp_smallNodes.replace(large_id, oldSmall);

            // add into large_id node's large partner
            HashSet<Integer> oldLarge = this.cp_largeNodes.get(large_id);
            oldLarge.add(id);
            this.cp_largeNodes.replace(large_id, oldLarge);


            // create L-L link
            ArrayList<Integer> targets1 = new ArrayList<>();
            targets1.add(id);
            LinkMessage linkMessage1 = new LinkMessage(large_id, true, targets1, this.globalStatusId);

            ArrayList<Integer> targets2 = new ArrayList<>();
            targets2.add(large_id);
            LinkMessage linkMessage2 = new LinkMessage(id, true, targets2, this.globalStatusId);

            this.sendMessage(large_id, linkMessage1);
            this.sendMessage(id, linkMessage2);

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


    private boolean checkRequest(Request request, boolean needToSatisfy){


        int srcId = request.srcId;
        int dstId = request.dstId;

        if(srcId == 11 && dstId == 15){
            int ssadk = 1;
        }

        System.out.println("SDN : Checking request from " + srcId + " to " + dstId);
        HashSet<Integer> srcCorrespondingCpSet = this.smallStatusBits.get(dstId) ?
                this.cp_smallNodes.get(srcId) : this.cp_largeNodes.get(srcId);
        HashSet<Integer> dstCorrespondingCpSet = this.smallStatusBits.get(srcId) ?
                this.cp_smallNodes.get(dstId) : this.cp_largeNodes.get(dstId);

        if(srcCorrespondingCpSet == null){
            return true;
        }

        // check
        // This is necessary because same request may send twice
        if(needToSatisfy){
            if(srcCorrespondingCpSet.contains(dstId)  && dstCorrespondingCpSet.contains(srcId)){
                System.out.println("SDN: The node send a  request but " + srcId + " and " + dstId + " already exists!");
                return false;
            }
            return true;
        }
        else{
            if(!srcCorrespondingCpSet.contains(dstId)  || !dstCorrespondingCpSet.contains(srcId)){
                Tools.fatalError("SDN: The node send a wantToRemove request but " + srcId + " and " + dstId + " are " +
                        "not partners!");
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

        if(srcCorrespondingCpSet == null){
            srcCorrespondingCpSet = new HashSet<>();
        }
        if(dstCorrespondingCpSet == null){
            dstCorrespondingCpSet = new HashSet<>();
        }

        // check
        // This is necessary because same request may send twice
        if(srcCorrespondingCpSet.contains(dstId)  && dstCorrespondingCpSet.contains(srcId)){
            Tools.fatalError("SDN: The node send a  request but " + srcId + " and " + dstId + " already exists!");
            return false;
        }

        if(srcCorrespondingCpSet.contains(dstId)  || dstCorrespondingCpSet.contains(srcId)){
            Tools.fatalError("SDN: The node send a request but one of it " + srcId + " and " + dstId + " already exists! " +
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

        System.out.println(srcCpNum);
        System.out.println(dstCpNum);

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
            Tools.fatalError("SDN: The node send a wantToRemove request but " + srcId + " and " + dstId + " are " +
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


    private void broadCastToAllNode(StatusChangedMessage message){
        for(int id : this.allNodeIds){
            this.sendMessage(id, message);
        }
    }


    // create ego tree part
    private void createEgoTree(int largeNodeId){
        if(largeNodeId == 49){
            int djioasd = 2;
        }

        HashSet<Integer> small_cp = this.cp_smallNodes.getOrDefault(largeNodeId, null);
        if(small_cp == null){
            Tools.fatalError("SDN: In createEgoTree of SDN, the small partner of " + largeNodeId + " is null" );
            return;
        }

        List<Integer> smallCpList = new ArrayList<>(small_cp);
        smallCpList.sort(Comparator.naturalOrder());

        EgoTreeMessage egoTreeMessage = new EgoTreeMessage(smallCpList, this.globalStatusId);


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

        this.sendMessage(largeNodeId, linkMessage);

        System.out.println("Send Link Message to " + largeNodeId + " " +
                "" + linkMessage.toString());
        // Ego Tree Message , contains the node which SDN send linkMessage to.
        this.sendMessage(largeNodeId, egoTreeMessage);

        createEgoTree(smallCpList, largeNodeId, largeNodeId);

    }

    class Pair{
        public int l = -1;
        public int r = -1;
    }


    private void createEgoTree(List<Integer> nodes, int largeNodeId, int parent){

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

        int curNode = nodes.get(mid);

        List<Integer> nodes1 = nodes.subList(0, mid);
        List<Integer> nodes2 = nodes.subList(mid+1, len);

        Pair p =new Pair();

        getLeftChildAndRightChild(nodes1, nodes2, p);

        System.out.println(curNode + "\t" + p.l + "\t" + p.r);
        this.sendEgoTreeLinkMessage(curNode, parent, p.l, p.r, largeNodeId);

        createEgoTree(nodes1, largeNodeId, curNode);
        createEgoTree(nodes2, largeNodeId, curNode);
    }

    private void sendEgoTreeLinkMessage(int curNodeId, int p, int l, int r, int largeNodeId){
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
        if(p > 0){
            targets.add(p);
            relationships.add('p');
            targetsIsNull = false;
        }
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
            LinkMessage linkMessage = new LinkMessage(curNodeId, true, largeNodeId, targets, relationships,
                    this.globalStatusId);
            if(p == largeNodeId){
                linkMessage.setParentIsLnFlag();
            }
            this.sendMessage(curNodeId, linkMessage);
            Tools.appendToOutput("Send Link Message to " + curNodeId + " " +
                    "" + linkMessage.toString());
            System.out.println("Send Link Message to " + curNodeId + " " +
                    "" + linkMessage.toString());
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

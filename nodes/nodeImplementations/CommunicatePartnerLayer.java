package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.tableEntry.Request;
import projects.cbrenet.nodes.messages.CbRenetMessage;
import projects.cbrenet.nodes.messages.SDNMessage.RPCSdnMessage;
import projects.cbrenet.nodes.messages.SDNMessage.RequestMessage;
import sinalgo.configuration.Configuration;
import sinalgo.tools.Tools;

import java.util.LinkedHashMap;

public abstract class CommunicatePartnerLayer extends CommunicationNodeSDNLayer{


    // TODO 运行的时候注意一下这个玩意
    int parac;



    // todo 极低优先级 access order
    private LinkedHashMap<Integer, Integer> smallNodes;
    private LinkedHashMap<Integer, Integer> largeNodes;


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
    }


    public void removeCommunicationPartner(int id){
        if(smallNodes.containsKey(id)){
            this.smallNodes.remove(id);
        }
        else if(largeNodes.containsKey(id)){
            this.largeNodes.remove(id);
        }
        else{
            Tools.fatalError("Want to remove an non-existing communication partner " + id);
        }
    }

    public void addCommunicationPartner(int id){
        boolean idLargeFlag = !this.isNodeSmall(id);
        if(idLargeFlag){
            if(!this.largeNodes.containsKey(id)) {
                this.largeNodes.put(id, id);
            }
            else{
                Tools.warning("Want to add exist cp "+ id +" in to " + this.ID +"'s co");
            }
        }
        else{
            if(!this.smallNodes.containsKey(id)){
                this.smallNodes.put(id, id);
            }
            else{
                Tools.warning("Want to add exist cp "+ id +" in to " + this.ID +"'s co");
            }
        }
    }

    public void clearPartners(boolean mode)
    {
        /**
         *@description
         *@parameters  [mode] :  related to delete which partner, equal to the smallStatus message 's small flag
         *             T : means the node would be changed from large to small, all partners should be deleted
         *             F : means the node would be large, so only delete the large partners
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/21
         */
        this.largeNodes.clear();
        if(mode){
            this.smallNodes.clear();
        }

    }

    // TODO 这两可能没啥用
    public void changeLargePartnerToSmall(int id){
        if(this.largeNodes.containsKey(id)){
            this.largeNodes.remove(id);
        }
    }

    public void changeSmallPartnerToLarge(int id){
        if(this.smallNodes.containsKey(id)){
            this.smallNodes.remove(id);
        }
    }
    // TODO 要不考虑删除




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
}

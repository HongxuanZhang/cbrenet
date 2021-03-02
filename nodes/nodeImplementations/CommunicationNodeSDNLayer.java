package projects.cbrenet.nodes.nodeImplementations;


import projects.cbrenet.nodes.messages.SDNMessage.RequestMessage;
import projects.cbrenet.nodes.tableEntry.Request;
import sinalgo.tools.Tools;

public abstract class CommunicationNodeSDNLayer extends LargeSmallNodeLayer{

    private int SDNId = -1;

    public void setSDNId(int sdnId){
        this.SDNId = sdnId;
    }

    public int getSDNId(){
        return this.SDNId;
    }

    public void tellSDNUnsatisfiedRequest(Request request){
        /**
         *@description call this method to tell SDN to make sure the request can be satisfied!
         *@parameters  [request]
         *@return  void
         *@author  Zhang Hongxuan
         *@create time  2021/2/6
         */
        int dst = request.dstId;
        RequestMessage requestMessage = new RequestMessage(request);
        this.sendDirect(requestMessage, Tools.getNodeByID(this.getSDNId()));
    }

}

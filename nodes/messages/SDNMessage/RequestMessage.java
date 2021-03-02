package projects.cbrenet.nodes.messages.SDNMessage;

import projects.cbrenet.nodes.tableEntry.Request;
import sinalgo.nodes.messages.Message;

public class RequestMessage extends Message {

    public Request unsatisfiedRequest;
    public Request wantToRemove;
    
    
    public RequestMessage(Request unsatisfiedRequest){
        this.unsatisfiedRequest = unsatisfiedRequest;
        this.wantToRemove = null;
    }

    public RequestMessage(Request unsatisfiedRequest, Request wantToRemove){
        /**
         *@description 
         *@parameters  [unsatisfiedRequest could be null, wantToRemove]
         *@return  
         *@author  Zhang Hongxuan
         *@create time  2021/2/28
         */
        this.unsatisfiedRequest = unsatisfiedRequest;
        this.wantToRemove = wantToRemove;
    }

    @Override
    public Message clone() {
        return null;
    }
}

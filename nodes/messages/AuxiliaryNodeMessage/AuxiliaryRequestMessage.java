package projects.cbrenet.nodes.messages.AuxiliaryNodeMessage;

import sinalgo.nodes.messages.Message;

public class AuxiliaryRequestMessage extends Message {

    int helpedId;

    int largeId;

    int egoTreeId_p;
    int egoTreeId_l;
    int egoTreeId_r;

    int sendId_p;
    int sendId_l;
    int sendId_r;


    public AuxiliaryRequestMessage(int helpedId, int largeId,
                                   int egoTreeId_p, int egoTreeId_l, int egoTreeId_r,
                                   int sendId_p, int sendId_l, int sendId_r){
        this.helpedId = helpedId;
        this.largeId = largeId;

        this.egoTreeId_p = egoTreeId_p;
        this.egoTreeId_l = egoTreeId_l;
        this.egoTreeId_r = egoTreeId_r;
        this.sendId_p = sendId_p;
        this.sendId_l = sendId_l;
        this.sendId_r = sendId_r;

    }



    @Override
    public Message clone() {
        return null;
    }


    // Getter & Setter

    public int getHelpedId() {
        return helpedId;
    }

    public int getLargeId() {
        return largeId;
    }

    public int getEgoTreeId_p() {
        return egoTreeId_p;
    }

    public int getEgoTreeId_l() {
        return egoTreeId_l;
    }

    public int getEgoTreeId_r() {
        return egoTreeId_r;
    }


    public int getSendId_p() {
        return sendId_p;
    }

    public int getSendId_l() {
        return sendId_l;
    }

    public int getSendId_r() {
        return sendId_r;
    }
}

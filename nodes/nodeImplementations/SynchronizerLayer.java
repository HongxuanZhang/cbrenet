package projects.cbrenet.nodes.nodeImplementations;

/**
 * SynchronizerLayer TODO : maybe we want to change the network to be synchronized
 */
public abstract class SynchronizerLayer{

    private int MAX_TIMESLOT = 12;
    private int timeslot;
    private long round;

    public int getCurrentTimeSlot() {
        return this.timeslot;
    }

    public long getCurrentRound() {
        return this.round;
    }

//    @Override
//    public void init() {
////        super.init();
//        this.timeslot = 0;
//    }

//    @Override
    public void postStep() {
        
        switch (this.timeslot) {
        case 0:
            updateState();
            timeslot0();
            break;

        case 1:
            timeslot1();
            break;

        case 2:
            timeslot2();
            break;

        case 3:
            timeslot3();
            break;

        case 4:
            timeslot4();
            break;

        case 5:
            timeslot5();
            break;

        case 6:
            timeslot6();
            break;

        case 7:
            timeslot7();
            break;

        case 8:
            timeslot8();
            break;

        case 9:
            timeslot9();
            break;

        case 10:
        	timeslot10();
        	break;

        case 11:
            timeslot11();
            posRound();
            this.round++;
            break;

        default:
            break;
        }
        this.timeslot = (this.timeslot + 1) % MAX_TIMESLOT;

        this.nodeStep();
    }

    public void updateState() {

    }

    public void timeslot0() {

    }

    public void timeslot1() {

    } 
    
    public void timeslot2() {

    } 

    public void timeslot3() {

    } 

    public void timeslot4() {

    } 
    
    public void timeslot5() {

    }

    public void timeslot6() {

    } 

    public void timeslot7() {

    } 

    public void timeslot8() {

    }

    public void timeslot9() {

    } 
    
    public void timeslot10() {
    	
    }

    public void timeslot11() {

    }

    // LOG
    public void posRound() {
        
    }

    public void nodeStep() {

    }

}
package projects.cbrenet.nodes.timers;

import projects.cbrenet.CustomGlobal;
import projects.cbrenet.nodes.nodeImplementations.CBNetNode;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;

/**
 * TimerExponentialDistribution
 */
public class TriggerNodeOperation extends Timer {

    public int src;
    public int dst;

    public TriggerNodeOperation(int src, int dst) {
        this.src = src;
        this.dst = dst;
    }

    @Override
    public void fire() {

        CBNetNode srcnode = (CBNetNode) Tools.getNodeByID(src);
        srcnode.newMessage(dst);

        CustomGlobal.mustGenerateSplay = true;
    }

}
package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.tableEntry.CBInfo;
import projects.cbrenet.nodes.tableEntry.CBRenetNodeInfo;

/**
 * CBTreeLayer -> CBTreeWeightLayer
 */
public abstract class CBTreeWeightLayer extends CbRenetBinarySearchTreeLayer {

    private long weight;

    @Override
    public void init() {
        super.init();
        this.weight = 0;
    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public void incrementWeight() {
        this.weight++;
    }

    public void updateWeights(int largeId,int from, int to) {
        CBTreeWeightLayer aux = this;
        
        if (!(aux.getParent(largeId) == null)) {
	        while (!(aux.getParent(largeId) == null)) {
	        	if (aux.isAncestorOf(largeId, from) && aux.isAncestorOf(largeId, to)) {
	        		aux.incrementWeight();
	        		aux.incrementWeight();
	        	}
	        	aux = (CBTreeWeightLayer) aux.getParent(largeId);
			} 
	        
	        if (aux.isAncestorOf(largeId, from) && aux.isAncestorOf(largeId, to)) {
        		aux.incrementWeight();
        		aux.incrementWeight();
        	}
        }
		aux.incrementWeight();
    }

    @Override
    public CBInfo getNodeInfo() {
        // todo , 此中查询出的NodeInfo要强制转换为（CBTreeWeightLayer）??
        return new CBInfo(this, this.getParents(), this.getLeftChildren(),
                this.getRightChildren(), this.getMinIdInSubtrees(), this.getMaxIdInSubtrees(),
                this.getWeight());
    }

}
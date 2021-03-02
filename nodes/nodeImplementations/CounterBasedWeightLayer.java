package projects.cbrenet.nodes.nodeImplementations;

import projects.cbrenet.nodes.tableEntry.CBInfo;

import java.util.HashMap;

public abstract class CounterBasedWeightLayer extends CounterBasedBSTLayer {
    private HashMap<Integer, Long> weights;

    @Override
    public void init() {
        super.init();
        this.weights = new HashMap<>();
    }

    public HashMap<Integer, Long> getWeights() {
        return weights;
    }

    public long getWeights(int largeId) {
        return weights.getOrDefault(largeId,-1L);
    }

    public void setWeight(int largeId, long weight) {
        this.weights.put(largeId, weight);
    }

    public void incrementWeight(int largeId) {
        if(this.weights.containsKey(largeId)){
            long weightTmp = this.weights.get(largeId);
            weightTmp++;
            this.weights.replace(largeId,weightTmp);
        }
        else{
            return;
        }
    }
}

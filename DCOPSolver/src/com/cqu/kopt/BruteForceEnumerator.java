package com.cqu.kopt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dyc on 2017/6/5.
 */
public class BruteForceEnumerator extends AbstractEnumerator {

    public BruteForceEnumerator(Map<Integer, Map<Integer, int[][]>> constraintView, Map<Integer, int[]> domainView, Map<Integer, Integer> valueView, List<Integer> activeAgents, int center) {
        super(constraintView, domainView, valueView, activeAgents, center);
    }

    @Override
    public Assignment enumerate() {
        Assignment assignment = new Assignment();
        int[] ids = new int[activeAgents.size()];
        int[] values = new int[activeAgents.size()];
        Map<Integer,Integer> index = new HashMap<>();
        for (int i = 0; i < ids.length; i++){
            ids[i] = activeAgents.get(i);
            index.put(ids[i],i);
        }
        // compute old cost;
        int sum = 0;
        for (int id : activeAgents){
            Map<Integer,int[][]> constraintsCost = constraintView.get(id);
            for (int otherId : constraintsCost.keySet()){
                sum += constraintsCost.get(otherId)[valueView.get(id)][valueView.get(otherId)];
            }
        }
        assignment.setCenter(center);
        assignment.setBeforeCost(sum);
        int minCost = Integer.MAX_VALUE;
        ext:while (true){
            sum = 0;
            for (int i = 0; i < ids.length; i++){
                Map<Integer,int[][]> constraintsCost = constraintView.get(ids[i]);
                for (int otherId : constraintsCost.keySet()){
                    sum += constraintsCost.get(otherId)[values[i]][index.containsKey(otherId) ? values[index.get(otherId)] : valueView.get(otherId)];
                }
            }
            if (sum < minCost){
                minCost = sum;
                for (int id : valueView.keySet()){
                    if (index.containsKey(id)){
                        assignment.put(id,values[index.get(id)]);
                    }

                }
            }
            values[values.length - 1]++;
            for (int i = values.length - 1; i >= 0; i--){
                if (values[i] == domainView.get(ids[i]).length){
                    if (i == 0){
                        break ext;
                    }
                    else {
                        values[i] = 0;
                        values[i - 1]++;
                    }
                }
            }
        }
        assignment.setTotalCost(minCost);
        return assignment;
    }
}

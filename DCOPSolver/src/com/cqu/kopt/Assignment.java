package com.cqu.kopt;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by dyc on 2017/6/5.
 */
public class Assignment {
    private Map<Integer,Integer> assignment;
    private int center;
    private int totalCost;
    private int beforeCost;

    public Assignment() {
        assignment = new HashMap<>();
    }

    public int getCenter() {
        return center;
    }

    public void setCenter(int center) {
        this.center = center;
    }

    public int getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(int totalCost) {
        this.totalCost = totalCost;
    }

    public int getBeforeCost() {
        return beforeCost;
    }

    public void setBeforeCost(int beforeCost) {
        this.beforeCost = beforeCost;
    }

    public void put(int id,int value){
        assignment.put(id,value);
    }

    public int get(int id){
        return assignment.get(id);
    }

    public boolean contains(int id){
        return assignment.containsKey(id);
    }

    public Set<Integer> idSet(){
        return assignment.keySet();
    }

    @Override
    public String toString() {
        return assignment.toString() + ",center=" + center + ",cost=" + totalCost;
    }
}

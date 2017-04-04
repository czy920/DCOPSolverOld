package com.cqu.maxsum;

/**
 * Created by dyc on 2017/3/6.
 */
public class UtilityMessage {
    private int[] utility;
    private int sum;
    private int fakeIndex;
    private int optimalUtility;
    private int optimalIndex;
    private int valueIndex;
    private int[] context;

    public UtilityMessage(int[] utility) {
        setUtility(utility);
    }

    public int getFakeIndex() {
        return fakeIndex;
    }

    public void setFakeIndex(int fakeIndex) {
        this.fakeIndex = fakeIndex;
    }

    public int[] getUtility() {
        return utility;
    }

    public void setUtility(int[] utility) {
        this.utility = utility;
        sum = 0;
        for (int i = 0; i < utility.length; i++){
            sum += utility[i];
        }
    }

    public void addUtility(UtilityMessage utilityMessage){
        if (utilityMessage.utility.length != this.utility.length){
            throw new RuntimeException("utility must be same length!");
        }
        sum += utilityMessage.sum;
        int normalizer = sum / utility.length;
        optimalUtility = Integer.MIN_VALUE;
        for (int i = 0; i < utility.length; i++){
            this.utility[i] += utilityMessage.utility[i];
            this.utility[i] -= normalizer;
            if (this.utility[i] > optimalUtility){
                optimalUtility = this.utility[i];
                optimalIndex = i;
            }
        }
        sum %= utility.length;
    }

    public int getOptimalIndex() {
        return optimalIndex;
    }

    public int getOptimalUtility() {
        return optimalUtility;
    }

    public void setValueIndex(int valueIndex) {
        this.valueIndex = valueIndex;
    }

    public int getValueIndex() {
        return valueIndex;
    }

    public int[] getContext() {
        return context;
    }

    public void setContext(int[] context) {
        this.context = context;
    }
}

package com.cqu.maxsum;

/**
 * Created by YanChenDeng on 2016/5/19.
 */
public class MessageContent {

    private HyperCube cube;
    private int optimalIndex;
    private int fakeIndex = -1;

    public int getOptimalIndex() {
        return optimalIndex;
    }

    public void setOptimalIndex(int optimalIndex) {
        this.optimalIndex = optimalIndex;
    }

    public HyperCube getCube() {
        return cube;
    }

    public void setCube(HyperCube cube) {
        this.cube = cube;
    }

    public int getFakeIndex() {
        return fakeIndex;
    }

    public void setFakeIndex(int fakeIndex) {
        this.fakeIndex = fakeIndex;
    }
}

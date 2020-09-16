package com.cqu.ga;

public class AcceptMessage {
    protected int [] OfferValue;
    protected int [] receiverValue;
    protected int [] globalGain;
    protected boolean [] isChanged;

    public AcceptMessage(int[] offerValue, int[] receiverValue, int[] globalGain, boolean[] isChanged) {
        OfferValue = offerValue;
        this.receiverValue = receiverValue;
        this.globalGain = globalGain;
        this.isChanged = isChanged;
    }

    public int[] getOfferValue() {
        return OfferValue;
    }

    public int[] getReceiverValue() {
        return receiverValue;
    }

    public int[] getGlobalGain() {
        return globalGain;
    }

    public boolean[] getIsChanged() {
        return isChanged;
    }
}

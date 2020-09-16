package com.cqu.ga;

import java.util.HashMap;

public class Offer {
    protected int offerValue;
    protected int receiverValue;
    protected int offerGain;

    public int getOfferValue() {
        return offerValue;
    }

    public int getReceiverValue() {
        return receiverValue;
    }

    public int getOfferGain() {
        return offerGain;
    }

    public Offer(int offerValue, int receiverValue, int offerGain) {
        this.offerValue = offerValue;
        this.receiverValue = receiverValue;
        this.offerGain = offerGain;
    }

}

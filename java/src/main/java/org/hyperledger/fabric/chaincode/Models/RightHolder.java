package org.hyperledger.fabric.chaincode.Models;

public class RightHolder {
    private String ISNI;
    private Attribute attribute;
    private Double percentage;


    public RightHolder(String ISNI, Attribute attribute, double percentage) {
        this.ISNI = ISNI;
        this.attribute = attribute;
        this.percentage = percentage;
    }

    private RightHolder() {}

    public String getISNI() {
        return ISNI;
    }

    public void setISNI(String ISNI) {
        this.ISNI = ISNI;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}

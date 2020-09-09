package org.hyperledger.fabric.chaincode.Models;

public class NonRightHolder {
    private String nameOrISNI;

    public NonRightHolder(String nameOrISNI) {
        this.nameOrISNI = nameOrISNI;
    }

    private NonRightHolder(){}

    public String getNameOrISNI() {
        return nameOrISNI;
    }

    public void setNameOrISNI(String nameOrISNI) {
        this.nameOrISNI = nameOrISNI;
    }
}

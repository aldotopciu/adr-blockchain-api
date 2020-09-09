package org.hyperledger.fabric.chaincode.Models;

import java.math.BigDecimal;

public class Wallet {
    private String walletId;
    private BigDecimal tokenAmount;

    public Wallet(String walletId, BigDecimal tokenAmount) {
        this.walletId = walletId;
        this.tokenAmount = tokenAmount;
    }

    private Wallet() {}

    public String getWalletId() {
        return walletId;
    }

    public BigDecimal getTokenAmount() {
        return tokenAmount;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public void setTokenAmount(BigDecimal tokenAmount) {
        this.tokenAmount = tokenAmount;
    }
}
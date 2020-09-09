package org.hyperledger.fabric.chaincode.Models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class Work {
    private String ISRC;
    private String ISWC;
    private String managerId;
    private String title;
    private List<RightHolder> rightHolders;
    private List<NonRightHolder> nonRightHolders;


    public Work(String ISRC, String ISWC, String managerId, String title, List<RightHolder> rightHolders, List<NonRightHolder> nonRightHolders) {
        this.ISRC = ISRC;
        this.ISWC = ISWC;
        this.managerId = managerId;
        this.title = title;
        this.rightHolders = rightHolders;
        this.nonRightHolders = nonRightHolders;
    }

    public Work(String ISRC, String ISWC, String managerId, String title, List<RightHolder> rightHolders) {
        this.ISRC = ISRC;
        this.ISWC = ISWC;
        this.managerId = managerId;
        this.title = title;
        this.rightHolders = rightHolders;
    }

    private Work() {
    }

    public String getISRC() {
        return ISRC;
    }

    public String getISWC() {
        return ISWC;
    }

    public String getManagerId() {
        return managerId;
    }

    public String getTitle() {
        return title;
    }

    public void setISRC(String ISRC) {
        this.ISRC = ISRC;
    }

    public void setISWC(String ISWC) {
        this.ISWC = ISWC;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<RightHolder> getRightHolders() {
        return rightHolders;
    }

    public void setRightHolders(List<RightHolder> rightHolders) {
        this.rightHolders = rightHolders;
    }

    public List<NonRightHolder> getNonRightHolders() {
        return nonRightHolders;
    }

    public void setNonRightHolders(List<NonRightHolder> nonRightHolders) {
        this.nonRightHolders = nonRightHolders;
    }
}
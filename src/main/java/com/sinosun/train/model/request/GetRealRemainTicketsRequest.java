package com.sinosun.train.model.request;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;
import java.util.List;

public class GetRealRemainTicketsRequest {
    private String fromName;

    private String targetName;

    private List<String> trainType;

    private boolean isHighSpeed = Boolean.TRUE;

    /**
     * 出发日期（格式：yyyy-mm-dd）
     */
    @JSONField(format = "yyyy-MM-dd")
    private Date fromDate;

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public boolean isHighSpeed() {
        return isHighSpeed;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public void setHighSpeed(boolean highSpeed) {
        isHighSpeed = highSpeed;
    }

    public List<String> getTrainType() {
        return trainType;
    }

    public void setTrainType(List<String> trainType) {
        this.trainType = trainType;
    }
}

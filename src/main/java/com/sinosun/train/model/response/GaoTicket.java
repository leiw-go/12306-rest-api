package com.sinosun.train.model.response;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONType;

@JSONType(naming = PropertyNamingStrategy.PascalCase)
public class GaoTicket {
    String trainNo;

    String fromStation;

    String toStation;

    String secondClassTicketNum;

    public String getTrainNo() {
        return trainNo;
    }

    public void setTrainNo(String trainNo) {
        this.trainNo = trainNo;
    }

    public String getFromStation() {
        return fromStation;
    }

    public void setFromStation(String fromStation) {
        this.fromStation = fromStation;
    }

    public String getToStation() {
        return toStation;
    }

    public void setToStation(String toStation) {
        this.toStation = toStation;
    }

    public String getSecondClassTicketNum() {
        return secondClassTicketNum;
    }

    public void setSecondClassTicketNum(String secondClassTicketNum) {
        this.secondClassTicketNum = secondClassTicketNum;
    }

    @Override
    public String toString() {
        return "GaoTicket{" +
                "trainNo='" + trainNo + '\'' +
                ", fromStation='" + fromStation + '\'' +
                ", toStation='" + toStation + '\'' +
                ", secondClassTicketNum='" + secondClassTicketNum + '\'' +
                '}';
    }
}

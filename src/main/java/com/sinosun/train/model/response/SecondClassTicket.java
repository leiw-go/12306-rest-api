package com.sinosun.train.model.response;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.base.MoreObjects;

import java.math.BigDecimal;

/**
 * Created on 2019/1/10 20:38.
 *
 * @author caogu
 */
@JSONType(naming = PropertyNamingStrategy.PascalCase)
public class SecondClassTicket {

    /**
     * 列车号
     */
    private String trainNo;

    /**
     * 车次代码
     */
    private String trainCode;

    /**
     * 车次类型
     */
    private String trainType;

    /**
     * 出发站点名字
     */
    private String fromStation;

    /**
     * 到达站点名字
     */
    private String toStation;

    /**
     * 出发时间
     */
    private String fromTime;

    /**
     * 到达时间
     */
    private String toTime;

    /**
     * 运行时间
     */
    private String runTime;

    /**
     * 该车次是否有余票可以预定，所有席别无票则为false
     */
    private Boolean canBook;

    /**
     * 	二等座剩余票数
     */
    private String secondClassTicKetNum;

    /**
     * 二等座价格
     */
    private BigDecimal secondClassTicketPrice;

    /**
     * 	无座剩余票数
     */
    private String noSeatTicketNum;

    /**
     * 无座价格
     */
    private BigDecimal noSeatTicketPrice;

    /**
     * 	其他剩余票数
     */
    private String otherCheapTicketNum;

    /**
     * 其他价格
     */
    private BigDecimal otherCheapTicketPrice;

    public String getTrainNo() {
        return trainNo;
    }

    public void setTrainNo(String trainNo) {
        this.trainNo = trainNo;
    }

    public String getTrainCode() {
        return trainCode;
    }

    public void setTrainCode(String trainCode) {
        this.trainCode = trainCode;
    }

    public String getTrainType() {
        return trainType;
    }

    public void setTrainType(String trainType) {
        this.trainType = trainType;
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

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    public Boolean getCanBook() {
        return canBook;
    }

    public void setCanBook(Boolean canBook) {
        this.canBook = canBook;
    }

    public String getSecondClassTicKetNum() {
        return secondClassTicKetNum;
    }

    public void setSecondClassTicKetNum(String secondClassTicKetNum) {
        this.secondClassTicKetNum = secondClassTicKetNum;
    }

    public BigDecimal getSecondClassTicketPrice() {
        return secondClassTicketPrice;
    }

    public void setSecondClassTicketPrice(BigDecimal secondClassTicketPrice) {
        this.secondClassTicketPrice = secondClassTicketPrice;
    }

    public String getNoSeatTicketNum() {
        return noSeatTicketNum;
    }

    public void setNoSeatTicketNum(String noSeatTicketNum) {
        this.noSeatTicketNum = noSeatTicketNum;
    }

    public BigDecimal getNoSeatTicketPrice() {
        return noSeatTicketPrice;
    }

    public void setNoSeatTicketPrice(BigDecimal noSeatTicketPrice) {
        this.noSeatTicketPrice = noSeatTicketPrice;
    }

    public String getOtherCheapTicketNum() {
        return otherCheapTicketNum;
    }

    public void setOtherCheapTicketNum(String otherCheapTicketNum) {
        this.otherCheapTicketNum = otherCheapTicketNum;
    }

    public BigDecimal getOtherCheapTicketPrice() {
        return otherCheapTicketPrice;
    }

    public void setOtherCheapTicketPrice(BigDecimal otherCheapTicketPrice) {
        this.otherCheapTicketPrice = otherCheapTicketPrice;
    }

    @Override
    public String toString() {
        return "SecondClassTicket{" +
                "trainCode='" + trainCode + '\'' +
                '}';
    }
}

package com.sinosun.train.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.sinosun.train.constants.UrlConstant;
import com.sinosun.train.datamap.SeatTypeMap;
import com.sinosun.train.datamap.TrainCodeTrainNoMap;
import com.sinosun.train.enums.train.PassengerType;
import com.sinosun.train.model.request.GetRealRemainTicketsRequest;
import com.sinosun.train.model.request.GetTicketListRequest;
import com.sinosun.train.model.request.GetTrainLineRequest;
import com.sinosun.train.model.response.*;
import com.sinosun.train.model.vo.TicketPrice;
import com.sinosun.train.utils.StationUtil;
import com.sinosun.train.utils.TicketUtil;
import com.sinosun.train.utils.TrainWebHelper;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created on 2019/1/10 21:00.
 *
 * @author caogu
 */
@Service
public class TrainTicketService {
    private static final Logger logger = LoggerFactory.getLogger(TrainTicketService.class);

    @Autowired
    private TrainStationService trainStationService;

    private static final String DATE_FORMAT = "yyyy-MM-dd";


    private static String getTicketPriceUrlFmt = UrlConstant.BASE_URL +
            "/leftTicket/queryTicketPrice?" +
            "train_no=%s&" +
            "from_station_no=%s&" +
            "to_station_no=%s&" +
            "seat_types=%s&" +
            "train_date=%s";

    public TicketListResult getTicketList(GetTicketListRequest requestBody) throws InterruptedException {
        requestBody.validate();
        return new TicketListResult(new TicketList(TrainWebHelper.getTicketListFrom12306Cn(requestBody)));
    }

    public TrainLineResult getTrainLine(GetTrainLineRequest requestBody) throws InterruptedException {
        requestBody.validate();
        String trainNo = TrainCodeTrainNoMap.getTrainNo(requestBody.getTrainCode());
        // 在获取不到trainNo时，trainCode必须有值
        if (StringUtils.isEmpty(trainNo)) {
            TrainCodeResult trainCodeResult = trainStationService.getAllTrainCode(null);
            trainNo = (String) trainCodeResult.getResult().get(requestBody.getTrainCode());
            logger.info("get train no {} of {} form 12306 js.", trainNo, requestBody.getTrainCode());
        }
        String fromDate = TrainWebHelper.convertFromDate(requestBody.getFromDate());
        return new TrainLineResult(TrainWebHelper.getTrainLineFrom12306(trainNo, fromDate, requestBody.getFromStationCode(), requestBody.getToStationCode()));
    }

    public SecondClassTicketListResult getRemainTicket(GetRealRemainTicketsRequest request) throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        List<TrainLine> lines = StationUtil.getLinesBetweenStations(request);
        Station fromStation = StationUtil.getStationFromName(request.getFromName());
        List<SecondClassTicket> remainTickets = new ArrayList<>();
        Map<String, List<String>> middleStationMapTrain = new HashMap<>();
        for (TrainLine line : lines) {
            int startNo = StationUtil.getStationNoFromName(request.getFromName(), line.getStops());
            int endNo = StationUtil.getStationNoFromName(request.getTargetName(), line.getStops());
            if (startNo == endNo) {
                return new SecondClassTicketListResult();
            }
           StationUtil.getTrainCodeMap(startNo, endNo, line, middleStationMapTrain);
        }
        logger.info("Already got middle station train map {}.", middleStationMapTrain);
        logger.info("this map is real train which can take us to target.");
        for (Map.Entry<String, List<String>> entry : middleStationMapTrain.entrySet()) {
            GetTicketListRequest model = new GetTicketListRequest();
            model.setFromStationCode(fromStation.getStationCode());
            model.setToStationCode(StationUtil.getStationFromName(entry.getKey()).getStationCode());
            model.setFromDate(request.getFromDate());
            model.setTrainType(request.getTrainType());
            List<Ticket> sourceTickets = TrainWebHelper.getSpecificTicketListFrom12306Cn(model);
            List<Ticket> tickets = TicketUtil.filterTicketTypeAndNum(model, sourceTickets);
            List<SecondClassTicket> secondClassTickets = TicketUtil.filterRealWeWantLineAndBuildTickets(tickets, entry);
            logger.info("Between {} and {}, filterList is {}.", fromStation.getName(), entry.getKey(), secondClassTickets);
            logger.info("collect tickets between {} and {} success, about lines {}",
                    request.getFromName(), entry.getKey(), entry.getValue());
            remainTickets.addAll(secondClassTickets);
        }
        watch.stop();
        logger.info("we got this all train data, it takes {}s", watch.getTotalTimeSeconds());
        return new SecondClassTicketListResult(new SecondClassTicketList(remainTickets));
    }

    /**
     * 查询车票的价格
     * @param trainNo 列车号 注意区分车次代码
     * @param fromDate 出发日期
     * @param fromStationNo 出发站序
     * @param toStationNo 到达站序
     * @param seatTypes ? 查询车票列表时的ypEx字段
     * @return 所有票价
     */
    private TicketPrice queryTicketPrice(String trainNo, String fromDate, String fromStationNo, String toStationNo, String seatTypes) {
        String getTicketPriceUrl = String.format(getTicketPriceUrlFmt, trainNo, fromStationNo, toStationNo, seatTypes, fromDate);
        JSONObject ret12306 = TrainWebHelper.requestTo12306(getTicketPriceUrl);
        JSONObject data = ret12306.getJSONObject("data");

        Map<String, String> seatTypeMap = SeatTypeMap.getSeatTypeMap();
        BigDecimal swzPrice = processPrice(data.getString(seatTypeMap.get("swz")));
        BigDecimal ydzPrice = processPrice(data.getString(seatTypeMap.get("ydz")));
        BigDecimal edzPrice = processPrice(data.getString(seatTypeMap.get("edz")));
        BigDecimal grPrice = processPrice(data.getString(seatTypeMap.get("gr")));
        BigDecimal rwPrice = processPrice(data.getString(seatTypeMap.get("rw")));
        BigDecimal dwPrice = processPrice(data.getString(seatTypeMap.get("dw")));
        BigDecimal ywPrice = processPrice(data.getString(seatTypeMap.get("yw")));
        BigDecimal rzPrice = processPrice(data.getString(seatTypeMap.get("rz")));
        BigDecimal yzPrice = processPrice(data.getString(seatTypeMap.get("yz")));
        BigDecimal wzPrice = processPrice(data.getString(seatTypeMap.get("wz")));

        logger.info("商务座/特等座价格:{} 一等座价格:{} 二等座价格:{} 高级软卧价格:{} 软卧价格:{} 动卧价格:{} 硬卧价格:{} 软座价格:{} 硬座价格:{} 无座价格:{}",
                swzPrice, ydzPrice, edzPrice, grPrice, rwPrice, dwPrice, ywPrice, rzPrice, yzPrice, wzPrice);

        TicketPrice ticketPrice = new TicketPrice();
        ticketPrice.setSwzPrice(swzPrice);
        ticketPrice.setYdzPrice(ydzPrice);
        ticketPrice.setEdzPrice(edzPrice);
        ticketPrice.setGjrwPrice(grPrice);
        ticketPrice.setRwPrice(rwPrice);
        ticketPrice.setDwPrice(dwPrice);
        ticketPrice.setYwPrice(ywPrice);
        ticketPrice.setRzPrice(rzPrice);
        ticketPrice.setYzPrice(yzPrice);
        ticketPrice.setWzPrice(wzPrice);
        return ticketPrice;
    }

    /**
     * 获取站点类型 始|终|过
     * @param stationNo 站序（对应火车经停信息中的站序）01表示始发站，大于1则表示过站
     * @param trainLine 火车经停信息
     * @return 站点类型
     */
    private String getStationTypeName(String stationNo, TrainLine trainLine) {
        String ret = "过";
        if (Integer.parseInt(stationNo) == 1) {
            ret = "始";
        } else if (Integer.parseInt(stationNo) == trainLine.getStops().size()) {
            ret = "终";
        } else {
            ret = "过";
        }
        return ret;
    }

    /**
     * 价格去掉前置¥
     * @param price 原始价格
     * @return 去掉¥的价格
     */
    private BigDecimal processPrice(String price) {
        BigDecimal ret = null;
        if (StringUtils.isNotEmpty(price) && price.startsWith("¥")) {
            ret = new BigDecimal(price.substring(1));
        }
        return ret;
    }

}

package com.sinosun.train.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.sinosun.train.constants.UrlConstant;
import com.sinosun.train.datamap.SeatTypeMap;
import com.sinosun.train.datamap.TrainCodeTrainNoMap;
import com.sinosun.train.enums.train.PassengerType;
import com.sinosun.train.model.request.GetTicketListRequest;
import com.sinosun.train.model.response.Station;
import com.sinosun.train.model.response.Stop;
import com.sinosun.train.model.response.Ticket;
import com.sinosun.train.model.response.TrainLine;
import com.sinosun.train.model.vo.TicketPrice;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created on 2019/1/16 20:42.
 *
 * @author caogu
 */
@Component
public class TrainWebHelper {
    private static final Logger logger = LoggerFactory.getLogger(TrainWebHelper.class);
    @Getter
    private static long SLEEP_TIME;

    @Value("${sleepTime}")
    public void setSleepTime(long time) {
        SLEEP_TIME = time;
    }

    /**
     * 从12306获取最新的火车站点数据
     * @return 火车站点数据
     */
    public static List<Station> getTrainAllCityFromNet() {
        List<Station> stations = Lists.newArrayList();

        String httpRes = HttpUtil.request(UrlConstant.TRAIN_ALL_STATION_URL, Connection.Method.GET, null);
        if (StringUtils.isNotEmpty(httpRes)) {
            // var station_names ='@bjb|北京北|VAP|beijingbei|bjb|0@bjd|北京东|BOP|beijingdong|bjd|1'
            String originalStationStr = httpRes.substring(httpRes.indexOf("\'") + 1, httpRes.lastIndexOf("\'"));
            List<String> originalStations = Lists.newArrayList(Splitter.on("@").omitEmptyStrings().trimResults().split(originalStationStr));
            for (String originalStation : originalStations) {
                List<String> stationInfo = Lists.newArrayList(Splitter.on("|").omitEmptyStrings().trimResults().split(originalStation));
                if (stationInfo.size() > 3) {
                    Station station = new Station();
                    station.setPingYinShort(stationInfo.get(0));
                    station.setName(stationInfo.get(1));
                    station.setStationCode(stationInfo.get(2));
                    station.setPingYin(stationInfo.get(3));
                    stations.add(station);
                } else {
                    logger.error("从12306获取到的火车站点信息有误，stationInfo={}", originalStation);
                }
            }
        } else {
            logger.error("从12306获取到的火车站点信息为空");
        }
        return stations;
    }

    /**
     * 从12306获取最新的热门火车站点数据
     * @return 火车站点数据
     */
    public static List<Station> getTrainHotStationFromNet() {
        List<Station> stations = Lists.newArrayList();

        String httpRes = HttpUtil.request(UrlConstant.TRAIN_HOT_STATION_URL, Connection.Method.GET, null);
        if (StringUtils.isNotEmpty(httpRes)) {
            // var station_names ='@bjb|北京北|VAP|beijingbei|bjb|0@bjd|北京东|BOP|beijingdong|bjd|1'
            String originalStationStr = httpRes.substring(httpRes.indexOf("\'") + 1, httpRes.lastIndexOf("\'"));
            List<String> originalStations = Lists.newArrayList(Splitter.on("@").omitEmptyStrings().trimResults().split(originalStationStr));
            for (String originalStation : originalStations) {
                List<String> stationInfo = Lists.newArrayList(Splitter.on("|").omitEmptyStrings().trimResults().split(originalStation));
                if (stationInfo.size() > 3) {
                    Station station = new Station();
                    station.setPingYinShort(stationInfo.get(0));
                    station.setName(stationInfo.get(1));
                    station.setStationCode(stationInfo.get(2));
                    station.setPingYin(stationInfo.get(3));
                    stations.add(station);
                } else {
                    logger.error("从12306获取到的热门火车站点信息有误，stationInfo={}", originalStation);
                }
            }
        } else {
            logger.error("从12306获取到的热门火车站点信息为空");
        }
        return stations;
    }

    /**
     * 向12306发送get请求，并返回json对象
     * @param url 请求URL
     * @return json对象
     */
    public static JSONObject requestTo12306(String url) {
        return JsonUtil.parseObject(HttpUtil.request(url, Connection.Method.GET, null));
    }

    /**
     * 从12306获取车票列表，先请求一次，若URL发生跳转则用新地址继续请求
     * @param requestBody 请求
     * @return 车票列表
     */
    public static List<Ticket> getTicketListFrom12306Cn(GetTicketListRequest requestBody) throws InterruptedException {
        JSONObject ret12306 = JsonUtil.parseObject(HttpUtil.request(getTicketListUrl(requestBody), Connection.Method.GET, null));
        logger.debug("start to sleep in getTicketListFrom12306Cn...");
        Thread.sleep(SLEEP_TIME);
        JSONObject data = ret12306.getJSONObject("data");
        return buildTicketList(requestBody, data);
    }

    /**
     * 从12306获取车票列表，获取完全指定终点车站的票
     * @param requestBody 请求
     * @return 车票列表
     */
    public static List<Ticket> getSpecificTicketListFrom12306Cn(GetTicketListRequest requestBody) throws InterruptedException {
        JSONObject ret12306 = JsonUtil.parseObject(HttpUtil.request(getTicketListUrl(requestBody), Connection.Method.GET, null));
        logger.debug("start to sleep in getSpecificTicketListFrom12306Cn...");
        Thread.sleep(SLEEP_TIME);
        JSONObject data = ret12306.getJSONObject("data");
        return buildTicketList(requestBody, data).stream()
                .filter(f -> StationUtil.getStationFromName(f.getToStation()).getStationCode().equals(requestBody.getToStationCode())).collect(Collectors.toList());
    }

    /**
     * 获取查询车票的url
     * @param requestBody 请求参数
     * @return 完成URL
     */
    public static String getTicketListUrl(GetTicketListRequest requestBody) {
        String fromDate = new DateTime(requestBody.getFromDate()).toString("yyyy-MM-dd");
        String passengerType = requestBody.getIsStudent() ? PassengerType.STUDENT.value() : PassengerType.ADULT.value();
        return String.format(UrlConstant.GET_REMAIN_TICKETS_URL_FMT, UrlConstant.LEFT_TICKET_URL_MAYBE_CHANGE, fromDate, requestBody.getFromStationCode(),
                requestBody.getToStationCode(), passengerType);
    }

    /**
     * 12306车票列表信息，转为为平台的车票列表
     * @param requestBody 请求
     * @param data 12306车票列表信息
     * @return 平台的车票列表
     */
    private static List<Ticket> buildTicketList(GetTicketListRequest requestBody, JSONObject data) {
        List<Ticket> ret = Lists.newArrayList();

        //站点代码和名字映射
        JSONObject map = data.getJSONObject("map");
        String flag = data.getString("flag");
        JSONArray result = data.getJSONArray("result");
        if ("1".equals(flag)) {
            for (int i = 0; i < result.size(); i++) {
                String train = result.getString(i);
                List<String> trainItem = Splitter.on("|").splitToList(train);

                String secretStr = trainItem.get(0); //?
                String buttonTextInfo = trainItem.get(1); //按钮名字：预订

                String trainNo = trainItem.get(2); //列车号
                String trainCode = trainItem.get(3); //车次

                String startStationCode = trainItem.get(4); // 起始站代码
                String endStationCode = trainItem.get(5); // 终点站站代码

                String fromStationCode = trainItem.get(6); //出发站代码
                String toStationCode = trainItem.get(7); //到达站代码
                String fromStationName = map.getString(fromStationCode); //出发站名称
                String toStationName = map.getString(toStationCode); //到达站名称

                String startTime = trainItem.get(8); //出发时刻
                String arriveTime = trainItem.get(9); //到达时刻
                String runTime = trainItem.get(10); //历时

                String canWebBuy = trainItem.get(11); //是否能购买：Y 可以 N 不可 IS_TIME_NOT_BUY 列车运行图调整,暂停发售/列车停运
                String ypInfo = trainItem.get(12); //?

                String startTrainDate = trainItem.get(13); //列车起始站发成日期

                String trainSeatFeature = trainItem.get(14); //?
                String locationCode = trainItem.get(15); //?
                String fromStationNo = trainItem.get(16); //出发站站序（对应火车经停信息中的站序）01表示始发站，大于1则表示过站
                String toStationNo = trainItem.get(17); //到达站站序（对应火车经停信息中的站序）
                String isSupportCard = trainItem.get(18); //可凭二代身份证直接进出站 1 可以 0 不可以

                String controlledTrainFlag = trainItem.get(19); //?
                String ggNum = StringUtils.isNotEmpty(trainItem.get(20)) ? trainItem.get(20) : "0"; //?

                String grNum = StringUtils.isNotEmpty(trainItem.get(21)) ? trainItem.get(21) : "0"; // 高级软卧
                String qtNum = StringUtils.isNotEmpty(trainItem.get(22)) ? trainItem.get(22) : "0"; // 其他

                String rwNum = StringUtils.isNotEmpty(trainItem.get(23)) ? trainItem.get(23) : "0"; // 软卧/一等卧
                String rzNum = StringUtils.isNotEmpty(trainItem.get(24)) ? trainItem.get(24) : "0"; // 软座

                String tzNum = StringUtils.isNotEmpty(trainItem.get(25)) ? trainItem.get(25) : "0"; //? 特等座?
                String wzNum = StringUtils.isNotEmpty(trainItem.get(26)) ? trainItem.get(26) : "0"; // 无座
                String ybNum = StringUtils.isNotEmpty(trainItem.get(27)) ? trainItem.get(27) : "0"; //?
                String ywNum = StringUtils.isNotEmpty(trainItem.get(28)) ? trainItem.get(28) : "0"; // 硬卧/二等卧
                String yzNum = StringUtils.isNotEmpty(trainItem.get(29)) ? trainItem.get(29) : "0"; // 硬座
                String edzNum = StringUtils.isNotEmpty(trainItem.get(30)) ? trainItem.get(30) : "0"; // 二等座
                String ydzNum = StringUtils.isNotEmpty(trainItem.get(31)) ? trainItem.get(31) : "0"; // 一等座
                String swzNum = StringUtils.isNotEmpty(trainItem.get(32)) ? trainItem.get(32) : "0"; // 商务座/特等座
                String srrbNum = StringUtils.isNotEmpty(trainItem.get(33)) ? trainItem.get(33) : "0"; // 动卧

                String ypEx = trainItem.get(34); //? 查询车票价格时的seat_types字段
                String seatTypes = trainItem.get(35); //?
                String exchangeTrainFlag = trainItem.get(36); //?

                logger.debug("列车号：{} 出发站:{} 达站:{} 出发时间:{} 到达时间:{} 历时:{} 列车起始站发车日期:{} " +
                                "商务座特等座:{} 一等座:{} 二等座:{} 高级软卧:{} 软卧:{} 动卧:{} 硬卧:{} 软座:{} 硬座:{} 无座:{} 其他:{} 备注:{}",
                        trainCode, fromStationName, toStationName, startTime, arriveTime, runTime, startTrainDate,
                        swzNum, ydzNum, edzNum, grNum, rwNum, srrbNum, ywNum, rzNum, yzNum, wzNum, qtNum, buttonTextInfo);

                // 缓存车次代码--站点代码映射
                TrainCodeTrainNoMap.put(trainCode, trainNo);
                String fromDate = convertFromDate(requestBody.getFromDate());

//                TicketPrice ticketPrice = queryTicketPrice(trainNo, fromDate, fromStationNo, toStationNo, ypEx);
//                TrainLine trainLine = getTrainLineFrom12306(trainNo,fromDate, fromStationCode, toStationCode);

                TicketPrice ticketPrice = new TicketPrice();

                Ticket ticket = new Ticket();
                ticket.setTrainNo(trainNo);
                ticket.setTrainCode(trainCode);
                ticket.setTrainType(getTrainType(trainCode));
                ticket.setFromStation(fromStationName);
                ticket.setToStation(toStationName);
//                ticket.setFromStationType(getStationTypeName(fromStationNo, trainLine));
//                ticket.setToStationType(getStationTypeName(toStationNo, trainLine));
                ticket.setFromTime(startTime);
                ticket.setToTime(arriveTime);
                ticket.setRunTime(runTime);
                ticket.setCanBook(getCanBook(buttonTextInfo, canWebBuy));

                ticket.setSwzNum(getTicketNum(swzNum));
                ticket.setSwzPrice(ticketPrice.getSwzPrice());
                ticket.setYdzNum(getTicketNum(ydzNum));
                ticket.setYdzPrice(ticketPrice.getYdzPrice());
                ticket.setEdzNum(getTicketNum(edzNum));
                ticket.setEdzPrice(ticketPrice.getEdzPrice());
                ticket.setGjrwNum(getTicketNum(grNum));
                ticket.setGjrwPrice(ticketPrice.getGjrwPrice());
                ticket.setRwNum(getTicketNum(rwNum));
                ticket.setRwPrice(ticketPrice.getRwPrice());
                ticket.setDwNum(getTicketNum(srrbNum));
                ticket.setDwPrice(ticketPrice.getDwPrice());
                ticket.setYwNum(getTicketNum(ywNum));
                ticket.setYwPrice(ticketPrice.getYwPrice());
                ticket.setRzNum(getTicketNum(rzNum));
                ticket.setRzPrice(ticketPrice.getRzPrice());
                ticket.setYzNum(getTicketNum(yzNum));
                ticket.setYzPrice(ticketPrice.getYzPrice());
                ticket.setWzNum(getTicketNum(wzNum));
                ticket.setWzPrice(ticketPrice.getWzPrice());
                ticket.setQtNum(getTicketNum(qtNum));
                ticket.setQtPrice(ticketPrice.getQtPrice());
                ret.add(ticket);
            }
        }
        return ret;
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
    private static Map<String, String> querySecondClassTicketPrice(String trainNo, String fromDate, String fromStationNo, String toStationNo, String seatTypes) {
        String getTicketPriceUrl = String.format(UrlConstant.GET_PRICE_URL_FMT, trainNo, fromStationNo, toStationNo, seatTypes, fromDate);
        JSONObject ret12306 = TrainWebHelper.requestTo12306(getTicketPriceUrl);
        JSONObject data = ret12306.getJSONObject("data");

        Map<String, String> priceMap = new HashMap<>();
        priceMap.put("secondClassPrice", data.getString("O"));
        priceMap.put("noSeatPrice", data.getString("WZ"));
        return priceMap;
    }

    /**
     * Date类型转为yyyy-MM-dd字符串
     * @param fromDate Date类型时间
     * @return yyyy-MM-dd字符串
     */
    public static String convertFromDate(Date fromDate) {
        return new DateTime(fromDate).toString("yyyy-MM-dd");
    }

    /**
     * 获取可预订的座位数
     * 有|表示充足 --|表示无此类型的座位 数字表示剩余座位数
     * @param num 12306返回的座位数
     * @return 转化后的座位数
     */
    private static String getTicketNum(String num) {
        return "无".equals(num) ? "0" : num;
    }

    /**
     * 判断车票是否可预订
     * @param buttonTextInfo 预订两字
     * @param canWebBuy 是否能购买：Y 可以 N 不可 IS_TIME_NOT_BUY 列车运行图调整,暂停发售/列车停运
     * @return 是否可预订
     */
    private static boolean getCanBook(String buttonTextInfo, String canWebBuy) {
        return "预订".endsWith(buttonTextInfo) && "Y".equals(canWebBuy);
    }

    /**
     * 获取列车类别
     * G高铁 C城际 D动车 Z直达 T特快 K快速 O其他
     * @param trainCode 列车车次
     * @return 列车类别
     */
    private static String getTrainType(String trainCode) {
        String trainType = "O";
        if (Character.isLetter(trainCode.charAt(0))) {
            trainType = String.valueOf(trainCode.charAt(0));
        }
        return trainType;
    }

    /**
     * 查询列车经停信息
     * @param trainNo 列车号 注意区分车次代码
     * @param fromDate 出发日期
     * @param fromStationCode 出发站点代码
     * @param toStationCode 到达站点代码
     * @return 经停信息
     */
    public static TrainLine getTrainLineFrom12306(String trainNo, String fromDate, String fromStationCode, String toStationCode) throws InterruptedException {
        String getTicketLineUrl = String.format(UrlConstant.GET_TRAIN_LINE_URL_FMT, trainNo, fromStationCode, toStationCode, fromDate);
        JSONObject ret12306 = JsonUtil.parseObject(HttpUtil.request(getTicketLineUrl, Connection.Method.GET, null));
        Thread.sleep(SLEEP_TIME);
        JSONArray stops = ret12306.getJSONObject("data").getJSONArray("data");
        TrainLine trainLine = new TrainLine();
        if (!CollectionUtils.isEmpty(stops)) {
            JSONObject stopInfoFirst = stops.getJSONObject(0);
            String startStationName = stopInfoFirst.getString("start_station_name"); //出发城市
            String endStationName = stopInfoFirst.getString("end_station_name"); //到达城市
            String stationTrainCode = stopInfoFirst.getString("station_train_code"); //车次号
            String trainClassName = stopInfoFirst.getString("train_class_name"); //车次类型， 快速等
            String serviceType = stopInfoFirst.getString("service_type"); //服务类型 0表示无空调 其他表示有空调
            String serviceName = "0".equals(serviceType) ? "无空调" : "有空调";

            List<Stop> resultStops = Lists.newArrayList();
            for (int i = 0; i < stops.size(); i++) {
                Stop stop = new Stop();
                JSONObject stopInfo = stops.getJSONObject(i);
                String startTime = stopInfo.getString("start_time"); //出发时间（格式 HH:mm）
                String arriveTime = stopInfo.getString("arrive_time"); //到达时间（格式 HH:mm 或者----）
                String stationName = stopInfo.getString("station_name"); //站名
                String stopoverTime = stopInfo.getString("stopover_time"); //停留时间（分钟） 可能为----
                String stationNo = stopInfo.getString("station_no"); //站序（01开始）
                Boolean isSearchStation = stopInfo.getBoolean("isEnabled"); //是否是我们搜索的出行站和到达站 false不是 true是

                stop.setStartTime(startTime);
                stop.setArriveTime(arriveTime);
                stop.setStationName(stationName);
                stop.setStopoverTime(stopoverTime);
                stop.setStationNo(stationNo);
                stop.setIsSearchStation(isSearchStation);
                resultStops.add(stop);
            }

            trainLine.setTrainCode(stationTrainCode);
            trainLine.setStartStationName(startStationName);
            trainLine.setEndStationName(endStationName);
            trainLine.setTrainClassName(trainClassName);
            trainLine.setServiceName(serviceName);
            trainLine.setStops(resultStops);
            return trainLine;
        }

        return trainLine;
    }

    /**
     * 从12306获取最新的 车次 - 列车号 关联数据
     *
     * @return
     */
    public static Map<String, Object> getAllTrainNoListFromNet() {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {
            // HttpUtil.request获得的结果在json对象序列化时存在unclosed.str异常
            // String httpRes = HttpUtil.request(UrlConstant.TRAIN_ALL_CODE_LIST_URL, Connection.Method.GET, null);
            String httpRes = cn.hutool.http.HttpUtil.get(UrlConstant.TRAIN_ALL_CODE_LIST_URL);

            // 返回例子：{ "2019-03-17": { "D": [ { "station_train_code": "D1(北京-沈阳南)", "train_no": "24000000D120" },.... ], .... } }
            if (StringUtils.isNotEmpty(httpRes) && httpRes.startsWith("var")) {
                httpRes = httpRes.replaceAll("var train_list =", "");

                JSONObject jsonObject = JSONObject.parseObject(httpRes);

                // 第一层遍历，时间
                for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                    // 获取列车类型对象
                    JSONObject dateElementObj = (JSONObject) entry.getValue();
                    // 遍历每个时间的每个列车类型
                    for (Map.Entry<String, Object> trainTypeEntry : dateElementObj.entrySet()) {
                        JSONArray trainTypeArray = (JSONArray) trainTypeEntry.getValue();
                        // 遍历每个车型的车次
                        for (int i = 0; i < trainTypeArray.size(); i++) {
                            JSONObject trainInfo = trainTypeArray.getJSONObject(i);

                            resultMap.put(trainInfo.getString("station_train_code").replaceAll("[^0-9a-zA-Z]", ""), trainInfo.getString("train_no"));
                        }
                    }
                }
            } else {
                logger.error("从12306获取到的火车站点信息为空");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultMap;
    }
}

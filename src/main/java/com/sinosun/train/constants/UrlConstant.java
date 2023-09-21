package com.sinosun.train.constants;

/**
 * Created on 2019/1/16 13:48.
 *
 * @author caogu
 */
public class UrlConstant {
    /**
     * {@value}12306获取火车站站点列表URL
     */
    public static final String TRAIN_ALL_STATION_URL = "https://kyfw.12306.cn/otn/resources/js/framework/station_name.js";


    /**
     * {@value}12306获取热点火车站点列表URL
     */
    public static final String TRAIN_HOT_STATION_URL = "https://kyfw.12306.cn/otn/resources/js/framework/favorite_name.js";

    /**
     * {@value}12306获取所有车次列表URL
     */
    public static final String TRAIN_ALL_CODE_LIST_URL = "https://kyfw.12306.cn/otn/resources/js/query/train_list.js";

    public static final String BASE_URL = "https://kyfw.12306.cn/otn";

    public static String GET_REMAIN_TICKETS_URL_FMT = BASE_URL +
            "/%s?" +
            "leftTicketDTO.train_date=%s&" +
            "leftTicketDTO.from_station=%s&" +
            "leftTicketDTO.to_station=%s&" +
            "purpose_codes=%s";

    public static String LEFT_TICKET_URL_MAYBE_CHANGE = "leftTicket/queryZ";

    public static String GET_TRAIN_LINE_URL_FMT = BASE_URL +
            "/czxx/queryByTrainNo?" +
            "train_no=%s&" +
            "from_station_telecode=%s&" +
            "to_station_telecode=%s&" +
            "depart_date=%s";

    public static String GET_PRICE_URL_FMT = BASE_URL +
            "/leftTicket/queryTicketPrice?" +
            "from_station_no=%s&" +
            "to_station_no=%s&" +
            "seat_types=%s" +
            "train_date=%s";
}

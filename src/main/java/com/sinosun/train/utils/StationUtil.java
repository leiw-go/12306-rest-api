package com.sinosun.train.utils;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.sinosun.train.constants.RedisKeyConstant;
import com.sinosun.train.model.request.GetRealRemainTicketsRequest;
import com.sinosun.train.model.request.GetTicketListRequest;
import com.sinosun.train.model.request.SearchCityRequest;
import com.sinosun.train.model.response.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class StationUtil {

    public static RedisUtils staticRedisUtils;

    @Resource
    public void setStaticRedisUtils(RedisUtils redisUtils) {
        StationUtil.staticRedisUtils = redisUtils;
    }

    /**
     * 获取当前地点的所有车站
     *
     * @param areaName 当前地点名称 如上海，北京
     * @return 车站列表 如 北京南 上海虹桥
     */
    public static List<Station> getThisAreaAllStations(String areaName) {
        List<Station> ret = Lists.newArrayList();
        // 从本地获取
        // List<Station> stations = getAllStation();
        // 从redis获取
        List<Station> stations = getAllStation();
        for (Station station : stations) {
            boolean isMatching = station.getName().startsWith(areaName)
                    || station.getPingYin().toLowerCase(Locale.ENGLISH).startsWith(areaName.toLowerCase(Locale.ENGLISH))
                    || station.getPingYinShort().toLowerCase(Locale.ENGLISH).startsWith(areaName.toLowerCase(Locale.ENGLISH));
            if (isMatching) {
                ret.add(station);
            }
        }
        return ret;
    }

    /**
     * 获取火车站点数据，先从redis获取，获取是失败在从12306获取
     *
     * @return 所有火车站点数据
     */
    public static List<Station> getAllStation() {
        // 优先从redis中获取站点信息
        String allStationStr = (String) staticRedisUtils.get(RedisKeyConstant.REDIS_KEY_LOCAL_DATA_STATION);
        List<Station> stations = null;
        if (StringUtils.isNotBlank(allStationStr)) {
            stations = JSONObject.parseArray(allStationStr, Station.class);
        }

        if (CollectionUtils.isEmpty(stations)) {
            stations = TrainWebHelper.getTrainAllCityFromNet();
            // 设置到缓存
            staticRedisUtils.set(RedisKeyConstant.REDIS_KEY_LOCAL_DATA_STATION, JSONObject.toJSONString(stations), 1L, TimeUnit.DAYS);
        }
        return stations;
    }

    /**
     * 获取两地之间的所有可能线路
     *
     * @param request 出发地
     * @return 两地直接所有线路
     */
    public static List<TrainLine> getLinesBetweenStations(GetRealRemainTicketsRequest request) throws InterruptedException {
        List<Station> fromStations = getThisAreaAllStations(request.getFromName());
        List<Station> toStations = getThisAreaAllStations(request.getTargetName());
        List<Ticket> tickets = new ArrayList<>();
        List<TrainLine> trainLines = new ArrayList<>();
        OUT:
        for (Station fromStation : fromStations) {
            for (Station toStation: toStations) {
                GetTicketListRequest model = new GetTicketListRequest();
                model.setFromStationCode(fromStation.getStationCode());
                model.setToStationCode(toStation.getStationCode());
                model.setFromDate(request.getFromDate());
                tickets = TrainWebHelper.getTicketListFrom12306Cn(model);
                if (!tickets.isEmpty()) {
                    break OUT;
                }
            }
        }
        if (tickets.isEmpty()) {
            return new ArrayList<>();
        }
        for (Ticket ticket : tickets) {
            TrainLine line = TrainWebHelper.getTrainLineFrom12306(ticket.getTrainNo(),
                    TrainWebHelper.convertFromDate(request.getFromDate()),
                    StationUtil.getStationFromName(ticket.getFromStation()).getStationCode(),
                    StationUtil.getStationFromName(ticket.getToStation()).getStationCode());
            trainLines.add(line);
        }
        return trainLines;
    }

    /**
     * 根据站名精确查询称号
     *
     * @param keyword 站名
     * @return Station
     */
    public static Station getStationFromName(String keyword) {
        List<Station> stations = getAllStation();
        for (Station station : stations) {
            boolean isMatching = station.getName().equals(keyword);
            if (isMatching) {
                return station;
            }
        }
        return new Station();
    }
}

package com.sinosun.train.utils;

import com.google.common.collect.Lists;
import com.sinosun.train.model.response.Station;
import com.sinosun.train.model.response.TrainLine;

import java.util.List;
import java.util.Locale;

public class StationUtil {

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
        List<Station> stations = TrainWebHelper.getTrainAllCityFromNet();
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
     * 获取两地之间的所有可能线路
     *
     * @param fromName 出发地
     * @param targetName 到达地
     * @return 两地直接所有线路
     */
    public static List<TrainLine> getLinesBetweenStation(String fromName, String targetName, boolean isHighSpeed) {
        return null;
    }

    public static List<Station> getLinesBetweenStations(Station fromStation, Station toStation) {
        return null;
    }

}

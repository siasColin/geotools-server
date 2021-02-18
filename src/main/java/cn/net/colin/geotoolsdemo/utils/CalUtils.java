package cn.net.colin.geotoolsdemo.utils;

import org.locationtech.jts.geom.Coordinate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Package: cn.net.colin.geotoolsdemo.utils
 * @Author: sxf
 * @Date: 2020-11-12
 * @Description: wgs84 坐标系统下计算球面距离和球面面积
 */
public class CalUtils {
    private static final double EARTH_RADIUS = 6378137.0D;

    /**
     * 计算两点距离
     * @param startLon 起点经度
     * @param startLat 起点纬度
     * @param endLon 终点经度
     * @param endLat 终点纬度
     * @return  单位米
     */
    public static double calDistance(double startLon, double startLat, double endLon, double endLat) {
        double lat1 = toRadians(startLat);
        double lat2 = toRadians(endLat);
        double deltaLatBy2 = (lat2 - lat1) / 2.0D;
        double deltaLonBy2 = toRadians(endLon - startLon) / 2.0D;
        double a = Math.sin(deltaLatBy2) * Math.sin(deltaLatBy2) + Math.sin(deltaLonBy2) * Math.sin(deltaLonBy2) * Math.cos(lat1) * Math.cos(lat2);
        return EARTH_RADIUS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0D - a));
    }

    /**
     * 一个环的面积
     * @param coordinates  [[lon0,lat0],[lon1,lat1],[lon2,lat2],[lon3,lat3] ... [lonn,latn],[lon0,lat0]]
     * @return 单位平方米
     */
    public static double calArea(List<double[]> coordinates) {
        double area = 0.0D;
        int len = coordinates.size();
        if (len < 3) {
            return 0.0D;
        } else {
            double x1 = coordinates.get(len - 1)[0];
            double y1 = coordinates.get(len - 1)[1];

            for (int i = 0; i < len; ++i) {
                double x2 = coordinates.get(i)[0];
                double y2 = coordinates.get(i)[1];
                area += toRadians(x2 - x1) * (2.0D + Math.sin(toRadians(y1)) + Math.sin(toRadians(y2)));
                x1 = x2;
                y1 = y2;
            }

            return Math.abs(area * EARTH_RADIUS * EARTH_RADIUS / 2.0D);
        }
    }

    public static double calArea(Coordinate[] coordinates) {
        double area = 0.0D;
        int len = coordinates.length;
        if (len < 3) {
            return 0.0D;
        } else {
            double x1 = coordinates[len - 1].x;
            double y1 = coordinates[len - 1].y;

            for (int i = 0; i < len; ++i) {
                double x2 = coordinates[i].x;
                double y2 = coordinates[i].y;
                area += toRadians(x2 - x1) * (2.0D + Math.sin(toRadians(y1)) + Math.sin(toRadians(y2)));
                x1 = x2;
                y1 = y2;
            }

            return Math.abs(area * EARTH_RADIUS * EARTH_RADIUS / 2.0D);
        }
    }

    /**
     * 度转弧度
     * @param angleInDegrees
     * @return
     */
    static double toRadians(double angleInDegrees) {
        return angleInDegrees * 3.141592653589793D / 180.0D;
    }

}

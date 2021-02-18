package cn.net.colin.geotoolsdemo.utils.test;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;

/**
 * 计算两点间距离
 */

public class CaculateDistanceTest{
    private static String sourcePoint = "POINT(103.83489981581 33.462715497945)";
    private static String targetPoint = "POINT(104.83489981581 34.462715497945)";
    public static void main(String[] args){
        GlobalCoordinates source = new GlobalCoordinates(29.997411991870223,120.19086328125002);
        GlobalCoordinates target = new GlobalCoordinates(29.997411991870223,120.39610482816215);

        double meter1 = getDistanceMeter(source, target, Ellipsoid.Sphere);
        double meter2 = getDistanceMeter(source, target, Ellipsoid.WGS84);
        System.out.println("Sphere坐标系计算结果："+meter1 + "米");
        System.out.println("WGS84坐标系计算结果："+meter2 + "米");

        try{
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory( null );
            WKTReader reader = new WKTReader( geometryFactory );
            Point spoint = (Point) reader.read(sourcePoint);
            Point tpoint = (Point) reader.read(targetPoint);
            System.out.println(spoint.distance(tpoint)*100000);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static double getDistanceMeter(GlobalCoordinates gpsFrom, GlobalCoordinates gpsTo, Ellipsoid ellipsoid){
        //创建GeodeticCalculator，调用计算方法，传入坐标系、经纬度用于计算距离
        GeodeticCurve geoCurve = new GeodeticCalculator().calculateGeodeticCurve(ellipsoid, gpsFrom, gpsTo);

        return geoCurve.getEllipsoidalDistance();
    }
}

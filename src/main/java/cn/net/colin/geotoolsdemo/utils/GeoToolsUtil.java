package cn.net.colin.geotoolsdemo.utils;

import org.geotools.geojson.GeoJSONUtil;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;

/**
 * @Package: cn.net.colin.geotoolsdemo.utils
 * @Author: sxf
 * @Date: 2020-8-27
 * @Description:
 */
public class GeoToolsUtil {
    private static String wktPoint = "POINT(103.83489981581 33.462715497945)";
    private static String wktLine = "LINESTRING(108.32803893589 41.306670233001,99.950999898452 25.84722546391)";
    private static String wktPolygon = "POLYGON((100.02715479879 32.168082192159,102.76873121104 37.194305614622,107.0334056301 34.909658604412,105.96723702534 30.949603786713,100.02715479879 32.168082192159))";
    private static String wktPolygon1 = "POLYGON((96.219409781775 32.777321394882,96.219409781775 40.240501628236,104.82491352023001 40.240501628236,104.82491352023001 32.777321394882,96.219409781775 32.777321394882))";

    public static void main(String[] args) throws Exception {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory( null );
        WKTReader reader = new WKTReader( geometryFactory );
        Point point = (Point) reader.read(wktPoint);
        LineString line = (LineString) reader.read(wktLine);
        Polygon polygon = (Polygon) reader.read(wktPolygon);
        Polygon polygon1 = (Polygon) reader.read(wktPolygon1);
        System.out.println("-------空间关系判断-------");
        System.out.println(polygon.contains(point));
        System.out.println(polygon.intersects(line));
        System.out.println(polygon.overlaps(polygon1));
        // 设置保留6位小数，否则GeometryJSON默认保留4位小数
        /*FileInputStream in = new FileInputStream(new File("E:\\wechatFile\\WeChat Files\\wxid_eyj831t3v6gm41\\FileStorage\\File\\2020-08\\111.geojson"));
        GeometryJSON geometryJson = new GeometryJSON(14);
        Polygon polygon = geometryJson.readPolygon(in);
//        MultiPolygon multiPolygon = geometryJson.readMultiPolygon(in);
        System.out.println(polygon.toString());
//        System.out.println(multiPolygon.toString());
//        System.out.println(multiPolygon.contains(point));
        in.close();*/
        /*GeometryJSON geometryJson = new GeometryJSON(14);
        StringWriter writer = new StringWriter();
        geometryJson.writePolygon(polygon,writer);
        System.out.println(writer);
        writer.close();*/

        FileInputStream in = new FileInputStream(new File("E:\\wechatFile\\WeChat Files\\wxid_eyj831t3v6gm41\\FileStorage\\File\\2020-08\\111.geojson"));
        GeometryJSON geometryJson = new GeometryJSON(14);
        MultiPolygon multiPolygon = geometryJson.readMultiPolygon(in);
        multiPolygonUnion(multiPolygon);
    }

    public static void multiPolygonUnion(MultiPolygon multiPolygon){
        WKTWriter write = new WKTWriter();
        Geometry union = multiPolygon.union();
        System.out.println(write.write(union));
    }
}

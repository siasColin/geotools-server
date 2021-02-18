package cn.net.colin.geotoolsdemo.utils.test;

import org.geotools.data.DataUtilities;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeatureType;

import java.awt.geom.Point2D;
import java.io.StringWriter;

/**
 * @Package: cn.net.colin.geotoolsdemo.utils.test
 * @Author: sxf
 * @Date: 2020-10-13
 * @Description:
 */
public class CTest {
    private static String sourcePoint = "POINT(103.83489981581 33.462715497945)";
    public static void main(String[] args) {
        SimpleFeatureType TYPE = null;
        try {
            TYPE = DataUtilities.createType("", "Location", "locations:Point:srid=4326," + "id:Integer" // a
                    // number
                    // attribute
            );
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Point point = geometryFactory.createPoint(
                new Coordinate(
                        120.080,
                        29.185
                )
        );
        try {
            GeodeticCalculator calc = new  GeodeticCalculator(DefaultGeographicCRS.WGS84);
            calc.setStartingGeographicPoint(point.getX(), point.getY());
            calc.setDirection(0.0, 20000);
            Point2D p2 = calc.getDestinationGeographicPoint();
            calc.setDirection(90.0, 20000);
            Point2D p3 = calc.getDestinationGeographicPoint();

            double dy = p2.getY() - point.getY();
            double dx = p3.getX() - point.getX();
            double distance = (dy + dx) / 2.0;
            Polygon p1 = (Polygon) point.buffer(distance);
            GeometryJSON geometryJson = new GeometryJSON(14);
            StringWriter writer = new StringWriter();
            geometryJson.write(p1,writer);
            System.out.println(writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

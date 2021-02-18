package cn.net.colin.geotoolsdemo.utils.test;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @Package: cn.net.colin.geotoolsdemo.utils.test
 * @Author: sxf
 * @Date: 2020-10-15
 * @Description:
 */
public class ShpTest {
    @Test
    public void test3(){
        SimpleFeatureSource simpleFeatureSource = readStoreByShp("D:\\突发\\浙江\\应急辅助一张图\\省市县边界\\zj.shp");
        GeometryJSON geometryJson = new GeometryJSON(4);
        FeatureJSON fjson = new FeatureJSON();
        try {
            SimpleFeatureType TYPE = simpleFeatureSource.getSchema();
            SimpleFeatureCollection featureCollection = simpleFeatureSource.getFeatures();
            SimpleFeatureIterator iters = featureCollection.features();
            //遍历打印
            while(iters.hasNext()){
                SimpleFeature sf = iters.next();
                try{
                    System.out.println(sf.getBounds().getMinY());
                    System.out.println(sf.getBounds().getMaxY());
                    System.out.println(sf.getBounds().getMinX());
                    System.out.println(sf.getBounds().getMaxX());
                    Geometry geometry = (Geometry)sf.getDefaultGeometry();
                    System.out.println(geometry);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SimpleFeatureSource readStoreByShp(String path ){

        File file = new File(path);

        FileDataStore store;
        SimpleFeatureSource featureSource = null;
        try {
            store = FileDataStoreFinder.getDataStore(file);
            ((ShapefileDataStore) store).setCharset(Charset.forName("UTF-8"));
            featureSource = store.getFeatureSource();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return featureSource ;
    }
}

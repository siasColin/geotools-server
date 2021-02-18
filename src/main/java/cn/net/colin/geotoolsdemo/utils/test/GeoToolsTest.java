package cn.net.colin.geotoolsdemo.utils.test;

import cn.net.colin.geotoolsdemo.utils.*;
import org.apache.commons.io.FileUtils;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @Package: cn.net.colin.geotoolsdemo.utils
 * @Author: sxf
 * @Date: 2020-9-1
 * @Description:
 */
public class GeoToolsTest {

    @Test
    public void test1(){
        try{
            FileInputStream in = new FileInputStream(new File("E:\\FTPROOT\\geotools-test\\geojson\\g2.geojson"));
            GeometryJSON geometryJson = new GeometryJSON(14);
            FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(14));
            SimpleFeatureCollection featureCollection = (SimpleFeatureCollection)featureJSON.readFeatureCollection(in);
            System.out.println(featureCollection.toString());
            SimpleFeatureIterator features = featureCollection.features();
            double maxX = 180;
            double minX = -180;
            double maxY = 90;
            double minY = -90;
            MultiPolygon resultMultiPolygon = null;
            if(features.hasNext()){
                SimpleFeature feature = features.next();
                resultMultiPolygon = (MultiPolygon)feature.getDefaultGeometry();
                BoundingBox bounds = feature.getBounds();
                maxX = bounds.getMaxX();
                minX = bounds.getMinX();
                maxY = bounds.getMaxY();
                minY = bounds.getMinY();
            }
            while (features.hasNext()){
                SimpleFeature simpleFeature = features.next();
                MultiPolygon m = (MultiPolygon)simpleFeature.getDefaultGeometry();
                resultMultiPolygon = (MultiPolygon)resultMultiPolygon.union(m);
                BoundingBox bounds = simpleFeature.getBounds();
                if(bounds.getMaxX() > maxX){
                    maxX = bounds.getMaxX();
                }
                if(bounds.getMinX() < minX){
                    minX = bounds.getMinX();
                }
                if(bounds.getMaxY() > maxY){
                    maxY = bounds.getMaxY();
                }
                if(bounds.getMinY() < minY){
                    minY = bounds.getMinY();
                }
            }
            System.out.println(resultMultiPolygon.toString());
            StringWriter writer = new StringWriter();
            geometryJson.write(resultMultiPolygon,writer);
            System.out.println(writer);
            StringBuilder maxPolygonSb = new StringBuilder("POLYGON((");
            maxPolygonSb.append(minX);
            maxPolygonSb.append(" ");
            maxPolygonSb.append(minY);
            maxPolygonSb.append(",");

            maxPolygonSb.append(minX);
            maxPolygonSb.append(" ");
            maxPolygonSb.append(maxY);
            maxPolygonSb.append(",");

            maxPolygonSb.append(maxX);
            maxPolygonSb.append(" ");
            maxPolygonSb.append(maxY);
            maxPolygonSb.append(",");

            maxPolygonSb.append(maxX);
            maxPolygonSb.append(" ");
            maxPolygonSb.append(minY);
            maxPolygonSb.append(",");

            maxPolygonSb.append(minX);
            maxPolygonSb.append(" ");
            maxPolygonSb.append(minY);
            maxPolygonSb.append("))");
            //创建GeometryFactory工厂
            GeometryFactory geometryFactory = new GeometryFactory();
            WKTReader reader = new WKTReader( geometryFactory );
            Polygon polygon = (Polygon)reader.read(maxPolygonSb.toString());
            StringWriter polygonWriter = new StringWriter();
            geometryJson.write(polygon,polygonWriter);
            System.out.println(polygonWriter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Test
    public void test2(){
        /*double maxX = 125.32;
        double minX = 114.67;
        double maxY = 32.42;
        double minY = 25.43;*/
        double maxX = 123.17;
        double minX = 118;
        double maxY = 31.25;
        double minY = 27.14;
        try{
            GeometryJSON geometryJson = new GeometryJSON(14);
            StringBuilder maxPolygonSb = new StringBuilder("POLYGON((");
            maxPolygonSb.append(minX);
            maxPolygonSb.append(" ");
            maxPolygonSb.append(minY);
            maxPolygonSb.append(",");

            maxPolygonSb.append(minX);
            maxPolygonSb.append(" ");
            maxPolygonSb.append(maxY);
            maxPolygonSb.append(",");

            maxPolygonSb.append(maxX);
            maxPolygonSb.append(" ");
            maxPolygonSb.append(maxY);
            maxPolygonSb.append(",");

            maxPolygonSb.append(maxX);
            maxPolygonSb.append(" ");
            maxPolygonSb.append(minY);
            maxPolygonSb.append(",");

            maxPolygonSb.append(minX);
            maxPolygonSb.append(" ");
            maxPolygonSb.append(minY);
            maxPolygonSb.append("))");
            //创建GeometryFactory工厂
            GeometryFactory geometryFactory = new GeometryFactory();
            WKTReader reader = new WKTReader( geometryFactory );
            Polygon polygon = (Polygon)reader.read(maxPolygonSb.toString());
            StringWriter polygonWriter = new StringWriter();
            geometryJson.write(polygon,polygonWriter);
            System.out.println(polygonWriter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void test3(){
        SimpleFeatureSource simpleFeatureSource = readStoreByShp("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\BOU_TOW_PY.shp");
        GeometryJSON geometryJson = new GeometryJSON(14);
        FeatureJSON fjson = new FeatureJSON();
        try {
            SimpleFeatureType TYPE = simpleFeatureSource.getSchema();
            SimpleFeatureCollection featureCollection = simpleFeatureSource.getFeatures();
            SimpleFeatureIterator iters = featureCollection.features();
            //遍历打印
            while(iters.hasNext()){
                SimpleFeature sf = iters.next();
                String areacode = sf.getAttribute("GNID").toString();
                FileOutputStream out = new FileOutputStream(new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\geojson\\"+areacode+".geojson"));
                OutputStreamWriter outWriter = new OutputStreamWriter(out, "UTF-8");
                try{
                   /* List<Object> attributes = sf.getAttributes();
                    MultiPolygon multiPolygon = (MultiPolygon)sf.getDefaultGeometry();
                    multiPolygon.setUserData(attributes);
                    geometryJson.write(multiPolygon,outWriter);*/
                    fjson.writeFeature(sf,outWriter);
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    out.flush();
                    out.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test11(){
        SimpleFeatureSource simpleFeatureSource = readStoreByShp("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\BOU_TOW_PY.shp");
        Map<String,Object> resultMap = new HashMap<String,Object>();
        List<Map<String,Object>> areaList = new ArrayList<Map<String,Object>>();
        try {
            SimpleFeatureCollection featureCollection = simpleFeatureSource.getFeatures();
            SimpleFeatureIterator iters = featureCollection.features();
            //遍历打印
            while(iters.hasNext()){
                SimpleFeature sf = iters.next();
                String areacode = sf.getAttribute("GNID").toString();
                String areaname = sf.getAttribute("NAME").toString();
                String county = sf.getAttribute("COUNTY").toString();
                String city = sf.getAttribute("CITY").toString();
                try{
                    Map<String,Object> areaInfo = new HashMap<String,Object>();
                    areaInfo.put("areaname",areaname);
                    areaInfo.put("areacode",areacode);
                    areaInfo.put("county",county);
                    areaInfo.put("city",city);
//                    resultMap.put(areacode,areaInfo);
                    areaList.add(areaInfo);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            FileUtils.writeStringToFile(new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\areaInfo2.json"),JsonUtils.toString(areaList),"UTF-8");
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

    @Test
    public void test4() throws Exception {
//        FileInputStream in = new FileInputStream(new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\geojson\\330102003000.geojson"));
        InputStream in = ResourceRenderer.resourceLoader("classpath:/geojson/330102003000.geojson");
        FileInputStream intest1 = new FileInputStream(new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\testGeojson\\test1.geojson"));
        FileInputStream intest2 = new FileInputStream(new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\testGeojson\\test2.geojson"));
        FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(14));
        FeatureJSON featureJSON_test1 = new FeatureJSON(new GeometryJSON(14));
        FeatureJSON featureJSON_test2 = new FeatureJSON(new GeometryJSON(14));
        SimpleFeature simpleFeature = featureJSON.readFeature(in);
        SimpleFeature simpleFeature_test1 = ((SimpleFeatureCollection)featureJSON_test1.readFeatureCollection(intest1)).features().next();
        SimpleFeature simpleFeature_test2 = ((SimpleFeatureCollection)featureJSON_test2.readFeatureCollection(intest2)).features().next();

        MultiPolygon resultMultiPolygon = (MultiPolygon)simpleFeature.getDefaultGeometry();
        MultiPolygon resultMultiPolygon_test1 = (MultiPolygon)simpleFeature_test1.getDefaultGeometry();
        MultiPolygon resultMultiPolygon_test2 = (MultiPolygon)simpleFeature_test2.getDefaultGeometry();
        System.out.println(resultMultiPolygon.intersects(resultMultiPolygon_test1));
        System.out.println(resultMultiPolygon.intersects(resultMultiPolygon_test2));


        System.out.println(resultMultiPolygon.overlaps(resultMultiPolygon_test1));
        System.out.println(resultMultiPolygon.overlaps(resultMultiPolygon_test2));

        in.close();
    }

    @Test
    public void test5(){
        try{
            FileInputStream in = new FileInputStream(
                    new File("E:\\wechatFile\\WeChat Files\\wxid_eyj831t3v6gm41\\FileStorage\\File\\2020-09\\three1.geojson"));
            GeometryJSON geometryJson = new GeometryJSON(14);
            FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(14));
            SimpleFeatureCollection featureCollection = (SimpleFeatureCollection)featureJSON.readFeatureCollection(in);
            SimpleFeatureIterator features = featureCollection.features();
            Geometry geometry = null;
            while (features.hasNext()) {
                SimpleFeature simpleFeature = features.next();
                MultiPolygon resultMultiPolygon = (MultiPolygon)simpleFeature.getDefaultGeometry();
                Geometry geo = resultMultiPolygon.intersection(resultMultiPolygon);
                if(geometry == null){
                    geometry = geo;
                }else{
                    geometry = geometry.union(geo);
                }
            }
            FileOutputStream out = new FileOutputStream(new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\geojson\\aaaa.geojson"));
            geometryJson.write(geometry,out);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void test6(){
        try{
            FileInputStream in = new FileInputStream(
                    new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\geojson\\aaaa.geojson"));
            GeometryJSON geometryJson = new GeometryJSON(14);
            Polygon polygon = geometryJson.readPolygon(in);
            FileOutputStream out = new FileOutputStream(new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\geojson\\bbbb.geojson"));
            geometryJson.write( polygon.intersection(polygon),out);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Test
    public void test7(){
        try{
            FileInputStream in = new FileInputStream(
                    new File("C:\\Users\\sxf\\Desktop\\aaaa\\c.geojson"));
            FileInputStream inb = new FileInputStream(
                    new File("C:\\Users\\sxf\\Desktop\\aaaa\\b.geojson"));

            GeometryJSON geometryJson = new GeometryJSON(14);
            MultiPolygon c = geometryJson.readMultiPolygon(in);
            MultiPolygon b = geometryJson.readMultiPolygon(inb);
            Geometry difference = b.difference(c);
            FileOutputStream out = new FileOutputStream(new File("C:\\Users\\sxf\\Desktop\\aaaa\\b1.geojson"));
            OutputStreamWriter outWriter = new OutputStreamWriter(out, "utf-8");
            geometryJson.write(difference,outWriter);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Test
    public void test8() {
        try {
            FileInputStream in = new FileInputStream(
                    new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\newgeojson\\3333.geojson"));

            FileInputStream in2 = new FileInputStream(
                    new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\newgeojson\\330305004000.geojson"));
            FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(14));
            SimpleFeatureCollection featureCollection = (SimpleFeatureCollection)featureJSON.readFeatureCollection(in);
            SimpleFeatureIterator features = featureCollection.features();
            //合并多面
            Geometry geometry = null;
            while (features.hasNext()) {
                SimpleFeature simpleFeature = features.next();
                MultiPolygon m = (MultiPolygon)simpleFeature.getDefaultGeometry();
                Geometry geo = m.intersection(m);
                if(geometry == null){
                    geometry = geo;
                }else{
                    geometry = geometry.union(geo);
                }
            }
            if(geometry != null){
                SimpleFeature simpleFeature = featureJSON.readFeature(in2);
                Geometry geo = (Geometry)simpleFeature.getDefaultGeometry();
                System.out.println(geometry.intersects(geo));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test9() {
        try {
            FileInputStream in = new FileInputStream(
                    new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\newgeojson\\test\\2000.geojson"));
            FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(14));
            SimpleFeature simpleFeature = featureJSON.readFeature(in);
            SimpleFeatureType featureType = simpleFeature.getFeatureType();
            Geometry geo = (Geometry)simpleFeature.getDefaultGeometry();
            System.out.println(geo.getArea()+"###"+geo.getLength());
            geo = GeoUtil.transEPSG(geo);
            System.out.println(geo.getArea()+"###"+geo.getLength());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void test10() {
        try {
            String allStr = FileUtils.readFileToString(new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\newgeojson\\zhejiang_4p.geojson"),"UTF-8");
            Map<String, Object> map = JsonUtils.toMap(allStr, String.class, Object.class);
            List features = (List)map.get("features");
            for (int i = 0; i < features.size(); i++) {
                Map<String, Object> feature =  (Map<String, Object>)features.get(i);
                String citycode = ((Map<String, Object>)feature.get("properties")).get("GNID").toString();
                System.out.println(citycode);
                FileUtils.writeStringToFile(new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\newgeojson\\precision_4\\"+citycode+".geojson"),JsonUtils.toString(feature),"UTF-8");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Test
    public void test12() {
        String [] areacodeArr = {"330782101000","330782100000","331122101000","331081105000"};
        try {
            FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(6));
            GeometryJSON geometryJson = new GeometryJSON(6);
            Geometry geometry = null;
            for (int i = 0; i < areacodeArr.length; i++) {
                FileInputStream in = null;
                try {
                    in = new FileInputStream(new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\newgeojson\\precision_6\\"+areacodeArr[i]+".geojson"));
                    SimpleFeature simpleFeature = featureJSON.readFeature(in);
                    MultiPolygon areaMultiPolygon = (MultiPolygon)simpleFeature.getDefaultGeometry();
                    Geometry m = (Geometry)simpleFeature.getDefaultGeometry();
                    m = m.intersection(m);
                    if(geometry != null){
                        geometry = geometry.union(m);
                    }else{
                        geometry = m;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if(in != null){
                        in.close();
                        in = null;
                    }
                }
            }
            FileOutputStream out = new FileOutputStream(new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\test.geojson"));
            geometryJson.write(geometry,out);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void test13(){
        FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(14));
        GeometryJSON geometryJson = new GeometryJSON(14);
        FileInputStream in = null;
        try {
            in = new FileInputStream(new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\geojson\\330110102000.geojson"));
            SimpleFeature simpleFeature = featureJSON.readFeature(in);
            Geometry geometry = (Geometry)simpleFeature.getDefaultGeometry();
            int numGeometries = geometry.getNumGeometries();
            for (int i = 0; i < numGeometries; i++) {
                System.out.println(new BigDecimal(GeometryUtil.getAreaLonLat((Polygon)geometry.getGeometryN(i))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (in != null) {
                try {
                    in.close();
                    in = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void test14() {
        try {
            String allStr = FileUtils.readFileToString(new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\zjx.geojson"),"UTF-8");
            Map<String, Object> map = JsonUtils.toMap(allStr, String.class, Object.class);
            List features = (List)map.get("features");
            for (int i = 0; i < features.size(); i++) {
                Map<String, Object> feature =  (Map<String, Object>)features.get(i);
                String citycode = ((Map<String, Object>)feature.get("properties")).get("PAC").toString()+"000000";
                System.out.println(citycode);
                FileUtils.writeStringToFile(new File("D:\\突发\\浙江\\应急辅助一张图\\全省乡镇\\xian\\"+citycode+".geojson"),JsonUtils.toString(feature),"UTF-8");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

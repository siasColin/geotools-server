package cn.net.colin.geotoolsdemo.utils;

import cn.net.colin.geotoolsdemo.common.Constants;
import cn.net.colin.geotoolsdemo.thread.PointFilterByMultiPolygonThread;
import cn.net.colin.geotoolsdemo.thread.PointFilterByPolygonThread;
import org.apache.commons.io.FileUtils;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.io.File;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Package: cn.net.colin.geotoolsdemo.utils
 * @Author: sxf
 * @Date: 2020-8-27
 * @Description:
 */
public class GeoUtil {
    public static Map<String,String> getLonLatExtent(String polygon){
        Map<String,String> extentMap = new HashMap<String,String>();
        String [] pointArr = polygon.split(",");
        double [] lonArr = new double[pointArr.length];
        double [] latArr = new double[pointArr.length];
        for (int i=0;i<pointArr.length;i++) {
            String [] lonlat = pointArr[i].trim().split(" ");
            lonArr[i] = Double.parseDouble(lonlat[0]);
            latArr[i] = Double.parseDouble(lonlat[1]);
        }
        Arrays.sort(lonArr);
        Arrays.sort(latArr);
        extentMap.put("minLon",lonArr[0]+"");
        extentMap.put("maxLon",lonArr[lonArr.length-1]+"");
        extentMap.put("minLat",latArr[0]+"");
        extentMap.put("maxLat",latArr[latArr.length-1]+"");
        return extentMap;
    }

    public static Map<String,Object> featureUnion(String geoJson,String [] lonlats,double [] radius){
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try{
            FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(14));
            SimpleFeatureCollection featureCollection = (SimpleFeatureCollection)featureJSON.readFeatureCollection(geoJson);
            SimpleFeatureIterator features = featureCollection.features();
            double maxX = Double.MIN_VALUE;
            double minX = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE;
//            MultiPolygon resultMultiPolygon = null;
            Geometry geometry = null;
            Set<String> areaCodeSet = new HashSet<String>();
            if(features.hasNext()){
                SimpleFeature feature = features.next();
                Object areaCodes = feature.getAttribute("areaCodes");
                if(areaCodes != null){
                    String [] areaCodeArr = areaCodes.toString().split(",");
                    if(areaCodeArr != null && areaCodeArr.length > 0){
                        for (String areacode : areaCodeArr) {
                            areaCodeSet.add(areacode);
                        }
                    }
                }
                Geometry m = (Geometry)feature.getDefaultGeometry();
                geometry = m.intersection(m);
                BoundingBox bounds = feature.getBounds();
                maxX = bounds.getMaxX();
                minX = bounds.getMinX();
                maxY = bounds.getMaxY();
                minY = bounds.getMinY();
            }
            while (features.hasNext()){
                SimpleFeature simpleFeature = features.next();
                Object areaCodes = simpleFeature.getAttribute("areaCodes");
                if(areaCodes != null){
                    String [] areaCodeArr = areaCodes.toString().split(",");
                    if(areaCodeArr != null && areaCodeArr.length > 0){
                        for (String areacode : areaCodeArr) {
                            areaCodeSet.add(areacode);
                        }
                    }
                }
                Geometry m = (Geometry)simpleFeature.getDefaultGeometry();
                m = m.intersection(m);
                geometry = geometry.union(m);
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
            if(lonlats != null && lonlats.length > 0 && radius != null && radius.length > 0){
                GeometryJSON geometryJson = new GeometryJSON(14);
                for (int i = 0; i < lonlats.length; i++) {
                    String [] lonlatArr = lonlats[i].split(" ");
                    double lon = Double.parseDouble(lonlatArr[0]);
                    double lat = Double.parseDouble(lonlatArr[1]);
                    Geometry geo = BufferPoint.getGeometryByPointAndRadius(lon,lat,radius[i]);
                    if(geometry == null){
                        geometry = geo;
                    }else{
                        geometry = geometry.union(geo);
                    }
                }
                StringBuffer geojsonSb = new StringBuffer();
                geojsonSb.append("{\"type\": \"Feature\", \"geometry\":");
                StringWriter writer = new StringWriter();
                geometryJson.write(geometry,writer);
                geojsonSb.append(writer.toString());
                geojsonSb.append("}");
                SimpleFeature simpleFeature = featureJSON.readFeature(geojsonSb.toString());
                writer.close();
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
            resultMap.put("resultGeometry",geometry);
            resultMap.put("areaCodeSet",areaCodeSet);
            resultMap.put("maxX",maxX);
            resultMap.put("minX",minX);
            resultMap.put("maxY",maxY);
            resultMap.put("minY",minY);
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultMap;
    }

    public static List<Map<String,Object>> pointFilterByMultiPolygon(List<Map<String,Object>> pointList,Geometry polygon){
        List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
        try{
            int threadNum = 1;
            if(pointList.size() > 100 && pointList.size() < 1000){
                threadNum = 3;
            }else if(pointList.size() >= 1000){
                threadNum = 5;
            }
            int baseSize = pointList.size()/threadNum;
            ThreadPoolExecutor pointFilterByMultiPolygonThreadPool = Constants.pointFilterByPolygonThreadPool;
            List<Future<Map<String,Object>>> futureList = new ArrayList<Future<Map<String,Object>>>();
            for (int i = 0; i < threadNum; i++) {
                List<Map<String,Object>> baseList = new ArrayList<Map<String,Object>>();
                if(threadNum == 1){
                    baseList = pointList;
                }else{
                    if(i == threadNum - 1){
                        baseList = pointList.subList(i*baseSize,pointList.size());
                    }else{
                        baseList = pointList.subList(i*baseSize,(i+1)*baseSize);
                    }
                }
                PointFilterByMultiPolygonThread pointFilterByMultiPolygonThread = new PointFilterByMultiPolygonThread(baseList,polygon);
                Future<Map<String, Object>> future = pointFilterByMultiPolygonThreadPool.submit(pointFilterByMultiPolygonThread);
                futureList.add(future);
            }
            //获取结果
            for (Future<Map<String,Object>> future : futureList) {
                Map<String, Object> resultMap = future.get();
                if(resultMap != null && resultMap.get("code") != null && !"-1".equals(resultMap.get("code").toString())){
                    resultList.addAll((ArrayList)resultMap.get("data"));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultList;
    }

    public static Geometry transEPSG(Geometry geometry){
        try{
            CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326",true);
            CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:3857",true);
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, false);
            geometry = JTS.transform(geometry, transform);
        }catch (Exception e){
            e.printStackTrace();
        }
        return geometry;
    }

    public static void main(String[] args) {
//        String polygon = "120.64285142590434 30.08985112075478, 120.64285142590434 30.08985112075478, 120.39323548612502 29.883944371868196, 120.41700843277069 29.842711831724714, 120.49427050936904 29.72407346555029, 120.69634055585705 29.59495946154013, 120.77954586911682 29.57428585439994, 121.16585625210863 29.476028430760056, 121.39169924524225 29.55877787036453, 121.47490455850202 29.641459587499522, 121.47490455850202 29.76535492865746, 121.39764248190367 29.925159867545435, 121.2490615653684 29.98695112147673, 121.18962919875423 30.079565930900696, 121.0053888622505 30.20805386313804, 120.8270917624081 30.213189898509235, 120.63096495258152 30.259402149713534, 120.63096495258152 30.259402149713534, 120.64285142590434 30.08985112075478";
//        System.out.println(JsonUtils.toString(getLonLatExtent(polygon)));
        try{
            Map<String, Object> map = featureUnion(FileUtils.readFileToString(new File("E:\\FTPROOT\\geotools-test\\geojson\\g2.geojson")),null,null);
            MultiPolygon multiPolygon = (MultiPolygon)map.get("resultMultiPolygon");
            System.out.println("结果面："+multiPolygon);
            System.out.println("maxX："+map.get("maxX").toString());
            System.out.println("minX："+map.get("minX").toString());
            System.out.println("maxY："+map.get("maxY").toString());
            System.out.println("minY："+map.get("minY").toString());
            Set<String> areaCodeSet =  (HashSet)map.get("areaCodeSet");
            System.out.println("+++++++++++++++++地区编码+++++++++++++++++++++");
            for (String areaCode : areaCodeSet) {
                System.out.println(areaCode);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

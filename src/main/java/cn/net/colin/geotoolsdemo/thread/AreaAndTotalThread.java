package cn.net.colin.geotoolsdemo.thread;

import cn.net.colin.geotoolsdemo.common.Constants;
import cn.net.colin.geotoolsdemo.utils.JsonUtils;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @Package: cn.net.colin.geotoolsdemo.thread
 * @Author: sxf
 * @Date: 2020-8-27
 * @Description:
 */
public class AreaAndTotalThread implements Callable<Map<String, Object>> {
    private static final Logger logger = LoggerFactory.getLogger(AreaAndTotalThread.class);

    private List<File> resourceList;
    private Geometry polygon;
    private String relationshipType;

    public AreaAndTotalThread(List<File> resourceList, Geometry polygon,String relationshipType) {
        this.resourceList = resourceList;
        this.polygon = polygon;
        this.relationshipType = relationshipType;
    }

    @Override
    public Map<String, Object> call() throws Exception {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
        try{
            FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(6));
            List<File> filterList = resourceList.stream().filter(file -> {
                FileInputStream in = null;
                try{
                    in = new FileInputStream(file);
                    SimpleFeature simpleFeature = featureJSON.readFeature(in);
                    MultiPolygon areaMultiPolygon = (MultiPolygon)simpleFeature.getDefaultGeometry();
                    boolean i = false;
                    if(relationshipType == null || (relationshipType != null && relationshipType.trim().toString().equals(""))){
                        i = polygon.intersects(areaMultiPolygon);
                    }else if(relationshipType.trim().equals("intersects")){
                        i = polygon.intersects(areaMultiPolygon);
                    }else if(relationshipType.trim().equals("contains")){
                        i = polygon.contains(areaMultiPolygon);
                    }
                    if(i){
                        String areacode = file.getName().substring(0,12);
                        resultList.add((Map<String,Object>)Constants.areaInfoMap.get(areacode));
                    }
                    return i;
                }catch (Exception e){
                    logger.error("计算出错+++++++++++++++"+file.getName());
                    return false;
                }finally {
                    if(in != null){
                        try {
                            in.close();
                            in = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).collect(Collectors.toList());
            resultMap.put("code","0");
            resultMap.put("msg","成功");
            resultMap.put("data",resultList);
        }catch (Exception e){
            resultMap.put("code","-1");
            resultMap.put("msg","失败："+e.getMessage());
            e.printStackTrace();
        }
        return resultMap;
    }
}

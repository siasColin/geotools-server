package cn.net.colin.geotoolsdemo.thread;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.springframework.util.StringUtils;

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
public class PointFilterByMultiPolygonThread implements Callable<Map<String, Object>> {

    private List<Map<String,Object>> resourceList;
    private Geometry polygon;

    public PointFilterByMultiPolygonThread(List<Map<String,Object>> resourceList, Geometry polygon) {
        this.resourceList = resourceList;
        this.polygon = polygon;
    }

    @Override
    public Map<String, Object> call() throws Exception {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
        try{
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory( null );
            WKTReader reader = new WKTReader( geometryFactory );
            resultList = resourceList.parallelStream().filter(resourceMap -> {
                try{
                    StringBuilder wktPointSb = new StringBuilder("POINT(");
                    if(StringUtils.isEmpty(resourceMap.get("Lon")) || StringUtils.isEmpty(resourceMap.get("Lat"))){
                        return false;
                    }
                    wktPointSb.append(resourceMap.get("Lon").toString().trim());
                    wktPointSb.append(" ");
                    wktPointSb.append(resourceMap.get("Lat").toString().trim());
                    wktPointSb.append(")");
                    Point point = (Point) reader.read(wktPointSb.toString());
                    return polygon.contains(point);
                }catch (Exception e){
                    e.printStackTrace();
                    return false;
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

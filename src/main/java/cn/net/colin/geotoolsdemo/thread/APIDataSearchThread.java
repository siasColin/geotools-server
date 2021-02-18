package cn.net.colin.geotoolsdemo.thread;

import cn.net.colin.geotoolsdemo.utils.GeoUtil;
import cn.net.colin.geotoolsdemo.utils.HttpClientUtil;
import cn.net.colin.geotoolsdemo.utils.JsonUtils;
import org.locationtech.jts.geom.Geometry;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * @Package: cn.net.colin.geotoolsdemo.thread
 * @Author: sxf
 * @Date: 2020-9-14
 * @Description:
 */
public class APIDataSearchThread implements Callable<Map<String, Object>> {
    private Map<String, Object> featureMap;
    private String apiUrl;
    private String apiUserCode;
    private String apiPassword;
    private int defaultLimit;
    private String interfaceCode;

    public APIDataSearchThread(Map<String, Object> featureMap,String apiUrl,String apiUserCode,String apiPassword,
                               int defaultLimit,String interfaceCode) {
        this.featureMap = featureMap;
        this.apiUrl = apiUrl;
        this.apiUserCode = apiUserCode;
        this.apiPassword = apiPassword;
        this.defaultLimit = defaultLimit;
        this.interfaceCode = interfaceCode;
    }
    @Override
    public Map<String, Object> call() throws Exception {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        resultMap.put("interfaceCode",interfaceCode);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            String maxX = featureMap.get("maxX").toString();
            String minX = featureMap.get("minX").toString();
            String maxY = featureMap.get("maxY").toString();
            String minY = featureMap.get("minY").toString();
            Map<String,Object> mapParms = new HashMap<String,Object>();
//            mapParms.put("areaCode", "330300000000");
//            mapParms.put("isChildrens", "true");
            mapParms.put("offset", 0);
            mapParms.put("limit", defaultLimit);
            mapParms.put("maxLng", maxX);
            mapParms.put("minLng", minX);
            mapParms.put("maxLat", maxY);
            mapParms.put("minLat", minY);

            Map<String,String> dataParams = new HashMap<String, String>();
            dataParams.put("userCode", apiUserCode);
            dataParams.put("password", apiPassword);
            dataParams.put("interfaceCode", interfaceCode);
            dataParams.put("dataformat", "JSON");
            dataParams.put("params", JsonUtils.toString(mapParms));
            dataParams.put("token", "");
            System.out.println("开始请求接口时间："+dateFormat.format(new Date()));
            String resultStr = HttpClientUtil.doPost(apiUrl,dataParams);
            System.out.println("接口请求结束时间："+dateFormat.format(new Date()));
            Map<String, Object> map = JsonUtils.toMap(resultStr, String.class, Object.class);
            Geometry resultGeometry = (Geometry)featureMap.get("resultGeometry");
            List<Map<String,Object>> filterList = GeoUtil.pointFilterByMultiPolygon((ArrayList<Map<String,Object>>)map.get("data"),resultGeometry);
            resultMap.put("filterList",filterList);
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultMap;
    }
}

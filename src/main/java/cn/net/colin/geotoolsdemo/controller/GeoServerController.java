package cn.net.colin.geotoolsdemo.controller;

import cn.net.colin.geotoolsdemo.common.Constants;
import cn.net.colin.geotoolsdemo.entities.PointsAndPolygon;
import cn.net.colin.geotoolsdemo.entities.common.ResultCode;
import cn.net.colin.geotoolsdemo.entities.common.ResultInfo;
import cn.net.colin.geotoolsdemo.thread.APIDataSearchThread;
import cn.net.colin.geotoolsdemo.thread.AreaAndTotalThread;
import cn.net.colin.geotoolsdemo.thread.PointFilterByPolygonThread;
import cn.net.colin.geotoolsdemo.utils.*;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Package: cn.net.colin.geotoolsdemo.controller
 * @Author: sxf
 * @Date: 2020-8-27
 * @Description:
 */
@RestController
@RequestMapping("/geoServer")
@Api(tags = "空间计算分析",description = "空间计算分析相关接口")
@CrossOrigin(origins = "*",maxAge = 3600)
@Slf4j
public class GeoServerController {

    @Value("${api.url}")
    private String apiUrl;
    @Value("${api.userCode}")
    private String apiUserCode;
    @Value("${api.password}")
    private String apiPassword;
    @Value("${api.defaultLimit}")
    private int defaultLimit;
    @Value("${geojson.path}")
    private String geoJsonPath;
    @Value("${geojson.countysPath}")
    private String countysPath;


    @ApiOperation(value = "点过滤",response = ResultInfo.class)
    @PostMapping(value = "/pointFilterByPolygon")
    @ResponseBody
    public ResultInfo pointFilterByPolygon(@RequestBody PointsAndPolygon pointsAndPolygon) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("开始："+format.format(new Date()));
        List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
        try{
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory( null );
            WKTReader reader = new WKTReader( geometryFactory );
            List<Map<String,Object>> pointList = pointsAndPolygon.getPoints();
            String wktPolygon = "POLYGON(("+pointsAndPolygon.getPolygon().toString().trim()+"))";
            Polygon polygon = (Polygon) reader.read(wktPolygon);
            int threadNum = 1;
            if(pointList.size() > 100 && pointList.size() < 1000){
                threadNum = 3;
            }else if(pointList.size() >= 1000){
                threadNum = 5;
            }
            int baseSize = pointList.size()/threadNum;
            ThreadPoolExecutor pointFilterByPolygonThreadPool = Constants.pointFilterByPolygonThreadPool;
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
                PointFilterByPolygonThread pointFilterByPolygonThread = new PointFilterByPolygonThread(baseList,polygon);
                Future<Map<String, Object>> future = pointFilterByPolygonThreadPool.submit(pointFilterByPolygonThread);
                futureList.add(future);
            }
            //获取结果
            for (Future<Map<String,Object>> future : futureList) {
                Map<String, Object> resultMap = future.get();
                if(resultMap != null && resultMap.get("code") != null && !"-1".equals(resultMap.get("code").toString())){
                    resultList.addAll((ArrayList)resultMap.get("data"));
                }
            }
            log.info("结束："+format.format(new Date()));
            return ResultInfo.ofDataAndTotal(ResultCode.SUCCESS,resultList,resultList.size());
        }catch (Exception e){
            e.printStackTrace();
            return ResultInfo.of(ResultCode.UNKNOWN_ERROR);
        }
    }

    @PostMapping(value = "/multiPolygonHandler")
    @ResponseBody
    @ApiOperation(value = "多面处理，并检索面内责任人等信息",response = ResultInfo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "geojson", value = "geojson数据", required = true, paramType="body",example = "{\"type\": \"FeatureCollection\"}"),
            @ApiImplicitParam(name="centerLonlats",value="中心点经纬度集合",dataType = "String",required=false,paramType="query"),
            @ApiImplicitParam(name="otherLonlats",value="其他点经纬度集合（和中心点经纬度集合一一对应）",dataType = "String",required=false,paramType="query")
    })
    public ResultInfo multiPolygonHandler(HttpServletRequest request,String [] centerLonlats,String [] otherLonlats){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        BufferedReader br = null;
        try{
            System.out.println("开始时间："+dateFormat.format(new Date()));
            // 读取请求内容
            br = new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }         // 将资料解码
            String reqBody = sb.toString();
            double [] radiusArr = new double[centerLonlats == null ? 0 : centerLonlats.length];
            if(centerLonlats != null && centerLonlats.length > 0 && otherLonlats != null && otherLonlats.length > 0) {
                for (int i = 0; i < centerLonlats.length; i++) {
                    String[] centerLonlatArr = centerLonlats[i].split(" ");
                    String[] otherLonlatArr = otherLonlats[i].split(" ");
                    double radius = GeoDistance.distanceOfTwoPoints(Double.parseDouble(centerLonlatArr[0]), Double.parseDouble(centerLonlatArr[1]),
                            Double.parseDouble(otherLonlatArr[0]), Double.parseDouble(otherLonlatArr[1]), GeoDistance.GaussSphere.WGS84);
                    radiusArr[i] = radius;
                }
            }
            Map<String, Object> featureMap = GeoUtil.featureUnion(reqBody,centerLonlats,radiusArr);
            ThreadPoolExecutor multiPolygonHandlerThreadPool = Constants.multiPolygonHandlerThreadPool;
            List<Future<Map<String,Object>>> futureList = new ArrayList<Future<Map<String,Object>>>();
            //获取气象信息员信息
            APIDataSearchThread apiDataSearchThread_M0026 = new APIDataSearchThread(featureMap,apiUrl,apiUserCode,apiPassword,defaultLimit,"M0026");
            Future<Map<String, Object>> submit_M0026 = multiPolygonHandlerThreadPool.submit(apiDataSearchThread_M0026);
            futureList.add(submit_M0026);
            //获取气象灾害责任人信息
            APIDataSearchThread apiDataSearchThread_M0024 = new APIDataSearchThread(featureMap,apiUrl,apiUserCode,apiPassword,defaultLimit,"M0024");
            Future<Map<String, Object>> submit_M0024 = multiPolygonHandlerThreadPool.submit(apiDataSearchThread_M0024);
            futureList.add(submit_M0024);
            //获取网格责任人信息
            APIDataSearchThread apiDataSearchThread_M0025 = new APIDataSearchThread(featureMap,apiUrl,apiUserCode,apiPassword,defaultLimit,"M0025");
            Future<Map<String, Object>> submit_M0025 = multiPolygonHandlerThreadPool.submit(apiDataSearchThread_M0025);
            futureList.add(submit_M0025);

            Map<String,Object> resultMap = new HashMap<String,Object>();
            //获取结果
            for (Future<Map<String,Object>> future : futureList) {
                Map<String, Object> futureMap = future.get();
                if(futureMap != null && futureMap.get("filterList") != null){
                    List<Map<String,Object>> filterList = (List<Map<String,Object>>)futureMap.get("filterList");
                    if(futureMap.get("interfaceCode") != null){
                        Map<String,Object> itemMap = new HashMap<String,Object>();
                        itemMap.put("total",filterList.size());
                        //气象信息员
                        if(futureMap.get("interfaceCode").toString().trim().equals("M0026")){
                            itemMap.put("dataList",filterList);
                            resultMap.put("miInfo",itemMap);
                        }
                        //象灾害责任人
                        if(futureMap.get("interfaceCode").toString().trim().equals("M0024")){
                            itemMap.put("dataList",filterList);
                            resultMap.put("rpInfo",itemMap);
                        }
                        //网格责任人
                        if(futureMap.get("interfaceCode").toString().trim().equals("M0025")){
                            itemMap.put("dataList",filterList);
                            resultMap.put("gridperInfo",itemMap);
                        }
                    }
                }
            }
            System.out.println("结束时间："+dateFormat.format(new Date()));
            return ResultInfo.ofData(ResultCode.SUCCESS,resultMap);
        }catch (Exception e){
            e.printStackTrace();
            return  ResultInfo.of(ResultCode.FAILED);
        }finally {
            if(br != null){
                try {
                    br.close();
                    br = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @PostMapping(value = "/getAreaAndTotal")
    @ResponseBody
    @ApiOperation(value = "计算指定面在辖区内覆盖乡/镇个数",response = ResultInfo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "geojson", value = "geojson数据", required = true, paramType="body",example = "{\"type\": \"FeatureCollection\"}"),
            @ApiImplicitParam(name="centerLonlats",value="中心点经纬度集合",dataType = "String",required=false,paramType="query"),
            @ApiImplicitParam(name="otherLonlats",value="其他点经纬度集合（和中心点经纬度集合一一对应）",dataType = "String",required=false,paramType="query"),
            @ApiImplicitParam(name="relationshipType",value="覆盖关系类型",dataType = "String",required=false,paramType="query")
    })
    public ResultInfo getAreaAndTotal(HttpServletRequest request,String [] centerLonlats,String [] otherLonlats,String relationshipType){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
        Map<String,Object> returnMap = new HashMap<String,Object>();
        ServletInputStream inputStream = null;
        try{
            System.out.println("开始时间："+dateFormat.format(new Date()));
            // 读取请求内容
            inputStream = request.getInputStream();
            FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(14));
            SimpleFeatureCollection featureCollection = (SimpleFeatureCollection)featureJSON.readFeatureCollection(inputStream);
            SimpleFeatureIterator features = featureCollection.features();
            //合并多面
            Geometry geometry = null;
            while (features.hasNext()) {
                SimpleFeature simpleFeature = features.next();
                Geometry m = (Geometry)simpleFeature.getDefaultGeometry();
                Geometry geo = null;
                try{
                    //处理自相交的问题
                    geo = GeometryUtil.validate(m);
                }catch (Exception e){
                    e.printStackTrace();
                    geo = m;
                }
                if(geometry == null){
                    geometry = geo;
                }else{
                    geometry = geometry.union(geo);
                }
            }
            if(centerLonlats != null && centerLonlats.length > 0 && otherLonlats != null && otherLonlats.length > 0){
                for (int i = 0; i < centerLonlats.length; i++) {
                    String [] centerLonlatArr = centerLonlats[i].split(" ");
                    String [] otherLonlatArr = otherLonlats[i].split(" ");
                    double radius = GeoDistance.distanceOfTwoPoints(Double.parseDouble(centerLonlatArr[0]),Double.parseDouble(centerLonlatArr[1]),
                            Double.parseDouble(otherLonlatArr[0]),Double.parseDouble(otherLonlatArr[1]), GeoDistance.GaussSphere.WGS84);
                    Geometry geo = BufferPoint.getGeometryByPointAndRadius(Double.parseDouble(centerLonlatArr[0]),Double.parseDouble(centerLonlatArr[1]),radius);
                    if(geometry == null){
                        geometry = geo;
                    }else{
                        geometry = geometry.union(geo);
                    }
                }
            }
            if(geometry != null){
                File rootFile = new File(geoJsonPath);
                //获取全省乡镇边界文件列表
                File[] files = rootFile.listFiles();
                List<File> fileList = new ArrayList<File>(Arrays.asList(files));
                int threadNum = 5;
                int baseSize = fileList.size()/threadNum;
                ThreadPoolExecutor pointFilterByMultiPolygonThreadPool = Constants.pointFilterByPolygonThreadPool;
                List<Future<Map<String,Object>>> futureList = new ArrayList<Future<Map<String,Object>>>();
                for (int i = 0; i < threadNum; i++) {
                    List<File> baseList = new ArrayList<File>();
                    if (i == threadNum - 1) {
                        baseList = fileList.subList(i * baseSize, fileList.size());
                    } else {
                        baseList = fileList.subList(i * baseSize, (i + 1) * baseSize);
                    }
                    AreaAndTotalThread areaAndTotalThread = new AreaAndTotalThread(baseList,geometry,relationshipType);
                    Future<Map<String, Object>> future = pointFilterByMultiPolygonThreadPool.submit(areaAndTotalThread);
                    futureList.add(future);
                }
                //获取结果
                for (Future<Map<String,Object>> future : futureList) {
                    Map<String, Object> resultMap = future.get();
                    if(resultMap != null && resultMap.get("code") != null && !"-1".equals(resultMap.get("code").toString())){
                        returnList.addAll((ArrayList)resultMap.get("data"));
                    }
                }
                Set<String> countySet = new LinkedHashSet<String>();
                Set<String> countyNameSet = new LinkedHashSet<String>();
                Set<String> citySet = new LinkedHashSet<String>();
                Set<String> cityNameSet = new LinkedHashSet<String>();
                for (int i = 0; i < returnList.size(); i++) {
                    countySet.add(returnList.get(i).get("areacode").toString().substring(0,6)+"000000");
                    countyNameSet.add(returnList.get(i).get("county").toString());
                    citySet.add(returnList.get(i).get("areacode").toString().substring(0,4)+"00000000");
                    cityNameSet.add(returnList.get(i).get("city").toString());
                }
                Collections.sort(returnList, new Comparator<Map<String,Object>>() {
                    public int compare(Map arg0, Map arg1) {
                        return arg0.get("areacode").toString().compareTo(arg1.get("areacode").toString());
                    }
                });
                returnMap.put("townList",returnList);
                returnMap.put("countyList",countySet);
                returnMap.put("countyNameList",countyNameSet);
                returnMap.put("cityList",citySet);
                returnMap.put("cityNameList",cityNameSet);
            }
            System.out.println("结束时间："+dateFormat.format(new Date()));
            return ResultInfo.ofDataAndTotal(ResultCode.SUCCESS,returnMap,returnList.size());
        }catch (Exception e){
            e.printStackTrace();
            return  ResultInfo.of(ResultCode.FAILED);
        }finally {
            try {
                if(inputStream != null){
                    inputStream.close();
                    inputStream = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @PostMapping(value = "/featureUnion")
    @ResponseBody
    @ApiOperation(value = "合并多个feature",response = ResultInfo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "geojson", value = "geojson数据", required = true, paramType="body",example = "{\"type\": \"FeatureCollection\"}"),
            @ApiImplicitParam(name="centerLonlats",value="中心点经纬度集合",dataType = "String",required=false,paramType="query"),
            @ApiImplicitParam(name="otherLonlats",value="其他点经纬度集合（和中心点经纬度集合一一对应）",dataType = "String",required=false,paramType="query")
    })
    public String featureUnion(HttpServletRequest request,String [] centerLonlats,String [] otherLonlats){
        ServletInputStream inputStream = null;
        String resultStr = "";
        try{
            // 读取请求内容
            inputStream = request.getInputStream();
            FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(6));
            GeometryJSON geometryJson = new GeometryJSON(6);
            SimpleFeatureCollection featureCollection = (SimpleFeatureCollection)featureJSON.readFeatureCollection(inputStream);
            SimpleFeatureIterator features = featureCollection.features();
            //合并多面
            Geometry geometry = null;
            while (features.hasNext()) {
                SimpleFeature simpleFeature = features.next();
                Geometry m = (Geometry)simpleFeature.getDefaultGeometry();
//                Geometry geo = m.intersection(m);
                Geometry geo = GeometryUtil.validate(m);
                if(geometry == null){
                    geometry = geo;
                }else{
                    geometry = geometry.union(geo);
                }
            }
            if(centerLonlats != null && centerLonlats.length > 0 && otherLonlats != null && otherLonlats.length > 0){
                for (int i = 0; i < centerLonlats.length; i++) {
                    String [] centerLonlatArr = centerLonlats[i].split(" ");
                    String [] otherLonlatArr = otherLonlats[i].split(" ");
                    double radius = GeoDistance.distanceOfTwoPoints(Double.parseDouble(centerLonlatArr[0]),Double.parseDouble(centerLonlatArr[1]),
                            Double.parseDouble(otherLonlatArr[0]),Double.parseDouble(otherLonlatArr[1]), GeoDistance.GaussSphere.WGS84);
                    Geometry geo = BufferPoint.getGeometryByPointAndRadius(Double.parseDouble(centerLonlatArr[0]),Double.parseDouble(centerLonlatArr[1]),radius);
                    if(geometry == null){
                        geometry = geo;
                    }else{
                        geometry = geometry.union(geo);
                    }
                }
            }
            StringWriter writer = new StringWriter();
            geometryJson.write(geometry,writer);
            resultStr = writer.toString();
        }catch (Exception e){
            e.printStackTrace();
            try {
                resultStr = IOUtils.toString(inputStream);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                    inputStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultStr;
    }


    @PostMapping(value = "/townsBound")
    @ResponseBody
    @ApiOperation(value = "获取乡镇边界(相邻自动合并)",response = ResultInfo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name="areaCodes",value="乡镇地区编码（多个逗号分隔）",required=true,paramType="query"),
    })
    public String townsBound(String areaCodes){
        String returnStr = "";
        try{
            String [] areacodeArr = areaCodes.split(",");
            FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(6));
            GeometryJSON geometryJson = new GeometryJSON(6);
            Geometry geometry = null;
            for (int i = 0; i < areacodeArr.length; i++) {
                FileInputStream in = null;
                try {
                    File geojsonFile = new File(geoJsonPath+"/"+areacodeArr[i]+".geojson");
                    if(geojsonFile.exists()){
                        in = new FileInputStream(geojsonFile);
                        SimpleFeature simpleFeature = featureJSON.readFeature(in);
                        Geometry m = (Geometry)simpleFeature.getDefaultGeometry();
//                    m = m.intersection(m);
                        m = GeometryUtil.validate(m);
                        geometry = geometryHandler(geometry,m);
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
            StringWriter writer = new StringWriter();
            geometryJson.write(geometry,writer);
            returnStr = writer.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return returnStr;
    }
    @PostMapping(value = "/getCountysArea")
    @ResponseBody
    @ApiOperation(value = "获取地区集合的面积",response = ResultInfo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name="areaCodes",value="区县地区编码（多个逗号分隔）",required=true,paramType="query"),
            @ApiImplicitParam(name="areaLevel",value="行政级别 0：国家 1：省 2：市3：县区 4：乡镇 5：村（目前仅支持3和4）",required=true,paramType="query")
    })
    public ResultInfo  getCountysArea(String areaCodes,String areaLevel){
        Map<String,Object> resultMap = new HashMap<String,Object>();
        ResultInfo resultInfo = ResultInfo.of(ResultCode.UNKNOWN_ERROR);
        try{
            String [] areacodeArr = areaCodes.split(",");
            FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(6));
            double totalArea = 0;
            for (int i = 0; i < areacodeArr.length; i++) {
                FileInputStream in = null;
                try {
                    String rootPath = "";
                    if(areaLevel != null){
                        if(areaLevel.trim().equals("3")){
                            rootPath = countysPath;
                        }else if(areaLevel.trim().equals("4")){
                            rootPath = geoJsonPath;
                        }
                        if(rootPath != null && !rootPath.trim().equals("")){
                            File geojsonFile = new File(rootPath+"/"+areacodeArr[i]+".geojson");
                            if(geojsonFile.exists()){
                                in = new FileInputStream(geojsonFile);
                                SimpleFeature simpleFeature = featureJSON.readFeature(in);
                                Geometry m = (Geometry)simpleFeature.getDefaultGeometry();
                                int numGeometries = m.getNumGeometries();
                                for (int j = 0; j < numGeometries; j++) {
                                    double d = CalUtils.calArea(m.getGeometryN(j).getCoordinates());
                                    totalArea = DoubleUtil.sum(totalArea,d);
                                }
                            }
                        }
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
            BigDecimal area = new BigDecimal(Double.toString(totalArea));
            area = area.setScale(3, BigDecimal.ROUND_HALF_UP);
            resultMap.put("area",area.toString());
            resultInfo = ResultInfo.ofData(ResultCode.SUCCESS,resultMap);
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultInfo;
    }
    private Geometry geometryHandler(Geometry target,Geometry source){
        if(source instanceof GeometryCollection){
            GeometryCollection geometryCollection = (GeometryCollection)source;
            for (int j = 0; j < geometryCollection.getNumGeometries(); j++) {
                Geometry nm = geometryCollection.getGeometryN(j);
                target = geometryHandler(target,nm);
            }
        }else{
            if(target != null){
                if(target instanceof GeometryCollection){
                    GeometryCollection geometryCollection = (GeometryCollection)target;
                    Geometry ntarget = null;
                    for (int j = 0; j < geometryCollection.getNumGeometries(); j++) {
                        Geometry nm = geometryCollection.getGeometryN(j);
                        if(nm instanceof LineString || nm instanceof Point){
                            continue;
                        }
                        ntarget = geometryHandler(ntarget,nm);
                    }
                    target = ntarget;
                }
                target = target.union(source);
            }else{
                target = source;
            }
        }
        return target;
    }
}

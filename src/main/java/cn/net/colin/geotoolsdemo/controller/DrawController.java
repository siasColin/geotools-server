package cn.net.colin.geotoolsdemo.controller;

import cn.net.colin.geotoolsdemo.common.Constants;
import cn.net.colin.geotoolsdemo.entities.common.ResultInfo;
import cn.net.colin.geotoolsdemo.entities.draw.DrawData;
import cn.net.colin.geotoolsdemo.entities.draw.LegendColor;
import cn.net.colin.geotoolsdemo.utils.CsvParser;
import cn.net.colin.geotoolsdemo.utils.JsonUtils;
import cn.net.colin.geotoolsdemo.utils.draw.DataHandler;
import cn.net.colin.geotoolsdemo.utils.draw.LegendColorHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import rainpoetry.java.draw.DrawBuilder;
import rainpoetry.java.draw.bean.DrawStyle;
import rainpoetry.java.draw.bean.Tuple3;
import rainpoetry.java.draw.bean.Tuple5;
import wContour.Interpolate;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * @Package: cn.net.colin.geotoolsdemo.controller
 * @Author: sxf
 * @Date: 2020-10-21
 * @Description:
 */
@RestController
@RequestMapping("/draw")
@Api(tags = "出图服务",description = "出图服务")
@CrossOrigin(origins = "*",maxAge = 3600)
@Slf4j
public class DrawController {
    @PostMapping(value = "/contourDraw")
    @ResponseBody
    @ApiOperation(value = "生成等值线图",response = ResultInfo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "datajson", value = "geojson数据", required = true, paramType="body"),
            @ApiImplicitParam(name="borderCode",value="边界线编码（不带后缀的文件名）",dataType = "String",required=true,paramType="query")
    })
    public String contourDraw(HttpServletRequest request, String borderCode){
        BufferedReader br = null;
        String resultStr = "";
        try{
            List<Tuple5<Double, Double, Double, Double, String>> borders = Constants.bordersMap.get(borderCode);
            if(borders != null && borders.size() > 0){
                // 读取请求内容
                br = new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF-8"));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                DrawData drawData = JsonUtils.toBean(sb.toString(), DrawData.class);
                List<Tuple5<Double, Double, Integer, Integer, Integer>> colors = LegendColorHandler.legendColorHandler(drawData.getLegendColor());
                List<Tuple3<Double, Double, Double>> datas = DataHandler.dataHandler(drawData.getData());
//                String dataPath = this.getClass().getClassLoader().getResource("contour/data-zj.csv").getPath();
//                List<Map<String, String>> dataList = CsvParser.parse(dataPath);
//                List<Tuple3<Double, Double, Double>> datas = dataToTuple(dataList);
                List<Tuple3<Double, Double, String>> externs = new ArrayList<Tuple3<Double, Double, String>>();
                externs = new ArrayList<>();
                for (Tuple3<Double, Double, Double> t : datas) {
                    externs.add(new Tuple3<>(t._1, t._2, t._3.toString()));
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.out.println("开始时间："+format.format(new Date()));
                DrawBuilder.of("contour")
                        .path("D:/temp/demo")
                        .config("data", datas)
                        .config("borders", borders)
                        .config("colors", colors)
                        // IDW 插值行数（数值越高经度等值线图精度越高处理过程越慢，默认值200）
                        .config("algorithm.rows",200)
                        // IDW 插值列数（数值越高经度等值线图精度越高处理过程越慢，默认值200）
                        .config("algorithm.cols",200)
                        // 等值线图是否根据区域边界进行裁剪
                        .config("bordersCut", true)
                        .config("white.transparent", true)
                        //// 区域线控制
                        .config("style.area.line",new DrawStyle(false, 1, Color.gray))
                        // 等值线是否绘制 、样式控制
                        .config("style.line",new DrawStyle(false, 1, Color.ORANGE))
                        //  等值线值 显示控制、大小控制
                        .config("style.line.value", new DrawStyle(false, 40, Color.BLACK))
                        // 等值面颜色填充
                        .config("style.contour.fill",true)
                        // 原始数据点绘制
                        /*.extern(externs)
                        .ovalStyle(30, Color.RED)
                        .textStyle(new Font("微软雅黑",Font.PLAIN, 10), Color.BLACK, 5, 5)*/
                        .build();
                System.out.println("结束时间："+format.format(new Date()));
            }
        }catch (Exception e){
            e.printStackTrace();
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
        return null;
    }

    @PostMapping(value = "/gridDraw")
    @ResponseBody
    @ApiOperation(value = "生成格点填色图",response = ResultInfo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "datajson", value = "geojson数据", required = true, paramType="body"),
            @ApiImplicitParam(name="fillType",value="填色类型（fixed：根据固定值填色，range：根据范围填色）",dataType = "String",required=true,paramType="query"),
            @ApiImplicitParam(name="legendColorSub",value="色带范围是否根据实际数值截取（1：截取，0：不截取）",dataType = "int",required=true,paramType="query"),
            @ApiImplicitParam(name="numberOfNearestNeighbors",value="进行插值时最近相邻点的数量",dataType = "int",required=true,paramType="query"),
            @ApiImplicitParam(name="unDefData",value="进行插值时要忽略的数值",dataType = "double",required=true,paramType="query"),
            @ApiImplicitParam(name="savePath",value="图片保存路径",dataType = "String",required=true,paramType="query")
    })
    public String gridDraw(HttpServletRequest request,String fillType,int legendColorSub,int numberOfNearestNeighbors,double unDefData,String savePath){
        BufferedReader br = null;
        String resultStr = "";
        try{
            // 读取请求内容
            br = new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            DrawData drawData = JsonUtils.toBean(sb.toString(), DrawData.class);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("开始时间："+format.format(new Date()));
            List<LegendColor> legendColorList = new ArrayList<LegendColor>();
            double[][] discreteData = new double[3][drawData.getLegendColor().size()];
            if(legendColorSub == 1){
                Map<String, Object> dataMap = DataHandler.dataHandlerToArray(drawData.getData(), drawData.getLegendColor());
                if(dataMap != null){
                    discreteData = (double[][])dataMap.get("discreteData");
                    legendColorList = (List<LegendColor>)dataMap.get("legendColorList");
                }
            }else{
                discreteData = DataHandler.dataHandlerToArray(drawData.getData());
                legendColorList = drawData.getLegendColor();
            }
            List<Tuple5<Double, Double, Integer, Integer, Integer>> legendColorTuples = new ArrayList<Tuple5<Double, Double, Integer, Integer, Integer>>();
            if(fillType != null && fillType.trim().equals("range")){
                legendColorTuples = LegendColorHandler.legendColorHandler(legendColorList);
            }
            Map<String, Object> legendColorMap = LegendColorHandler.legendColorParse(legendColorList);
            List<Color> colorList = (List<Color>)legendColorMap.get("colorList");
            List<Double> valueList = (List<Double>)legendColorMap.get("valueList");
            double[] _X = new double[drawData.getWidth()];
            double[] _Y = new double[drawData.getHeight()];
            Interpolate.CreateGridXY_Num(drawData.getStartX(), drawData.getStartY(), drawData.getEndX(), drawData.getEndY(), _X, _Y);
            double[][] _gridData = Interpolate.Interpolation_IDW_Neighbor(discreteData,
                    _X, _Y, numberOfNearestNeighbors, unDefData);// IDW插值
            //创建一个图，并按照图例进行渲染
            int w = drawData.getWidth();
            int h = drawData.getHeight();
            BufferedImage img=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
            for(int i=0;i<h;i++){
                for(int j=0;j<w;j++){
                    int rgb = 0;
                    long dataValue = Math.round(_gridData[i][j]);
                    if(fillType != null && fillType.trim().equals("range")){
                        if(dataValue<legendColorTuples.get(0)._1){
                            rgb = colorList.get(0).getRGB();
                        }else if(dataValue >= legendColorTuples.get(legendColorTuples.size()-1)._2){
                            rgb = colorList.get(legendColorTuples.size()-1).getRGB();
                        }else{
                            for (int k = 0; k < legendColorTuples.size(); k++) {
                                Tuple5<Double, Double, Integer, Integer, Integer> legendColorTuple = legendColorTuples.get(k);
                                if(dataValue >= legendColorTuple._1 && dataValue< legendColorTuple._2){
                                    rgb = colorList.get(k).getRGB();
                                    break;
                                }
                            }
                        }
                    }else{
                        for (int k = 0; k < valueList.size(); k++) {
                            long colorValue = Math.round(valueList.get(k));
                            if(dataValue == colorValue){//相等
                                rgb = colorList.get(k).getRGB();
                                break;
                            }
                        }
                    }
                    img.setRGB(j, (h-i-1), rgb);
                }
            }
            String name= UUID.randomUUID().toString();
            String path=savePath+"/"+name+".png";
            ImageIO.write(img, "png",new File(path));
            resultStr = path;
            System.out.println("结束时间："+format.format(new Date()));
        }catch (Exception e){
            e.printStackTrace();
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
        return resultStr;
    }

    private List<Tuple3<Double, Double, Double>> dataToTuple(List<Map<String, String>> listMaps) {
        List<Tuple3<Double, Double, Double>> retList = new ArrayList<>();
        for (Map<String, String> map : listMaps) {
            Double lon = Double.parseDouble(map.get("LON").trim());
            Double lat = Double.parseDouble(map.get("LAT").trim());
            Double value = Double.parseDouble(map.get("VALUE").trim());
            retList.add(new Tuple3<>(lon, lat, value));
        }
        return retList;
    }
}

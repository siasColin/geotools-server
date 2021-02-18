package cn.net.colin.geotoolsdemo.utils.draw;

import cn.net.colin.geotoolsdemo.entities.draw.LegendColor;
import rainpoetry.java.draw.bean.Tuple5;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Package: cn.net.colin.geotoolsdemo.utils.draw
 * @Author: sxf
 * @Date: 2020-10-16
 * @Description:
 */
public class LegendColorHandler {
    public static List<Tuple5<Double, Double, Integer, Integer, Integer>> legendColorHandler(List<LegendColor> legendColorList){
        List<Tuple5<Double, Double, Integer, Integer, Integer>> list = new ArrayList<Tuple5<Double, Double, Integer, Integer, Integer>>();
        for (LegendColor legendColor: legendColorList) {
            List<String> colorItems = legendColor.getColor();
            String min = colorItems.get(0).trim();
            String max = colorItems.get(1).trim();
            String rgbfull = colorItems.get(2);
            String rgb = rgbfull.substring(rgbfull.indexOf("(")+1,rgbfull.indexOf(")"));
            String [] rgbArr = rgb.split(",");
            Tuple5 tuple5 = new Tuple5<Double, Double, Integer, Integer, Integer>(
                    min.equals("min") ? Double.MIN_VALUE : Double.parseDouble(min),
                    max.equals("max") ? Double.MAX_VALUE : Double.parseDouble(max),
                    Integer.parseInt(rgbArr[0]),
                    Integer.parseInt(rgbArr[1]),
                    Integer.parseInt(rgbArr[2])
                    );
            list.add(tuple5);
        }
        return list;
    }

    public static Map<String,Object> legendColorParse(List<LegendColor> legendColorList){
        Map<String,Object> resultMap = new HashMap<String,Object>();
        List<Color> colorList = new ArrayList<Color>();
        List<Double> valueList = new ArrayList<Double>();
        for (int i = 0; i < legendColorList.size(); i++) {
            String value = legendColorList.get(i).getValue().trim();
            List<String> colorItems = legendColorList.get(i).getColor();
            String rgbfull = colorItems.get(2);
            String rgb = rgbfull.substring(rgbfull.indexOf("(")+1,rgbfull.indexOf(")")).trim();
            String [] rgbArr = rgb.split(",");
            if(rgb.equals("255,255,255,0")){//白色透明
                Color color = new Color(Integer.parseInt(rgbArr[0]),Integer.parseInt(rgbArr[1]),Integer.parseInt(rgbArr[2]),0);
                colorList.add(color);
            }else{
                Color color = new Color(Integer.parseInt(rgbArr[0]),Integer.parseInt(rgbArr[1]),Integer.parseInt(rgbArr[2]));
                colorList.add(color);
            }
            if(value.equals("min")){
                valueList.add(Double.MIN_VALUE);
            }else if(value.equals("max")){
                valueList.add(Double.MAX_VALUE);
            }else{
                valueList.add(Double.parseDouble(value));
            }
        }
        resultMap.put("colorList",colorList);
        resultMap.put("valueList",valueList);
        return resultMap;
    }

    /**
     * 字符串转换成Color对象
     * @param colorStr 16进制颜色字符串
     * @return Color对象
     * */
    public static Color toColorFromString(String colorStr){

        if(colorStr.contains("0x")||colorStr.contains("0X")){
            colorStr = colorStr.substring(2,colorStr.length());
        }
        else if(colorStr.contains("#")){
            colorStr = colorStr.substring(1,colorStr.length());
        }

        Color color =  new Color(Integer.parseInt(colorStr, 16)) ;
        //java.awt.Color[r=0,g=0,b=255]
        return color;
    }
}

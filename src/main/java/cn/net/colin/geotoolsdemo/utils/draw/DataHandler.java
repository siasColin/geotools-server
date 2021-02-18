package cn.net.colin.geotoolsdemo.utils.draw;

import cn.net.colin.geotoolsdemo.entities.draw.LegendColor;
import rainpoetry.java.draw.bean.Tuple3;

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
public class DataHandler {
    public static List<Tuple3<Double, Double, Double>> dataHandler(List<List<Double>> dataList){
        List<Tuple3<Double, Double, Double>> list = new ArrayList<Tuple3<Double, Double, Double>>();
        for (int i = 0; i < dataList.size(); i++) {
            List<Double> dl = dataList.get(i);
            Tuple3<Double, Double, Double> tuple3 = new Tuple3<Double, Double, Double>(dl.get(0),dl.get(1),dl.get(2));
            list.add(tuple3);
        }
        return list;
    }
    public static double[][] dataHandlerToArray(List<List<Double>> dataList){
        double[][] discreteData = new double[3][dataList.size()];
        for (int i = 0; i < dataList.size(); i++) {
            List<Double> dl = dataList.get(i);
            discreteData[0][i] = dl.get(0);
            discreteData[1][i] = dl.get(1);
            discreteData[2][i] = dl.get(2);
        }
        return discreteData;
    }
    public static Map<String,Object> dataHandlerToArray(List<List<Double>> dataList,List<LegendColor> legendColors){
        Map<String,Object> resultMap = new HashMap<String,Object>();
        List<LegendColor> legendColorList = new ArrayList<LegendColor>();
        double[][] discreteData = new double[3][dataList.size()];
        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;
        for (int i = 0; i < dataList.size(); i++) {
            List<Double> dl = dataList.get(i);
            discreteData[0][i] = dl.get(0);
            discreteData[1][i] = dl.get(1);
            discreteData[2][i] = dl.get(2);
            minValue = Math.min(dl.get(2),minValue);
            maxValue = Math.max(dl.get(2),maxValue);
        }
        for (LegendColor legendColor : legendColors) {
            long vmin = Math.round(minValue);
            long vmax = Math.round(maxValue);
            String value = legendColor.getValue().trim();
            if(value != null){
                if(value.equals("min")){
                    legendColorList.add(legendColor);
                }else if(value.equals("max")){
                    legendColorList.add(legendColor);
                }else {
                    long val = Math.round(Double.parseDouble(value));
                    if(val>= vmin && val<=vmax){
                        legendColorList.add(legendColor);
                    }
                }
            }
        }
        resultMap.put("discreteData",discreteData);
        resultMap.put("legendColorList",legendColorList);
        return resultMap;
    }
}

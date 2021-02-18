package cn.net.colin.geotoolsdemo.init;

import cn.net.colin.geotoolsdemo.common.Constants;
import cn.net.colin.geotoolsdemo.utils.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import rainpoetry.java.draw.bean.Tuple5;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * @Package: cn.net.colin.geotoolsdemo.init
 * @Author: sxf
 * @Date: 2020-10-16
 * @Description: 完成项目启动后的一些初始化工作
 */
@Component
public class Init implements ApplicationRunner {
    @Value("${geojson.drawBordersPath:}")
    private String drawBordersPath;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, List<Tuple5<Double, Double, Double, Double, String>>> bordersMap = Constants.bordersMap;
        try {
            if(drawBordersPath != null && !drawBordersPath.trim().equals("")){
                FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(14));
                GeometryJSON geometryJson = new GeometryJSON(14);
                File rootFile = new File(drawBordersPath);
                //获取全省乡镇边界文件列表
                File[] files = rootFile.listFiles();
                if(files != null && files.length > 0){
                    for (int i = 0; i < files.length; i++) {
                        FileInputStream in = null;
                        try {
                            File file = files[i];
                            if(!file.getName().endsWith(".geojson")){
                                continue;
                            }
                            String fileName = file.getName().substring(0,file.getName().lastIndexOf(".geojson"));
                            in = new FileInputStream(file);
                            SimpleFeatureCollection featureCollection = (SimpleFeatureCollection)featureJSON.readFeatureCollection(in);
                            SimpleFeatureIterator features = featureCollection.features();
                            List<Tuple5<Double, Double, Double, Double, String>> tuple5List = new ArrayList<Tuple5<Double, Double, Double, Double, String>>();
                            while (features.hasNext()) {
                                SimpleFeature simpleFeature = features.next();
                                Geometry geometry = (Geometry)simpleFeature.getDefaultGeometry();
                                String regionStr = geometry.toString().replace("MULTIPOLYGON","")
                                        .replace("(","")
                                        .replace(")","")
                                        .replace(", ",",");
                                BoundingBox bounds = simpleFeature.getBounds();
                                tuple5List.add(new Tuple5(bounds.getMinX()-0.1, bounds.getMinY()-0.1, bounds.getMaxX()+0.1, bounds.getMaxY()+0.1, regionStr.trim()));
                            }
                            bordersMap.put(fileName,tuple5List);
                            File saveFile = new File("d://bbbbb.json");
//                            FileUtils.writeStringToFile(saveFile, JsonUtils.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }finally {
                            if(in != null){
                                in.close();
                                in = null;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

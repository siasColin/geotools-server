package cn.net.colin.geotoolsdemo;

/*
 * User: chenchong
 * Date: 2019/4/17
 * description:
 */

import cn.net.colin.geotoolsdemo.utils.GeometryUtil;
import cn.net.colin.geotoolsdemo.utils.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;
import rainpoetry.java.draw.DrawBuilder;
import rainpoetry.java.draw.bean.DrawStyle;
import rainpoetry.java.draw.bean.Tuple3;
import rainpoetry.java.draw.bean.Tuple5;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ContourTest {

	//  sample: llon，llat,rlon,rlat,region
	protected List<Tuple5<Double, Double, Double, Double, String>> borders;
	//	sample: lon,lat,value
	protected List<Tuple3<Double, Double, Double>> datas;
	// sample: value_min,value_max,r,g,b
	protected List<Tuple5<Double, Double, Integer, Integer, Integer>> colors;
	//	sample: lon,lat,text
	protected List<Tuple3<Double, Double, String>> externs;

	@Before
	public void before() {
		String dataPath = this.getClass().getClassLoader().getResource("contour/data-zj.csv").getPath();
		String colorPath = this.getClass().getClassLoader().getResource("contour/color.csv").getPath();
		String borderPath = this.getClass().getClassLoader().getResource("contour/border-zj.csv").getPath();
		List<Map<String, String>> dataList = CsvParser.parse(dataPath);
		List<Map<String, String>> colorList = CsvParser.parse(colorPath);
		List<Map<String, String>> borderList = CsvParser.parse(borderPath);
//		borders = borderToTuple(borderList);
		borders = borderToTuple2();
		datas = dataToTuple(dataList);
		colors = colorToTuple(colorList);

		externs = new ArrayList<>();
		for (Tuple3<Double, Double, Double> t : datas) {
			externs.add(new Tuple3<>(t._1, t._2, t._3.toString()));
		}

	}

	private List<Tuple5<Double, Double, Double, Double, String>> borderToTuple2() {
		FileInputStream in = null;
		FeatureJSON featureJSON = new FeatureJSON(new GeometryJSON(6));
		List<Tuple5<Double, Double, Double, Double, String>> tuple5List = new ArrayList<Tuple5<Double, Double, Double, Double, String>>();
		try {
			File file = new File("D:\\突发\\浙江\\应急辅助一张图\\省市县边界\\xzx.geojson");
			String fileName = file.getName().substring(0,file.getName().lastIndexOf(".geojson"));
			Map<String,Object> map = JsonUtils.toBean(FileUtils.readFileToString(file,"UTF-8"),Map.class);
			List<LinkedHashMap<String,Object>> features = (List<LinkedHashMap<String,Object>>)map.get("features");
			for (LinkedHashMap<String,Object> feature : features) {
				LinkedHashMap<String,Object> geometry = (LinkedHashMap<String,Object>)feature.get("geometry");
				ArrayList<ArrayList> coordinatesList = (ArrayList<ArrayList>)geometry.get("coordinates");

				for (ArrayList<ArrayList> cl : coordinatesList) {
					ArrayList<ArrayList<Double>> coordinates = cl.get(0);
					Double minx = Double.MAX_VALUE;
					Double maxx = Double.MIN_VALUE;
					Double miny = Double.MAX_VALUE;
					Double maxy = Double.MIN_VALUE;
					StringBuilder regionStr_sb = new StringBuilder();
					for (int i=0;i< coordinates.size();i++) {
						ArrayList<Double> lonlat = coordinates.get(i);
						regionStr_sb.append(lonlat.get(0)).append(" ").append(lonlat.get(1));
						if(i != coordinates.size()-1){
							regionStr_sb.append(",");
						}
						minx = Math.min(minx,lonlat.get(0));
						maxx = Math.max(maxx,lonlat.get(0));
						miny = Math.min(miny,lonlat.get(1));
						maxy = Math.max(maxy,lonlat.get(1));
					}
					tuple5List.add(new Tuple5(minx-0.1,miny-0.1, maxx+0.1, maxy+0.1, regionStr_sb.toString()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				in = null;
			}
		}
		return tuple5List;
	}

	@Test
	public void deal() {
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
				//// 区域线控制
				.config("style.area.line",new DrawStyle(true, 1, Color.gray))
				// 等值线是否绘制 、样式控制
				.config("style.line",new DrawStyle(false, 2, Color.ORANGE))
				//  等值线值 显示控制、大小控制
				.config("style.line.value", new DrawStyle(false, 40, Color.BLACK))
				// 等值面颜色填充
				.config("style.contour.fill",true)
				// 原始数据点绘制
				/*.extern(externs)
				.ovalStyle(30, Color.RED)
				.textStyle(new Font("微软雅黑",Font.PLAIN, 40), Color.BLACK, 10, 10)*/
				.build();
		System.out.println("结束时间："+format.format(new Date()));
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

	private List<Tuple5<Double, Double, Double, Double, String>> borderToTuple(List<Map<String, String>> listMaps) {
		List<Tuple5<Double, Double, Double, Double, String>> retList = new ArrayList<>();
		for (Map<String, String> map : listMaps) {
			Double llon = Double.parseDouble(map.get("LLON").trim());
			Double llat = Double.parseDouble(map.get("LLAT").trim());
			Double rlon = Double.parseDouble(map.get("RLON").trim());
			Double rlat = Double.parseDouble(map.get("RLAT").trim());
			String region = map.get("REGION").trim();
			/*String [] regionArr = region.split(",");
			double [] lonArr = new double[regionArr.length];
			double [] latArr = new double[regionArr.length];
			for (int i = 0; i < regionArr.length; i++) {
				String [] lonlatArr = regionArr[i].trim().split(" |  |   ");
				lonArr[i] = Double.parseDouble(lonlatArr[0]);
				latArr[i] = Double.parseDouble(lonlatArr[1]);
			}
			Arrays.sort(lonArr);
			Arrays.sort(latArr);
			System.out.println("minx:"+lonArr[0]+" miny:"+latArr[0]+"###"+"maxx:"+lonArr[lonArr.length-1]+" maxy:"+latArr[latArr.length-1]);*/
			retList.add(new Tuple5(llon, llat, rlon, rlat, region));
		}
		return retList;
	}

	private List<Tuple5<Double, Double, Integer, Integer, Integer>> colorToTuple(List<Map<String, String>> listMaps) {
		List<Tuple5<Double, Double, Integer, Integer, Integer>> retList = new ArrayList<>();
		for (Map<String, String> map : listMaps) {
			Double value_min = Double.parseDouble(map.get("VALUE_MIN").trim());
			Double value_max = Double.parseDouble(map.get("VALUE_MAX").trim());
			int r = Integer.parseInt(map.get("R").trim());
			int g = Integer.parseInt(map.get("G").trim());
			int b = Integer.parseInt(map.get("B").trim());
			retList.add(new Tuple5(value_min, value_max, r, g, b));
		}
		return retList;
	}
}

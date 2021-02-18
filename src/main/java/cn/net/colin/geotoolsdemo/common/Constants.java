package cn.net.colin.geotoolsdemo.common;


import cn.net.colin.geotoolsdemo.utils.JsonUtils;
import cn.net.colin.geotoolsdemo.utils.ResourceRenderer;
import org.apache.commons.io.IOUtils;
import rainpoetry.java.draw.bean.Tuple5;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Constants {

	public static final ThreadPoolExecutor pointFilterByPolygonThreadPool = new ThreadPoolExecutor(5, 10,
			200, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(5),
			Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
	public static final ThreadPoolExecutor areaAndTotalThreadPool = new ThreadPoolExecutor(5, 10,
			200, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(5),
			Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
	public static final ThreadPoolExecutor multiPolygonHandlerThreadPool = new ThreadPoolExecutor(5, 10,
			200, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(5),
			Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());

	public static final Map<String,Object> areaInfoMap;
	public static Map<String, List<Tuple5<Double, Double, Double, Double, String>>> bordersMap = new HashMap<String, List<Tuple5<Double, Double, Double, Double, String>>>();

	static {
		InputStream in = null;
		String areaInfo = "";
		try {
			in = ResourceRenderer.resourceLoader("classpath:/areaInfo.json");
			areaInfo = IOUtils.toString(in, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(in != null){
				try {
					in.close();
					in = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		areaInfoMap = JsonUtils.toMap(areaInfo,String.class,Object.class);
	}
}
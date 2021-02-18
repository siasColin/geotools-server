package cn.net.colin.geotoolsdemo.utils;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Package: cn.net.colin.geotoolsdemo.utils
 * @Author: sxf
 * @Date: 2020-9-11
 * @Description:
 */
public class ResourceRenderer {
    public static InputStream resourceLoader(String fileFullPath) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        return resourceLoader.getResource(fileFullPath).getInputStream();
    }
}

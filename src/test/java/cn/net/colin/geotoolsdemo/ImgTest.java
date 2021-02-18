package cn.net.colin.geotoolsdemo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @Package: cn.net.colin.geotoolsdemo
 * @Author: sxf
 * @Date: 2020-10-21
 * @Description:
 */
public class ImgTest {
    public static void main(String[] args) {
        try {
            int w = 200;
            int h = 200;
            BufferedImage img=new BufferedImage(w,h,BufferedImage.TYPE_4BYTE_ABGR);
           /* Graphics2D g2d = img.createGraphics();
            // 设置画布透明模式
            img = g2d.getDeviceConfiguration().createCompatibleImage(w, h, Transparency.TRANSLUCENT);
            // 释放资源
            // graphics2D 使用的是系统资源，不释放的话，其他 graphics2D 的实例对象无法获取系统资源
            g2d.dispose();*/
            Color color = new Color(255,255,255,0);
            for(int i=0;i<h;i++){
                for(int j=0;j<w;j++){
                            /*int rgb = 0;
                            long dataValue = Math.round(_gridData[i][j]);
                            for (int k = 0; k < valueList.size(); k++) {
                                long colorValue = Math.round(valueList.get(k));
                                if(dataValue == colorValue){//相等
                                    rgb = colorList.get(k).getRGB();
                                    break;
                                }
                            }*/
                    img.setRGB(j, (h-i-1), color.getRGB());
                }
            }
            String name= UUID.randomUUID().toString();
            String path="D:/tmp/"+name+".png";
            ImageIO.write(img, "png",new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

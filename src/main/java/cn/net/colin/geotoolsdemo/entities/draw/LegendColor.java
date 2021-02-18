package cn.net.colin.geotoolsdemo.entities.draw;

import java.util.List;

/**
 * @Package: cn.net.colin.geotoolsdemo.entities.draw
 * @Author: sxf
 * @Date: 2020-10-16
 * @Description:
 */
public class LegendColor {
    private List<String> color;
    private String caption;
    private String value;

    public List<String> getColor() {
        return color;
    }

    public void setColor(List<String> color) {
        this.color = color;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

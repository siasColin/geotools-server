package cn.net.colin.geotoolsdemo.entities.draw;

import java.util.List;

/**
 * @Package: cn.net.colin.geotoolsdemo.entities.draw
 * @Author: sxf
 * @Date: 2020-10-16
 * @Description:
 */
public class DrawData {
    private List<List<Double>> data;
    private String datatime;
    private String caption;
    private int nx;
    private int ny;
    private int type;
    private double endX;
    private double endY;
    private String times;
    private double xspan;
    private double yspan;
    private double startX;
    private double startY;
    private List<LegendColor> legendColor;
    private int width;
    private int height;
    private List<Double> values;
    private List<String> colors;

    public List<List<Double>> getData() {
        return data;
    }

    public void setData(List<List<Double>> data) {
        this.data = data;
    }

    public String getDatatime() {
        return datatime;
    }

    public void setDatatime(String datatime) {
        this.datatime = datatime;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public int getNx() {
        return nx;
    }

    public void setNx(int nx) {
        this.nx = nx;
    }

    public int getNy() {
        return ny;
    }

    public void setNy(int ny) {
        this.ny = ny;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getEndX() {
        return endX;
    }

    public void setEndX(double endX) {
        this.endX = endX;
    }

    public double getEndY() {
        return endY;
    }

    public void setEndY(double endY) {
        this.endY = endY;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public double getXspan() {
        return xspan;
    }

    public void setXspan(double xspan) {
        this.xspan = xspan;
    }

    public double getYspan() {
        return yspan;
    }

    public void setYspan(double yspan) {
        this.yspan = yspan;
    }

    public double getStartX() {
        return startX;
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public double getStartY() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    public List<LegendColor> getLegendColor() {
        return legendColor;
    }

    public void setLegendColor(List<LegendColor> legendColor) {
        this.legendColor = legendColor;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<Double> getValues() {
        return values;
    }

    public void setValues(List<Double> values) {
        this.values = values;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }
}

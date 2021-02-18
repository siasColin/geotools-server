package cn.net.colin.geotoolsdemo.entities;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Package: cn.net.colin.geotoolsdemo.entities
 * @Author: sxf
 * @Date: 2020-8-27
 * @Description:
 */
@ApiModel(value = "PointsAndPolygon",description = "点和面实体类")
public class PointsAndPolygon implements Serializable {
    private static final long serialVersionUID = -7797954297995146185L;

    @ApiModelProperty(value = "points",example = "[{\"Lon\":120.744819922583446,\"Lat\":29.844334759731133},{\"Lon\":121.067479596091417,\"Lat\":29.839944832200413},{\"Lon\":120.889687531097223,\"Lat\":30.002372150837083},{\"Lon\":120.439719959198342,\"Lat\":29.605083709306854},{\"Lon\":120.837008400728578,\"Lat\":29.468995955854506},{\"Lon\":121.405504015956922,\"Lat\":30.039686534848208},{\"Lon\":121.21893209590128,\"Lat\":29.787265701831764}]",required = true)
    private List<Map<String,Object>> points;
    @ApiModelProperty(value = "polygon",example = "120.64285142590434 30.08985112075478, 120.64285142590434 30.08985112075478, 120.39323548612502 29.883944371868196, 120.41700843277069 29.842711831724714, 120.49427050936904 29.72407346555029, 120.69634055585705 29.59495946154013, 120.77954586911682 29.57428585439994, 121.16585625210863 29.476028430760056, 121.39169924524225 29.55877787036453, 121.47490455850202 29.641459587499522, 121.47490455850202 29.76535492865746, 121.39764248190367 29.925159867545435, 121.2490615653684 29.98695112147673, 121.18962919875423 30.079565930900696, 121.0053888622505 30.20805386313804, 120.8270917624081 30.213189898509235, 120.63096495258152 30.259402149713534, 120.63096495258152 30.259402149713534, 120.64285142590434 30.08985112075478",required = true)
    private String polygon;

    public List<Map<String, Object>> getPoints() {
        return points;
    }

    public void setPoints(List<Map<String, Object>> points) {
        this.points = points;
    }

    public String getPolygon() {
        return polygon;
    }

    public void setPolygon(String polygon) {
        this.polygon = polygon;
    }
}

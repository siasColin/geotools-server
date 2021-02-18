package cn.net.colin.geotoolsdemo.utils;

import org.geotools.data.DataUtilities;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.measure.Measure;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.*;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import si.uom.SI;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

public class BufferPoint {
    public static void main(String args[]) {

        GeometryJSON geometryJson = new GeometryJSON(14);
        StringWriter writer = new StringWriter();
        try {
            Geometry geo = BufferPoint.getGeometryByPointAndRadius(120.080,29.185,30000.0);
            geometryJson.write(geo, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(writer);
    }

    public static Geometry getGeometryByPointAndRadius(double lon, double lat,double radius){
        SimpleFeatureType TYPE = null;
        try {
            TYPE = DataUtilities.createType("", "Location", "locations:Point:srid=4326," + "id:Integer");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Point point = geometryFactory.createPoint(
                new Coordinate(
                        lon,
                        lat
                )
        );
        featureBuilder.add(point);
        SimpleFeature feature = featureBuilder.buildFeature("fid.1"); // b
        SimpleFeature out = BufferPoint.bufferFeature(feature, new Measure(radius, SI.METRE));
        return (Geometry) out.getDefaultGeometry();
    }
    public static SimpleFeature bufferFeature(SimpleFeature feature, Measure distance) {
        GeometryAttribute gProp = feature.getDefaultGeometryProperty();
        CoordinateReferenceSystem origCRS = gProp.getDescriptor().getCoordinateReferenceSystem();
        Geometry geom = (Geometry) feature.getDefaultGeometry();
        Geometry pGeom = geom;
        MathTransform toTransform, fromTransform = null;
        if (!(origCRS instanceof ProjectedCRS)) {

            double x = geom.getCoordinate().x;
            double y = geom.getCoordinate().y;

            String code = "AUTO:42001," + x + "," + y;
            CoordinateReferenceSystem auto;
            try {
                auto = CRS.decode(code);
                toTransform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, auto);
                fromTransform = CRS.findMathTransform(auto, DefaultGeographicCRS.WGS84);
                pGeom = JTS.transform(geom, toTransform);

            } catch (MismatchedDimensionException | TransformException | FactoryException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        // buffer
        Geometry out = buffer(pGeom, distance.doubleValue());
        Geometry retGeom = out;
        // reproject the geometry to the original projection
        if (!(origCRS instanceof ProjectedCRS)) {
            try {
                retGeom = JTS.transform(out, fromTransform);

            } catch (MismatchedDimensionException | TransformException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // return a new feature containing the geom
        SimpleFeatureType schema = feature.getFeatureType();
        SimpleFeatureTypeBuilder ftBuilder = new SimpleFeatureTypeBuilder();
        ftBuilder.setCRS(origCRS);

        for (AttributeDescriptor attrib : schema.getAttributeDescriptors()) {
            AttributeType type = attrib.getType();

            if (type instanceof GeometryType) {
                String oldGeomAttrib = attrib.getLocalName();
                ftBuilder.add(oldGeomAttrib, Polygon.class);
            } else {
                ftBuilder.add(attrib);
            }
        }
        ftBuilder.setName(schema.getName());

        SimpleFeatureType nSchema = ftBuilder.buildFeatureType();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(nSchema);
        List<Object> atts = feature.getAttributes();
        for (int i = 0; i < atts.size(); i++) {
            if (atts.get(i) instanceof Geometry) {
                atts.set(i, retGeom);
            }
        }
        SimpleFeature nFeature = builder.buildFeature(null, atts.toArray());
        return nFeature;
    }

    private static Geometry buffer(Geometry geom, double dist) {
        Geometry buffer = geom.buffer(dist);
        return buffer;

    }


}

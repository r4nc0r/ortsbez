package arse;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import fu.esi.SQL;
import pp.dorenda.client2.additional.UniversalPainterWriter;


class DBQuery {
    public static void doDBQuery(String DBParams, UniversalPainterWriter upw, int startLSI, int endLSI){
        //Tries to create a DBConnection with the DBParams, LSI Class and a BoundingBox around the ConcaveHull
        DBConnection DBCon = new DBConnection(DBParams,"SELECT realname, geodata_point FROM domain WHERE geometry='P' AND lsiclass1 BETWEEN "+ startLSI +" AND "+ endLSI +" AND "+ SQL.createIndexQuery(ConcaveHullCreation.getConcaveHullJTS(),true));

        //checks if result is inside the ConcaveHull if yes writes Point to result.txt
        for(ResultClass result: DBCon.getDBData()) {
            GeometryFactory geometryFactory = new GeometryFactory();
            Geometry geometry = geometryFactory.createPoint(result.Coordinate);
            if (geometry.within(ConcaveHullCreation.getConcaveHullJTS()))
                upw.flag(result.Coordinate.y, result.Coordinate.x, 0, 0, 255, 200, result.Name);
        }
    }
}


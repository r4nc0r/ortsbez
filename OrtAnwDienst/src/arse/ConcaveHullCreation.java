package arse;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fu.util.ConcaveHullGenerator;
import pp.dorenda.client2.additional.UniversalPainterWriter;

import java.util.ArrayList;


public class ConcaveHullCreation {

    private static Geometry concaveHullJTS;

    public static Geometry getConcaveHullJTS() {return concaveHullJTS;}

    public static ArrayList<double[]> closedPositions = new ArrayList<double[]>();

    public static void generateConcaveHull(UniversalPainterWriter upw) throws Exception {


        //Get coordinates for each crossing in closed list
        for (Crossing cross: DijkstraAlgorithm.getClosedList().values()) {
            closedPositions.add(convertToDoubleArray(Isochrone.getNavData().getCrossingLongE6(cross.id),Isochrone.getNavData().getCrossingLatE6(cross.id)));
        }
        double alpha =0.02;

        //create ConcaveHull
        if (closedPositions.size()==1) {
           closedPositions.add(new double[]{closedPositions.get(0)[0]+00.000001,closedPositions.get(0)[1]+00.000001});
        }
        concaveHullJTS = ConcaveHullGenerator.concaveHullJTS(closedPositions,alpha);

        //write ConcaveHull to result.txt
        upw.jtsGeometry(concaveHullJTS,102,102,102,200,1,5,0);

        //write StartPosition in result.txt
        double[] startpos = convertToDoubleArray(Isochrone.getStartLat(),Isochrone.getStartLon());
        upw.flag(startpos[0],startpos[1],255,0,0,200,"Start");
    }

    public static double[] convertToDoubleArray(int lat,int lon){
        double[] array = new double[2];
        array[0]=lat;
        array[0]= array[0]/1000000;
        array[1]=lon;
        array[1]= array[1]/1000000;
        return array;
    }
}

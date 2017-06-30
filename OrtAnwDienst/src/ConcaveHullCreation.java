import com.vividsolutions.jts.geom.Geometry;
import fu.util.ConcaveHullGenerator;
import pp.dorenda.client2.additional.UniversalPainterWriter;

import java.util.ArrayList;


public class ConcaveHullCreation {

    private static Geometry concaveHullJTS;

    public static Geometry getConcaveHullJTS() {return concaveHullJTS;}


    public static void generateConcaveHull(UniversalPainterWriter upw) {
        ArrayList<double[]> closedPositions = new ArrayList<double[]>();

        //Get coordinates for each crossing in closed list
        for (Crossing cross: DijkstraAlgorithm.getClosedList().values()) {
            closedPositions.add(convertToDoubleArray(Isochrone.getNavData().getCrossingLongE6(cross.id),Isochrone.getNavData().getCrossingLatE6(cross.id)));
        }

        //create ConcaveHull
        concaveHullJTS = ConcaveHullGenerator.concaveHullJTS(closedPositions,0.04d);

        //write ConcaveHull to result.txt
        upw.jtsGeometry(concaveHullJTS,102,102,102,200,1,0,0);

        //write StartPosition in result.txt
        double[] startpos = convertToDoubleArray(Isochrone.getStartLat(),Isochrone.getStartLon());
        upw.flag(startpos[0],startpos[1],255,0,0,200,"Start");
    }

    private static double[] convertToDoubleArray(int lat,int lon){
        double[] array = new double[2];
        array[0]=lat;
        array[0]= array[0]/1000000;
        array[1]=lon;
        array[1]= array[1]/1000000;
        return array;
    }
}

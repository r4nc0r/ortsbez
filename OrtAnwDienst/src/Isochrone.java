import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import fu.esi.SQL;
import nav.NavData;
import pp.dorenda.client2.additional.UniversalPainterWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;


public class Isochrone {
    private static nav.NavData navData;
    private static int startLat;
    private static int startLon;
    private static int totalSeconds;

    public static NavData getNavData() {return navData;}
    public static int getStartLat() {return startLat;}
    public static int getStartLon() {return startLon;}
    public static int getTotalSeconds() {return totalSeconds;}

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        checkStatement(args);

        long startTime = System.nanoTime();

        String DBParams= args[0];
        navData = new NavData(args[1], true);
        startLat = (int) (Double.parseDouble(args[2])*1000000);
        startLon = (int) (Double.parseDouble(args[3])*1000000);
        totalSeconds = Integer.parseInt(args[4]) *60;
        int startLSI =Integer.parseInt(args[5]);
        int endLSI =Integer.parseInt(args[6]);




        long startTimeAfterTables = System.nanoTime();

        UniversalPainterWriter upw = new UniversalPainterWriter("result.txt");

        long startTimeAfterUPW = System.nanoTime();

        DijkstraAlgorithm.runAlgorithm();

        long durationCACLoadTime = startTimeAfterTables - startTime;
        long durationAStar = System.nanoTime() - startTimeAfterUPW;
        System.out.println("\nCAC Loading Time:");
        printDurationNano(durationCACLoadTime);

        System.out.println("\nA-Star Duration:");
        printDurationNano(durationAStar);

        System.out.println("\nGenerating Concave Hull:");
        startTime = System.nanoTime();
        ConcaveHullCreation.generateConcaveHull(upw);

        System.out.println("\nConcave Hull Duration:");
        printDurationNano(System.nanoTime() - startTime);

        System.out.println("\nStarting DB-Query:");
        startTime = System.nanoTime();
        doDBQuery(DBParams,upw,startLSI,endLSI);
        System.out.println("\nDB-Query Duration:");
        printDurationNano(System.nanoTime() - startTime);

        upw.close();
    }

    private static void checkStatement(String[] args){
        if (args.length <7){
            throw new IllegalArgumentException("Incorrect Input");
        }
        if (args.length >7){
            System.out.println("Too many Arguments, ignoring unnecessary Arguments");
        }
    }

    private static void doDBQuery(String DBParams, UniversalPainterWriter upw,int startLSI, int endLSI){
        //Tries to create a DBConnection with the DBParams, LSI Class and a BoundingBox around the ConcaveHull
        DBConnection DBCon = new DBConnection(DBParams,"SELECT realname, geodata_point FROM domain WHERE geometry='P' AND lsiclass1 BETWEEN "+ startLSI +" AND "+ endLSI +" AND "+SQL.createIndexQuery(ConcaveHullCreation.getConcaveHullJTS(),true));

        //checks if result is inside the ConcaveHull if yes writes Point to result.txt
        for(ResultClass result: DBCon.getDBData()) {
            GeometryFactory geometryFactory = new GeometryFactory();
            Geometry geometry = geometryFactory.createPoint(result.Coordinate);
            if (geometry.within(ConcaveHullCreation.getConcaveHullJTS()))
                upw.flag(result.Coordinate.y, result.Coordinate.x, 0, 0, 255, 200, result.Name);
        }
    }

    private static void printDurationNano(long duration) {
        long diffMS = TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS);
        long diffSec = TimeUnit.SECONDS.convert(duration, TimeUnit.NANOSECONDS);
        long diffMin = TimeUnit.MINUTES.convert(duration, TimeUnit.NANOSECONDS);

        System.out.println(diffMS + " ms");
        System.out.println(diffMin + " min, "
                + (diffSec - TimeUnit.SECONDS.convert(diffMin, TimeUnit.MINUTES)) + " sec, "
                + (diffMS - (TimeUnit.MILLISECONDS.convert(diffMin, TimeUnit.MINUTES) + TimeUnit.MILLISECONDS.convert(diffSec, TimeUnit.SECONDS))) + " ms"
        );
    }
}

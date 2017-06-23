import com.vividsolutions.jts.geom.*;
import fu.keys.LSIClassCentreDB;
import fu.esi.SQL;
import fu.util.ConcaveHullGenerator;
import nav.NavData;
import opensphere.geometry.algorithm.ConcaveHull;
import pp.dorenda.client2.additional.UniversalPainterWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;


public class OrtAnwDienst {

    private static nav.NavData navData;

    private static Map<Integer, Crossing> closedList;

    private static PriorityQueue<Crossing> openList;

    private static Map<Integer, Crossing> openListMap;

    private static ArrayList<double[]> positions;

    private static int startLat;
    private static int startLon;
    private static int totalSeconds;
    private static int[] LSI=new int[2];

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        long startTime = System.nanoTime();

        startLat = 49465646;
        startLon = 11154443;
        totalSeconds = 5*60;
        LSI[0]=20505600;
        LSI[1]=20505700;

        navData = new NavData("roth\\Roth_LBS\\CAR_CACHE_mittelfranken_noCC.cac", true);

        long startTimeAfterTables = System.nanoTime();

        UniversalPainterWriter upw = new UniversalPainterWriter("result.txt");

        long startTimeAfterUPW = System.nanoTime();

        boolean isAStarTerminated = false;
        Crossing activeCrossing;

        initAStarAlgo();

        while(!isAStarTerminated){
            //System.out.println("Open List Größe: " + openList.size());
            if (openList.peek().gVal > totalSeconds){
                isAStarTerminated = true;
            }
            else{
                activeCrossing = openList.poll();
                openListMap.remove(activeCrossing.id);
                expandCrossing(activeCrossing);
            }
        }


        long durationCACLoadTime = startTimeAfterTables - startTime;
        long durationAStar = System.nanoTime() - startTimeAfterUPW;
        System.out.println("\nCAC Loading Time:");
        printDurationNano(durationCACLoadTime);
        System.out.println("\nA-Star Duration:");
        printDurationNano(durationAStar);



        startTime = System.nanoTime();
        positions = new ArrayList<double[]>();
        generateConcaveHull(upw);

        System.out.println("\nconcaveHull Duration:");
        printDurationNano(System.nanoTime() - startTime);

        double w=positions.get(1)[1];
        double n=positions.get(1)[0];
        double e=positions.get(1)[1];
        double s=positions.get(1)[0];
        for (int i =0; i<positions.size();i++)
        {
            if (positions.get(i)[1]>e)
                e=positions.get(i)[1];
            if (positions.get(i)[0]>n)
                n=positions.get(i)[0];
            if (positions.get(i)[1]<w)
                w=positions.get(i)[1];
            if (positions.get(i)[0]<s)
                s=positions.get(i)[0];
        }
        GeometryFactory geometryFactory= new GeometryFactory();
        Geometry geometry = ConcaveHullGenerator.concaveHullJTS(positions,1d);
        ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
        DBConnection DBCon = new DBConnection("geosrv.informatik.fh-nuernberg.de/5432/dbuser/dbuser/deproDB20","SELECT realname, geodata_point FROM domain WHERE geometry='P' AND lsiclass1 BETWEEN "+LSI[0]+" AND "+LSI[1]+" AND "+SQL.createIndexQuery(w,n,e,s,true));
        for(Coordinate coord: DBCon.getDBData())
        {

            Geometry geometry1= geometryFactory.createPoint(coord);
            if (geometry.within(geometry1))
            {
                coordinates.add(coord);
                System.out.println(coord.x + ""+coord.y);
            }
        }

    }

    private static void generateConcaveHull(UniversalPainterWriter upw) {
        for (Crossing cross: closedList.values()) {
            positions.add(convertToDoubleArray(navData.getCrossingLatE6(cross.id),navData.getCrossingLongE6(cross.id)));
        }
        //ArrayList<double[]>  hullPositions = ConcaveHullGenerator.concaveHull(positions,0.04d);
        //upw.polygon(hullPositions,102,102,102,200);
        //Geometry geometry = ConcaveHullGenerator.concaveHullJTS(positions,0.04d);
        GeometryFactory geometryFactory = new GeometryFactory();
        Geometry geometry = geometryFactory.createPolygon(generateCoordinateArray());
        upw.jtsGeometry(geometry,102,102,102,200,1,0,0);
        double[] startpos = convertToDoubleArray(startLat,startLon);
        upw.flag(startpos[0],startpos[1],0,0,255,200,"Start");
        upw.close();
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

    private static double[] convertToDoubleArray(int lat,int lon){
        double[] array = new double[2];
        array[0]=lat;
        array[0]= array[0]/1000000;
        array[1]=lon;
        array[1]= array[1]/1000000;
        return array;
    }
    private static Coordinate[] generateCoordinateArray(){
        Coordinate[] coordinates = new Coordinate[closedList.size()+1];
        int cnt=0;
        int crossId=0;
        double lat;
        double lon;
        for (Crossing cross: closedList.values()) {
            if (cnt==0)
            {
                crossId=cross.id;
            }
            lat =navData.getCrossingLatE6(cross.id);
            lon = navData.getCrossingLongE6(cross.id);
            Coordinate coordinate = new Coordinate(lon/1000000,lat/1000000);
            coordinates[cnt] =coordinate;
            cnt++;
        }
        lat =navData.getCrossingLatE6(crossId);
        lon = navData.getCrossingLongE6(crossId);
        Coordinate coordinate = new Coordinate(lon/1000000,lat/1000000);
        coordinates[cnt] =coordinate;
       return coordinates;
    }

    private static void initAStarAlgo(){
        OpenListComp openListComp = new OpenListComp();

        openList = new PriorityQueue(2000, openListComp);
        openListMap = new HashMap<Integer, Crossing>(2000);

        closedList = new HashMap<Integer, Crossing>(300000);

        int startCrossID = navData.getNearestCrossing(startLat, startLon);
        Crossing startCrossing = new Crossing(startCrossID, navData, totalSeconds);
        openList.add(startCrossing);
        openListMap.put(startCrossing.id, startCrossing);
    }

    private static void expandCrossing(Crossing activeCrossing){
        int[] neighboursID = activeCrossing.getNeighboursIDs();

        for (int i = 0; i < neighboursID.length; i++){
            int neighbourID = neighboursID[i];

            if (neighbourID != activeCrossing.getPreviousCrossingID()){

                Crossing neighbour = getCrossingFromOpenList(neighbourID);

                if (neighbour != null){
                    // this neighbour crossing is already in the openList
                    if (neighbour.updateGValue(activeCrossing)){
                        neighbour.setPreviousCrossing(activeCrossing);
                        openList.remove(neighbour);
                        openList.add(neighbour);
                    }
                }
                else{
                    neighbour = getCrossingFromClosedList(neighbourID);

                    if (neighbour == null){
                        // this neighbour crossing is not found yet
                        Crossing newCrossing = new Crossing(neighbourID, activeCrossing);
                        openList.add(newCrossing);
                        openListMap.put(newCrossing.id, newCrossing);

                    }
                }
            }
        }

        closedList.put(activeCrossing.id, activeCrossing);


    }

    private static Crossing getCrossingFromOpenList(int crossingID){
        return  openListMap.get(crossingID);
    }

    private static Crossing getCrossingFromClosedList(int crossingID){
        return  closedList.get(crossingID);
    }

}

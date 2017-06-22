import com.vividsolutions.jts.geom.Coordinate;
import fu.esi.SQL;
import fu.util.ConcaveHullGenerator;
import nav.NavData;
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
        totalSeconds = 90*60;
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


        DBConnection DBCon = new DBConnection("geosrv.informatik.fh-nuernberg.de/5432/dbuser/dbuser/deproDB20","SELECT realname, geodata_point FROM domain WHERE geometry='P' AND lsiclass1 BETWEEN "+LSI[0]+" AND "+LSI[1]+" AND"+ SQL.createIndexQuery(ConcaveHullGenerator.concaveHullJTS(positions,1d), SQL.COMPLETELY_INSIDE));
        for(String str: DBCon.getDBData())
        {
            System.out.println(str);
        }
        //+" AND "+SQL.createIndexQuery(ConcaveHullGenerator.concaveHullJTS(positions,1d),SQL.COMPLETELY_INSIDE)
    }

    private static void generateConcaveHull(UniversalPainterWriter upw) {
        for (Crossing cross: closedList.values()) {
            positions.add(convertToDoubleArray(navData.getCrossingLatE6(cross.id),navData.getCrossingLongE6(cross.id)));
        }
        ArrayList<double[]>  hullPositions = ConcaveHullGenerator.concaveHull(positions,0.04d);
        upw.polygon(hullPositions,102,102,102,200);
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
        for (Crossing cross: closedList.values()) {
            if (cnt==0)
            {
                crossId=cross.id;
            }
            Coordinate coordinate = new Coordinate(navData.getCrossingLatE6(cross.id)/1000000,navData.getCrossingLongE6(cross.id)/1000000);
            coordinates[cnt] =coordinate;
            cnt++;
        }
        Coordinate coordinate = new Coordinate(navData.getCrossingLatE6(crossId)/1000000,navData.getCrossingLongE6(crossId)/1000000);
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

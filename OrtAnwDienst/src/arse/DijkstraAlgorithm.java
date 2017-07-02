package arse;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import fu.geo.Spherical;
import fu.geo.SphericalJTS;
import nav.NavData;

import java.util.*;


public class DijkstraAlgorithm {

    private static Map<Integer, Crossing> closedList;
    private static PriorityQueue<Crossing> openList;
    private static Map<Integer, Crossing> openListMap;

    public static Map<Integer, Crossing> getClosedList() {return closedList;}
    private static Crossing getCrossingFromOpenList(int crossingID){return  openListMap.get(crossingID);}
    private static Crossing getCrossingFromClosedList(int crossingID){return  closedList.get(crossingID);}


    public static void runAlgorithm(){
        initDijkstraAlgorithm();

        boolean isAStarTerminated = false;
        Crossing activeCrossing;

        // The main loop of the Dijkstra algorithm
        while(!isAStarTerminated){
            if (openList.isEmpty() || openList.peek().gVal > Isochrone.getTotalSeconds()){
                isAStarTerminated = true;
            }
            else{
                //Take the next crossing from the open list and expand it
                activeCrossing = openList.poll();
                openListMap.remove(activeCrossing.id);
                expandCrossing(activeCrossing);
            }
        }

        //Fall 1: Start innerhalb, Ende Außerhalb
        while (openList.peek() !=null)
        {
            Crossing cross = openList.poll();
            int[] neighboursIDs= cross.getNeighboursIDs();
            int cnt=0;
            for (int crossid:neighboursIDs ){
                Crossing insideCross = getCrossingFromClosedList(crossid);
                if(insideCross!= null){

                    int linkId = cross.getOutgoingLinksIDs()[cnt];
                    if (!Isochrone.getNavData().goesCounterOneway(linkId))
                    {
                        checkLinkGeometryPoints(linkId, insideCross.gVal);
                    }
                }
                cnt++;
            }
        }
    }

    private static void checkLinkGeometryPoints(int linkId, double gval){
        NavData navData = Isochrone.getNavData();
        int start = navData.getDomainPosNrFrom(linkId);
        int end = navData.getDomainPosNrTo(linkId);
        int domain = navData.getDomainID(linkId);
        int[] domainLatsE6= navData.getDomainLatsE6(domain);
        int[] domainLongsE6 = navData.getDomainLongsE6(domain);
        ArrayList<double[]> coordinates= new ArrayList<double[]>();
       
        if (end < start){
            for(int i = end; i<=start;i++){
                coordinates.add(ConcaveHullCreation.convertToDoubleArray(domainLongsE6[i],domainLatsE6[i]));
            }

        }
        else{
            for(int i = start; i<=end;i++){
                coordinates.add(ConcaveHullCreation.convertToDoubleArray(domainLongsE6[i],domainLatsE6[i]));
            }
            Collections.reverse(coordinates);
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        for (int i = 0; i<coordinates.size()-1;i++){
            Coordinate coordinate = new Coordinate(coordinates.get(i)[0],coordinates.get(i)[1]);
            Coordinate newcoordinate = new Coordinate(coordinates.get(i+1)[0],coordinates.get(i+1)[1]);

            double distance=SphericalJTS.metersBetween(geometryFactory.createPoint(coordinate),geometryFactory.createPoint(newcoordinate));


            int speed = Crossing.getSpeedLimit(linkId);

            double linkMaxSpeedMS = speed / 3.6;
            double newgval = distance/linkMaxSpeedMS;
            if (gval+newgval>Isochrone.getTotalSeconds()) {
                   return;
            }

            ConcaveHullCreation.closedPositions.add(coordinates.get(i+1));
            gval= gval +newgval;
        }

    }

       // Creating the open list, closed list and the start crossing
    private static void initDijkstraAlgorithm(){
        OpenListComp openListComp = new OpenListComp();

        // Initialize the lists
        openList = new PriorityQueue<Crossing>(2000, openListComp);
        openListMap = new HashMap<Integer, Crossing>(2000);

        closedList = new HashMap<Integer, Crossing>(300000);

        int startCrossID = Isochrone.getNavData().getNearestCrossing(Isochrone.getStartLat(),Isochrone.getStartLon());
        Crossing startCrossing = new Crossing(startCrossID);
        openList.add(startCrossing);
        openListMap.put(startCrossing.id, startCrossing);
    }

    //expanding the actual crossing according to the Dijkstra algorithm
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
                    // if this neighbour crossing is in the closed list, do nothing
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


        //Fall 2: ab Link-Länge werden alle Geometriepunkte bei der Berechnung der Isochrone berücksichtigt
        int[] neighboursIDs= activeCrossing.getNeighboursIDs();
        int cnt=0;
        for (int crossid:neighboursIDs ){
            Crossing insideCross = getCrossingFromClosedList(crossid);
            if(insideCross!= null){
                int reverseLink = Isochrone.getNavData().getReverseLink(activeCrossing.getOutgoingLinksIDs()[cnt]);
                int forwardLink = activeCrossing.getOutgoingLinksIDs()[cnt];
                if (!Isochrone.getNavData().goesCounterOneway(reverseLink)) {
                    inspectDomainGeometry(reverseLink,activeCrossing.gVal);
                }
                if (!Isochrone.getNavData().goesCounterOneway(forwardLink)) {
                    inspectDomainGeometry(forwardLink, activeCrossing.gVal);
                }

            }
            cnt++;
        }
        //put the crossing in the closed list after its completely expanded
        closedList.put(activeCrossing.id, activeCrossing);
    }

    private static void inspectDomainGeometry(int linkId, double gval){
        if (Isochrone.getNavData().getLengthMeters(linkId) >50){
            checkLinkGeometryPoints(linkId,gval);
        }
    }
}

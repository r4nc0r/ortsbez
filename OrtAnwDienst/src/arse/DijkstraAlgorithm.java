package arse;

import com.vividsolutions.jts.awt.PointShapeFactory;
import com.vividsolutions.jts.geom.Coordinate;
import nav.NavData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;


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
            if (openList.peek().gVal > Isochrone.getTotalSeconds()){
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

                    int linkId = Isochrone.getNavData().getReverseLink(cross.getOutgoingLinksIDs()[cnt]);
                    if (!Isochrone.getNavData().goesCounterOneway(linkId))
                    {
                        addLastGeometryPoint(linkId, insideCross.gVal);
                    }
                }
                cnt++;
            }
        }
    }

    private static void addLastGeometryPoint(int linkId, double gval){
        NavData navData = Isochrone.getNavData();
        int start = navData.getDomainPosNrFrom(linkId);
        int end = navData.getDomainPosNrTo(linkId);
        int domain = navData.getDomainID(linkId);
        int[] domainLatsE6= navData.getDomainLatsE6(domain);
        int[] domainLongsE6 = navData.getDomainLongsE6(domain);
        ArrayList<double[]> coordinates= new ArrayList<double[]>();

        for(int i = start; i<=end;i++){
            coordinates.add(ConcaveHullCreation.convertToDoubleArray(domainLongsE6[i],domainLatsE6[i]));

        }
        for (int i = 0; i<coordinates.size()-1;i++){
            double distance =distanceBetweenCoordinates(coordinates.get(i)[0],coordinates.get(i)[1],coordinates.get(i+1)[0],coordinates.get(i+1)[1]);
            int speed = Crossing.getSpeedLimit(linkId);

            double linkMaxSpeedMS = speed / 3.6;
            double newgval = distance/linkMaxSpeedMS;
            if (gval+newgval<Isochrone.getTotalSeconds()) {
                if (coordinates.get(i+1)[1] ==49.492040 )
                {
                    System.out.println("");
                }
                ConcaveHullCreation.closedPositions.add(coordinates.get(i+1));
                gval= gval +newgval;
            }

        }


    }

    private static double distanceBetweenCoordinates(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return (double) (float) (earthRadius * c);
    }


    // Creating the open list, closed list and the start crossing
    private static void initDijkstraAlgorithm(){
        OpenListComp openListComp = new OpenListComp();

        // Initialize the lists
        openList = new PriorityQueue(2000, openListComp);
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
            addLastGeometryPoint(linkId,gval);
        }
    }
}

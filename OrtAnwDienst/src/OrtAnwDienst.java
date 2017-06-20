/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import fu.util.ConcaveHullGenerator;
import nav.NavData;
import pp.dorenda.client2.additional.UniversalPainterWriter;

import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 *
 * @author Michael
 */
public class OrtAnwDienst {

    private static nav.NavData navData;
    private static List<Crossing> closedList;
    private static PriorityQueue<Crossing> openList;
    private static ArrayList<double[]> positions;
    private static int startLat;
    private static int startLon;
    private static int totalSeconds;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        long startTime = System.nanoTime();

        startLat = 49465646;
        startLon = 11154443;
        // totalSeconds = 1 * 60;
        totalSeconds = 3 * 60;

        navData = new NavData("roth\\Roth_LBS\\CAR_CACHE_mittelfranken_noCC.CAC", true);

        boolean goOn = true;
        Crossing activeCrossing;

        initAStarAlgo();

        while(goOn){
            System.out.println("Open List Größe: " + openList.size());
            if (openList.peek().gVal > totalSeconds){
                goOn = false;
            }
            else{
                activeCrossing = openList.poll();
                expandCrossing(activeCrossing);
            }
        }

        printAStarDuration(startTime);

//        for (Crossing cross:closedList) {
//            System.out.println((double) navData.getCrossingLatE6(cross.id) / 1000000 + " " + (double) navData.getCrossingLongE6(cross.id) / 1000000);
//        }

        positions = new ArrayList<double[]>();
        for (Crossing cross:closedList) {
            positions.add(convertToDoubleArray(navData.getCrossingLatE6(cross.id),navData.getCrossingLongE6(cross.id)));
        }
        UniversalPainterWriter upw = new UniversalPainterWriter("result.txt");
        upw.line(ConcaveHullGenerator.concaveHull(positions,0.2),0,255,0,200,4,3,null,null,null);
        upw.close();

    }

    private static void printAStarDuration(long startTime) {
        long diffNano = System.nanoTime() - startTime;
        long diffMS = TimeUnit.MILLISECONDS.convert(diffNano, TimeUnit.NANOSECONDS);
        long diffSec = TimeUnit.SECONDS.convert(diffNano, TimeUnit.NANOSECONDS);
        long diffMin = TimeUnit.MINUTES.convert(diffNano, TimeUnit.NANOSECONDS);

        System.out.println("Total execution time: " + diffMS + " ms");
        System.out.println("Total execution time: "
                + diffMin + " min, "
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

    private static void initAStarAlgo(){
        OpenListComp openListComp = new OpenListComp();
        ClosedListComp closedListComp = new ClosedListComp();

        openList = new PriorityQueue(500, openListComp);
        closedList = new ArrayList<Crossing>();

        int startCrossID = navData.getNearestCrossing(startLat, startLon);
        Crossing startCrossing = new Crossing(startCrossID, navData);
        openList.add(startCrossing);
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

                    }
                }
            }
        }

        openList.remove(activeCrossing);
        closedList.add(activeCrossing);
    }

    private static Crossing getCrossingFromOpenList(int crossingID){
        for (Crossing tempCrossing : openList) {
            if (tempCrossing.id == crossingID)
                return tempCrossing;
        }
        return null;
    }

    private static Crossing getCrossingFromClosedList(int crossingID){
        for (Crossing tempCrossing : closedList) {
            if (tempCrossing.id == crossingID)
                return tempCrossing;
        }
        return null;
    }

}


















        // .getDomainName(1234);
        
        /*int[] links = navData.getLinksForCrossing(startCrossID);
        int link = links[0];
        int cross2 = navData.getCrossingIDTo(link);

        int lsinr = navData.getLSIclass(link);
        LSIClass lc = LSIClassCentre.lsiClassByID(lsinr);
        
        System.out.println("Ausgabe:");
        System.out.println(navData.getCrossingLatE6(startCrossID));
        System.out.println(navData.getCrossingLongE6(startCrossID));
        System.out.println(navData.getCrossingLatE6(cross2));
        System.out.println(navData.getCrossingLongE6(cross2));
        System.out.println(lc.className);
        
        
        myComparator comp = new myComparator();
        PriorityQueue<Node> queue = new PriorityQueue(200, comp);
        
        Node node1 = new Node(2);
        Node node2 = new Node(7);
        Node node3 = new Node(6);
        Node node4 = new Node(1);*/
        
        /*queue.add(node1);
        queue.add(node2);
        queue.add(node3);
        queue.add(node4);
        
        Iterator<Node> iter = queue.iterator();
        boolean found = false;
        
        while (iter.hasNext() && !found) {
            System.out.println("searching...");
            
            if (iter.next().dist == 6) {
                System.out.println("FOUND!");
                found = true;
            }
        }
        
        System.out.println("Asugabe:");
        System.out.println(queue.poll().dist);
        System.out.println(queue.poll().dist);
        System.out.println(queue.poll().dist);
        System.out.println(queue.poll().dist);*/




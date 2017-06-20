/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import fu.util.ConcaveHullGenerator;
import nav.NavData;
import pp.dorenda.client2.additional.UniversalPainterWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

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

        startLat = 49605031;
        startLon = 10994610;
        // totalSeconds = 1 * 60;
        totalSeconds = 10;

        navData = new NavData("roth\\Roth_LBS\\CAR_CACHE_mittelfranken_noCC.CAC", true);

        initAStarAlgo();

        boolean goOn = true;
        Crossing activeCrossing;

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
        positions = new ArrayList<double[]>();
        for (Crossing cross:closedList) {
            positions.add(convertToDoubleArray(navData.getCrossingLatE6(cross.id),navData.getCrossingLongE6(cross.id)));
        }
        UniversalPainterWriter upw=new UniversalPainterWriter("result.txt");
        upw.line(ConcaveHullGenerator.concaveHull(positions,0.2),0,255,0,200,4,3,null,null,null);
        upw.close();

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
        myComparator OpenListComp = new myComparator();
        openList = new PriorityQueue(500, OpenListComp);

        closedList = new ArrayList<Crossing>();

        int startCrossID = navData.getNearestCrossing(startLat, startLon);
        Crossing startCrossing = new Crossing(startCrossID, navData);
        // System.out.println("size1: " + openList.size());
        openList.add(startCrossing);
        // System.out.println("size2: " + openList.size());
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
                        openList.remove(neighbour);
                        openList.add(neighbour);
                    }
                }
                else{
                    neighbour = getCrossingFromClosedList(neighbourID);

                    if (neighbour == null){
                        // this neighbour crossing is not found yet
                        Crossing newCrossing = new Crossing(neighbourID, activeCrossing, navData);
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
        // closedList.add();
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




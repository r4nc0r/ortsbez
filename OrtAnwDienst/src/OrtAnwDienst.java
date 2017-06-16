/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import nav.NavData;

import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Michael
 */
public class OrtAnwDienst {

    private static nav.NavData navData;
    private static List<Crossing> closedList;
    private static PriorityQueue<Crossing> openList;
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
        totalSeconds = 300;


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




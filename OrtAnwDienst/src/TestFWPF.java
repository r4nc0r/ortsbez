/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import fu.keys.LSIClass;
import fu.keys.LSIClassCentre;
import nav.NavData;

import java.util.Iterator;
import java.util.PriorityQueue;

/**
 *
 * @author Michael
 */
public class TestFWPF {

    private static nav.NavData navData;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        int startLat = 49605031;
        int startLon = 10994610;
        int time = 20;

        navData = new NavData("C:\\Users\\aroldmi61242\\Documents\\Projects\\Roth_LBS\\CAR_CACHE_mittelfranken_noCC.CAC", true);

        int cross = navData.getNearestCrossing(startLat, startLon);
        Crossing start = new Crossing(cross, navData);



















        // .getDomainName(1234);
        
        int[] links = navData.getLinksForCrossing(cross);
        int link = links[0];
        int cross2 = navData.getCrossingIDTo(link);

        int lsinr = navData.getLSIclass(link);
        LSIClass lc = LSIClassCentre.lsiClassByID(lsinr);
        
        System.out.println("Ausgabe:");
        System.out.println(navData.getCrossingLatE6(cross));
        System.out.println(navData.getCrossingLongE6(cross));
        System.out.println(navData.getCrossingLatE6(cross2));
        System.out.println(navData.getCrossingLongE6(cross2));
        System.out.println(lc.className);
        
        
        myComparator comp = new myComparator();
        PriorityQueue<Node> queue = new PriorityQueue(200, comp);
        
        Node node1 = new Node(2);
        Node node2 = new Node(7);
        Node node3 = new Node(6);
        Node node4 = new Node(1);
        
        queue.add(node1);
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
        System.out.println(queue.poll().dist);
        
        
    }
    
}



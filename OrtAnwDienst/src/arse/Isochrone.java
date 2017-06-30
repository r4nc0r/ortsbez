package arse;

import nav.NavData;
import pp.dorenda.client2.additional.UniversalPainterWriter;

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
        long durationCACLoadTime = startTimeAfterTables - startTime;

        UniversalPainterWriter upw = new UniversalPainterWriter("result.txt");
        long startTimeAfterUPW = System.nanoTime();

        DijkstraAlgorithm.runAlgorithm();
        long durationAStar = System.nanoTime() - startTimeAfterUPW;

        System.out.println("\nCAC Loading Time:");
        printDurationNano(durationCACLoadTime);

        System.out.println("\nDijkstra Duration:");
        printDurationNano(durationAStar);

        System.out.println("\nGenerating Concave Hull:");
        startTime = System.nanoTime();

        ConcaveHullCreation.generateConcaveHull(upw);

        System.out.println("\nConcave Hull Duration:");
        printDurationNano(System.nanoTime() - startTime);

        System.out.println("\nStarting DB-Query:");
        startTime = System.nanoTime();
        DBQuery.doDBQuery(DBParams,upw,startLSI,endLSI);

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

    // print the measured time formatted in the console
    private static void printDurationNano(long duration) {
        long diffMS = TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS);
        long diffSec = TimeUnit.SECONDS.convert(duration, TimeUnit.NANOSECONDS);
        long diffMin = TimeUnit.MINUTES.convert(duration, TimeUnit.NANOSECONDS);

        System.out.println(diffMin + " min, "
                + (diffSec - TimeUnit.SECONDS.convert(diffMin, TimeUnit.MINUTES)) + " sec, "
                + (diffMS - (TimeUnit.MILLISECONDS.convert(diffMin, TimeUnit.MINUTES) + TimeUnit.MILLISECONDS.convert(diffSec, TimeUnit.SECONDS))) + " ms"
        );
    }
}

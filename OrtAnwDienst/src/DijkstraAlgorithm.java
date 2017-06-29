import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Created by samue on 29.06.2017.
 */
public class DijkstraAlgorithm {

    private static Map<Integer, Crossing> closedList;

    private static PriorityQueue<Crossing> openList;

    private static Map<Integer, Crossing> openListMap;


    public static Map<Integer, Crossing> getClosedList() {return closedList;}

    private static Crossing getCrossingFromOpenList(int crossingID){
        return  openListMap.get(crossingID);
    }

    private static Crossing getCrossingFromClosedList(int crossingID){
        return  closedList.get(crossingID);
    }


    public static void runAlgorithm(){
        boolean isAStarTerminated = false;
        Crossing activeCrossing;
        initDijkstraAlgorithm();
        while(!isAStarTerminated){
            if (openList.peek().gVal > Isochrone.getTotalSeconds()){
                isAStarTerminated = true;
            }
            else{
                activeCrossing = openList.poll();
                openListMap.remove(activeCrossing.id);
                expandCrossing(activeCrossing);
            }
        }
    }

    private static void initDijkstraAlgorithm(){
        OpenListComp openListComp = new OpenListComp();

        openList = new PriorityQueue(2000, openListComp);
        openListMap = new HashMap<Integer, Crossing>(2000);

        closedList = new HashMap<Integer, Crossing>(300000);

        int startCrossID = Isochrone.getNavData().getNearestCrossing(Isochrone.getStartLat(),Isochrone.getStartLon());
        Crossing startCrossing = new Crossing(startCrossID);
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
}

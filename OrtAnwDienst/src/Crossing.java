/**
 * Created by aroldmi61242 on 08.06.2017.
 */
public class Crossing {

    private static nav.NavData navData;

    private static int counter;

    private int[] outgoingLinksIDs;
    private int[] neighboursIDs;
    private Crossing previousCrossing;

    int id;
    double gVal;

    public Crossing(int id, nav.NavData navData){

        counter++;
        System.out.println("Crossings: " + counter);

        this.navData = navData;
        this.previousCrossing = null;
        this.id = id;
        this.outgoingLinksIDs = navData.getLinksForCrossing(id);
        this.gVal = 0;
        neighboursIDs = new int[outgoingLinksIDs.length];

        for (int i = 0; i < outgoingLinksIDs.length; i++)
        {
            neighboursIDs[i] = navData.getCrossingIDTo(outgoingLinksIDs[i]);
        }
    }

    public Crossing(int id, Crossing previousCrossing, nav.NavData navData){

        counter++;
        System.out.println("Crossings: " + counter);

        this.navData = navData;
        this.previousCrossing = previousCrossing;
        this.id = id;
        this.outgoingLinksIDs = navData.getLinksForCrossing(id);
        neighboursIDs = new int[outgoingLinksIDs.length];

        for (int i = 0; i < outgoingLinksIDs.length; i++)
        {
            neighboursIDs[i] = navData.getCrossingIDTo(outgoingLinksIDs[i]);
        }
        // System.out.println(outgoingLinksIDs);
        // System.out.println(neighboursIDs);

        setGValue();
    }

    public boolean updateGValue(Crossing newPreviousCrossing){
        int linkIDPreviousToThis = getLinkIDPreviousToThis(newPreviousCrossing.id);
        double newGVal = newPreviousCrossing.gVal + getLinkIDDriveTime(linkIDPreviousToThis);

        if (newGVal < this.gVal){
            this.gVal = newGVal;
            return true;
        }
        return false;
    }

    private void setGValue(){
        int linkIDPreviousToThis = getLinkIDPreviousToThis(previousCrossing.id);
        this.gVal = previousCrossing.gVal + getLinkIDDriveTime(linkIDPreviousToThis);
    }

    private int getLinkIDPreviousToThis(int previousID){
        // System.out.println("length: " + neighboursIDs.length);
        for(int i = 0; i < neighboursIDs.length; i++){

            // System.out.println("i: " + i);

            if (neighboursIDs[i] == previousID){
                int linkIDPreviousToThis = navData.getReverseLink(outgoingLinksIDs[i]);

                if (navData.goesCounterOneway(linkIDPreviousToThis)){
                    return -1; // Link is counter oneway
                }
                else
                    return linkIDPreviousToThis;
            }
        }

        return -1;  // Link to Previous not found
    }

    private double getLinkIDDriveTime(int linkID){
        // System.out.println("test: " + linkID);
        if (linkID == -1)
            return 0;

        int linkLength = navData.getLengthMeters(linkID);
        int linkMaxSpeedKMH = navData.getMaxSpeedKMperHours(linkID);

        if (linkMaxSpeedKMH == 0)
            linkMaxSpeedKMH = 130;

        double linkMaxSpeedMS = linkMaxSpeedKMH / 3.6;

        return (int) Math.round(linkLength / linkMaxSpeedMS);
    }

    public int[] getNeighboursIDs(){
        return neighboursIDs;
    }

    public int getPreviousCrossingID(){
        if (previousCrossing == null)
            return -1;
        else
            return previousCrossing.id;
    }
}
/**
 * Created by aroldmi61242 on 08.06.2017.
 */
public class Crossing {

    private static nav.NavData navData;

    private static int counter;
    private static Boolean test;

    private int[] outgoingLinksIDs;
    private int[] neighboursIDs;
    private Crossing previousCrossing;

    int id;
    double gVal;

    public Crossing(int id, nav.NavData navData){

        test = true;
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

    public Crossing(int id, Crossing previousCrossing,int time){

        counter++;
        System.out.println("Crossings: " + counter);

//        this.navData = navData;
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

        setGValue(time);
    }

    public boolean updateGValue(Crossing newPreviousCrossing){
        int linkIDPreviousToThis = getLinkIDPreviousToThis(newPreviousCrossing.id);
        if (linkIDPreviousToThis ==-1)
        {
            return false;
        }
        else {
            double newGVal = newPreviousCrossing.gVal + getLinkIDDriveTime(linkIDPreviousToThis);

            if (newGVal < this.gVal) {
                this.gVal = newGVal;
                return true;
            }
            return false;
        }
    }

    private void setGValue(int time){
        int linkIDPreviousToThis = getLinkIDPreviousToThis(previousCrossing.id);
        if (linkIDPreviousToThis ==-1)
        {
            this.gVal = time*2;
        }
        else{
            this.gVal = previousCrossing.gVal + getLinkIDDriveTime(linkIDPreviousToThis);
            System.out.println("gVal: " + this.gVal);
        }

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
        int lsi;

        if (linkMaxSpeedKMH == 0) {
            lsi =navData.getLSIclass(linkID);
            if(lsi >=34100000 && lsi<=34120000){
                linkMaxSpeedKMH=120;
            }
            else if(lsi >=34130000 && lsi<=34134000){
                linkMaxSpeedKMH=100;
            }
            else if(lsi >=34140000 && lsi<=34142000){
                linkMaxSpeedKMH=50;
            }
            else if(lsi >=34143000 && lsi<=34143200){
                linkMaxSpeedKMH=3;
            }
            else if(lsi >=34150000 && lsi<=34160000){
                linkMaxSpeedKMH=5;
            }
            else if(lsi >=34170000 && lsi<=34176000){
                linkMaxSpeedKMH=50;
            }
            else {
                return 0;
            }
        }


        double linkMaxSpeedMS = linkMaxSpeedKMH / 3.6;

//        if (test){
//            System.out.println("Test:");
//            System.out.println((double) navData.getCrossingLatE6(navData.getCrossingIDFrom(linkID)) / 1000000 + " " + (double) navData.getCrossingLongE6(navData.getCrossingIDFrom(linkID)) / 1000000);
//            System.out.println((double) navData.getCrossingLatE6(navData.getCrossingIDTo(linkID)) / 1000000 + " " + (double) navData.getCrossingLongE6(navData.getCrossingIDTo(linkID)) / 1000000);
//            System.out.println(linkLength);
//            System.out.println(linkMaxSpeedKMH + " " + linkMaxSpeedMS);
//            System.out.println(linkLength / linkMaxSpeedMS);
//            test = false;
//        }

//        return (int) Math.round(linkLength / linkMaxSpeedMS);
        return linkLength / linkMaxSpeedMS;
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

    public void setPreviousCrossing(Crossing previousCrossing) {
        this.previousCrossing = previousCrossing;
    }
}

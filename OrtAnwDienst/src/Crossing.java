

public class Crossing {

    private static nav.NavData navData;

    private static int crossingCounter;
    private static int totalSeconds;

    private int[] outgoingLinksIDs;
    private int[] neighboursIDs;
    private Crossing previousCrossing;

    int id;
//    String idStr;
    double gVal;

    public Crossing(int id, nav.NavData navData, int totalSeconds){

        crossingCounter++;
       // System.out.println("Crossings: " + crossingCounter);

        this.navData = navData;
        this.previousCrossing = null;
        this.id = id;
//        this.idStr = id + "";
        this.totalSeconds = totalSeconds;
        this.outgoingLinksIDs = navData.getLinksForCrossing(id);
        this.gVal = 0;
        neighboursIDs = new int[outgoingLinksIDs.length];

        for (int i = 0; i < outgoingLinksIDs.length; i++)
        {
            neighboursIDs[i] = navData.getCrossingIDTo(outgoingLinksIDs[i]);
        }
    }

    public Crossing(int id, Crossing previousCrossing){

        crossingCounter++;
        //System.out.println("Crossings: " + crossingCounter);

        this.previousCrossing = previousCrossing;
        this.id = id;
//        this.idStr = id + "";
        this.outgoingLinksIDs = navData.getLinksForCrossing(id);
        neighboursIDs = new int[outgoingLinksIDs.length];

        for (int i = 0; i < outgoingLinksIDs.length; i++)
        {
            neighboursIDs[i] = navData.getCrossingIDTo(outgoingLinksIDs[i]);
        }

        setGValue();
    }

    public boolean updateGValue(Crossing newPreviousCrossing){
        int linkIDPreviousToThis = getLinkIDPreviousToThis(newPreviousCrossing.id);

        if (linkIDPreviousToThis ==-1)
        {
            return false;
        }
        else
        {
            double newGVal = newPreviousCrossing.gVal + getLinkIDDriveTime(linkIDPreviousToThis);

            if (newGVal < this.gVal) {
                this.gVal = newGVal;
                return true;
            }
            return false;
        }
    }

    private void setGValue(){
        int linkIDPreviousToThis = getLinkIDPreviousToThis(previousCrossing.id);
        if (linkIDPreviousToThis == -1)
        {
            this.gVal = totalSeconds * 2;
        }
        else{
            this.gVal = previousCrossing.gVal + getLinkIDDriveTime(linkIDPreviousToThis);
            //System.out.println("gVal: " + this.gVal);
        }
    }

    private int getLinkIDPreviousToThis(int previousID){
        for(int i = 0; i < neighboursIDs.length; i++){

            if (neighboursIDs[i] == previousID){
                int linkIDPreviousToThis = navData.getReverseLink(outgoingLinksIDs[i]);

                if (navData.goesCounterOneway(linkIDPreviousToThis)){
                    return -1; // Link is crossingCounter oneway
                }
                else
                    return linkIDPreviousToThis;
            }
        }

        return -1;  // Link to Previous not found
    }

    private double getLinkIDDriveTime(int linkID){
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Crossing crossing = (Crossing) o;

        return id == crossing.id;
    }
}

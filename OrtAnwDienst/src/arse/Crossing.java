package arse;

import fu.keys.LSIClass;
import fu.keys.LSIClassCentre;

public class Crossing {

    private static nav.NavData navData;
    private static int totalSeconds;

    public int[] getOutgoingLinksIDs() {return outgoingLinksIDs;}

    private int[] outgoingLinksIDs;
    private int[] neighboursIDs;
    private Crossing previousCrossing;

    public int id;
    public double gVal;

    public Crossing(int id){
        navData = Isochrone.getNavData();
        this.previousCrossing = null;
        this.id = id;
        totalSeconds = Isochrone.getTotalSeconds();
        this.outgoingLinksIDs = navData.getLinksForCrossing(id);
        this.gVal = 0;
        neighboursIDs = new int[outgoingLinksIDs.length];

        for (int i = 0; i < outgoingLinksIDs.length; i++)
        {
            neighboursIDs[i] = navData.getCrossingIDTo(outgoingLinksIDs[i]);
        }
    }

    public Crossing(int id, Crossing previousCrossing){
        this.previousCrossing = previousCrossing;
        this.id = id;
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
        }
    }

    public static int getSpeedLimit(int linkId){
        int linkMaxSpeedKMH = navData.getMaxSpeedKMperHours(linkId);

        if (linkMaxSpeedKMH == 0) {
            //If Autobahn oder KRAFTFAHRSTRASSE
            int lsi=navData.getLSIclass(linkId);
            if(lsi >=34100000 && lsi<=34120000){
                return 120;
            }
            //Bundesstraße
            else if(lsi==34131000){
                return 100;
            }
            //Landstraße (sekundär)
            else if (lsi == 34132000){ return 80;}
            //Landstraße (tertiär)
            else if (lsi == 34133000){ return 70;}
            //Landstraße (unklassifiziert)
            else if (lsi == 34130000 || lsi == 34134000){ return 80; }
            //if Innerorts
            else if(lsi >=34140000 && lsi<=34141000){
                return 50;
            }
            //if verkehrsberuighter Bereich
            else if( lsi==34142000){
                return 3;
            }
            //if Erschließungsweg
            else if(lsi >=34143000 && lsi<=34143200){
                return 3;
            }
            //if Waldwege
            else if(lsi >=34150000 && lsi<=34160000){
                return 20;
            }
            //if Auffahrtsstraße Autobahn
            else if(lsi >=34170000 && lsi<=34175000){
                return 100;
            }
            //if Kreisverkehr
            else if (lsi == 34176000)
            {
                return 30;
            }
            //Baustelle
            else if (lsi ==32711000)
            {
                return 30;
            }
            else {
                return 0;
            }
        }

       return linkMaxSpeedKMH;
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
        int linkLength = navData.getLengthMeters(linkID);
        int linkMaxSpeedKMH = getSpeedLimit(linkID);

        double linkMaxSpeedMS = linkMaxSpeedKMH / 3.6;

        return linkLength / linkMaxSpeedMS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Crossing crossing = (Crossing) o;

        return id == crossing.id;
    }

    public void setPreviousCrossing(Crossing previousCrossing) {this.previousCrossing = previousCrossing;}

    public int[] getNeighboursIDs(){return neighboursIDs;}

    public int getPreviousCrossingID(){
        if (previousCrossing == null)
            return -1;
        else
            return previousCrossing.id;
    }
}

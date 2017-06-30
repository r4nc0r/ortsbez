package arse;

import java.util.Comparator;


//The comparator for the open list implemented as priority queue
public class OpenListComp implements Comparator<Crossing> {
    public int compare(Crossing cross1, Crossing cross2){
        return Double.compare(cross1.gVal, cross2.gVal);
    }
}

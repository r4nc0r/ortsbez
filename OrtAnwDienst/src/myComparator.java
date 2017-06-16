/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Comparator;
/**
 *
 * @author Michael
 */
public class myComparator implements Comparator<Crossing> {
    
    public int compare(Crossing cross1, Crossing cross2){
        return Double.compare(cross1.gVal, cross2.gVal);
    }
}

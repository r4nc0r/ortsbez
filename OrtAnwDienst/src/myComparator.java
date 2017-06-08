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
public class myComparator implements Comparator<Node> {
    
    public int compare(Node n1, Node n2){
        return Integer.compare(n1.dist, n2.dist);
    }
}

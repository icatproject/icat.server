/*
 * d.java
 * 
 * Created on 15-Aug-2007, 15:57:47
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.test;

import java.util.ArrayList;
import java.util.Collection;
import uk.icat3.search.AdvancedSearchDetails;

/**
 *
 * @author gjd37
 */
public class d {

    /** Creates a new instance of d */
    public d() {
          AdvancedSearchDetails asd = new AdvancedSearchDetails();
          Collection<String> k = new ArrayList<String>();
          k.add("edf");
        System.out.println(asd.hasRunNumber());
        System.out.println(asd.isValid());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        new d();
    }

}

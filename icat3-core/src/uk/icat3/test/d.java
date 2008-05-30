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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import uk.icat3.search.AdvancedSearchDetails;

/**
 *
 * @author gjd37
 */
public class d {

    /** Creates a new instance of d */
    public d() {
          AdvancedSearchDetails asd = new AdvancedSearchDetails();
          asd.setRunEnd(9.9);
          asd.setRunStart(null);
          Collection<String> k = new ArrayList<String>();
          k.add("edf");
        System.out.println(asd.hasRunNumber());
        //System.out.println(asd.isValid());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
      //  new d();
        Properties props = System.getProperties();
        for (Object string : props.values()) {
            System.out.println(string);
        }
        Enumeration sd = props.keys();
        while(sd.hasMoreElements()){
            System.out.println(sd.nextElement());
        }
       
        //System.out.println(System.getProperties("-CacheClassPath.keepJars"));
    }

}

/*
 * NewMain.java
 * 
 * Created on 23-Oct-2007, 12:26:50
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.test;

import java.io.File;

/**
 *
 * @author gjd37
 */
public class NewMain {

    /** Creates a new instance of NewMain */
    public NewMain() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        File file = new File("c:/d/d/d");
        System.out.println(file.mkdir());
    }

}

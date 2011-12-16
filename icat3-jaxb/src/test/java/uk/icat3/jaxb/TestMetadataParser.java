/*
 * TestMetadataParser.java
 * 
 * Created on 23-May-2007, 16:49:47
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.jaxb;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 *
 * @author df01
 */
public class TestMetadataParser {

    public TestMetadataParser() {
    }
    
    public static void main(String[] args) {
            System.out.println("duke ");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("src/uk/icat3/jaxb/icatXML.xml")));
            String line = "";
            String buffer = "";
            while((line = br.readLine()) != null) {
            buffer += line;
            }
            System.out.println("buffer: " + buffer);
            
            MetadataParser.parseMetadata("userId", buffer);
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

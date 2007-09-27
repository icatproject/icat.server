/*
 * TestMetadataIngest.java
 * 
 * Created on 13-Jun-2007, 10:01:15
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.jaxb;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author df01
 */
public class TestMetadataIngest {

    public TestMetadataIngest() {
    }
    
    public static void main (String[] args) {
        EntityManagerFactory emf =  Persistence.createEntityManagerFactory("icat3-jaxbPU");
        EntityManager em = emf.createEntityManager();
        
        em.getTransaction().begin();
                

    try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("src/uk/icat3/jaxb/00019.xml")));
            //BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("c:/vms/nxingest-1.3/test_data/00018.xml")));
            String line = "";
            String buffer = "";
            while((line = br.readLine()) != null) {
            buffer += line;
            }
            System.out.println("buffer: " + buffer);           
        
            Long[] ids = MetadataIngest.ingestMetadata("dwf64", buffer, em);
            if ((ids != null) && (ids.length >0)) {
                for (Long id : ids) System.out.println("____" + id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        em.getTransaction().commit();
        
        em.close();
        
    }

   

}

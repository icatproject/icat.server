/*
 * AnnotationTest.java
 *
 * Created on 09 March 2007, 09:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.test;
import java.util.Collection;
import javax.persistence.Column;

public class AnnotationTest {
    
    public AnnotationTest(){
       Collection<String> c = null;
       
       for(String n : c){
           System.out.println(n);
       }
        
       /*
        
        TestEntity te = new TestEntity("d");
        //EntityBaseBean te = new EntityBaseBean();
        try {
            
            te.isValid();
        } catch (ValidationException ex) {
            ex.printStackTrace();
        }*/
    }
    
    @Column(updatable = false, name = "flight_name", nullable = false,        length=50)
    public String nothing;
    
    
    @Column(updatable = false, name = "flight_name", nullable = false,     length=50)
    private String nothingprivate;
    
    @Deprecated
    public void aMethod() {
    }
    
    public static void main(String[] args) throws Exception {
        new AnnotationTest();
    }
}
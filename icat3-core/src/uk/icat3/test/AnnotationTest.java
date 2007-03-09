/*
 * AnnotationTest.java
 *
 * Created on 09 March 2007, 09:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.test;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.persistence.Column;
import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.ValidationException;


public class AnnotationTest {
    
    public AnnotationTest(){
       
        TestEntity te = new TestEntity("d");
        //EntityBaseBean te = new EntityBaseBean();
        try {
            
            te.isValid();
        } catch (ValidationException ex) {
            ex.printStackTrace();
        }
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
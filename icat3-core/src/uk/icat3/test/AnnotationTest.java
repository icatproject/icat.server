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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.persistence.Column;
import uk.icat3.entity.ICAT;

public class AnnotationTest {
    
    public AnnotationTest() throws Exception{
        
        
        TestEntity te = new TestEntity("d");
        
        TestEntity fromte = new TestEntity("b");
        te.isValid();
    //    te.merge(fromte);
        
        //swapProperty("purposeSeen",fromte, te);
        
        System.out.println("old was d new is "+te.getPurposeSeen());
        
        //EntityBaseBean te = new EntityBaseBean();
       /* try {
        
            //te.isValid();
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
    
    @SuppressWarnings("all")
    public void swapProperty(String name, Object from, Object to) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        
        
        
        String prop = Character.toUpperCase(name.charAt(0)) +
                name.substring(1);
        String mname = "get" + prop;
        
        Class[] types = new Class[]{};
        Method method = from.getClass().getMethod(mname, types);
        Object result = method.invoke(from, (Object[])types);
        
        
        mname = "set" + prop;
        types = new Class[] { from.getClass().getDeclaredField(name).getType() };
        method = to.getClass().getMethod(mname, types);
        method.invoke(to, new Object[] { result });
    }
    
    public void merge(Object toObject, Object fromObject){
        
        Field[] passsedFields = fromObject.getClass().getDeclaredFields();
        Field[] thisFields = toObject.getClass().getDeclaredFields();
        
        outer: for (Field field : passsedFields) {
            //get name of field
            String fieldName = field.getName();
            //log.trace(fieldName);
            //now check all annoatations
            for (Annotation a : field.getDeclaredAnnotations()) {
                //if this means its a none null column field
                //log.trace(a.annotationType().getName());
                if(a.annotationType().getName().equals(ICAT.class.getName()) && a.toString().contains("merge=false") ){
                    System.out.println("not merging, icat(merge=false) "+fieldName);
                    continue outer;
                }
                if(!a.annotationType().getName().contains("Column") ||
                        a.annotationType().getName().contains("Id")){
                    System.out.println("not merging, not Column, or Id "+fieldName);
                    continue outer;
                }
            }
            
            try {
                for(Field thisField : thisFields) {
                    // log.trace(thisField);
                    if(thisField.getName().equals(fieldName)){
                        //now transfer the data
                        System.out.println("Setting "+fieldName+" to "+field.get(fromObject));
                        thisField.set(toObject, field.get(fromObject));
                    }
                }
            }  catch (Exception ex) {
                System.out.println("Error transferring data for field: "+fieldName+"\n"+ex);
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        new AnnotationTest();
    }
}
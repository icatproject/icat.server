package uk.icat3.exposed;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;



@RunWith(Suite.class)
@Suite.SuiteClasses({
           
})
public class TestAllWebServices {
       
    
    
    
    public static Test suite() {              
        
        return new JUnit4TestAdapter(TestAllWebServices.class);
    }
}

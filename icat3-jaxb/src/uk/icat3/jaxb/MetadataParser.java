package uk.icat3.jaxb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import uk.icat3.jaxb.gen.*;

/*
 * MetadataParser.java
 * 
 * Created on 23-May-2007, 13:45:18
 * 
 * This class uses JAXB to read and parse an ICAT compliant XML file.
 * Validating schema 'icatXSD.xsd' is used for validation.
 * 
 * @author df01
 */
public class MetadataParser {

    public static Icat parseMetadata(String userId, String xml) throws Exception {
                
       JAXBContext jaxbContext = JAXBContext.newInstance("uk.icat3.jaxb.gen");
       Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();

       SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);            
       URL url = MetadataParser.class.getResource("icatXSD.xsd");       
       if(url == null) throw new Exception("Unable to location icatXSD.xsd");
       Schema schema = sf.newSchema(url);            
       unMarshaller.setSchema(schema);           

       ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes("UTF-8"));
       JAXBElement<Icat> po = (JAXBElement<Icat>)unMarshaller.unmarshal(bais);                                  

       /*
       Icat icat = po.getValue();
       List<Study> studies = icat.getStudy();
        for (Study study : studies) {               
               System.out.println("study " + study.getName());               
               List<Investigation> investigations = study.getInvestigation();
               
               for (Investigation inv : investigations) {                   
                   System.out.println("inv " + inv.getTitle());    System.out.println("Trusted: " + inv.isTrusted());               
                   List<Dataset> datasets = inv.getDataset();                                       
                   
                   for (Dataset dataset : datasets) {                       
                       System.out.println("dataset " + dataset.getName());
                       List<Datafile> datafiles = dataset.getDatafile();
                       List<Parameter> dsParams = dataset.getParameter();
                       Sample sample = dataset.getSample();
                 
                               
                       for (Datafile datafile : datafiles) {                           
                           System.out.println("datafile " + datafile.getName());
                           List<Parameter> dfParams = datafile.getParameter();
                           
                           for (Parameter dfParam : dfParams) {                               
                               System.out.println("data file param " + dfParam.getName());                               
                           }//end for df parameter
                           
                       }//end for datafile
                   
                       
                       for (Parameter dsParam : dsParams) {                               
                           System.out.println("dataset param " + dsParam.getName());                               
                       }//end for df parameter
                       
                       
                   }
               }
           
           }
           */
       
       return po.getValue();              
    }
    
    

}

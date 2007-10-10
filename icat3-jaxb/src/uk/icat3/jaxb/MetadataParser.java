package uk.icat3.jaxb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
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

       return po.getValue();              
    }
    
    public static String marshalMetadata(Icat icat) throws Exception {
         JAXBContext jaxbContext = JAXBContext.newInstance("uk.icat3.jaxb.gen");
         
        Marshaller marshaller = jaxbContext.createMarshaller();
         StringWriter sw = new StringWriter();
         marshaller.marshal(icat, sw);        
        return sw.toString();
    }

}

// $Id: IcatUtil.java 934 2011-08-09 13:16:35Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import uk.icat.cmd.entity.Configuration;
import uk.icat3.client.BadParameterException;
import uk.icat3.client.EntitySummary;
import uk.icat3.client.FieldInfo;
import uk.icat3.client.ICAT;
import uk.icat3.client.ICATService;
import uk.icat3.client.ICATServiceLocator;
import uk.icat3.client.IcatInternalException;

public class IcatUtil {

	private ICAT serviceInstance;
	private String sid;

	public IcatUtil(Configuration config) throws Exception {
		try {
			URL baseUrl = uk.icat3.client.ICATService.class.getResource(".");
			URL serviceWsdlLocation = new URL(baseUrl, config.getUrl());

			//serviceInstance = new ICATServiceLocator(serviceWsdlLocation, new QName("client.icat3.uk", "ICATService")).getICATPort();
			serviceInstance = new ICATServiceLocator(config.getUrl(), new QName("client.icat3.uk", "ICATService")).getICATPort();
			sid = serviceInstance.login(config.getUser(), config.getPassword());
		} catch (Exception e) {
			System.err.println("Error during SID negotiation: " + e.getMessage());
			System.exit(1);
		}
	}
	

	public String getSid() {
		return sid;
	}

	public Object getService() {
		return serviceInstance;
	}


	public List<String> getMandatoryParameters(Class<?> type) throws Exception { 
		List<String> params = new ArrayList<String>();
		for (FieldInfo fi : serviceInstance.getEntitySummary(type.getSimpleName()).getFields()) {
			if (!fi.isNullable()) {
				params.add(fi.getName());
			}
		}
		return params;
	}

}

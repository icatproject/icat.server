package uk.icat3.sessionbeans.search;

import java.util.List;

import javax.ejb.Local;
import javax.persistence.EntityManager;

import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.exceptions.IcatInternalException;
import uk.icat3.exceptions.InsufficientPrivilegesException;
import uk.icat3.exceptions.SessionException;

@Local
public interface SearchLocal {
	
	public  List<?> search(String sessionId, String query) throws SessionException, IcatInternalException, BadParameterException, InsufficientPrivilegesException;
	
}

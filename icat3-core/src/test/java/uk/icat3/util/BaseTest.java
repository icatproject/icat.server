package uk.icat3.util;

import javax.persistence.EntityManager;

import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.Facility;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.InvestigationType;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.ParameterType;
import uk.icat3.entity.ParameterType.ParameterValueType;
import uk.icat3.entity.User;

public class BaseTest {

	static protected EntityManager em;

	protected ParameterType createParameter(String name, String units, boolean searchable,
			ParameterValueType valueType, boolean isSampleParameter, boolean isDatasetParameter,
			boolean isDatafileParameter, boolean facilityAcquired, boolean verified) throws Exception {
		ParameterType parameter = new ParameterType();
		parameter.setName(name);
		parameter.setUnits(units);
		parameter.setValueType(valueType);
		parameter.setApplicableToDatafile(isDatafileParameter);
		parameter.setApplicableToDataset(isDatasetParameter);
		parameter.setApplicableToSample(isSampleParameter);
		parameter.setVerified(verified);
		return parameter;
	}

	protected Investigation createInvestigation(String number, String title, InvestigationType type, Facility facility) {
		Investigation investigation = new Investigation();
		investigation.setName(number);
		investigation.setTitle(title);
		investigation.setType(type);
		investigation.setFacility(facility);
		return investigation;
	}

	protected User createFacilityUser(String userId) {
		User facilityUser = new User();
		facilityUser.setName(userId);
		return facilityUser;
	}

	protected Investigator createInvestigator(User fu, Investigation inv) {
		Investigator investigator = new Investigator();
		investigator.setUser(fu);
		investigator.setInvestigation(inv);
		return investigator;
	}

	protected Dataset createDataset(Investigation investigation, String dsn, DatasetType dst) {
		Dataset ds = new Dataset();
		ds.setInvestigation(investigation);
		ds.setName(dsn);
		ds.setType(dst);
		return ds;
	}

	protected DatasetType createDatasetType(String name, String description) {
		DatasetType dst = new DatasetType();
		dst.setName(name);
		dst.setDescription(description);
		return dst;
	}

	protected Datafile createDatafile(Dataset dataset, String name) {
		Datafile df = new Datafile();
		df.setDataset(dataset);
		df.setName(name);
		return df;
	}

}

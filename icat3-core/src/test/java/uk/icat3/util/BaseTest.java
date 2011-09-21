package uk.icat3.util;

import javax.persistence.EntityManager;

import uk.icat3.entity.Datafile;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetType;
import uk.icat3.entity.FacilityUser;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.InvestigatorPK;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.ParameterPK;

public class BaseTest {

	static protected EntityManager em;

	protected Parameter createParameter(String name, String units, boolean searchable, ParameterValueType valueType,
			boolean isSampleParameter, boolean isDatasetParameter, boolean isDatafileParameter,
			boolean facilityAcquired, boolean verified) throws Exception {
		ParameterPK pk = new ParameterPK();
		pk.setName(name);
		pk.setUnits(units);
		Parameter parameter = new Parameter();
		parameter.setParameterPK(pk);
		parameter.setValueType(valueType);
		parameter.setSampleParameter(isSampleParameter);
		parameter.setDatasetParameter(isDatasetParameter);
		parameter.setDatafileParameter(isDatafileParameter);
		parameter.setVerified(verified);
		return parameter;
	}

	protected Investigation createInvestigation(String number, String title, String type, String facilityName) {
		Investigation investigation = new Investigation();
		investigation.setInvNumber(number);
		investigation.setTitle(title);
		investigation.setInvType(type);
		investigation.setFacility(facilityName);
		return investigation;
	}

	protected FacilityUser createFacilityUser(String facilityUserId) {
		FacilityUser facilityUser = new FacilityUser();
		facilityUser.setFacilityUserId(facilityUserId);
		return facilityUser;
	}

	protected Investigator createInvestigator(String fu, Long inv) {
		InvestigatorPK pk = new InvestigatorPK();
		pk.setFacilityUserId(fu);
		pk.setInvestigationId(inv);
		Investigator investigator = new Investigator();
		investigator.setInvestigatorPK(pk);
		return investigator;
	}

	protected Dataset createDataset(Long investigationId, String dsn, String dst) {
		Dataset ds = new Dataset();
		ds.setInvestigationId(investigationId);
		ds.setName(dsn);
		ds.setDatasetType(dst);
		return ds;
	}

	protected DatasetType createDatasetType(String name, String description) {
		DatasetType dst = new DatasetType();
		dst.setName(name);
		dst.setDescription(description);
		return dst;
	}

	protected Datafile createDatafile(Long datasetId, String name) {
		Datafile df = new Datafile();
		df.setDatasetId(datasetId);
		df.setName(name);
		return df;
	}

}

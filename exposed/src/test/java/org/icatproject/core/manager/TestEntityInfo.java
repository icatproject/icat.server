package org.icatproject.core.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.DataCollection;
import org.icatproject.core.entity.DataCollectionDatafile;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.DatasetParameter;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Facility;
import org.icatproject.core.entity.Instrument;
import org.icatproject.core.entity.Investigation;
import org.icatproject.core.entity.InvestigationType;
import org.icatproject.core.entity.InvestigationUser;
import org.icatproject.core.entity.Job;
import org.icatproject.core.entity.Keyword;
import org.icatproject.core.entity.ParameterType;
import org.icatproject.core.entity.Rule;
import org.icatproject.core.entity.User;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;
import org.junit.Test;

public class TestEntityInfo {

	private static EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	@Test(expected = IcatException.class)
	public void testBadname() throws Exception {
		eiHandler.getEntityInfo("Fred");
	}

	@Test
	public void testExportHeaders() throws Exception {
		assertEquals("Facility(daysUntilRelease:0,description:1,fullName:2,name:3,url:4)",
				eiHandler.getExportHeader(Facility.class));
		assertEquals("InvestigationType(description:0,facility(name:1),name:2)",
				eiHandler.getExportHeader(InvestigationType.class));
		assertEquals(
				"Investigation(doi:0,endDate:1,facility(name:2),name:3,releaseDate:4,startDate:5,"
						+ "summary:6,title:7,type(facility(name:8),name:9),visitId:10)",
				eiHandler.getExportHeader(Investigation.class));
		assertEquals(
				"Dataset(complete:0,description:1,doi:2,endDate:3,investigation(facility(name:4),"
						+ "name:5,visitId:6),location:7,name:8,sample(investigation(facility(name:9),"
						+ "name:10,visitId:11),name:12),startDate:13,type(facility(name:14),name:15))",
				eiHandler.getExportHeader(Dataset.class));
		assertEquals("DataCollection(?:0)", eiHandler.getExportHeader(DataCollection.class));
		assertEquals(
				"Job(?:0,application(facility(name:1),name:2,version:3),arguments:4,inputDataCollection(?:5),outputDataCollection(?:6))",
				eiHandler.getExportHeader(Job.class));
		assertEquals(
				"DatasetParameter(dataset(investigation(facility(name:0),name:1,visitId:2),name:3),"
						+ "dateTimeValue:4,error:5,numericValue:6,rangeBottom:7,rangeTop:8,stringValue:9,"
						+ "type(facility(name:10),name:11,units:12))",
				eiHandler.getExportHeader(DatasetParameter.class));
		assertEquals(
				"ParameterType(applicableToDataCollection:0,applicableToDatafile:1,applicableToDataset:2,"
						+ "applicableToInvestigation:3,applicableToSample:4,description:5,enforced:6,facility(name:7),"
						+ "maximumNumericValue:8,minimumNumericValue:9,name:10,units:11,unitsFullName:12,"
						+ "valueType:13,verified:14)",
				eiHandler.getExportHeader(ParameterType.class));
		assertEquals("DataCollection(?:0)", eiHandler.getExportHeader(DataCollection.class));
		assertEquals("Rule(?:0,crudFlags:1,grouping(name:2),what:3)",
				eiHandler.getExportHeader(Rule.class));
		assertEquals(
				"DataCollectionDatafile(dataCollection(?:0),datafile(dataset(investigation(facility(name:1),name:2,visitId:3),name:4),name:5))",
				eiHandler.getExportHeader(DataCollectionDatafile.class));
	}

	@Test
	public void testExportHeaderAll() throws Exception {
		assertEquals("Facility(createId:0,createTime:1,modId:2,"
				+ "modTime:3,daysUntilRelease:4,description:5,fullName:6,name:7,url:8)",
				eiHandler.getExportHeaderAll(Facility.class));
		assertEquals("InvestigationType(createId:0,createTime:1,modId:2,"
				+ "modTime:3,description:4,facility(name:5),name:6)",
				eiHandler.getExportHeaderAll(InvestigationType.class));
		assertEquals("Investigation(createId:0,createTime:1,modId:2,modTime:3,"
				+ "doi:4,endDate:5,facility(name:6),name:7,releaseDate:8,startDate:9,"
				+ "summary:10,title:11,type(facility(name:12),name:13),visitId:14)",
				eiHandler.getExportHeaderAll(Investigation.class));
		assertEquals("Dataset(createId:0,createTime:1,modId:2,modTime:3,"
				+ "complete:4,description:5,doi:6,endDate:7,investigation(facility(name:8),"
				+ "name:9,visitId:10),location:11,name:12,"
				+ "sample(investigation(facility(name:13),name:14,visitId:15),"
				+ "name:16),startDate:17,type(facility(name:18),name:19))",
				eiHandler.getExportHeaderAll(Dataset.class));
		assertEquals("DataCollection(?:0,createId:1,createTime:2,modId:3,modTime:4)",
				eiHandler.getExportHeaderAll(DataCollection.class));
		assertEquals("Job(?:0,createId:1,createTime:2,modId:3,modTime:4,"
				+ "application(facility(name:5),name:6,version:7),arguments:8,"
				+ "inputDataCollection(?:9),outputDataCollection(?:10))",
				eiHandler.getExportHeaderAll(Job.class));
		assertEquals("DatasetParameter(createId:0,createTime:1,modId:2,modTime:3,"
				+ "dataset(investigation(facility(name:4),name:5,visitId:6),name:7),"
				+ "dateTimeValue:8,error:9,numericValue:10,rangeBottom:11,"
				+ "rangeTop:12,stringValue:13,type(facility(name:14),name:15,units:16))",
				eiHandler.getExportHeaderAll(DatasetParameter.class));
		assertEquals("ParameterType(createId:0,createTime:1,modId:2,modTime:3,"
				+ "applicableToDataCollection:4,applicableToDatafile:5,"
				+ "applicableToDataset:6,applicableToInvestigation:7,"
				+ "applicableToSample:8,description:9,enforced:10,facility(name:11),"
				+ "maximumNumericValue:12,minimumNumericValue:13,name:14,units:15,"
				+ "unitsFullName:16,valueType:17,verified:18)",
				eiHandler.getExportHeaderAll(ParameterType.class));
		assertEquals("DataCollection(?:0)", eiHandler.getExportHeader(DataCollection.class));
		assertEquals("Rule(?:0,createId:1,createTime:2,modId:3,modTime:4,"
				+ "crudFlags:5,grouping(name:6),what:7)", eiHandler.getExportHeaderAll(Rule.class));
		assertEquals("DataCollectionDatafile(createId:0,createTime:1,modId:2,modTime:3,"
				+ "dataCollection(?:4),datafile(dataset(investigation(facility(name:5),"
				+ "name:6,visitId:7),name:8),name:9))",
				eiHandler.getExportHeaderAll(DataCollectionDatafile.class));
	}

	@Test
	public void testExportNull() throws Exception {
		assertEquals("NULL", eiHandler.getExportNull(Facility.class));
		assertEquals("NULL,NA", eiHandler.getExportNull(InvestigationType.class));
		assertEquals("NULL,NA,NA", eiHandler.getExportNull(Investigation.class));
		assertEquals("NULL,NA,NA,NA", eiHandler.getExportNull(Dataset.class));
		assertEquals("", eiHandler.getExportNull(DataCollection.class));
		assertEquals("", eiHandler.getExportNull(Job.class));
		assertEquals("NULL,NA,NA,NA,NA,NA,NA", eiHandler.getExportNull(DatasetParameter.class));
		assertEquals("NULL,NA,NA", eiHandler.getExportNull(ParameterType.class));
	}

	@Test
	public void testFields() throws Exception {
		testField(
				"applications,datafileFormats,datasetTypes,daysUntilRelease,description,facilityCycles,"
						+ "fullName,instruments,investigationTypes,investigations,name,parameterTypes,sampleTypes,url",
				Facility.class);
		testField("description,facility,investigations,name", InvestigationType.class);
		testField(
				"datasets,doi,endDate,facility,investigationInstruments,investigationUsers,keywords,"
						+ "name,parameters,publications,releaseDate,samples,shifts,startDate,studyInvestigations,"
						+ "summary,title,type,visitId", Investigation.class);
		testField(
				"complete,dataCollectionDatasets,datafiles,description,doi,endDate,investigation,location,"
						+ "name,parameters,sample,startDate,type", Dataset.class);
		testField(
				"dataCollectionDatafiles,dataCollectionDatasets,jobsAsInput,jobsAsOutput,parameters",
				DataCollection.class);
		testField("application,arguments,inputDataCollection,outputDataCollection", Job.class);
		testField("dataset,dateTimeValue,error,numericValue,rangeBottom,rangeTop,stringValue,type",
				DatasetParameter.class);
		testField(
				"applicableToDataCollection,applicableToDatafile,applicableToDataset,applicableToInvestigation,applicableToSample,dataCollectionParameters,datafileParameters,datasetParameters,description,enforced,facility,investigationParameters,maximumNumericValue,minimumNumericValue,name,permissibleStringValues,sampleParameters,units,unitsFullName,valueType,verified",
				ParameterType.class);
	}

	private void testField(String result, Class<? extends EntityBaseBean> klass) throws Exception {
		String sep = "";
		StringBuilder sb = new StringBuilder();
		for (Field f : eiHandler.getFields(klass)) {
			sb.append(sep + f.getName());
			sep = ",";
		}
		assertEquals(result, sb.toString());
	}

	@Test
	public void testConstraints() throws Exception {
		testConstraint(Investigation.class, "facility", "name", "visitId");
		testConstraint(Dataset.class, "investigation", "name");
		testConstraint(Keyword.class, "name", "investigation");
		testConstraint(InvestigationUser.class, "user", "investigation");
		testConstraint(User.class, "name");
		testConstraint(Job.class);
	}

	private void testConstraint(Class<? extends EntityBaseBean> klass, String... name)
			throws Exception {
		List<Field> result = eiHandler.getConstraintFields(klass);
		if (name.length == 0) {
			assertEquals("One", 0, result.size());
		} else {
			assertFalse("One", 0 == result.size());
			assertEquals(klass.getSimpleName() + " count", name.length, result.size());

			int i = 0;
			for (Field re : result) {
				assertEquals(klass.getSimpleName() + " value " + i, name[i++], re.getName());
			}
		}
	}

	@Test
	public void testFieldByName() throws Exception {
		Map<String, Field> fields = eiHandler.getFieldsByName(EntityInfoHandler
				.getClass("DatafileParameter"));
		assertEquals("protected java.lang.Long org.icatproject.core.entity.EntityBaseBean.id",
				fields.get("id").toString());
		assertEquals(
				"private org.icatproject.core.entity.Datafile org.icatproject.core.entity.DatafileParameter.datafile",
				fields.get("datafile").toString());
		assertEquals("private java.lang.String org.icatproject.core.entity.Parameter.stringValue",
				fields.get("stringValue").toString());
	}

	@Test
	public void testRels() throws Exception {

		testRel(Investigation.class,
				"From Investigation to Keyword by keywords many setInvestigation",
				"From Investigation to Sample by samples many setInvestigation",
				"From Investigation to StudyInvestigation by studyInvestigations many setInvestigation",
				"From Investigation to Shift by shifts many setInvestigation",
				"From Investigation to Dataset by datasets many setInvestigation",
				"From Investigation to Publication by publications many setInvestigation",
				"From Investigation to InvestigationUser by investigationUsers many setInvestigation",
				"From Investigation to InvestigationInstrument by investigationInstruments many setInvestigation",
				"From Investigation to InvestigationType by type one",
				"From Investigation to Facility by facility one",
				"From Investigation to InvestigationParameter by parameters many setInvestigation");

		testRel(Dataset.class,
				"From Dataset to DataCollectionDataset by dataCollectionDatasets many setDataset",
				"From Dataset to DatasetParameter by parameters many setDataset",
				"From Dataset to Investigation by investigation one",
				"From Dataset to Datafile by datafiles many setDataset",
				"From Dataset to DatasetType by type one", "From Dataset to Sample by sample one");

		testRel(Keyword.class, "From Keyword to Investigation by investigation one");

		testRel(InvestigationUser.class,
				"From InvestigationUser to Investigation by investigation one",
				"From InvestigationUser to User by user one");

		testRel(User.class, "From User to InvestigationUser by investigationUsers many setUser",
				"From User to UserGroup by userGroups many setUser",
				"From User to InstrumentScientist by instrumentScientists many setUser",
				"From User to Study by studies many setUser");

		testRel(Job.class, "From Job to DataCollection by inputDataCollection one",
				"From Job to Application by application one",
				"From Job to DataCollection by outputDataCollection one");

		testRel(Instrument.class,
				"From Instrument to Facility by facility one",
				"From Instrument to InstrumentScientist by instrumentScientists many setInstrument",
				"From Instrument to InvestigationInstrument by investigationInstruments many setInstrument");
	}

	private void testRel(Class<? extends EntityBaseBean> klass, String... rels) throws Exception {
		Set<Relationship> results = eiHandler.getRelatedEntities(klass);
		Set<String> rStrings = new HashSet<String>();
		for (Relationship rel : results) {
			rStrings.add(rel.toString());
		}
		System.out.println(results);
		assertEquals(klass.getSimpleName() + " count", rels.length, results.size());
		for (String rel : rels) {
			assertTrue(klass.getSimpleName() + " value " + rel, rStrings.contains(rel));
		}
	}

	@Test
	public void testOnes() throws Exception {

		testOne(Investigation.class, "InvestigationType", "Facility");

		testOne(Dataset.class, "Investigation", "DatasetType", "Sample");

		testOne(Keyword.class, "Investigation");

		testOne(InvestigationUser.class, "Investigation", "User");

		testOne(User.class);

		testOne(Job.class, "Application", "DataCollection", "DataCollection");
	}

	private void testOne(Class<? extends EntityBaseBean> klass, String... rels) throws Exception {
		Set<Relationship> results = eiHandler.getOnes(klass);
		Set<String> rStrings = new HashSet<String>();
		for (Relationship rel : results) {
			rStrings.add(rel.getDestinationBean().getSimpleName());
		}
		// System.out.println(results);
		assertEquals(klass.getSimpleName() + " count", rels.length, results.size());
		for (String rel : rels) {
			assertTrue(klass.getSimpleName() + " value " + rel, rStrings.contains(rel));
		}
	}

	@Test
	public void notNullableFields() throws Exception {
		testNNF(Investigation.class, "name", "title", "facility", "visitId", "type");
		testNNF(Dataset.class, "type", "name", "complete", "investigation");
		testNNF(Keyword.class, "name", "investigation");
		testNNF(InvestigationUser.class, "investigation", "user");
		testNNF(User.class, "name");
		testNNF(ParameterType.class, "valueType", "name", "facility", "units");
		testNNF(Job.class, "application");
	}

	private void testNNF(Class<? extends EntityBaseBean> klass, String... nnfs) throws Exception {
		List<Field> results = eiHandler.getNotNullableFields(klass);
		// System.out.println(results);
		Set<String> rStrings = new HashSet<String>();
		for (Field field : results) {
			rStrings.add(field.getName());
		}
		assertEquals(klass.getSimpleName() + " count", nnfs.length, results.size());

		for (String nnf : nnfs) {
			assertTrue(klass.getSimpleName() + " value " + nnf, rStrings.contains(nnf));
		}
	}

	@Test
	public void stringFields() throws Exception {
		testSF(Investigation.class, "visitId 255", "summary 4000", "name 255", "title 255",
				"doi 255");
		testSF(Dataset.class, "name 255", "description 255", "location 255", "doi 255");
		testSF(Keyword.class, "name 255");
		testSF(InvestigationUser.class, "role 255");
		testSF(User.class, "name 255", "fullName 255");
		testSF(ParameterType.class, "description 255", "unitsFullName 255", "units 255", "name 255");
		testSF(Job.class, "arguments 255");

	}

	private void testSF(Class<? extends EntityBaseBean> klass, String... sfs) throws Exception {
		Map<Field, Integer> results = eiHandler.getStringFields(klass);
		Set<String> rStrings = new HashSet<String>();
		for (Entry<Field, Integer> entry : results.entrySet()) {
			rStrings.add(entry.getKey().getName() + " " + entry.getValue());
		}
		assertEquals(klass.getSimpleName() + " count", sfs.length, results.size());

		for (String sf : sfs) {
			assertTrue(klass.getSimpleName() + " value " + sf, rStrings.contains(sf));
		}
	}

	@Test
	public void getters() throws Exception {
		testGetters(Investigation.class, 20);
		testGetters(Dataset.class, 14);
		testGetters(Keyword.class, 3);
		testGetters(InvestigationUser.class, 4);
		testGetters(User.class, 7);
		testGetters(ParameterType.class, 22);
		testGetters(Job.class, 5);
	}

	private void testGetters(Class<? extends EntityBaseBean> klass, int count) throws Exception {
		Map<Field, Method> results = eiHandler.getGetters(klass);
		assertEquals(klass.getSimpleName() + " count", count, results.size());
		for (Entry<Field, Method> entry : results.entrySet()) {
			String cap = entry.getKey().getName();
			cap = Character.toUpperCase(cap.charAt(0)) + cap.substring(1);
			String m = entry.getValue().getName();
			assertTrue(klass.getSimpleName() + " value ",
					m.equals("get" + cap) || m.equals("is" + cap));
		}

	}

	@Test
	public void setters() throws Exception {
		testSetters(Investigation.class, 20);
		testSetters(Dataset.class, 14);
		testSetters(Keyword.class, 3);
		testSetters(InvestigationUser.class, 4);
		testSetters(User.class, 7);
		testSetters(ParameterType.class, 22);
		testSetters(Job.class, 5);
	}

	@Test
	public void updaters() throws Exception {
		testSettersForUpdate(Investigation.class, 10);
		testSettersForUpdate(Dataset.class, 10);
		testSettersForUpdate(Keyword.class, 2);
		testSettersForUpdate(InvestigationUser.class, 3);
		testSettersForUpdate(User.class, 2);
		testSettersForUpdate(ParameterType.class, 15);
		testSettersForUpdate(Job.class, 4);
	}

	private void testSettersForUpdate(Class<? extends EntityBaseBean> klass, int count)
			throws Exception {
		Map<Field, Method> results = eiHandler.getSettersForUpdate(klass);
		assertEquals(klass.getSimpleName() + " count", count, results.size());
		for (Entry<Field, Method> entry : results.entrySet()) {
			String cap = entry.getKey().getName();
			cap = Character.toUpperCase(cap.charAt(0)) + cap.substring(1);
			String m = entry.getValue().getName();
			assertTrue(klass.getSimpleName() + " value ", m.equals("set" + cap));
		}
	}

	private void testSetters(Class<? extends EntityBaseBean> klass, int count) throws Exception {
		Map<Field, Method> results = eiHandler.getSetters(klass);
		assertEquals(klass.getSimpleName() + " count", count, results.size());
		for (Entry<Field, Method> entry : results.entrySet()) {
			String cap = entry.getKey().getName();
			cap = Character.toUpperCase(cap.charAt(0)) + cap.substring(1);
			String m = entry.getValue().getName();
			assertTrue(klass.getSimpleName() + " value ", m.equals("set" + cap));
		}
	}

}

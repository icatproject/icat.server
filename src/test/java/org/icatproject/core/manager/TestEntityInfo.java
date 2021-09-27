package org.icatproject.core.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.icatproject.core.Constants;
import org.icatproject.core.IcatException;
import org.icatproject.core.entity.DataCollection;
import org.icatproject.core.entity.DataCollectionDatafile;
import org.icatproject.core.entity.Datafile;
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
import org.icatproject.core.entity.Study;
import org.icatproject.core.manager.EntityInfoHandler.Relationship;
import org.junit.Test;

public class TestEntityInfo {

	private static EntityInfoHandler eiHandler = EntityInfoHandler.getInstance();

	@Test(expected = IcatException.class)
	public void testBadname() throws Exception {
		eiHandler.getEntityInfo("Fred");
	}

	@Test
	public void testHasLuceneDoc() throws Exception {
		Set<String> docdbeans = new HashSet<>(Arrays.asList("Investigation", "Dataset", "Datafile",
				"InvestigationParameter", "DatasetParameter", "DatafileParameter", "InvestigationUser", "Sample"));
		for (String beanName : EntityInfoHandler.getEntityNamesList()) {
			@SuppressWarnings("unchecked")
			Class<? extends EntityBaseBean> bean = (Class<? extends EntityBaseBean>) Class
					.forName(Constants.ENTITY_PREFIX + beanName);
			if (docdbeans.contains(beanName)) {
				assertTrue(eiHandler.hasLuceneDoc(bean));
			} else {
				assertFalse(eiHandler.hasLuceneDoc(bean));
			}
		}
	}

	@Test
	public void testExportHeaders() throws Exception {
		assertEquals("Facility(daysUntilRelease:0,description:1,fullName:2,name:3,url:4)",
				eiHandler.getExportHeader(Facility.class));
		assertEquals("InvestigationType(description:0,facility(name:1),name:2)",
				eiHandler.getExportHeader(InvestigationType.class));
		assertEquals(
				"Investigation(doi:0,endDate:1,facility(name:2),fileCount:3,name:4,releaseDate:5,size:6,"
						+ "startDate:7,summary:8,title:9,type(facility(name:10),name:11),visitId:12)",
				eiHandler.getExportHeader(Investigation.class));
		assertEquals(
				"Dataset(complete:0,description:1,doi:2,endDate:3,fileCount:4,investigation(facility(name:5),"
						+ "name:6,visitId:7),location:8,name:9,sample(investigation(facility(name:10),"
						+ "name:11,visitId:12),name:13),size:14,startDate:15,type(facility(name:16),name:17))",
				eiHandler.getExportHeader(Dataset.class));
		assertEquals("DataCollection(?:0,doi:1)", eiHandler.getExportHeader(DataCollection.class));
		assertEquals(
				"Job(?:0,application(facility(name:1),name:2,version:3),arguments:4,inputDataCollection(?:5),outputDataCollection(?:6))",
				eiHandler.getExportHeader(Job.class));
		assertEquals( "Study(?:0,description:1,endDate:2,name:3,pid:4,startDate:5,status:6,user(name:7))",
				eiHandler.getExportHeader(Study.class));
		assertEquals(
				"DatasetParameter(dataset(investigation(facility(name:0),name:1,visitId:2),name:3),"
						+ "dateTimeValue:4,error:5,numericValue:6,rangeBottom:7,rangeTop:8,stringValue:9,"
						+ "type(facility(name:10),name:11,units:12))",
				eiHandler.getExportHeader(DatasetParameter.class));
		assertEquals("ParameterType(applicableToDataCollection:0,applicableToDatafile:1,applicableToDataset:2,"
				+ "applicableToInvestigation:3,applicableToSample:4,description:5,enforced:6,facility(name:7),"
				+ "maximumNumericValue:8,minimumNumericValue:9,name:10,pid:11,units:12,unitsFullName:13,"
				+ "valueType:14,verified:15)", eiHandler.getExportHeader(ParameterType.class));
		assertEquals("DataCollection(?:0,doi:1)", eiHandler.getExportHeader(DataCollection.class));
		assertEquals("Rule(?:0,crudFlags:1,grouping(name:2),what:3)", eiHandler.getExportHeader(Rule.class));
		assertEquals(
				"DataCollectionDatafile(dataCollection(?:0),datafile(dataset(investigation(facility(name:1),name:2,visitId:3),name:4),name:5))",
				eiHandler.getExportHeader(DataCollectionDatafile.class));
	}

	@Test
	public void testExportHeaderAll() throws Exception {
		assertEquals(
				"Facility(createId:0,createTime:1,modId:2,"
						+ "modTime:3,daysUntilRelease:4,description:5,fullName:6,name:7,url:8)",
				eiHandler.getExportHeaderAll(Facility.class));
		assertEquals(
				"InvestigationType(createId:0,createTime:1,modId:2,"
						+ "modTime:3,description:4,facility(name:5),name:6)",
				eiHandler.getExportHeaderAll(InvestigationType.class));
		assertEquals(
				"Investigation(createId:0,createTime:1,modId:2,modTime:3,"
						+ "doi:4,endDate:5,facility(name:6),fileCount:7,name:8,releaseDate:9,size:10,"
						+ "startDate:11,summary:12,title:13,type(facility(name:14),name:15),visitId:16)",
				eiHandler.getExportHeaderAll(Investigation.class));
		assertEquals(
				"Dataset(createId:0,createTime:1,modId:2,modTime:3,"
						+ "complete:4,description:5,doi:6,endDate:7,fileCount:8,"
						+ "investigation(facility(name:9),name:10,visitId:11),location:12,name:13,"
						+ "sample(investigation(facility(name:14),name:15,visitId:16),"
						+ "name:17),size:18,startDate:19,type(facility(name:20),name:21))",
				eiHandler.getExportHeaderAll(Dataset.class));
		assertEquals("DataCollection(?:0,createId:1,createTime:2,modId:3,modTime:4,doi:5)",
				eiHandler.getExportHeaderAll(DataCollection.class));
		assertEquals(
				"Job(?:0,createId:1,createTime:2,modId:3,modTime:4,"
						+ "application(facility(name:5),name:6,version:7),arguments:8,"
						+ "inputDataCollection(?:9),outputDataCollection(?:10))",
				eiHandler.getExportHeaderAll(Job.class));
		assertEquals( "Study(?:0,description:1,endDate:2,name:3,pid:4,startDate:5,status:6,user(name:7))",
				eiHandler.getExportHeader(Study.class));
		assertEquals(
				"DatasetParameter(createId:0,createTime:1,modId:2,modTime:3,"
						+ "dataset(investigation(facility(name:4),name:5,visitId:6),name:7),"
						+ "dateTimeValue:8,error:9,numericValue:10,rangeBottom:11,"
						+ "rangeTop:12,stringValue:13,type(facility(name:14),name:15,units:16))",
				eiHandler.getExportHeaderAll(DatasetParameter.class));
		assertEquals(
				"ParameterType(createId:0,createTime:1,modId:2,modTime:3,"
						+ "applicableToDataCollection:4,applicableToDatafile:5,"
						+ "applicableToDataset:6,applicableToInvestigation:7,"
						+ "applicableToSample:8,description:9,enforced:10,facility(name:11),"
						+ "maximumNumericValue:12,minimumNumericValue:13,name:14,pid:15,"
						+ "units:16,unitsFullName:17,valueType:18,verified:19)",
				eiHandler.getExportHeaderAll(ParameterType.class));
		assertEquals("DataCollection(?:0,doi:1)", eiHandler.getExportHeader(DataCollection.class));
		assertEquals("Rule(?:0,createId:1,createTime:2,modId:3,modTime:4," + "crudFlags:5,grouping(name:6),what:7)",
				eiHandler.getExportHeaderAll(Rule.class));
		assertEquals(
				"DataCollectionDatafile(createId:0,createTime:1,modId:2,modTime:3,"
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
				"applications,dataPublications,datafileFormats,datasetTypes,daysUntilRelease,description,facilityCycles,"
						+ "fullName,instruments,investigationTypes,investigations,name,parameterTypes,sampleTypes,url",
				Facility.class);
		testField("description,facility,investigations,name", InvestigationType.class);
		testField(
				"datasets,doi,endDate,facility,fileCount,fundingReferences,investigationGroups,investigationInstruments,"
						+ "investigationUsers,keywords,name,parameters,publications,releaseDate,samples,shifts,"
						+ "size,startDate,studyInvestigations,summary,title,type,visitId",
				Investigation.class);
		testField("complete,dataCollectionDatasets,datafiles,datasetInstruments,datasetTechniques,description,"
				+ "doi,endDate,fileCount,investigation,location,name,parameters,sample,size,startDate,type", Dataset.class);
		testField("dataCollectionDatafiles,dataCollectionDatasets,dataPublications,doi,jobsAsInput,jobsAsOutput,parameters",
				DataCollection.class);
		testField("application,arguments,inputDataCollection,outputDataCollection", Job.class);
		testField( "description,endDate,name,pid,startDate,status,studyInvestigations,user",Study.class);
		testField("dataset,dateTimeValue,error,numericValue,rangeBottom,rangeTop,stringValue,type",
				DatasetParameter.class);
		testField(
				"applicableToDataCollection,applicableToDatafile,applicableToDataset,applicableToInvestigation,applicableToSample,dataCollectionParameters,datafileParameters,datasetParameters,description,enforced,facility,investigationParameters,maximumNumericValue,minimumNumericValue,name,permissibleStringValues,pid,sampleParameters,units,unitsFullName,valueType,verified",
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
		testConstraint(InvestigationUser.class, "user", "investigation", "role");
		testConstraint(User.class, "name");
		testConstraint(Job.class);
	}

	private void testConstraint(Class<? extends EntityBaseBean> klass, String... name) throws Exception {
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
		Map<String, Field> fields = eiHandler.getFieldsByName(EntityInfoHandler.getClass("DatafileParameter"));
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

		testRel(Investigation.class, "From Investigation to Keyword by keywords many setInvestigation",
				"From Investigation to Sample by samples many setInvestigation",
				"From Investigation to StudyInvestigation by studyInvestigations many setInvestigation",
				"From Investigation to Shift by shifts many setInvestigation",
				"From Investigation to Dataset by datasets many setInvestigation",
				"From Investigation to Publication by publications many setInvestigation",
				"From Investigation to InvestigationFunding by fundingReferences many setInvestigation",
				"From Investigation to InvestigationUser by investigationUsers many setInvestigation",
				"From Investigation to InvestigationGroup by investigationGroups many setInvestigation",
				"From Investigation to InvestigationInstrument by investigationInstruments many setInvestigation",
				"From Investigation to InvestigationType by type one", "From Investigation to Facility by facility one",
				"From Investigation to InvestigationParameter by parameters many setInvestigation");

		testRel(Dataset.class, "From Dataset to DataCollectionDataset by dataCollectionDatasets many setDataset",
				"From Dataset to DatasetParameter by parameters many setDataset",
				"From Dataset to DatasetTechnique by datasetTechniques many setDataset",
				"From Dataset to Investigation by investigation one",
				"From Dataset to Datafile by datafiles many setDataset",
				"From Dataset to DatasetInstrument by datasetInstruments many setDataset",
				"From Dataset to DatasetType by type one", "From Dataset to Sample by sample one");

		testRel(Keyword.class, "From Keyword to Investigation by investigation one");

		testRel(InvestigationUser.class, "From InvestigationUser to Investigation by investigation one",
				"From InvestigationUser to User by user one");

		testRel(User.class, "From User to InvestigationUser by investigationUsers many setUser",
				"From User to UserGroup by userGroups many setUser",
				"From User to InstrumentScientist by instrumentScientists many setUser",
				"From User to Study by studies many setUser");

		testRel(Job.class, "From Job to DataCollection by inputDataCollection one",
				"From Job to Application by application one", "From Job to DataCollection by outputDataCollection one");

		testRel(Instrument.class, "From Instrument to Facility by facility one",
				"From Instrument to InstrumentScientist by instrumentScientists many setInstrument",
				"From Instrument to InvestigationInstrument by investigationInstruments many setInstrument",
				"From Instrument to DatasetInstrument by datasetInstruments many setInstrument",
				"From Instrument to Shift by shifts many setInstrument");
	}

	private void testRel(Class<? extends EntityBaseBean> klass, String... rels) throws Exception {
		Set<Relationship> results = eiHandler.getRelatedEntities(klass);
		Set<String> rStrings = new HashSet<String>();
		for (Relationship rel : results) {
			rStrings.add(rel.toString());
		}
		assertEquals(klass.getSimpleName() + " count", rels.length, results.size());
		for (String rel : rels) {
			assertTrue(klass.getSimpleName() + " value " + rel, rStrings.contains(rel));
		}
		Set<Entry<String, Relationship>> map = eiHandler.getRelationshipsByName(klass).entrySet();
		assertEquals(rels.length, map.size());
		for (Entry<String, Relationship> e : map) {
			Relationship rel = e.getValue();
			assertEquals(rel.getField().getName(), e.getKey());
			assertTrue(klass.getSimpleName() + " value " + rel, rStrings.contains(rel.toString()));
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

		testOne(Study.class, "User");
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
		testNNF(InvestigationUser.class, "investigation", "user", "role");
		testNNF(User.class, "name");
		testNNF(ParameterType.class, "valueType", "name", "facility", "units");
		testNNF(Job.class, "application");
		testNNF(Study.class, "name");
	}

	private void testNNF(Class<? extends EntityBaseBean> klass, String... nnfs) throws Exception {
		List<Field> results = eiHandler.getNotNullableFields(klass);
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
		testSF(Investigation.class, "visitId 255", "summary 4000", "name 255", "title 255", "doi 255");
		testSF(Dataset.class, "name 255", "description 255", "location 255", "doi 255");
		testSF(Keyword.class, "name 255");
		testSF(InvestigationUser.class, "role 255");
		testSF(User.class, "name 255", "fullName 255", "givenName 255", "familyName 255", "affiliation 255", "email 255", "orcidId 255");
		testSF(ParameterType.class, "pid 255", "description 255", "unitsFullName 255", "units 255", "name 255");
		testSF(Job.class, "arguments 255");
		testSF(Study.class, "name 255", "description 4000", "pid 255");

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
		testGetters(Investigation.class, 28);
		testGetters(Dataset.class, 22);
		testGetters(Keyword.class, 7);
		testGetters(InvestigationUser.class, 8);
		testGetters(User.class, 16);
		testGetters(ParameterType.class, 27);
		testGetters(Job.class, 9);
		testGetters(Study.class, 13);
	}

	@Test
	public void setters() throws Exception {
		testSetters(Investigation.class, 24);
		testSetters(Dataset.class, 18);
		testSetters(Keyword.class, 3);
		testSetters(InvestigationUser.class, 4);
		testSetters(User.class, 12);
		testSetters(ParameterType.class, 23);
		testSetters(Job.class, 5);
		testSetters(Study.class, 9);
	}

	@Test
	public void updaters() throws Exception {
		testSettersForUpdate(Investigation.class, 12);
		testSettersForUpdate(Dataset.class, 12);
		testSettersForUpdate(Keyword.class, 2);
		testSettersForUpdate(InvestigationUser.class, 3);
		testSettersForUpdate(User.class, 7);
		testSettersForUpdate(ParameterType.class, 16);
		testSettersForUpdate(Job.class, 4);
		testSettersForUpdate(Facility.class, 5);
		testSettersForUpdate(InvestigationType.class, 3);
		testSettersForUpdate(Study.class, 7);
	}

	@Test
	public void relInKey() throws Exception {
		testRelInkey(Dataset.class, "investigation");
		testRelInkey(Datafile.class, "dataset");
		testRelInkey(Facility.class);
		testRelInkey(DataCollectionDatafile.class, "dataCollection", "datafile");
	}

	private void testRelInkey(Class<? extends EntityBaseBean> klass, String... fieldNames) throws Exception {
		Set<Field> results = eiHandler.getRelInKey(klass);
		Set<String> rStrings = new HashSet<String>();
		for (Field field : results) {
			rStrings.add(field.getName());
		}
		assertEquals(klass.getSimpleName() + " count", fieldNames.length, results.size());

		for (String fn : fieldNames) {
			assertTrue(klass.getSimpleName() + " value " + fn, rStrings.contains(fn));
		}
	}

	private void testGetters(Class<? extends EntityBaseBean> klass, int count) throws Exception {
		Map<Field, Method> results = eiHandler.getGetters(klass);
		assertEquals(klass.getSimpleName() + " count", count, results.size());
		for (Entry<Field, Method> entry : results.entrySet()) {
			String cap = entry.getKey().getName();
			cap = Character.toUpperCase(cap.charAt(0)) + cap.substring(1);
			String m = entry.getValue().getName();
			assertTrue(klass.getSimpleName() + " value ", m.equals("get" + cap) || m.equals("is" + cap));
		}
	}

	private void testSettersForUpdate(Class<? extends EntityBaseBean> klass, int count) throws Exception {
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

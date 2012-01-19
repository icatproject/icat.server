package uk.icat3.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import uk.icat3.entity.Dataset;
import uk.icat3.entity.EntityBaseBean;
import uk.icat3.entity.FacilityUser;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.Job;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.Parameter;
import uk.icat3.entity.Topic;
import uk.icat3.entity.TopicList;
import uk.icat3.security.EntityInfoHandler.KeyType;
import uk.icat3.security.EntityInfoHandler.Relationship;
import uk.icat3.util.BaseTestTransaction;

public class TestEntityInfo extends BaseTestTransaction {

	private static EntityInfoHandler pkHandler = EntityInfoHandler.getInstance();

	@Test
	public void testSimplePKS() throws Exception {
		testPKS(Investigation.class, "getId");
		testPKS(Dataset.class, "getId");
		testPKS(Keyword.class, "getKeywordPK", "getInvestigationId", "getName");
		testPKS(TopicList.class, "getTopicListPK", "getInvestigationId", "getTopicId");
		testPKS(Investigator.class, "getInvestigatorPK", "getInvestigationId", "getFacilityUserId");
		testPKS(FacilityUser.class, "getFacilityUserId");
		testPKS(Topic.class, "getId");
		testPKS(Job.class, "getId");
	}

	private void testPKS(Class<? extends EntityBaseBean> klass, String... pkname) throws Exception {
		List<String> results = pkHandler.getKeysFor(klass);
		assertEquals(klass.getSimpleName() + " count", pkname.length, results.size());

		int i = 0;
		for (String re : results) {
			assertEquals(klass.getSimpleName() + " value " + i, pkname[i++], re);
		}
	}

	@Test
	public void testRels() throws Exception {

		testRel(Investigation.class, "Keyword by keywordCollection many", "Sample by sampleCollection many",
				"TopicList by topicListCollection many", "StudyInvestigation by studyInvestigationCollection many",
				"Shift by shiftCollection many", "Dataset by datasetCollection many",
				"Publication by publicationCollection many", "Investigator by investigatorCollection many",
				"FacilityCycle by facilityCycle one");

		testRel(Dataset.class, "InputDataset by inputDatasets many",
				"DatasetParameter by datasetParameterCollection many", "Investigation by investigation one",
				"Datafile by datafileCollection many", "OutputDataset by outputDatasets many");

		testRel(Keyword.class, "Investigation by investigation one");

		testRel(TopicList.class, "Investigation by investigation one", "Topic by topic one");

		testRel(Investigator.class, "Investigation by investigation one", "FacilityUser by facilityUser one");

		testRel(FacilityUser.class, "Investigator by investigatorCollection many");

		testRel(Topic.class, "TopicList by topicListCollection many");

		testRel(Job.class, "InputDataset by inputDatasets many", "InputDatafile by inputDatafiles many",
				"OutputDatafile by outputDatafiles many", "Application by application one",
				"OutputDataset by outputDatasets many");
	}

	private void testRel(Class<? extends EntityBaseBean> klass, String... rels) throws Exception {
		Set<Relationship> results = pkHandler.getRelatedEntities(klass);
		Set<String> rStrings = new HashSet<String>();
		for (Relationship rel : results) {
			rStrings.add(rel.toString());
		}
		assertEquals(klass.getSimpleName() + " count", rels.length, results.size());

		for (String rel : rels) {
			assertTrue(klass.getSimpleName() + " value " + rel, rStrings.contains(rel));
		}
	}

	@Test
	public void notNullableFields() throws Exception {
		testNNF(Investigation.class, "invNumber", "title", "facility", "invType");
		testNNF(Dataset.class, "datasetType", "name");
		testNNF(Keyword.class);
		testNNF(TopicList.class);
		testNNF(Investigator.class);
		testNNF(FacilityUser.class, "facilityUserId");
		testNNF(Topic.class, "id");
		testNNF(Parameter.class, "valueType");
		testNNF(Job.class, "application");
	}

	private void testNNF(Class<? extends EntityBaseBean> klass, String... nnfs) throws Exception {
		List<Field> results = pkHandler.getNotNullableFields(klass);
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
		testSF(Investigation.class, "instrument 255", "visitId 255", "invParamName 255", "invAbstract 4000",
				"invNumber 255", "title 255", "prevInvNumber 255", "invParamValue 255", "bcatInvStr 255",
				"invType 255", "facility 255");
		testSF(Dataset.class, "name 255", "description 255", "location 255", "datasetType 255", "datasetStatus 255");
		testSF(Keyword.class);
		testSF(TopicList.class);
		testSF(Investigator.class, "role 255");
		testSF(FacilityUser.class, "title 255", "lastName 255", "middleName 255", "initials 255", "facilityUserId 255",
				"federalId 255", "firstName 255");
		testSF(Topic.class, "name 255");
		testSF(Parameter.class, "description 255", "nonNumericValueFormat 255", "unitsLongVersion 255");
		testSF(Job.class);

	}

	private void testSF(Class<? extends EntityBaseBean> klass, String... sfs) throws Exception {
		Map<Field, Integer> results = pkHandler.getStringFields(klass);
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
		testGetters(Investigation.class, 25);
		testGetters(Dataset.class, 15);
		testGetters(Keyword.class, 2);
		testGetters(TopicList.class, 3);
		testGetters(Investigator.class, 4);
		testGetters(FacilityUser.class, 8);
		testGetters(Topic.class, 5);
		testGetters(Parameter.class, 12);
		testGetters(Job.class, 6);
	}

	private void testGetters(Class<? extends EntityBaseBean> klass, int count) throws Exception {
		Map<Field, Method> results = pkHandler.getGetters(klass);
		assertEquals(klass.getSimpleName() + " count", count, results.size());
		for (Entry<Field, Method> entry : results.entrySet()) {
			String cap = entry.getKey().getName();
			cap = Character.toUpperCase(cap.charAt(0)) + cap.substring(1);
			String m = entry.getValue().getName();
			assertTrue(klass.getSimpleName() + " value ", m.equals("get" + cap) || m.equals("is" + cap));
		}

	}

	@Test
	public void setters() throws Exception {
		testSetters(Investigation.class, 16);
		testSetters(Dataset.class, 9);
		testSetters(Keyword.class, 0);
		testSetters(TopicList.class, 1);
		testSetters(Investigator.class, 1);
		testSetters(FacilityUser.class, 6);
		testSetters(Topic.class, 3);
		testSetters(Parameter.class, 8);
		testSetters(Job.class, 1);
	}

	private void testSetters(Class<? extends EntityBaseBean> klass, int count) throws Exception {
		Map<Field, Method> results = pkHandler.getSetters(klass);
		assertEquals(klass.getSimpleName() + " count", count, results.size());
		for (Entry<Field, Method> entry : results.entrySet()) {
			String cap = entry.getKey().getName();
			cap = Character.toUpperCase(cap.charAt(0)) + cap.substring(1);
			String m = entry.getValue().getName();
			assertTrue(klass.getSimpleName() + " value ", m.equals("set" + cap));
		}
	}

	@Test
	public void keytype() throws Exception {
		testKeytype(Investigation.class, EntityInfoHandler.KeyType.GENERATED);
		testKeytype(Dataset.class, EntityInfoHandler.KeyType.GENERATED);
		testKeytype(Keyword.class, EntityInfoHandler.KeyType.COMPOUND);
		testKeytype(TopicList.class, EntityInfoHandler.KeyType.COMPOUND);
		testKeytype(Investigator.class, EntityInfoHandler.KeyType.COMPOUND);
		testKeytype(FacilityUser.class, EntityInfoHandler.KeyType.SIMPLE);
		testKeytype(Topic.class, EntityInfoHandler.KeyType.SIMPLE);
		testKeytype(Parameter.class, EntityInfoHandler.KeyType.COMPOUND);
		testKeytype(Job.class, EntityInfoHandler.KeyType.GENERATED);
	}

	private void testKeytype(Class<? extends EntityBaseBean> klass, KeyType keyType) throws Exception {
		assertEquals(klass.getSimpleName() + " keyType", keyType, pkHandler.getKeytype(klass));
	}

}

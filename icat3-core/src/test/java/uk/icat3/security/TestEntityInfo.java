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
import uk.icat3.entity.User;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Investigator;
import uk.icat3.entity.Job;
import uk.icat3.entity.Keyword;
import uk.icat3.entity.ParameterType;
import uk.icat3.entity.Topic;
import uk.icat3.entity.TopicInvestigation;
import uk.icat3.security.EntityInfoHandler.KeyType;
import uk.icat3.security.EntityInfoHandler.Relationship;
import uk.icat3.util.BaseTestTransaction;

public class TestEntityInfo extends BaseTestTransaction {

	private static EntityInfoHandler pkHandler = EntityInfoHandler.getInstance();

	@Test
	public void testSimplePKS() throws Exception {
		testPKS(Investigation.class, "getId");
		testPKS(Dataset.class, "getId");
		testPKS(Keyword.class, "getId");
		testPKS(TopicInvestigation.class, "getId");
		testPKS(Investigator.class, "getId");
		testPKS(User.class, "getName");
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

		testRel(Investigation.class, "Instrument by instrument one", "Keyword by keywords many",
				"Sample by samples many", "TopicInvestigation by topicInvestigations many",
				"StudyInvestigation by studyInvestigations many", "Shift by shifts many", "Dataset by datasets many",
				"Publication by publications many", "Investigator by investigators many",
				"FacilityCycle by facilityCycle one", "InvestigationType by type one",
				"Facility by facility one", "InvestigationParameter by investigationParameters many");

		testRel(Dataset.class, "InputDataset by inputDatasets many", "DatasetParameter by datasetParameters many",
				"Investigation by investigation one", "Datafile by datafiles many",
				"OutputDataset by outputDatasets many", "DatasetStatus by status one",
				"DatasetType by type one", "Sample by sample one");

		testRel(Keyword.class, "Investigation by investigation one");

		testRel(TopicInvestigation.class, "Investigation by investigation one", "Topic by topic one");

		testRel(Investigator.class, "Investigation by investigation one", "User by user one");

		testRel(User.class, "Investigator by investigatorCollection many", "UserGroup by userGroups many");

		testRel(Topic.class, "TopicInvestigation by topicInvestigations many");

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
		System.out.println(results);
		assertEquals(klass.getSimpleName() + " count", rels.length, results.size());
		for (String rel : rels) {
			assertTrue(klass.getSimpleName() + " value " + rel, rStrings.contains(rel));
		}
	}

	@Test
	public void notNullableFields() throws Exception {
		testNNF(Investigation.class, "name", "title", "facility", "name");
		testNNF(Dataset.class, "type", "name");
		testNNF(Keyword.class, "name", "investigation");
		testNNF(TopicInvestigation.class, "topic", "investigation");
		testNNF(Investigator.class);
		testNNF(User.class);
		testNNF(Topic.class);
		testNNF(ParameterType.class, "valueType", "name", "units");
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
		testSF(Investigation.class, "visitId 255", "summary 4000", "name 255", "title 255");
		testSF(Dataset.class, "name 255", "description 255", "location 255");
		testSF(Keyword.class, "name 255");
		testSF(TopicInvestigation.class);
		testSF(Investigator.class, "role 255");
		testSF(User.class, "title 255", "lastName 255", "middleName 255", "initials 255", "name 255", "firstName 255");
		testSF(Topic.class, "name 255");
		testSF(ParameterType.class, "description 255", "unitsFullName 255", "units 255", "name 255");
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
		testGetters(Investigation.class, 21);
		testGetters(Dataset.class, 14);
		testGetters(Keyword.class, 3);
		testGetters(TopicInvestigation.class, 3);
		testGetters(Investigator.class, 4);
		testGetters(User.class, 8);
		testGetters(Topic.class, 3);
		testGetters(ParameterType.class, 15);
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
		testSetters(Investigation.class, 11);
		testSetters(Dataset.class, 9);
		testSetters(Keyword.class, 2);
		testSetters(TopicInvestigation.class, 2);
		testSetters(Investigator.class, 3);
		testSetters(User.class, 5);
		testSetters(Topic.class, 1);
		testSetters(ParameterType.class, 10);
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
		testKeytype(Keyword.class, EntityInfoHandler.KeyType.GENERATED);
		testKeytype(TopicInvestigation.class, EntityInfoHandler.KeyType.GENERATED);
		testKeytype(Investigator.class, EntityInfoHandler.KeyType.GENERATED);
		testKeytype(User.class, EntityInfoHandler.KeyType.SIMPLE);
		testKeytype(Topic.class, EntityInfoHandler.KeyType.GENERATED);
		testKeytype(ParameterType.class, EntityInfoHandler.KeyType.GENERATED);
		testKeytype(Job.class, EntityInfoHandler.KeyType.GENERATED);
	}

	private void testKeytype(Class<? extends EntityBaseBean> klass, KeyType keyType) throws Exception {
		assertEquals(klass.getSimpleName() + " keyType", keyType, pkHandler.getKeytype(klass));
	}

}

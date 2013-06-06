package org.icatproject.core.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.Investigation;
import org.junit.Ignore;
import org.junit.Test;

public class TestLuceneSingleton {

	@Ignore
	@Test()
	public void testInit() throws Exception {
		LuceneSingleton lucene = LuceneSingleton.getInstance();
		lucene = LuceneSingleton.getInstance();
		assertEquals(2, lucene.getInstanceCount());
		lucene.close();
		assertEquals(1, lucene.getInstanceCount());
		lucene.close();
		assertEquals(0, lucene.getInstanceCount());
	}

	@Test()
	public void one() throws Exception {
		LuceneSingleton lucene = LuceneSingleton.getInstance();
		lucene.clear();
		lucene.close();
		lucene = LuceneSingleton.getInstance();

		Investigation investigation = new Investigation();

		investigation.setName("Inv 1");
		investigation.setTitle("Title of first investigation");
		investigation.setId(42L);
		lucene.addDocument(investigation);

		investigation.setName("Inv 2");
		investigation.setTitle("Title of second investigation");
		investigation.setId(43L);
		lucene.addDocument(investigation);

		Dataset dataset = new Dataset();
		dataset.setName("DS 1");
		dataset.setLocation("Location of first dataset");
		dataset.setId(44L);
		lucene.addDocument(dataset);

		lucene.close();
		lucene = LuceneSingleton.getInstance();

		String query = "first AND investigation";
		int n = lucene.getCount(query);
		assertEquals(1, n);
		List<String> results = lucene.search(query, 0, n);
		assertTrue(results.contains("Investigation:42"));

		query = "first  investigation";
		n = lucene.getCount(query);
		assertEquals(3, n);
		results = lucene.search(query, 0, n);
		assertTrue(results.contains("Investigation:42"));
		assertTrue(results.contains("Investigation:43"));
		assertTrue(results.contains("Dataset:44"));

		query = "first";
		n = lucene.getCount(query);
		assertEquals(2, n);
		results = lucene.search(query, 0, n);
		assertTrue(results.contains("Investigation:42"));
		assertTrue(results.contains("Dataset:44"));

		query = "Dataset:first";
		n = lucene.getCount(query);
		assertEquals(1, n);
		results = lucene.search(query, 0, n);
		assertTrue(results.contains("Dataset:44"));

		dataset = new Dataset();
		dataset.setName("DS 1");
		dataset.setLocation("Location of dataset");
		dataset.setId(44L);
		lucene.updateDocument(dataset);

		lucene.close();
		lucene = LuceneSingleton.getInstance();

		query = "first AND investigation";
		n = lucene.getCount(query);
		assertEquals(1, n);
		results = lucene.search(query, 0, n);
		assertTrue(results.contains("Investigation:42"));

		query = "first  investigation";
		n = lucene.getCount(query);
		assertEquals(2, n);
		results = lucene.search(query, 0, n);
		assertTrue(results.contains("Investigation:42"));
		assertTrue(results.contains("Investigation:43"));

		query = "first";
		n = lucene.getCount(query);
		assertEquals(1, n);
		results = lucene.search(query, 0, n);
		assertTrue(results.contains("Investigation:42"));

		query = "Dataset:first";
		n = lucene.getCount(query);
		assertEquals(0, n);
		
		investigation.setName("Inv 1");
		investigation.setTitle("Title of first investigation");
		investigation.setId(42L);
		lucene.deleteDocument(investigation);
		
		lucene.close();
		lucene = LuceneSingleton.getInstance();

		query = "first AND investigation";
		n = lucene.getCount(query);
		assertEquals(0, n);

		query = "first  investigation";
		n = lucene.getCount(query);
		assertEquals(1, n);
		results = lucene.search(query, 0, n);
		assertTrue(results.contains("Investigation:43"));

		query = "first";
		n = lucene.getCount(query);
		assertEquals(0, n);

		query = "Dataset:first";
		n = lucene.getCount(query);
		assertEquals(0, n);

		lucene.close();

	}

}

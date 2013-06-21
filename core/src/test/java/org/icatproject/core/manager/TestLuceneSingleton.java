package org.icatproject.core.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.icatproject.core.IcatException;
import org.icatproject.core.IcatException.IcatExceptionType;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.Investigation;
import org.icatproject.core.manager.LuceneSingleton.LuceneSearchResult;
import org.junit.Test;

public class TestLuceneSingleton {

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

		// Commit after clear
		lucene.commit();

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

		// Commit after addition of 3 entries
		lucene.commit();

		String query = "first AND investigation";
		List<String> results = lucene.search(query, 10, null).getResults();
		assertEquals(1, results.size());
		assertTrue(results.contains("Investigation:42"));

		query = "first investigation";
		results = lucene.search(query, 10, null).getResults();
		assertEquals(3, results.size());
		assertTrue(results.contains("Investigation:42"));
		assertTrue(results.contains("Investigation:43"));
		assertTrue(results.contains("Dataset:44"));

		LuceneSearchResult luceneResult = lucene.search(query, 1, null);
		assertEquals(1, luceneResult.getResults().size());
		assertEquals("Investigation:42", luceneResult.getResults().get(0));

		luceneResult = lucene.searchAfter(luceneResult, 1);
		assertEquals(1, luceneResult.getResults().size());
		assertEquals("Investigation:43", luceneResult.getResults().get(0));

		luceneResult = lucene.searchAfter(luceneResult, 1);
		assertEquals(1, luceneResult.getResults().size());
		assertEquals("Dataset:44", luceneResult.getResults().get(0));

		luceneResult = lucene.searchAfter(luceneResult, 1);
		assertEquals(0, luceneResult.getResults().size());

		query = "first";
		results = lucene.search(query, 10, null).getResults();
		assertEquals(2, results.size());
		assertTrue(results.contains("Investigation:42"));
		assertTrue(results.contains("Dataset:44"));

		query = "first";
		results = lucene.search(query, 10, "Dataset").getResults();
		assertEquals(1, results.size());
		assertTrue(results.contains("Dataset:44"));

		query = "first";

		try {
			results = lucene.search(query, 10, "dataset").getResults();
			fail("Must throw a bad paremeter exception");
		} catch (IcatException e) {
			assertEquals(IcatExceptionType.BAD_PARAMETER, e.getType());
		}

		dataset = new Dataset();
		dataset.setName("DS 1");
		dataset.setLocation("Location of special dataset");
		dataset.setId(44L);
		lucene.updateDocument(dataset);

		// Commit after update of 1 entry
		lucene.commit();

		query = "first AND investigation";
		results = lucene.search(query, 10, null).getResults();
		assertEquals(1, results.size());
		assertTrue(results.contains("Investigation:42"));

		query = "first  investigation";
		results = lucene.search(query, 10, null).getResults();
		assertEquals(2, results.size());
		assertTrue(results.contains("Investigation:42"));
		assertTrue(results.contains("Investigation:43"));

		query = "first";
		results = lucene.search(query, 10, null).getResults();
		assertEquals(1, results.size());
		assertTrue(results.contains("Investigation:42"));

		query = "Dataset:first";
		results = lucene.search(query, 10, null).getResults();
		assertEquals(0, results.size());

		investigation.setName("Inv 1");
		investigation.setTitle("Title of first investigation");
		investigation.setId(42L);
		lucene.deleteDocument(investigation);

		// Commit after deletion of 1 entry
		lucene.commit();

		query = "first AND investigation";
		results = lucene.search(query, 10, null).getResults();
		assertEquals(0, results.size());

		query = "first  investigation";
		results = lucene.search(query, 10, null).getResults();
		assertEquals(1, results.size());
		assertTrue(results.contains("Investigation:43"));

		query = "first";
		results = lucene.search(query, 10, null).getResults();
		assertEquals(0, results.size());

		query = "Dataset:first";
		results = lucene.search(query, 10, null).getResults();
		assertEquals(0, results.size());

		lucene.close();

	}

}

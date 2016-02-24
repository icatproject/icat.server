package org.icatproject.core.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;

public class TestLucene {

	static final int scale = (int) 1.0e5;

	@Test
	public void testIcatAnalyzer() throws Exception {
		final String text = "This is a demo   of the 1st (or is it number 2) all singing and dancing TokenStream's API with added aardvarks";
		int n = 0;
		String newString = "";

		try (Analyzer analyzer = new IcatAnalyzer()) {
			TokenStream stream = analyzer.tokenStream("field", new StringReader(text));
			CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
			try {
				stream.reset(); // Curiously this is required
				while (stream.incrementToken()) {
					n++;
					newString = newString + " " + termAtt;
				}
				stream.end();
			} finally {
				stream.close();
			}
		}

		assertEquals(11, n);
		assertEquals(" demo 1st number 2 all sing danc tokenstream api ad aardvark", newString);
	}

	@Test
	public void testJoins() throws Exception {
		Analyzer analyzer = new IcatAnalyzer();
		IndexWriterConfig config;

		Path tmpLuceneDir = Files.createTempDirectory("lucene");
		FSDirectory investigationDirectory = FSDirectory.open(tmpLuceneDir.resolve("Investigation"));
		config = new IndexWriterConfig(analyzer);
		config.setOpenMode(OpenMode.CREATE);
		IndexWriter investigationWriter = new IndexWriter(investigationDirectory, config);

		FSDirectory investigationUserDirectory = FSDirectory.open(tmpLuceneDir.resolve("InvestigationUser"));
		config = new IndexWriterConfig(analyzer);
		config.setOpenMode(OpenMode.CREATE);
		IndexWriter investigationUserWriter = new IndexWriter(investigationUserDirectory, config);

		FSDirectory datasetDirectory = FSDirectory.open(tmpLuceneDir.resolve("Dataset"));
		config = new IndexWriterConfig(analyzer);
		config.setOpenMode(OpenMode.CREATE);
		IndexWriter datasetWriter = new IndexWriter(datasetDirectory, config);

		FSDirectory datafileDirectory = FSDirectory.open(tmpLuceneDir.resolve("Datafile"));
		config = new IndexWriterConfig(analyzer);
		config.setOpenMode(OpenMode.CREATE);
		IndexWriter datafileWriter = new IndexWriter(datafileDirectory, config);

		addInvestigationUser(investigationUserWriter, "fred", 101);
		addInvestigationUser(investigationUserWriter, "bill", 101);
		addInvestigationUser(investigationUserWriter, "mary", 102);
		addInvestigation(investigationWriter, "inv1", 101);
		addInvestigation(investigationWriter, "inv2", 102);
		addDataset(datasetWriter, "ds1", 1, 101);
		addDataset(datasetWriter, "ds2", 2, 101);
		addDataset(datasetWriter, "ds3", 3, 102);
		addDatafile(datafileWriter, "df1", 1, 1);
		addDatafile(datafileWriter, "df2", 2, 1);
		addDatafile(datafileWriter, "df3", 3, 2);
		addDatafile(datafileWriter, "df4", 4, 2);
		addDatafile(datafileWriter, "df5", 5, 2);

		for (int i = 0; i < scale; i++) {
			addInvestigationUser(investigationUserWriter, "extra" + i, 500 + i);
			addInvestigation(investigationWriter, "extra" + i, 500 + i);
		}

		investigationWriter.close();
		investigationUserWriter.close();
		datasetWriter.close();
		datafileWriter.close();

		IndexSearcher investigationSearcher = new IndexSearcher(DirectoryReader.open(investigationDirectory));
		IndexSearcher investigationUserSearcher = new IndexSearcher(DirectoryReader.open(investigationUserDirectory));
		IndexSearcher datasetSearcher = new IndexSearcher(DirectoryReader.open(datasetDirectory));
		IndexSearcher datafileSearcher = new IndexSearcher(DirectoryReader.open(datafileDirectory));

		StandardQueryParser parser = new StandardQueryParser();
		StandardQueryConfigHandler qpConf = (StandardQueryConfigHandler) parser.getQueryConfigHandler();
		qpConf.set(ConfigurationKeys.ANALYZER, analyzer);
		qpConf.set(ConfigurationKeys.ALLOW_LEADING_WILDCARD, true);

		long start = System.currentTimeMillis();

		checkInvestigations(Arrays.asList(0), "inv1", null, investigationSearcher, investigationUserSearcher, parser);
		checkInvestigations(Arrays.asList(1), "inv2", null, investigationSearcher, investigationUserSearcher, parser);
		checkInvestigations(Arrays.asList(0, 1), "inv*", null, investigationSearcher, investigationUserSearcher,
				parser);
		checkInvestigations(Arrays.asList(), "inv3", null, investigationSearcher, investigationUserSearcher, parser);
		checkInvestigations(Arrays.asList(), "inv1", "mary", investigationSearcher, investigationUserSearcher, parser);
		checkInvestigations(Arrays.asList(1), "inv2", "mary", investigationSearcher, investigationUserSearcher, parser);

		checkInvestigations(Arrays.asList(1), null, "mary", investigationSearcher, investigationUserSearcher, parser);
		checkInvestigations(Arrays.asList(0), null, "fred", investigationSearcher, investigationUserSearcher, parser);
		checkInvestigations(Arrays.asList(), null, "harry", investigationSearcher, investigationUserSearcher, parser);

		checkDatasets(Arrays.asList(2), "ds3", null, investigationSearcher, investigationUserSearcher, datasetSearcher,
				parser);
		checkDatasets(Arrays.asList(0, 1), null, "fred", investigationSearcher, investigationUserSearcher,
				datasetSearcher, parser);
		checkDatasets(Arrays.asList(2), "ds3", "mary", investigationSearcher, investigationUserSearcher,
				datasetSearcher, parser);
		checkDatasets(Arrays.asList(), "ds3", "fred", investigationSearcher, investigationUserSearcher, datasetSearcher,
				parser);

		checkDatafiles(Arrays.asList(0), "df1", null, investigationSearcher, investigationUserSearcher, datasetSearcher,
				datafileSearcher, parser);

		checkDatafiles(Arrays.asList(0), "df1", "fred", investigationSearcher, investigationUserSearcher,
				datasetSearcher, datafileSearcher, parser);

		checkDatafiles(Arrays.asList(0, 1, 2, 3, 4), null, "fred", investigationSearcher, investigationUserSearcher,
				datasetSearcher, datafileSearcher, parser);

		checkDatafiles(Arrays.asList(), null, "mary", investigationSearcher, investigationUserSearcher, datasetSearcher,
				datafileSearcher, parser);

		checkDatafiles(Arrays.asList(), "df1", "mary", investigationSearcher, investigationUserSearcher,
				datasetSearcher, datafileSearcher, parser);

		System.out.println("Join tests took " + (System.currentTimeMillis() - start) + "ms");
	}

	private void checkDatafiles(List<Integer> dnums, String fname, String uname, IndexSearcher investigationSearcher,
			IndexSearcher investigationUserSearcher, IndexSearcher datasetSearcher, IndexSearcher datafileSearcher,
			StandardQueryParser parser) throws IOException, QueryNodeException {
		ScoreDoc[] hits = get(fname, uname, investigationSearcher, investigationUserSearcher, datasetSearcher,
				datafileSearcher, parser);
		assertEquals("Size", dnums.size(), hits.length);
		for (ScoreDoc hit : hits) {
			assertTrue("Found unexpected " + hit.doc, dnums.contains(hit.doc));
			assertNotNull(datafileSearcher.doc(hit.doc).get("id"));
		}
	}

	/* Datasets */
	private ScoreDoc[] get(String sname, String uname, IndexSearcher investigationSearcher,
			IndexSearcher investigationUserSearcher, IndexSearcher datasetSearcher, StandardQueryParser parser)
					throws IOException, QueryNodeException {
		BooleanQuery.Builder theQuery = new BooleanQuery.Builder();
		if (uname != null) {
			Query iuQuery = JoinUtil.createJoinQuery("investigation", false, "id",
					new TermQuery(new Term("name", uname)), investigationUserSearcher, ScoreMode.None);

			Query invQuery = JoinUtil.createJoinQuery("id", false, "investigation", iuQuery, investigationSearcher,
					ScoreMode.None);

			theQuery.add(invQuery, Occur.MUST);
		}

		if (sname != null) {
			theQuery.add(parser.parse(sname, "name"), Occur.MUST);
		}

		TopDocs topDocs = datasetSearcher.search(theQuery.build(), 50);
		return topDocs.scoreDocs;

	}

	/* Datafiles */
	private ScoreDoc[] get(String fname, String uname, IndexSearcher investigationSearcher,
			IndexSearcher investigationUserSearcher, IndexSearcher datasetSearcher, IndexSearcher datafileSearcher,
			StandardQueryParser parser) throws IOException, QueryNodeException {
		BooleanQuery.Builder theQuery = new BooleanQuery.Builder();
		if (uname != null) {
			Query iuQuery = JoinUtil.createJoinQuery("investigation", false, "id",
					new TermQuery(new Term("name", uname)), investigationUserSearcher, ScoreMode.None);

			Query invQuery = JoinUtil.createJoinQuery("id", false, "investigation", iuQuery, investigationSearcher,
					ScoreMode.None);

			Query dsQuery = JoinUtil.createJoinQuery("id", false, "dataset", invQuery, datasetSearcher, ScoreMode.None);

			theQuery.add(dsQuery, Occur.MUST);
		}

		if (fname != null) {
			theQuery.add(parser.parse(fname, "name"), Occur.MUST);
		}

		TopDocs topDocs = datafileSearcher.search(theQuery.build(), 50);
		return topDocs.scoreDocs;
	}

	/* Investigations */
	private ScoreDoc[] get(String iname, String uname, IndexSearcher investigationSearcher,
			IndexSearcher investigationUserSearcher, StandardQueryParser parser)
					throws QueryNodeException, IOException {
		BooleanQuery.Builder theQuery = new BooleanQuery.Builder();

		if (iname != null) {
			theQuery.add(parser.parse(iname, "name"), Occur.MUST);
		}

		if (uname != null) {
			Query iuQuery = JoinUtil.createJoinQuery("investigation", false, "id",
					new TermQuery(new Term("name", uname)), investigationUserSearcher, ScoreMode.None);
			theQuery.add(iuQuery, Occur.MUST);
		}

		TopDocs topDocs = investigationSearcher.search(theQuery.build(), 50);
		return topDocs.scoreDocs;

	}

	private void checkDatasets(List<Integer> dnums, String sname, String uname, IndexSearcher investigationSearcher,
			IndexSearcher investigationUserSearcher, IndexSearcher datasetSearcher, StandardQueryParser parser)
					throws IOException, QueryNodeException {
		ScoreDoc[] hits = get(sname, uname, investigationSearcher, investigationUserSearcher, datasetSearcher, parser);
		assertEquals("Size", dnums.size(), hits.length);
		for (ScoreDoc hit : hits) {
			assertTrue("Found unexpected " + hit.doc, dnums.contains(hit.doc));
			assertNotNull(datasetSearcher.doc(hit.doc).get("id"));
		}

	}

	private void checkInvestigations(List<Integer> dnums, String iname, String uname,
			IndexSearcher investigationSearcher, IndexSearcher investigationUserSearcher, StandardQueryParser parser)
					throws QueryNodeException, IOException {

		ScoreDoc[] hits = get(iname, uname, investigationSearcher, investigationUserSearcher, parser);
		assertEquals("Size", dnums.size(), hits.length);
		for (ScoreDoc hit : hits) {
			assertTrue("Found unexpected " + hit.doc, dnums.contains(hit.doc));
			assertNotNull(investigationSearcher.doc(hit.doc).get("id"));
		}
	}

	private void addInvestigation(IndexWriter iwriter, String name, long iNum) throws IOException {
		Document doc = new Document();
		doc.add(new StringField("name", name, Store.NO));
		doc.add(new SortedDocValuesField("id", new BytesRef(Long.toString(iNum))));
		doc.add(new StringField("id", Long.toString(iNum), Store.YES));
		iwriter.addDocument(doc);
	}

	private void addInvestigationUser(IndexWriter iwriter, String name, long iNum) throws IOException {
		Document doc = new Document();
		doc.add(new StringField("name", name, Store.NO));
		doc.add(new SortedDocValuesField("investigation", new BytesRef(Long.toString(iNum))));
		iwriter.addDocument(doc);
	}

	private void addDataset(IndexWriter iwriter, String name, int sNum, int iNum) throws IOException {
		Document doc = new Document();
		doc.add(new StringField("name", name, Store.NO));
		doc.add(new StringField("id", Long.toString(sNum), Store.YES));
		doc.add(new SortedDocValuesField("id", new BytesRef(Long.toString(sNum))));
		doc.add(new StringField("investigation", Long.toString(iNum), Store.NO));
		iwriter.addDocument(doc);
	}

	private void addDatafile(IndexWriter iwriter, String name, int fNum, int sNum) throws IOException {
		Document doc = new Document();
		doc.add(new StringField("name", name, Store.NO));
		doc.add(new StringField("id", Long.toString(fNum), Store.YES));
		doc.add(new StringField("dataset", Long.toString(sNum), Store.NO));
		iwriter.addDocument(doc);
	}

}

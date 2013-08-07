package org.icatproject.core.oldparser;

import static org.junit.Assert.assertEquals;

import org.icatproject.core.parser.GetQuery;
import org.icatproject.core.parser.Input;
import org.icatproject.core.parser.Tokenizer;
import org.junit.Test;

public class OldTestGet {

	@Test
	public void testGood1() throws Exception {
		String query = "Dataset";
		String expect = "Dataset AS Dataset$";
		test(query, expect);
	}

	@Test
	public void testGood2() throws Exception {
		String query = "Dataset INCLUDE Datafile, DatasetParameter, DatafileParameter, DatasetType";
		syntest(query);
	}

	private void syntest(String query) throws Exception {
		OldGetQuery q = new OldGetQuery(new OldInput(OldTokenizer.getTokens(query)));
		query = q.getNewQuery();
		new GetQuery(new Input(Tokenizer.getTokens(query)));
	}

	@Test
	public void testGood6() throws Exception {
		String query = "Datafile INCLUDE 1";
		String expect = "Datafile AS Datafile$ INCLUDE 1";
		test(query, expect);
	}

	private void test(String query, String expect) throws Exception {
		OldGetQuery q = new OldGetQuery(new OldInput(OldTokenizer.getTokens(query)));
		query = q.getNewQuery();
		assertEquals(expect, query);
		new GetQuery(new Input(Tokenizer.getTokens(query)));
	}
}

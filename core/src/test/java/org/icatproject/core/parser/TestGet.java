package org.icatproject.core.parser;

import static org.junit.Assert.assertEquals;

import org.icatproject.core.parser.GetQuery;
import org.icatproject.core.parser.Input;
import org.icatproject.core.parser.Tokenizer;
import org.junit.Test;

public class TestGet {

	@Test
	public void testGood1() throws Exception {
		String query = "Dataset AS Dataset$";
		String expect = "Dataset";
		test(query, expect);
	}

	@Test
	public void testGood2() throws Exception {
		String query = "Dataset AS Dataset$ INCLUDE Dataset$.parameters AS DatasetParameter_$, "
				+ "Dataset$.datafiles AS Datafile_$, Datafile_$.parameters AS DatafileParameter_$, "
				+ "Dataset$.type AS DatasetType_$Dataset";
		String expect = "Dataset AS $0$ INCLUDE 0.parameters -> 1, 0.datafiles -> 2, 2.parameters -> 3, 0.type -> 4";
		test(query, expect);
	}

	@Test
	public void testGood6() throws Exception {
		String query = "Datafile INCLUDE 1";
		String expect = "Datafile AS $0$ INCLUDE 1";
		test(query, expect);
	}

	private void test(String query, String expect) throws Exception {
		query = new GetQuery(new Input(Tokenizer.getTokens(query))).toString();
		assertEquals(expect, query);

	}
}

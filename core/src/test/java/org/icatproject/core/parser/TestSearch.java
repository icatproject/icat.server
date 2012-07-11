package org.icatproject.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

public class TestSearch {

	@Test
	public void testGood1() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("AVG(Dataset.id) [id != 5]");
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertEquals("AVG(Dataset.id)[id != 5]", sq.toString());
		assertNull(input.peek(0));
	}

}
package org.icatproject.core.parser;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class TestTokenizer {

	@Test
	public void testGood1() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("<-> investigation investigator facility_user_id = '$user'");
		String[] tostrings = { "<", "-", ">", "investigation", "investigator",
				"facility_user_id", "=", "$user" };
		assertEquals(tostrings.length, tokens.size());
		int i = 0;
		for (Token t : tokens) {
			assertEquals(tostrings[i++], t.toString());
		}
	}

	@Test
	public void testGood2() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("AND and oR nOT() != <> > < 17 15. 17.0E-1");
		String[] tostrings = { "AND", "AND", "OR", "NOT", "(", ")", "!=", "<>", ">", "<", "17",
				"15.0", "1.7" };
		assertEquals(tostrings.length, tokens.size());
		int i = 0;
		for (Token t : tokens) {
			assertEquals(tostrings[i++], t.toString());
		}
	}

	@Test
	public void testTS() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("{ts 1950-01-21 02:00:00} {  ts   1950-01-21   02:00:00      }");
		String[] tostrings = { "{ts 1950-01-21 02:00:00}", "{ts 1950-01-21 02:00:00}" };
		assertEquals(tostrings.length, tokens.size());
		int i = 0;
		for (Token t : tokens) {
			assertEquals(tostrings[i++], t.toString());
		}
	}

	@Test
	public void testQuotes() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("c='aaa', d='bbb''qqq', e = ' ', f = '', g = ''''''");
		String[] tostrings = { "c", "=", "aaa", ",", "d", "=", "bbb'qqq", ",", "e", "=", " ", ",",
				"f", "=", "", ",", "g", "=", "''" };
		assertEquals(tostrings.length, tokens.size());
		int i = 0;
		for (Token t : tokens) {
			assertEquals(tostrings[i++], t.toString());
		}
	}

	@Test(expected = LexerException.class)
	public void testBad1() throws Exception {
		Tokenizer.getTokens("!");
	}

	@Test(expected = LexerException.class)
	public void testBad2() throws Exception {
		Tokenizer.getTokens("! ");
	}

	@Test(expected = LexerException.class)
	public void testBad3() throws Exception {
		Tokenizer.getTokens("'abcds''qwe ");
	}

	@Test(expected = LexerException.class)
	public void testBad4() throws Exception {
		Tokenizer.getTokens("{ts 1950-01-21T02:00:00}");
	}

}

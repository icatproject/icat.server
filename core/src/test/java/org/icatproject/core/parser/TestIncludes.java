package org.icatproject.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.DatafileFormat;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.DatasetParameter;
import org.icatproject.core.entity.DatasetType;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Facility;
import org.icatproject.core.entity.Investigation;
import org.icatproject.core.entity.Sample;
import org.icatproject.core.entity.SampleParameter;
import org.junit.Ignore;
import org.junit.Test;

public class TestIncludes {

	private void testGood(List<Token> tokens, Class<? extends EntityBaseBean>... incArray)
			throws Exception {
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertNull(input.peek(0));
		Set<Class<? extends EntityBaseBean>> incSet = new HashSet<Class<? extends EntityBaseBean>>(
				Arrays.asList(incArray));
		assertEquals("Included entities", incSet, sq.getInclude().getBeans());
		assertTrue("One", !sq.getInclude().isOne());
	}

	@SuppressWarnings("unchecked")
	@Test(expected = ParserException.class)
	public void testGood1() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("Dataset INCLUDE Datafile, 1 [id = 5]");
		testGood(tokens, Datafile.class, DatasetType.class, Sample.class, DatafileFormat.class,
				Investigation.class);
	}

	@Test
	public void testGood2() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("Dataset INCLUDE 1 [id != 5]");
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertNull(input.peek(0));
		assertTrue("One", sq.getInclude().isOne());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGood3() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("Dataset INCLUDE Datafile [id != 53]");
		testGood(tokens, Datafile.class);
	}

	@Test
	public void testGood4() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("Dataset [id != 573]");
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertNull(input.peek(0));
		assertNull("No Include", sq.getInclude());
	}

	@Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void testGood5() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("Investigation INCLUDE Dataset, Datafile, DatasetParameter, Facility, Sample, SampleParameter");
		testGood(tokens, Dataset.class, Datafile.class, DatasetParameter.class, Facility.class,
				Sample.class, SampleParameter.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBad1() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("Dataset.id INCLUDE Datafile [id != 53]");
		try {
			testGood(tokens, Datafile.class);
			fail("Exception not thrown");
		} catch (ParserException e) {
			assertEquals(
					"Expected token from types [ENTSEP] at token INCLUDE in Dataset.id < INCLUDE > Datafile [ ",
					e.getMessage());
		}
	}
}
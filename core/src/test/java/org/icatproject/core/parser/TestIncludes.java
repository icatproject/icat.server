package org.icatproject.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
import org.junit.Test;

public class TestIncludes {

	private void testGood(List<Token> tokens, Class<? extends EntityBaseBean>... incArray)
			throws Exception {
		Input input = new Input(tokens);
		SearchQuery sq = new SearchQuery(input);
		assertNull(input.peek(0));
		Set<Class<? extends EntityBaseBean>> incSet = new HashSet<Class<? extends EntityBaseBean>>(
				Arrays.asList(incArray));
		assertEquals("Included entities", incSet, sq.getIncludes());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGood1() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("Dataset INCLUDE Datafile, 1 [id = 5]");
		testGood(tokens, Datafile.class, DatasetType.class, Sample.class, DatafileFormat.class,
				Investigation.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGood2() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("Dataset INCLUDE 1 [id != 5]");
		testGood(tokens, DatasetType.class, Sample.class, Investigation.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGood3() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("Dataset INCLUDE Datafile [id != 53]");
		testGood(tokens, Datafile.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGood4() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("Dataset [id != 573]");
		testGood(tokens);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGood5() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("Investigation INCLUDE Dataset, Datafile, DatasetParameter, Facility, Sample, SampleParameter");
		testGood(tokens, Dataset.class, Datafile.class, DatasetParameter.class, Facility.class,
				Sample.class, SampleParameter.class);
	}
}
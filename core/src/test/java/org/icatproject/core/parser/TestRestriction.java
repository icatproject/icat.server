package org.icatproject.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.parser.Input;
import org.icatproject.core.parser.Restriction;
import org.icatproject.core.parser.Token;
import org.icatproject.core.parser.Tokenizer;
import org.junit.Test;


public class TestRestriction {

	private void testGood(List<Token> tokens, String q, String sw, List<String> res, String top)
			throws Exception {
		Input input = new Input(tokens);
		Restriction e = new Restriction(input);
		assertNull(input.peek(0));

		Set<String> relatedEntityNames = new HashSet<String>();
		for (Class<? extends EntityBaseBean> bean : e.getRelatedEntities()) {
			relatedEntityNames.add(bean.getSimpleName());
		}

		assertEquals("Related entities", new HashSet<String>(res), relatedEntityNames);
		assertEquals("SearchWhere", sw, e.getSearchWhere(top));
		assertEquals("Query", q + sw, e.getQuery(top));
	}

	@Test
	public void testGood1() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("[id = 20] <-> Investigation <-> InvestigationUser <-> User[name = :user]");
		String sw = "(Dataset$.id = 20) AND (User$.name = :user)";
		String q = "SELECT COUNT(Dataset$) FROM Dataset AS Dataset$ LEFT JOIN Dataset$.investigation AS Investigation$ LEFT JOIN Investigation$.investigationUsers AS InvestigationUser$ LEFT JOIN InvestigationUser$.user AS User$  WHERE (Dataset$.id = :pkid) AND ";
		testGood(tokens, q, sw, Arrays.asList("Investigation", "InvestigationUser", "User"),
				"Dataset");
	}

	@Test
	public void testGood2() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("Investigation InvestigationUser [user.userId = :user]");
		String sw = "(InvestigationUser$.user.userId = :user)";
		String q = "SELECT COUNT(Dataset$) FROM Dataset AS Dataset$ LEFT JOIN Dataset$.investigation AS Investigation$ LEFT JOIN Investigation$.investigationUsers AS InvestigationUser$  WHERE (Dataset$.id = :pkid) AND ";
		testGood(tokens, q, sw, Arrays.asList("Investigation", "InvestigationUser"), "Dataset");
	}

	@Test
	public void testGood3() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("Dataset Investigation [name = 'A']");
		String sw = "(Investigation$.name = 'A')";
		String q = "SELECT COUNT(DatasetParameter$) FROM DatasetParameter AS DatasetParameter$ LEFT JOIN DatasetParameter$.dataset AS Dataset$ LEFT JOIN Dataset$.investigation AS Investigation$  WHERE (DatasetParameter$.id = :pkid) AND ";
		Input input = new Input(tokens);
		Restriction e = new Restriction(input);
		assertNull(input.peek(0));
		System.out.println(e.getQuery("DatasetParameter"));
		for (Class<? extends EntityBaseBean> bean : e.getRelatedEntities()) {
			System.out.println(bean.getSimpleName());
		}
		System.out.println(e.getSearchWhere("DatasetParameter"));
		testGood(tokens, q, sw, Arrays.asList("Investigation", "Dataset"), "DatasetParameter");
	}

}

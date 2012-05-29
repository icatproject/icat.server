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
import org.junit.Ignore;

public class TestRestriction {

	private void testGood(List<Token> tokens, String q, String sw, List<String> res, String top)
			throws Exception {
		Input input = new Input(tokens);
		Restriction r = new Restriction(input);
		assertNull(input.peek(0));
		//		System.out.println(r);

		Set<String> relatedEntityNames = new HashSet<String>();
		for (Class<? extends EntityBaseBean> bean : r.getRelatedEntities()) {
			relatedEntityNames.add(bean.getSimpleName());
		}

		assertEquals("Related entities", new HashSet<String>(res), relatedEntityNames);
		assertEquals("SearchWhere", sw, r.getSearchWhere(top));
		assertEquals("Query", q + sw, r.getQuery(top));
	}

	@Test
	public void testGood1() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("[id = 20] <-> Investigation <-> InvestigationUser <-> User[name = :user]");
		String sw = "(Dataset$.id = 20) AND (User$.name = :user)";
		String q = "SELECT COUNT(Dataset$) FROM Dataset AS Dataset$ JOIN Dataset$.investigation AS Investigation$ JOIN Investigation$.investigationUsers AS InvestigationUser$ JOIN InvestigationUser$.user AS User$  WHERE (Dataset$.id = :pkid) AND ";
		testGood(tokens, q, sw, Arrays.asList("Investigation", "InvestigationUser", "User"),
				"Dataset");
	}

	@Test
	public void testGood2() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("<-> Investigation <-> InvestigationUser [user.userId = :user]");
		String sw = "(InvestigationUser$.user.userId = :user)";
		String q = "SELECT COUNT(Dataset$) FROM Dataset AS Dataset$ JOIN Dataset$.investigation AS Investigation$ JOIN Investigation$.investigationUsers AS InvestigationUser$  WHERE (Dataset$.id = :pkid) AND ";
		testGood(tokens, q, sw, Arrays.asList("Investigation", "InvestigationUser"), "Dataset");
	}
	
	@Test
	public void testGood3() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("<-> Investigation <-> InvestigationUser [user.userId IN( -1, -23, 4, -4.12, 4.12, 1.0E99, -1.0E99, 1.0E-99, -1.0E-99) ]");
		String sw = "(InvestigationUser$.user.userId IN (-1, -23, 4, -4.12, 4.12, 1.0E99, -1.0E99, 1.0E-99, -1.0E-99))";
		String q = "SELECT COUNT(Dataset$) FROM Dataset AS Dataset$ JOIN Dataset$.investigation AS Investigation$ JOIN Investigation$.investigationUsers AS InvestigationUser$  WHERE (Dataset$.id = :pkid) AND ";
		testGood(tokens, q, sw, Arrays.asList("Investigation", "InvestigationUser"), "Dataset");	}

	@Ignore
	@Test
	public void testGood4() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("<-> Dataset <-> Investigation [name = 'A']");
		String sw = "(Investigation$.name = 'A')";
		String q = "SELECT COUNT(DatasetParameter$) FROM DatasetParameter AS DatasetParameter$ LEFT JOIN DatasetParameter$.dataset AS Dataset$ LEFT JOIN Dataset$.investigation AS Investigation$  WHERE (DatasetParameter$.id = :pkid) AND ";
		testGood(tokens, q, sw, Arrays.asList(" <-> Investigation", "Dataset"), "DatasetParameter");
	}

	@Ignore
	@Test
	public void testGood5() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("<-> Dataset <-> Datafile([name = 'fred'][name = 'bill']");
		String sw = "(Investigation$.name = 'A')";
		String q = "SELECT COUNT(DatasetParameter$) FROM DatasetParameter AS DatasetParameter$ LEFT JOIN DatasetParameter$.dataset AS Dataset$ LEFT JOIN Dataset$.investigation AS Investigation$  WHERE (DatasetParameter$.id = :pkid) AND ";
		testGood(tokens, q, sw, Arrays.asList("Investigation", "Dataset"), "DatasetParameter");
	}

}

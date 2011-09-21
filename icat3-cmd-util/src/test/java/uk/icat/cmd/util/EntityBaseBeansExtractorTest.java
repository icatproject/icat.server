package uk.icat.cmd.util;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;

import uk.icat.cmd.util.EntityBaseBeansExtractor;

public class EntityBaseBeansExtractorTest {

	@Test
	public void shouldReturnBeansSet() {
		List<String> set = EntityBaseBeansExtractor.getSimpleNames();
		assertNotNull(set);
		assertTrue(set.size() > 0);
	}

}

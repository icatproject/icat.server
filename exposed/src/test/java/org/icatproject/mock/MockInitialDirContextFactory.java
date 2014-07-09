package org.icatproject.mock;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.spi.InitialContextFactory;

public class MockInitialDirContextFactory implements InitialContextFactory {

	private static DirContext mockContext = new MockDirContext();

	public static DirContext getLatestMockContext() {
		return mockContext;
	}

	public MockInitialDirContextFactory() {
	}

	@Override
	public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
		return mockContext;
	}
}
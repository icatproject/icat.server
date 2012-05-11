package org.icatproject.core.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.icatproject.core.user.AddressChecker;
import org.junit.Test;

public class TestAddressChecker {
	@Test
	public void t4() throws Exception {
		AddressChecker a = new AddressChecker(" 192.168.3.0/24 190.168.3.0/28 ");
		assertTrue("One", a.check("192.168.3.255"));
		assertTrue("Two", a.check("190.168.3.15"));
		assertFalse("Three", a.check("192.168.4.19"));
		assertFalse("Four", a.check("190.168.3.16"));
	}

	@Test
	public void t6() throws Exception {
		AddressChecker a = new AddressChecker("192:168:3:0:0:0:0:0/112");
		assertTrue("One", a.check("192:168:3:0:0:0:0:0"));
		assertTrue("Two", a.check("192:168:3:0:0:0:0:FFFF"));
		assertFalse("Three", a.check("192:168:3:0:0:0:1:0"));
	}
}
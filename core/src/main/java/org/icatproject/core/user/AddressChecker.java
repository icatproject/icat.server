package org.icatproject.core.user;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.icatproject.core.IcatException;

public class AddressChecker {

	enum IP {
		IP4, IP6
	}

	public class Actual {

		private BigInteger number;
		private IP ip;

		public Actual(String add) throws IcatException {

			if (add.indexOf('.') >= 0) {
				ip = IP.IP4;
				String[] sections = add.split("\\.");
				if (sections.length != 4) {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "IP4 addresses must have 4 parts");
				}
				number = new BigInteger(sections[0]);
				for (int i = 1; i < 4; i++) {
					number = number.shiftLeft(8).add(new BigInteger(sections[i]));
				}
			} else {
				ip = IP.IP6;
				String[] sections = add.split(":");
				if (sections.length != 8) {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL, "IP6 addresses must have 8 parts");
				}
				number = new BigInteger(sections[0], 16);
				for (int i = 1; i < 8; i++) {
					number = number.shiftLeft(16).add(new BigInteger(sections[i], 16));
				}
			}
		}

		public boolean matches(Pattern pat) {
			if (ip != pat.ip) {
				return false;
			}
			return (number.xor(pat.number).and(pat.mask).equals(BigInteger.ZERO));
		}
	}

	public class Pattern {

		private BigInteger number;
		private BigInteger mask;
		private IP ip;
		private final static String prefix = "Configuration error: ";

		public Pattern(String string) throws IcatException {
			String[] parts = string.split("/");
			if (parts.length != 2) {
				throw new IcatException(IcatException.IcatExceptionType.INTERNAL, prefix
						+ "AddressChecker patterns must have one slash not: '" + string + "'");
			}
			int len = Integer.parseInt(parts[1]);
			String add = parts[0];
			if (add.indexOf('.') >= 0) {
				ip = IP.IP4;
				String[] sections = add.split("\\.");
				if (sections.length != 4) {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL, prefix + "IP4 addresses must have 4 parts");
				}
				number = new BigInteger(sections[0]);
				for (int i = 1; i < 4; i++) {
					number = number.shiftLeft(8).add(new BigInteger(sections[i]));
				}
				mask = BigInteger.ZERO;
				for (int i = 32 - len; i < 32; i++) {
					mask = mask.setBit(i);
				}
			} else {
				ip = IP.IP6;
				String[] sections = add.split(":");
				if (sections.length != 8) {
					throw new IcatException(IcatException.IcatExceptionType.INTERNAL, prefix + "IP6 addresses must have 8 parts");
				}
				number = new BigInteger(sections[0], 16);
				for (int i = 1; i < 8; i++) {
					number = number.shiftLeft(16).add(new BigInteger(sections[i], 16));
				}
				mask = BigInteger.ZERO;
				for (int i = 128 - len; i < 128; i++) {
					mask = mask.setBit(i);
				}
			}

		}

	}

	private List<Pattern> patterns = new ArrayList<Pattern>();

	public AddressChecker(String patternString) throws IcatException {
		for (String s : patternString.trim().split("\\s+")) {
			patterns.add(new Pattern(s));
		}

	}

	public boolean check(String addr) throws IcatException {
		Actual act = new Actual(addr);
		for (Pattern pattern : patterns) {
			if (act.matches(pattern)) {
				return true;
			}
		}
		return false;
	}

}
package org.icatproject.core.manager;

public class SingletonFinder {

	private static GateKeeper gateKeeper;

	public static void setGateKeeper(GateKeeper gateKeeper) {
		SingletonFinder.gateKeeper = gateKeeper;
	}

	public static GateKeeper getGateKeeper() {
		return gateKeeper;
	}

}
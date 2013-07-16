package org.icatproject.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.icatproject.core.IcatException;
import org.icatproject.core.entity.Datafile;
import org.icatproject.core.entity.DatafileParameter;
import org.icatproject.core.entity.Dataset;
import org.icatproject.core.entity.DatasetParameter;
import org.icatproject.core.entity.EntityBaseBean;
import org.icatproject.core.entity.Facility;
import org.icatproject.core.entity.InputDatafile;
import org.icatproject.core.entity.InputDataset;
import org.icatproject.core.entity.Instrument;
import org.icatproject.core.entity.Investigation;
import org.icatproject.core.entity.InvestigationInstrument;
import org.icatproject.core.entity.Job;
import org.icatproject.core.entity.ParameterType;
import org.icatproject.core.entity.User;
import org.junit.Test;

public class TestDagHandler {

	@Test
	public void good1() throws Exception {
		Set<Class<? extends EntityBaseBean>> es = new HashSet<Class<? extends EntityBaseBean>>();
		es.add(Investigation.class);
		es.add(Datafile.class);
		es.add(DatasetParameter.class);
		es.add(DatafileParameter.class);
		DagHandler.findSteps(Dataset.class, es);
		DagHandler.checkIncludes(Dataset.class, es);
	}

	@Test(expected = IcatException.class)
	public void good2() throws Exception {
		Set<Class<? extends EntityBaseBean>> es = new HashSet<Class<? extends EntityBaseBean>>();
		es.add(Investigation.class);
		es.add(Datafile.class);
		es.add(DatasetParameter.class);
		es.add(User.class);
		DagHandler.findSteps(Dataset.class, es);
		DagHandler.checkIncludes(Dataset.class, es);
	}

	@Test
	public void good3() throws Exception {
		Set<Class<? extends EntityBaseBean>> es = new HashSet<Class<? extends EntityBaseBean>>();
		es.add(Dataset.class);
		es.add(Datafile.class);
		es.add(DatasetParameter.class);
		es.add(Facility.class);
		DagHandler.findSteps(Investigation.class, es);
		DagHandler.checkIncludes(Investigation.class, es);
	}

	@Test
	public void good4() throws Exception {
		Set<Class<? extends EntityBaseBean>> es = new HashSet<Class<? extends EntityBaseBean>>();
		es.add(InputDataset.class);
		es.add(InputDatafile.class);
		es.add(Dataset.class);
		es.add(Datafile.class);
		try {
			DagHandler.findSteps(Job.class, es);
			fail("Exception not thrown");
		} catch (IcatException e) {
			assertEquals(IcatException.IcatExceptionType.BAD_PARAMETER, e.getType());
			assertTrue(e.getMessage().startsWith("Can't have loop in graph of entities."));
		}
		try {
			DagHandler.checkIncludes(Job.class, es);
			fail("Exception not thrown");
		} catch (IcatException e) {
			assertEquals(IcatException.IcatExceptionType.BAD_PARAMETER, e.getType());
			assertTrue(e.getMessage().startsWith("Can't have loop in graph of entities."));
		}

	}

	@Test
	public void good5() throws Exception {
		Set<Class<? extends EntityBaseBean>> es = new HashSet<Class<? extends EntityBaseBean>>();
		es.add(Instrument.class);
		es.add(InvestigationInstrument.class);
		es.add(Facility.class);
		es.add(Investigation.class);
		try {
			DagHandler.findSteps(Dataset.class, es);
			fail("Exception not thrown");
		} catch (IcatException e) {
			// Nothing for now
		}
		try {
			DagHandler.checkIncludes(Dataset.class, es);
			fail("Exception not thrown");
		} catch (IcatException e) {
			// Nothing for now
		}
	}

	@Test
	public void good6() throws Exception {
		Set<Class<? extends EntityBaseBean>> es = new HashSet<Class<? extends EntityBaseBean>>();
		es.add(Datafile.class);
		es.add(Dataset.class);
		es.add(DatasetParameter.class);
		DagHandler.findSteps(ParameterType.class, es);
		try {
			DagHandler.checkIncludes(ParameterType.class, es);
			fail("Exception not thrown");
		} catch (IcatException e) {
			// Nothing for now
		}
	}
}

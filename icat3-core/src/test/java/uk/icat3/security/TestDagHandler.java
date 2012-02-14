package uk.icat3.security;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import uk.icat3.entity.Datafile;
import uk.icat3.entity.DatafileParameter;
import uk.icat3.entity.Dataset;
import uk.icat3.entity.DatasetParameter;
import uk.icat3.entity.EntityBaseBean;
import uk.icat3.entity.User;
import uk.icat3.entity.Investigation;
import uk.icat3.exceptions.BadParameterException;
import uk.icat3.security.DagHandler.Step;
import uk.icat3.util.BaseTestTransaction;

public class TestDagHandler extends BaseTestTransaction {
	
	@Test
	public void t1() throws Exception {
		Set<Class<? extends EntityBaseBean>> es = new HashSet<Class<? extends EntityBaseBean>>();
		es.add(Investigation.class);
		es.add(Datafile.class);
		es.add(DatasetParameter.class);
		es.add(DatafileParameter.class);
		Step s = DagHandler.fixes(Dataset.class, es);
		System.out.println(s);
	}
	
	@Test(expected = BadParameterException.class )
	public void t2() throws Exception {
		Set<Class<? extends EntityBaseBean>> es = new HashSet<Class<? extends EntityBaseBean>>();
		es.add(Investigation.class);
		es.add(Datafile.class);
		es.add(DatasetParameter.class);
		es.add(User.class);
		DagHandler.fixes(Dataset.class, es);
	}

}

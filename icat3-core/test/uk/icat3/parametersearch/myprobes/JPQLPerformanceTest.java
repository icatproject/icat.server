/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 6 juil. 2010
 */

package uk.icat3.parametersearch.myprobes;

import uk.icat3.search.parameter.util.ParameterSearchUtil;
import java.util.List;
import org.junit.Test;
import uk.icat3.entity.Investigation;
import uk.icat3.entity.Parameter;
import uk.icat3.util.BaseTestClassTX;

/**
 * Datafile_parameter count = 1392669
 * Dataset_parameter count = 0
 * Sample_parameter count = 759
 * @author cruzcruz
 */
public class JPQLPerformanceTest extends BaseTestClassTX {
    public static ParameterSearchUtil searchUtil;
    public static  List<Parameter> lp;

    public JPQLPerformanceTest() {
        searchUtil = new ParameterSearchUtil();
    }


    /**
     * Initialites list parameters variable 'lp'
     * @throws Exception
     */
    @Test
    public void firstTest () throws Exception {
        lp = em.createQuery("select p from Parameter p" ).getResultList();
        List<Investigation> li = em.createQuery("select distinct d.datafile.dataset.investigation i from " +
                "DatafileParameter d, DatafileParameter d1 WHERE " +

                "d.datafile = d1.datafile " +
                "and (d.parameter =  :p " +
                "and d1.parameter = :p1 )")
                .setParameter("p", lp.get(2))
                .setParameter("p1", lp.get(3))
                .getResultList();
        show(li);
    }
    
    @Test
    public void joinEqualTest () throws Exception {
         List<Investigation> li = em.createQuery("select distinct df.dataset.investigation i from " +
                "Datafile df JOIN df.datafileParameterCollection d, DatafileParameter d1 " +
                "WHERE d.datafile = d1.datafile and " +
                "(d.parameter = :p or d1.parameter = :p1)" +
                "")
                .setParameter("p", lp.get(2))
                .setParameter("p1", lp.get(3))
                .setMaxResults(400).getResultList();
        show(li);
    }


    @Test
    public void equalTest () {
        List<Investigation> li = em.createQuery("select distinct d.datafile.dataset.investigation i from " +
                "DatafileParameter d, DatafileParameter d1 WHERE " +
                "d.datafile = d1.datafile " +
                "and (d.parameter =  :p " +
                "or d1.parameter = :p1 )")
                .setParameter("p", lp.get(2))
                .setParameter("p1", lp.get(3))
                .getResultList();
        show(li);
    }

    @Test
    public void existsTest () throws Exception {
        List<Investigation> li = em.createQuery("select distinct df.dataset.investigation i from Datafile df WHERE " +
                "EXISTS (" +
                "select d from df.datafileParameterCollection d, df.datafileParameterCollection d1 WHERE " +
                "d.parameter = :p or d1.parameter = :p1" +
                ")")
                .setParameter("p", lp.get(2))
                .setParameter("p1", lp.get(3))
                .getResultList();
        show(li);
    }
    

    protected void show(List<Investigation> li) {
        System.out.println("******* Number of results: " + li.size());
        int cont = 1;
        for (Investigation i : li) {
            System.out.println (cont++ + " " + i.getInvNumber() + " " + i.getTitle());
        }
    }

    
}

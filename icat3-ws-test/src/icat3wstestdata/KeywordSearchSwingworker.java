/*
 * DownloadDatafile.java
 *
 * Created on 17-Oct-2007, 15:30:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package icat3wstestdata;

import uk.icat3.client.*;
import icat3wstest.Constants;
import icat3wstest.ICATSingleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.SwingWorker;

/**
 *
 * @author gjd37
 */
public class KeywordSearchSwingworker {

    /** Creates a new instance of DownloadDatafile */
    public KeywordSearchSwingworker() {
    }
    static final ICAT icat = ICATSingleton.getInstance();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        final int[] finished = {0};
        final int[] failed = {0};
        final long start = System.currentTimeMillis();
        try { // Call Web Service Operation

            Collection<SwingWorker> sws = new ArrayList<SwingWorker>();

            for (int i = 0; i < 1; i++) {

                SwingWorker sw = new SwingWorker<String, String>() {
                   
                    @Override
                    public String doInBackground() {
                        long time = System.currentTimeMillis();
                        
                        try {
                            List<String> d = new ArrayList<String>();
                            d.add("hrpd");
                            
                            Collection d2 = icat.searchByKeywords(Constants.SID, d);

                            java.lang.System.out.println("Result = " + d2.size());

                            float totalTime = (System.currentTimeMillis() - time) / 1000f;

                            System.out.println("\nTime taken to search: " + totalTime + " seconds");
                            //time = System.currentTimeMillis(); //reset time

                        } catch (Exception ex) {
                            System.out.println("Error with: " + ex);
                            ex.printStackTrace();
                            failed[0]++;
                        }
                        return "finished";
                    }

                    @Override
                    public void done() {
                        try {
                            get();
                            float totalTime = (System.currentTimeMillis() - start) / 1000f;
                            System.out.println("finished ok " + finished[0]++ + " : Total time: " + totalTime + " seconds, failed " + failed[0]);
                        } catch (Exception ex) {
                            finished[0]++;
                            System.out.println(ex.getMessage());
                        }
                    }
                };

                sws.add(sw);
            }

            //now execute them
            int i = 0;
            Thread.sleep(2000);
            for (SwingWorker swingWorker : sws) {
                Thread.sleep(10);
                System.out.println("Starting" + i++);
                swingWorker.execute();
            }

        } catch (Exception ex) {
            System.out.println(ex);
        // TODO handle custom exceptions here
        }
    }
}


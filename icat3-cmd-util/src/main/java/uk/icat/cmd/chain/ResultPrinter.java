// $Id: ResultPrinter.java 951 2011-08-11 13:18:19Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.chain;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import uk.icat.cmd.entity.State;

public class ResultPrinter extends Command {

	@Override
	public void process(State state) throws Exception {
		Object result = state.getResult();
		System.out.println("Result: ");
		if (result instanceof List<?>){
			List<?> list = (List<?>) result;
			System.out.println("Got list with " + list.size() + " elements:");
			for (Object r : list) {
				printObject(r);
			}
		} else { 
			printObject(result);
		}
	}
	
	void printObject(Object o) {
		if (o instanceof Number || o instanceof String) {
			System.out.println(o);
		} else {
			System.out.println(ToStringBuilder.reflectionToString(o, ToStringStyle.MULTI_LINE_STYLE));
		}
	}

}

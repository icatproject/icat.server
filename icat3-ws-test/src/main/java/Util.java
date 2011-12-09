import uk.icat3.client.DatafileFormat;
import uk.icat3.client.DatafileFormatPK;
import uk.icat3.client.ObjectAlreadyExistsException_Exception;

class Util {
	private static void createDatafileFormat(String name, String formatType) throws Exception {
		DatafileFormatPK dffpk = new DatafileFormatPK();
		dffpk.setName(name);
		dffpk.setVersion("1");
		DatafileFormat dff = new DatafileFormat();
		dff.setDatafileFormatPK(dffpk);
		dff.setFormatType(formatType);
		try {
			Setup.icatEP.create(Setup.sessionId, dff);
		} catch (ObjectAlreadyExistsException_Exception e) {
			System.out.println(e.getMessage());
		}

	}

}
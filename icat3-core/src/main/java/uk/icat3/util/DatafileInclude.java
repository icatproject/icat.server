package uk.icat3.util;

public enum DatafileInclude {

	DATAFILE_PARAMETERS, RELATED_DATAFILES, ALL, NONE;

	public boolean isDatafileParameters() {
		return this == DatafileInclude.DATAFILE_PARAMETERS || this == DatafileInclude.ALL;
	}

	public boolean isRelatedDatafiles() {
		return this == DatafileInclude.RELATED_DATAFILES || this == DatafileInclude.ALL;
	}

}

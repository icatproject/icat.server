package uk.icat3.util;

public enum DatasetInclude {

	DATASET_AND_DATAFILES_ONLY, DATASET_PARAMETERS_ONLY, DATASET_DATAFILES_AND_PARAMETERS, NONE;

	public boolean isDatafiles() {
		return this == DatasetInclude.DATASET_AND_DATAFILES_ONLY
				|| this == DatasetInclude.DATASET_DATAFILES_AND_PARAMETERS;

	}

	public boolean isDatafilesAndParameters() {
		return this == DatasetInclude.DATASET_DATAFILES_AND_PARAMETERS;
	}

	public boolean isDatasetParameters() {
		return this == DatasetInclude.DATASET_PARAMETERS_ONLY
				|| this == DatasetInclude.DATASET_DATAFILES_AND_PARAMETERS;
	}
}

package org.icatproject.exposed.compatibility;

public enum InvestigationInclude {

	INVESTIGATORS_ONLY, KEYWORDS_ONLY, PUBLICATIONS_ONLY, INVESTIGATORS_AND_KEYWORDS, INVESTIGATORS_AND_SHIFTS, 
	INVESTIGATORS_SHIFTS_AND_SAMPLES, INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS, DATASETS_ONLY, 
	DATASETS_AND_DATASET_PARAMETERS_ONLY, DATASETS_AND_DATAFILES, DATASETS_DATAFILES_AND_PARAMETERS, 
	SAMPLES_ONLY, ROLE_ONLY, SHIFT_ONLY, ALL, NONE, ALL_EXCEPT_DATASETS_AND_DATAFILES, 
	ALL_EXCEPT_DATASETS_DATAFILES_AND_ROLES;

	public boolean isDatasets() {
		return this == InvestigationInclude.DATASETS_AND_DATAFILES || this == InvestigationInclude.DATASETS_ONLY
				|| this == InvestigationInclude.DATASETS_AND_DATASET_PARAMETERS_ONLY
				|| this == InvestigationInclude.ALL || this == InvestigationInclude.DATASETS_DATAFILES_AND_PARAMETERS;
	}

	public boolean isDatasetsAndParameters() {
		return this == InvestigationInclude.DATASETS_AND_DATASET_PARAMETERS_ONLY || this == InvestigationInclude.ALL
				|| this == InvestigationInclude.DATASETS_DATAFILES_AND_PARAMETERS;
	}

	public boolean isRoles() {
		return this == InvestigationInclude.ROLE_ONLY || this == InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES
				|| this == InvestigationInclude.ALL;
	}

	public boolean isDatasetsAndDatafiles() {
		return this == InvestigationInclude.DATASETS_AND_DATAFILES || this == InvestigationInclude.ALL
				|| this == InvestigationInclude.DATASETS_DATAFILES_AND_PARAMETERS;
	}

	public boolean isDatasetsDatafilesAndParameters() {
		return this == InvestigationInclude.ALL || this == InvestigationInclude.DATASETS_DATAFILES_AND_PARAMETERS;
	}

	public boolean isInvestigators() {
		return this == InvestigationInclude.INVESTIGATORS_AND_KEYWORDS
				|| this == InvestigationInclude.INVESTIGATORS_ONLY
				|| this == InvestigationInclude.INVESTIGATORS_SHIFTS_AND_SAMPLES
				|| this == InvestigationInclude.INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS
				|| this == InvestigationInclude.INVESTIGATORS_AND_SHIFTS || this == InvestigationInclude.ALL
				|| this == InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES
				|| this == InvestigationInclude.ALL_EXCEPT_DATASETS_DATAFILES_AND_ROLES;
	}

	public boolean isKeywords() {
		return this == InvestigationInclude.INVESTIGATORS_AND_KEYWORDS || this == InvestigationInclude.KEYWORDS_ONLY
				|| this == InvestigationInclude.ALL || this == InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES
				|| this == InvestigationInclude.ALL_EXCEPT_DATASETS_DATAFILES_AND_ROLES;
	}

	public boolean isPublications() {
		return this == InvestigationInclude.PUBLICATIONS_ONLY || this == InvestigationInclude.ALL
				|| this == InvestigationInclude.INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS
				|| this == InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES
				|| this == InvestigationInclude.ALL_EXCEPT_DATASETS_DATAFILES_AND_ROLES;
	}

	public boolean isShifts() {
		return this == InvestigationInclude.SHIFT_ONLY || this == InvestigationInclude.INVESTIGATORS_AND_SHIFTS
				|| this == InvestigationInclude.INVESTIGATORS_SHIFTS_AND_SAMPLES
				|| this == InvestigationInclude.INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS
				|| this == InvestigationInclude.ALL || this == InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES
				|| this == InvestigationInclude.ALL_EXCEPT_DATASETS_DATAFILES_AND_ROLES;
	}

	public boolean isSamples() {
		return this == InvestigationInclude.SAMPLES_ONLY || this == InvestigationInclude.ALL
				|| this == InvestigationInclude.INVESTIGATORS_SHIFTS_AND_SAMPLES
				|| this == InvestigationInclude.INVESTIGATORS_SHIFTS_SAMPLES_AND_PUBLICATIONS
				|| this == InvestigationInclude.ALL_EXCEPT_DATASETS_AND_DATAFILES
				|| this == InvestigationInclude.ALL_EXCEPT_DATASETS_DATAFILES_AND_ROLES;
	}

}

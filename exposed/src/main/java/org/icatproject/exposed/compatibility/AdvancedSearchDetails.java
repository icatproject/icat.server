/*
 * AdvancedSearchDetails.java
 *
 * Created on 21 February 2007, 16:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.icatproject.exposed.compatibility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class AdvancedSearchDetails implements Serializable {

	static Logger log = Logger.getLogger(AdvancedSearchDetails.class);
	/**
	 * Investigation Name
	 */
	private String investigationName; // inv title
	/**
	 * Visit Id
	 */
	private String visitId; // visit id
	/**
	 * Investigation abstract
	 */
	private String investigationAbstract;
	/**
	 * Investigation type
	 */
	private String investigationType;
	// /**
	// * Investigation grant Id
	// */
	// private Long grantId;
	// /**
	// * Investigator Name
	// */
	// private String backCatalogueInvestigatorString; // back catalogue
	// // investigator string
	/**
	 * List of surnames
	 */
	private Collection<String> investigators; // surname
	/**
	 * Investigation number
	 */
	private String experimentNumber; // inv_number
	/**
	 * List of instruments
	 */
	private Collection<String> instruments;
	/**
	 * Long value of start date in datafile parameter
	 */
	private Double runStart; // data file parameter, run_number
								// datafile_parameter
	/**
	 * Long value of start end in datafile parameter
	 */
	private Double runEnd;// data file parameter, run_number datafile_parameter
	/**
	 * Sample name
	 */
	private String sampleName; // sample
	/**
	 * Datafile name
	 */
	private String datafileName; // data file name
	/**
	 * datafile create date
	 */
	private Date dateRangeStart; // (datafile_CREATE_time)
	/**
	 * datafile create date
	 */
	private Date dateRangeEnd;// (datafile_CREATE_time)
	/**
	 * List of keywords
	 */
	private Collection<String> keywords;
	/**
	 * InvestigationInclude in the data returned. {@link InvestigationInclude}
	 */
	private InvestigationInclude investigationInclude;
	/**
     *
     */
	private boolean caseSensitive = true;

	/**
	 * Creates a new instance of AdvancedSearchDetails
	 */
	public AdvancedSearchDetails() {
	}

	public String getInvestigationName() {
		if (investigationName != null && investigationName.length() != 0) {
			String investigationNameFuzzy = investigationName.replace("*", "%");
			if (caseSensitive) {
				return investigationNameFuzzy;
			} else {
				return investigationNameFuzzy.toLowerCase();
			}
		} else {
			return null;
		}
	}

	public void setInvestigationName(String investigationName) {
		this.investigationName = investigationName;
	}

	public Collection<String> getInvestigators() {
		Collection<String> investigatorsChanged = new ArrayList<String>();
		if (investigators == null) {
			return null;
		} else {
			for (String investigator : investigators) {
				String investigatorFuzzy = investigator.replace("*", "%");
				if (caseSensitive) {
					investigatorsChanged.add(investigatorFuzzy);
				} else {
					investigatorsChanged.add(investigatorFuzzy.toLowerCase());
				}
			}
		}
		return investigatorsChanged;
	}

	public void setInvestigators(Collection<String> investigators) {
		this.investigators = investigators;
	}

	public String getExperimentNumber() {
		return experimentNumber;
	}

	public void setExperimentNumber(String experimentNumber) {
		this.experimentNumber = experimentNumber;
	}

	public Double getRunStart() {
		if (runStart == null) {
			return new Double(0);
		} else {
			return runStart;
		}
	}

	public void setRunStart(Double runStart) {
		this.runStart = runStart;
	}

	public Double getRunEnd() {
		if (runEnd == null) {
			return new Double(Integer.MAX_VALUE - 1);
		} else {
			return runEnd;
		}
	}

	public void setRunEnd(Double runEnd) {
		this.runEnd = runEnd;
	}

	public String getSampleName() {
		if (sampleName != null && sampleName.length() != 0) {
			String sampleNameFuzzy = sampleName.replace("*", "%");
			if (caseSensitive) {
				return sampleNameFuzzy;
			} else {
				return sampleNameFuzzy.toLowerCase();
			}
		} else {
			return null;
		}
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	public String getDatafileName() {
		if (datafileName != null && datafileName.length() != 0) {
			String datafileNameFuzzy = datafileName.replace("*", "%");
			if (caseSensitive) {
				return datafileNameFuzzy;
			} else {
				return datafileNameFuzzy.toLowerCase();
			}
		} else {
			return null;
		}
	}

	public void setDatafileName(String datafileName) {
		this.datafileName = datafileName;
	}

	public Date getDateRangeStart() {
		// if null, pass in 1901
		if (dateRangeStart == null) {
			return new Date(0);
		} // 1901/1/1
		else {
			return dateRangeStart;
		}
	}

	public void setDateRangeStart(Date dateRangeStart) {
		this.dateRangeStart = dateRangeStart;
	}

	public Date getDateRangeEnd() {
		// if null, pass in todays date
		if (dateRangeEnd == null) {
			return new Date();
		} else {
			return dateRangeEnd;
		}
	}

	public void setDateRangeEnd(Date dateRangeEnd) {
		this.dateRangeEnd = dateRangeEnd;
	}

	public Collection<String> getKeywords() {
		Collection<String> keywordsChanged = new ArrayList<String>();
		if (keywords == null) {
			return null;
		} else {
			for (String keyword : keywords) {
				String keywordFuzzy = keyword.trim().replace("*", "%");
				if (keywordFuzzy.equals("") || keywordFuzzy.equalsIgnoreCase("AND")) {
					continue;
				} // if empty remove
				if (caseSensitive) {
					keywordsChanged.add(keywordFuzzy);
				} else {
					keywordsChanged.add(keywordFuzzy.toLowerCase());
				}
			}
		}

		return keywordsChanged;
	}

	public void setKeywords(Collection<String> keywords) {
		this.keywords = keywords;
	}

	public Collection<String> getInstruments() {
		return instruments;
	}

	public void setInstruments(Collection<String> instruments) {
		this.instruments = instruments;
	}

	// public String getBackCatalogueInvestigatorString() {
	// if (backCatalogueInvestigatorString != null &&
	// backCatalogueInvestigatorString.length() != 0) {
	// String backCatalogueInvestigatorStringFuzzy =
	// backCatalogueInvestigatorString.replace("*", "%");
	// if (caseSensitive) {
	// return backCatalogueInvestigatorStringFuzzy;
	// } else {
	// return backCatalogueInvestigatorStringFuzzy.toLowerCase();
	// }
	// } else {
	// return null;
	// }
	// }
	//
	// public void setBackCatalogueInvestigatorString(String
	// backCatalogueInvestigatorString) {
	// this.backCatalogueInvestigatorString = backCatalogueInvestigatorString;
	// }

	public String getVisitId() {
		return visitId;
	}

	public void setVisitId(String visitId) {
		this.visitId = visitId;
	}

	public String getInvestigationAbstract() {
		if (investigationAbstract != null && investigationAbstract.length() != 0) {
			String investigationAbstractFuzzy = investigationAbstract.replace("*", "%");
			if (caseSensitive) {
				return investigationAbstractFuzzy;
			} else {
				return investigationAbstractFuzzy.toLowerCase();
			}
		} else {
			return null;
		}
	}

	public void setInvestigationAbstract(String investigationAbstract) {
		this.investigationAbstract = investigationAbstract;
	}

	public String getInvestigationType() {
		return investigationType;
	}

	public void setInvestigationType(String investigationType) {
		this.investigationType = investigationType;
	}

	// public Long getGrantId() {
	// return grantId;
	// }
	//
	// public void setGrantId(Long grantId) {
	// this.grantId = grantId;
	// }

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	// /////////// Util methods for AdvancedSearch creation in
	// InvestigationSearch /////////////////
	public boolean hasOtherParameters() {
		if (investigators != null && investigators.size() != 0) {
			return true;
		}
		if (keywords != null && keywords.size() != 0) {
			return true;
		}
		if (sampleName != null) {
			return true;
		}
		if (datafileName != null) {
			return true;
		}
		if (runEnd != null || runStart != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasInstruments() {
		if (getInstruments() != null && getInstruments().size() != 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasInvestigators() {
		if (getInvestigators() != null && getInvestigators().size() != 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasKeywords() {
		if (getKeywords() != null && getKeywords().size() != 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasAbstract() {
		if (investigationAbstract != null && investigationAbstract.length() != 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasTitle() {
		if (investigationName != null && investigationName.length() != 0) {
			return true;
		} else {
			return false;
		}
	}

	// public boolean hasGrantId() {
	// if (grantId != null) {
	// return true;
	// } else {
	// return false;
	// }
	// }

	public boolean hasVisitId() {
		if (visitId != null && visitId.length() != 0) {
			return true;
		} else {
			return false;
		}
	}

	// public boolean hasBackCatalogueInvestigatorString() {
	// if (backCatalogueInvestigatorString != null &&
	// backCatalogueInvestigatorString.length() != 0) {
	// return true;
	// } else {
	// return false;
	// }
	// }

	public boolean hasInvestigationType() {
		if (investigationType != null && investigationType.length() != 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasExperimentNumber() {
		if (experimentNumber != null && experimentNumber.length() != 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasRunNumber() {
		if (runEnd == null && runStart == null) {
			return false;
		} else if (runEnd != null && runEnd != 0.0) {
			return true;
		} else if (runStart != null && runStart != 0.0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasSample() {
		if (sampleName != null && getSampleName().length() != 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasStartDate() {
		if (dateRangeStart != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasDate() {
		if (dateRangeStart != null) {
			return true;
		} else if (dateRangeEnd != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasEndDate() {
		return (dateRangeEnd == null) ? false : true;
	}

	public boolean hasDataFileParameters() {
		if (datafileName != null) {
			return true;
		} else {
			return false;
		}
	}

	// public boolean hasInvestigationParameters() {
	// if (dateRangeEnd != null || dateRangeStart != null || grantId != null
	// || backCatalogueInvestigatorString != null || experimentNumber != null ||
	// investigationAbstract != null
	// || investigationName != null || investigationType != null || visitId !=
	// null) {
	// return true;
	// } else {
	// return false;
	// }
	//
	// }

	// /////////// End of methods /////////////////
	public InvestigationInclude getInvestigationInclude() {
		if (investigationInclude == null) {
			return InvestigationInclude.NONE;
		}
		return investigationInclude;
	}

	public void setInvestigationInclude(InvestigationInclude investigationInclude) {
		this.investigationInclude = investigationInclude;
	}

	/**
	 * 
	 * NEEDS to be updated since partitioning of ICAT_AUTHORISATION This checks
	 * if the values give are valid in terms of the search on the DB
	 * 
	 * For example, just searching on run number will create a very slow long
	 * search on say ISIS DB (24 million datafile parameters)
	 * 
	 * Tests show, with only Run numbers query takes hours mins run numbers and
	 * instrument 2 secs GOOD! run numbers and start year 7 mins run numbers,
	 * instrument and start year 3 mins run numbers, datafile name 20s GOOD! run
	 * numbers, keyword 35 mins run numbers, sample 34 mins
	 * 
	 * Just year start 7 mins (all years) Just datafile name 57 secs
	 * 
	 * 
	 * start year and instrument 10 secs start year and datafile name 4 secs
	 * start year and keywords 7 mins start year and sample 2mins OK!
	 * 
	 * New Tests (returning 100) ==========================
	 * 
	 * start year and sample 18 secs start year and keywords 49 secs start year
	 * 42 sec run number 30 secs run number instrument 15 secs run number
	 * keywords 3 secs run number end time 2 secs run number all time 1 secs
	 * datafilename 24s secs
	 * 
	 */
	public boolean isValid() {
		// log.trace(" "+hasRunNumber() +"  "+runEnd);
		/*
		 * if(hasRunNumber()){ //so they have set run number, check something on
		 * investigation is set) if(hasInvestigationParameters() ||
		 * getDatafileName() != null || hasInstruments()) return true; else
		 * throw new IllegalArgumentException(
		 * "Must search investigation information, instruments or datafile name if searching with run numbers"
		 * ); } else if(dateRangeEnd != null || dateRangeStart != null){ //got
		 * to here run number is not set to check all others
		 * if(hasInvestigationParameters() || getDatafileName() != null ||
		 * hasInstruments() || hasSample()) return true; else throw new
		 * IllegalArgumentException(
		 * "Must search investigation information, instruments, sample name or datafile name if searching with datafile date ranges"
		 * ); } else
		 */return true;

	}
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.icat3.search;

import java.util.Collection;
import uk.icat3.util.InvestigationInclude;
import uk.icat3.util.LogicalOperator;

/**
 *
 * @author gjd37
 */
public class KeywordDetails {

    private Collection<String> keywords;
    private InvestigationInclude investigationIncludes = InvestigationInclude.NONE;
    private boolean caseSensitve = true;
   
    private LogicalOperator operator = LogicalOperator.AND;

    public boolean isCaseSensitve() {
        return caseSensitve;
    }

    public void setCaseSensitve(boolean caseSensitve) {
        this.caseSensitve = caseSensitve;
    }
    
    public InvestigationInclude getInvestigationIncludes() {
        return investigationIncludes;
    }

    public void setInvestigationIncludes(InvestigationInclude investigationIncludes) {
        this.investigationIncludes = investigationIncludes;
    }

    public Collection<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Collection<String> keywords) {
        this.keywords = keywords;
    }

    public LogicalOperator getOperator() {
        return operator;
    }

    public void setOperator(LogicalOperator operator) {
        this.operator = operator;
    }
    
    
}

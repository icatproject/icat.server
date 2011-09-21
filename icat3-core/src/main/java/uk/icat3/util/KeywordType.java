/*
 * KeywordType.java
 *
 * Created on 13 February 2007, 09:43
 */

package uk.icat3.util;
/**
 * Type of keyword search
 *
 * @author gjd37
 */
public enum KeywordType {
    /**
     * All keywords
     */
    ALL,
    /**
     * Consisting of or using letters, numbers, punctuation marks, and mathematical and other conventional symbols
     */
    ALPHA_NUMERIC,
    /**
     * Strings only, [a-zA-Z]
     */
    ALPHA;
}

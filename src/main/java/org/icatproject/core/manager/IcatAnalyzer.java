package org.icatproject.core.manager;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class IcatAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer source = new StandardTokenizer();
		TokenStream sink = new StandardFilter(source);
		sink = new EnglishPossessiveFilter(sink);
		sink = new LowerCaseFilter(sink);
		sink = new StopFilter(sink, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		sink = new PorterStemFilter(sink);
		return new TokenStreamComponents(source, sink);
	}
}
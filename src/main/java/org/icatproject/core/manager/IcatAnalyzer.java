package org.icatproject.core.manager;

import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

public class IcatAnalyzer extends StopwordAnalyzerBase {

	public IcatAnalyzer() {
		super(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		Tokenizer source = new StandardTokenizer(reader);
		TokenStream sink = new StandardFilter(source);
		sink = new EnglishPossessiveFilter(Version.LATEST, sink);
		sink = new LowerCaseFilter(sink);
		sink = new StopFilter(sink, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		sink = new PorterStemFilter(sink);
		return new TokenStreamComponents(source, sink);
	}

}
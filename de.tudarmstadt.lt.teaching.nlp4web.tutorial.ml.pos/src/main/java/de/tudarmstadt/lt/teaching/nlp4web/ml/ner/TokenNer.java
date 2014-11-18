package de.tudarmstadt.lt.teaching.nlp4web.ml.ner;

import org.apache.uima.jcas.JCas;
import org.cleartk.ne.type.NamedEntityMention;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TokenNer extends Token {

	private NamedEntityMention ner;

	public TokenNer(JCas jcas, int begin, int end) {
		super(jcas, begin, end);
	}
	
	public NamedEntityMention getNer() {
		return ner;
	}

	public void setNer(NamedEntityMention ner) {
		this.ner = ner;
	}
	
	
}

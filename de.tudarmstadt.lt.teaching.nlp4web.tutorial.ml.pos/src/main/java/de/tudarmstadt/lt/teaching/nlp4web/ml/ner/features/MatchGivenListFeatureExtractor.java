package de.tudarmstadt.lt.teaching.nlp4web.ml.ner.features;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.NamedFeatureExtractor1;

import de.tudarmstadt.lt.teaching.nlp4web.ml.ner.NERAnnotator;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class MatchGivenListFeatureExtractor implements NamedFeatureExtractor1<Token> {

	public HashMap<String, String> getEntityList(){
		HashMap<String, String> nerValueMap = new HashMap<String, String>();
		try {
			String f = FileUtils.readFileToString(new File("src/main/resources/ner/eng.list"));
			String[] lines = f.split("(\r\n|\n)");
			for (String line : lines){
				String[] elements = line.split(" ");
				if (elements.length == 2){
					// get (IOB, Value) for NER of 1 word 
					nerValueMap.put(elements[0], elements[1]);
				}
				// TODO Get NER bigger than 1 word
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nerValueMap;	
	}
	
	@Override
	public List<Feature> extract(JCas jcas, Token token)
			throws CleartkExtractorException {
		HashMap<String, String> nerValueMap = getEntityList();
		boolean isListed = nerValueMap.containsValue(token.getCoveredText());
		return null; //Collections.;
	}

	@Override
	public String getFeatureName() {
		// TODO Auto-generated method stub
		return null;
	}

	

	
}

package de.tudarmstadt.lt.teaching.nlp4web.ml.ner.features;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.NamedFeatureExtractor1;
import org.cleartk.ml.feature.function.FeatureFunction;

import de.tudarmstadt.lt.teaching.nlp4web.ml.ner.NERAnnotator;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class MatchGivenListFeatureExtractor implements NamedFeatureExtractor1<Token> {

	private HashMap<String, String> nerValueMap;
	
	public MatchGivenListFeatureExtractor() {
		super();
		initializeEntityList();
		
	}
	private void initializeEntityList() {
		nerValueMap = new HashMap<String, String>();
		try {
			String f = FileUtils.readFileToString(new File("src/main/resources/ner/eng.list"));
			String[] lines = f.split("(\r\n|\n)");
			for (String line : lines){
				String[] elements = line.split(" ");
				if (elements.length == 2){
					// get (Value, IOB) for NER of 1 word 
					nerValueMap.put(elements[1].toLowerCase(), elements[0]);
				}
				// TODO Get NER bigger than 1 word
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public HashMap<String, String> getEntityList(){
		return nerValueMap;	
	}
	
	@Override
	public List<Feature> extract(JCas jcas, Token token)
			throws CleartkExtractorException {
		HashMap<String, String> nerValueMap = getEntityList();
		boolean isListed = nerValueMap.containsKey(token.getCoveredText().toLowerCase());
		if (isListed){
			return Collections.singletonList(new Feature("MatchList" , nerValueMap.get(token.getCoveredText().toLowerCase())+"_"+token.getCoveredText() ));			
		} else {
			return Collections.singletonList(new Feature("MatchList" , "O_"+token.getCoveredText() ));		}
	}

	@Override
	public String getFeatureName() {
		return "MatchList";
	}

	

	
}

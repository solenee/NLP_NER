package de.tudarmstadt.lt.teaching.nlp4web.ml.ner.features;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

/** 
 * 
 * Should be used with CoveredTextExtractor as Extractor.
 *
 */
public class MatchGivenListFeatureFunction implements FeatureFunction {

	public static final String DEFAULT_NAME = "MatchList"; 
	
	// TODO have a different list for each IOB type and generated eventually many features ?
	// To cover the case where a name can be ORG and PER e.g. 
	private HashMap<String, String> nerValueMap;
	
	public MatchGivenListFeatureFunction() {
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
	public List<Feature> apply(Feature input) {
		Object featureValue = input.getValue();
		if (featureValue instanceof String) {

			HashMap<String, String> nerValueMap = getEntityList();
			String tokenText = (String) featureValue;
			String normalizedValue = tokenText.toLowerCase();
			boolean isListed = nerValueMap.containsKey(normalizedValue);
			if (isListed) {
				List<Feature> result = new ArrayList<Feature>();
				result.add(new Feature(DEFAULT_NAME,
						nerValueMap.get(normalizedValue) + "_"+tokenText)
						);
				result.add(new Feature(DEFAULT_NAME+"_LowerCase",
						nerValueMap.get(normalizedValue) + "_"+normalizedValue)
						);
				return result;
			} else {
				return Collections.emptyList();
			}
		} else {
			return Collections.emptyList();
		}
	}
	

	
}

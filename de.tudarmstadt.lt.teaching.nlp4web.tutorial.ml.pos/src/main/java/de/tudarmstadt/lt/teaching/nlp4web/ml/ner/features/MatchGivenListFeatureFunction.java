package de.tudarmstadt.lt.teaching.nlp4web.ml.ner.features;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

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
	public static final String LOC = "LOC";
	public static final String MISC = "MISC";
	public static final String PER = "PER";
	public static final String ORG = "ORG";
	
	// TODO have a different list for each IOB type and generated eventually many features ?
	// To cover the case where a name can be ORG and PER e.g.
	private HashMap<String, String> nerValueMap;
	private List<String> locValueMap;
	private List<String> miscValueMap;
	private List<String> perValueMap;
	private List<String> orgValueMap;
	
	public MatchGivenListFeatureFunction() {
		super();
		//initializeEntityList();
		initializeLists();
	}

	private void initializeLists() {
		//System.out.println("Lists initialization ...");
		locValueMap = new ArrayList<String>() ;
		miscValueMap = new ArrayList<String>() ;
		perValueMap = new ArrayList<String>() ;
		orgValueMap = new ArrayList<String>() ;
		try {
			String f = FileUtils.readFileToString(new File("src/main/resources/ner/eng.list"));
			String[] lines = f.split("(\r\n|\n)");
			for (String line : lines){
				String[] elements = line.split(" ");
				String value = line.substring(elements[0].length()+1);
				if (elements[0].equals(LOC)) {
					locValueMap.add(value);
				}
				if (elements[0].equals(MISC)) {
					miscValueMap.add(value);
				}
				if (elements[0].equals(PER)) {
					perValueMap.add(value);
				}
				if (elements[0].equals(ORG)) {
					orgValueMap.add(value);
				}
			}
			//System.out.println("Lists initialized");
		} catch (IOException e) {
			e.printStackTrace();
		}

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
					nerValueMap.put(elements[1], elements[0]);
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

			/*HashMap<String, String> nerValueMap = getEntityList();
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
			}*/
			String tokenText = (String) featureValue; 
			String normalizedValue = tokenText.toLowerCase().replaceAll("\\.", "\".\"");
			List<Feature> result = new ArrayList<Feature>();

//			if (normalizedValue.matches(".*\\(.*") || normalizedValue.matches(".*\\).*")) {
//				// skip
//			} else {
				String regex = "(.+ )*" + "\\Q" + normalizedValue + "\\E"
						+ "( .+)*";
				// System.out.println("regex = " +regex);
				for (String elt : locValueMap) {
					String feat = "(" + elt.replaceAll(" ", "_") + ")";
					if (Pattern.matches(regex, elt.toLowerCase())) {
						result.add(new Feature(DEFAULT_NAME + "_LowerCase", LOC
								+ "_"+feat));
						//System.out.println(elt + "-> "+normalizedValue);
					}
				}
				for (String elt : miscValueMap) {
					String feat = "(" + elt.replaceAll(" ", "_") + ")";
					if (Pattern.matches(regex, elt.toLowerCase())) {
						result.add(new Feature(DEFAULT_NAME + "_LowerCase",
								MISC + "_"+feat));
						//System.out.println(elt + "-> "+normalizedValue);
					}
				}
				for (String elt : perValueMap) {
					String feat = "(" + elt.replaceAll(" ", "_") + ")";
					if (Pattern.matches(regex, elt.toLowerCase())) {
						result.add(new Feature(DEFAULT_NAME + "_LowerCase", PER
								+ "_"+feat));
						// System.out.println(elt + "-> "+normalizedValue);
					}
				}
				for (String elt : orgValueMap) {
					String feat = "(" + elt.replaceAll(" ", "_") + ")";
					if (Pattern.matches(regex, elt.toLowerCase())) {
						result.add(new Feature(DEFAULT_NAME + "_LowerCase", ORG
								+ "_"+feat));
						// System.out.println(elt + "-> "+normalizedValue);
					}
				}
			//}
			return result;
		} else {
			return Collections.emptyList();
		}
	}

}

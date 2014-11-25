package de.tudarmstadt.lt.teaching.nlp4web.ml.ner;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.ml.feature.extractor.TypePathExtractor;
/*import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;*/
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * This class analaysis the classification result and prints the precision and
 * recall for each pos tag and prints the average precision and recall for all
 * pos tags and the accuracy
 *
 *
 */
public class AnalyzeFeatures extends JCasAnnotator_ImplBase {
	public static final String PARAM_TOKEN_VALUE_PATH = "TokenValuePath";
	public static final String PARAM_INPUT_FILE = "InputFile";
	/**
	 * To make this class general, the path to the feature that is used for the
	 * evaluation the tokenValuePath has to be set to the feature e.g. for the
	 * pos value: pos/PosValue is used (works only for token:
	 * de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token)
	 */
	@ConfigurationParameter(name = PARAM_TOKEN_VALUE_PATH, mandatory = true)
	private String tokenValuePath;
	@ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
	private String inputFile;
	Logger logger = UIMAFramework.getLogger(AnalyzeFeatures.class);

	private class Classification {
		int fp = 0;
		int tp = 0;
		int fn = 0;
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		try {
			logger.log(Level.INFO, "Start analyzing results");

			HashMap<String, Classification> map = new HashMap<String, Classification>();
			TypePathExtractor<Token> extractor;
			extractor = new TypePathExtractor<Token>(Token.class,
					tokenValuePath);
			String line;
			String[] splitLine;
			BufferedReader reader = new BufferedReader(
					new FileReader(inputFile));

			// Ecrire dans fichier
			FileWriter fw = new FileWriter("src/test/resources/results/ner_eng.txt");
			BufferedWriter output = new BufferedWriter(fw);
			
			int correct = 0;
			int tokenCount = 0;
			// Consum -DOCSTART- -X- -X- O
			//line = reader.readLine();
			//System.out.println("1st line consumed : " + line);
			
			for (Sentence sentence : select(jCas, Sentence.class)) {
				line = reader.readLine();
				output.write("\n");
				// System.out.println("line begin sentence : " + line);
				// System.out.println("sentence : " + sentence.getCoveredText());
				List<Token> tokens = selectCovered(jCas, Token.class, sentence);
				for (Token token : tokens) {
					//System.out.println("token :"+token.getCoveredText());
					line = reader.readLine();
					//System.out.println("line : " + line);
					splitLine = line.split("\\s");
					String trueValue = splitLine[3];
					//System.out.println("trueValue :"+trueValue);
					// System.out.println("ok :)");
//					String classifiedValue = extractor.extract(jCas, token)
//							.get(0).getValue().toString();
					 List<NamedEntity> ner = selectCovered(jCas, NamedEntity.class, token);
					 String classifiedValue =  ner.get(0).getValue();
					 //System.out.println("classifiedValue :"+classifiedValue);

					 output.write(line + " " + classifiedValue + "\n");
					 
					if (splitLine[0].equals(token.getCoveredText())) {
						if (trueValue.equals(classifiedValue)) {
							correct++;

							if (!map.containsKey(trueValue)) {
								map.put(trueValue, new Classification());
							}
							map.get(trueValue).tp++;
						} else {
							if (!map.containsKey(trueValue)) {
								map.put(trueValue, new Classification());
							}
							if (!map.containsKey(classifiedValue)) {
								map.put(classifiedValue, new Classification());
							}
							map.get(trueValue).fn++;
							map.get(classifiedValue).fp++;
						}
						tokenCount++;
					} else {
						logger.log(
								Level.WARNING,
								"Token of predicting file does not match to text ("
										+ splitLine[0] + "!="
										+ token.getCoveredText() + ")");
					}
					if (!trueValue.equals(classifiedValue)){
						logger.log(
								Level.WARNING,
								"=================================== \n ClassifiedValue does not match to TrueValue ("
										+ classifiedValue + "!="
										+ trueValue + ") for token : " + token.getCoveredText() + " \n in sentence " + sentence.getCoveredText() + "\n ================================");						
					}
				}
			}
			reader.close();
			
			double precisionSum = 0.0;
			double recallSum = 0.0;
			double fmeasureSum = 0.0;
			logger.log(Level.INFO, "NER-Tag\tprecision\trecall\tF-measure");
			for (Entry<String, Classification> e : map.entrySet()) {
				double precision = 0.0;
				double recall = 0.0;
				double fmeasure = 0.0;
				double tp = e.getValue().tp;
				double fp = e.getValue().fp;
				double fn = e.getValue().fn;
				if (e.getValue().tp > 0.0) {
					precision = 1.0 * tp / (tp + fp);
					recall = 1.0 * tp / (tp + fn);
					fmeasure = 2.0 * precision * recall / (precision + recall);
					recallSum += recall;
					precisionSum += precision;
					fmeasureSum += fmeasure;
				}

				logger.log(Level.INFO, e.getKey() + "\t" + precision + "\t"
						+ recall + "\t" + fmeasure);
			}
			logger.log(Level.INFO, "Accuracy: \t"
					+ (1.0 * correct / tokenCount));
			logger.log(Level.INFO, "Precision:\t" + (precisionSum / map.size()));
			logger.log(Level.INFO, "Recall:   \t" + (recallSum / map.size()));
			logger.log(Level.INFO, "F-Measure:\t" + (fmeasureSum / map.size()));

			output.flush();
			output.close();
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, e.getMessage());
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage());
		}
	}
}

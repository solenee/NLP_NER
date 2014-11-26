package de.tudarmstadt.lt.teaching.nlp4web.ml.ner;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
/*import org.cleartk.classifier.CleartkSequenceAnnotator;
 import org.cleartk.classifier.Instance;
 import org.cleartk.classifier.feature.extractor.CleartkExtractor;
 import org.cleartk.classifier.feature.extractor.CleartkExtractor.Following;
 import org.cleartk.classifier.feature.extractor.CleartkExtractor.Preceding;
 import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
 import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
 import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
 import org.cleartk.classifier.feature.function.CapitalTypeFeatureFunction;
 import org.cleartk.classifier.feature.function.CharacterNGramFeatureFunction;
 import org.cleartk.classifier.feature.function.FeatureFunctionExtractor;
 import org.cleartk.classifier.feature.function.LowerCaseFeatureFunction;
 import org.cleartk.classifier.feature.function.NumericTypeFeatureFunction;*/
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Following;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.extractor.TypePathExtractor;
import org.cleartk.ml.feature.extractor.WhiteSpaceExtractor;
import org.cleartk.ml.feature.function.CapitalTypeFeatureFunction;
import org.cleartk.ml.feature.function.CharacterNgramFeatureFunction;
import org.cleartk.ml.feature.function.CharacterNgramFeatureFunction.Orientation;
import org.cleartk.ml.feature.function.FeatureFunctionExtractor;
import org.cleartk.ml.feature.function.LowerCaseFeatureFunction;
import org.cleartk.ml.feature.function.NumericTypeFeatureFunction;

import com.thoughtworks.xstream.XStream;

import de.tudarmstadt.lt.teaching.nlp4web.ml.ner.features.ChunkValueExtractor;
import de.tudarmstadt.lt.teaching.nlp4web.ml.ner.features.ChunkPOSValueExtractor;
import de.tudarmstadt.lt.teaching.nlp4web.ml.ner.features.MatchGivenListFeatureFunction;
import de.tudarmstadt.lt.teaching.nlp4web.ml.xml.XStreamFactory;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

public class NERAnnotator
    extends CleartkSequenceAnnotator<String>
{

    public static final String PARAM_FEATURE_EXTRACTION_FILE = "FeatureExtractionFile";
    public static final String PARAM_CLEARTK_EXTRACTION_FILE = "CleartkExtractionFile";

    /**
     * if a feature extraction/context extractor filename is given the xml file is parsed and the
     * features are used, otherwise it will not be used
     */
    @ConfigurationParameter(name = PARAM_FEATURE_EXTRACTION_FILE, mandatory = false)
    private String featureExtractionFile = null;
    @ConfigurationParameter(name = PARAM_CLEARTK_EXTRACTION_FILE, mandatory = false)
    private String cleartkExtractionFile = null;

    private FeatureExtractor1<Token> tokenFeatureExtractor;
//    private FeatureExtractor1<Token> databaseFeatureExtractor; // replaced by a function in tokenFeatureExtractor
    private CleartkExtractor<Token, POS> posFeatureExtractor;
    private FeatureExtractor1<Token> otherAnnotationsExtractor;
    private CleartkExtractor<Chunk, Chunk> chunkContextFeatureExtractor;
    private CleartkExtractor<Token, Token> contextFeatureExtractor;
//    private TypePathExtractor<Token> stemExtractor; // not useful for english

    @SuppressWarnings("unchecked")
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);

		// add feature extractors
		if (featureExtractionFile == null) {

			// Features with token covered text
			this.tokenFeatureExtractor = new FeatureFunctionExtractor<Token>(
					new CoveredTextExtractor<Token>(),
					new CapitalTypeFeatureFunction(),
					new LowerCaseFeatureFunction(),
					new MatchGivenListFeatureFunction());

			// Replaced by a function in tokenFeatureExtractor
			// this.databaseFeatureExtractor = new
			// FeatureFunctionExtractor<Token>(
			// new MatchGivenListFeatureExtractor(), new
			// CapitalTypeFeatureFunction(),
			// new LowerCaseFeatureFunction()
			// );

			// TODO Serialize this extractor
			// Given Chunk and POS value for each Token as a feature
			this.otherAnnotationsExtractor = new FeatureFunctionExtractor<Token>(
					new ChunkPOSValueExtractor());
		} else {
			// load the settings from a file
			// initialize the XStream if a xml file is given:
			XStream xstream = XStreamFactory.createXStream();
			List<FeatureExtractor1<Token>> tokenFeats = (List<FeatureExtractor1<Token>>) xstream
					.fromXML(new File(featureExtractionFile));
			System.out.println("tokenFeats.size() = " + tokenFeats.size());
			tokenFeatureExtractor = tokenFeats.get(0);
			otherAnnotationsExtractor = tokenFeats.get(1);
		}

		// add cleartk extractors
		if (cleartkExtractionFile == null) {
			this.contextFeatureExtractor = new CleartkExtractor<Token, Token>(
					Token.class, new CoveredTextExtractor<Token>(),
					new Preceding(3), new Following(3));
		} else {
			// load the settings from a file
			// initialize the XStream if a xml file is given:
			XStream xstream = XStreamFactory.createXStream();
			List<CleartkExtractor<Token, Token>> contextFeats = (List<CleartkExtractor<Token, Token>>) xstream
					.fromXML(new File(cleartkExtractionFile));
			System.out.println("contextFeats.size() = " + contextFeats.size());
			this.contextFeatureExtractor = contextFeats.get(0);
		}

		// TODO Serialize these extractors
		// Context features with Chunk
		this.chunkContextFeatureExtractor = new CleartkExtractor<Chunk, Chunk>(
				Chunk.class, new ChunkValueExtractor(), new Preceding(2),
				new Following(2));
		// Context features with POS
		this.posFeatureExtractor = new CleartkExtractor<Token, POS>(POS.class,
				new TypePathExtractor<POS>(POS.class, "pos/PosValue"),
				new Preceding(2), new Following(2));

	}

    @Override
    public void process(JCas jCas)
        throws AnalysisEngineProcessException
    {
        for (Sentence sentence : select(jCas, Sentence.class)) {
            List<Instance<String>> instances = new ArrayList<Instance<String>>();
            List<Token> tokens = selectCovered(jCas, Token.class, sentence);
                     
            for (Token token : tokens) {

                Instance<String> instance = new Instance<String>();

                instance.addAll(tokenFeatureExtractor.extract(jCas, token));
                instance.addAll(otherAnnotationsExtractor.extract(jCas, token));
//                instance.addAll(databaseFeatureExtractor.extract(jCas, token));
                instance.addAll(contextFeatureExtractor.extractWithin(jCas, token, sentence));
                instance.addAll(posFeatureExtractor.extractWithin(jCas, token, sentence));
//                instance.addAll(stemExtractor.extract(jCas, token));
                List<Chunk> chunks = selectCovered(jCas, Chunk.class, token);
                if (chunks.size() != 1) {
                	System.err.println("Oups : chunks.size() = "+chunks.size());
                } else {
                	 instance.addAll(chunkContextFeatureExtractor.extractWithin(jCas, chunks.get(0), sentence));
                }
                
                List<NamedEntity> namedEntity = selectCovered(jCas, NamedEntity.class, token);
                if (namedEntity.size() != 1) {
                	System.err.println("Waaaaaaaa : namedEntity.size() = "+namedEntity.size());
                } else {
                	instance.setOutcome(namedEntity.get(0).getValue());
                }

                // add the instance to the list !!!
                instances.add(instance);
            }
            // differentiate between training and classifying
            if (this.isTraining()) {
                this.dataWriter.write(instances);
            }
            else {
            	// get the predicted IOB outcome labels from the classifier
                List<String> outcomes = this.classify(instances);
                int i = 0;
                for (Token token : tokens) {
                	// update NamedEntity value with the guessed one 
                	List<NamedEntity> ner = selectCovered(jCas, NamedEntity.class, token);
                    ner.get(0).setValue(outcomes.get(i++));
                }
            }
        }

    }

}

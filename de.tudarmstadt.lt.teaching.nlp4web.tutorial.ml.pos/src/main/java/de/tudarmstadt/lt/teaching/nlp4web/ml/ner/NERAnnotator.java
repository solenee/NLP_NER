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
import org.cleartk.ml.chunking.BioChunking;
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
import org.cleartk.ne.type.NamedEntityMention;

import com.thoughtworks.xstream.XStream;

import de.tudarmstadt.lt.teaching.nlp4web.ml.ner.features.ChunkExtractor;
import de.tudarmstadt.lt.teaching.nlp4web.ml.ner.features.MatchGivenListFeatureExtractor;
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

    /**
     * if a feature extraction/context extractor filename is given the xml file is parsed and the
     * features are used, otherwise it will not be used
     */
    @ConfigurationParameter(name = PARAM_FEATURE_EXTRACTION_FILE, mandatory = false)
    private String featureExtractionFile = null;

    private FeatureExtractor1<Token> tokenFeatureExtractor;
    private FeatureExtractor1<Token> databaseFeatureExtractor;
    private CleartkExtractor<Token, POS> posFeatureExtractor;
    private FeatureFunctionExtractor<Token> chunkFeatureExtractor;
    private CleartkExtractor<Token, Token> contextFeatureExtractor;
    private TypePathExtractor<Token> stemExtractor;
    
	private BioChunking<Token, NamedEntityMention> chunking = new BioChunking<Token, NamedEntityMention>(
	        Token.class,
	        NamedEntityMention.class,
	        "mentionType");

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        // add feature extractors
        if (featureExtractionFile == null) {
            CharacterNgramFeatureFunction.Orientation fromRight = Orientation.RIGHT_TO_LEFT;

            stemExtractor = new TypePathExtractor<Token>(Token.class, "stem/value");

            this.tokenFeatureExtractor = new FeatureFunctionExtractor<Token>(
            		  new CoveredTextExtractor<Token>(), new CapitalTypeFeatureFunction(),
            		  new LowerCaseFeatureFunction()
//                    new CapitalTypeFeatureFunction(), new NumericTypeFeatureFunction(),
//            		  new CharacterNgramFeatureFunction(fromRight, 0, 2)
            		  );
            // add there
            // NP & begins with a capital letter
                        
			this.databaseFeatureExtractor = new FeatureFunctionExtractor<Token>(
					new MatchGivenListFeatureExtractor(), new CapitalTypeFeatureFunction(),
          		  new LowerCaseFeatureFunction()
					);
            this.contextFeatureExtractor = new CleartkExtractor<Token, Token>(Token.class,
                    new CoveredTextExtractor<Token>(), new Preceding(3), new Following(3));
            this.posFeatureExtractor = new CleartkExtractor<Token, POS>(POS.class,
                    new TypePathExtractor<POS>(POS.class, "pos/PosValue"),
                    new Preceding(2), new Following(2));
            this.chunkFeatureExtractor = new FeatureFunctionExtractor<Token>(
                    new ChunkExtractor());
        }
        else {// load the settings from a file
              // initialize the XStream if a xml file is given:
            XStream xstream = XStreamFactory.createXStream();
            tokenFeatureExtractor = (FeatureExtractor1<Token>) xstream.fromXML(new File(
                    featureExtractionFile));
        }

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
                instance.addAll(databaseFeatureExtractor.extract(jCas, token));
                instance.addAll(contextFeatureExtractor.extractWithin(jCas, token, sentence));
                instance.addAll(posFeatureExtractor.extractWithin(jCas, token, sentence));
                instance.addAll(chunkFeatureExtractor.extract(jCas, token));
                instance.addAll(stemExtractor.extract(jCas, token));

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
            	// get the predicted BIO outcome labels from the classifier
                List<String> outcomes = this.classify(instances);
               	// create the NamedEntityMention annotations in the CAS
                // List<NamedEntityMention> chunks = this.chunking.createChunks(jCas, tokens, outcomesTags);
                int i = 0;
                for (Token token : tokens) {
//                    NamedEntity ner = new NamedEntity(jCas, token.getBegin(), token.getEnd());
//                    ner.setValue(outcomes.get(i++));
                	// update NamedEntity value with the guessed one 
                	List<NamedEntity> ner = selectCovered(jCas, NamedEntity.class, token);
                    ner.get(0).setValue(outcomes.get(i++));
                }
            }
        }

    }

}

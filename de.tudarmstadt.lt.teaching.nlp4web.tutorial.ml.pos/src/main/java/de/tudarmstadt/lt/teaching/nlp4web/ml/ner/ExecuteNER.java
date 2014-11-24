package de.tudarmstadt.lt.teaching.nlp4web.ml.ner;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
/*import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;*/
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.crfsuite.CrfSuiteStringOutcomeDataWriter;
import org.cleartk.ml.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.mallet.MalletCrfStringOutcomeDataWriter;
import org.cleartk.util.cr.FilesCollectionReader;

import de.tudarmstadt.lt.teaching.nlp4web.ml.reader.ConllAnnotator;
import de.tudarmstadt.ukp.dkpro.core.snowball.SnowballStemmer;

public class ExecuteNER {

	public static void writeModel(File nerTagFile, String modelDirectory, String language)
			throws ResourceInitializationException, UIMAException, IOException {

		runPipeline(
				FilesCollectionReader.getCollectionReaderWithSuffixes(
						nerTagFile.getAbsolutePath(),
						ConllAnnotator.CONLL_VIEW, nerTagFile.getName()),
						createEngine(ConllAnnotator.class),
						createEngine(SnowballStemmer.class,
						SnowballStemmer.PARAM_LANGUAGE, language),
						createEngine(
						NERAnnotator.class,
						CleartkSequenceAnnotator.PARAM_IS_TRAINING,true,
						DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, modelDirectory,
						DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
						//MalletCrfStringOutcomeDataWriter.class)); // or 
						CrfSuiteStringOutcomeDataWriter.class));
	}

	public static void trainModel(String modelDirectory) throws Exception {
	    org.cleartk.ml.jar.Train.main(modelDirectory);

	}

	public static void classifyTestFile(String modelDirectory, File testNerFile, String language) throws ResourceInitializationException, UIMAException, IOException {
		runPipeline(FilesCollectionReader.getCollectionReaderWithSuffixes(
				testNerFile.getAbsolutePath(),
				ConllAnnotator.CONLL_VIEW, testNerFile.getName()),
				createEngine(ConllAnnotator.class),
				createEngine(SnowballStemmer.class,
						SnowballStemmer.PARAM_LANGUAGE, language),
				createEngine(NERAnnotator.class,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, 	modelDirectory+"model.jar"),
				createEngine(AnalyzeFeatures.class,
						AnalyzeFeatures.PARAM_INPUT_FILE, testNerFile.getAbsolutePath(),
						AnalyzeFeatures.PARAM_TOKEN_VALUE_PATH,"pos/PosValue")
			);
	}

	public static void main(String[] args) throws Exception {

		long start = System.currentTimeMillis();
		String modelDirectory = "src/test/resources/model/";
		String language = "en";
		//File posTagFile=   new File("src/main/resources/pos/wsj_pos.train_100");
		File nerTagFile=   new File("src/main/resources/ner/ner_eng.train");
		//File testPosFile = new File("src/main/resources/pos/wsj_pos.dev");
		File testNerFile = new File("src/main/resources/ner/ner_eng.dev");
		new File(modelDirectory).mkdirs();
		writeModel(nerTagFile, modelDirectory,language);
		trainModel(modelDirectory);
		classifyTestFile(modelDirectory, testNerFile,language);
		long now = System.currentTimeMillis();
		UIMAFramework.getLogger().log(Level.INFO,"Time: "+(now-start)+"ms");
	}
}

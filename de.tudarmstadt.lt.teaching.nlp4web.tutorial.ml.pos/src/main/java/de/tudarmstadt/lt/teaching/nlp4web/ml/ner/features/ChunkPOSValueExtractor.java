package de.tudarmstadt.lt.teaching.nlp4web.ml.ner.features;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.NamedFeatureExtractor1;
import org.cleartk.ml.feature.function.FeatureFunction;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

public class ChunkPOSValueExtractor implements NamedFeatureExtractor1<Token> {
	
	@Override
	public List<Feature> extract(JCas jCas, Token focusAnnotation)
			throws CleartkExtractorException {
		List<Feature> result = new ArrayList<Feature>();
		result.add(new Feature("POSValue", focusAnnotation.getPos()
				.getPosValue()));
		List<Chunk> chunks = selectCovered(jCas, Chunk.class, focusAnnotation);
		if (chunks.size() != 1) {
			System.err.println("Oups : chunks.size() = " + chunks.size());
		} else {
			result.add(new Feature("ChunkValue", chunks.get(0).getChunkValue()));
		}
		return result;
	}
	
	@Override
	public String getFeatureName() {
		return "ChunkPOSValueExtractor";
	}

}

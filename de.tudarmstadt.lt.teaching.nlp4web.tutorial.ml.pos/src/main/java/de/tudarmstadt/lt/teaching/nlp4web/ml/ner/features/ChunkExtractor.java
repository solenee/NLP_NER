package de.tudarmstadt.lt.teaching.nlp4web.ml.ner.features;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.NamedFeatureExtractor1;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

public class ChunkExtractor implements NamedFeatureExtractor1<Token> {

	@Override
	public List<Feature> extract(JCas view, Token focusAnnotation)
			throws CleartkExtractorException {
		List<Chunk> chunks = selectCovered(view, Chunk.class, focusAnnotation);
		if (chunks.size() == 1) {
			return Collections.singletonList(new Feature(getFeatureName(),
					chunks.get(0)));
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public String getFeatureName() {
		return "ChunkExtractor";
	}

}

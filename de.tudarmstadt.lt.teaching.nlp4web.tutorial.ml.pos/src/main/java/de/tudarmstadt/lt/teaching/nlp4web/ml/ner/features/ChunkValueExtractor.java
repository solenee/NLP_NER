package de.tudarmstadt.lt.teaching.nlp4web.ml.ner.features;

import java.util.Collections;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.NamedFeatureExtractor1;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

public class ChunkValueExtractor implements NamedFeatureExtractor1<Chunk> {

	@Override
	public List<Feature> extract(JCas view, Chunk chunk)
			throws CleartkExtractorException {
		if (chunk != null) {
			return Collections.singletonList(new Feature(getFeatureName(),	chunk.getChunkValue()));
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public String getFeatureName() {
		return "ChunkValue";
	}

}

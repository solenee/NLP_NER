package de.tudarmstadt.lt.teaching.nlp4web.ml.xml;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;

import com.thoughtworks.xstream.XStream;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class Features2Xml {
	public static void generateFeatureExtractors(String filename) throws FileNotFoundException{

	    List<FeatureExtractor1<Token>> tokenFeatureExtractors;
        tokenFeatureExtractors = new ArrayList<FeatureExtractor1<Token>>();

		//here begins your task !!!

		XStream xstream = XStreamFactory.createXStream();
		String x = xstream.toXML(tokenFeatureExtractors);
		x = removeLogger(x);
		PrintStream ps = new PrintStream(filename);
		ps.println(x);
		ps.close();
	}

	public static void generateContextFeatureExtractors(String filename) throws FileNotFoundException{

		List<CleartkExtractor<Token, Token>> contextFeatureExtractors;
		contextFeatureExtractors = new ArrayList<CleartkExtractor<Token, Token>>();

		// here begins your task

		XStream xstream = XStreamFactory.createXStream();
		String x = xstream.toXML(contextFeatureExtractors);
		x = removeLogger(x);
		PrintStream ps = new PrintStream(filename);
		ps.println(x);
		ps.close();
	}

	/**
	 * To make the xml file more readable remove the logger elements
	 * that are'nt needed
	 * @param x
	 * @return
	 */
	private static String removeLogger(String x) {
		StringBuffer buffer = new StringBuffer();
		String[] lines=x.split("\n");
		boolean loggerFound=false;
		for(String l:lines){
			if(l.trim().startsWith("<logger>")){
				loggerFound=true;
			}
			if(!loggerFound){
				buffer.append(l);
				buffer.append("\n");
			}else{
				if(l.trim().startsWith("</logger>")){
					loggerFound=false;
				}
			}
		}

		return buffer.toString();
	}

	public static void main(String[] args) throws FileNotFoundException {
		String contextFeatureFileName="context.xml";
		String featureFileName="feature.xml";
		generateContextFeatureExtractors(contextFeatureFileName);
		generateFeatureExtractors(featureFileName);
	}
}

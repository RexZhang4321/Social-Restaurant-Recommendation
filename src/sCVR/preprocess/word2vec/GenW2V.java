package sCVR.preprocess.word2vec;

import java.io.File;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

public class GenW2V {
	public static Word2Vec vec;
	
	public static void generate(String model){
		File gModel = new File(model);
        vec = WordVectorSerializer.readWord2VecModel(gModel);
	}
	public static Word2Vec getInstance(){
        return vec;
    }
	
}

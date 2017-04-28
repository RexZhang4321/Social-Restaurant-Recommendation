package IR.Project;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.File;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	File gModel = new File("/Users/Dylan/Downloads/glove.6B/glove.6B.300d.txt");
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        double cosSim = vec.similarity("day", "night");
        System.out.println(cosSim);
    }
}

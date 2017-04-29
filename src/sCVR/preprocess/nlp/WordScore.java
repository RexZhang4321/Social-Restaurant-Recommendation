package sCVR.preprocess.nlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Dylan on 4/28/17.
 */
public class WordScore {
    public static List<List<String[]>> scoreFun(String text)
            throws IOException {
        List<List<String[]>> result = new ArrayList<List<String[]>>();

        StanfordCoreNLP pipeline = SentimentCal.getPipeline();

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and
        // has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            Tree copy = tree.deepCopy();
            setSentimentLabels(copy);
            List<String[]> currResult = treeParser(copy.toString());
            result.add(currResult);
        }

        return result;
    }

    public static List<String[]> scoreWrapper (String text) throws IOException{
        List<List<String[]>> preScore = scoreFun(text);
        List<String[]> result = new ArrayList<String[]>();
        for(List<String[]> curr : preScore){
            for(String[] c : curr){
                System.out.println(c[0] + " " + c[1]);
                if(c[1].matches("^[a-zA-Z0-9']+$")){
                    if(Integer.parseInt(c[0]) >= 3){
                        c[0] = "1";
                    }else{
                        c[0] = "0";
                    }
                    result.add(c);
                }
            }
        }
        System.out.println("scoreWrapper result size " + result.size());
        return result;
    }
    public static void main(String[] args) throws IOException {
        SentimentCal.init();
        String text = "I really love the new iphone.";

        List<String[]> result = scoreWrapper(text);
//        List<List<String[]>> result = scoreFun(text);
        for(String[] curr : result){
            System.out.println(curr[0] + " " + curr[1]);
            System.out.println();
        }
    }

    static List<String[]> treeParser(String input) {
        List<String[]> curr = new ArrayList<String[]>();
        String[] split = input.split("\\(|\\)");
        for (String s : split) {
            String[] sArray = s.split(" ");
            if (sArray.length == 2) {
                curr.add(sArray);
            }
        }
        return curr;
    }

    static void setSentimentLabels(Tree tree) {
        if (tree.isLeaf()) {
            // System.out.println("node " + tree.nodeString());
            return;
        }

        for (Tree child : tree.children()) {
            setSentimentLabels(child);
        }

        Label label = tree.label();
        if (!(label instanceof CoreLabel)) {
            throw new IllegalArgumentException(
                    "Required a tree with CoreLabels");
        }
        CoreLabel cl = (CoreLabel) label;
        cl.setValue(Integer.toString(RNNCoreAnnotations.getPredictedClass(tree)));
    }
}

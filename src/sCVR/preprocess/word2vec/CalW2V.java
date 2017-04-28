package sCVR.preprocess.word2vec;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import sCVR.preprocess.bean.CatSco;
import org.deeplearning4j.models.word2vec.Word2Vec;

public class CalW2V {
	public static Word2Vec vec = GenW2V.getInstance();
	
	public static double getScore(String word, String concept){
		double cosSim = vec.similarity(word, concept);
        return cosSim;
	}
	
	public static String getBestScore(List<String> review, Set<String> concepts){
        String result = "";
		PriorityQueue<CatSco> heap = new PriorityQueue<CatSco>(3, new
				Comparator<CatSco>() {
					public int compare(CatSco o1, CatSco o2) {
						if(o1.getScore() - o2.getScore() > 0){
							return 1;
						}else{
							return -1;
						}
					}
				});

		for(String c : concepts){
			double curr = 0;
			double check;
			double currLength = 0;
			for(String r : review){
				check = getScore(r.toLowerCase(),c.toLowerCase());
				if(!Double.isNaN(check)){
					curr += check;
					currLength++;
				}
			}
			if(currLength != 0) {
				curr = curr / currLength;
			}

            heap.add(new CatSco(c, curr));
            if(heap.size() > 10){
                heap.poll();
            }
//			if(max < curr){
//				result = c;
//				max = curr;
//			}
		}
		while(!heap.isEmpty()){
		    result = heap.poll().getCategory() + " " + result;
        }

		return result;
	}
}

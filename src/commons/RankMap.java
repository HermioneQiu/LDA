package commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class RankMap {
	private ArrayList<String> rankResults = new ArrayList();
	private ArrayList<Integer> rankMarks = new ArrayList();
	private HashMap<String, Integer> map;
	private ArrayList<Integer> topNMarks = new ArrayList();
	private ArrayList<Float> rankWeights = new ArrayList();
	
	public ArrayList<Integer> getRankMarks() {
		return rankMarks;
	}

	public ArrayList<Integer> getTopNMarks() {
		return topNMarks;
	}

	public ArrayList<Float> getRankWeight() {
		return rankWeights;
	}

	public ArrayList<Float> getTopNWeight() {
		return topNWeight;
	}
	private ArrayList<Float> topNWeight = new ArrayList();
	
	private ArrayList<String> topNResult = new ArrayList();
	private Integer topN = 20; 
	public ArrayList<String> getRankResults() {
		return rankResults;
	}

	public ArrayList<String> getTopNResult() {
		return topNResult;
	}

	public RankMap(HashMap<String, Integer> map, Integer topN){
		this.map = map;
		this.rankByVal();
		this.initToPN(topN);
	}
	
	public RankMap(HashMap<String, Integer> map){
		this.map = map;
		this.rankByVal();
		this.initToPN(topN);
	}
	
	public void rankByVal(){
		Integer[] valArray = new Integer[map.size()]; 
//		Iterator itTmp = map.entrySet().iterator();
		Set<String> keys = map.keySet();
		Integer sumCount = 0;
		Integer v_i = 0;
		for (String key:keys){
			valArray[v_i] = map.get(key); 
			sumCount += valArray[v_i];
			v_i++;
		}
		
		
		Arrays.sort(valArray, Collections.reverseOrder());
		ArrayList<Integer> sortValList = new ArrayList();
		for(int val_i=0;val_i<valArray.length; val_i++){
			if (!sortValList .contains(valArray[val_i])){
				sortValList.add(valArray[val_i]);
			}
		}
		for(int val_i=0; val_i<sortValList.size(); val_i++){
			Iterator it = map.entrySet().iterator();
			while(it.hasNext()){
				Entry<String,Integer> entry = (Entry) it.next();
				Integer tmpVal = entry.getValue();
				String tmpKey = entry.getKey(); 
				if (tmpVal == sortValList.get(val_i)){
					this.rankResults.add(tmpKey);
					float tmpMark= (float)tmpVal/sumCount;
					this.rankWeights.add(tmpMark);
					this.rankMarks.add(tmpVal);
				}
			}
		}
	}
	public void initToPN(Integer topN){
		if (topN>rankResults.size()) {
			topN = rankResults.size();
		}	
		for (int t_i = 0; t_i < topN; t_i++) {
			topNResult.add(this.rankResults.get(t_i));
			topNMarks.add(rankMarks.get(t_i));
			topNWeight.add(rankWeights.get(t_i));
		}
		
	}
}


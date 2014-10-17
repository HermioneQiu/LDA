package ParallelLDA;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class TopicMessageCount {
	private Integer z[][];
	private float threshold;
	private Integer numTopics;
	// for store of topic distribution
	private float[][] topicDistribution;
	// for store of topic belongs of documents
	private Integer[] topicBelong;
	public Integer[] getTopicBelong() {
		for(Integer topic_i=0; topic_i < numTopics; topic_i++){
			System.out.println(topicBelong[topic_i]);
		}
		return topicBelong;
	}
	private String resultDir;
	public TopicMessageCount(Integer[][] z, String resultDir, float threshold, Integer numTopics, Integer taskId) throws IOException{
		this.z = z;
		this.threshold = threshold;
		this.numTopics = numTopics;
		initialDistribution();
		initialTopicBelong();
		//
		topicDistribute();
		getCount();
		// save(taskId)
		this.resultDir = resultDir;
		saveNum(taskId);
	}
	public void initialTopicBelong(){
		topicBelong = new Integer[numTopics];
		for(Integer topic_i=0; topic_i<topicBelong.length; topic_i++){
			topicBelong[topic_i] = 0;
		}
	}
	
	public void initialDistribution(){
		topicDistribution = new float[z.length][];
		for(Integer doc_i=0; doc_i<z.length; doc_i++){
			topicDistribution[doc_i] = new float[numTopics];
			for(Integer w_i=0; w_i<numTopics; w_i++){
				topicDistribution[doc_i][w_i] = 0;
			}
		}
	}
	
	public void topicDistribute(){
		for(Integer z_i=0; z_i < z.length; z_i++){
			for(Integer z_j=0; z_j < z[z_i].length; z_j++){
				topicDistribution[z_i][z[z_i][z_j]] += 1;
			}
		}

		// normalization
		for(Integer z_i=0; z_i < z.length; z_i++){
			float tmpSum = 0;
			for(Integer t_i=0; t_i < numTopics; t_i++){
				tmpSum += topicDistribution[z_i][t_i];
			}
			for(Integer t_i=0; t_i < numTopics; t_i++){
				topicDistribution[z_i][t_i] = topicDistribution[z_i][t_i]/tmpSum;
			}
		}
	}
	
	public void getCount(){
		// rank all topicVals, get the ranked vals and topicIndex
		// if the later - former > threshold, then stop 
		// ------ rank ------
		// for every document, caculate its belongs according rank results and threshold.
		for(Integer z_i=0; z_i< topicDistribution.length; z_i++){
			float[] tmp = new float[topicDistribution[z_i].length];
			float[] tmpTopicDistribution = topicDistribution[z_i];
			System.arraycopy(topicDistribution[z_i], 0, tmp, 0, topicDistribution[z_i].length);
			Arrays.sort(tmp); 
			//reverse and remove dulplications
			ArrayList<Float> tmpList = new ArrayList();
			for (Integer i=tmp.length-1; i>=0; i--){
				if (!tmpList.contains(tmp[i])){
					tmpList.add(tmp[i]);
				}
			}
			ArrayList<Integer> rankIndex = new ArrayList();
			ArrayList<Float> rankVals = new ArrayList();
			for (float val:tmpList){
				for(Integer t_i=0; t_i < tmpTopicDistribution.length; t_i++){
					if(tmpTopicDistribution[t_i] == val){
						rankIndex.add(t_i);
						rankVals.add(val);
				}
				}
			}
			// record what topic does this document belong to
			ArrayList<Integer> tmpBelong = new ArrayList();
		   // ------ threshold ------
		   for(Integer val_i=0; val_i < rankVals.size()-1; val_i++){
			   float prevVal = rankVals.get(val_i);
			   float laterVal = rankVals.get(val_i+1);
			   if((prevVal-laterVal)<threshold){
				   tmpBelong.add(rankIndex.get(val_i));
			   }else{
				   if(!(tmpBelong.size()>0)){
					   tmpBelong.add(rankIndex.get(val_i));
				   }
				   break;
			   }
		   }
		   // put it all in this.topicBelong
		   for(Integer topic:tmpBelong){
			   this.topicBelong[topic] += 1;
		   }
	   }
	}
	public void saveNum(Integer taskId) throws IOException{
		String numFilePath = this.resultDir + "/topic.num" + String.valueOf(taskId);
		FileWriter fw = new FileWriter(numFilePath);
		for(Integer topic_i=0; topic_i<this.topicBelong.length; topic_i++){
			String lineStr = String.valueOf(topicBelong[topic_i]+"\n");
			fw.write(lineStr);
		}
		fw.close();
	}
	/*
	public static void main(String[] args) throws IOException{
		Integer[][] z={{2,3,0,1,3,2,3},
					   {2,3,0,1,3,2,3,2,1,2},
					   {2,3,0,1,3,2,3,1,1,3,0,1,1}};
		float threshold = (float) 0.1;
		Integer numTopics = 4;
		Integer taskId = 4;
		String resultDir = "E:\\workspace\\LDA\\data\\result\\";
		TopicMessageCount topicMessageCount = new TopicMessageCount(z, resultDir, threshold, numTopics, taskId);
	}
	*/
}

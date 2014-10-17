package keyword;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import Input.Document;

public class KeyWord {
	private String docFilePath;
	// comprised by word_index
	private Integer[][] documents;
	// number of keyword
	private Integer keyWordNum;
	// word dict and rev-dict
	private Map<String, Integer> alphabetMap;
	private Map<Integer, String> revAlphabetMap;
	// counts Array
	// TF: for word in all documents, accumulate counts
	private float[] tf;
	private float[] idf;
	private float[] tfidf;
	private String[] keyWords;
	private float[] keyVals;
//	private String resultDir;
	
	// initialization
	public KeyWord(String docFilePath, String tmpFileDir, Integer keyWordNum, Integer taskId) throws IOException{
		this.docFilePath = docFilePath;
		Document documentTmp = new Document(docFilePath, tmpFileDir); 
		this.documents = documentTmp.getDocument();
		this.alphabetMap = documentTmp.getAlphabetMap();
		this.revAlphabetMap = documentTmp.getRevAlphabetMap();
		this.keyWordNum = keyWordNum;
		// initial
		initial();
		// tf-idf 
		tfIdf();
		// find the top keyWords
		rank();
		// store keywords
		saveKeyWords(taskId);
	}
	
	// initial tf-idf
	public void initial(){
		
		this.tf = new float[this.alphabetMap.size()];
		this.idf = new float[this.alphabetMap.size()];
		this.tfidf = new float[this.alphabetMap.size()];
		this.keyWords = new String[this.keyWordNum];
		this.keyVals = new float[this.keyWordNum];
		
		for(Integer tf_i=0; tf_i < tf.length; tf_i++ ){
			tf[tf_i] = 0;
		}
		for(Integer idf_i=0; idf_i < idf.length; idf_i++){
			idf[idf_i] = 0;
		}
		for(Integer tfidf_i=0; tfidf_i< tfidf.length; tfidf_i++){
			tfidf[tfidf_i] = 0;
		}
		
	}
	
	// word count: tf-idf
	public void tfIdf(){
		for (Integer doc_i=0; doc_i<documents.length; doc_i++){
			HashSet<Integer> tmpDocSet = new HashSet();
			for (Integer w_i=0; w_i<documents[doc_i].length; w_i++){
				// accumulate tf
				tf[documents[doc_i][w_i]] += 1.0/(documents[doc_i].length);
				tmpDocSet.add(documents[doc_i][w_i]);
			}
			// accumulate idf
			for (Integer a_i=0; a_i<alphabetMap.size(); a_i++){
				if (tmpDocSet.contains(a_i)){
					idf[a_i] += 1.0;
				}
			}
		}
		for (Integer tf_i=0; tf_i < tf.length; tf_i++){
			tfidf[tf_i] = (float) (tf[tf_i]*Math.log(documents.length/(idf[tf_i]+1)));
		}
	}
	
	public void rank(){
		float[] tmpTfidf = new float[tfidf.length];
		System.arraycopy(tfidf, 0, tmpTfidf, 0, tfidf.length);
		Arrays.sort(tmpTfidf); 
		//reverse and remove dulplications
		ArrayList<Float> tfidfList = new ArrayList();
		for (Integer i=tmpTfidf.length-1; i>=0; i--){
			if (!tfidfList.contains(tmpTfidf[i])){
				tfidfList.add(tmpTfidf[i]);
			}
		}
		// 
		ArrayList<Integer> rankIndex = new ArrayList();
		ArrayList<Float> valIndex = new ArrayList();
		for (float val:tfidfList){
			for(Integer tfidf_i=0; tfidf_i < tfidf.length; tfidf_i++){
				if(tfidf[tfidf_i] == val){
					rankIndex.add(tfidf_i);
					valIndex.add(val);
				}
			}
		}
		// get topN words
		if (keyWordNum > rankIndex.size()){
			keyWordNum = rankIndex.size();
		}
		for (Integer w_i=0; w_i < this.keyWordNum; w_i ++){
			keyWords[w_i] = revAlphabetMap.get(rankIndex.get(w_i));
			//value...
			keyVals[w_i] = valIndex.get(w_i);
		}
	}
	
	public void saveKeyWords(Integer taskId) throws IOException{
		File docFile = new File(docFilePath);
		String rootDir = docFile.getParent();
		String resultDir = rootDir + "/result/";
		File root = new File(resultDir);
		if (!root.exists()){
			root.mkdir();
		}
		String resultFilePath = resultDir + "keyWords" + String.valueOf(taskId);
		FileWriter fw = new FileWriter(resultFilePath);
		//
		for (Integer w_i=0; w_i<keyWords.length; w_i++){
			String lineStr = keyWords[w_i]+":" + String.valueOf(keyVals[w_i]) +"\n";
			fw.write(lineStr);
		}
		fw.close();
	}
	/*
	public static void main(String[] args) throws IOException{
		String docFilePath = "E:\\workspace\\LDA\\data\\message_5.lda";
		File docFile = new File(docFilePath);
		String rootDir = docFile.getParent();
	    String tmpDirStr = rootDir + "/tmp/";
		File tmpDir= new File(tmpDirStr);
		if(! tmpDir.exists()){
			tmpDir.mkdir();
		}
//		String tmpFileDir = "";
		Integer keyWordNum = 10;
		Integer taskId = 5;
		KeyWord keyWord = new KeyWord( docFilePath, tmpDirStr, keyWordNum, taskId);
	}
	*/
}

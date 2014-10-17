package ParallelLDA;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import keyword.KeyWord;
import Input.Document;
import commons.OptionParse;
import commons.RankMap;


/**
 * @author hermione
 *
 */
/**
 * @author hermione
 *
 */
/**
 * @author hermione
 *
 */
public class ParallelLDA {
	
	/** 
	 * max iterations 
	 */  
	// set default value of numIterations and numThreads
	public Integer numIterations = 1000; 
	public Integer numThreads = 2;
//	/** 
//	 * size of statistics 
//	 */  
//	public Integer numstats;  
	/** 
	 * document data (term lists) 
	 */  
	public Integer[][] documents;  
	/** 
	 * vocabulary size 
	 */  
	public Integer V;  
	/** 
	 * number of topics 
	 */  
	public Integer K;  
	/** 
	 * Dirichlet parameter (document--topic associations) 
	 */  
	public double alpha = 2;  
	/** 
	 * Dirichlet parameter (topic--term associations) 
	 */  
	public double beta = .5;  
	/** 
	 * topic assignments for each word. 
	 * N * M 缁达紝绗竴缁存槸鏂囨。锛岀浜岀淮鏄痺ord 
	 */  
	public Integer z[][];  
	
	public void setZ(LDARunnable[] ldaRunnables) {
		// initial Z of parallelLDA by all ldarunnables
		Integer recordZ = 0;
		for (Integer thread_i=0; thread_i<numThreads; thread_i++ ){
			Integer[][] tmpZ = ldaRunnables[thread_i].getZ();
			for(Integer tz_i=0; tz_i<tmpZ.length; tz_i++){
				z[recordZ] = tmpZ[tz_i];
				recordZ += 1;
			}
		}
	}

	public Integer[][] getZ() {
		return z;
	}
	/** 
	 * number of instances of word i (term?) assigned to topic j. 
	 */  
	private Integer[][] typeTopicCounts;  
	/** 
	 * number of words in document i assigned to topic j. 
	 */  
	private Integer[][] topicDocCounts;
	/** 
	 * total number of words assigned to topic j. 
	 */  
	private Integer[]tokensPerTopic;  
	/** 
	 * total number of words in document i. 
	 */  
	private Integer[]docLengthCounts;
	/** 
	 * cumulative statistics of theta 
	 */  
	public double[][] thetasum;  
	/** 
	 * cumulative statistics of phi 
	 */  
	public double[][] phisum;  
	// unused params
	public Integer  numstats;
	public Integer SAMPLE_LAG;
	// for the need of outputing words, instead of index  
	private Map<String, Integer> alphabetMap;
	private Map<Integer, String> revAlphabetMap;
	private String docFilePath;
	private String outFileDir;
	private String tmpFileDir;
	private Integer numState = 50;
	private Integer taskId;
	public Integer[][] getTypeTopicCounts() {
		return typeTopicCounts;
	}

	public void setTypeTopicCounts(Integer[][] typeTopicCounts) {
		this.typeTopicCounts = typeTopicCounts;
	}

	public Integer[][] getTopicDocCounts() {
		return topicDocCounts;
	}

	public void setTopicDocCounts(Integer[][] topicDocCounts) {
		this.topicDocCounts = topicDocCounts;
	}

	public Integer[]getTokensPerTopic() {
		return tokensPerTopic;
	}

	public void setTokensPerTopic(Integer[]tokensPerTopic) {
		this.tokensPerTopic = tokensPerTopic;
	}

	public Integer[]getDocLengthCounts() {
		return docLengthCounts;
	}

	public void setDocLengthCounts(Integer[]docLengthCounts) {
		this.docLengthCounts = docLengthCounts;
	}

	/**
	 * 鍒濆鍖栵紝 璁剧疆璇濋鏁帮紝杩唬鏁帮紝绾跨▼鏁�	 * @param documents
	 * @param V
	 * @param K
	 * @param alpha
	 * @param beta
	 * @param numThreads
	 * @param numIterations
	 * @throws IOException 
	 */
	public ParallelLDA(String docFilePath, String outFileDir, String tmpFileDir, Integer K, Integer numThreads, Integer numIterations, Integer numState, Integer taskId) throws IOException{
//		this.documents = documents;
		// init documents/ alphabetMap/ revAlphabetMap
		this.docFilePath = docFilePath;
		this.outFileDir = outFileDir;
		this.tmpFileDir = tmpFileDir;
		Document documentTmp = new Document(docFilePath, tmpFileDir); 
		this.documents = documentTmp.getDocument();
		
		this.alphabetMap = documentTmp.getAlphabetMap();
		this.revAlphabetMap = documentTmp.getRevAlphabetMap();
		this.V = alphabetMap.size();
		this.K = K;
		Integer M = documents.length;
		this.tokensPerTopic = new Integer[K];
		this.typeTopicCounts = new Integer[V][K];
		this.numIterations = numIterations;
		this.numThreads = numThreads;
		this.numState = numState;
		this.taskId = taskId;
		initialZ();
		/*
		String alphaTmpFile = this.tmpFileDir+"alphabetDict.dat";
		FileWriter fw = new FileWriter(alphaTmpFile);
		Iterator mrIt = alphabetMap.entrySet().iterator();
		while(mrIt.hasNext()){
			Entry<String, Integer> entity = (Entry<String, Integer>) mrIt.next();
			String key = entity.getKey();
			Integer val = entity.getValue();
			String lineStr = key+":"+val+"\n";
			fw.write(lineStr);
		}
		fw.close();
		*/
	}
	/*
	public ParallelLDA(String docFilePath, Integer K, Integer numThreads, Integer numIterations) throws IOException{
//		this.documents = documents;
		// init documents/ alphabetMap/ revAlphabetMap
		this.docFilePath = docFilePath;
		this.outFileDir = "/home/hermione/workspace/LDA/data/output/";
		Document documentTmp = new Document(docFilePath); 
		this.documents = documentTmp.getDocument();
		
		this.alphabetMap = documentTmp.getAlphabetMap();
		this.revAlphabetMap = documentTmp.getRevAlphabetMap();
		this.V = alphabetMap.size();
		this.K = K;
		Integer M = documents.length;
		this.tokensPerTopic = new Integer[K];
		this.typeTopicCounts = new Integer[V][K];
		this.numIterations = numIterations;
		this.numThreads = numThreads;
		this.numState = numState;
		// test start
		String outFilePath = "/home/hermione/workspace/LDA/data/tmp/alphabet.m";
		FileWriter fw = new FileWriter(outFilePath);

		Iterator mrIt = alphabetMap.entrySet().iterator();
		while(mrIt.hasNext()){
			Entry<String, Integer> entity = (Entry<String, Integer>) mrIt.next();
			String key = entity.getKey();
			Integer val = entity.getValue();
			String lineStr = key+":"+val+"\n";
			fw.write(lineStr);
		}
		fw.close();
		// test end
	}
*/
	public void initTypeTopicCounts(LDARunnable[] ldaRunnables) throws IOException{
		// for tokensPerTopic, clear
		Arrays.fill(tokensPerTopic, 0);
		// for typeTopicCounts, clear
		for(Integer type=0; type<V; type++){
			Arrays.fill(typeTopicCounts[type], 0);
		}
		for(Integer thread=0; thread<numThreads; thread++){
			// for tokensPerTopic, add all thread
			Integer[] sourceTotals = ldaRunnables[thread].getTokensPerTopic();
			for(Integer topic=0; topic<K; topic++){
				tokensPerTopic[topic] += sourceTotals[topic];
				
			}
			// for typeToipcCounts, add all thread

			Integer[][] sourceTypeCounts = ldaRunnables[thread].getTypeTopicCounts();
			for(Integer type=0; type<V; type++){
				for(Integer topic=0; topic<K; topic++){
					typeTopicCounts[type][topic] += sourceTypeCounts[type][topic];
				}
			}
			/* for test
			String testFilePath = this.tmpFileDir + "counts.init" + thread; 
			this.outCounts(testFilePath, sourceTypeCounts);
			*/
		}
		/* for test
		String testFilePath = this.tmpFileDir + "counts.init"; 
		this.outCounts(testFilePath, this.typeTopicCounts);
		*/ 
	}
	// for test error
	public void outCounts(String outFileName, Integer[][] tmpTypeTopicCounts) throws IOException{
		FileWriter fw = new FileWriter(outFileName);
		
		for(int type_i=0; type_i<tmpTypeTopicCounts.length; type_i++){
			for(int topic_i=0; topic_i<tmpTypeTopicCounts[type_i].length; topic_i++){
				fw.write(tmpTypeTopicCounts[type_i][topic_i]+",");
			}
			fw.write("\n");
		}
		fw.close();
	}
	
	public void execute() throws InterruptedException, IOException{
		LDARunnable[] ldaRunnables = new LDARunnable[numThreads]; 
		String testFileName = tmpFileDir + "document";
		outCounts(testFileName, documents);
		// test end
		
		if (numThreads > 1) {
			// divide the documents according to numThreads
			Integer docNum = documents.length;
			System.out.println("doclen:"+docNum);
			Integer singleDocNum = docNum/numThreads;
			ArrayList<Integer> partIndexList = new ArrayList<Integer>();
			for(Integer thread=0; thread<numThreads; thread++){
				partIndexList.add(thread*singleDocNum);
			}
			partIndexList.add(docNum);

			for (Integer i=0; i<numThreads; i++){
				Integer endIndex = partIndexList.get(i+1);
				Integer startIndex = partIndexList.get(i);
				Integer curLen = endIndex - startIndex;
				Integer[][] curDoc = new Integer[curLen][];
				for(Integer doc=0; doc<curLen; doc++){
					curDoc[doc] = documents[startIndex+doc];
				}
				System.out.println("Thread "+ i);
				System.out.println(startIndex+":"+endIndex +":" +curDoc.length);
				
				Integer[]runnableTotals = new Integer[K];
				Integer[][] runnableCounts = new Integer[V][K];
				// 姣忎釜绾跨▼澶勭悊涓�儴鍒嗙殑documents
				ldaRunnables[i] = new LDARunnable(curDoc, K, V,
											alpha, beta, 
											runnableCounts, runnableTotals);
				// init every thread's typeTopicCounts
				ldaRunnables[i].initialState();
				// need ********
			}
			// use all thread's typeTopicCounts to init this typeTopicCounts
			initTypeTopicCounts(ldaRunnables);
		
			for(Integer thread=0; thread<numThreads; thread++){
				// user this typeTopicCounts to init every thread
				ldaRunnables[thread].setTypeTopicCounts(typeTopicCounts, tokensPerTopic);
			}
		}
		
		// 灏氭湭鑰冭檻鍗曠嚎绋�***
		// 寮�杩愯绾跨▼
		System.out.println("executors begin to run.");
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

		//寮�杩唬
		for(Integer iteration=0; iteration<numIterations; iteration++){
			if(iteration%numState == 0){
				System.out.println("iteration_times: "+iteration);
			}
			
			   if (numThreads>1){
				for(Integer thread_i=0; thread_i<numThreads; thread_i++){
					executor.execute(new Thread(ldaRunnables[thread_i]));
				}
				boolean finished = false;
				while (! finished) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {

					}
					finished = true;
					// Are all the threads done?
					for (Integer thread = 0; thread < numThreads; thread++) {
						finished = finished && ldaRunnables[thread].isFinished;
					}
				}
//				
				sumCounts(ldaRunnables);
				// updateCounts() 锛宼ypeTopicCounts, tokensTopics
				updateCounts(ldaRunnables);
			}
		}
		executor.shutdownNow();
		setZ(ldaRunnables);
	}
	
	public void initialZ(){
		Integer docLen = this.documents.length;
		this.z = new Integer[documents.length][];
		for (Integer doc_i=0; doc_i<documents.length; doc_i++){
			this.z[doc_i] =new Integer[documents[doc_i].length];
		}
	}

	public void sumCounts(LDARunnable[] ldaRunnables){
		
		// all tokensPerTopic/typeTopicCounts = base + bias
		// base tokensPerTopic this.tokensPerTopic
		// base typeTopicCounts this.typeTopicCounts
		// init bias tokenPerTopic
		Integer[] biasTokensPerTopic = new Integer[K];
		for(int topic_i=0; topic_i<K; topic_i++){
			biasTokensPerTopic[topic_i] =0;
		}
		
		// clear bias typeTopicCounts
		Integer[][] biasTypeTopicCounts = new Integer[V][K];
		for(int type_i=0; type_i<V; type_i++){
			for(int topic_i=0; topic_i<K; topic_i++){
				biasTypeTopicCounts[type_i][topic_i] =0;
			}
		}
		
		for(Integer thread=0; thread< numThreads; thread++){
			// first, add all threads's delta tokensPerTopic together
			Integer[]sourceTotals = ldaRunnables[thread].getTokensPerTopic();
			for (Integer topic = 0; topic < K; topic++) {
				biasTokensPerTopic[topic] += sourceTotals[topic] - this.tokensPerTopic[topic];
			}
			// second, add all threads' delta typeTopicCounts together
			Integer[][] sourceTypeTopicCounts = 
					ldaRunnables[thread].getTypeTopicCounts();
			for(Integer type=0; type < V; type++){
				for(Integer topic=0; topic < K; topic++){
					biasTypeTopicCounts[type][topic] += sourceTypeTopicCounts[type][topic] -this.typeTopicCounts[type][topic];
				}
			}
		}
		//add delta to base counts
		for(Integer topic=0; topic<K; topic++){
			tokensPerTopic[topic] += biasTokensPerTopic[topic];
		}
		for(Integer type=0; type<V; type++){
			for(Integer topic=0; topic<K; topic++){
				typeTopicCounts[type][topic] += biasTypeTopicCounts[type][topic];
			}
		}
	}

	public void updateCounts(LDARunnable[] ldaRunnables){
		for (Integer thread = 0; thread < numThreads; thread++) {
			// 鏇存柊 tokensPerTopic
			Integer[]runnableTotals = ldaRunnables[thread].getTokensPerTopic();
			System.arraycopy(tokensPerTopic, 0, runnableTotals, 0, K);
			Integer[][] runnableCounts = ldaRunnables[thread].getTypeTopicCounts();
			for(Integer type=0; type < V; type++){
				System.arraycopy(typeTopicCounts[type], 0, runnableCounts[type], 0, runnableCounts[type].length);
			}
		}
	}
	
	public void updateDoc(LDARunnable[] ldaRunnables) {

		// clear
		Arrays.fill(docLengthCounts, 0);
		for (Integer topic = 0; topic < topicDocCounts.length; topic++) {
			Arrays.fill(topicDocCounts[topic], 0);
		}
		
		for (Integer thread = 0; thread < numThreads; thread++) {
			Integer[]sourceLengthCounts = ldaRunnables[thread].getDocLengthCounts();
			Integer[][] sourceTopicCounts = ldaRunnables[thread].getTopicDocCounts();
			// count--
			for (Integer count=0; count < sourceLengthCounts.length; count++) {
				if (sourceLengthCounts[count] > 0) {
					docLengthCounts[count] += sourceLengthCounts[count];
					sourceLengthCounts[count] = 0;
				}
			}
			//
			for(Integer count=0; count < sourceTopicCounts.length; count++){
				for (Integer topic=0; topic < K; topic++) {
					topicDocCounts[count][topic] += sourceTopicCounts[count][topic];
				}
			}
		}
	}
	public void updateTheta(LDARunnable[] ldaRunnables){
		updateDoc(ldaRunnables);
		for(Integer doc=0; doc < docLengthCounts.length; doc++){
			for(Integer topic=0; topic < K; topic++){
				thetasum[doc][topic] += (topicDocCounts[doc][topic] + alpha)/(docLengthCounts[doc] + K * alpha);
			}
		}
	}
	public void updatePhi(LDARunnable[] ldaRunnables){
		updateDoc(ldaRunnables);
		for(Integer word=0; word < V; word++){
			for(Integer topic=0; topic < K; topic++){
				phisum[word][topic] += (typeTopicCounts[word][topic]+beta)/(tokensPerTopic[topic] + V*beta);
			}
		}
	}

	/** 
	 * Add to the statistics the values of theta and phi for the current state. 
	 */  
	private void updateParams(LDARunnable[] ldaRunnables) {  
		updateTheta(ldaRunnables);
		updatePhi(ldaRunnables);
		numstats++;  
	}  

	/** 
	 * Retrieve estimated document--topic associations. If sample lag > 0 then 
	 * the mean value of all sampled statistics for theta[][] is taken. 
	 *  
	 * @return theta multinomial mixture of document topics (M x K) 
	 */  
	public double[][] getTheta() {  
		double[][] theta = new double[documents.length][K];  

		if (SAMPLE_LAG > 0) {  
			for (Integer m = 0; m < documents.length; m++) {  
				for (Integer k = 0; k < K; k++) {  
					theta[m][k] = thetasum[m][k] / numstats;  
				}  
			}  

		} else {  
			for (Integer m = 0; m < documents.length; m++) {  
				for (Integer k = 0; k < K; k++) {  
					theta[m][k] = (topicDocCounts[m][k] + alpha) / (docLengthCounts[m] + K * alpha);  
				}  
			}  
		}  
		return theta;  
	}  

	/** 
	 * Retrieve estimated topic--word associations. If sample lag > 0 then the 
	 * mean value of all sampled statistics for phi[][] is taken. 
	 *  
	 * @return phi multinomial mixture of topic words (K x V) 
	 */  
	public double[][] getPhi() {  
		double[][] phi = new double[K][V];  
		if (SAMPLE_LAG > 0) {  
			for (Integer k = 0; k < K; k++) {  
				for (Integer w = 0; w < V; w++) {  
					phi[k][w] = phisum[k][w] / numstats;  
				}  
			}  
		} else {  
			for (Integer k = 0; k < K; k++) {  
				for (Integer w = 0; w < V; w++) {  
					phi[k][w] = (typeTopicCounts[w][k] + beta) / (tokensPerTopic[k] + V * beta);  
				}  
			}  
		}  
		return phi;  
	}  
	
	/**
	 * @param topN
	 * @return
	 * @throws IOException 
	 */
	public void getTopNWords(Integer topN) throws IOException{
		// 
		Integer[][] revTypeTopicCounts = new Integer[K][V];
		for(Integer type_i=0; type_i<V;type_i++){
			for(Integer topic_i=0; topic_i<K; topic_i++){
				revTypeTopicCounts[topic_i][type_i] = typeTopicCounts[type_i][topic_i];
			}
		}
		// a map for every topic
		HashMap<String, Integer>[] wordMaps = new HashMap[K];
		for(Integer topic_i=0; topic_i<K; topic_i++){
			wordMaps[topic_i] = new HashMap<String, Integer>();
		}
		// put words and there counts into a map
		for(Integer topic_i=0; topic_i<K; topic_i++){
			Integer[] tmpArray = new Integer[V];
			tmpArray = revTypeTopicCounts[topic_i];
			for(int type_i=0; type_i<V; type_i++){
				wordMaps[topic_i].put(revAlphabetMap.get(type_i), tmpArray[type_i]);
			}
		}
		//sort for every topic
		// and save results and marks
		String tmpFilePath = this.outFileDir + "topic.kwords"+String.valueOf(this.taskId);
		String resultFilePath = this.outFileDir + "topic.kwordsR"+String.valueOf(this.taskId);
		FileWriter fw = new FileWriter(tmpFilePath);
		FileWriter fwr = new FileWriter(resultFilePath);
		for(Integer topic_i=0; topic_i<K; topic_i++){
			RankMap rankMap = new RankMap(wordMaps[topic_i]);
			ArrayList<String> rankResults = rankMap.getTopNResult();
			ArrayList<Float> rankWeights = rankMap.getTopNWeight();
			ArrayList<Integer> rankMarks = rankMap.getTopNMarks();
			String tmpLineStr = "";
			String lineStr = "";
			for(int r_i=0; r_i<rankResults.size(); r_i++){
				tmpLineStr+=rankResults.get(r_i)+"\tweight:"+rankWeights.get(r_i)+"\tcount:"+rankMarks.get(r_i)+"\n";
				lineStr += rankResults.get(r_i)+"\n";
			}
			fw.write("topic "+String.valueOf(topic_i)+" key words:\n");
			fw.write(tmpLineStr);
			fwr.write("topic "+String.valueOf(topic_i)+"\n");
			fwr.write(lineStr);
		}
		fw.close();
		fwr.close();
	}
	/**
	 * according to documents's wordsIndex and topicIndex, get the phrase 
	 * @author hermione
	 * @param topN
	 * @throws IOException 
	 */
public void getTopNPhrase(Integer topN) throws IOException{
		// first, get typetopic
		// {topic_i, topic_i...}
		Integer[] typeTopic = new Integer[V];
		// typeTopicCounts
		for(Integer type_i=0; type_i<V; type_i++){
			Integer[] tmpArray = new Integer[typeTopicCounts[type_i].length];
			System.arraycopy(typeTopicCounts[type_i], 0, tmpArray, 0, typeTopicCounts[type_i].length);
			Integer topic = -1;
			// sort and get index of maximum
			Arrays.sort(tmpArray, Collections.reverseOrder());
			Integer maxVal = tmpArray[tmpArray.length-1];
			for(Integer val_i=0; val_i<tmpArray.length; val_i++){
				if (typeTopicCounts[type_i][val_i]==maxVal){
					topic = val_i;
					break;
				}
			}
			typeTopic[type_i] = topic;
			
		}		
		// get typeTopicsPerDoc
		HashMap<String, Integer>[] phraseMaps = new HashMap[K];
		for (int topic_i=0; topic_i<K; topic_i++){
			phraseMaps[topic_i] = new HashMap();
		}
		
		// save topNPhrases
		String tmpFilePath = this.outFileDir + "topic.phrases"+String.valueOf(this.taskId);
		String resultFilePath = this.outFileDir + "topic.phraseR"+String.valueOf(this.taskId);
		FileWriter fw = new FileWriter(tmpFilePath);
		FileWriter fwr = new FileWriter(resultFilePath);

		Integer[][] featureSequence = this.documents;
		for(Integer doc_i=0; doc_i<documents.length; doc_i++){
			// get the wordTopic for every document in documents.
			Integer[]typeTopicPerDoc = new Integer[documents[doc_i].length];
			for(Integer type_i=0; type_i<documents[doc_i].length; type_i++){
				Integer type = documents[doc_i][type_i];
				// 
				Integer topic = typeTopic[type];
				typeTopicPerDoc[type_i] = topic;
			}
			// get phrase Index
			Integer prevtopic = -1;
			Integer prevfeature = -1;
			Integer topic = -1;
			String sb = null;
			Integer feature = -1;
			for (Integer type_i=0; type_i<typeTopicPerDoc.length; type_i++){
				feature = featureSequence[doc_i][type_i];
				topic = typeTopicPerDoc[type_i];
				if (topic == prevtopic) {
					if (sb == null)
						sb = new String (revAlphabetMap.get(prevfeature) + " " + revAlphabetMap.get(feature));
					else {
						sb += " ";
						sb += revAlphabetMap.get(feature);
					}
				} else if (sb != null) {
//						System.out.println(prevtopic);
						if ((phraseMaps[prevtopic].size()==0)||(phraseMaps[prevtopic].get(sb) == null)){
					    	phraseMaps[prevtopic].put(sb, 1);
					    }
					    else{
							Integer val = phraseMaps[prevtopic].get(sb);
							val ++;
							phraseMaps[prevtopic].put(sb, val);
					    }
						prevtopic = prevfeature = -1;
						sb = null;
				} else {
						prevtopic = topic;
						prevfeature = feature;
				}
			}
		}
		// sort phrases or words
		for(Integer topic_i=0; topic_i<K; topic_i++){
			// sort Map, according to vals, return keys.
			if(phraseMaps[topic_i].size()!=0){
				RankMap rankMap= new RankMap(phraseMaps[topic_i]);
//				RankMap rankMap= new RankMap(phraseMaps[topic_i], topN);

				ArrayList<String> rankResults = rankMap.getTopNResult();
				ArrayList<Integer> rankMarks = rankMap.getTopNMarks();
				ArrayList<Float> rankWeights = rankMap.getTopNWeight();
				// change ArrayList into String
				String tmpLineStr = "";
				String lineStr = "";
				for(int r_i=0; r_i<rankResults.size(); r_i++){
					tmpLineStr+=rankResults.get(r_i)+"\tweight:"+rankWeights.get(r_i)+"\tcount:"+rankMarks.get(r_i)+"\n";
					lineStr += rankResults.get(r_i)+"\n";
				}
				fw.write("topic "+String.valueOf(topic_i)+" phrases:\n");
				fw.write(tmpLineStr);
				fwr.write("topic "+ String.valueOf(topic_i)+"\n");
				fwr.write(lineStr);
			}
		}
		fw.close();
		fwr.close();
	}
	/**
	 *  main function
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterruptedException, IOException{
		// startTime of MultiThread process
		long startTime = System.currentTimeMillis();
		
		// parse input parameters
		// ****command: -docFilePath -numTopics -numThreads -numIterations -numState -taskId
		if((args.length==16)){
			    // ------------------ topic train---------------------------------
				OptionParse optionParse = new OptionParse(args);
				boolean flag = optionParse.isFlag();
				if(flag){
				Integer numTopics = Integer.valueOf(optionParse.getNumTopics());
				Integer numThreads = Integer.valueOf(optionParse.getNumThreads());
				Integer numIterations = Integer.valueOf(optionParse.getNumIterations());
				Integer numState = Integer.valueOf(optionParse.getNumState());
				Integer taskId = Integer.valueOf(optionParse.getTaskId());
				float threshold = Float.parseFloat(optionParse.getThreshold());
				String docFilePath = optionParse.getDocFilePath();
				
				File docFile = new File(docFilePath); 
				String fileDir = docFile.getParent();
				String outFileDirStr = fileDir + "/result/";
				if ((flag = new File(outFileDirStr).exists())== false){
					new File(outFileDirStr).mkdir();
				}
				String tmpFileDirStr = fileDir + "/tmp/";
				if ((flag = new File(tmpFileDirStr).exists())== false){
					new File(tmpFileDirStr).mkdir();
				}
				System.out.println("correct parameter, begin train process.");
				ParallelLDA  lda = new ParallelLDA(docFilePath, outFileDirStr, tmpFileDirStr, numTopics, numThreads, numIterations, numState, taskId);
				lda.execute();
				//endTimeof MultiThread process
				long endTime = System.currentTimeMillis();
				long deltaTime = (endTime - startTime)/1000;
				System.out.println("this trainning process consume "+deltaTime+" seconds");
				// get topics keyWords (topN)
				System.out.println("begin to get topic.kwords");
				Integer topNWords = 10;
				lda.getTopNWords(topNWords);
				System.out.println("finished topic.kwords");
				// get topics Phrases (topN)
				System.out.println("begin to get topic.phrase");
				Integer topNPhrases = 10;
				lda.getTopNPhrase(topNPhrases);
				System.out.println("finished topic.phrase");
				//get topics chapter
				System.out.println("begin to get topic.documents.num");
				Integer[][] Z = lda.getZ();
				TopicMessageCount topicMessageCount = new TopicMessageCount(Z, outFileDirStr, threshold, numTopics, taskId);
				
				System.out.println("finish to get topic.documents.num");
				// ---------------------keyword-----------------------
				// keyWordNum 
				System.out.println("begin to get Keywords");
				Integer keyWordNum = Integer.valueOf(optionParse.getNumKeyWords());
				KeyWord keyWord = new KeyWord( docFilePath, tmpFileDirStr, keyWordNum, taskId);
				System.out.println("finished getting Keywords");

				}else{
					optionParse.hint();
				}
				
		}else{
			System.out.println("numbers of parameter is not correct.");
			OptionParse.hint();
		}
	}
}

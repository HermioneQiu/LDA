package ParallelLDA;

import java.util.concurrent.TimeUnit;


public class LDARunnable implements Runnable {
	/** 
	 * document data (term lists) 
	 */  
	protected Integer[][] documents;  
	/** 
	 * vocabulary size 
	 */  
	protected Integer V;  
	/** 
	 * number of topics 
	 */  
	protected Integer K;  
	/** 
	 * Dirichlet parameter (document--topic associations) 
	 */  
	protected double alpha;  
	/** 
	 * Dirichlet parameter (topic--term associations) 
	 */  
	protected double beta;  
	/** 
	 * topic assignments for each word. 
	 * N * M 缁达紝绗竴缁存槸鏂囨。锛岀浜岀淮鏄痺ord 
	 */  
	protected Integer z[][];  
	public Integer[][] getZ() {
		return z;
	}

	/** 
	 * number of instances of word i (term?) assigned to topic j. 
	 */  
	protected Integer[][] typeTopicCounts;  
	/** 
	 * number of words in document i assigned to topic j. 
	 */  
	protected Integer[][] topicDocCounts;
	/** 
	 * total number of words assigned to topic j. 
	 */  
	protected Integer[] tokensPerTopic;  
	/** 
	 * total number of words in document i. 
	 */  
	protected Integer[] docLengthCounts;
	/** 
	 * cumulative statistics of theta 
	 */  
	double[][] thetasum;  
	/** 
	 * cumulative statistics of phi 
	 */  
	double[][] phisum;  

	public boolean isFinished = true;

	public Integer[] getTokensPerTopic() { return tokensPerTopic; }
	public Integer[][] getTypeTopicCounts() { return typeTopicCounts; }

	public Integer[] getDocLengthCounts() { return docLengthCounts; }
	public Integer[][] getTopicDocCounts() { return topicDocCounts; }

	/** 
	 * Initialise this thread with data. 
	 *  
	 * @param V 
	 *            vocabulary size 
	 * @param data 
	 */  
	public LDARunnable(Integer[][] documents, Integer K, Integer V,
			double alpha, double beta,
			Integer[][] typeTopicCounts, Integer[] tokensPerTopic) {  
		// documets that should be processed in this thread.
		this.documents = documents;  
		// all tokens contained in the corpus
		this.V = V; 
		// number of topics
		this.K = K;
		this.alpha = alpha;
		this.beta = beta;
		this.typeTopicCounts = typeTopicCounts;
		this.tokensPerTopic = tokensPerTopic;
	}  
	
	public void setTypeTopicCounts(Integer[][] srcTypeTopicCounts, Integer[] srcTokensPerTopic){
		this.typeTopicCounts = srcTypeTopicCounts;
		this.tokensPerTopic = srcTokensPerTopic;
	}
	
	/** 
	 * Initialisation: Must start with an assignment of observations to topics ? 
	 * Many alternatives are possible, I chose to perform random assignments 
	 * with equal probabilities 
	 *  
	 * @param K 
	 *            number of topics 
	 * @return z assignment of topics to words 
	 */  
	//鍦ㄧ嚎绋嬭繍琛屼箣鍓嶈皟鐢�	
	public void initialState() {  
		
		Integer M = documents.length;  
		System.out.println("Thread docNum "+M);
		// initialise count variables.  
		typeTopicCounts = new Integer[V][K];  
		topicDocCounts = new Integer[M][K];  
		tokensPerTopic = new Integer[K];  
		docLengthCounts = new Integer[M];  
		for(int type_i=0; type_i<V; type_i++){
			for(int topic_i=0; topic_i<K; topic_i++){
				typeTopicCounts[type_i][topic_i]=0;
			}
		}
		for(int doc_i=0; doc_i<M; doc_i++){
			for(int topic_i=0; topic_i<K; topic_i++){
				topicDocCounts[doc_i][topic_i] = 0;
			}
		}
		for(int topic_i=0; topic_i<K; topic_i++){
			tokensPerTopic[topic_i] = 0;
		}
		for(int doc_i=0; doc_i<M; doc_i++){
			docLengthCounts[doc_i]=0;
		}
		// The z_i are are initialised to values in [1,K] to determine the  
		// initial state of the Markov chain.  
		// 涓轰簡鏂逛究锛屼粬娌＄敤浠庣媱鍒╁厠闆峰弬鏁伴噰鏍凤紝鑰屾槸闅忔満鍒濆鍖栦簡锛� 
		z = new Integer[M][];  
		for (Integer m = 0; m < M; m++) {  
			Integer N = documents[m].length;  
			z[m] = new Integer[N];  
			
			for (Integer n = 0; n < N; n++) {
				//闅忔満鍒濆鍖栵紒  
				Integer topic = (int) (Math.random() * (K));  
				z[m][n] = topic;  
				// number of instances of word i assigned to topic j  
				// documents[m][n] 鏄m涓猟oc涓殑绗琻涓瘝  
				typeTopicCounts[documents[m][n]][topic]++;  
				// number of words in document i assigned to topic j.  
				topicDocCounts[m][topic]++;  
				// total number of words assigned to topic j.  
				tokensPerTopic[topic]++;  
			}  
			// total number of words in document i  
			docLengthCounts[m] = N;  
		}  
	}  

	/** 
	 * Main method: Select initial state ? Repeat a large number of times: 1. 
	 * Select an element 2. Update conditional on other elements. If 
	 * appropriate, output summary for each run. 
	 *  
	 * @param K 
	 *            number of topics 
	 * @param alpha 
	 *            symmetric prior parameter on document--topic associations 
	 * @param beta 
	 *            symmetric prior parameter on topic--term associations 
	 */  
	public void gibbs() {  
		// for all z_i  
		for (Integer m = 0; m < z.length; m++) {  
			for (Integer n = 0; n < z[m].length; n++) {  
				// (z_i = z[m][n])  
				// sample from p(z_i|z_-i, w)  
				//鏍稿績姝ラ锛岄�杩囪鏂囦腑琛ㄨ揪寮忥紙78锛変负鏂囨。m涓殑绗琻涓瘝閲囨牱鏂扮殑topic  
				Integer topic = sampleFullConditional(m, n);  
				z[m][n] = topic;  
			}  
		}  
	}  
	/** 
	 * Sample a topic z_i from the full conditional distribution: p(z_i = j | 
	 * z_-i, w) = (n_-i,j(w_i) + beta)/(n_-i,j(.) + W * beta) * (n_-i,j(d_i) + 
	 * alpha)/(n_-i,.(d_i) + K * alpha) 
	 * 锛燂紵锛熷瓨鍦ㄧ殑闂锛屽ぇ閮ㄥ垎鍗曡瘝闆嗕腑鍦╰opic0涓嬨�
	 *  
	 * @param m 
	 *            document 
	 * @param n 
	 *            word 
	 */  
	public Integer sampleFullConditional(Integer m, Integer n) {  

		// remove z_i from the count variables  
		//杩欓噷棣栧厛瑕佹妸鍘熷厛鐨則opic z(m,n)浠庡綋鍓嶇姸鎬佷腑绉婚櫎 
		Integer topic = z[m][n];  
		typeTopicCounts[documents[m][n]][topic]--;  
		topicDocCounts[m][topic]--;  
		tokensPerTopic[topic]--;  
		docLengthCounts[m]--;  
		// do multinomial sampling via cumulative method:  
		double[] p = new double[K];  
		for (Integer k = 0; k < K; k++) {  
			//typeTopicCounts[V][K] 鏄i涓獁ord琚祴浜堢j涓猼opic鐨勪釜鏁� 
			//鍦ㄤ笅寮忎腑锛宒ocuments[m][n]鏄痺ord id锛宬涓虹k涓猼opic  
			//topicDocCounts 涓虹m涓枃妗ｄ腑琚祴浜坱opic k鐨勮瘝鐨勪釜鏁� 
			p[k] = (typeTopicCounts[documents[m][n]][k] + beta) / (tokensPerTopic[k] + V * beta)  
					* (topicDocCounts[m][k] + alpha) / (docLengthCounts[m] + K * alpha);  
		}  
		// cumulate multinomial parameters  
		for (Integer k = 1; k < p.length; k++) {  
			p[k] += p[k - 1];  
		}  
		// scaled sample because of unnormalised p[]  
		double u = Math.random() * p[K-1];  
		// how to refine this ????*****
		// 浣縯opic鍦蓟0锛�锛戒箣鍐�		
		for (topic = 0; topic < p.length-1; topic++) {  
			if (u < p[topic])  
				break;  
		}  
		// add newly estimated z_i to count variables  
//		System.out.println(documents[m][n]+":"+topic+":"+typeTopicCounts.length+":"+typeTopicCounts[0].length);
		typeTopicCounts[documents[m][n]][topic]++;  
		topicDocCounts[m][topic]++;  
		tokensPerTopic[topic]++;  
		docLengthCounts[m]++;  
		return topic;  
	}  

	/** 
	 * Driver with example data. 
	 *  
	 * @param args 
	 */  
	public void run() {
		// TODO Auto-generated method stub
		if (! isFinished) { System.out.println("already running!"); return; }
		isFinished = false;
		// 鎵цgibbs閲囨牱
		this.gibbs();
		isFinished = true;
		try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}

	}  
}

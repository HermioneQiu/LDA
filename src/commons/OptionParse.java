package commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.swing.text.html.HTMLDocument.Iterator;

public class OptionParse {
	private String[] args;
	private String numTopics;
	private String numThreads;
	private String numIterations;
	private String numState;
	private String docFilePath;
	private String taskId;
	private String numKeyWords;
	public String getNumKeyWords() {
		this.numKeyWords = argsMap.get("-numKeyWords");
		return numKeyWords;
	}
	public String getThreshold() {
		this.threshold = argsMap.get("-threshold");
		return threshold;
		
	}
	private String threshold;
	public String getTaskId() {
		this.taskId = argsMap.get("-taskId");
		return taskId;
	}
	private boolean flag;
	public boolean isFlag() {
		return flag;
	}
	public String getDocFilePath() {
		this.docFilePath = argsMap.get("-docFilePath");
		return docFilePath;
	}
	public String getNumTopics() {
		this.numTopics = argsMap.get("-numTopics");
		return numTopics;
	}
	public String getNumThreads() {
		this.numThreads = argsMap.get("-numThreads");
		return numThreads;
	}
	public String getNumIterations() {
		this.numIterations = argsMap.get("-numIterations");
		return numIterations;
	}
	public String getNumState() {
		this.numState = argsMap.get("-numState");
		return numState;
	}
	private HashMap<String, String> argsMap= new HashMap();
	private ArrayList<String> OPTIONS = new ArrayList();
	public OptionParse(String[] args){
		this.args = args;
		OPTIONS.add("-numTopics");
		OPTIONS.add("-numThreads");
		OPTIONS.add("-numIterations");
		OPTIONS.add("-numState");
		OPTIONS.add("-docFilePath");
		OPTIONS.add("-taskId");
		OPTIONS.add("-numKeyWords");
		OPTIONS.add("-threshold");
		flag = this.parse();
	}
	public boolean parse(){
		if(args.length==16){
			for(Integer args_i=0;args_i<args.length; args_i++){
				if(OPTIONS.contains(args[args_i])){
					argsMap.put(args[args_i], args[args_i+1]);
				}
			}
			if(argsMap.size()!=8){
				System.out.println("parameter is not correct.");
				hint();
				return false;
			}else{
				 return true;
			}
		}else{
			// hint for incorrect command
			System.out.println("parameter is not complete.");
			hint();
			return false;
		}
		
	}
	public static void hint(){
		System.out.println("hints for right command to use LDA.jar");
		System.out.println("---------------------------------------");
		System.out.println("java -jar LDA.jar -numTopics args0 -numThread args1 -numIterations args2 -numState args3(default 50) -taskId args4 -numKeyWords args5 -threshold args6");
		System.out.println("---------------------------------------");
	}
	/*
	// for test 
	public static void main(String args[]){
		String[] argsTest = {"-docFilePath", "/home/", "-numThreads","2","-numTopics","4","-numIteration", "4"};
		OptionParse optionParse = new OptionParse(argsTest);
		System.out.println(optionParse.getNumTopics());
	}
	*/
}

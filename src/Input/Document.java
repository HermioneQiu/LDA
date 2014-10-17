package Input;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Document {
	private String[] docFilePaths;
	private String docFilePath;
	private Integer[][] document;
	private String tmpFileDir;
	private Map<String, Integer> alphabetMap = new HashMap();
	public Map<String, Integer> getAlphabetMap() {
		return alphabetMap;
	}
	public Map<Integer, String> getRevAlphabetMap() {
		return revAlphabetMap;
	}
	private HashSet<String> alphabetSet = new HashSet();
	private Map<Integer, String> revAlphabetMap = new HashMap();
	public Integer[][] getDocument() {
		return document;
	}
	public Document(String docFilePath, String tmpFileDir) throws IOException{
		this.docFilePath = docFilePath;
		this.tmpFileDir = tmpFileDir;
		this.initAlphabet();
		this.initDocument();
	}
	// get all alphabet from docFiles
	// now,process
	public void initAlphabet() throws IOException{
		// only have one document
//			FileReader fr = new FileReader(docFilePath);
			FileInputStream  fis = new FileInputStream(docFilePath);
		    InputStreamReader  bis = new InputStreamReader(fis,"UTF-8");
			BufferedReader bfr = new BufferedReader(bis);
			int token_i = 0;
			String line = bfr.readLine();
			
			while((line = bfr.readLine())!=null){
				String[] words = line.split("\\s+");
				for(int w_i=0; w_i<words.length; w_i++){
					if (!alphabetSet.contains(words[w_i])){
						alphabetSet.add(words[w_i]);
						alphabetMap.put(words[w_i], token_i);
						token_i += 1;
					}
				}
			}
			String alphbetFilePath = this.tmpFileDir + "alphabet.Dict";
			FileWriter fw = new FileWriter(alphbetFilePath);

			Iterator mrIt = alphabetMap.entrySet().iterator();
			while(mrIt.hasNext()){
				Entry<String, Integer> entity = (Entry<String, Integer>) mrIt.next();
				String key = entity.getKey();
				Integer val = entity.getValue();
				String lineStr = key+":"+val+"\n";
				fw.write(lineStr);
			}
			fw.close();
			// get revAlpahbetMap
			// init 
			Iterator it = alphabetMap.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String, Integer> entry = (Entry<String, Integer>) it.next();
				String key = entry.getKey();
				Integer val = entry.getValue();
				revAlphabetMap.put(val, key);
			}
	}
	// genarate document 
	public void initDocument() throws IOException{
			// every line in the file represent a doc in document
			FileInputStream fis= new FileInputStream(docFilePath);
			InputStreamReader bis =new InputStreamReader(fis, "UTF-8");
			BufferedReader bfr= new BufferedReader(bis);
			// read docLineNum from the first line of docFile
			String lineNumStr = bfr.readLine();
			Integer lineNum = Integer.parseInt(lineNumStr);
			this.document = new Integer[lineNum][];
			Integer line_i = 0;
			String line = null;
			while((line = bfr.readLine())!=null){
				String[] words = line.split("\\s+");
				Integer wordsNum = words.length;
				
				document[line_i] = new Integer[words.length];
				for(int w_i=0; w_i<words.length; w_i++){
					document[line_i][w_i] = alphabetMap.get(words[w_i]);
				}
				line_i++;
			}
		}
}

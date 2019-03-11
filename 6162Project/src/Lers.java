import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Lers {
	 private static final String SEPARATOR = System.getProperty("line.separator");
	/**
	 * key = set of attributes
	 * value = lines it occurred on
	 */
	private Map<HashSet<String>, HashSet<String>> attributeValues = new HashMap<HashSet<String>, HashSet<String>>(); 
	private Map<String, HashSet<String>> distinctAttributeValues;
	private List<String> attributeNames;
	private String result;
	
	/**
	 * value = set of attributes on left of rule arrow. Each set is a different set of attributes for a rule 
	 * key = attribute on right of arrow.
	 * Each set within the value is a set of the attributes that implies the decision attribute(key)
	 */
	private Map<String, HashSet<HashSet<String>>> certainRules = new HashMap<String, HashSet<HashSet<String>>>();
	
	/**
	 * key = 
	 * value = 
	 */
	//private Map<String, HashSet<HashSet<String>>> possibleRules;
	private ArrayList<String> possibleRules = new ArrayList<String>();
	
	/**
	 * key = set of attributes in rule, both on left and right of arrow
	 */
	private Map<HashSet<String>, Integer> rulesSupport = new HashMap<HashSet<String>, Integer>();
	//private Map<HashSet<String>, Float> rulesConfidence = new HashMap<HashSet<String>, Float>();
	private HashSet<String> decisionSetInitial;
	private HashSet<String> decisionSetTo;
	private String decisionValueInitial;
	private String decisionValueTo;
	
	public Lers(String decisionValueInitial, String decisionValueTo, Map<String, HashSet<String>> attributeValues, 
				Map<String, HashSet<String>> distinctAttributeValues, List<String> attributeNames) {
		this.decisionValueInitial = decisionValueInitial;
		this.decisionValueTo = decisionValueTo;
		this.decisionSetInitial = attributeValues.get(decisionValueInitial);
		this.decisionSetTo = attributeValues.get(decisionValueTo);
		
		HashSet<String> tempSet;
		for(Map.Entry<String, HashSet<String>> entry : attributeValues.entrySet()) {
			tempSet = new HashSet<String>();
			tempSet.add(entry.getKey());
			this.attributeValues.put(tempSet, entry.getValue());
		}

		this.distinctAttributeValues = distinctAttributeValues;
		this.attributeNames = attributeNames;
		result = "";
	}
	
	/**
	 * 
	 */
	public void runLers() {
		HashSet<String> tempSet;
		HashMap<HashSet<String>, HashSet<String>> tempValueMap = new HashMap<HashSet<String>, HashSet<String>>();
		File output = new File("output.txt");
		if(output.exists())
			output.delete();
		
		Path file = Paths.get("output.txt");
		
		try (BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE)) {
		    
		
		int loop = 0;
		
		tempSet = new HashSet<String>();
		tempSet.add(decisionValueInitial);
		attributeValues.remove(tempSet);
		
		tempSet.clear();
		tempSet.add(decisionValueTo);
		attributeValues.remove(tempSet);
		
		while(!attributeValues.isEmpty()) {
			tempValueMap.clear();
			tempValueMap.putAll(attributeValues);
			
			loop++;
			printAttributeSets(loop);			
			
			for(Map.Entry<HashSet<String>, HashSet<String>> entry : tempValueMap.entrySet()) {						
				if (decisionSetInitial.containsAll(entry.getValue())) { 
					addCertainRule(entry.getKey(), entry.getValue().size(), decisionValueInitial);
					//certainRules.put(decisionAttributeValue.get(0), entry.getValue());
					attributeValues.remove(entry.getKey());
				}else if(decisionSetTo.containsAll(entry.getValue())){
					addCertainRule(entry.getKey(), entry.getValue().size(), decisionValueTo);
					//certainRules.put(decisionAttributeValue.get(0), entry.getValue());
					attributeValues.remove(entry.getKey()); //No longer need this value to be considered
				}else {//Calculate possible rules
					if (!addPossibleRule(entry.getValue(), entry.getKey()));
						//attributeValues.remove(entry.getKey()); 
				}
			}
			
			printRules(loop);
			writer.write(result);
			result = SEPARATOR;
			combineAttributeValues(loop);
			writer.write(result);
			result = SEPARATOR;
		}
		
		
		} catch (IOException error) {
		    System.out.println(error.getStackTrace());
		}
		
		//return result;
	}

	private void printAttributeSets(int loop) {
		result += (SEPARATOR + "Loop " + loop + SEPARATOR 
							+ "Sets:" + SEPARATOR);
		
		for(Map.Entry<HashSet<String>, HashSet<String>> entry : attributeValues.entrySet()) 
			result += (entry.getKey().toString() + ": " + entry.getValue().toString() + SEPARATOR);
		
		result += ("[" + decisionValueInitial + "]: ");
		result += (decisionSetInitial.toString() + SEPARATOR);
		
		result += ("[" + decisionValueTo + "]: ");
		result += (decisionSetTo.toString() + SEPARATOR);

	}

	private void printRules(int loop) {
		result += (SEPARATOR + "Certain Rules:" + SEPARATOR);
		Iterator<HashSet<String>> iterateSets;
		HashSet<String> tempSet = new HashSet<String>();
		
		for(Map.Entry<String, HashSet<HashSet<String>>> entry : certainRules.entrySet()) {
			iterateSets = entry.getValue().iterator();
			while(iterateSets.hasNext()) {
				tempSet = new HashSet<String>();
				tempSet.addAll(iterateSets.next());
				result += (tempSet.toString()); //Set on left of certain rule
				result += (" --> ");
				result += (entry.getKey()); //Set on right of certain rule
				
				tempSet.add(entry.getKey());
				result += ("\tSupport: ");
				result += (rulesSupport.get(tempSet).toString());
				result += ("  Confidence: 100%" + SEPARATOR); //Any certain rule has 100% confidence
			}
		}
		
		result += (SEPARATOR + "Possible Rules:" + SEPARATOR);
		for(int i = 0; i<possibleRules.size(); i++) {
			result += (possibleRules.get(i));
		}
		
		possibleRules = new ArrayList<String>();
	}

	/**
	 * 
	 */
	private void combineAttributeValues(int loop) {
		Map<HashSet<String>, HashSet<String>> tempAttributeValues = new HashMap<HashSet<String>, HashSet<String>>();
		Map<HashSet<String>, HashSet<String>> tempAttributeValues2 = new HashMap<HashSet<String>, HashSet<String>>();
		HashSet<HashSet<String>> checkedKeys = new HashSet<HashSet<String>>();
		tempAttributeValues.putAll(attributeValues);
		HashSet<String> firstSetKey;
		HashSet<String> firstSetValue;
		HashSet<String> newSetKey;
		HashSet<String> newSetValue;
		String nextLineOccurrence;
		
		tempAttributeValues2.putAll(attributeValues);
		//attributeValues.putAll(tempAttributeValues2);
		
		for(Map.Entry<HashSet<String>, HashSet<String>> entry : attributeValues.entrySet()) {
			firstSetKey = entry.getKey();
			firstSetValue = entry.getValue();
			//attributeValues.remove(entry.getKey());
			//tempAttributeValues2.clear();
			//attributeValues.putAll(tempAttributeValues2);
			//entry.getKey().addAll(checkedKeys);
			//checkedKeys.add(entry.getKey());
			tempAttributeValues.remove(entry.getKey());
			
			//Iterator<HashSet<String>> setsIterator = checkedKeys.iterator();
			//while(setsIterator.hasNext()) {
			//	tempAttributeValues2.remove(setsIterator.next());
			//}
			
			//tempAttributeValues2.remove(entry.getKey());
			
			for(Map.Entry<HashSet<String>, HashSet<String>> entryRemain : tempAttributeValues2.entrySet()) {
				newSetKey = new HashSet<String>();
				newSetKey.addAll(firstSetKey);
				newSetKey.addAll(entryRemain.getKey());
				
				//Each attribute set should only have the size of the next loop. i.e. loop two should have sets of two
				if(newSetKey.size() != (loop+1))
					continue;
				
				//iterate through all lines that the first set occurs in
				Iterator<String> linesIterator = firstSetValue.iterator();
				newSetValue = new HashSet<String>();
				
				while(linesIterator.hasNext()) {
					nextLineOccurrence = linesIterator.next();
					
					//Both attribute values occur on the same line
					if(entryRemain.getValue().contains(nextLineOccurrence)) { 
						newSetValue.add(nextLineOccurrence); 
					}
				}
				
				//Don't add attribute sets that never occur together
				if(newSetValue.size() > 0) {
					boolean subsetIsRule = false;
					for(Map.Entry<String, HashSet<HashSet<String>>> ruleSets : certainRules.entrySet()) {
						Iterator<HashSet<String>> setsIterator = ruleSets.getValue().iterator();
						//Go through all certain rule sets
						while(setsIterator.hasNext()) {
							//if a set in certain rules is a subset of the new combination, don't continue with set
							if(newSetKey.containsAll(setsIterator.next()))
								subsetIsRule = true;
						}
					}
					if(!subsetIsRule)
						tempAttributeValues.put(newSetKey, newSetValue);
				}
			}
		}
		//Save newly combined sets
		attributeValues = tempAttributeValues;
	}

	/**
	 * 
	 * @param value
	 * @param decisionSetInitial
	 * @param decisionSetTo
	 * @return boolean to represent if the set is a subset of any decision value. If it is not, it should
	 * 			be removed from considered sets for future rules
	 */
	private boolean addPossibleRule(HashSet<String> value, HashSet<String> key) {
		int supportDecisionInitial = 0;
		int supportDecisionTo = 0;
		boolean isSubset = true;
		float confidence;
		String temp;
		NumberFormat formatter = new DecimalFormat("#0.00");
		
		for(String currOccurence : value.toArray(new String[value.size()])) {
			if(decisionSetInitial.contains(currOccurence)) {
				supportDecisionInitial++;
			}else if(decisionSetTo.contains(currOccurence)) {
				supportDecisionTo++;
			}
		}
		
		if(supportDecisionInitial > 0) {
			String[] keyString = key.toArray(new String[key.size()]);
			temp = keyString[0];
			for(int i = 1; i < keyString.length; i++) {
				temp += "^" + keyString[i];
			}
			temp += " --> " + decisionValueInitial;
			temp += "\tSupport:" + supportDecisionInitial;
			
			confidence = ((float)supportDecisionInitial/value.size() * 100);
			temp += " Confidence:" + formatter.format((confidence)) + "%";
			possibleRules.add(temp + SEPARATOR);
		}
		
		if(supportDecisionTo > 0) {
			String[] keyString = key.toArray(new String[key.size()]);
			temp = keyString[0];
			for(int i = 1; i < keyString.length; i++) {
				temp += "^" + keyString[i]; 
			}
			temp += " --> " + decisionValueTo;
			temp += "\tSupport:" + supportDecisionTo;
			
			confidence = ((float)supportDecisionTo/value.size() * 100);
			temp += " Confidence:" + formatter.format((confidence)) + "%";
			possibleRules.add(temp + SEPARATOR);
		}
		
		if(supportDecisionInitial == 0 && supportDecisionTo == 0) {
			isSubset = false;
		}
		
		return isSubset;
	}

	/**
	 * 
	 * @param value set of attributes on left of arrow
	 * @param key decision attribute on right of rule
	 */
	private void addCertainRule(HashSet<String> value, int support, String key) {
		HashSet<HashSet<String>> tempSet;
		HashSet<String> supportSet = new HashSet<String>();
		
		if(certainRules.containsKey(key)) {
			tempSet = certainRules.get(key);
			tempSet.add(value);
			certainRules.put(key, tempSet);
		}else{
			tempSet = new HashSet<HashSet<String>>();
			tempSet.add(value);
			certainRules.put(key, tempSet);
		}
		
		supportSet.addAll(value);
		supportSet.add(key);
		rulesSupport.put(supportSet, support);
	}

	public Map<String, HashSet<HashSet<String>>> getCertainRules(){
		return certainRules;
	}
	
}

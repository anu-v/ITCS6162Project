/**
 * Author: Group 8
 * 
 * This class manages the calculation of LERS
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
	 * Keeps track of all attribute values and their line occurrences. A hashset is used as the key
	 * to be able to accommodate the combined attribute values at each loop
	 * key = set of attributes
	 * value = lines it occurred on
	 */
	private Map<HashSet<String>, HashSet<String>> attributeValues = new HashMap<HashSet<String>, HashSet<String>>(); 
	/**
	 * String used to track what is to be written to output file
	 */
	private String result;
	/**
	 * value = set of attributes on left of rule arrow. Each set is a different set of attributes for a rule 
	 * key = attribute on right of arrow.
	 * Each set within the value is a set of the attributes that implies the decision attribute(key)
	 */
	private Map<String, HashSet<HashSet<String>>> certainRules = new HashMap<String, HashSet<HashSet<String>>>();
	
	/**
	 * As possible rules will not be directly used, we simply save the list of them as they are
	 * calculated.
	 */
	private ArrayList<String> possibleRules = new ArrayList<String>();
	
	/**
	 * Map of all certain rules and their support values
	 * key = set of attributes in rule, both on left and right of arrow
	 * value = support value 
	 */
	private Map<HashSet<String>, Integer> rulesSupport = new HashMap<HashSet<String>, Integer>();
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

		result = "";
	}
	
	/**
	 * Main logic of LERS. Runs loop to find certain rules and possible rules, then combines sets for next
	 * loop
	 */
	public void runLers() {
		HashSet<String> tempSet;
		HashMap<HashSet<String>, HashSet<String>> tempValueMap = new HashMap<HashSet<String>, HashSet<String>>();
		File output = new File("output.txt");
		int loop = 0;
		
		if(output.exists())
			output.delete();
		
		Path file = Paths.get("output.txt");
		
		try (BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE)) {		
			//Initial and final decision values won't be combined with any other attribute values
			tempSet = new HashSet<String>();
			tempSet.add(decisionValueInitial);
			attributeValues.remove(tempSet);
			tempSet.clear();
			tempSet.add(decisionValueTo);
			attributeValues.remove(tempSet);
			
			//Continue until there are no more attribute values to combine
			while(!attributeValues.isEmpty()) {
				//Temp used to allow removal of attribute values when they are certain rules
				tempValueMap.clear();
				tempValueMap.putAll(attributeValues);
				
				loop++;
				
				printAttributeSets(loop);
				writer.write(result);
				result = "";
				
				//Check each attribute value set to check if it is a certain rule
				for(Map.Entry<HashSet<String>, HashSet<String>> entry : tempValueMap.entrySet()) {						
					if (decisionSetInitial.containsAll(entry.getValue())) { 
						addCertainRule(entry.getKey(), entry.getValue().size(), decisionValueInitial);
						attributeValues.remove(entry.getKey());
					}else if(decisionSetTo.containsAll(entry.getValue())){
						addCertainRule(entry.getKey(), entry.getValue().size(), decisionValueTo);
						attributeValues.remove(entry.getKey()); 
					}else {
						addPossibleRule(entry.getValue(), entry.getKey());
					}
				}
				
				printRules(loop);
				//Result now contains all rules for the loop
				writer.write(result);
				
				combineAttributeValues(loop);
				
				result = SEPARATOR;
			}
		} catch (IOException error) {
		    System.out.println(error.getStackTrace());
		}
	}

	/**
	 * Sets all current attribute sets and their line occurrences to the result string for runLers to 
	 * save to file
	 * 
	 * @param loop Number of current loop
	 */
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

	/**
	 * Sets all current possible rules and the certain rules to the result string for runLers to 
	 * save to file
	 * 
	 * @param loop number of current loop
	 */
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
		
		//Reset possible rules as these have been printed once already
		possibleRules = new ArrayList<String>();
	}

	/**
	 * Combines the attribute values and their line occurrences for next loop of the LERS algorithm.
	 */
	private void combineAttributeValues(int loop) {
		Map<HashSet<String>, HashSet<String>> tempAttributeValues = new HashMap<HashSet<String>, HashSet<String>>();
		Map<HashSet<String>, HashSet<String>> tempAttributeValues2 = new HashMap<HashSet<String>, HashSet<String>>();
		HashSet<String> firstSetKey;
		HashSet<String> firstSetValue;
		HashSet<String> newSetKey;
		HashSet<String> newSetValue;
		String nextLineOccurrence;
		
		//Used to keep track of the newly created combined attribute values
		tempAttributeValues.putAll(attributeValues);
		
		//Used to allow a nested loop to combine a first set with a second attribute value set
		tempAttributeValues2.putAll(attributeValues);
		
		for(Map.Entry<HashSet<String>, HashSet<String>> entry : attributeValues.entrySet()) {
			firstSetKey = entry.getKey();
			firstSetValue = entry.getValue();

			tempAttributeValues.remove(entry.getKey());
			tempAttributeValues2.remove(entry.getKey());
			
			//Combine the first set with all other attribute value sets
			for(Map.Entry<HashSet<String>, HashSet<String>> entryRemain : tempAttributeValues2.entrySet()) {
				newSetKey = new HashSet<String>();
				newSetKey.addAll(firstSetKey);
				newSetKey.addAll(entryRemain.getKey());
				
				//If set was already created, don't need to calculate again
				if(tempAttributeValues.containsKey(newSetKey))
					continue;
				
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
						
						while(setsIterator.hasNext()) {
							//if a set in certain rules is a subset of the new combination, don't continue with set
							if(newSetKey.containsAll(setsIterator.next()))
								subsetIsRule = true;
						}
					}
					if(!subsetIsRule) {
						tempAttributeValues.put(newSetKey, newSetValue);
					}
				}
			}
		}
		//Save newly combined sets
		attributeValues = tempAttributeValues;
	}

	/**
	 * Calculates possible rules based on given set and line occurrences
	 * 
	 * @param value line occurrences 
	 * @param key attribute set
	 */
	private void addPossibleRule(HashSet<String> value, HashSet<String> key) {
		int supportDecisionInitial = 0;
		int supportDecisionTo = 0;
		float confidence;
		String temp;
		NumberFormat formatter = new DecimalFormat("#0.00");
		
		//Find support in possible rule to both decision values
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
	}

	/**
	 * Adds certain rule to the certainRules mapping and support value to rulesSupport
	 * 
	 * @param value set of attributes on left of arrow
	 * @param support the support value for this rule
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

	/**
	 * Retruns certain rules map
	 * @return certain rules map
	 */
	public Map<String, HashSet<HashSet<String>>> getCertainRules(){
		return certainRules;
	}
	
}

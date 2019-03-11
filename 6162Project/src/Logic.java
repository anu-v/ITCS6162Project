import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Logic {
	
	private static final String SEPARATOR = System.getProperty("line.separator");
	private List<String> attributeNames;
	private Map<String, ArrayList<String>> data;
	private String decisionValueInitial;
	private String decisionValueTo;
	private HashSet<String> stable;
	private HashSet<String> flexible;
	private int minSupport;
	private int minConfidence;
	private Lers lers;
	/**
	 * value = set of attributes on left of rule arrow. Each set is a different set of attributes for a rule. 
	 * key = attribute on right of arrow.
	 * Each set within the value is a set of the attributes that implies the decision attribute(key)
	 */
	private Map<String, HashSet<HashSet<String>>> certainRules = new HashMap<String, HashSet<HashSet<String>>>();
	/**
	 * key = from portion of action rule
	 * value = to portion of action rule
	 */
	private Map<ArrayList<String>, ArrayList<String>> actionRules;
	private Map<ArrayList<String>, ArrayList<Integer>> ruleSuppConf;
	/**
	 * Key = String of attribute name 
	 * value = Hashset of all distinct values of that attribute 
	 */
	private Map<String, HashSet<String>> distinctAttributeValues;
	
	/**
	 * Key = String of attribute name + distinct attribute value
	 * Value = Hashset of all lines that contain this value in form (x + line number)
	 */
	private Map<String, HashSet<String>> attributeValues; 
	
	public Logic() {
		attributeNames = new ArrayList<String>();
		data = new HashMap<String, ArrayList<String>>();
		actionRules = new HashMap<ArrayList<String>, ArrayList<String>>();
		distinctAttributeValues = new HashMap<String, HashSet<String>>();
		attributeValues = new HashMap<String, HashSet<String>>();
		ruleSuppConf = new HashMap<ArrayList<String>, ArrayList<Integer>>();
	}
	
	public void setDecisionAttributes() {
		
	}
	
	public void readFile(File header, File inFile) {
		String line;
		int lineNum = 0;
		String[] lineData;
		String currentValue;
		HashSet<String> tempSet = new HashSet<String>();
		String currentKey;

		try(BufferedReader reader = new BufferedReader((new FileReader(header)))) {
			while((line = reader.readLine()) != null) { //Handles if attribute names are on separated by newline
				//Read header
				for(String partition : line.split(",|\t")) {
					attributeNames.add(partition);
				}
			}
		}catch(IOException e) {
			System.out.println(e.getMessage());
		}
		
		try(BufferedReader reader = new BufferedReader((new FileReader(inFile)))) {
			//Read Data until EOF
			while((line = reader.readLine()) != null) {
				lineNum++;
				
				lineData = line.split(",|\t");
				data.put("x" + Integer.toString(lineNum), new ArrayList<String>(Arrays.asList(lineData)));
				
				for(int i = 0; i < lineData.length; i++) {
					//If data doesn't exist, skip the entry
					if(lineData[i].equals("?")) {
						continue;
					}
					
					//get distinct attributes
					currentValue = lineData[i];
					
					
					tempSet = distinctAttributeValues.get(attributeNames.get(i));
					
					//Distinct attribute value is not recorded yet
					if(tempSet == null) {
						tempSet = new HashSet<String>();
						tempSet.add(currentValue);
						distinctAttributeValues.put(attributeNames.get(i), tempSet);
					}else {
						tempSet = new HashSet<String>();
						tempSet.addAll(distinctAttributeValues.get(attributeNames.get(i)));
						tempSet.add(currentValue);
						distinctAttributeValues.put(attributeNames.get(i), tempSet);
						
					}
										
					//set data sets to attribute values
					currentKey = attributeNames.get(i) + currentValue;
					
					if(attributeValues.containsKey(currentKey)) {
						tempSet = attributeValues.get(currentKey);
						tempSet.add("x" + lineNum);
						attributeValues.put(currentKey, tempSet);
					}else { //If this is the first time the value has been seen
						tempSet = new HashSet<String>();
						tempSet.add("x" + lineNum);
						attributeValues.put(currentKey, tempSet);
					}
				}
			}
			
		} catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * Reads file with header included
	 * @param inFile
	 */
	public void readFile(String inFile) {
		String line;
		int lineNum = 0;
		String[] lineData;
		String currentValue;
		HashSet<String> tempSet = new HashSet<String>();
		String currentKey;
		
		
		try(BufferedReader reader = new BufferedReader((new FileReader(inFile)))) {
			//Read header
			line = reader.readLine();
			//splitLine = line.split(",|\t");
			for(String partition : line.split(",|\t")){
				attributeNames.add(partition);
			}
			
			//Read Data until EOF
			while((line = reader.readLine()) != null) {
				lineNum++;
				
				lineData = line.split(",|\t");
				data.put("x" + Integer.toString(lineNum), new ArrayList<String>(Arrays.asList(lineData)));
				
				for(int i = 0; i < lineData.length; i++) {
					//If data doesn't exist, skip the entry
					if(lineData[i].equals("?")) {
						continue;
					}
					
					//get distinct attributes
					currentValue = lineData[i];
					
					
					tempSet = distinctAttributeValues.get(attributeNames.get(i));
					
					//Distinct attribute value is not recorded yet
					if(tempSet == null) {
						tempSet = new HashSet<String>();
						tempSet.add(currentValue);
						distinctAttributeValues.put(attributeNames.get(i), tempSet);
					}else {
						tempSet = new HashSet<String>();
						tempSet.addAll(distinctAttributeValues.get(attributeNames.get(i)));
						tempSet.add(currentValue);
						distinctAttributeValues.put(attributeNames.get(i), tempSet);
						
					}
										
					//set data sets to attribute values
					currentKey = attributeNames.get(i) + currentValue;
					
					if(attributeValues.containsKey(currentKey)) {
						tempSet = attributeValues.remove(currentKey);
						tempSet.add("x" + lineNum);
						attributeValues.put(currentKey, tempSet);
					}else { //If this is the first time the value has been seen
						tempSet = new HashSet<String>();
						tempSet.add("x" + lineNum);
						attributeValues.put(currentKey, tempSet);
					}
				}
			}
			
		} catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param decisionValueInitial
	 * @param decisionValueTo
	 * @return
	 */
	public void runLers(String decisionValueInitial, String decisionValueTo) {
		this.decisionValueInitial = decisionValueInitial;
		this.decisionValueTo = decisionValueTo;
		lers = new Lers(decisionValueInitial, decisionValueTo, attributeValues, 
				distinctAttributeValues, attributeNames);
		 lers.runLers();
	}
	
	public void calculateActionRules() {
		certainRules = lers.getCertainRules();
		HashSet<HashSet<String>> decisionToSets  = certainRules.get(decisionValueTo);
		HashSet<String> toSet;
		Iterator<HashSet<String>> toIterator;
		ArrayList<String> toActionSet;
		List<String> header;
		List<String> headerSupportSearch;
		List<String> headerOccurrences;
		List<String> headerNames;
		ArrayList<String> attributeTest;
		List<String> attributeTestOccurrences;
		
		if(!decisionToSets.isEmpty()) {
			toIterator = decisionToSets.iterator();
			while(toIterator.hasNext()) {
				header = new ArrayList<String>();
				headerOccurrences = new ArrayList<String>();
				toSet = toIterator.next();
				ArrayList<String> toNames = new ArrayList<String>();
				headerNames = new ArrayList<String>();
				
				//TODO What about times when the to rule has no header?
				header = getHeader(toSet);
				for(String attribute : header) {
					headerNames.add(deriveAttributeName(attribute));
				}
				
				for(String attribute : toSet) {
					toNames.add(deriveAttributeName(attribute));
				}
				
				headerSupportSearch = new ArrayList<String>();
				headerSupportSearch.addAll(header);
				headerSupportSearch.add(decisionValueInitial);
				headerOccurrences = findOccurrences(headerSupportSearch);
				
				//Combine with all remaining attributes and find subsets
				for(Map.Entry<String, HashSet<String>> entry : distinctAttributeValues.entrySet()) {
					//Don't test attributes already in header or a decision value
					if(headerNames.contains(entry.getKey()) || entry.getKey().equals(deriveAttributeName(decisionValueTo))) 
							continue;
					
					//Don't test attribute values already in toSet
					for(String potentialRuleValue : entry.getValue()) {
						if(toSet.contains(entry.getKey() + potentialRuleValue)) 
							continue;
						
						attributeTest = new ArrayList<String>();
						attributeTest.addAll(header);
						attributeTest.add(entry.getKey() + potentialRuleValue);
						//Potential set for action rule is a subset of certain rule occurrences
						attributeTestOccurrences = findOccurrences(attributeTest);
						
						if(!attributeTestOccurrences.isEmpty() && headerOccurrences.containsAll(attributeTestOccurrences)) {
							if(toNames.contains(entry.getKey())) {
								toActionSet = new ArrayList<String>(toSet);
								addActionRule(attributeTest, toActionSet);
							}else {
								toActionSet = new ArrayList<String>(toSet);
								toActionSet.add(entry.getKey() + potentialRuleValue);
								addActionRule(attributeTest, toActionSet);
							}
						}
					}
				}
			}
		}
			
	}
	

	
	private void addActionRule(ArrayList<String> fromAction, ArrayList<String> toAction) {
		ArrayList<String> tempFrom = new ArrayList<String>();
		ArrayList<String> tempTo = new ArrayList<String>();

		tempFrom.addAll(fromAction);
		tempFrom.removeAll(getHeader(fromAction));
		tempTo.addAll(toAction);
		tempTo.removeAll(getHeader(toAction));
		
		
		boolean add = true;
		
		//Don't accept duplicates
		for(Map.Entry<ArrayList<String>, ArrayList<String>> entry : actionRules.entrySet()) {
			if(entry.getKey().equals(fromAction) && entry.getValue().equals(toAction))
				add = false;
		}
		
		//Only add if minimum supp/conf met
		if(!checkSupportConfidence(fromAction, toAction)) {
			add = false;
		}
		
		if(add)
			actionRules.put(fromAction, toAction);
	}

	public void printActionRules() {
		ArrayList<String> toAction;
		ArrayList<String> fromAction;
		ArrayList<String> fromNames;
		ArrayList<String> toNames;
		List<String> header;
		NumberFormat formatter = new DecimalFormat("#0.00");
		String result = "Action Rules: " + SEPARATOR;
		Path file = Paths.get("output.txt");
		
		try(BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.APPEND))  {
			for(Map.Entry<ArrayList<String>, ArrayList<String>> entry : actionRules.entrySet()) {
				fromAction = new ArrayList<String>();
				toAction = new ArrayList<String>();
				fromNames = new ArrayList<String>();
				toNames = new ArrayList<String>();
				
				fromAction.addAll(entry.getKey());
				toAction.addAll(entry.getValue());
				
				header = getHeader(toAction);
				
				for(String attribute : header) {
					if(result.isEmpty() || result.equals("Action Rules: " + SEPARATOR))
						result += "[(" + attribute + ")";
					else
						result += "^" + "(" + attribute + ")";
				}
				
				fromAction.removeAll(header);
				toAction.removeAll(header);
				
				//Add flexible attributes
				for(String name : fromAction) {
					fromNames.add(deriveAttributeName(name));
				}
				
				for(String name : toAction) {
					toNames.add(deriveAttributeName(name));
				}
				
				for(String name : toNames) { //toNames should have every attribute that fromAction has or more
					if(fromNames.contains(name)) {//When both from and to have the same attribute type
						String fromValue = fromAction.get(fromNames.indexOf(name));
						String toValue = toAction.get(toNames.indexOf(name));
						
						if(result.isEmpty()) { 
							if(fromValue.equals(toValue)) //When there is no change to flexible attribute
								result += "[(" + toValue + ")";
							else
								result += "[(" + name + ", " + fromValue + "-->" + toValue + ")";
						}else {
							if(fromValue.equals(toValue))
								result += "^(" + toValue + ")";
							else
								result += "^(" + name + ", " + fromValue + "-->" + toValue + ")";
						}
					}else { //times when the from part is blank for the attribute
						if(result.isEmpty()) {
							result += "[(" + name + ", " + "-->" + toAction.get(toNames.indexOf(name)) + ")";
						}else {
							result += "^(" + name + ", " + "-->" + toAction.get(toNames.indexOf(name)) + ")";
						}
					}
				}
				//Add right side of action rule
				result += "] --> (" + deriveAttributeName(decisionValueInitial) + ", " + decisionValueInitial + 
						"-->" + decisionValueTo + ")";
				
				//Add supp/conf
				ArrayList<Integer> suppConf = ruleSuppConf.get(entry.getKey());
				result += "\tSupport: " + suppConf.get(0);
				result += "\tConfidence: " + formatter.format((suppConf.get(1))) + "%" + SEPARATOR;
				writer.write(result);
				result = "";
			}
		}catch (IOException error) {
		    System.out.println(error.getStackTrace());
		}
		
		
		//return result;
	}
	
	public void setMinSupportConfidence(int support, int confidence) {
		minSupport = support;
		minConfidence = confidence;
	}
	
	public boolean checkSupportConfidence(ArrayList<String> fromAction, ArrayList<String> toAction) {
		boolean add = true;
		int supportFrom = 0;
		int supportFromDec = 0;
		int supportTo = 0;
		int supportToDec = 0;
		int support;
		int confidence;
		HashSet<String> supportSet = new HashSet<String>();
		HashSet<String> temp;
		HashSet<String> remove;
		
		//Find support of from attributes
		for(String attribute : fromAction) {
			temp = attributeValues.get(attribute);
			
			if(supportSet.isEmpty()) { //First iteration. Take first attributes lines
				supportSet.addAll(temp);
				continue;
			}else {
				remove = new HashSet<String>();
				for(String potentialLine : supportSet) {
					if(!temp.contains(potentialLine))
						remove.add(potentialLine); //The set does not occur together on that line
				}
				
				supportSet.removeAll(remove);
				
				if(supportSet.isEmpty()) //Set does not occur together at all. Reiteration would add next attributes lines
					break;
			}
		}
		supportFrom = supportSet.size();
		
		//Find support of from attributes with from decision attribute
		temp = attributeValues.get(decisionValueInitial);
		
		remove = new HashSet<String>();
		for(String potentialLine : supportSet) {
			if(!temp.contains(potentialLine))
				remove.add(potentialLine); //The set does not occur together on that line
		}
		
		supportSet.removeAll(remove);
		supportFromDec = supportSet.size();
		
		//Find support of to attributes
		supportSet = new HashSet<String>();
		for(String attribute : toAction) {
			temp = attributeValues.get(attribute);
			
			if(supportSet.isEmpty()) { //First iteration. Take first attributes lines
				supportSet.addAll(temp);
				continue;
			}else {
				remove = new HashSet<String>();
				for(String potentialLine : supportSet) {
					if(!temp.contains(potentialLine))
						remove.add(potentialLine); //The set does not occur together on that line
				}
				
				supportSet.removeAll(remove);
				
				if(supportSet.isEmpty()) //Set does not occur together at all. Reiteration would add next attributes lines
					break;
			}
		}
		supportTo = supportSet.size();
		
		//Find support of to attributes with to decision attribute
		temp = attributeValues.get(decisionValueTo);
		remove = new HashSet<String>();
		for(String potentialLine : supportSet) {
			if(!temp.contains(potentialLine))
				remove.add(potentialLine); //The set does not occur together on that line
		}
		
		supportSet.removeAll(remove);
		supportToDec = supportSet.size();
		
		if(supportFromDec < supportToDec)
			support = supportFromDec;
		else
			support = supportToDec;
		
		if(supportFrom == 0 || supportTo == 0) 
			confidence = 0;
		else
			confidence = (supportFromDec/supportFrom) * (supportToDec/supportTo) * 100;
		
		if(support < minSupport)
			add = false;
		
		if(confidence < minConfidence)
			add = false;
		
		if(add) {
			ArrayList<Integer> suppConf = new ArrayList<Integer>();
			suppConf.add(support);
			suppConf.add(confidence);
			ruleSuppConf.put(fromAction, suppConf);
		}
		
		return add;
	}
	
	
	/**
	 * 
	 * @param supportSearch
	 * @return
	 */
	public List<String> findOccurrences(List<String> supportSearch){
		HashSet<String> temp = new HashSet<String>();
		ArrayList<String> supportSet = new ArrayList<String>();
		HashSet<String> remove = new HashSet<String>();
		
		for(String attribute : supportSearch) {
			temp = attributeValues.get(attribute);
			
			if(supportSet.isEmpty()) { //First iteration. Take first attributes lines
				supportSet.addAll(temp);
				continue;
			}else {
				remove = new HashSet<String>();
				for(String potentialLine : supportSet) {
					if(!temp.contains(potentialLine))
						remove.add(potentialLine); //The set does not occur together on that line
				}
				
				supportSet.removeAll(remove);
				
				if(supportSet.isEmpty()) //Set does not occur together at all. Reiteration would add next attributes lines
					break;
			}
		}
		return supportSet;
	}
	
	/**
	 * 
	 * @param supportSearch
	 * @return
	 */
	private int[] getAttributePos(List<String> supportSearch) {
		int[] supportPos = new int[supportSearch.size()];
		
		for(int i = 0; i < supportSearch.size(); i++) {
			String searchName = deriveAttributeName(supportSearch.get(i));
			for(int k = 0; k < attributeNames.size(); k++) {
				if(attributeNames.get(k).equals(searchName)) {
					supportPos[i] = k;
					break;
				}
			}
		}
		
		return supportPos;
	}
	
	private List<String> getHeader(ArrayList<String> set) {
		ArrayList<String> header = new ArrayList<String>();
		
		for(String attribute: set) {
			if(stable.contains(deriveAttributeName(attribute)))
					header.add(attribute);
		}
		
		return header;
	}
	
	private List<String> getHeader(HashSet<String> set) {
		ArrayList<String> header = new ArrayList<String>();
		
		for(String attribute: set) {
			if(stable.contains(deriveAttributeName(attribute)))
					header.add(attribute);
		}
		
		return header;
	}
	
	public String getAttributeValue(String value) {
		String aValue = "";
		
		for(String currName : attributeNames) {
			if(value.startsWith(currName)) {
				aValue = value.substring(currName.length());
				break;
			}
		}
		
		return aValue;
	}
	
	/**
	 * 
	 * @param value a value of attribute name + attribute value
	 * @return the attribute name 
	 */
	public String deriveAttributeName(String value) {
		String name = "";
		
		for(String currName : attributeNames) {
			if(value.startsWith(currName)) {
				name = currName;
				break;
			}
		}
		
		return name;		
	}
	
	public List<String> getAttributeNames() {
		return attributeNames;
	}

	public Map<String, HashSet<String>> getDistinctAttributeValues() {
		return distinctAttributeValues;
	}
	
	public HashSet<String> getDistinctAttributeValues(String key){
		return distinctAttributeValues.get(key);
	}

	public void setStableFlexible(HashSet<String> stable) {
		this.stable = stable;
		
		flexible = new HashSet<String>();
		for(String name : attributeNames) {
			if(!stable.contains(name)) {
				flexible.add(name);
			}
		}
	}
}

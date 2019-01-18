
package jmetal.problems;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.rits.cloning.Cloner;

import elements.Test;
import elements.TestAPI;
import elements.TestCase;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.wrapper.XReal;

/**
 * Class representing problem ZDT3
 */
public class Implantation extends Problem {
	private ArrayList<TestCase> caseList = new ArrayList<TestCase>();
	private ArrayList<String> caseOutTemp;
	private ArrayList<TestCase> caseSelected = new ArrayList<TestCase>();
	private ArrayList<TestCase> caseForMutation = new ArrayList<TestCase>();

	private double totalExecutionTime;

	private Set<String> totalAPIUsed = new HashSet<String>();
	private Set<String> totalConfigUsed = new HashSet<String>();
	private List<String> constraintList = new ArrayList<>(Arrays.asList("audio_only", "auto_answer"));
	private Map<String, ArrayList<String>> testConfigMap = new HashMap<String, ArrayList<String>>(); // all
	private Map<String, List<Map<String, String>>> neededAPITest = new HashMap<String, List<Map<String, String>>>();
	private Map<String, ArrayList<String>> neededConfigTest = new HashMap<String, ArrayList<String>>(); // all
	private int neededAPITestSize;
	private int configMapSize;

	// hashmap needed configuration along with its value
	private Map<String, HashSet<String>> configMap = new HashMap<String, HashSet<String>>();

	// for the fitness function
	private Map<String, List<Map<String, String>>> coveredAPI = new HashMap<String, List<Map<String, String>>>();
	private Map<String, HashSet<String>> coveredConfigMap = new HashMap<String, HashSet<String>>();
	private int modificationLine = 0;
	private double takenExecutionTime = 0;
	private double modifiedTestCaseNum = 0;

	public void resetValues() {
		this.coveredAPI = new HashMap<String, List<Map<String, String>>>();
		this.coveredConfigMap = new HashMap<String, HashSet<String>>();
		this.takenExecutionTime = 0;
		this.modificationLine = 0;
		this.modifiedTestCaseNum = 0;
	}

	public Implantation(String solutionType) throws ClassNotFoundException {
		this(solutionType, 118); // 118 variables by default
	}

	/**
	 * Constructor. Creates a instance of ZDT3 problem.
	 * 
	 * @param numberOfVariables
	 *            Number of variables.
	 * @param solutionType
	 *            The solution type must "Real", "BinaryReal, and "ArrayReal".
	 */
	public Implantation(String solutionType, Integer numberOfVariables) {
		numberOfVariables_ = numberOfVariables.intValue();
		numberOfObjectives_ = 5;
		numberOfConstraints_ = 0;
		problemName_ = "Implantation";

		upperLimit_ = new double[numberOfVariables_];
		lowerLimit_ = new double[numberOfVariables_];

		for (int var = 0; var < numberOfVariables_; var++) {
			lowerLimit_[var] = 0;
			upperLimit_[var] = 1;
		} // for

		if (solutionType.compareTo("BinaryReal") == 0)
			solutionType_ = new BinaryRealSolutionType(this);
		else if (solutionType.compareTo("Real") == 0)
			solutionType_ = new RealSolutionType(this);
		else if (solutionType.compareTo("ArrayReal") == 0)
			solutionType_ = new ArrayRealSolutionType(this);
		else {
			System.out.println("Error: solution type " + solutionType + " invalid");
			System.exit(-1);
		}
	}

	public ArrayList<TestCase> reshuffleAndSelect(Solution solution) throws JMException {
		XReal vars = new XReal(solution);
		Map<Double, Integer> numVariablesHash = new HashMap<Double, Integer>();
		List<Double> numVariables = new ArrayList<Double>();
		// sort the numbers
		for (int i = 0; i < numberOfVariables_; i++) {
			numVariables.add(vars.getValue(i));
			numVariablesHash.put(vars.getValue(i), i);
		}
		Collections.sort(numVariables, Collections.reverseOrder());

		ArrayList<TestCase> tempCase2 = new ArrayList<TestCase>();
		caseOutTemp = new ArrayList<String>();

		Set<String> configUsed = new HashSet<String>();
		Set<String> apiUsed = new HashSet<String>();
		for (int i = 0; i < numberOfVariables_; i++) {
			TestCase tempCase = solution.getSelectedCaseList().get(numVariablesHash.get(numVariables.get(i)));

			if (tempCase.getExecutionTime() > 81)
				continue;

			tempCase2.add(tempCase);
			caseOutTemp.add(tempCase.getName());

			configUsed.addAll(tempCase.getConfigUsed());
			apiUsed.addAll(tempCase.getApiUsed());

			if (i + 1 < numVariables.size()) {
				if (numVariables.get(i + 1) <= 0.5) // number of test API commands
					break;
			}
		}
		return tempCase2;
	}

	// to measure the feature pairwise coverage
	public int calculatePC(Map<String, List<Map<String, String>>> neededAPI) {
		List<String> parValueList = new ArrayList<String>();
		for (String key : neededAPI.keySet()) {
			for (Map<String, String> parameter : neededAPI.get(key)) {
				List<String> keyList = new ArrayList<String>(parameter.keySet());

				for (int i = 0; i < keyList.size(); i++) {
					for (int j = i + 1; j < keyList.size(); j++) {
						String key2 = keyList.get(i);
						String key3 = keyList.get(j);

						if (!parValueList
								.contains(key + " " + key2 + parameter.get(key2) + " " + key3 + parameter.get(key3))) {
							// System.out.println(key+" "+key2+parameter.get(key2)+"
							// "+key3+parameter.get(key3));
							parValueList.add(key + " " + key2 + parameter.get(key2) + " " + key3 + parameter.get(key3));
						}
					}
				}
			}
		}
		return (parValueList.size());
	}

	// merge the hashmaps
	public Map<String, String> mergeValues(Map<String, String> from, Map<String, String> to) {
		for (String key : to.keySet()) {
			if (!from.containsKey(key))
				from.put(key, to.get(key));
		}
		return from;
	}

	public boolean checkAPI(Map<String, String> from, Map<String, String> to) {
		boolean ret = true;
		for (String keyFrom : from.keySet()) {
			if (!keyFrom.equalsIgnoreCase("timeout") && !keyFrom.equalsIgnoreCase("check_media")) {
				if (to.get(keyFrom) != null) {
					if (!from.get(keyFrom).equals(to.get(keyFrom))) {
						ret = false;
						break;
					}
				}
			}
		}
		return ret;
	}

	// check only the configurations
	public boolean checkAPIOnlyConf(Map<String, String> from, Map<String, String> to) {
		boolean ret = true;
		// System.out.println(from.keySet().size());
		for (String keyFrom : from.keySet()) {
			// System.out.println(keyFrom);
			if (keyFrom.equalsIgnoreCase("protocol") || keyFrom.equalsIgnoreCase("bandwidth")) {
				if (to.get(keyFrom) != null) {
					if (!from.get(keyFrom).equals(to.get(keyFrom))) {
						ret = false;
						break;
					}
				}
			}
		}
		return ret;
	}

	// check and modify only the configurations
	public boolean modifyAPIOnlyConf(Map<String, String> from, Map<String, String> to) {
		boolean ret = false;
		int check = 0;
		// System.out.println(from.keySet().size());
		for (String keyFrom : from.keySet()) {
			// System.out.println(keyFrom);
			if (keyFrom.equalsIgnoreCase("protocol") || keyFrom.equalsIgnoreCase("bandwidth")) {
				if (to.get(keyFrom) != null && keyFrom.equalsIgnoreCase("protocol")) {
					if (from.get(keyFrom).equals(to.get(keyFrom))) {
						check++;
					}

				}
			}
		}
		if (check > 0) {
			to.put("bandwidth", from.get("bandwidth"));
			ret = true;
		}
		return ret;
	}

	// modify the configurations final option
	public Map<String, String> addAPIConf(Map<String, String> from, Map<String, String> to) {
		to.put("protocol", from.get("protocol"));
		to.put("bandwidth", from.get("bandwidth"));
		return to;
	}

	public TestCase getTestCaseFromMap(Map<String, TestCase> testCaseRemoval, String key) {
		TestCase ret = testCaseRemoval.get(key);
		return ret;
	}

	// remove the apis without the proper configuration
	public List<Map<String, String>> removeAPIPairwise(List<Map<String, String>> apiParmAndValue, TestCase testCase) {
		boolean present = false, remove = false;
		for (String config : testCase.getConfigUsed()) {
			if (config.equalsIgnoreCase("interface")) {
				present = true;
				break;
			}
		}
		if (!present) {
			Iterator<Map<String, String>> iter = apiParmAndValue.iterator();
			while (iter.hasNext()) {
				Map<String, String> tempHash = iter.next();
				remove = false;
				if (tempHash.get("interface_name") != null) {
					if (!tempHash.get("interface_name").contains("None"))
						remove = true;
				}
				if (remove)
					iter.remove();
			}
		}
		return apiParmAndValue;
	}

	private List<Map<String, String>> removeConstraints(List<Map<String, String>> apiParmAndValue,
			Map<String, String> paramValue, String api) {
		for (String constraint : constraintList) {
			if (paramValue.containsKey(constraint)) {
				Iterator<Map<String, String>> iter = apiParmAndValue.iterator();
				while (iter.hasNext()) {
					Map<String, String> tempHash = iter.next();
					if (!tempHash.get(constraint).equalsIgnoreCase(paramValue.get(constraint)))
						iter.remove();
				}
			}
		}
		return apiParmAndValue;
	}

	// remove the apis without the proper configuration
	public List<Map<String, String>> removeAPIPairwiseConstraints(List<Map<String, String>> apiParmAndValue, Test test,
			String api) {
		Map<String, String> paramValue = test.getParameterWithValue().get(api);

		apiParmAndValue = removeConstraints(apiParmAndValue, paramValue, api);
		return apiParmAndValue;
	}

	/**
	 * Evaluates a solution
	 * 
	 * @param solution
	 *            The solution to evaluate
	 * @throws Exception
	 */
	public void evaluate(Solution solution) throws Exception {
		double[] f = new double[numberOfObjectives_];

		// get coverage
		findCoverage(solution.getSelectedCaseList());
		if (solution.getSelectedCaseList().size() > 0) {
			double configNum = neededConfigMapSize(this.coveredConfigMap);
			int numPair = calculatePC(this.coveredAPI);

			f[0] = 1 - Nor(numPair);
			f[1] = 1 - Nor(configNum);
			f[2] = Nor(this.modificationLine);
			f[3] = Nor(this.modifiedTestCaseNum);
			f[4] = Nor(this.takenExecutionTime);

			for (int i = 0; i < numberOfObjectives_; i++) {
				solution.setObjective(i, f[i]);
			}
			solution.setCaseList(this.caseList);
			cases = new String[caseOutTemp.size()];
			cases = caseOutTemp.toArray(cases);
			solution.setCases_(cases);
		} else {
			for (int i = 0; i < numberOfObjectives_; i++) {
				solution.setObjective(i, 1);
			}
			cases = new String[0];
			solution.setCases_(cases);
		}
	} // evaluate

	// check if initial configuration API are already set
	public boolean checkInitialConfigurationAPI(TestCase testCase) {
		boolean initialConfigurationAPIPresent = false;
		if (testCase.apiUsed.size() > 0) {
			for (Test test : testCase.getTestListArray()) {
				if (test.getParameterWithValue().size() > 0) {
					initialConfigurationAPIPresent = true;
					break;
				}
			}
		} else {
			initialConfigurationAPIPresent = true;
		}
		return initialConfigurationAPIPresent;
	}

	// check if initial configuration config are already set
	public boolean checkInitialConfigurationConfig(TestCase testCase) {
		boolean initialConfigurationConfigPresent = false;
		if (testCase.configUsed.size() > 0) {
			for (Test test : testCase.getTestListArray()) {
				if (test.getConfigWithValue().size() > 0) {
					initialConfigurationConfigPresent = true;
					break;
				}
			}
		} else {
			initialConfigurationConfigPresent = true;
		}
		return initialConfigurationConfigPresent;
	}

	// sort based on highest api pairwise required to lower
	public void changeConfig(TestCase testCase, Test test, Map<String, List<Map<String, String>>> notCoveredAPI) {
		List<Map<String, String>> apiParmAndValueActual;

		Map<String, Integer> numVariablesHash = new HashMap<String, Integer>();
		for (String api : test.apiUsed) {
			apiParmAndValueActual = notCoveredAPI.get(api);
			Cloner cloner = new Cloner();
			List<Map<String, String>> apiParmAndValue = cloner.deepClone(apiParmAndValueActual); // this for removing
			// API
			if (apiParmAndValue == null || apiParmAndValue.size() == 0) {
				continue;
			}
			numVariablesHash.put(api, apiParmAndValue.size());
		}
		entriesSortedByValues(numVariablesHash);

		List<String> requiredAPI = new ArrayList<String>();
		for (Map.Entry<String, Integer> map : numVariablesHash.entrySet())
			requiredAPI.add(map.getKey());
		test.setApiUsed(requiredAPI);
	}

	// sorting based on values
	static <K, V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
		List<Entry<K, V>> sortedEntries = new ArrayList<Entry<K, V>>(map.entrySet());

		Collections.sort(sortedEntries, new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e1, Entry<K, V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});
		return sortedEntries;
	}

	// use from random if all pairwise have been tested
	public Map<String, String> useRandom(String key, TestCase testCase, Map<String, String> apiParmMerge, Test test) {
		List<Map<String, String>> neededAPI = neededAPITest.get(key);
		Cloner cloner = new Cloner();
		List<Map<String, String>> apiParmAndValue = cloner.deepClone(neededAPI);
		// remove interface
		// apiParmAndValue = removeAPIPairwise(apiParmAndValue, testCase);
		apiParmAndValue = removeAPIPairwiseConstraints(apiParmAndValue, test, key);
		Map<String, String> apiParm = new HashMap<String, String>();
		boolean isSelect = false;

		while (!isSelect) {
			int num = generateRandom(0, apiParmAndValue.size());
			apiParm = apiParmAndValue.get(num);
			if (apiParmMerge.size() > 0)
				isSelect = checkAPIOnlyConf(apiParmMerge, apiParm);
			else
				isSelect = true;
		}
		return apiParm;
	}

	// configuration covered
	public void findConfigCovered(TestCase testCase) {
		for (Test test : testCase.getTestListArray()) {
			for (Map.Entry<String, String> entry : test.getConfigWithValue().entrySet()) {
				HashSet<String> tempSet = new HashSet<String>();

				if (this.coveredConfigMap.containsKey(entry.getKey()))
					tempSet.addAll(this.coveredConfigMap.get(entry.getKey()));
				tempSet.add(entry.getValue());
				this.coveredConfigMap.put(entry.getKey(), tempSet);
			}
		}

		if (testCase.configUsed.size() > 0) {
			Map<String, HashSet<String>> configCovered = testCase.getConfigCovered();
			if (configCovered != null) {
				for (Map.Entry<String, HashSet<String>> entry : configCovered.entrySet()) {
					HashSet<String> tempSet = new HashSet<String>();
					if (this.coveredConfigMap != null) {
						if (this.coveredConfigMap.containsKey(entry.getKey())) {
							tempSet.addAll(this.coveredConfigMap.get(entry.getKey()));
						}
					}
					tempSet.addAll(entry.getValue());
					this.coveredConfigMap.put(entry.getKey(), tempSet);
				}
			}
		}
	}

	// modify covered config based on API coverage
	public void configCoveredBasedOnAPI(Map<String, List<Map<String, String>>> coveredAPI) {
		HashSet<String> modifyableAPI = new HashSet<>(Arrays.asList("protocol", "bandwidth", "timeout"));
		for (String key : coveredAPI.keySet()) {
			for (Map<String, String> parameter : coveredAPI.get(key)) {
				for (Map.Entry<String, String> entry : parameter.entrySet()) {
					HashSet<String> tempSet = new HashSet<String>();
					if (modifyableAPI.contains(entry.getKey())) {
						if (this.coveredConfigMap.get(entry.getKey()) != null)
							tempSet = this.coveredConfigMap.get(entry.getKey());
						String text = "";
						if (entry.getValue().length() > 0)
							text = entry.getValue().substring(0, 1).toUpperCase()
									+ entry.getValue().substring(1).toLowerCase();
						if (entry.getKey().equals("bandwidth")) {
							if (text.contains("64") || text.contains("65") || text.contains("5999")
									|| text.contains("6000")) {
								tempSet.add(text);
								this.coveredConfigMap.put(entry.getKey(), tempSet);
							}
						} else {
							tempSet.add(text);
							this.coveredConfigMap.put(entry.getKey(), tempSet);
						}
					}
				}
			}
		}
	}

	// random mutation of the test cases
	public void testCaseMutation(Solution solution) throws Exception {
		// modifyTestCases(solution);
		this.caseForMutation = reshuffleAndSelect(solution);
		solution.setCaseForMutation(this.caseForMutation);
		// double mutationProbability = 0.00847457627118644;
		double mutationProbability = 0.33;
		Cloner cloner = new Cloner();
		this.neededConfigTest = cloner.deepClone(this.testConfigMap); // needed config

		for (TestCase testCase : this.caseForMutation) {
			if (testCase.getApiUsed().size() > 0 || testCase.configUsedTest.size() > 0) {
				if (PseudoRandom.randDouble() < mutationProbability) {
					mutationNum(2, testCase);
				}
				// modified it
				else if (PseudoRandom.randDouble() < mutationProbability) {
					mutationNum(0, testCase);
				} else if (PseudoRandom.randDouble() < mutationProbability) {
					mutationNum(1, testCase);
				}
			}
		}
	}

	// mutation number stating what to do
	private void mutationNum(int value, TestCase testCase) {
		Test test = new Test();
		Map<String, List<Map<String, String>>> notCoveredAPI = new HashMap<String, List<Map<String, String>>>();
		test = findToChange(testCase);

		switch (value) {
		case 0:
			notCoveredAPI = findCoverageTestCase(this.caseForMutation, testCase, test);
			// notCoveredAPI = findCoverageTestCaseOne(testCase, test);
			if (test != null && (test.apiUsed.size() > 0 || test.getConfigUsed().size() > 0)) {
				modifyOneTest(test, notCoveredAPI, testCase);
				testCase.setModified(true);
			}
			break;
		case 1:
			notCoveredAPI = findCoverageTestCaseToAdd(this.caseForMutation);
			// notCoveredAPI = findCoverageTestCaseOne(testCase, test);
			if (test != null && (test.apiUsed.size() > 0 || test.getConfigUsed().size() > 0)) {
				addTest(test, notCoveredAPI, testCase);
				testCase.setModified(true);
			}
			break;
		case 2:
			// find the present coverage first
			notCoveredAPI = findCoverageForDeletion(this.caseForMutation, testCase, test);
			if (test != null) {
				// delete the redundant api
				deleteTestUnnecessary(test, testCase, notCoveredAPI);
				// deleteTest(test, testCase);
				testCase.setModified(true);
			}
			break;
		}
	}

	// delete test
	private void deleteTestUnnecessary(Test test, TestCase testCase,
			Map<String, List<Map<String, String>>> coveredAPI) {
		boolean unique = true;
		for (String api : test.getApiUsed()) {
			List<Map<String, String>> coveredAPIParam = new ArrayList<Map<String, String>>();
			if (coveredAPI.get(api) != null)
				coveredAPIParam = coveredAPI.get(api);
			Map<String, String> coveredAPIParamTest = test.getParameterWithValue().get(api);
			if (coveredAPIParam.contains(coveredAPIParamTest)) {
				unique = false;// covered includes this so safe to delete
			}
		}
		if (!unique) {// covered includes this so safe to delete
			if (test.isDeleted() == false) {
				// double time = testCase.getExecutionTime();
				double time = testCase.getExecutionTime() - test.getNumAPI() * testCase.getTimeOneAPI()
						- test.getConfigUsed().size() * testCase.getTimeOneAPI();

				testCase.setExecutionTime(time);
			}
			int i = 0;
			if (test.isAdded() == true)
				testCase.testListArray.remove(test);
			else {
				for (String text : test.getText()) {
					if (text.contains("\"\"\"")) {
						i++;
					}
					if (i == 2)
						i = 0;
					if (!text.contains("#") && i != 1)
						testCase.modificationLine++;
				}
				test.setDeleted(true);
			}
		}
	}

	private boolean modifyAPI(Test test) {
		boolean isAPI = false;
		Map<Integer, String> testList = new HashMap<Integer, String>();
		int counter = 0;
		for (int i = 0; i < test.getApiUsed().size(); i++) {
			testList.put(counter, "api");
			counter++;
		}
		for (int i = 0; i < test.getConfigUsed().size(); i++) {
			testList.put(counter, "config");
			counter++;
		}

		int value = generateRandom(0, counter);
		String modify = testList.get(value);
		if (modify.equalsIgnoreCase("api"))
			isAPI = true;
		return isAPI;
	}

	// modify one api in a test
	private void modifyOneTest(Test test, Map<String, List<Map<String, String>>> notCoveredAPI, TestCase testCase) {
		boolean isModified = false;
		if (modifyAPI(test)) {
			// random API to modify
			int value = generateRandom(0, test.getApiUsed().size());

			// convert to list
			List<String> list = new ArrayList<String>(test.getApiUsed());
			// swap the api to be modified to the first position
			Collections.swap(list, 0, value);

			List<Map<String, String>> apiParmAndValueActual;
			boolean firstAPI = true;
			boolean modifiedFirstAPI = false;

			Map<String, String> apiParm = new HashMap<String, String>();
			Map<String, String> apiParmMerge = new HashMap<String, String>();
			for (String api : list) {
				apiParmAndValueActual = notCoveredAPI.get(api);
				Cloner cloner = new Cloner();
				// this for removing the one without config in API
				List<Map<String, String>> apiParmAndValue = cloner.deepClone(apiParmAndValueActual);
				// break if the api was not modified
				if (firstAPI == false && modifiedFirstAPI == false)
					break;
				// first check if not covered API has value
				if (apiParmAndValueActual == null || apiParmAndValueActual.size() == 0) {
					Map<String, List<Map<String, String>>> notCoveredAPI2 = findCoverageTestCaseOne(testCase, test);
					apiParmAndValue = notCoveredAPI2.get(api);
				}
				if (apiParmAndValue == null || apiParmAndValue.size() == 0) {
					firstAPI = false;
					// if still empty put random that fits
					if (modifiedFirstAPI) {
						apiParmAndValue = cloner.deepClone(this.neededAPITest.get(api));
					}
					if (apiParmAndValue == null)
						continue;
				}
				// remove interface
				// apiParmAndValue = removeAPIPairwise(apiParmAndValue,
				// testCase);
				apiParmAndValue = removeAPIPairwiseConstraints(apiParmAndValue, test, api);

				if (apiParmAndValue == null || apiParmAndValue.size() == 0) {
					firstAPI = false;
					continue;
				}
				// modify the API
				if (firstAPI) {
					int num = generateRandom(0, apiParmAndValue.size());
					apiParm = apiParmAndValue.get(num);
					if (apiParmAndValue.size() > 0) {
						test.parameterWithValue.put(api, apiParm);
						apiParmAndValueActual.remove(apiParm);
						apiParmAndValue.remove(num);
						mergeValues(apiParmMerge, apiParm);
						if (test.isAdded() == false) {
							// check if the particular api has already been
							// modified
							// before
							if (!test.apiIsModified.get(api)) {
								testCase.modificationLine++;
								// testCase.modificationLine += 4;
								test.apiIsModified.put(api, true);
							}
							isModified = true;
							modifiedFirstAPI = true;
						}
					}
				} else { // now modify other apis influenced by it
					if (apiParmMerge.size() == 0)
						break;
					// check if need for modification
					Map<String, String> current = test.parameterWithValue.get(api);
					boolean isSelect = false;
					isSelect = checkAPI(apiParmMerge, current);
					if (isSelect) // no need to change
						continue;
					// now modify if it does not match
					Iterator<Map<String, String>> iter = apiParmAndValue.iterator();
					isSelect = false;
					while (iter.hasNext()) {
						Map<String, String> checkTemp = iter.next();
						isSelect = checkAPIOnlyConf(apiParmMerge, checkTemp);
						if (isSelect) {
							test.parameterWithValue.put(api, checkTemp);
							iter.remove();
							apiParmAndValueActual.remove(checkTemp);
							mergeValues(apiParmMerge, checkTemp);
							if (test.isAdded() == false) {
								if (!test.apiIsModified.get(api)) {
									testCase.modificationLine++;
									test.apiIsModified.put(api, true);
								}
								isModified = true;
							}
							break;
						}
					}
					if (!isSelect) { // check only wrt the current test case
						Map<String, List<Map<String, String>>> notCoveredAPI2 = findCoverageTestCaseOne(testCase, test);
						apiParmAndValue = notCoveredAPI2.get(api);
						apiParmAndValue = removeAPIPairwiseConstraints(apiParmAndValue, test, api);
						Iterator<Map<String, String>> iter2 = apiParmAndValue.iterator();
						while (iter2.hasNext()) {
							Map<String, String> checkTemp = iter2.next();
							isSelect = checkAPIOnlyConf(apiParmMerge, checkTemp);
							if (isSelect) {
								test.parameterWithValue.put(api, checkTemp);
								iter2.remove();
								apiParmAndValueActual.remove(checkTemp);
								mergeValues(apiParmMerge, checkTemp);

								if (test.isAdded() == false) {
									if (!test.apiIsModified.get(api)) {
										testCase.modificationLine++;
										test.apiIsModified.put(api, true);
									}
									isModified = true;
								}
								break;
							}
						}
						if (!isSelect) { // still cannot find or not available
							// modify one
							iter2 = apiParmAndValue.iterator();
							while (iter2.hasNext()) {
								Map<String, String> checkTemp = iter2.next();
								isSelect = modifyAPIOnlyConf(apiParmMerge, checkTemp);
								if (isSelect) {
									test.parameterWithValue.put(api, checkTemp);
									iter2.remove();
									apiParmAndValueActual.remove(checkTemp);
									mergeValues(apiParmMerge, checkTemp);

									test.setModified(true);
									break;
								} else {
									// still cannot find, final option
									checkTemp = addAPIConf(apiParmMerge, apiParmAndValue.get(0));
									test.parameterWithValue.put(api, checkTemp);
									iter2.remove();
									apiParmAndValueActual.remove(checkTemp);
									mergeValues(apiParmMerge, checkTemp);

									test.setModified(true);
									break;
								}
							}
						}
					}
				}
				firstAPI = false;
			}

		} else {
			int value = generateRandom(0, test.getConfigUsed().size());
			String configToChange = test.getConfigUsed().get(value);

			if (this.neededConfigTest.containsKey(configToChange)) {
				if (!this.neededConfigTest.get(configToChange).isEmpty()) {
					String configValue = this.neededConfigTest.get(configToChange).get(0);
					if (!configValue.equals(test.getConfigWithValue().get(configToChange))) {
						isModified = true;
						test.getConfigWithValue().put(configToChange, configValue);
					}
				}
			}
		}
		if (isModified) {
			test.comment += "# was modified";
			test.setModified(true);
		}
	}

	// modify one api in a test
	private boolean addOneTest(Test test, Map<String, List<Map<String, String>>> notCoveredAPI, TestCase testCase) {
		boolean isDifferent = false;
		int line = 0;

		if (!modifyAPI(test)) {
			for (int i = 0; i < test.getConfigUsed().size(); i++) {
				int value = i;
				String configToChange = test.getConfigUsed().get(value);
				if (this.neededConfigTest.containsKey(configToChange)) {
					if (!this.neededConfigTest.get(configToChange).isEmpty()) {
						String configValue = this.neededConfigTest.get(configToChange).get(0);
						if (!configValue.equals(test.getConfigWithValue().get(configToChange))) {
							isDifferent = true;
							test.getConfigWithValue().put(configToChange, configValue);
							line++;
						}
					}
				}
			}
		}
		if (test.getApiUsed().size() > 0) {
			// random API to modify
			int value = generateRandom(0, test.getApiUsed().size());

			List<String> list = new ArrayList<String>(test.getApiUsed());//
			// convert to list
			// changeConfig(testCase, test, notCoveredAPI);
			// swap the api to be modified to the first position
			Collections.swap(list, 0, value);

			List<Map<String, String>> apiParmAndValueActual;
			boolean firstAPI = true;

			Map<String, String> apiParm = new HashMap<String, String>();
			Map<String, String> apiParmMerge = new HashMap<String, String>();

			test.setModified(true);

			List<String> tempAPIList = new ArrayList<String>();
			boolean modifiedFirstAPI = false;

			for (String api : list) {
				if (firstAPI == false && modifiedFirstAPI == false)
					break;
				if (tempAPIList.contains(api))
					continue;
				tempAPIList.add(api);
				line++;
				apiParmAndValueActual = notCoveredAPI.get(api);
				Cloner cloner = new Cloner();
				List<Map<String, String>> apiParmAndValue = cloner.deepClone(apiParmAndValueActual);

				if ((api.equalsIgnoreCase("consultative_transfer")
						|| api.equalsIgnoreCase("blind_transfer") && testCase.getTestListArray().size() > 2)) {
					List<Map<String, Map<String, String>>> temp2 = new ArrayList<Map<String, Map<String, String>>>();
					for (Test testTemp : testCase.getTestListArray()) {
						if (testTemp.isDeleted() == false) {
							temp2.add(testTemp.getParameterWithValue());
						}
					}
				}

				// first check if not covered API has value
				if (apiParmAndValueActual == null || apiParmAndValueActual.size() == 0) {
					// if not covered API is empty search wrt the particular
					// test case
					Map<String, List<Map<String, String>>> notCoveredAPI2 = findCoverageTestCaseOne(testCase, test);
					apiParmAndValue = notCoveredAPI2.get(api);
				}

				if (apiParmAndValue == null || apiParmAndValue.size() == 0) {
					// if still empty put random that fits
					apiParmAndValue = cloner.deepClone(this.neededAPITest.get(api));
					firstAPI = false;
					if (apiParmAndValue == null)
						continue;
				}
				apiParmAndValue = removeAPIPairwiseConstraints(apiParmAndValue, test, api);

				// System.out.println(apiParmAndValue.size());
				if (apiParmAndValue == null || apiParmAndValue.size() == 0) {
					apiParm = useRandom(api, testCase, apiParmMerge, test);
					test.parameterWithValue.put(api, apiParm);
					mergeValues(apiParmMerge, apiParm);
					firstAPI = false;
					continue;
				}

				if (firstAPI) {
					int num = generateRandom(0, apiParmAndValue.size());
					apiParm = apiParmAndValue.get(num);
					if (apiParmAndValue.size() > 0) {
						test.parameterWithValue.put(api, apiParm);
						apiParmAndValueActual.remove(apiParm);
						apiParmAndValue.remove(num);
						mergeValues(apiParmMerge, apiParm);
						firstAPI = false;
						modifiedFirstAPI = true;
						isDifferent = true;
					}
				} else {
					Iterator<Map<String, String>> iter = apiParmAndValue.iterator();
					boolean isSelect = false;
					while (iter.hasNext()) {
						Map<String, String> checkTemp = iter.next();
						// isSelect = checkAPI(apiParmMerge, checkTemp);
						isSelect = checkAPIOnlyConf(apiParmMerge, checkTemp);
						if (isSelect) {
							test.parameterWithValue.put(api, checkTemp);
							iter.remove();
							apiParmAndValueActual.remove(checkTemp);
							mergeValues(apiParmMerge, checkTemp);

							break;
						}
					}
					if (!isSelect) { // check only wrt the current test case
						Map<String, List<Map<String, String>>> notCoveredAPI2 = findCoverageTestCaseOne(testCase, test);
						apiParmAndValue = notCoveredAPI2.get(api);
						apiParmAndValue = removeAPIPairwiseConstraints(apiParmAndValue, test, api);
						Iterator<Map<String, String>> iter2 = apiParmAndValue.iterator();
						while (iter2.hasNext()) {
							Map<String, String> checkTemp = iter2.next();
							isSelect = checkAPIOnlyConf(apiParmMerge, checkTemp);
							if (isSelect) {
								test.parameterWithValue.put(api, checkTemp);
								iter2.remove();
								apiParmAndValueActual.remove(checkTemp);
								mergeValues(apiParmMerge, checkTemp);

								break;
							}
						}
						if (!isSelect) { // still cannot find or not available
							// modify one

							iter2 = apiParmAndValue.iterator();
							while (iter2.hasNext()) {
								Map<String, String> checkTemp = iter2.next();
								isSelect = modifyAPIOnlyConf(apiParmMerge, checkTemp);
								if (isSelect) {
									test.parameterWithValue.put(api, checkTemp);
									iter2.remove();
									apiParmAndValueActual.remove(checkTemp);
									mergeValues(apiParmMerge, checkTemp);

									break;
								}
							}
						}
					}
				}
			}
		}
		if (isDifferent)
			testCase.setModificationLine(testCase.getModificationLine() + line);
		return isDifferent;
	}

	// add a mini test
	private void addTest(Test test, Map<String, List<Map<String, String>>> notCoveredAPI, TestCase testCase) {
		Test testNew = new Test(test.getApiUsed(), test.getName(), test.getText(), test.getNumAPI(),
				test.getParameterWithValue(), test.getConfigUsed(), test.getConfigWithValue());
		int miniTestNumber = testCase.testListArray.size() + 1;
		testNew.setName(testNew.getName().replaceAll("\\d+", miniTestNumber + ""));
		boolean isDifferent = addOneTest(testNew, notCoveredAPI, testCase);
		if (isDifferent) {
			testNew.comment += "# was added";
			testNew.setAdded(true);
			testCase.getTestListArray().add(testNew);

			double time;
			time = testCase.getExecutionTime() + test.getNumAPI() * testCase.getTimeOneAPI()
					+ test.getConfigUsed().size() * testCase.getTimeOneAPI();

			testCase.setExecutionTime(time);

		}
	}

	// uncovered API with respect to all test case for modification
	public Map<String, List<Map<String, String>>> findCoverageTestCase(ArrayList<TestCase> caseList, TestCase testCaseS,
			Test unselectTest) {
		Cloner cloner = new Cloner();
		// this.coveredAPI = new HashMap<String, List<Map<String, String>>>();
		Map<String, List<Map<String, String>>> neededAPI = cloner.deepClone(neededAPITest); // total pairwise testing
		for (TestCase testCase : caseList) {
			// if test case has some APIs
			if (checkInitialConfigurationAPI(testCase)) {
				for (Test test : testCase.getTestListArray()) {
					if (test.isDeleted() == false && test != unselectTest) {
						for (Map.Entry<String, Map<String, String>> parameterValue : test.getParameterWithValue()
								.entrySet()) {
							if (!parameterValue.getValue().isEmpty())
								neededAPI.get(parameterValue.getKey()).remove(parameterValue.getValue());
						}
					}
				}
			}
			// if test case has some config
			if (checkInitialConfigurationConfig(testCase)) {
				for (Test test : testCase.getTestListArray()) {
					if (test.isDeleted() == false && test != unselectTest) {
						for (Map.Entry<String, String> configValue : test.getConfigWithValue().entrySet()) {
							if (this.neededConfigTest.containsKey(configValue.getKey()))
								if (!this.neededConfigTest.get(configValue.getKey()).isEmpty())
									this.neededConfigTest.get(configValue.getKey()).remove(configValue.getValue());
						}
					}
				}
			}
		}
		return neededAPI;
	}

	// get the covered API with respect to all test case except the mini test
	public Map<String, List<Map<String, String>>> findCoverageForDeletion(ArrayList<TestCase> caseList,
			TestCase testCaseS, Test unselectTest) {
		Map<String, List<Map<String, String>>> coveredAPI = new HashMap<String, List<Map<String, String>>>();

		for (TestCase testCase : caseList) {
			// if test case has some API then only do
			if (checkInitialConfigurationAPI(testCase)) {

				for (Test test : testCase.getTestListArray()) {
					if (testCase.getName().equalsIgnoreCase(testCaseS.getName()) && test == unselectTest)
						continue;
					else {
						mergeCoveredAPI(coveredAPI, test.getParameterWithValue());
					}
				}
			}
			// if test case has some config
			if (checkInitialConfigurationConfig(testCase)) {
				for (Test test : testCase.getTestListArray()) {
					if (testCase.getName().equalsIgnoreCase(testCaseS.getName()) && test == unselectTest)
						continue;
					else {
						for (Map.Entry<String, String> configValue : test.getConfigWithValue().entrySet()) {
							if (this.neededConfigTest.containsKey(configValue.getKey()))
								if (!this.neededConfigTest.get(configValue.getKey()).isEmpty())
									this.neededConfigTest.get(configValue.getKey()).remove(configValue.getValue());
						}
					}
				}
			}
		}
		return coveredAPI;
	}

	public void mergeCoveredAPI(Map<String, List<Map<String, String>>> coveredAPI,
			Map<String, Map<String, String>> testAPI) {
		for (Map.Entry<String, Map<String, String>> parameterValue : testAPI.entrySet()) {
			List<Map<String, String>> tempList = new ArrayList<Map<String, String>>();
			if (!coveredAPI.containsKey(parameterValue.getKey())) {
				tempList.add(parameterValue.getValue());
				coveredAPI.put(parameterValue.getKey(), tempList);
			} else {
				tempList = coveredAPI.get(parameterValue.getKey());
				if (!tempList.contains(parameterValue.getValue())) {
					coveredAPI.put(parameterValue.getKey(), tempList);
				}
			}
		}
	}

	// uncovered API with respect to all test case for adding
	public Map<String, List<Map<String, String>>> findCoverageTestCaseToAdd(ArrayList<TestCase> caseList) {
		Cloner cloner = new Cloner();
		// this.coveredAPI = new HashMap<String, List<Map<String, String>>>();
		Map<String, List<Map<String, String>>> neededAPI = cloner.deepClone(neededAPITest); // total pairwise testing

		for (TestCase testCase : caseList) {
			if (checkInitialConfigurationAPI(testCase)) {
				for (Test test : testCase.getTestListArray()) {
					if (test.isDeleted() == false) {
						for (Map.Entry<String, Map<String, String>> parameterValue : test.getParameterWithValue()
								.entrySet()) {
							if (!parameterValue.getValue().isEmpty())
								neededAPI.get(parameterValue.getKey()).remove(parameterValue.getValue());
						}
					}
				}
			}
			// if test case has some config
			if (checkInitialConfigurationConfig(testCase)) {
				for (Test test : testCase.getTestListArray()) {
					if (test.isDeleted() == false) {
						for (Map.Entry<String, String> configValue : test.getConfigWithValue().entrySet()) {
							if (this.neededConfigTest.containsKey(configValue.getKey()))
								if (!this.neededConfigTest.get(configValue.getKey()).isEmpty())
									this.neededConfigTest.get(configValue.getKey()).remove(configValue.getValue());
						}

					}
				}
			}
		}
		return neededAPI;
	}

	private Map<String, List<Map<String, String>>> findCoverageTestCaseOne(TestCase testCase, Test unselectTest) {
		Cloner cloner = new Cloner();
		Map<String, List<Map<String, String>>> neededAPI = cloner.deepClone(neededAPITest); // total pairwise testing

		for (Test test : testCase.getTestListArray()) {
			if (test != unselectTest && test.isDeleted() == false) {
				for (Map.Entry<String, Map<String, String>> parameterValue : test.getParameterWithValue().entrySet()) {
					if (!parameterValue.getValue().isEmpty())
						neededAPI.get(parameterValue.getKey()).remove(parameterValue.getValue());
				}
			}
		}
		return neededAPI;
	}

	// find which mini test to modify
	private Test findToChange(TestCase testCase) {
		int val = 0;
		Map<Integer, Test> testList = new HashMap<Integer, Test>();
		for (Test test : testCase.getTestListArray()) {
			if (test.getApiUsed().size() > 0 || test.getConfigUsed().size() > 0) {
				testList.put(val, test);
				val++;
			}
		}
		if (val == 0)
			return null;
		else {
			int value = generateRandom(0, val);
			return testList.get(value);
		}
	}

	// get the overall coverage by the selected test cases before modifying
	public Map<String, List<Map<String, String>>> findCoverage(List<TestCase> list) {
		this.resetValues();
		Map<String, List<Map<String, String>>> neededAPITemp = new HashMap<String, List<Map<String, String>>>();
		boolean addDirectly = true;

		for (TestCase testCase : list) {
			// only if test case has some configuration
			if (checkInitialConfigurationAPI(testCase)) {
				for (Test test : testCase.getTestListArray()) {
					if (test.isDeleted() == false) {
						for (Map.Entry<String, Map<String, String>> parameterValue : test.getParameterWithValue()
								.entrySet()) {
							Map<String, String> paramWithValue = parameterValue.getValue();
							neededAPITemp = checkCoveredAPI(neededAPITemp, addDirectly, parameterValue.getKey(),
									paramWithValue); // for the covered APIs
							addDirectly = false;
						}
					}
				}
			}
			findConfigCovered(testCase);

			this.takenExecutionTime += testCase.getExecutionTime();
			if (testCase.isModified() == true) {
				this.modifiedTestCaseNum++;
				this.modificationLine += testCase.getModificationLine();
			}
		}
		this.coveredAPI = neededAPITemp;
		configCoveredBasedOnAPI(this.coveredAPI);
		return neededAPITemp;
	}

	// remove the covered ones
	public Map<String, List<Map<String, String>>> notCoveredAPI(Map<String, List<Map<String, String>>> neededAPI,
			Map<String, List<Map<String, String>>> neededAPITemp) {
		for (Map.Entry<String, List<Map<String, String>>> parameterValue : neededAPI.entrySet()) {
			if (neededAPITemp != null) {
				if (neededAPITemp.containsKey(parameterValue.getKey())) {
					List<Map<String, String>> coveredList = neededAPITemp.get(parameterValue.getKey());
					parameterValue.getValue().removeAll(coveredList);
				}
			}

		}
		return neededAPI;
	}

	// check and add the coverage
	public Map<String, List<Map<String, String>>> checkCoveredAPI(Map<String, List<Map<String, String>>> neededAPITemp,
			boolean addDirectly, String key, Map<String, String> paramWithValue) {
		boolean add = true;
		if (addDirectly == false) {
			if (neededAPITemp.get(key) != null) {
				List<Map<String, String>> listAPI = neededAPITemp.get(key);
				if (!listAPI.contains(paramWithValue)) {
					listAPI.add(paramWithValue);
				}
				add = false;
			}
		}
		if (add) {
			List<Map<String, String>> listAPITemp = new ArrayList<Map<String, String>>();
			listAPITemp.add(paramWithValue);
			neededAPITemp.put(key, listAPITemp);
		}
		return neededAPITemp;
	}

	// count the api pairwise testing
	public int countPairwise(Map<String, List<Map<String, String>>> neededAPITest) {
		int retCount = 0;
		for (String api : this.totalAPIUsed) {
			List<Map<String, String>> a = neededAPITest.get(api);
			if (a != null)
				retCount += neededAPITest.get(api).size();
			else
				retCount++;
		}
		return retCount;
	}

	public void compare() {

	}

	@SuppressWarnings("unchecked")
	public HashSet<String> getParameters(String item) throws Exception {
		TestAPI testAPI = new TestAPI();
		// Get the private field
		final Field field = testAPI.getClass().getDeclaredField(item);
		// Allow modification on the field
		field.setAccessible(true);

		// Return the Object corresponding to the field
		return ((HashSet<String>) field.get(testAPI));
	}

	// normalization function
	public double Nor(double n) {
		double m = n / (n + 1);
		return m;
	}

	// Generate random
	public int randInt(int min, int max) {
		Random rand = new Random();
		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

	public void evaluateConstraints(Solution solution) throws JMException {

	}

	public int generateRandom(int min, int max) { // generate random
		int ret = 0;
		if (max > 1)
			ret = ThreadLocalRandom.current().nextInt(0, max);
		return ret;
	}

	public double evalH(double f, double g) {
		double h = 0.0;
		h = 1.0 - java.lang.Math.sqrt(f / g) - (f / g) * java.lang.Math.sin(10.0 * java.lang.Math.PI * f);
		return h;
	}

	private int neededConfigMapSize(Map<String, HashSet<String>> configMap) {
		int retSize = 0;
		for (String key : configMap.keySet()) {
			int a = configMap.get(key).size();
			if (a == 0)
				a = 1;
			retSize += configMap.get(key).size();
		}
		return retSize;
	}

	public ArrayList<TestCase> getCaseList() {
		return caseList;
	}

	public void setCaseList(ArrayList<TestCase> caseList) {
		this.caseList = caseList;
	}

	public Set<String> getTotalAPIUsed() {
		return totalAPIUsed;
	}

	public void setTotalAPIUsed(Set<String> totalAPIUsed) {
		this.totalAPIUsed = totalAPIUsed;
	}

	public Set<String> getTotalConfigUsed() {
		return totalConfigUsed;
	}

	public void setTotalConfigUsed(Set<String> totalConfigUsed) {
		this.totalConfigUsed = totalConfigUsed;
	}

	public double getTotalExecutionTime() {
		return totalExecutionTime;
	}

	public void setTotalExecutionTime(double totalExecutionTime) {
		this.totalExecutionTime = totalExecutionTime;
	}

	public Map<String, List<Map<String, String>>> getNeededAPITest() {
		return neededAPITest;
	}

	public void setNeededAPITest(Map<String, List<Map<String, String>>> neededAPITest) {
		this.neededAPITest = neededAPITest;
	}

	public Map<String, HashSet<String>> getConfigMap() {
		return configMap;
	}

	public void setConfigMap(Map<String, HashSet<String>> configMap) {
		this.configMap = configMap;
	}

	public int getNeededAPITestSize() {
		return neededAPITestSize;
	}

	public void setNeededAPITestSize(int neededAPITestSize) {
		this.neededAPITestSize = neededAPITestSize;
	}

	public int getConfigMapSize() {
		return configMapSize;
	}

	public void setConfigMapSize(int configMapSize) {
		this.configMapSize = configMapSize;
	}

	public ArrayList<TestCase> getCaseSelected() {
		return caseSelected;
	}

	public void setCaseSelected(ArrayList<TestCase> caseSelected) {
		this.caseSelected = caseSelected;
	}

	public ArrayList<TestCase> getCaseForMutation() {
		return caseForMutation;
	}

	public void setCaseForMutation(ArrayList<TestCase> caseForMutation) {
		this.caseForMutation = caseForMutation;
	}

	public Map<String, ArrayList<String>> getTestConfigMap() {
		return testConfigMap;
	}

	public void setTestConfigMap(Map<String, ArrayList<String>> testConfigMap) {
		this.testConfigMap = testConfigMap;
	}
}
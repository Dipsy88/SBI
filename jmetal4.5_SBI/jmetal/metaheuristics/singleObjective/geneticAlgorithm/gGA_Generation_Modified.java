//  gGA.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.metaheuristics.singleObjective.geneticAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.rits.cloning.Cloner;

import elements.Test;
import elements.TestCase;
import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.comparators.ObjectiveComparator;

/**
 * Class implementing a generational genetic algorithm
 */
public class gGA_Generation_Modified extends Algorithm {

	/**
	 *
	 * Constructor Create a new GGA instance.
	 * 
	 * @param problem
	 *            Problem to solve.
	 */
	public gGA_Generation_Modified(Problem problem) {
		super(problem);
	} // GGA

	/**
	 * Execute the GGA algorithm
	 * 
	 * @throws Exception
	 */
	public SolutionSet execute() throws Exception {
		int populationSize;
		int maxEvaluations;
		int evaluations;

		SolutionSet population;
		SolutionSet offspringPopulation;

		Operator mutationOperator;
		Operator crossoverOperator;
		Operator selectionOperator;

		Comparator comparator;
		comparator = new ObjectiveComparator(0); // Single objective comparator

		// Read the params
		populationSize = ((Integer) this.getInputParameter("populationSize")).intValue();
		maxEvaluations = ((Integer) this.getInputParameter("maxEvaluations")).intValue();

		// Initialize the variables
		population = new SolutionSet(populationSize);
		offspringPopulation = new SolutionSet(populationSize);

		evaluations = 0;

		// Read the operators
		mutationOperator = this.operators_.get("mutation");
		crossoverOperator = this.operators_.get("crossover");
		selectionOperator = this.operators_.get("selection");

		// Create the initial solutionSet
		Solution newSolution;
		// problem_.modifyAllOnce(problem_.getCaseList());

		findCoverage(problem_.getCaseList());
		writeFile();

		// set initial case list
		for (int i = 0; i < populationSize; i++) {
			Cloner cloner = new Cloner();
			ArrayList<TestCase> testCaseList = cloner.deepClone(problem_.getCaseList());
			problem_.caseListArray.add(testCaseList);
		}

		int count = 0;
		while (count < populationSize) {
			newSolution = new Solution(problem_);
			problem_.setCaseList(problem_.caseListArray.get(count));
			newSolution.setSelectedCaseList(problem_.caseListArray.get(count));
			problem_.testCaseMutation(newSolution);
			problem_.evaluate(newSolution);
			problem_.evaluateConstraints(newSolution);

			findCoverage(newSolution.getSelectedCaseList());
			int numPair = calculatePC(this.coveredAPI);
			// if (numPair >= 212) {
			evaluations++;
			population.add(newSolution);
			count++;
			// }
		}

		// Sort population
		population.sort(comparator);
		while (evaluations < maxEvaluations) {
			// if ((evaluations % 10) == 0) {
			if ((evaluations > 1000)) {
				System.out.println(evaluations + ": " + population.get(0).getObjective(0));
			} //
			Solution sol1 = population.get(0);

			Solution sol2 = population.get(1);
			// Copy the best two individuals to the offspring population
			offspringPopulation.add(sol1);
			evaluations++;
			offspringPopulation.add(sol2);
			evaluations++;
			int counter = 0;
			while (counter < (populationSize - 2)) {
				// Reproductive cycle
				if (evaluations < maxEvaluations) {
					// Selection
					Solution[] parents = new Solution[2];

					parents[0] = (Solution) selectionOperator.execute(population);
					parents[1] = (Solution) selectionOperator.execute(population);

					// Crossover
					Solution[] offspring = (Solution[]) crossoverOperator.execute(parents);

					// Mutation
					mutationOperator.execute(offspring[0]);
					mutationOperator.execute(offspring[1]);

					// Evaluation of the new individual
					problem_.testCaseMutation(offspring[0]);
					problem_.evaluate(offspring[0]);
					problem_.testCaseMutation(offspring[1]);
					problem_.evaluate(offspring[1]);

					findCoverage(offspring[0].getSelectedCaseList());
					int numPair = calculatePC(this.coveredAPI);
					if (numPair >= 212) {
						evaluations++;
						offspringPopulation.add(offspring[0]);
						counter++;
					}
					findCoverage(offspring[1].getSelectedCaseList());
					numPair = calculatePC(this.coveredAPI);
					if (numPair >= 212 && counter < populationSize) {
						evaluations++;
						offspringPopulation.add(offspring[1]);
						counter++;
					}
				} else
					break;

			} // for
				// The offspring population becomes the new current population
			population.clear();
			for (int i = 0; i < populationSize; i++) {
				population.add(offspringPopulation.get(i));
			}
			offspringPopulation.clear();
			population.sort(comparator);
		} // while

		// Return a population with the best individual
		SolutionSet resultPopulation = new SolutionSet(1);
		resultPopulation.add(population.get(0));

		// System.out.println("Evaluations: " + evaluations ) ;
		return resultPopulation;
	} // execute

	public Map<String, List<Map<String, String>>> findCoverage(List<TestCase> list) {
		this.resetValues();
		Map<String, List<Map<String, String>>> neededAPITemp = new HashMap<String, List<Map<String, String>>>();
		boolean addDirectly = true;

		for (TestCase testCase : list) {
			if (checkInitialConfiguration(testCase)) { // if test case has some
														// configuration then
														// only do
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
			this.selectedTestCaseNum++;

			if (testCase.isModified() == true) {
				this.modifiedTestCaseNum++;
				this.modificationLine += testCase.getModificationLine();
			}
		}
		this.coveredAPI = neededAPITemp;
		configCoveredBasedOnAPI(this.coveredAPI);
		return neededAPITemp;
	}

	// configuration covered
	public void findConfigCovered(TestCase testCase) {
		for (Test test : testCase.getTestListArray()) {
			for (Map.Entry<String, String> entry : test.getConfigWithValue().entrySet()) {
				int a;
				HashSet<String> tempSet = new HashSet<String>();
				// if (entry.getKey().contains("PreferredIPSignaling")){
				// a = 3;
				// System.out.println(entry.getValue());
				// }
				if (this.coveredConfigMap.containsKey(entry.getKey()))
					tempSet.addAll(this.coveredConfigMap.get(entry.getKey()));
				tempSet.add(entry.getValue());
				this.coveredConfigMap.put(entry.getKey(), tempSet);
			}

		}
		// also add system
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

		int a;
		a = 4;
	}

	public int countCoveredAPI() {
		int retSize = 0;
		for (String key : this.coveredAPI.keySet()) {
			int a = this.coveredAPI.get(key).size();
			if (a == 0)
				a = 1;
			retSize += a;
		}
		return retSize;
	}

	private int neededConfigMapSize() {
		int retSize = 0;
		for (String key : this.coveredConfigMap.keySet()) {
			int a = this.coveredConfigMap.get(key).size();
			if (a == 0)
				a = 1;
			retSize += this.coveredConfigMap.get(key).size();
		}
		return retSize;
	}

	public void writeFile() {
		int apiPairwise = this.countCoveredAPI();
		int configCovered = this.neededConfigMapSize();
		int numPair = calculatePC(this.coveredAPI);

		System.out.println("api: " + apiPairwise);

		System.out.println("pairWise: " + numPair);
		System.out.println("configuration: " + configCovered);
		System.out.println("modification: " + this.modificationLine);
		System.out.println("selected: " + this.selectedTestCaseNum);
		System.out.println("executionTime: " + this.takenExecutionTime);
		System.out.println("modifiedCases: " + this.modifiedTestCaseNum);
	}

	// to measure the feature pairwise coverage based on talk with Shuai
	public int calculatePC(Map<String, List<Map<String, String>>> neededAPI) {
		int retPairNum = 0;
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
		return retPairNum = parValueList.size();
	}

	/*
	 * // to measure the feature pairwise coverage public int
	 * calculatePC(Map<String, List<Map<String, String>>> neededAPI) { int
	 * retPairNum = 0; Map<String, List<String>> uniquePair = new HashMap<String,
	 * List<String>>(); for (String key : neededAPI.keySet()) { for (Map<String,
	 * String> parameter : neededAPI.get(key)) { for (Map.Entry<String, String>
	 * entry : parameter.entrySet()) { String newValue = entry.getKey() +
	 * entry.getValue(); if (uniquePair.containsKey(key)) { List<String> valueList =
	 * uniquePair.get(key);
	 * 
	 * if (valueList.contains(newValue)) continue; else { valueList.add(newValue);
	 * retPairNum++; } } else { List<String> valueList = new ArrayList<String>();
	 * uniquePair.put(key, valueList); retPairNum++; } } } } return retPairNum; }
	 * 
	 */

	private Map<String, List<Map<String, String>>> coveredAPI = new HashMap<String, List<Map<String, String>>>();
	private Map<String, HashSet<String>> coveredConfigMap = new HashMap<String, HashSet<String>>();
	private double takenExecutionTime = 0;
	private double selectedTestCaseNum = 0;
	private int modificationLine = 0;
	private double modifiedTestCaseNum = 0;

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

	// check if initial configuration are already set
	public boolean checkInitialConfiguration(TestCase testCase) {
		boolean initialConfigurationPresent = false;
		if (testCase.apiUsed.size() > 0) {
			for (Test test : testCase.getTestListArray()) {
				if (test.getParameterWithValue().size() > 0) {
					initialConfigurationPresent = true;
					break;
				}
			}
		} else {
			initialConfigurationPresent = true;
		}
		return initialConfigurationPresent;
	}

	public void resetValues() {
		this.coveredAPI = new HashMap<String, List<Map<String, String>>>();
		this.coveredConfigMap = new HashMap<String, HashSet<String>>();
		this.takenExecutionTime = 0;
		this.selectedTestCaseNum = 0;
		this.modificationLine = 0;
		this.modifiedTestCaseNum = 0;
	}

} // gGA
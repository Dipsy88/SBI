//  NSGAII.java
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

package jmetal.metaheuristics.nsgaII;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rits.cloning.Cloner;

import elements.Test;
import elements.TestCase;
//import machineLearning.ML_ClassifyInstanceTest_allNew_2_3Obj;
import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Distance;
import jmetal.util.Ranking;
import jmetal.util.comparators.CrowdingComparator;

/**
 * Implementation of NSGA-II. This implementation of NSGA-II makes use of a
 * QualityIndicator object to obtained the convergence speed of the algorithm.
 * This version is used in the paper: A.J. Nebro, J.J. Durillo, C.A. Coello
 * Coello, F. Luna, E. Alba "A Study of Convergence Speed in Multi-Objective
 * Metaheuristics." To be presented in: PPSN'08. Dortmund. September 2008.
 */

public class NSGAII_Generation_Modified extends Algorithm {
	/**
	 * Constructor
	 * 
	 * @param problem
	 *            Problem to solve
	 */
	public NSGAII_Generation_Modified(Problem problem) {
		super(problem);
	} // NSGAII

	File fileName;

	/**
	 * Runs the NSGA-II algorithm.
	 * 
	 * @return a <code>SolutionSet</code> that is a set of non dominated solutions
	 *         as a result of the algorithm execution
	 * @throws Exception
	 */
	public SolutionSet execute() throws Exception {
		int populationSize;
		int maxEvaluations;
		int evaluations;

		QualityIndicator indicators; // QualityIndicator object
		int requiredEvaluations; // Use in the example of use of the
		// indicators object (see below)

		SolutionSet population;
		SolutionSet offspringPopulation;
		SolutionSet union;

		Operator mutationOperator;
		Operator crossoverOperator;
		Operator selectionOperator;

		Distance distance = new Distance();

		// Read the parameters
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
		indicators = (QualityIndicator) getInputParameter("indicators");

		// Initialize the variables
		population = new SolutionSet(populationSize);
		evaluations = 0;

		requiredEvaluations = 0;

		// Read the operators
		mutationOperator = operators_.get("mutation");
		crossoverOperator = operators_.get("crossover");
		selectionOperator = operators_.get("selection");

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
		while (count < 100) {
			newSolution = new Solution(problem_);
			problem_.setCaseList(problem_.caseListArray.get(count));
			newSolution.setSelectedCaseList(problem_.caseListArray.get(count));

			problem_.testCaseMutation(newSolution);
			problem_.evaluate(newSolution);
			problem_.evaluateConstraints(newSolution);

			findCoverage(newSolution.getSelectedCaseList());
			int numPair = calculatePC(this.coveredAPI);
			if (numPair >= 212) {
				evaluations++;
				population.add(newSolution);
				count++;
			}

		} // for

		// double increase = 0.02;
		// Generations
		while (evaluations < maxEvaluations) {
			// Create the offSpring solutionSet
			offspringPopulation = new SolutionSet(populationSize);
			Solution[] parents = new Solution[2];
			int counter = 0;
			while (counter < (populationSize)) {
				// for (int i = 0; i < (populationSize / 2); i++) {
				if (evaluations < maxEvaluations) {
					// obtain parents
					parents[0] = (Solution) selectionOperator.execute(population);
					parents[1] = (Solution) selectionOperator.execute(population);
					Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
					mutationOperator.execute(offSpring[0]);
					mutationOperator.execute(offSpring[1]);

					problem_.testCaseMutation(offSpring[0]);
					problem_.evaluate(offSpring[0]);
					problem_.evaluateConstraints(offSpring[0]);
					// offSpring[0].setSelectedCaseList(problem_.getCaseSelected());
					// offSpring[0].setCases_(problem_.getCases());

					problem_.testCaseMutation(offSpring[1]);
					problem_.evaluate(offSpring[1]);
					problem_.evaluateConstraints(offSpring[1]);
					// offSpring[1].setSelectedCaseList(problem_.getCaseSelected());
					// offSpring[1].setCases_(problem_.getCases());
					findCoverage(offSpring[0].getSelectedCaseList());
					// if (this.countCoveredAPI() >= 35 + increase
					// && counter < populationSize
					// && this.neededConfigMapSize() >= 40) {
					int numPair = calculatePC(this.coveredAPI);
					if (numPair >= 212) {
						evaluations++;
						offspringPopulation.add(offSpring[0]);
						counter++;
					}
					findCoverage(offSpring[1].getSelectedCaseList());
					numPair = calculatePC(this.coveredAPI);
					if (numPair >= 212 && counter < populationSize) {
						evaluations++;
						offspringPopulation.add(offSpring[1]);
						counter++;
					}

					// System.out.println("evaluations: " + evaluations);
				} // if
			} // for
				// if (increase < 2)
			SolutionSet tempPopulation = new SolutionSet(population.size());
			for (int k = 0; k < population.size(); k++) {
				// Solution sol = population.get(k);
				// findCoverage(sol.getSelectedCaseList());
				// if (this.countCoveredAPI() >= 35 + increase
				// && this.neededConfigMapSize() >= 40)
				tempPopulation.add(population.get(k));

			}
			// if (increase < 1)
			// increase += 0.02;
			System.out.println("evaluations: " + evaluations);

			// Create the solutionSet union of solutionSet and offSpring
			// union = ((SolutionSet) population).union(offspringPopulation);
			union = ((SolutionSet) tempPopulation).union(offspringPopulation);
			// for (int a = 0; a < population.size(); a++) {
			// int b;
			// if (population.get(a).getSelectedCaseList().size() < 1)
			// b = 3;
			// }

			// Ranking the union
			Ranking ranking = new Ranking(union);

			int remain = populationSize;
			int index = 0;
			SolutionSet front = null;
			population.clear();

			// Obtain the next front
			front = ranking.getSubfront(index);

			while ((remain > 0) && (remain >= front.size())) {
				// Assign crowding distance to individuals
				distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
				// Add the individuals of this front
				for (int k = 0; k < front.size(); k++) {
					population.add(front.get(k));
				} // for

				// Decrement remain
				remain = remain - front.size();

				// Obtain the next front
				index++;
				if (remain > 0) {
					front = ranking.getSubfront(index);
				} // if
			} // while

			// Remain is less than front(index).size, insert only the best one
			if (remain > 0) { // front contains individuals to insert
				distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
				front.sort(new CrowdingComparator());
				for (int k = 0; k < remain; k++) {
					population.add(front.get(k));
				} // for

				remain = 0;
			} // if

			// This piece of code shows how to use the indicator object into the
			// code
			// of NSGA-II. In particular, it finds the number of evaluations
			// required
			// by the algorithm to obtain a Pareto front with a hypervolume
			// higher
			// than the hypervolume of the true Pareto front.
			if ((indicators != null) && (requiredEvaluations == 0)) {
				double HV = indicators.getHypervolume(population);
				if (HV >= (0.98 * indicators.getTrueParetoFrontHypervolume())) {
					requiredEvaluations = evaluations;
				} // if
			} // if

			// System.out.println(evaluations);
		} // while

		// Return as output parameter the required evaluations
		setOutputParameter("evaluations", requiredEvaluations);

		// Return the first non-dominated front
		Ranking ranking = new Ranking(population);
		ranking.getSubfront(0).printFeasibleFUN("FUN_NSGAII");

		// dipesh
		// printQuality(ranking.getSubfront(0));

		return ranking.getSubfront(0);
	} // execute

	// Dipesh
	public void printObj(SolutionSet population) {
		double obj1 = 0, obj2 = 0, obj3 = 0, obj4 = 0;

		for (int i = 0; i < population.size(); i++) {
			obj1 += population.get(i).getObjective(0);
			obj2 += population.get(i).getObjective(1);
			obj3 += population.get(i).getObjective(2);
			obj4 += population.get(i).getObjective(3);
		}
		System.out.println(obj1 / population.size() + "\t" + obj2 / population.size() + "\t" + obj3 / population.size()
				+ "\t" + obj4 / population.size());
	}

	public void printQuality(SolutionSet solutionSet) {
		for (int i = 0; i < solutionSet.size(); i++) {
			Solution solution = solutionSet.get(i);
			List<TestCase> caseList = solution.getCaseList();
			for (TestCase testCase : caseList) {
				// time += testCase.getExecutionTime();
			}
			double timeTotal = 0, apiTotal = 0, configTotal = 0;
			Set<String> configSearched = new HashSet<String>();
			Set<String> apiSearched = new HashSet<String>();

			for (TestCase testCase : caseList) {
				Set<String> configTC = new HashSet<String>();
				configTC = testCase.getConfigUsed();
				configTC.removeAll(configSearched); // to get the unique
													// configurations covered by
													// this test case
				configTotal += configTC.size();
				configSearched.addAll(configTC); // add the unique
													// configurations to the
													// total configurations
													// covered already

				@SuppressWarnings("unchecked")
				Set<String> apiTC = (Set<String>) new HashSet<>(testCase.getApiUsed());
				apiTC.removeAll(apiSearched);
				apiTotal += apiTC.size();
				apiSearched.addAll(apiTC);

				timeTotal += testCase.getExecutionTime();
			}
			System.out.println("configSearched: " + configSearched.size() + "  apiSearched: " + apiSearched.size()
					+ "  time:  " + timeTotal);
		}
	}

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

} // NSGA-II
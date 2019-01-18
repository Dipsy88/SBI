//  SolutionSet.Java
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

package jmetal.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import elements.Test;
import elements.TestCase;
import jmetal.util.Configuration;

/**
 * Class representing a SolutionSet (a set of solutions)
 */
public class SolutionSet implements Serializable {

	/**
	 * Stores a list of <code>solution</code> objects.
	 */
	protected final List<Solution> solutionsList_;

	public List<Solution> getSolutionList_() {
		return solutionsList_;
	}

	/**
	 * Maximum size of the solution set
	 */
	private int capacity_ = 0;

	/**
	 * Constructor. Creates an unbounded solution set.
	 */
	public SolutionSet() {
		solutionsList_ = new ArrayList<Solution>();
	} // SolutionSet

	/**
	 * Creates a empty solutionSet with a maximum capacity.
	 * 
	 * @param maximumSize
	 *            Maximum size.
	 */
	public SolutionSet(int maximumSize) {
		solutionsList_ = new ArrayList<Solution>();
		capacity_ = maximumSize;
	} // SolutionSet

	/**
	 * Inserts a new solution into the SolutionSet.
	 * 
	 * @param solution
	 *            The <code>Solution</code> to store
	 * @return True If the <code>Solution</code> has been inserted, false otherwise.
	 */
	public boolean add(Solution solution) {
		if (solutionsList_.size() == capacity_) {
			Configuration.logger_.severe("The population is full");
			Configuration.logger_.severe("Capacity is : " + capacity_);
			Configuration.logger_.severe("\t Size is: " + this.size());
			return false;
		} // if

		solutionsList_.add(solution);
		return true;
	} // add

	public boolean add(int index, Solution solution) {
		solutionsList_.add(index, solution);
		return true;
	}

	/*
	 * public void add(Solution solution) { if (solutionsList_.size() == capacity_)
	 * try { throw new JMException("SolutionSet.Add(): the population is full") ; }
	 * catch (JMException e) { e.printStackTrace(); } else
	 * solutionsList_.add(solution); }
	 */
	/**
	 * Returns the ith solution in the set.
	 * 
	 * @param i
	 *            Position of the solution to obtain.
	 * @return The <code>Solution</code> at the position i.
	 * @throws IndexOutOfBoundsException
	 *             Exception
	 */
	public Solution get(int i) {
		if (i >= solutionsList_.size()) {
			throw new IndexOutOfBoundsException("Index out of Bound " + i);
		}
		return solutionsList_.get(i);
	} // get

	/**
	 * Returns the maximum capacity of the solution set
	 * 
	 * @return The maximum capacity of the solution set
	 */
	public int getMaxSize() {
		return capacity_;
	} // getMaxSize

	/**
	 * Sorts a SolutionSet using a <code>Comparator</code>.
	 * 
	 * @param comparator
	 *            <code>Comparator</code> used to sort.
	 */
	public void sort(Comparator comparator) {
		if (comparator == null) {
			Configuration.logger_.severe("No criterium for comparing exist");
			return;
		} // if
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		Collections.sort(solutionsList_, comparator);
	} // sort

	/**
	 * Returns the index of the best Solution using a <code>Comparator</code>. If
	 * there are more than one occurrences, only the index of the first one is
	 * returned
	 * 
	 * @param comparator
	 *            <code>Comparator</code> used to compare solutions.
	 * @return The index of the best Solution attending to the comparator or
	 *         <code>-1<code> if the SolutionSet is empty
	 */
	int indexBest(Comparator comparator) {
		if ((solutionsList_ == null) || (this.solutionsList_.isEmpty())) {
			return -1;
		}

		int index = 0;
		Solution bestKnown = solutionsList_.get(0), candidateSolution;
		int flag;
		for (int i = 1; i < solutionsList_.size(); i++) {
			candidateSolution = solutionsList_.get(i);
			flag = comparator.compare(bestKnown, candidateSolution);
			if (flag == +1) {
				index = i;
				bestKnown = candidateSolution;
			}
		}

		return index;
	} // indexBest

	/**
	 * Returns the best Solution using a <code>Comparator</code>. If there are more
	 * than one occurrences, only the first one is returned
	 * 
	 * @param comparator
	 *            <code>Comparator</code> used to compare solutions.
	 * @return The best Solution attending to the comparator or <code>null<code> if
	 *         the SolutionSet is empty
	 */
	public Solution best(Comparator comparator) {
		int indexBest = indexBest(comparator);
		if (indexBest < 0) {
			return null;
		} else {
			return solutionsList_.get(indexBest);
		}

	} // best

	/**
	 * Returns the index of the worst Solution using a <code>Comparator</code>. If
	 * there are more than one occurrences, only the index of the first one is
	 * returned
	 * 
	 * @param comparator
	 *            <code>Comparator</code> used to compare solutions.
	 * @return The index of the worst Solution attending to the comparator or
	 *         <code>-1<code> if the SolutionSet is empty
	 */
	public int indexWorst(Comparator comparator) {
		if ((solutionsList_ == null) || (this.solutionsList_.isEmpty())) {
			return -1;
		}

		int index = 0;
		Solution worstKnown = solutionsList_.get(0), candidateSolution;
		int flag;
		for (int i = 1; i < solutionsList_.size(); i++) {
			candidateSolution = solutionsList_.get(i);
			flag = comparator.compare(worstKnown, candidateSolution);
			if (flag == -1) {
				index = i;
				worstKnown = candidateSolution;
			}
		}

		return index;

	} // indexWorst

	/**
	 * Returns the worst Solution using a <code>Comparator</code>. If there are more
	 * than one occurrences, only the first one is returned
	 * 
	 * @param comparator
	 *            <code>Comparator</code> used to compare solutions.
	 * @return The worst Solution attending to the comparator or <code>null<code> if
	 *         the SolutionSet is empty
	 */
	public Solution worst(Comparator comparator) {

		int index = indexWorst(comparator);
		if (index < 0) {
			return null;
		} else {
			return solutionsList_.get(index);
		}

	} // worst

	/**
	 * Returns the number of solutions in the SolutionSet.
	 * 
	 * @return The size of the SolutionSet.
	 */
	public int size() {
		return solutionsList_.size();
	} // size

	/**
	 * Writes the objective function values of the <code>Solution</code> objects
	 * into the set in a file.
	 * 
	 * @param path
	 *            The output file name
	 */
	public void printObjectivesToFile(String path) {
		try {
			/* Open the file */
			FileOutputStream fos = new FileOutputStream(path, true);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);

			for (Solution aSolutionsList_ : solutionsList_) {
				// if (this.vector[i].getFitness()<1.0) {
				System.out.println(aSolutionsList_.getObjective(0));
				System.out.println("\n");

				int a;
				if (aSolutionsList_.getObjective(0) < 0.59)
					a = 3;

				bw.write(aSolutionsList_.toString());
				bw.newLine();
				findCoverage(aSolutionsList_.getSelectedCaseList());
				writeFile();
				// }
			}

			/* Close the file */
			bw.close();
		} catch (IOException e) {
			Configuration.logger_.severe("Error acceding to the file");
			e.printStackTrace();
		}
	} // printObjectivesToFile

	public void printObjectivesToFileTab(String path1, String path2) {
		// DirectoryUtils.createDirectory(path1);
		String funFileName = String.format("%s%sFUN_%s", path1, File.separator, path2);

		try {
			/* Open the file */
			FileOutputStream fos = new FileOutputStream(funFileName, false);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);

			for (Solution aSolutionsList_ : solutionsList_) {
				// if (this.vector[i].getFitness()<1.0) {
				String sol = aSolutionsList_.toString();
				sol = sol.replaceAll(" ", "\t");
				bw.write(sol);
				bw.newLine();
				// }
			}

			/* Close the file */
			bw.close();
		} catch (IOException e) {
			Configuration.logger_.severe("Error acceding to the file");
			e.printStackTrace();
		}
	} // printObjectivesToFile

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

				modifiedCase.add((testCase.getName()));
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

	/*
	 * // configuration covered public void findConfigCovered(TestCase testCase) {
	 * if (testCase.configUsed.size() > 0) { Map<String, HashSet<String>>
	 * configCovered = testCase .getConfigCovered(); if (configCovered != null) {
	 * for (Map.Entry<String, HashSet<String>> entry : configCovered .entrySet()) {
	 * HashSet<String> tempSet = new HashSet<String>(); if (this.coveredConfigMap !=
	 * null) { if (this.coveredConfigMap.containsKey(entry.getKey())) {
	 * tempSet.addAll(this.coveredConfigMap.get(entry .getKey())); } }
	 * tempSet.addAll(entry.getValue()); this.coveredConfigMap.put(entry.getKey(),
	 * tempSet); } } } }
	 */
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

	public int countCoveredAPI() {
		int retSize = 0;
		for (String key : this.coveredAPI.keySet()) {
			int a = coveredAPI.get(key).size();
			if (a == 0)
				a = 1;
			retSize += a;

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
		System.out.println("modification line: " + this.modificationLine);
		System.out.println("selected: " + this.selectedTestCaseNum);
		System.out.println("executionTime: " + this.takenExecutionTime);
		System.out.println("modification cases: " + this.modifiedTestCaseNum);

		for (String temp : this.modifiedCase)
			System.out.print(temp + "\t");
		System.out.println();
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
	 */

	private Map<String, List<Map<String, String>>> coveredAPI = new HashMap<String, List<Map<String, String>>>();
	private Map<String, HashSet<String>> coveredConfigMap = new HashMap<String, HashSet<String>>();
	private double takenExecutionTime = 0;
	private double selectedTestCaseNum = 0;
	private double modifiedTestCaseNum = 0;
	private List<String> modifiedCase = new ArrayList<String>();
	private int modificationLine = 0;

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
		this.modifiedCase = new ArrayList<String>();
	}

	/**
	 * Writes the decision encodings.variable values of the <code>Solution</code>
	 * solutions objects into the set in a file.
	 * 
	 * @param path
	 *            The output file name
	 */
	public void printVariablesToFile(String path) {
		try {
			FileOutputStream fos = new FileOutputStream(path, true);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);

			if (size() > 0) {
				int numberOfVariables = solutionsList_.get(0).getDecisionVariables().length;
				for (Solution aSolutionsList_ : solutionsList_) {
					for (int j = 0; j < numberOfVariables; j++)
						bw.write(aSolutionsList_.getDecisionVariables()[j].toString() + " ");
					bw.newLine();
				}
			}
			bw.close();
		} catch (IOException e) {
			Configuration.logger_.severe("Error acceding to the file");
			e.printStackTrace();
		}
	} // printVariablesToFile

	/**
	 * Dipesh Write Test Case Writes the decision encodings.variable values of the
	 * <code>Solution</code> solutions objects into the set in a file.
	 * 
	 * @param path
	 *            The output file name
	 */
	public void printCaseToFile(String path) {
		try {
			FileOutputStream fos = new FileOutputStream(path, true);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);

			if (size() > 0) {

				for (Solution aSolutionsList_ : solutionsList_) {
					int numberOfVariables = aSolutionsList_.getCases_().length;

					for (int j = 0; j < numberOfVariables; j++)
						bw.write(aSolutionsList_.getCases_()[j].toString() + " ");
					bw.newLine();
				}
			}
			bw.close();
		} catch (IOException e) {
			Configuration.logger_.severe("Error acceding to the file");
			e.printStackTrace();
		}
	} // printVariablesToFile

	// Dipesh
	public void printForMLToFile(String path) {
		try {
			FileOutputStream fos = new FileOutputStream(path, true);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);

			if (size() > 0) {
				int numberOfVariablesFF = solutionsList_.get(0).getDecisionVariables().length;
				;
				// first write test case
				for (Solution aSolutionsList_ : solutionsList_) {
					int numberOfVariables = aSolutionsList_.getCases_().length;

					for (int j = 0; j < numberOfVariables; j++)
						bw.write(aSolutionsList_.getCases_()[j].toString() + ",");
					// System.out.print(aSolutionsList_.getCases_()[j].toString()
					// + ",");

					// System.out.print("\n");
					// values for objective
					// /*
					for (int k = 0; k < 4; k++) {
						double value = aSolutionsList_.getObjective(k);
						bw.write(convertVal(value));
						// bw.write(String.valueOf(aSolutionsList_.getObjective(k)));
						if ((k + 1) < 4)
							bw.write(",");
					}
					// */
					/*
					 * //values for single objective double sum=0; for (int k = 0; k < 4; k++){ sum
					 * += aSolutionsList_.getObjective(k)*0.25;
					 * 
					 * if ((k+1)==3) bw.write(String.valueOf(sum)); }
					 */
					bw.newLine();
				}
			}
			bw.close();
		} catch (IOException e) {
			Configuration.logger_.severe("Error acceding to the file");
			e.printStackTrace();
		}
	}

	/**
	 * Write the function values of feasible solutions into a file
	 * 
	 * @param path
	 *            File name
	 */
	public void printFeasibleFUN(String path) {
		try {
			FileOutputStream fos = new FileOutputStream(path);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);

			for (Solution aSolutionsList_ : solutionsList_) {
				if (aSolutionsList_.getOverallConstraintViolation() == 0.0) {
					bw.write(aSolutionsList_.toString());
					bw.newLine();
				}
			}
			bw.close();
		} catch (IOException e) {
			Configuration.logger_.severe("Error acceding to the file");
			e.printStackTrace();
		}
	}

	/**
	 * Write the encodings.variable values of feasible solutions into a file
	 * 
	 * @param path
	 *            File name
	 */
	public void printFeasibleVAR(String path) {
		try {
			FileOutputStream fos = new FileOutputStream(path);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);

			if (size() > 0) {
				int numberOfVariables = solutionsList_.get(0).getDecisionVariables().length;
				for (Solution aSolutionsList_ : solutionsList_) {
					if (aSolutionsList_.getOverallConstraintViolation() == 0.0) {
						for (int j = 0; j < numberOfVariables; j++)
							bw.write(aSolutionsList_.getDecisionVariables()[j].toString() + " ");
						bw.newLine();
					}
				}
			}
			bw.close();
		} catch (IOException e) {
			Configuration.logger_.severe("Error acceding to the file");
			e.printStackTrace();
		}
	}

	/**
	 * Empties the SolutionSet
	 */
	public void clear() {
		solutionsList_.clear();
	} // clear

	/**
	 * Deletes the <code>Solution</code> at position i in the set.
	 * 
	 * @param i
	 *            The position of the solution to remove.
	 */
	public void remove(int i) {
		if (i > solutionsList_.size() - 1) {
			Configuration.logger_.severe("Size is: " + this.size());
		} // if
		solutionsList_.remove(i);
	} // remove

	/**
	 * Returns an <code>Iterator</code> to access to the solution set list.
	 * 
	 * @return the <code>Iterator</code>.
	 */
	public Iterator<Solution> iterator() {
		return solutionsList_.iterator();
	} // iterator

	/**
	 * Returns a new <code>SolutionSet</code> which is the result of the union
	 * between the current solution set and the one passed as a parameter.
	 * 
	 * @param solutionSet
	 *            SolutionSet to join with the current solutionSet.
	 * @return The result of the union operation.
	 */
	public SolutionSet union(SolutionSet solutionSet) {
		// Check the correct size. In development
		int newSize = this.size() + solutionSet.size();
		if (newSize < capacity_)
			newSize = capacity_;

		// Create a new population
		SolutionSet union = new SolutionSet(newSize);
		for (int i = 0; i < this.size(); i++) {
			union.add(this.get(i));
		} // for

		for (int i = this.size(); i < (this.size() + solutionSet.size()); i++) {
			union.add(solutionSet.get(i - this.size()));
		} // for

		return union;
	} // union

	/**
	 * Replaces a solution by a new one
	 * 
	 * @param position
	 *            The position of the solution to replace
	 * @param solution
	 *            The new solution
	 */
	public void replace(int position, Solution solution) {
		if (position > this.solutionsList_.size()) {
			solutionsList_.add(solution);
		} // if
		solutionsList_.remove(position);
		solutionsList_.add(position, solution);
	} // replace

	/**
	 * Copies the objectives of the solution set to a matrix
	 * 
	 * @return A matrix containing the objectives
	 */
	public double[][] writeObjectivesToMatrix() {
		if (this.size() == 0) {
			return null;
		}
		double[][] objectives;
		objectives = new double[size()][get(0).getNumberOfObjectives()];
		for (int i = 0; i < size(); i++) {
			for (int j = 0; j < get(0).getNumberOfObjectives(); j++) {
				objectives[i][j] = get(i).getObjective(j);
			}
		}
		return objectives;
	} // writeObjectivesMatrix

	public void printObjectives() {
		for (int i = 0; i < solutionsList_.size(); i++)
			System.out.println("" + solutionsList_.get(i));
	}

	public void setCapacity(int capacity) {
		capacity_ = capacity;
	}

	public int getCapacity() {
		return capacity_;
	}

	public String convertVal(double value) {
		if (value < 0.1)
			return "value1";
		else if (value < 0.2)
			return "value2";
		else if (value < 0.3)
			return "value3";
		else if (value < 0.4)
			return "value4";
		else if (value < 0.5)
			return "value5";
		else if (value < 0.6)
			return "value6";
		else if (value < 0.7)
			return "value7";
		else if (value < 0.8)
			return "value8";
		else if (value < 0.9)
			return "value9";
		else
			return "value10";
	}

} // SolutionSet
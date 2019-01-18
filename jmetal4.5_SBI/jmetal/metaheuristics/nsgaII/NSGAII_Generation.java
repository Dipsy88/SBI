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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;














import com.rits.cloning.Cloner;

import elements.Test;
import elements.TestCase;
//import machineLearning.ML_ClassifyInstanceTest_allNew_2_3Obj;
import jmetal.core.*;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.Ranking;
import jmetal.util.comparators.CrowdingComparator;

/**
 * Implementation of NSGA-II. This implementation of NSGA-II makes use of a
 * QualityIndicator object to obtained the convergence speed of the algorithm.
 * This version is used in the paper: A.J. Nebro, J.J. Durillo, C.A. Coello
 * Coello, F. Luna, E. Alba
 * "A Study of Convergence Speed in Multi-Objective Metaheuristics." To be
 * presented in: PPSN'08. Dortmund. September 2008.
 */

public class NSGAII_Generation extends Algorithm {
	/**
	 * Constructor
	 * 
	 * @param problem
	 *            Problem to solve
	 */
	public NSGAII_Generation(Problem problem) {
		super(problem);
	} // NSGAII
	File fileName;
	/**
	 * Runs the NSGA-II algorithm.
	 * 
	 * @return a <code>SolutionSet</code> that is a set of non dominated
	 *         solutions as a result of the algorithm execution
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
		populationSize = ((Integer) getInputParameter("populationSize"))
				.intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations"))
				.intValue();
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
		
	

		//set initial case list
		for (int i=0;i<populationSize;i++){
			Cloner cloner = new Cloner();
			ArrayList<TestCase> testCaseList  = cloner.deepClone(problem_.getCaseList());
			problem_.caseListArray.add(testCaseList);		
		}

		for (int i = 0; i < 100; i++) {		
			newSolution = new Solution(problem_);	

			problem_.setCaseList(problem_.caseListArray.get(i));
			problem_.modifyTestCases(newSolution);
			problem_.evaluate(newSolution);
			problem_.evaluateConstraints(newSolution);

			evaluations++;
//			newSolution.setSelectedCaseList(problem_.getCaseSelected());
			population.add(newSolution);
			System.out.println(evaluations);
		} // for
		
		// Generations
		while (evaluations < maxEvaluations) {
			// Create the offSpring solutionSet
			offspringPopulation = new SolutionSet(populationSize);
			Solution[] parents = new Solution[2];
			for (int i = 0; i < (populationSize / 2); i++) {
				if (evaluations < maxEvaluations) {
					// obtain parents
					parents[0] = (Solution) selectionOperator
							.execute(population);
					parents[1] = (Solution) selectionOperator
							.execute(population);
					Solution[] offSpring = (Solution[]) crossoverOperator
							.execute(parents);
					mutationOperator.execute(offSpring[0]);
					mutationOperator.execute(offSpring[1]);
					int a;
					if (offSpring[0].getCaseList().size()==0 || offSpring[1].getCaseList().size()==0)
						a=3;
					problem_.testCaseMutation(offSpring[0]);					
					problem_.evaluate(offSpring[0]);
					problem_.evaluateConstraints(offSpring[0]);
//					offSpring[0].setSelectedCaseList(problem_.getCaseSelected());
					// offSpring[0].setCases_(problem_.getCases());
					
					problem_.testCaseMutation(offSpring[1]);	
					problem_.evaluate(offSpring[1]);
					problem_.evaluateConstraints(offSpring[1]);
//					offSpring[1].setSelectedCaseList(problem_.getCaseSelected());
					// offSpring[1].setCases_(problem_.getCases());
					offspringPopulation.add(offSpring[0]);
					offspringPopulation.add(offSpring[1]);
					evaluations += 2;
				} // if
			} // for
			System.out.println("evaluations: " + evaluations);
			// Create the solutionSet union of solutionSet and offSpring
			union = ((SolutionSet) population).union(offspringPopulation);

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
				distance.crowdingDistanceAssignment(front,
						problem_.getNumberOfObjectives());
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
				distance.crowdingDistanceAssignment(front,
						problem_.getNumberOfObjectives());
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
			
			
//			System.out.println(evaluations);
		} // while

		// Return as output parameter the required evaluations
		setOutputParameter("evaluations", requiredEvaluations);

		// Return the first non-dominated front
		Ranking ranking = new Ranking(population);
		ranking.getSubfront(0).printFeasibleFUN("FUN_NSGAII");
		
		//dipesh
//		printQuality(ranking.getSubfront(0));

		return ranking.getSubfront(0);
	} // execute


	//Dipesh
	public void printObj(SolutionSet population){
		double obj1=0, obj2=0, obj3=0, obj4=0;
		
		for (int i=0;i<population.size();i++){
			obj1 += population.get(i).getObjective(0);
			obj2 += population.get(i).getObjective(1);
			obj3 += population.get(i).getObjective(2);
			obj4 += population.get(i).getObjective(3);		
		}
		System.out.println(obj1/population.size()+"\t"+obj2/population.size()+"\t"+obj3/population.size()+"\t"+obj4/population.size());		
	}
	
	
	public void printQuality(SolutionSet solutionSet){
		for (int i=0;i<solutionSet.size();i++){
			Solution solution = solutionSet.get(i);
			List<TestCase> caseList= solution.getCaseList();
			for (TestCase testCase: caseList){
//				time += testCase.getExecutionTime();		
			}		
			  double timeTotal=0, apiTotal=0, configTotal=0;
			  Set<String> configSearched = new HashSet<String>();
			  Set<String> apiSearched = new HashSet<String>();
			  
			for (TestCase testCase:caseList){	  
					  Set<String> configTC = new HashSet<String>();
					  configTC = testCase.getConfigUsed();
					  configTC.removeAll(configSearched);  	//to get the unique configurations covered by this test case
					  configTotal += configTC.size();
					  configSearched.addAll(configTC);		//add the unique configurations to the total configurations covered already
					  
					  
					  @SuppressWarnings("unchecked")
					Set<String> apiTC = (Set<String>) new HashSet<>(testCase.getApiUsed());
					  apiTC.removeAll(apiSearched);		  
					  apiTotal += apiTC.size();
					  apiSearched.addAll(apiTC);
			
					  timeTotal += testCase.getExecutionTime();	
			  }
			System.out.println("configSearched: "+ configSearched.size() + "  apiSearched: " + apiSearched.size() + "  time:  " + timeTotal);
			int b;
			if(apiSearched.size()>16)
				b=3;
		}
		int a;
		a= 3;
	}
	
} // NSGA-II

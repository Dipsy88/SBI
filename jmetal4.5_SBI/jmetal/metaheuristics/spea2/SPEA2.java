//  SPEA2.java
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

package jmetal.metaheuristics.spea2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.Ranking;
import jmetal.util.Spea2Fitness;

/**
 * This class representing the SPEA2 algorithm
 */
public class SPEA2 extends Algorithm {
	File fileName;
	/**
	 * Defines the number of tournaments for creating the mating pool
	 */
	public static final int TOURNAMENTS_ROUNDS = 1;

	/**
	 * Constructor. Create a new SPEA2 instance
	 * 
	 * @param problem
	 *            Problem to solve
	 */
	public SPEA2(Problem problem) {
		super(problem);
	} // Spea2

	/**
	 * Runs of the Spea2 algorithm.
	 * 
	 * @return a <code>SolutionSet</code> that is a set of non dominated solutions
	 *         as a result of the algorithm execution
	 * @throws Exception
	 */
	public SolutionSet execute() throws Exception {
		int populationSize, archiveSize, maxEvaluations, evaluations;
		Operator crossoverOperator, mutationOperator, selectionOperator;
		SolutionSet solutionSet, archive, offSpringSolutionSet;

		// Read the params
		populationSize = ((Integer) getInputParameter("populationSize")).intValue();
		archiveSize = ((Integer) getInputParameter("archiveSize")).intValue();
		maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();

		// Read the operators
		crossoverOperator = operators_.get("crossover");
		mutationOperator = operators_.get("mutation");
		selectionOperator = operators_.get("selection");

		// Initialize the variables
		solutionSet = new SolutionSet(populationSize);
		archive = new SolutionSet(archiveSize);
		evaluations = 0;

		// -> Create the initial solutionSet
		Solution newSolution;
		for (int i = 0; i < populationSize; i++) {
			newSolution = new Solution(problem_);
			problem_.evaluate(newSolution);
			problem_.evaluateConstraints(newSolution);
			evaluations++;
			solutionSet.add(newSolution);
		}
		int fileNum = 0, num = 0;

		try {
			createFile(fileNum);
			FileWriter fwTime = new FileWriter(fileName.getAbsoluteFile(), true);
			BufferedWriter fileTime = new BufferedWriter(fwTime);
			for (int i = 0; i < solutionSet.size(); i++) {

				fileTime.write(solutionSet.get(i).getObjective(0) + " " + solutionSet.get(i).getObjective(1) + " "
						+ solutionSet.get(i).getObjective(2) + " " + solutionSet.get(i).getObjective(3) + " "
						+ solutionSet.get(i).getObjective(4));
				fileTime.write("\n");
			}
			fileTime.close();
			fileNum++;
			num += 1000;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (evaluations < maxEvaluations) {
			SolutionSet union = ((SolutionSet) solutionSet).union(archive);
			Spea2Fitness spea = new Spea2Fitness(union);
			spea.fitnessAssign();
			archive = spea.environmentalSelection(archiveSize);
			// Create a new offspringPopulation
			offSpringSolutionSet = new SolutionSet(populationSize);
			Solution[] parents = new Solution[2];
			while (offSpringSolutionSet.size() < populationSize) {
				int j = 0;
				do {
					j++;
					parents[0] = (Solution) selectionOperator.execute(archive);
				} while (j < SPEA2.TOURNAMENTS_ROUNDS); // do-while
				int k = 0;
				do {
					k++;
					parents[1] = (Solution) selectionOperator.execute(archive);
				} while (k < SPEA2.TOURNAMENTS_ROUNDS); // do-while

				// make the crossover
				Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
				mutationOperator.execute(offSpring[0]);
				problem_.evaluate(offSpring[0]);
				problem_.evaluateConstraints(offSpring[0]);
				offSpringSolutionSet.add(offSpring[0]);
				evaluations++;

				// System.out.println(evaluations);
			} // while
				// End Create a offSpring solutionSet
			solutionSet = offSpringSolutionSet;

			if (evaluations >= num) {
				try {
					createFile(fileNum);
					FileWriter fwTime = new FileWriter(fileName.getAbsoluteFile(), true);
					BufferedWriter fileTime = new BufferedWriter(fwTime);
					for (int i = 0; i < solutionSet.size(); i++) {

						fileTime.write(solutionSet.get(i).getObjective(0) + " " + solutionSet.get(i).getObjective(1)
								+ " " + solutionSet.get(i).getObjective(2) + " " + solutionSet.get(i).getObjective(3)
								+ " " + solutionSet.get(i).getObjective(4));
						fileTime.write("\n");
					}
					fileTime.close();
					fileNum++;
					num += 1000;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} // while

		Ranking ranking = new Ranking(archive);

		try {
			createFile(fileNum);
			FileWriter fwTime = new FileWriter(fileName.getAbsoluteFile(), true);
			BufferedWriter fileTime = new BufferedWriter(fwTime);
			for (int i = 0; i < ranking.getSubfront(0).size(); i++) {

				fileTime.write(ranking.getSubfront(0).get(i).getObjective(0) + " "
						+ ranking.getSubfront(0).get(i).getObjective(1) + " "
						+ ranking.getSubfront(0).get(i).getObjective(2) + " "
						+ ranking.getSubfront(0).get(i).getObjective(3) + " "
						+ ranking.getSubfront(0).get(i).getObjective(4));
				fileTime.write("\n");
			}
			fileTime.close();
			num++;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

	public void createFile(int count) throws Exception {
		fileName = new File("output/cisco2/spea2/HV/spea2" + count);

		// if file does not exists, then create it
		if (!fileName.exists()) {
			fileName.createNewFile();
		}
	}

} // SPEA2

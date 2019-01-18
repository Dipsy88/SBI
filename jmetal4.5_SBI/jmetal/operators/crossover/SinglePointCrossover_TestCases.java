//  SinglePointCrossover.java
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

package jmetal.operators.crossover;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.encodings.variable.Real;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import elements.TestCase;

/**
 * This class allows to apply a Single Point crossover operator using two parent
 * solutions.
 */
public class SinglePointCrossover_TestCases extends Crossover {
	/**
	 * Valid solution types to apply this operator
	 */
	private static final List VALID_TYPES = Arrays.asList(
			BinarySolutionType.class, BinaryRealSolutionType.class,
			IntSolutionType.class);

	private Double crossoverProbability_ = null;

	/**
	 * Constructor Creates a new instance of the single point crossover operator
	 */
	public SinglePointCrossover_TestCases(HashMap<String, Object> parameters) {
		super(parameters);
		if (parameters.get("probability") != null)
			crossoverProbability_ = (Double) parameters.get("probability");
	} // SinglePointCrossover

	/**
	 * Constructor Creates a new instance of the single point crossover operator
	 */
	// public SinglePointCrossover(Properties properties) {
	// this();
	// } // SinglePointCrossover

	/**
	 * Perform the crossover operation.
	 * 
	 * @param probability
	 *            Crossover probability
	 * @param parent1
	 *            The first parent
	 * @param parent2
	 *            The second parent
	 * @return An array containig the two offsprings
	 * @throws JMException
	 */
	public Solution[] doCrossover(double probability, Solution parent1,
			Solution parent2) throws JMException {
		Solution[] offSpring = new Solution[2];
		offSpring[0] = new Solution(parent1);
		offSpring[1] = new Solution(parent2);
		try {
			if (PseudoRandom.randDouble() < probability) {
				if ((parent1.getType().getClass() == BinarySolutionType.class)
						|| (parent1.getType().getClass() == BinaryRealSolutionType.class)) {
					// 1. Compute the total number of bits
					int totalNumberOfBits = 0;
					for (int i = 0; i < parent1.getDecisionVariables().length; i++) {
						totalNumberOfBits += ((Binary) parent1
								.getDecisionVariables()[i]).getNumberOfBits();
					}

					// 2. Calculate the point to make the crossover
					int crossoverPoint = PseudoRandom.randInt(0,
							totalNumberOfBits - 1);

					// 3. Compute the encodings.variable containing the
					// crossoverPoint bit
					int variable = 0;
					int acountBits = ((Binary) parent1.getDecisionVariables()[variable])
							.getNumberOfBits();

					while (acountBits < (crossoverPoint + 1)) {
						variable++;
						acountBits += ((Binary) parent1.getDecisionVariables()[variable])
								.getNumberOfBits();
					}

					// 4. Compute the bit into the selected encodings.variable
					int diff = acountBits - crossoverPoint;
					int intoVariableCrossoverPoint = ((Binary) parent1
							.getDecisionVariables()[variable])
							.getNumberOfBits()
							- diff;

					// 5. Make the crossover into the gene;
					Binary offSpring1, offSpring2;
					offSpring1 = (Binary) parent1.getDecisionVariables()[variable]
							.deepCopy();
					offSpring2 = (Binary) parent2.getDecisionVariables()[variable]
							.deepCopy();

					for (int i = intoVariableCrossoverPoint; i < offSpring1
							.getNumberOfBits(); i++) {
						boolean swap = offSpring1.bits_.get(i);
						offSpring1.bits_.set(i, offSpring2.bits_.get(i));
						offSpring2.bits_.set(i, swap);
					}

					offSpring[0].getDecisionVariables()[variable] = offSpring1;
					offSpring[1].getDecisionVariables()[variable] = offSpring2;

					// 6. Apply the crossover to the other variables
					for (int i = 0; i < variable; i++) {
						offSpring[0].getDecisionVariables()[i] = parent2
								.getDecisionVariables()[i].deepCopy();

						offSpring[1].getDecisionVariables()[i] = parent1
								.getDecisionVariables()[i].deepCopy();

					}

					// 7. Decode the results
					for (int i = 0; i < offSpring[0].getDecisionVariables().length; i++) {
						((Binary) offSpring[0].getDecisionVariables()[i])
								.decode();
						((Binary) offSpring[1].getDecisionVariables()[i])
								.decode();
					}
				} // Binary or BinaryReal
				else { // Integer representation
					int crossoverPoint = PseudoRandom.randInt(0,
							parent1.numberOfVariables() - 1);
					
					List<TestCase> caseList1 = new ArrayList<TestCase>();
					List<TestCase> caseList2 = new ArrayList<TestCase>();
					String[] cases1 = new String [parent1.numberOfVariables()];
					String[] cases2 = new String [parent2.numberOfVariables()];
					Map<String,TestCase> caseMap1 = new HashMap<String,TestCase>();
					Map<String,TestCase> caseMap2 = new HashMap<String,TestCase>();
					
					for (int i = 0; i < crossoverPoint; i++) {
//						caseList1.add(parent1.caseMap.get(parent1.getCases_()[i]));
//						caseList2.add(parent2.caseMap.get(parent2.getCases_()[i]));
						cases1[i] = (parent1.getCases_()[i]);
						cases2[i] = (parent2.getCases_()[i]);
						caseMap1.put(cases1[i], parent1.caseMap.get(parent1.getCases_()[i]));
						caseMap2.put(cases2[i], parent2.caseMap.get(parent2.getCases_()[i]));
					}
					
					for (int i = crossoverPoint; i < parent1.numberOfVariables(); i++) {
						if(caseMap1.get(parent2.getCases_()[i])==null){
							caseList1.add(parent2.caseMap.get(parent2.getCases_()[i]));
							cases1[i] = (parent2.getCases_()[i]);
						}else{
							caseMap1.put(parent2.getCases_()[i], parent2.caseMap.get(parent2.getCases_()[i]));
							cases1[i] = (parent1.getCases_()[i]);
							caseMap1.put(cases1[i], parent1.caseMap.get(parent1.getCases_()[i]));
						}
						if(caseMap2.get(parent1.getCases_()[i])==null){
							caseList2.add(parent1.caseMap.get(parent1.getCases_()[i]));
							cases2[i] = (parent2.getCases_()[i]);
						}else{
							caseMap2.put(parent1.getCases_()[i], parent1.caseMap.get(parent1.getCases_()[i]));
							cases2[i] = (parent2.getCases_()[i]);
							caseMap2.put(cases2[i], parent2.caseMap.get(parent2.getCases_()[i]));
						}							
					}	
					for (String key1:caseMap1.keySet()){
						caseList1.add(caseMap1.get(key1));
					}
					for (String key2:caseMap2.keySet()){
						caseList2.add(caseMap2.get(key2));
					}
					offSpring[0].caseMap = caseMap1;
					offSpring[0].setCaseList(caseList1);
					offSpring[0].setCases_(cases1);
					offSpring[1].caseMap = caseMap2;
					offSpring[1].setCaseList(caseList2);
					offSpring[1].setCases_(cases2);
		
					int aa;
						aa=3;
				} // Int representation
			}
		} catch (ClassCastException e1) {
			Configuration.logger_
					.severe("SinglePointCrossover.doCrossover: Cannot perfom "
							+ "SinglePointCrossover");
			Class cls = java.lang.String.class;
			String name = cls.getName();
			throw new JMException("Exception in " + name + ".doCrossover()");
		}
		return offSpring;
	} // doCrossover

	/**
	 * Executes the operation
	 * 
	 * @param object
	 *            An object containing an array of two solutions
	 * @return An object containing an array with the offSprings
	 * @throws JMException
	 */
	public Object execute(Object object) throws JMException {
		Solution[] parents = (Solution[]) object;

		// if (!(VALID_TYPES.contains(parents[0].getType().getClass()) &&
		// VALID_TYPES.contains(parents[1].getType().getClass())) ) {
		//
		// Configuration.logger_.severe("SinglePointCrossover.execute: the solutions "
		// +
		// "are not of the right type. The type should be 'Binary' or 'Int', but "
		// +
		// parents[0].getType() + " and " +
		// parents[1].getType() + " are obtained");
		//
		// Class cls = java.lang.String.class;
		// String name = cls.getName();
		// throw new JMException("Exception in " + name + ".execute()");
		// } // if
		//
		// if (parents.length < 2) {
		// Configuration.logger_.severe("SinglePointCrossover.execute: operator "
		// +
		// "needs two parents");
		// Class cls = java.lang.String.class;
		// String name = cls.getName();
		// throw new JMException("Exception in " + name + ".execute()");
		// }
		Solution[] offSpring;
		offSpring = doCrossover(crossoverProbability_, parents[0], parents[1]);

		// -> Update the offSpring solutions
		for (int i = 0; i < offSpring.length; i++) {
			offSpring[i].setCrowdingDistance(0.0);
			offSpring[i].setRank(0);
		}
		return offSpring;
	} // execute

	@Override
	public Object executeProblem(Object object, Problem problem)
			throws JMException {
		// TODO Auto-generated method stub
		return null;
	}
} // SinglePointCrossover

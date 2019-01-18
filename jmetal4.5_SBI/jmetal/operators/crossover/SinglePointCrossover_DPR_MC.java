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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.comparators.DominanceComparator;

/**
 * This class allows to apply a Single Point crossover operator using two parent
 * solutions.
 */
public class SinglePointCrossover_DPR_MC extends Crossover {
	/**
	 * Valid solution types to apply this operator
	 */
	private static final List VALID_TYPES = Arrays.asList(BinarySolutionType.class, BinaryRealSolutionType.class,
			IntSolutionType.class);

	private Double crossoverProbability_ = null;

	/**
	 * Constructor Creates a new instance of the single point crossover operator
	 */
	public SinglePointCrossover_DPR_MC(HashMap<String, Object> parameters) {
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
	 * @throws Exception
	 */
	public Solution[] doCrossover(double probability, Solution parent1, Solution parent2, Problem _problem)
			throws Exception {
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
						totalNumberOfBits += ((Binary) parent1.getDecisionVariables()[i]).getNumberOfBits();
					}

					// 2. Calculate the point to make the crossover
					int crossoverPoint = PseudoRandom.randInt(0, totalNumberOfBits - 1);

					// 3. Compute the encodings.variable containing the
					// crossoverPoint bit
					int variable = 0;
					int acountBits = ((Binary) parent1.getDecisionVariables()[variable]).getNumberOfBits();

					while (acountBits < (crossoverPoint + 1)) {
						variable++;
						acountBits += ((Binary) parent1.getDecisionVariables()[variable]).getNumberOfBits();
					}

					// 4. Compute the bit into the selected encodings.variable
					int diff = acountBits - crossoverPoint;
					int intoVariableCrossoverPoint = ((Binary) parent1.getDecisionVariables()[variable])
							.getNumberOfBits() - diff;

					// 5. Make the crossover into the gene;
					Binary offSpring1, offSpring2;
					offSpring1 = (Binary) parent1.getDecisionVariables()[variable].deepCopy();
					offSpring2 = (Binary) parent2.getDecisionVariables()[variable].deepCopy();

					for (int i = intoVariableCrossoverPoint; i < offSpring1.getNumberOfBits(); i++) {
						boolean swap = offSpring1.bits_.get(i);
						offSpring1.bits_.set(i, offSpring2.bits_.get(i));
						offSpring2.bits_.set(i, swap);
					}

					offSpring[0].getDecisionVariables()[variable] = offSpring1;
					offSpring[1].getDecisionVariables()[variable] = offSpring2;

					// 6. Apply the crossover to the other variables
					for (int i = 0; i < variable; i++) {
						offSpring[0].getDecisionVariables()[i] = parent2.getDecisionVariables()[i].deepCopy();

						offSpring[1].getDecisionVariables()[i] = parent1.getDecisionVariables()[i].deepCopy();

					}

					// 7. Decode the results
					for (int i = 0; i < offSpring[0].getDecisionVariables().length; i++) {
						((Binary) offSpring[0].getDecisionVariables()[i]).decode();
						((Binary) offSpring[1].getDecisionVariables()[i]).decode();
					}
				} // Binary or BinaryReal
				else { // Integer representation
					Solution[] offSpringTemp = new Solution[9];
					offSpringTemp[0] = new Solution(parent1);
					offSpringTemp[1] = new Solution(parent2);
					offSpringTemp[2] = new Solution(parent1);
					offSpringTemp[3] = new Solution(parent2);
					offSpringTemp[4] = new Solution(parent1);
					offSpringTemp[5] = new Solution(parent2);
					offSpringTemp[6] = new Solution(parent1);
					offSpringTemp[7] = new Solution(parent2);
					offSpringTemp[8] = new Solution(parent1);

					double counter = 0.1;
					int child = 0;

					Variable X1 = parent1.getDecisionVariables()[0];
					Variable X2 = parent2.getDecisionVariables()[0];

					List<Double> X1List = new ArrayList<Double>();
					List<Double> X2List = new ArrayList<Double>();

					Map<Double, Double> X1Hash = new HashMap<Double, Double>();
					Map<Double, Double> X2Hash = new HashMap<Double, Double>();

					double valueX1 = 0;
					double valueX2 = 0;

					while (counter <= 0.5) {
						int crossoverPoint = (int) (parent1.numberOfVariables() * counter);

						if (counter < 0.5) {
							valueX1 = 0;
							valueX2 = 0;

							X1List.clear();
							X2List.clear();
							X1Hash.clear();
							X2Hash.clear();
							// To avoid repetition
							for (int i = 0; i < crossoverPoint; i++) {
								X1List.add(X1.getValue(i));
								X2List.add(X2.getValue(i));
							}

							for (int i = crossoverPoint; i < parent1.numberOfVariables(); i++) {
								valueX1 = X1.getValue(i);
								valueX2 = X2.getValue(i);

								if (!X1List.contains(valueX2)) {
									X1Hash.put(valueX2, offSpringTemp[child].getDecisionVariables()[0].getValue(i));
									offSpringTemp[child].getDecisionVariables()[0].setValue(i, valueX2);
									X1List.add(valueX2);
									// System.out.println(i + " : " + valueX2);
								} else if (X1Hash.containsKey(valueX1)) {
									double key1Temp = X1Hash.get(valueX1);
									while (X1Hash.containsKey(key1Temp)) {
										if (X1Hash.containsKey(key1Temp))
											key1Temp = X1Hash.get(key1Temp);
										else
											continue;
									}
									offSpringTemp[child].getDecisionVariables()[0].setValue(i, key1Temp);
									X1List.add(key1Temp);
								} else
									X1List.add(valueX1);

								if (!X2List.contains(valueX1)) {
									X2Hash.put(valueX1, offSpringTemp[child + 1].getDecisionVariables()[0].getValue(i));
									offSpringTemp[child + 1].getDecisionVariables()[0].setValue(i, valueX1);
									X2List.add(valueX1);
								} else if (X2Hash.containsKey(valueX2)) {
									double key2Temp = X2Hash.get(valueX2);
									while (X2Hash.containsKey(key2Temp)) {
										if (X2Hash.containsKey(key2Temp))
											key2Temp = X2Hash.get(key2Temp);
										else
											continue;
									}
									offSpringTemp[child + 1].getDecisionVariables()[0].setValue(i, key2Temp);
									X2List.add(key2Temp);
								} else
									X2List.add(valueX2);
							}
						} else {
							valueX1 = 0;
							valueX2 = 0;

							X1List.clear();
							X2List.clear();
							X1Hash.clear();
							X2Hash.clear();

							for (int i = 0; i < crossoverPoint; i++) {
								X1List.add(X1.getValue(i));
							}

							for (int i = crossoverPoint; i < parent1.numberOfVariables(); i++) {
								valueX1 = X1.getValue(i);
								valueX2 = X2.getValue(i);

								if (!X1List.contains(valueX2)) {
									X1Hash.put(valueX2, offSpringTemp[child].getDecisionVariables()[0].getValue(i));
									offSpringTemp[child].getDecisionVariables()[0].setValue(i, valueX2);
									X1List.add(valueX2);
									// System.out.println(i + " : " + valueX2);
								} else if (X1Hash.containsKey(valueX1)) {
									double key1Temp = X1Hash.get(valueX1);
									while (X1Hash.containsKey(key1Temp)) {
										if (X1Hash.containsKey(key1Temp))
											key1Temp = X1Hash.get(key1Temp);
										else
											continue;
									}
									offSpringTemp[child].getDecisionVariables()[0].setValue(i, key1Temp);
									X1List.add(key1Temp);
								} else
									X1List.add(valueX1);
							}
						}

						counter += 0.1;
						child = child + 2;
						// for

					}
					for (int i = 0; i < 9; i++)
						_problem.evaluate(offSpringTemp[i]);

					Comparator dominance_ = new DominanceComparator();

					int best1 = 0, best2 = 1;
					for (int i = 0; i < 7; i++) {
						if ((i % 2) == 0) {
							int result1 = dominance_.compare(offSpringTemp[best1], offSpringTemp[i + 2]);
							if (result1 == 1)
								best1 = i + 2;
						} else {
							int result2 = dominance_.compare(offSpringTemp[best2], offSpringTemp[i + 2]);
							if (result2 == 1)
								best2 = i + 2;
						}

					}
					offSpring[0] = offSpringTemp[best1];
					offSpring[1] = offSpringTemp[best2];

					int aa;
					aa = 3;
				} // Int representation
			}
		} catch (ClassCastException e1) {
			Configuration.logger_.severe("SinglePointCrossover.doCrossover: Cannot perfom " + "SinglePointCrossover");
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
	 * @throws Exception
	 */
	public Object executeProblem(Object object, Problem _problem) throws Exception {
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
		offSpring = doCrossover(crossoverProbability_, parents[0], parents[1], _problem);

		// -> Update the offSpring solutions
		for (int i = 0; i < offSpring.length; i++) {
			offSpring[i].setCrowdingDistance(0.0);
			offSpring[i].setRank(0);
		}
		return offSpring;
	} // execute

	@Override
	public Object execute(Object object) throws JMException {
		// TODO Auto-generated method stub
		return null;
	}

} // SinglePointCrossover

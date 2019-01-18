/*
 * SBXCrossoverAndPolynomialMutation.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 *
 * This class returns a solution after applying SBX and Polynomial mutation
 */
package jmetal.util.offspring;

import java.util.HashMap;

import jmetal.core.Operator;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;

public class SBXCrossoverAndPolynomialMutationOffspring extends Offspring {

	private double mutationProbability_ = 0.0;
	private double crossoverProbability_ = 0.9;
	private double distributionIndexForMutation_ = 20;
	private double distributionIndexForCrossover_ = 20;
	private Operator crossover_;
	private Operator mutation_;
	private Operator selection_;

	private SBXCrossoverAndPolynomialMutationOffspring(double mutationProbability, double crossoverProbability,
			double distributionIndexForMutation, double distributionIndexForCrossover) throws JMException {
		HashMap parameters;
		mutationProbability_ = mutationProbability;
		crossoverProbability_ = crossoverProbability;
		distributionIndexForMutation_ = distributionIndexForMutation;
		distributionIndexForCrossover_ = distributionIndexForCrossover;

		// Crossover operator
		parameters = new HashMap();
		parameters.put("probability", crossoverProbability_);
		parameters.put("distributionIndex", distributionIndexForCrossover_);

		crossover_ = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);

		parameters = new HashMap();
		parameters.put("probability", mutationProbability_);
		parameters.put("distributionIndex", distributionIndexForMutation_);

		mutation_ = MutationFactory.getMutationOperator("PolynomialMutation", parameters);

		selection_ = SelectionFactory.getSelectionOperator("BinaryTournament", null);

		id_ = "SBX_Polynomial";
	}

} // SBXCrossoverAndPolynomialMutation

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
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;

public class DifferentialEvolutionOffspringPolynomial extends Offspring {

	private double mutationProbability_ = 0.0;
	double crossoverProbability_ = 0.9;
	private double distributionIndexForMutation_ = 20;
	double distributionIndexForCrossover_ = 20;
	private Operator mutation_;
	private Operator selection_;

	private DifferentialEvolutionOffspringPolynomial(double mutationProbability, double distributionIndexForMutation)
			throws JMException {
		HashMap parameters;
		mutationProbability_ = mutationProbability;
		distributionIndexForMutation_ = distributionIndexForMutation;

		parameters = new HashMap();
		parameters.put("probability", mutationProbability_);
		parameters.put("distributionIndex", distributionIndexForMutation_);
		mutation_ = MutationFactory.getMutationOperator("PolynomialMutation", parameters);

		selection_ = SelectionFactory.getSelectionOperator("BinaryTournament", null);

		id_ = "Polynomial";
	}

} // DifferentialEvolutionOffspring

/*
 * SBXCrossover.java
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
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;

public class SBXCrossoverOffspring extends Offspring {

	private double crossoverProbability_ = 0.9;
	private double distributionIndexForCrossover_ = 20;
	private Operator crossover_;
	private Operator selection_;

	public SBXCrossoverOffspring(double crossoverProbability, double distributionIndexForCrossover) throws JMException {
		HashMap parameters;
		crossoverProbability_ = crossoverProbability;
		distributionIndexForCrossover_ = distributionIndexForCrossover;

		// Crossover operator
		parameters = new HashMap();
		parameters.put("probability", crossoverProbability_);
		parameters.put("distributionIndex", distributionIndexForCrossover_);

		crossover_ = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);

		selection_ = SelectionFactory.getSelectionOperator("BinaryTournament", null);

		id_ = "SBXCrossover";
	}

	/**
	 * 
	 */

	public String configuration() {
		String result = "-----\n";
		result += "Operator: " + id_ + "\n";
		result += "Probability: " + crossoverProbability_ + "\n";
		result += "DistributionIndex: " + distributionIndexForCrossover_;

		return result;
	}
} // DifferentialEvolutionOffspring

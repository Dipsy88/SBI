/*
 * BLXAlphaCrossoverOffspring.java
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

public class BLXAlphaCrossoverOffspring extends Offspring {

	private double crossoverProbability_ = 0.9;
	private double alpha_ = 0.5;
	private Operator crossover_;
	Operator mutation_;
	private Operator selection_;

	private BLXAlphaCrossoverOffspring(double crossoverProbability, double alpha) throws JMException {
		HashMap parameters;
		crossoverProbability_ = crossoverProbability;
		alpha_ = alpha;

		// Crossover operator
		parameters = new HashMap();
		parameters.put("probability", crossoverProbability_);
		parameters.put("alpha", alpha_);

		crossover_ = CrossoverFactory.getCrossoverOperator("BLXAlphaCrossover", parameters);

		selection_ = SelectionFactory.getSelectionOperator("BinaryTournament", null);

		id_ = "BLXAlphaCrossover";
	}

	public String configuration() {
		String result = "-----\n";
		result += "Operator: " + id_ + "\n";
		result += "Probability: " + crossoverProbability_ + "\n";
		result += "Alpha: " + alpha_;

		return result;
	}
} // BLXAlphaCrossoverOffspring

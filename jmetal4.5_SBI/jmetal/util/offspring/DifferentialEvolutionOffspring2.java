/**
 * DifferentialEvolutionOffspring.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 *
 * This class returns a solution after applying DE
 */

package jmetal.util.offspring;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.core.Operator;
import jmetal.operators.crossover.DifferentialEvolutionCrossover;
import jmetal.operators.selection.SelectionFactory;
import jmetal.util.JMException;

public class DifferentialEvolutionOffspring2 extends Offspring {
	private double CR_;
	private double F_;

	private Operator crossover_;
	private Operator selection_;
	Operator mutation_;

	public void DifferentialEvolutionOffspring() {

	}

	/**
	 * Constructor
	 * 
	 * @param CR
	 * @param F
	 */
	private DifferentialEvolutionOffspring2(double CR, double F) {
		HashMap parameters = null;
		CR_ = CR;
		F_ = F;
		try {
			// Crossover operator
			parameters = new HashMap();
			parameters.put("CR", CR_);
			parameters.put("F", F_);
			crossover_ = new DifferentialEvolutionCrossover(parameters);

			// Selecion operator
			parameters = null;
			selection_ = SelectionFactory.getSelectionOperator("DifferentialEvolutionSelection", parameters);
		} catch (JMException ex) {
			Logger.getLogger(DifferentialEvolutionOffspring2.class.getName()).log(Level.SEVERE, null, ex);
		}
		id_ = "DE2";
	}

} // DifferentialEvolutionOffspring

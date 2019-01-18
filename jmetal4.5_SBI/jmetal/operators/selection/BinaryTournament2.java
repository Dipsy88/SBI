//  BinaryTournament2.java
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

package jmetal.operators.selection;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.comparators.DominanceComparator;

/**
 * This class implements an operator for binary selections using the same code
 * in Deb's NSGA-II implementation
 */
public class BinaryTournament2 extends Selection {

	/**
	 * dominance_ store the <code>Comparator</code> for check dominance_
	 */
	private Comparator dominance_;

	/**
	 * a_ stores a permutation of the solutions in the solutionSet used
	 */
	private int a_[];

	/**
	 * index_ stores the actual index for selection
	 */
	private int index_ = 0;

	/**
	 * Constructor Creates a new instance of the Binary tournament operator (Deb's
	 * NSGA-II implementation version)
	 */
	public BinaryTournament2(HashMap<String, Object> parameters) {
		super(parameters);
		dominance_ = new DominanceComparator();
	} // BinaryTournament2

	/**
	 * Performs the operation
	 * 
	 * @param object
	 *            Object representing a SolutionSet
	 * @return the selected solution
	 */
	public Object execute(Object object) {
		SolutionSet population = (SolutionSet) object;
		if (index_ == 0) // Create the permutation
		{
			a_ = (new jmetal.util.PermutationUtility()).intPermutation(population.size());
		}

		Solution solution1, solution2;
		solution1 = population.get(a_[index_]);
		solution2 = population.get(a_[index_ + 1]);

		index_ = (index_ + 2) % population.size();

		int flag = dominance_.compare(solution1, solution2);
		if (flag == -1)
			return solution1;
		else if (flag == 1)
			return solution2;
		else if (solution1.getCrowdingDistance() > solution2.getCrowdingDistance())
			return solution1;
		else if (solution2.getCrowdingDistance() > solution1.getCrowdingDistance())
			return solution2;
		else if (PseudoRandom.randDouble() < 0.5)
			return solution1;
		else
			return solution2;
	} // execute

	public Object execute2(Object object) {
		SolutionSet population = (SolutionSet) object;
		if (index_ == 0) // Create the permutation
		{
			a_ = (new jmetal.util.PermutationUtility()).intPermutation(population.size());
		}
		// if(index_>population.size()){
		// index_=0;
		// a_= (new
		// jmetal.util.PermutationUtility()).intPermutation(population.size());
		// }
		Solution solution1;
		solution1 = population.get(a_[index_]);

		index_++;
		return solution1;

	} // execute

	// */

	public Object execute3(Object object) {
		SolutionSet population = (SolutionSet) object;
		if (index_ == 0 || index_ + 2 > a_.length) // Create the permutation
		{
			a_ = (new jmetal.util.PermutationUtility()).intPermutation(population.size());
			index_ = 0;
		}

		Solution solution1, solution2;
		solution1 = population.get(a_[index_]);
		int aa = index_ + 1;
		solution2 = population.get(a_[index_ + 1]);

		index_ = (index_ + 2) % population.size();

		int flag = dominance_.compare(solution1, solution2);
		if (flag == -1)
			return solution1;
		else if (flag == 1)
			return solution2;
		else if (solution1.getCrowdingDistance() > solution2.getCrowdingDistance())
			return solution1;
		else if (solution2.getCrowdingDistance() > solution1.getCrowdingDistance())
			return solution2;
		else if (PseudoRandom.randDouble() < 0.5)
			return solution1;
		else
			return solution2;
	} // execute

	// Dipesh
	public void setIndex(int index) {
		this.index_ = index;
	}

	@Override
	public Object executeProblem(Object object, Problem problem) throws JMException {
		// TODO Auto-generated method stub
		return null;
	}

	protected int createRandomPoint(int min, int max, int num) {
		Random r = new Random();
		// System.out.println(max);
		int x = r.nextInt(max - min) + min;
		while (x == num)
			x = r.nextInt(max - min) + min;

		return x;
	}

	public int compareNotStrict(Solution solution1, Solution solution2) {
		double percentage = 0;

		int dominate1; // dominate1 indicates if some objective of solution1 //
						// dominates the same objective in solution2. dominate2
		int dominate2; // is the complementary of dominate1.

		dominate1 = 0;
		dominate2 = 0;

		int flag; // stores the result of the comparison
		// Equal number of violated constraints. Applying a dominance Test then
		double value1, value2;
		for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
			value1 = solution1.getObjective(i);
			value2 = solution2.getObjective(i);
			if (value1 < value2) {
				flag = -1;
			} else if (value1 > value2) {
				flag = 1;
			} else {
				flag = 0;
			}
			if (flag == -1) {
				dominate1++;
			}
			if (flag == 1) {
				dominate2++;
			}

			percentage += (value1 - value2) / value2 * 100;
		}

		if (dominate1 == dominate2) {
			if (percentage > 0)
				return 1; // solution2 dominate
		}
		if (dominate1 > dominate2) {
			return -1; // solution1 dominate
		}
		return 1; // solution2 dominate
	} // compare
} // BinaryTournament2

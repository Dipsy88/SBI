//  GeneralizedSpread.java
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

package jmetal.qualityIndicator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Arrays;

/**
 * This class implements the generalized spread metric for two or more dimensions.
 * It can be used also as command line program just by typing. 
 * $ java jmetal.qualityIndicator.GeneralizedSpread <solutionFrontFile> <trueFrontFile> <getNumberOfObjectives>
 * Reference: A. Zhou, Y. Jin, Q. Zhang, B. Sendhoff, and E. Tsang
 *           Combining model-based and genetics-based offspring generation for 
 *           multi-objective optimization using a convergence criterion, 
 *           2006 IEEE Congress on Evolutionary Computation, 2006, pp. 3234-3241.
 */
public class GeneralizedSpreadBACKUP {

	private static File fileName;
	  private static BufferedWriter file;
	  static String [] algorithmNameList_ = new String[] {"smpso", "nsgaii", "spea2", "cellde", "ibea", "mocell", "ea", "avm"};
  public static jmetal.qualityIndicator.util.MetricsUtil utils_;  // MetricsUtil provides some 
                                                  // utilities for implementing
                                                  // the metric
  
  /**
   * Constructor
   * Creates a new instance of GeneralizedSpread
   */
  public GeneralizedSpreadBACKUP() {
    utils_ = new jmetal.qualityIndicator.util.MetricsUtil();
  } // GeneralizedSpread
  
  
  
  /**
   *  Calculates the generalized spread metric. Given the 
   *  pareto front, the true pareto front as <code>double []</code>
   *  and the number of objectives, the method return the value for the
   *  metric.
   *  @param paretoFront The pareto front.
   *  @param paretoTrueFront The true pareto front.
   *  @param numberOfObjectives The number of objectives.
   *  @return the value of the generalized spread metric
   **/
  public double generalizedSpread(double [][] paretoFront,
                                  double [][] paretoTrueFront,                                         
                                  int numberOfObjectives) {
    
    /**
     * Stores the maximum values of true pareto front.
     */
    double [] maximumValue={1,1,1,1,1};
    
    /**
     * Stores the minimum values of the true pareto front.
     */
    double [] minimumValue={0,0,0,0,0};
    
    /**
     * Stores the normalized front.
     */
    double [][] normalizedFront;
    
    /**
     * Stores the normalized true Pareto front.
     */ 
    double [][] normalizedParetoFront;
    

//    // STEP 1. Obtain the maximum and minimum values of the Pareto front
//    maximumValue = utils_.getMaximumValues(paretoTrueFront,numberOfObjectives);
//    minimumValue = utils_.getMinimumValues(paretoTrueFront,numberOfObjectives);
    
    normalizedFront = utils_.getNormalizedFront(paretoFront,
                                                maximumValue,
                                                minimumValue);
    
    // STEP 2. Get the normalized front and true Pareto fronts
    normalizedParetoFront = utils_.getNormalizedFront(paretoTrueFront,
                                                      maximumValue,
                                                      minimumValue);
    
    // STEP 3. Find extremal values
    double [][] extremValues = new double[numberOfObjectives][numberOfObjectives];
    for (int i = 0; i < numberOfObjectives; i++) {
      Arrays.sort(normalizedParetoFront,new jmetal.qualityIndicator.util.ValueComparator(i));
        System.arraycopy(normalizedParetoFront[normalizedParetoFront.length - 1], 0, extremValues[i], 0, numberOfObjectives);
    }
    
    int numberOfPoints     = normalizedFront.length;
    int numberOfTruePoints = normalizedParetoFront.length;
    
    
    // STEP 4. Sorts the normalized front
    Arrays.sort(normalizedFront,new jmetal.qualityIndicator.util.LexicoGraphicalComparator());
    
    // STEP 5. Calculate the metric value. The value is 1.0 by default
    if (utils_.distance(normalizedFront[0],normalizedFront[normalizedFront.length-1])==0.0) {
      return 1.0;
    } else {
      
      double dmean = 0.0;
      
      // STEP 6. Calculate the mean distance between each point and its nearest neighbor
      for (double[] aNormalizedFront : normalizedFront) {
        dmean += utils_.distanceToNearestPoint(aNormalizedFront, normalizedFront);
      }
      
      dmean = dmean / (numberOfPoints);
      
      // STEP 7. Calculate the distance to extremal values
      double dExtrems = 0.0;
      for (double[] extremValue : extremValues) {
        dExtrems += utils_.distanceToClosedPoint(extremValue, normalizedFront);
      }
      
      // STEP 8. Computing the value of the metric
      double mean = 0.0;
      for (double[] aNormalizedFront : normalizedFront) {
        mean += Math.abs(utils_.distanceToNearestPoint(aNormalizedFront, normalizedFront) -
                dmean);
      }
      
      double value = (dExtrems + mean) / (dExtrems + (numberOfPoints*dmean));
      return value;
      
    }
  } // generalizedSpread
     
  /**
   * This class can be invoked from the command line. Three params are required:
   * 1) the name of the file containing the front,  
   * 2) the name of the file containig the true Pareto front
   * 3) the number of objectives
 * @throws Exception 
   */
  public static void main(String args[]) throws Exception {
  
    int numberObjectives = 5;	
    for (int algorithmIndex=0; algorithmIndex<algorithmNameList_.length; algorithmIndex++) {
		createFile(algorithmNameList_[algorithmIndex]);
		FileWriter fw = new FileWriter(fileName.getAbsoluteFile());
		file = new BufferedWriter(fw);
		for (int row=1;row<=100;row++){
			 for (int column=1;column<76;column++){
		    //Create a new instance of the metric
				    GeneralizedSpreadBACKUP qualityIndicator = new GeneralizedSpreadBACKUP();
				    //Read the front from the files
				    double [][] solutionFront = readFile(row, column, algorithmNameList_[algorithmIndex]);
				    double [][] trueFront     = readTrueFile(column);
				    
				    //Obtain delta value
				    double value = qualityIndicator.generalizedSpread(solutionFront,
				                                             trueFront,
				                                             numberObjectives);
				    
				    System.out.println(value);  
				    file.write(value +"\t" );
				 //   file.write(1-value +"\t" );
				    file.flush();
			 } 
			 file.write("\n");
		}
		 file.close();
    } 
  }  // main
  
  public static double [][] readFile(int row, int column, String algorithm){		
		double [][] solutionFront = new double[100][5] ;
	    double [][] trueFront;
	    int counter;
	   
			try {
			    BufferedReader in = new BufferedReader(new FileReader("/Users/Dipesh/OneDrive/Papers/JournalShuai/Result/"+algorithm+"/"+algorithm + column + "_" + row));
			    String str;
			    int i=0;
			    while ((str = in.readLine()) != null){
	//		    	if (i==1000)
	//		    		break;
			    	String line = str;
			    	String[] details = line.split(" ");
			    	solutionFront[i][0] =Double.parseDouble(details[0]);
			    	solutionFront[i][1] =Double.parseDouble(details[1]);
			    	solutionFront[i][2] =Double.parseDouble(details[2]);
			    	solutionFront[i][3] =Double.parseDouble(details[3]);
			    	solutionFront[i][4] =Double.parseDouble(details[4]);
			    	i++;
			    	if (i==100)
			    		break;
			    }
			    in.close();
			} catch (IOException e) {
			}
	    
		return solutionFront;
	}
  
  public static void createFile(String algorithm) throws Exception{
		fileName = new File("/Users/Dipesh/OneDrive/Papers/JournalShuai/Analysis/GS/original/" + algorithm+ ".txt");

		// if file does not exists, then create it
		if (!fileName.exists()) {
			fileName.createNewFile();
		}
	}
	
	public static double [][] readTrueFile(int problem) throws IOException{	
		File file = new File("/Users/Dipesh/OneDrive/Papers/JournalShuai/Analysis/referenceFronts/" + problem);
		int size = countLines(file);
		double [][] trueFront = new double[size][5] ;
	    int counter;
	   
			try {
			    BufferedReader in = new BufferedReader(new FileReader("/Users/Dipesh/OneDrive/Papers/JournalShuai/Analysis/referenceFronts/" + problem));
			    String str;
			    int i=0;
			    while ((str = in.readLine()) != null){
	//		    	if (i==1000)
	//		    		break;
			    	String line = str;
			    	String[] details = line.split(" ");
			    	trueFront[i][0] =Double.parseDouble(details[0]);
			    	trueFront[i][1] =Double.parseDouble(details[1]);
			    	trueFront[i][2] =Double.parseDouble(details[2]);
			    	trueFront[i][3] =Double.parseDouble(details[3]);
			    	trueFront[i][4] =Double.parseDouble(details[4]);
			    	i++;
			    	if (i==100)
			    		break;
			    }
			    in.close();
			} catch (IOException e) {
			}
	    
		return trueFront;
	}
	
	public static int countLines(File aFile) throws IOException {
	    LineNumberReader reader = null;
	    try {
	        reader = new LineNumberReader(new FileReader(aFile));
	        while ((reader.readLine()) != null);
	        return reader.getLineNumber();
	    } catch (Exception ex) {
	        return -1;
	    } finally { 
	        if(reader != null) 
	            reader.close();
	    }
	}
} // GeneralizedSpread

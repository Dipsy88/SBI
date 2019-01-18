//  GenerationalDistance.java
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

/**
 * This class implements the generational distance indicator. It can be used also 
 * as a command line by typing: 
 * "java jmetal.qualityIndicator.GenerationalDistance <solutionFrontFile>  
 * <trueFrontFile> <getNumberOfObjectives>"
 * Reference: Van Veldhuizen, D.A., Lamont, G.B.: Multiobjective Evolutionary 
 *            Algorithm Research: A History and Analysis. 
 *            Technical Report TR-98-03, Dept. Elec. Comput. Eng., Air Force 
 *            Inst. Technol. (1998)
 */
public class GenerationalDistanceBACKUP_RAW {
  public jmetal.qualityIndicator.util.MetricsUtil utils_;  //utils_ is used to access to the
                                           //MetricsUtil funcionalities
  static int numberCases = 1000;
  static final double pow_ = 2.0;          //pow. This is the pow used for the
                                           //distances
  private static File fileName;
  private static BufferedWriter file;
//  static String [] algorithmNameList_ = new String[] {"ea", "nsgaii", "spea2", "cellde", "ibea", "mocell", "avm","rs"};
  static String [] algorithmNameList_ = new String[] {"ga"};
  /**
   * Constructor.
   * Creates a new instance of the generational distance metric. 
   */
  public GenerationalDistanceBACKUP_RAW() {
    utils_ = new jmetal.qualityIndicator.util.MetricsUtil();
  } // GenerationalDistance
  
  /**
   * Returns the generational distance value for a given front
   * @param front The front 
   * @param trueParetoFront The true pareto front
   */
  public double generationalDistance(double [][] front,
                                     double [][] trueParetoFront, 
                                     int numberOfObjectives) {
        
    /**
     * Stores the maximum values of true pareto front.
     */
    double [] maximumValue={1,1,1,1,1};
    
    /**
     * Stores the minimum values of the true pareto front.
     */
    double [] minimumValue={0,0,0,0,0} ;
    
    /**
     * Stores the normalized front.
     */
    double [][] normalizedFront ;
    
    /**
     * Stores the normalized true Pareto front.
     */ 
    double [][] normalizedParetoFront ; 
    
 
    
    // STEP 2. Get the normalized front and true Pareto fronts
    normalizedFront       = utils_.getNormalizedFront(front, 
    		                                          maximumValue, 
    		                                          minimumValue);
    normalizedParetoFront = utils_.getNormalizedFront(trueParetoFront, 
    		                                          maximumValue,
    		                                          minimumValue);
    
    // STEP 3. Sum the distances between each point of the front and the 
    // nearest point in the true Pareto front
    double sum = 0.0;
    for (int i = 0; i < front.length; i++) 
      sum += Math.pow(utils_.distanceToClosedPoint(normalizedFront[i],
    		                                       normalizedParetoFront),
    		                                       pow_);
   
    
    // STEP 4. Obtain the sqrt of the sum
    sum = Math.pow(sum,1.0/pow_);
    
    // STEP 5. Divide the sum by the maximum number of points of the front
    double generationalDistance = sum / normalizedFront.length;
    
    return generationalDistance;
  } // generationalDistance
  
  /**
   * This class can be invoqued from the command line. Two params are required:
   * 1) the name of the file containing the front, and 2) the name of the file 
   * containig the true Pareto front
 * @throws Exception 
   **/
  public static void main(String args[]) throws Exception {
//    if (args.length < 2) {
//      System.err.println("GenerationalDistance::Main: Usage: java " +
//      		             "GenerationalDistance <FrontFile> " +
//      		             "<TrueFrontFile>  <getNumberOfObjectives>");
//      System.exit(1);
//    } // if
	  int numberObjectives = 5;	
	
		for (int algorithmIndex=0; algorithmIndex<algorithmNameList_.length; algorithmIndex++) {
			createFile(algorithmNameList_[algorithmIndex]);
			FileWriter fw = new FileWriter(fileName.getAbsoluteFile());
			file = new BufferedWriter(fw);
			for (int row=1;row<=100;row++){
				 for (int column=1;column<76;column++){
	    // STEP 1. Create an instance of Generational Distance
					    GenerationalDistanceBACKUP_RAW qualityIndicator = new GenerationalDistanceBACKUP_RAW();
					    
					    // STEP 2. Read the fronts from the files
					    double [][] solutionFront = readFile(row, column, algorithmNameList_[algorithmIndex]);
					    double [][] trueFront     = readTrueFile(column);
					    
					    // STEP 3. Obtain the metric value
					    double value = qualityIndicator.generationalDistance(
					    		                                 solutionFront,
					    		                                 trueFront,
					           									numberObjectives);
				    
//					    System.out.println(value);
					    file.write(value +"\t" );
						 //   file.write(1-value +"\t" );
						    file.flush();
				 }
				 file.write("\n");
			}
			 file.close();
		}
  } // main  
  

	public static double [][] readFile(int row, int column, String algorithm){		
		double [][] solutionFront = new double[100][5] ;
	    double [][] trueFront;
	    int counter;
	   
			try {
			    BufferedReader in = new BufferedReader(new FileReader("/Users/Dipesh/OneDrive/Papers/ICST2015/Result/RAW/"+numberCases+"/"+algorithm+"/"+algorithm + column + "_" + row));
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
		fileName = new File("/Users/Dipesh/OneDrive/Papers/ICST2015/Analysis/RAW/"+numberCases+"/GD/" + algorithm+ ".txt");

		// if file does not exists, then create it
		if (!fileName.exists()) {
			fileName.createNewFile();
		}
	}
	
	public static double [][] readTrueFile(int problem) throws IOException{	
		File file = new File("/Users/Dipesh/OneDrive/Papers/ICST2015/Analysis/RAW/"+numberCases+"/nondominated/"+problem+ ".txt");
		int size = countLines(file);
		double [][] trueFront = new double[size][5] ;
	    int counter;
	   
			try {
			    BufferedReader in = new BufferedReader(new FileReader("/Users/Dipesh/OneDrive/Papers/ICST2015/Analysis/RAW/"+numberCases+"/nondominated/"+problem+ ".txt"));
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
	
	
  
} // GenerationalDistance

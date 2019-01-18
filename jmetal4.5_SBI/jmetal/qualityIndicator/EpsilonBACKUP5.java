//  Epsilon.java
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

package jmetal.qualityIndicator ;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
//Fix by DPR
/**
 * This class implements the unary epsilon additive indicator as proposed in
 * E. Zitzler, E. Thiele, L. Laummanns, M., Fonseca, C., and Grunert da Fonseca.
 * V (2003): Performance Assesment of Multiobjective Optimizers: An Analysis and
 * Review. The code is the a Java version of the original metric implementation
 * by Eckart Zitzler.
 * It can be used also as a command line program just by typing
 * $java jmetal.qualityIndicator.Epsilon <solutionFrontFile> <trueFrontFile> <getNumberOfObjectives>
 */

public class EpsilonBACKUP5 {

  /* stores the number of objectives */
  int     dim_   ;     
  /* obj_[i]=0 means objective i is to be minimized. This code always assume the minimization of all the objectives
   */
  int  [] obj_    ;     /* obj_[i] = 0 means objective i is to be minimized */
  /* method_ = 0 means apply additive epsilon and method_ = 1 means multiplicative
   * epsilon. This code always apply additive epsilon
   */
  int     method_; 
  /* stores a reference to  qualityIndicatorUtils */
  public jmetal.qualityIndicator.util.MetricsUtil utils_ = 
  		new jmetal.qualityIndicator.util.MetricsUtil();

  static int numberCases = 1000;
	private static File fileName;
	  private static BufferedWriter file;
//	  static String [] algorithmNameList_ = new String[] {"ea", "nsgaii", "spea2", "cellde", "ibea", "mocell", "avm","rs"};
	  static String []  algorithmNameList_ = new String[] {"ga"};
  /**
   * Returns the epsilon indicator.
   * @param a True Pareto front
   * @param b Solution front
   * @return the value of the epsilon indicator
   */
  public double epsilon(double [][] a, double [][] b, int dim) {
    int  i, j, k;
    double  eps, eps_j = 0.0, eps_k=0.0, eps_temp;

    dim_ = dim ;
    set_params() ;
    
    if (method_ == 0)
      eps = Double.MIN_VALUE;
    else
      eps= 0;

    for (i = 0; i < a.length; i++) {
      for (j = 0; j < b.length; j++) {
        for (k = 0; k < dim_; k++) {
          switch (method_) {
            case 0:
              if (obj_[k] == 0)
                eps_temp = b[j][k] - a[i][k];                
                //eps_temp = b[j * dim_ + k] - a[i * dim_ + k];
              else
                eps_temp = a[i][k] - b[j][k];
                //eps_temp = a[i * dim_ + k] - b[j * dim_ + k];
              break;
            default:
              if ( (a[i][k] < 0 && b[j][k] > 0) ||
                   (a[i][k] > 0 && b[j][k] < 0) ||
                   (a[i][k] == 0 || b[j][k] == 0)) {
              //if ( (a[i * dim_ + k] < 0 && b[j * dim_ + k] > 0) ||
              //     (a[i * dim_ + k] > 0 && b[j * dim_ + k] < 0) ||
              //     (a[i * dim_ + k] == 0 || b[j * dim_ + k] == 0)) {
                System.err.println("error in data file");
                System.exit(0);
              }
              if (obj_[k] == 0)
                eps_temp = b[j][k] / a[i][k];
                //eps_temp = b[j * dim_ + k] / a[i * dim_ + k];
              else
                eps_temp = a[i][k] / b[j][k];
                //eps_temp = a[i * dim_ + k] / b[j * dim_ + k];
            break;
          }
          int ass;
          if (eps_temp==0)
        	  ass=2;
          if (k == 0)
            eps_k = eps_temp;
          else if (eps_k < eps_temp)
            eps_k = eps_temp;
        }
        if (j == 0)
          eps_j = eps_k;
        else if (eps_j > eps_k)
          eps_j = eps_k;
      }
      if (i == 0)
        eps = eps_j;
      else if (eps < eps_j)
        eps = eps_j;
    }
    return eps;
  } // epsilon
 
  /**
   * Established the params by default
   */
  void  set_params() {
    int  i;
    obj_ = new int[dim_];
    for (i = 0; i < dim_; i++) {
      obj_[i] = 0;
    }
    method_ = 0;
  } // set_params
  
  
  /** 
  * Returns the additive-epsilon value of the paretoFront. This method call to the
  * calculate epsilon-indicator one
 * @throws Exception 
  */
  public static void main(String [] args) throws Exception {
    double ind_value;
 
    int numberObjectives = 5;	
    for (int algorithmIndex=0; algorithmIndex<algorithmNameList_.length; algorithmIndex++) {
		createFile(algorithmNameList_[algorithmIndex]);
		FileWriter fw = new FileWriter(fileName.getAbsoluteFile());
		file = new BufferedWriter(fw);
		for (int row=1;row<101;row++){
			 for (int column=1;column<76;column++){
				    EpsilonBACKUP5 qualityIndicator = new EpsilonBACKUP5();
				    double [][] solutionFront = readFile(row, column, algorithmNameList_[algorithmIndex]);
				    double [][] trueFront     = readTrueFile(column);
				    //qualityIndicator.dim_ = trueParetoFront[0].length;
				    //qualityIndicator.set_params();
				            
				    ind_value = qualityIndicator.epsilon(trueFront,
				    									 solutionFront,
				                                         numberObjectives);
				    
				    //System.out.println(ind_value);
				    file.write(ind_value +"\t" );
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
				//System.out.println("/Users/Dipesh/OneDrive/Papers/ICST2015/Result/"+numberCases+"/"+algorithm+"/"+algorithm + column + "_" + row);
			    BufferedReader in = new BufferedReader(new FileReader("/Users/Dipesh/OneDrive/Papers/ICST2015/Result/"+numberCases+"/"+algorithm+"/"+algorithm + column + "_" + row));
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
		fileName = new File("/Users/Dipesh/OneDrive/Papers/ICST2015/Analysis/"+numberCases+"/epsilon/" + algorithm+ ".txt");
		//System.out.println("/Users/Dipesh/OneDrive/Papers/JournalShuai/Analysis/"+numberCases+"/epsilon/" + algorithm+ ".txt");
		// if file does not exists, then create it
		if (!fileName.exists()) {
			fileName.createNewFile();
		}
	}
	
	public static double [][] readTrueFile(int problem) throws IOException{	
		File file = new File("/Users/Dipesh/OneDrive/Papers/ICST2015/Analysis/"+numberCases+"/nondominated/"+problem+ ".txt");
		int size = countLines(file);
		double [][] trueFront = new double[size][5] ;
	    int counter;
	   
			try {
			    BufferedReader in = new BufferedReader(new FileReader("/Users/Dipesh/OneDrive/Papers/ICST2015/Analysis/"+numberCases+"/nondominated/"+problem+ ".txt"));
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
//			    	if (i==100)
//			    		break;
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
} // Epsilon


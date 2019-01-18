package jmetal.qualityIndicator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.experiments.Experiment;
import jmetal.qualityIndicator.Epsilon;
import jmetal.qualityIndicator.Hypervolume;
import jmetal.qualityIndicator.InvertedGenerationalDistance;
import jmetal.qualityIndicator.Spread;
import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.NonDominatedSolutionList;

public class QualityIndicatorBACKUP5 {

	int problemcount;
	int startindex;
	String [] algorithmNameList_; 
	String [] indicatorList_ ;
	int independentRuns_;
	
	int  numberCases =165;
	public QualityIndicatorBACKUP5() {
		// TODO Auto-generated constructor stub
		
		 problemcount =75;
//		problemcount = 1456;
		 independentRuns_ = 100;
		algorithmNameList_ = new String[] {"smpso", "nsgaii", "spea2", "cellde", "ibea", "mocell", "ea"};
		
		
		 //algorithmNameList_ = new String[] { "ea"};
		 indicatorList_ = new String[]{"HV", "SPREAD", "EPSILON"};
	}
	
	public void generateReferenceFronts()
	{
		for(int i = 1; i <=problemcount; i++)
		{
			generateReferenceFront(i);
//			System.out.println(i);
		}
	}
	private void generateReferenceFront(int problemIndex)
	{
		String path = "/Users/Dipesh/OneDrive/Papers/JournalShuai/Analysis/";
		File rfDirectory;
		String referenceFrontDirectory = path + "referenceFronts/";
		String frontPath_ = "/Users/Dipesh/OneDrive/Papers/ICST2015/Analysis/"+numberCases+"/nondominated/"+problemIndex+ ".txt";
		
		rfDirectory = new File(referenceFrontDirectory);

		if(!rfDirectory.exists()) { 													
			boolean result = new File(referenceFrontDirectory).mkdirs();			
			System.out.println("Creating " + referenceFrontDirectory);
		}


		MetricsUtil metricsUtils = new MetricsUtil();
		NonDominatedSolutionList solutionSet = new NonDominatedSolutionList();
		for (int algorithmIndex=0; algorithmIndex<algorithmNameList_.length; algorithmIndex++) {

			for (int numRun=1; numRun<=independentRuns_; numRun++) {
				String outputParetoFrontFilePath;
				outputParetoFrontFilePath = "/Users/Dipesh/OneDrive/Papers/ICST2015/Result/"+numberCases+"/"+ algorithmNameList_[algorithmIndex] +"/"+algorithmNameList_[algorithmIndex] + problemIndex + "_" + numRun;
				String solutionFrontFile = outputParetoFrontFilePath;

				metricsUtils.readNonDominatedSolutionSet(solutionFrontFile, solutionSet);
			} // for
		} // for
		solutionSet.printObjectivesToFile(frontPath_);
	}
	
	public void generateQualityIndicators() throws IOException
	{
		String path = "C:\\nonconformity\\";
		if (indicatorList_.length > 0) {

			for (int algorithmIndex=0; algorithmIndex<algorithmNameList_.length; algorithmIndex++) {

				String algorithmDirectory;
				algorithmDirectory = path + algorithmNameList_[algorithmIndex];
				
				for (int indicatorIndex = 0; indicatorIndex < indicatorList_.length; indicatorIndex++) {
					String qualityIndicatorFile =  path + "QualityIndicator\\" + algorithmNameList_[algorithmIndex] + "_" + indicatorList_[indicatorIndex]+ ".txt";
					FileWriter os = new FileWriter(qualityIndicatorFile.toString());
				for (int problemIndex=1 ; problemIndex<=problemcount; problemIndex++) {

					
					String paretoFrontPath = path + "referenceFronts\\" + problemIndex + ".rf";				

//					for (int indicatorIndex = 0; indicatorIndex < indicatorList_.length; indicatorIndex++) {
						System.out.println("Experiment - Quality indicator: " + indicatorList_[indicatorIndex]);						

//						resetFile(problemDirectory + "\\" + indicatorList_[indicatorIndex]);

						for (int numRun=1; numRun<=independentRuns_; numRun++) {
							String problemDirectory = algorithmDirectory + numRun;
							String outputParetoFrontFilePath;
							outputParetoFrontFilePath = problemDirectory + "\\FUN_" + problemIndex + "_0.txt";
							String solutionFrontFile = outputParetoFrontFilePath;
							
							double value = 0;							

							if (indicatorList_[indicatorIndex].equals("HV")) {

								Hypervolume indicators = new Hypervolume();
								double[][] solutionFront =
										indicators.utils_.readFront(solutionFrontFile);
								double[][] trueFront =
										indicators.utils_.readFront(paretoFrontPath);
								value = indicators.hypervolume(solutionFront, trueFront, trueFront[0].length);

								qualityIndicatorFile = qualityIndicatorFile + "\\HV";

							}
							if (indicatorList_[indicatorIndex].equals("SPREAD")) {
								Spread indicators = new Spread();
								double[][] solutionFront =
										indicators.utils_.readFront(solutionFrontFile);
								double[][] trueFront =
										indicators.utils_.readFront(paretoFrontPath);
								value = indicators.spread(solutionFront, trueFront, trueFront[0].length);

								qualityIndicatorFile = qualityIndicatorFile + "\\SPREAD";
							}
							if (indicatorList_[indicatorIndex].equals("IGD")) {
								InvertedGenerationalDistance indicators = new InvertedGenerationalDistance();
								double[][] solutionFront =
										indicators.utils_.readFront(solutionFrontFile);
								double[][] trueFront =
										indicators.utils_.readFront(paretoFrontPath);
								value = indicators.invertedGenerationalDistance(solutionFront, trueFront, trueFront[0].length);

								qualityIndicatorFile = qualityIndicatorFile + "\\IGD";
							}
							if (indicatorList_[indicatorIndex].equals("EPSILON")) {
								Epsilon indicators = new Epsilon();
								double[][] solutionFront =
										indicators.utils_.readFront(solutionFrontFile);
								double[][] trueFront =
										indicators.utils_.readFront(paretoFrontPath);
								value = indicators.epsilon(solutionFront, trueFront, trueFront[0].length);

								qualityIndicatorFile = qualityIndicatorFile + "\\EPSILON";
							}


							if (!qualityIndicatorFile.equals(problemDirectory)) {
								try {
									os.write(value + "\t");
								} catch (IOException ex) {
									Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
								}																	
							} // if
						} // for
						os.write("\r\n");
					} // for
				os.close();
				System.out.println("Finish a file");
				} // for
				
			} // for
		} // if
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		QualityIndicatorBACKUP5 qi = new QualityIndicatorBACKUP5();
		qi.generateReferenceFronts();
		//qi.generateQualityIndicators();
	}

}

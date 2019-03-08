import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;


public class SimulatedAnnealer 
{
	//Seed parameter sets
	static MethodParameterSet seedMPS;
	static InstrumentParameterSet ips;

	//Precalculated result array
	static ArrayList<MethodParameterSet> precalcArray;

	//fixed arrays
	static ArrayList<Integer> resolutionArray =  
			new ArrayList<Integer>(Arrays.asList(15000, 30000, 60000, 120000, 240000));
	static ArrayList<Integer> agcTargetArray =  
			new ArrayList<Integer>(Arrays.asList(20000,50000,100000,200000,500000,1000000,3000000,5000000));

	static ArrayList<Integer> injectionTimeArray =  
			new ArrayList<Integer>(Arrays.asList(10, 20, 30, 40, 50, 75));

	static ArrayList<Integer> topNArray =  
			new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5)); 
	static ArrayList<Double> isolationArray =  
			new ArrayList<Double>(Arrays.asList(0.7, 1.4, 2.1, 2.8, 3.5, 4.2)); 
	static ArrayList<Double> dynamicExclusionArray =  
			new ArrayList<Double>(Arrays.asList(5.0, 7.5, 10.0, 15.0)); 
	static ArrayList<Integer> minAGCArray =  
			new ArrayList<Integer>(Arrays.asList(100, 1000, 10000)); 

	static int bestMPSGeneration = 0;
	static int totalCalculations = 0;

	//Method to mutate a parameter set
	public static MethodParameterSet mutate(MethodParameterSet mps, double maxDutyCycle)
	{
		Random generator = new Random();

		int ms2Resolution;
		int ms2InjectionTime;
		int ms2AGCTarget;
		int topN;
		double isolationWidth;
		double dynamicExclusion;
		int minAGCTarget;

		while(true)
		{
			//Generate int key for variable to change
			int key = getRandomNumberInRange(4); 


			//MS2Res
			if (key == 0)
				ms2Resolution = getRandomIntVariable(resolutionArray);
			else
				ms2Resolution = mps.ms2Resolution;

			//ms2InjectionTime
			if (key == 0)
				ms2InjectionTime = getRandomIntVariable(injectionTimeArray);
			else
				ms2InjectionTime = mps.ms2InjectionTime;

			//topN
			if (key == 0)
				topN = getRandomIntVariable(topNArray);
			else
				topN = mps.posTopN;

			//MS2AGC
			if (key == 1)
				ms2AGCTarget = getRandomIntVariable(agcTargetArray);
			else
				ms2AGCTarget = mps.ms2AGCTarget;


			//isolationWidth
			if (key == 2)
				isolationWidth = getRandomDoubleVariable(isolationArray);
			else
				isolationWidth = mps.isolationWidth;

			//dynamicExclusion
			if (key == 3)
				dynamicExclusion = getRandomDoubleVariable(dynamicExclusionArray);
			else
				dynamicExclusion = mps.dynamicExclusion;

			//minAGC
			if (key == 4)
				minAGCTarget = getRandomIntVariable(minAGCArray);
			else
				minAGCTarget = mps.minAGCTarget;

			//Create mutated child
			MethodParameterSet result = new MethodParameterSet(
					seedMPS.polaritySwitching, seedMPS.polarity, seedMPS.ms1Resolution, ms2Resolution, 
					seedMPS.ms1InjectionTime,  ms2InjectionTime, ms2AGCTarget, 
					topN, topN, isolationWidth, dynamicExclusion, 
					minAGCTarget, seedMPS.gradientLength, seedMPS.excludeIsotopes, seedMPS.steppedCE);

			//Refactor topN
			refactorTopN(result, maxDutyCycle);

			//Calculate duty cycle
			recalcDutyCycle(result);

			//Calculate fitness score
			calcFitnessScore(result);

			if (ips.dutyCycle<maxDutyCycle && result.fitnessScore>0)
				return result;
		}
	}

	//Method to read in method parameters
	public static ArrayList<MethodParameterSet> readMethodParameters(String filename) throws IOException
	{	
		ArrayList<MethodParameterSet> parameters = new ArrayList<MethodParameterSet>();
		MethodParameterSet mpsTemp;
		String line;
		String[] split;

		//Create file buffer
		File file = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		//read line if not empty
		while ((line = reader.readLine()) != null)
		{
			//Read in parameters
			if (!line.contains("polarity") && line.contains(","))
			{
				split= line.split(",");
				mpsTemp = new MethodParameterSet(line);
				mpsTemp.fitnessScore = Integer.valueOf(split[19]);
				parameters.add(mpsTemp);
			}
		}

		reader.close();

		return parameters;
	}

	//Method to return random int between two bounds
	public static int getRandomNumberInRange(int max) 
	{
		Random r = new Random();
		return r.nextInt((max - 0) + 1) + 0;
	}

	//Method to return random double variable
	public static Double getRandomDoubleVariable(ArrayList<Double> array) 
	{
		return array.get(getRandomNumberInRange(array.size()-1));
	}

	//Method to return random double variable
	public static Integer getRandomIntVariable(ArrayList<Integer> array) 
	{
		return array.get(getRandomNumberInRange(array.size()-1));
	}

	//Method to generate initial population based on all ITs and random other parameter selection
	public static ArrayList<MethodParameterSet> generatePseudoRandomInitialPopulation(int numDuplicates, double maxDutyCycle)
	{
		ArrayList<MethodParameterSet> parameterSet = new ArrayList<MethodParameterSet>();
		MethodParameterSet mpsTemp;
		int counter;

		//For all injection times
		for (int i=0; i<injectionTimeArray.size(); i++)
		{
			counter = 0;

			//For number of duplicates specified
			while(counter<numDuplicates)
			{
				//Generate mps
				mpsTemp = getRandomMPS(maxDutyCycle);

				//Alter injection time
				mpsTemp.ms2InjectionTime = injectionTimeArray.get(i);

				//Refactor topN
				refactorTopN(mpsTemp, maxDutyCycle);

				//Calculate duty cycle
				recalcDutyCycle(mpsTemp);

				//Get fitness score
				calcFitnessScore(mpsTemp);

				//Add to population if below duty cycle
				if (ips.dutyCycle<maxDutyCycle && mpsTemp.fitnessScore>0)
				{
					parameterSet.add(mpsTemp);
					counter++;
				}
			}
		}

		return parameterSet;
	}

	public static void calcFitnessScore(MethodParameterSet mps)
	{
		for (int i=0; i<precalcArray.size(); i++)
		{
			MethodParameterSet precalcMPS = precalcArray.get(i);
			if (precalcMPS.polaritySwitching == mps.polaritySwitching
					&& precalcMPS.ms2Resolution == mps.ms2Resolution
					&& precalcMPS.ms2InjectionTime == mps.ms2InjectionTime
					&& precalcMPS.ms2AGCTarget == mps.ms2AGCTarget
					&& precalcMPS.posTopN == mps.posTopN
					&& precalcMPS.negTopN == mps.negTopN
					&& precalcMPS.isolationWidth == mps.isolationWidth
					&& precalcMPS.dynamicExclusion == mps.dynamicExclusion
					&& precalcMPS.minAGCTarget == mps.minAGCTarget
					&& precalcMPS.polaritySwitching == mps.polaritySwitching
					&& precalcMPS.polaritySwitching == mps.polaritySwitching
					&& precalcMPS.polaritySwitching == mps.polaritySwitching
					&& precalcMPS.polaritySwitching == mps.polaritySwitching
					&& precalcMPS.polaritySwitching == mps.polaritySwitching
					&& precalcMPS.polaritySwitching == mps.polaritySwitching
					&& precalcMPS.polaritySwitching == mps.polaritySwitching
					&& precalcMPS.polaritySwitching == mps.polaritySwitching)
			{
				mps.fitnessScore = precalcMPS.fitnessScore;
				return;
			}	
		}
		mps.fitnessScore = 0;
	}

	public static String getFitnessScoreString(ArrayList<MethodParameterSet> mpsArray)
	{
		Collections.sort(mpsArray);

		String result = "";

		for (int i=0; i<mpsArray.size(); i++)
		{
			result += mpsArray.get(i).fitnessScore+" ";
			//result += mpsArray.get(i).posTopN+"-"+mpsArray.get(i).ms2InjectionTime+" ";
		}

		return result;
	}

	//Method to generate initial population based on random parameter selection
	public static ArrayList<MethodParameterSet> generateRandomInitialPopulation(int popSize, double maxDutyCycle)
	{
		ArrayList<MethodParameterSet> parameterSet = new ArrayList<MethodParameterSet>();
		MethodParameterSet mpsTemp;

		//While remaining population slots open
		while (parameterSet.size()<popSize)
		{
			//Generate mps
			mpsTemp = refactorTopN(getRandomMPS(maxDutyCycle), maxDutyCycle);

			//Calculate duty cycle
			recalcDutyCycle(mpsTemp);

			//Add to population if below duty cycle
			if (ips.dutyCycle<maxDutyCycle && mpsTemp.fitnessScore>0)
				parameterSet.add(mpsTemp);
		}

		return parameterSet;
	}

	//Method to automatically maximimize TopN and ms2 Res without impacting duty cycle
	public static MethodParameterSet refactorTopN(MethodParameterSet mps, Double maxDutyCycle)
	{
		MethodParameterSet mpsTemp = mps;
		int oritinalTopN = mps.posTopN;
		int bestTopN = oritinalTopN;
		int bestMS2Res = mps.ms2Resolution;
		double previousDutyCycle = ips.dutyCycle;

		for (int i=0; i<topNArray.size(); i++)
		{
			mpsTemp.posTopN = topNArray.get(i);
			mpsTemp.negTopN = topNArray.get(i);
			recalcDutyCycle(mpsTemp);

			if (ips.dutyCycle<maxDutyCycle)
				bestTopN = mpsTemp.posTopN;
		}

		mpsTemp.posTopN = bestTopN;
		mpsTemp.negTopN = bestTopN;
		recalcDutyCycle(mpsTemp);
		previousDutyCycle = ips.dutyCycle;

		for (int i=0; i<resolutionArray.size(); i++)
		{
			mpsTemp.ms2Resolution = resolutionArray.get(i);
			recalcDutyCycle(mpsTemp);
			if (ips.dutyCycle<=previousDutyCycle)
			{
				bestMS2Res = resolutionArray.get(i);
				previousDutyCycle = ips.dutyCycle;
			}
		}

		mpsTemp.ms2Resolution = bestMS2Res;

		return mpsTemp;
	}

	//Method to get random method parameter set
	public static MethodParameterSet getRandomKnownMPS(Double maxDutyCycle)
	{
		int counter = 0;
		Collections.shuffle(precalcArray);
		
		while(true)
		{
			recalcDutyCycle(precalcArray.get(counter));
			if (ips.dutyCycle<maxDutyCycle)
				return precalcArray.get(counter);
			else
				counter++;
		}
	}
	
	//Method which returns a random method parameter set
	public static MethodParameterSet getRandomMPS(Double maxDutyCycle)
	{
		while(true)
		{
			int randomTopN = getRandomIntVariable(topNArray);

			MethodParameterSet result = new MethodParameterSet(
					seedMPS.polaritySwitching, seedMPS.polarity, seedMPS.ms1Resolution, getRandomIntVariable(resolutionArray), 
					seedMPS.ms1InjectionTime,  getRandomIntVariable(injectionTimeArray), getRandomIntVariable(agcTargetArray), 
					randomTopN, randomTopN, getRandomDoubleVariable(isolationArray), getRandomDoubleVariable(dynamicExclusionArray), 
					getRandomIntVariable(minAGCArray), seedMPS.gradientLength, seedMPS.excludeIsotopes, seedMPS.steppedCE);

			refactorTopN(result, maxDutyCycle);
			
			if (ips.dutyCycle<maxDutyCycle && result.fitnessScore>0)
				return result;
		}
	}

	//Method to calculate duty cycle
	public static void recalcDutyCycle(MethodParameterSet mps)
	{
		//Calculate duty cycle
		ips.methodParameters = mps;
		ips.calculateDutyCycle();
	}

	public static MethodParameterSet selectMPS(MethodParameterSet seedMPS, MethodParameterSet neighborMPS, double temperature)
	{
		Random r = new Random();

		//If not improved, select using probability
		//Generate acceptance probability
		double probability = Math.pow(2.71828, (((neighborMPS.fitnessScore-seedMPS.fitnessScore)*1.0/seedMPS.fitnessScore*1.0)*10.0)/temperature);
		double randSeed = r.nextDouble();

		//If acceptance probability is greater than random seed, return neighbor
		if (probability>randSeed)
		{
			//System.out.println(seedMPS.fitnessScore+" "+neighborMPS.fitnessScore+" "+probability+" ");
			return neighborMPS;
		}
		else
			return seedMPS;

	}

	public static void runSimulatedAnnealing(Double maxDutyCycle, Double 
			minTemperature, Double alpha, int numMutations)
	{
		MethodParameterSet seedMPS;
		MethodParameterSet neighborMPS;
		Double temperature = 1.0;

		//Generate random solution
		seedMPS = getRandomKnownMPS(1500.0);

		//Calculate cost
		calcFitnessScore(seedMPS);

		while(temperature>minTemperature)
		{		
			//Mutate
			for (int i=0; i<numMutations; i++)
			{	
				//Increment calculations
				totalCalculations++;

				//Generate random neighboring solution
				neighborMPS = mutate(seedMPS, maxDutyCycle);

				//Calculate new solution cost
				calcFitnessScore(neighborMPS);

				//Compare two solutions
				seedMPS = selectMPS(seedMPS, neighborMPS, temperature);

				System.out.println(totalCalculations+" "+seedMPS.fitnessScore);
			}

			//Cool temperature
			temperature = temperature*alpha;
		}

		System.out.println(seedMPS);
	}

	public static void main(String[] args) throws NumberFormatException, IOException
	{
		Double minTemperature = 0.01;
		Double alpha = 0.9;
		int numMutations = 100;


		//System.out.println(Math.pow(2.71828, (((newScore-oldScore)*1.0/oldScore*1.0)*10.0)/temp));
		
		
		//Get precalculated results
		precalcArray = readMethodParameters("C:/Users/Alicia/Desktop/Iterative_Method_Parameters_Optimization.csv");

		//Load seed parameter sets
		seedMPS = new MethodParameterSet("TRUE,-,60000,15000,20,5,500000,5,5,1.4,10,10000,30,TRUE,TRUE");
		ips = new InstrumentParameterSet(seedMPS, null, false);

		//Run simulated annealing
		runSimulatedAnnealing(1500.0, minTemperature, alpha, numMutations);

		System.out.println("Estimated time (min) "+((totalCalculations*24))/60.0);
	
	}
}

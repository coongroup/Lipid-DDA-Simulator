import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;


public class GeneticBreeder 
{		
	//Seed parameter sets
	MethodParameterSet seedMPS;
	InstrumentParameterSet ips;
	MethodOptimizerSetting mops;

	//Results
	MethodParameterSet bestMPS = null;
	MethodParameterSet bestRandomMPS = null;
	int bestMPSGeneration = 0;
	int totalIterations = 0;
	int runNumber = 0;
	ArrayList<MethodParameterSet> mpsPopulation;

	//Precalculated result array
	ArrayList<MethodParameterSet> precalcArray;

	//fixed arrays
	ArrayList<Integer> resolutionArray =  
			new ArrayList<Integer>(Arrays.asList(15000, 30000, 60000, 120000, 240000));
	ArrayList<Integer> agcTargetArray =  
			new ArrayList<Integer>(Arrays.asList(20000,50000,100000,200000,500000,1000000,3000000,5000000));

	//Array to populate

	ArrayList<Integer> injectionTimeArray =  
			new ArrayList<Integer>(Arrays.asList(10, 15, 20, 25, 30, 35, 40, 45, 50));

	ArrayList<Integer> topNArray =  
			new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5)); 
	ArrayList<Double> isolationArray =  
			new ArrayList<Double>(Arrays.asList(0.7, 1.4, 2.1, 2.8, 3.5, 4.2)); 
	ArrayList<Double> dynamicExclusionArray =  
			new ArrayList<Double>(Arrays.asList(5.0, 7.5, 10.0, 15.0)); 
	ArrayList<Integer> minAGCArray =  
			new ArrayList<Integer>(Arrays.asList(0, 100, 1000, 10000, 100000, 1000000)); 

	//variable bounds
	/*
	int injectionTimeMin = 10;
	int injectionTimeMax = 50;
	int injectionTimeStep = 10;

	int topNMin = 2;
	int topNMax = 5;
	int topNStep = 1;

	double isolationMin = 0.7;
	double isolationMax = 4.2;
	double isolationStep = 0.7;

	double deMin = 5.0;
	double deMax = 15.0;
	double deStep = 5.0;

	int minAGCMin = 0;
	int minAGCMax = 100000;
	int minAGCStep = 500;
	 */

	//Constructor
	public GeneticBreeder(MethodParameterSet seedMPS, InstrumentParameterSet seedIPS, 
			MethodOptimizerSetting mops, ArrayList<Integer> resolutionArray, 
			ArrayList<Integer> agcTargetArray) throws IOException
	{
		//Initialize class variables and arrays
		this.seedMPS = seedMPS;
		this.ips = seedIPS;
		this.mops = mops;
		this.resolutionArray = pruneIntArray(resolutionArray, mops.ms2ResMin, mops.ms2ResMin);
		this.agcTargetArray = pruneIntArray(agcTargetArray, mops.ms2AGCMin, mops.ms2AGCMax);
		this.minAGCArray = pruneIntArray(minAGCArray, mops.minAGCMin, mops.minAGCMax);

		//Populate possible variable arrays
		populateVariableArrays();

		//Get precalculated results
		precalcArray = new ArrayList<MethodParameterSet>();
		//precalcArray = readMethodParameters("C:/Users/Alicia/Desktop/Iterative_Method_Parameters_Exhaustive.csv");
	}

	//Method to remove values not in min max range from integer array
	public ArrayList<Integer> pruneIntArray(ArrayList<Integer> intArray, int min, int max)
	{
		ArrayList<Integer> temp = new ArrayList<Integer>();

		//For all input integers
		for (int i=0; i<intArray.size(); i++)
		{
			//If above min and below max
			if (intArray.get(i) >= min && intArray.get(i) <= max)
			{
				temp.add(intArray.get(i));
			}
		}

		return temp;
	}

	//Method to read in method parameters
	public ArrayList<MethodParameterSet> readMethodParameters(String filename) throws IOException
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
				mpsTemp.fitnessScore = Integer.valueOf(split[20]);
				parameters.add(mpsTemp);
			}
		}

		reader.close();

		return parameters;
	}

	//Method to populate all variable arrays
	public void populateVariableArrays()
	{
		//Populate arrays
		injectionTimeArray = populateIntArray(mops.injectionTimeMin, mops.injectionTimeMax, mops.injectionTimeStep);
		topNArray = populateIntArray(mops.topNMin, mops.topNMax, mops.topNStep);
		isolationArray = populateDoubleArray(mops.isolationMin, mops.isolationMax, mops.isolationStep);
		dynamicExclusionArray = populateDoubleArray(mops.deMin, mops.deMax, mops.deStep);
		
		//Modify method parameter set variables
		seedMPS.polaritySwitching = mops.polaritySwitching;
		seedMPS.polarity = mops.polarity;
		seedMPS.gradientLength = mops.gradientLength;
		seedMPS.ms1InjectionTime = mops.ms1InjectionTime;
		seedMPS.ms1Resolution = mops.ms1Resolution;
		seedMPS.excludeIsotopes = mops.excludeIsotopes;
		seedMPS.steppedCE = mops.steppedCE;
	}

	//Method to populate variable arrays with potential double values
	public ArrayList<Double> populateDoubleArray(Double min, Double max, Double step)
	{
		ArrayList<Double> doubleArray = new ArrayList<Double>();

		for (double i = min; i<max; i=i+step)
		{
			
			doubleArray.add(Math.round(i*10.0)/10.0);
		}

		return doubleArray;
	}

	//Method to populate variable arrays with potential double values
	public ArrayList<Integer> populateIntArray(Integer min, Integer max, Integer step)
	{
		ArrayList<Integer> intArray = new ArrayList<Integer>();

		for (int i = min; i<=max; i=i+step)
		{
			intArray.add(i);
		}

		return intArray;
	}

	//Method to return random double variable
	public Double getRandomDoubleVariable(ArrayList<Double> array) 
	{
		return array.get(getRandomNumberInRange(array.size()-1));
	}

	//Method to return random double variable
	public Integer getRandomIntVariable(ArrayList<Integer> array) 
	{
		return array.get(getRandomNumberInRange(array.size()-1));
	}

	//Method to return random int between two bounds
	public int getRandomNumberInRange(int max) 
	{
		Random r = new Random();
		return r.nextInt((max - 0) + 1) + 0;
	}

	//Method which returns a random method parameter set
	public MethodParameterSet getRandomMPS()
	{
		int randomTopN = getRandomIntVariable(topNArray);

		MethodParameterSet result = new MethodParameterSet(
				seedMPS.polaritySwitching, seedMPS.polarity, seedMPS.ms1Resolution, getRandomIntVariable(resolutionArray), 
				seedMPS.ms1InjectionTime,  getRandomIntVariable(injectionTimeArray), getRandomIntVariable(agcTargetArray), 
				randomTopN, randomTopN, getRandomDoubleVariable(isolationArray), getRandomDoubleVariable(dynamicExclusionArray), 
				getRandomIntVariable(minAGCArray), seedMPS.gradientLength, seedMPS.excludeIsotopes, seedMPS.steppedCE);

		
		
		return mutate(refactorTopN(result));
	}

	//Method to breed to method parameter sets until a child is created below duty cycle
	public MethodParameterSet breedFitMPS(ArrayList<MethodParameterSet> parentSets)
	{
		MethodParameterSet mpsTemp;

		while (true)
		{
			//Breed
			mpsTemp = breedMPS(parentSets);

			//Calculate duty cycle
			recalcDutyCycle(mpsTemp);

			//Add to population if below duty cycle
			if (ips.dutyCycle<mops.maxDutyCycle)
			{
				return mpsTemp;
			}
		}
	}

	//Method to calculate duty cycle
	public void recalcDutyCycle(MethodParameterSet mps)
	{
		//Calculate duty cycle
		ips.methodParameters = mps;
		ips.calculateDutyCycle();
	}

	//Method to breed two method parameter sets using random selection of parent variables
	public MethodParameterSet breedMPS(ArrayList<MethodParameterSet> parentSets)
	{
		MethodParameterSet childMPS;

		//Link TopN IT MS2Res
		int linkIndex = getRandomNumberInRange(1);

		boolean polaritySwitching = parentSets.get(getRandomNumberInRange(1)).polaritySwitching;
		String polarity = parentSets.get(getRandomNumberInRange(1)).polarity;
		int ms1Resolution = parentSets.get(getRandomNumberInRange(1)).ms1Resolution;
		int ms2Resolution = parentSets.get(linkIndex).ms2Resolution;
		int ms1InjectionTime = parentSets.get(getRandomNumberInRange(1)).ms1InjectionTime;
		int ms2InjectionTime = parentSets.get(linkIndex).ms2InjectionTime;
		int ms2AGCTarget = parentSets.get(getRandomNumberInRange(1)).ms2AGCTarget;
		int posTopN = parentSets.get(linkIndex).posTopN;
		int negTopN = parentSets.get(linkIndex).negTopN;
		double isolationWidth = parentSets.get(getRandomNumberInRange(1)).isolationWidth;
		double dynamicExclusion = parentSets.get(getRandomNumberInRange(1)).dynamicExclusion;
		int minAGCTarget = parentSets.get(getRandomNumberInRange(1)).minAGCTarget;
		Double gradientLength = parentSets.get(getRandomNumberInRange(1)).gradientLength;
		boolean excludeIsotopes = parentSets.get(getRandomNumberInRange(1)).excludeIsotopes;
		boolean steppedCE = parentSets.get(getRandomNumberInRange(1)).steppedCE;

		childMPS = new MethodParameterSet(polaritySwitching, polarity, ms1Resolution, 
				ms2Resolution, ms1InjectionTime, ms2InjectionTime, ms2AGCTarget, 
				posTopN, negTopN, isolationWidth, dynamicExclusion, minAGCTarget, gradientLength, 
				excludeIsotopes, steppedCE);

		return refactorTopN(childMPS);
	}


	//Method to generate child using crossover
	public MethodParameterSet generateChild(ArrayList<Integer> crossoverArray,
			ArrayList<MethodParameterSet> parentSets)
	{
		//Fixed variables
		boolean polaritySwitching = parentSets.get(0).polaritySwitching;
		String polarity = parentSets.get(0).polarity;
		int ms1Resolution = parentSets.get(0).ms1Resolution;
		int ms1InjectionTime = parentSets.get(0).ms1InjectionTime;
		Double gradientLength = parentSets.get(0).gradientLength;
		boolean excludeIsotopes = parentSets.get(0).excludeIsotopes;
		boolean steppedCE = parentSets.get(0).steppedCE;

		//Mutable variables
		int ms2Resolution = parentSets.get(crossoverArray.get(0)).ms2Resolution;
		int ms2InjectionTime = parentSets.get(crossoverArray.get(0)).ms2InjectionTime;
		int posTopN = parentSets.get(crossoverArray.get(0)).posTopN;
		int negTopN = parentSets.get(crossoverArray.get(0)).negTopN;

		int ms2AGCTarget = parentSets.get(crossoverArray.get(1)).ms2AGCTarget;
		double isolationWidth = parentSets.get(crossoverArray.get(2)).isolationWidth;
		double dynamicExclusion = parentSets.get(crossoverArray.get(3)).dynamicExclusion;
		int minAGCTarget = parentSets.get(crossoverArray.get(4)).minAGCTarget;


		MethodParameterSet childMPS = new MethodParameterSet(polaritySwitching, polarity, ms1Resolution, 
				ms2Resolution, ms1InjectionTime, ms2InjectionTime, ms2AGCTarget, 
				posTopN, negTopN, isolationWidth, dynamicExclusion, minAGCTarget, gradientLength, 
				excludeIsotopes, steppedCE);

		childMPS.generation = totalIterations;

		return refactorTopN(childMPS);
	}

	//Method to generate "mutant" by randomly selecting parent TopN-IT and randomly assigning all others
	public MethodParameterSet generateMutantFromParents(ArrayList<MethodParameterSet> parentSets)
	{
		//Fixed variables
		boolean polaritySwitching = parentSets.get(0).polaritySwitching;
		String polarity = parentSets.get(0).polarity;
		int ms1Resolution = parentSets.get(0).ms1Resolution;
		int ms1InjectionTime = parentSets.get(0).ms1InjectionTime;
		Double gradientLength = parentSets.get(0).gradientLength;
		boolean excludeIsotopes = parentSets.get(0).excludeIsotopes;
		boolean steppedCE = parentSets.get(0).steppedCE;

		//Mutable variables
		int randomKey = getRandomNumberInRange(1);
		int ms2Resolution = parentSets.get(randomKey).ms2Resolution;
		int ms2InjectionTime = parentSets.get(randomKey).ms2InjectionTime;
		int posTopN = parentSets.get(randomKey).posTopN;
		int negTopN = parentSets.get(randomKey).negTopN;

		int ms2AGCTarget = agcTargetArray.get(getRandomNumberInRange(agcTargetArray.size()-1));
		double isolationWidth = isolationArray.get(getRandomNumberInRange(isolationArray.size()-1));
		double dynamicExclusion = dynamicExclusionArray.get(getRandomNumberInRange(dynamicExclusionArray.size()-1));
		int minAGCTarget = minAGCArray.get(getRandomNumberInRange(minAGCArray.size()-1));

		MethodParameterSet childMPS = new MethodParameterSet(polaritySwitching, polarity, ms1Resolution, 
				ms2Resolution, ms1InjectionTime, ms2InjectionTime, ms2AGCTarget, 
				posTopN, negTopN, isolationWidth, dynamicExclusion, minAGCTarget, gradientLength, 
				excludeIsotopes, steppedCE);

		childMPS.generation = totalIterations;
		childMPS.isMutant = true;

		return refactorTopN(childMPS);
	}

	//Method to breed two method parameter sets using random selection of parent variables
	public ArrayList<MethodParameterSet> crossoverMPS(ArrayList<MethodParameterSet> parentSets)
	{
		ArrayList<Integer> firstChildIntArray = new ArrayList<Integer>();
		ArrayList<Integer> secondChildIntArray = new ArrayList<Integer>();
		ArrayList<MethodParameterSet> result = new ArrayList<MethodParameterSet>();

		//Generate random crossover
		for (int i=0; i<5; i++)
		{
			int key = getRandomNumberInRange(1);
			firstChildIntArray.add(key);
			secondChildIntArray.add(Math.abs(key-1));
		}		

		//Add first child
		result.add(mutate(generateChild(firstChildIntArray,parentSets)));

		//Add second child
		result.add(mutate(generateChild(secondChildIntArray,parentSets)));

		//Add mutant child
		result.add(mutate(generateMutantFromParents(parentSets)));

		return result;
	}


	//Method to refactor parameter set to maximize top N and MS2 res 
	//within duty cycle constraint
	public MethodParameterSet refactorTopN(MethodParameterSet mps)
	{
		MethodParameterSet mpsTemp = mps;
		int oritinalTopN = mps.posTopN;
		int bestTopN = oritinalTopN;
		int bestMS2Res = mps.ms2Resolution;
		double previousDutyCycle = ips.dutyCycle;

		//Try all top N values, and retain maximum allowed
		for (int i=0; i<topNArray.size(); i++)
		{
			mpsTemp.posTopN = topNArray.get(i);
			mpsTemp.negTopN = topNArray.get(i);
			recalcDutyCycle(mpsTemp);

			if (ips.dutyCycle<mops.maxDutyCycle)
				bestTopN = mpsTemp.posTopN;
		}

		//Assign to mps
		mpsTemp.posTopN = bestTopN;
		mpsTemp.negTopN = bestTopN;
		recalcDutyCycle(mpsTemp);
		previousDutyCycle = ips.dutyCycle;

		//Try all MS2 res values, and retain maximum allowed
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

		//Assign to mps
		mpsTemp.ms2Resolution = bestMS2Res;

		return mpsTemp;
	}

	//Method to generate initial population based on all ITs and random other parameter selection
	public ArrayList<MethodParameterSet> generatePseudoRandomInitialPopulation()
	{
		ArrayList<MethodParameterSet> parameterSet = new ArrayList<MethodParameterSet>();
		MethodParameterSet mpsTemp;
		int counter;

		//For all injection times
		for (int i=0; i<injectionTimeArray.size(); i++)
		{
			counter = 0;

			//For number of duplicates specified
			while(counter<mops.numDuplicates)
			{
				//Generate mps
				mpsTemp = getRandomMPS();

				//Alter injection time
				mpsTemp.ms2InjectionTime = injectionTimeArray.get(i);

				//Refactor topN
				refactorTopN(mpsTemp);

				//Calculate duty cycle
				recalcDutyCycle(mpsTemp);

				//Add to population if below duty cycle
				if (ips.dutyCycle<mops.maxDutyCycle)
				{
					mpsTemp.generation = totalIterations;
					parameterSet.add(mpsTemp);
					counter++;
				}
			}
		}

		return parameterSet;
	}


	//Method to select breeding partners randomly
	public ArrayList<ArrayList<MethodParameterSet>> generateBreedingPartners(ArrayList<MethodParameterSet> mpsArray)
	{
		ArrayList<ArrayList<MethodParameterSet>> partners = new ArrayList<ArrayList<MethodParameterSet>>();

		//Shuffle array
		Collections.shuffle(mpsArray);

		for (int i=0; i<mpsArray.size()-1; i = i+2)
		{
			ArrayList<MethodParameterSet> tempArray = new ArrayList<MethodParameterSet>();
			tempArray.add(mpsArray.get(i));
			tempArray.add(mpsArray.get(i+1));
			partners.add(tempArray);
		}

		return partners;
	}

	//Method to generate population from two parents using two child and crossover
	public ArrayList<MethodParameterSet> createNewCrossoverGeneration(ArrayList<MethodParameterSet> parentSets, 
			int popSize)
			{
		ArrayList<MethodParameterSet> newGeneration = new ArrayList<MethodParameterSet>();
		//While remaining population slots open
		while (newGeneration.size()<popSize)
		{
			//Breed new child and mutate
			newGeneration.addAll(crossoverMPS(parentSets));
		}
		return newGeneration;
			}

	//Method to mutate a parameter set
	public MethodParameterSet mutate(MethodParameterSet mps)
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
			//MS2Res
			if (generator.nextDouble()<mops.mutationRate)
				ms2Resolution = getRandomIntVariable(resolutionArray);
			else
				ms2Resolution = mps.ms2Resolution;

			//ms2InjectionTime
			if (generator.nextDouble()<mops.mutationRate)
				ms2InjectionTime = getRandomIntVariable(injectionTimeArray);
			else
				ms2InjectionTime = mps.ms2InjectionTime;

			//MS2AGC
			if (generator.nextDouble()<mops.mutationRate)
				ms2AGCTarget = getRandomIntVariable(agcTargetArray);
			else
				ms2AGCTarget = mps.ms2AGCTarget;

			//topN
			if (generator.nextDouble()<mops.mutationRate)
				topN = getRandomIntVariable(topNArray);
			else
				topN = mps.posTopN;

			//isolationWidth
			if (generator.nextDouble()<mops.mutationRate)
				isolationWidth = getRandomDoubleVariable(isolationArray);
			else
				isolationWidth = mps.isolationWidth;

			//dynamicExclusion
			if (generator.nextDouble()<mops.mutationRate)
				dynamicExclusion = getRandomDoubleVariable(dynamicExclusionArray);
			else
				dynamicExclusion = mps.dynamicExclusion;

			//dynamicExclusion
			if (generator.nextDouble()<mops.mutationRate)
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
			refactorTopN(result);

			//Get fitness core if already calculated
			calcFitnessScore(result);
			
			//Calculate duty cycle
			recalcDutyCycle(result);

			//If mutation less than max duty cycle, return value
			if (ips.dutyCycle<mops.maxDutyCycle)
			{
				//Add to pre-calculated array
				precalcArray.add(result);
				return result;
			}
		}
	}

	//Update best mps found to date
	public void checkBestMPS(MethodParameterSet mps)
	{
		//If none exists
		if (bestMPS == null)
		{
			bestMPS = mps;
			bestMPSGeneration = totalIterations;
		}
		//Otherwise retain most fit
		else if (mps.fitnessScore>bestMPS.fitnessScore)
		{
			bestMPS = mps;
			bestMPSGeneration = totalIterations;
		}
	}

	//Returns average fitness for a given population
	public Double getAverageFitness(ArrayList<MethodParameterSet> mpsArray)
	{
		int fitnessTotal = 0;

		for (int i=0; i<mpsArray.size(); i++)
		{
			fitnessTotal += mpsArray.get(i).fitnessScore;
		}

		return ((double)fitnessTotal)/((double)mpsArray.size());
	}

	//Return best method parameter set from population
	public MethodParameterSet getBestPopMPS(ArrayList<MethodParameterSet> mpsArray)
	{
		//Sort by weight
		Collections.sort(mpsArray);

		//Recalculate best
		checkBestMPS(mpsArray.get(0));

		return mpsArray.get(0);
	}

	//Method to select a certain number of survivors based on roulette selection
	public ArrayList<MethodParameterSet> selectSurvivingMPS(ArrayList<MethodParameterSet> mpsArray)
	{
		ArrayList<MethodParameterSet> survivors = new ArrayList<MethodParameterSet>();
		int counter = 0;

		//Sort by fitness
		Collections.sort(mpsArray);

		//Update best mps
		for (int i=0; i<mpsArray.size(); i++)
		{
			checkBestMPS(mpsArray.get(i));
		}

		//While more surivivors need to be found
		while(survivors.size()<mops.numSurvivors)
		{
			//Select top n fitness
			survivors.add(mpsArray.get(counter));
			counter ++;
		}

		return survivors;
	}

	//Method to check whether a population has converged
	public boolean hasConverged(ArrayList<MethodParameterSet> mpsArray)
	{
		boolean sufficientFitness = false;
		boolean minimalPopPercentDiff = false;
		boolean minimalAllTimePercentDiff = false;
		boolean sufficientIterations = false;

		if (totalIterations<2)
			return false;

		//Sort array
		Collections.sort(mpsArray);

		//Gest best from population
		MethodParameterSet bestPopMS = getBestPopMPS(mpsArray);

		//Calculate percent differences
		double popPercentDiff = Math.abs(((double)bestPopMS.fitnessScore-getAverageFitness(mpsArray))/getAverageFitness(mpsArray));
		double allTimePercentDiff = Math.abs(((double)bestPopMS.fitnessScore-(double)bestMPS.fitnessScore)/((double)bestMPS.fitnessScore));

		//Assess convergence
		if (bestPopMS.fitnessScore>mops.minFitness)
			sufficientFitness = true;

		if (popPercentDiff<mops.maxPopPercentDiff)
			minimalPopPercentDiff = true;

		if (allTimePercentDiff<mops.maxAllTimePercentDiff)
			minimalAllTimePercentDiff = true;

		if (totalIterations > mops.minIterations)
			sufficientIterations = true;

		//Return result
		if (sufficientFitness && minimalPopPercentDiff && minimalAllTimePercentDiff && sufficientIterations)
			return true;
		else
			return false;
	}

	//Merge two arrayLists
	public ArrayList<MethodParameterSet> merge(ArrayList<MethodParameterSet> a1, ArrayList<MethodParameterSet> a2)
	{
		ArrayList<MethodParameterSet> tList = new ArrayList<MethodParameterSet>();

		for (MethodParameterSet element : a1) {
			tList.add(element);
		}
		for (MethodParameterSet element : a2){
			tList.add(element);
		}

		return tList;
	}

	//Method to return string representation of array of fitness scores
	public String getFitnessScores(ArrayList<MethodParameterSet> mpsArray)
	{
		Collections.sort(mpsArray);

		String result = "";

		for (int i=0; i<mpsArray.size(); i++)
		{
			result += mpsArray.get(i).fitnessScore+" ";
		}

		return result;
	}

	//Method to return pre-calculated fitness scores
	public void calcFitnessScore(MethodParameterSet mps)
	{
		if (precalcArray.size()>0)
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
						&& precalcMPS.minAGCTarget == mps.minAGCTarget)
				{
					mps.fitnessScore = precalcMPS.fitnessScore;
					return;
				}	
			}
		}
		else
		{
			mps.fitnessScore = 0;
		}
	}

	//Returns the median of an arrayList of doubles
	public int findMedian(ArrayList<Integer> nums)
	{
		Collections.sort(nums);
		int middle = nums.size()/2;
		if (nums.size()%2 == 1) {
			return nums.get(middle);
		} else {
			return (nums.get(middle-1) + nums.get(middle)) / 2;
		}
	}

	//Method to run initialize gentic algorithm and population
	public void initializeGeneticAlgorithm()
	{
		//Reset variables
		mpsPopulation = new ArrayList<MethodParameterSet>();

		//Generate population using pseudo random initialization
		mpsPopulation = generatePseudoRandomInitialPopulation();

		//Increment total iterations
		totalIterations ++;
	}
	
	//Method to perform elitist selection for each family
	public ArrayList<MethodParameterSet> performElitistSelection(ArrayList<ArrayList<MethodParameterSet>> tempGeneration)
	{
		ArrayList<MethodParameterSet> survivors = 
				new ArrayList<MethodParameterSet>();
		
		//For each family, select two most fit
		for (int i=0; i<tempGeneration.size(); i++)
		{
			//Sort by fitness
			Collections.sort(tempGeneration.get(i));

			//Select two most fit of family
			survivors.add(tempGeneration.get(i).get(0));
			survivors.add(tempGeneration.get(i).get(1));
		}
		
		return survivors;
	}

	//Method to perform next round of partner selection, crossover, and mutation
	public ArrayList<ArrayList<MethodParameterSet>> createNextGeneration(ArrayList<MethodParameterSet> currentGeneration)
	{
		ArrayList<ArrayList<MethodParameterSet>> tempGeneration = 
				new ArrayList<ArrayList<MethodParameterSet>>();
		
		//Increment total iterations
		totalIterations ++;

		//Generate breeding partners
		ArrayList<ArrayList<MethodParameterSet>> partners = generateBreedingPartners(currentGeneration);

		//Breed with mutation and select survivors using elitist selection
		for (int i=0; i<partners.size(); i++)
		{
			//Breed
			ArrayList<MethodParameterSet> mpsFamily = 
					merge(partners.get(i),createNewCrossoverGeneration(partners.get(i), 2));

			//Add to generation
			tempGeneration.add(mpsFamily);
		}
		
		return tempGeneration;
	}
	


	//Method to run genetic algorithm continously
	public void runGeneticAlgorithm()
	{
		//Reset variables
		ArrayList<MethodParameterSet> mpsPopulation = new ArrayList<MethodParameterSet>();

		//Generate population using pseudo random initialization
		mpsPopulation = generatePseudoRandomInitialPopulation();

		System.out.println(totalIterations+"  --  "+getFitnessScores(mpsPopulation));

		//Select Survivors
		mpsPopulation = selectSurvivingMPS(mpsPopulation);


		//While convergence has not been reached, continue genetic algorithm
		while (!hasConverged(mpsPopulation))
		{
			System.out.println(totalIterations+"  --  "+getFitnessScores(mpsPopulation));

			//Increment iterations
			totalIterations ++;

			//Generate breeding partners
			ArrayList<ArrayList<MethodParameterSet>> partners = generateBreedingPartners(mpsPopulation);

			//Create new population array
			ArrayList<MethodParameterSet> newPopulation = new ArrayList<MethodParameterSet>();

			//Breed with mutation and select survivors using elitist selection
			for (int i=0; i<partners.size(); i++)
			{
				//Breed
				ArrayList<MethodParameterSet> mpsFamily = merge(partners.get(i),createNewCrossoverGeneration(partners.get(i), 2));

				//Sort by fitness
				Collections.sort(mpsFamily);

				//Select two most fit of family
				newPopulation.add(mpsFamily.get(0));
				newPopulation.add(mpsFamily.get(1));
			}

			//Replace population
			mpsPopulation = newPopulation;
		}
	}

	/*
	public void main(String[] args) throws NumberFormatException, IOException
	{
		//Get precalculated results
		precalcArray = readMethodParameters("C:/Users/Alicia/Desktop/Iterative_Method_Parameters_Exhaustive.csv");

		//Populate variable arrays
		//populateVariableArrays();
		bestMPS = null;
		bestRandomMPS = null;

		//Create seed method parameter set
		seedMPS = new MethodParameterSet("TRUE,-,60000,15000,20,5,500000,5,5,1.4,10,10000,30,TRUE,TRUE");
		ips = new InstrumentParameterSet(seedMPS, null, false);
		runGeneticAlgorithm();
	}
	 */
}

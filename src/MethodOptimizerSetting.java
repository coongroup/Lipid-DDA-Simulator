import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;


public class MethodOptimizerSetting 
{
	//Genetic algorithm parameters
	double maxDutyCycle = 1200.0;
	int numSurvivors = 8;
	double mutationRate = 0.2;
	int numDuplicates = 4;

	//Convergence parameters
	int minFitness = 400;
	double maxPopPercentDiff = 0.001;
	double maxAllTimePercentDiff = 0.001;
	int minIterations = 60;

	//Fixed variables
	boolean polaritySwitching = true;
	String polarity = "+";
	int ms1Resolution = 60000;
	int ms1InjectionTime = 20;
	double gradientLength = 30.0;
	boolean excludeIsotopes = true;
	boolean steppedCE = true;

	//variable bounds
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
	
	int ms2AGCMin = 10000;
	int ms2AGCMax = 5000000;

	int minAGCMin = 0;
	int minAGCMax = 100000;

	int ms2ResMin = 15000;
	int ms2ResMax = 240000;

	ArrayList<Integer> minAGCArray =  
			new ArrayList<Integer>(Arrays.asList(100, 1000, 10000, 100000, 1000000)); 

	public MethodOptimizerSetting(String filename) throws IOException
	{
		//Populate values
		readMethodOptimizerSettings(filename);
	}

	//Method to check integrity of min max value
	public void checkValues() throws CustomException
	{
		if (injectionTimeMax<injectionTimeMin)
		{
			throw new CustomException("Invalid injection times");
		}
		
		if (topNMax<topNMin)
		{
			throw new CustomException("Invalid top N values");
		}
		
		if (isolationMax<isolationMin)
		{
			throw new CustomException("Invalid isolation width values");
		}
		
		if (deMax<deMin)
		{
			throw new CustomException("Invalid dynamic exclusion values");
		}
		
		if (ms2AGCMax<ms2AGCMin)
		{
			throw new CustomException("Invalid AGC target values");
		}
		
		if (minAGCMax<minAGCMin)
		{
			throw new CustomException("Invalid minimum AGC values");
		}
		
		if (ms2ResMax<ms2ResMin)
		{
			throw new CustomException("Invalid ms2 resolution values");
		}
	}
	
	
	//Method to read optimizer settings from file
	public void readMethodOptimizerSettings(String filename) throws IOException
	{
		String line;
		String[] split;

		//Create file buffer
		File file = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		//read line if not empty
		while ((line = reader.readLine()) != null)
		{
			split = line.split(",");

			if (line.contains("Polarity Switching"))
				this.polaritySwitching = Boolean.valueOf(split[1].toLowerCase());

			else if (line.contains("Positive Polarity") && Boolean.valueOf(split[1].toLowerCase()))
				this.polarity = "+";

			else if (line.contains("Negative Polarity") && Boolean.valueOf(split[1].toLowerCase()))
				this.polarity = "-";

			else if (line.contains("Gradient Length (min)"))
				this.gradientLength = Double.valueOf(split[1]);

			else if (line.contains("MS1 Max IT (ms)"))
				this.ms1InjectionTime = Integer.valueOf(split[1]);

			else if (line.contains("MS1 Resolution"))
				this.ms1Resolution = Integer.valueOf(split[1]);

			else if (line.contains("Exclude Isotopes"))
				this.excludeIsotopes = Boolean.valueOf(split[1].toLowerCase());

			else if (line.contains("Stepped CE"))
				this.steppedCE = Boolean.valueOf(split[1].toLowerCase());

			else if (line.contains("Top N Min"))
				this.topNMin = Integer.valueOf(split[1]);

			else if (line.contains("Top N Max"))
				this.topNMax = Integer.valueOf(split[1]);

			else if (line.contains("Top N Step"))
				this.topNStep = Integer.valueOf(split[1]);

			else if (line.contains("IW Min"))
				this.isolationMin = Double.valueOf(split[1]);

			else if (line.contains("IW Max"))
				this.isolationMax = Double.valueOf(split[1]);

			else if (line.contains("IW Step"))
				this.isolationStep = Double.valueOf(split[1]);

			else if (line.contains("MS2 Res Min"))
				this.ms2ResMin = Integer.valueOf(split[1]);

			else if (line.contains("MS2 Res Max"))
				this.ms2ResMax = Integer.valueOf(split[1]);

			else if (line.contains("MS2 IT Min"))
				this.injectionTimeMin = Integer.valueOf(split[1]);

			else if (line.contains("MS2 IT Max"))
				this.injectionTimeMax = Integer.valueOf(split[1]);
			
			else if (line.contains("MS2 AGC Min"))
				this.ms2AGCMin = Integer.valueOf(split[1]);
			
			else if (line.contains("MS2 AGC Max"))
				this.ms2AGCMax = Integer.valueOf(split[1]);

			else if (line.contains("MS2 IT Step"))
				this.injectionTimeStep = Integer.valueOf(split[1]);

			else if (line.contains("Dynamic Exclusion Min"))
				this.deMin = Double.valueOf(split[1]);

			else if (line.contains("Dynamic Exclusion Max"))
				this.deMax = Double.valueOf(split[1]);

			else if (line.contains("Dynamic Exclusion Step"))
				this.deStep = Double.valueOf(split[1]);

			else if (line.contains("Min AGC Min"))
				this.minAGCMin = Integer.valueOf(split[1]);

			else if (line.contains("Min AGC Max"))
				this.minAGCMax = Integer.valueOf(split[1]);

			else if (line.contains("Max Duty Cycle (ms)"))
				this.maxDutyCycle = Double.valueOf(split[1]);

			else if (line.contains("Num Survivors"))
				this.numSurvivors = Integer.valueOf(split[1]);

			else if (line.contains("Mutation Rate"))
				this.mutationRate = Double.valueOf(split[1]);

			else if (line.contains("Initial Duplicates"))
				this.numDuplicates = Integer.valueOf(split[1]);

			else if (line.contains("Min IDs"))
				this.minFitness = Integer.valueOf(split[1]);

			else if (line.contains("Max Pop Diff"))
				this.maxPopPercentDiff = Double.valueOf(split[1]);

			else if (line.contains("Max Best Diff"))
				this.maxAllTimePercentDiff = Double.valueOf(split[1]);

			else if (line.contains("Min Generations"))
				this.minIterations = Integer.valueOf(split[1]);
		}

		reader.close();
	}

	public void writeMethodOptimizerSettings(String filename) throws IOException
	{
		PrintWriter pw = new PrintWriter(filename);

		pw.println("Polarity Switching,"+this.polaritySwitching);
		pw.println("Positive Polarity,"+this.polarity.equals("+"));
		pw.println("Negative Polarity,"+this.polarity.equals("-"));
		pw.println("Gradient Length (min),"+this.gradientLength);
		pw.println("MS1 Max IT (ms),"+this.ms1InjectionTime);
		pw.println("MS1 Resolution,"+this.ms1Resolution);
		pw.println("Exclude Isotopes,"+this.excludeIsotopes);
		pw.println("Stepped CE,"+this.steppedCE);
		pw.println("Top N Min,"+this.topNMin);
		pw.println("Top N Max,"+this.topNMax);
		pw.println("Top N Step,"+this.topNStep);
		pw.println("IW Min,"+this.isolationMin);
		pw.println("IW Max,"+this.isolationMax);
		pw.println("IW Step,"+this.isolationStep);
		pw.println("MS2 Res Min,"+this.ms2ResMin);
		pw.println("MS2 Res Max,"+this.ms2ResMax);
		pw.println("MS2 IT Min,"+this.injectionTimeMin);
		pw.println("MS2 IT Max,"+this.injectionTimeMax);
		pw.println("MS2 IT Step,"+this.injectionTimeStep);
		pw.println("MS2 AGC Min,"+this.ms2AGCMin);
		pw.println("MS2 AGC Max,"+this.ms2AGCMax);
		pw.println("Dynamic Exclusion Min,"+this.deMin);
		pw.println("Dynamic Exclusion Max,"+this.deMax);
		pw.println("Dynamic Exclusion Step,"+this.deStep);
		pw.println("Min AGC Min,"+this.minAGCMin);
		pw.println("Min AGC Max,"+this.minAGCMax);
		pw.println("Max Duty Cycle (ms),"+this.maxDutyCycle);
		pw.println("Num Survivors,"+this.numSurvivors);
		pw.println("Mutation Rate,"+this.mutationRate);
		pw.println("Initial Duplicates,"+this.numDuplicates);
		pw.println("Min IDs,"+this.minFitness);
		pw.println("Max Pop Diff,"+this.maxPopPercentDiff);
		pw.println("Max Best Diff,"+this.maxAllTimePercentDiff);
		pw.println("Min Generations,"+this.minIterations);
		pw.close();
	}
}

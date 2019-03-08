import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;


public class Optimizer
{
	static ArrayList<MethodParameterSet> mpsArray = new ArrayList<MethodParameterSet>();
	//Dependent
	static int[] ms2ResolutionArray = {15000, 30000, 60000};
	static int[] transientArray = {32, 64, 128};
	
	//Independent
	static double[] isolationWidth = {0.7,1.4,2.1,2.8,3.5,4.2};
	static int[] agc = {20000,50000,100000,200000,500000,1000000,3000000,5000000};
	static double[] dynamicExclusion = {5.0,7.5,10.0,15.0};
	static int[] ms2InjectionTimeArray = {10,15,20,25,30,35,40,45,50};
	static int[] minAGC = {100,1000,10000};
	static int[] topNArray = {2,3,4,5};
	static int[] countArray = {0,0,0,0,0,0};
	static int[] limitArray = {isolationWidth.length-1,agc.length-1,dynamicExclusion.length-1,ms2InjectionTimeArray.length-1,minAGC.length-1,topNArray.length-1};
	static double maxDutyCycle = 1200.0;


	public static void main(String[] args) throws NumberFormatException, IOException
	{    		
		generateAllCombinations();
	}

	//Method to iterate through permutation
	public static void generateAllCombinations() throws NumberFormatException, IOException
	{
		MethodParameterSet mps = new MethodParameterSet();

		mps.isolationWidth = isolationWidth[countArray[0]];
		mps.ms2AGCTarget = agc[countArray[1]];
		mps.dynamicExclusion = dynamicExclusion[countArray[2]];
		mps.ms2InjectionTime = ms2InjectionTimeArray[countArray[3]];
		mps.minAGCTarget = minAGC[countArray[4]];
		mps.negTopN = topNArray[countArray[5]];
		mps.posTopN = topNArray[countArray[5]];
		
		InstrumentParameterSet ips = new InstrumentParameterSet(mps, null, false);
		ips.calculateDutyCycle();

		if (ips.dutyCycle<maxDutyCycle)
		{
			mpsArray.add(mps);
		}

		while(nextPermutation(limitArray,countArray))
		{
			mps = new MethodParameterSet();
			mps.isolationWidth = isolationWidth[countArray[0]];
			mps.ms2AGCTarget = agc[countArray[1]];
			mps.dynamicExclusion = dynamicExclusion[countArray[2]];
			mps.ms2InjectionTime = ms2InjectionTimeArray[countArray[3]];
			mps.minAGCTarget = minAGC[countArray[4]];
			mps.negTopN = topNArray[countArray[5]];
			mps.posTopN = topNArray[countArray[5]];

			
			for (int i=0; i<transientArray.length-1; i++)
			{
				if (ms2InjectionTimeArray[countArray[3]]-ips.cTrapClearTime > transientArray[i])
				{
					mps.ms2Resolution = ms2ResolutionArray[i];
				}
			}
			
			ips = new InstrumentParameterSet(mps, null, false);
			ips.calculateDutyCycle();
	
			if (ips.dutyCycle<maxDutyCycle)
			{
				//System.out.println(ips.dutyCycle+" "+mps);
				mpsArray.add(mps);
			}

		}
	}

	//Method to advance array of counters for generation of all permutations
	public static boolean nextPermutation(int[] limits, int[] counters)
	{
		int c = 0; // the current counter
		counters[c]++; // increment the first counter
		while (counters[c] > limits[c]) // if counter c overflows
		{
			counters[c] = 0; // reset counter c
			c++; // increment the current counter
			if (c >= limits.length) return false; // if we run out of counters, we're done
			counters[c]++;
		}
		return true;
	}
	
	public static void writeParameters(String filename) throws FileNotFoundException
	{
		PrintWriter pw = new PrintWriter(filename);
	
		pw.println("polaritySwitching,polarity,ms1Resolution,ms2Resolution,ms1InjectionTime,"
				+ "ms2InjectionTime,ms2AGCTarget,posTopN,negTopN,isolationWidth,dynamicExclusion"
				+ ",minAGCTarget,gradientLength,excludeIsotopes,steppedCE");

		for (int i=0; i<mpsArray.size(); i++)
		{
			pw.println(mpsArray.get(i).getParametersString(false));
		}

		pw.close();
	}
}

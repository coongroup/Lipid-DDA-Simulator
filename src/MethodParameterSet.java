import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class MethodParameterSet implements Comparable<MethodParameterSet>
{
	boolean polaritySwitching = true;
	String parameterString;
	String polarity = "-";
	int ms1Resolution = 60000;
	int ms2Resolution = 15000;
	int ms1InjectionTime = 20;
	int ms2InjectionTime = 80;
	int ms2AGCTarget = 100000;
	int posTopN = 2;
	int negTopN = 2;
	double isolationWidth = 0.7;
	double dynamicExclusion = 10.0;
	int minAGCTarget = 10000;
	int minMSMSIntensity = minAGCTarget*(1000/ms2InjectionTime);
	double gradientLength = 30.0;
	boolean excludeIsotopes = true;
	boolean steppedCE = true;
	int experimentNumber = -1;
	int fitnessScore = 0;
	int generation;
	double fitnessWeight= 0.0;
	boolean isMutant = false;

	//Constructor using default parameters
	public MethodParameterSet()
	{

	}

	//Constructor using direct parameters
	public MethodParameterSet(boolean polaritySwitching, String polarity, int ms1Resolution, 
			int ms2Resolution, int ms1InjectionTime, int ms2InjectionTime, int ms2AGCTarget, 
			int posTopN, int negTopN, double isolationWidth, double dynamicExclusion, int minAGCTarget, Double gradientLength, 
			boolean excludeIsotopes, boolean steppedCE)
	{
		//Parse
		this.polaritySwitching = polaritySwitching;
		this.polarity = polarity;
		this.ms1Resolution = ms1Resolution;
		this.ms2Resolution = ms2Resolution;
		this.ms1InjectionTime = ms1InjectionTime;
		this.ms2InjectionTime = ms2InjectionTime;
		this.ms2AGCTarget = ms2AGCTarget;
		this.posTopN = posTopN;
		this.negTopN = negTopN;
		this.isolationWidth = isolationWidth;
		this.dynamicExclusion = dynamicExclusion;
		this.minAGCTarget = minAGCTarget;
		this.gradientLength = gradientLength;
		this.excludeIsotopes = excludeIsotopes;
		this.steppedCE = steppedCE;
	}
	
	//Constructor using parameters read in from .csv
	public MethodParameterSet(String parameterString)
	{
		//Split line
		String[] split = parameterString.split(",");
		this.parameterString = parameterString;

		//Parse
		polaritySwitching = Boolean.valueOf(split[0].toLowerCase());
		polarity = split[1];
		ms1Resolution = Integer.valueOf(split[2]);
		ms2Resolution = Integer.valueOf(split[3]);
		ms1InjectionTime = Integer.valueOf(split[4]);
		ms2InjectionTime = Integer.valueOf(split[5]);
		ms2AGCTarget = Integer.valueOf(split[6]);
		posTopN = Integer.valueOf(split[7]);
		negTopN = Integer.valueOf(split[8]);
		isolationWidth = Double.valueOf(split[9]);
		dynamicExclusion = Double.valueOf(split[10]);
		minAGCTarget = Integer.valueOf(split[11]);
		minMSMSIntensity = minAGCTarget*(1000/ms2InjectionTime);
		gradientLength = Double.valueOf(split[12]);
		excludeIsotopes = Boolean.valueOf(split[13].toLowerCase());
		steppedCE = Boolean.valueOf(split[14].toLowerCase());
	}

	public String toString()
	{
		String result = "";

		result += fitnessScore+" ";
		
		if (polaritySwitching)
			result += "+/-  ";
		else
			result += polarity+"  ";

		result += "MS1Res:"+String.valueOf(ms1Resolution).substring
				(0, String.valueOf(ms1Resolution).length()-3)+"K ";
		result += "MS2Res:"+String.valueOf(ms2Resolution).substring
				(0, String.valueOf(ms2Resolution).length()-3)+"K ";
		result += "MS1IT:"+ms1InjectionTime+" ";
		result += "MS2IT:"+ms2InjectionTime+" ";
		result += "MS2 AGC:"+ms2AGCTarget+" ";
		result += "Top"+posTopN+" ";
		result += "IW:"+isolationWidth+" ";
		result += "DE:"+dynamicExclusion+" ";
		result += "MinAGC:"+minAGCTarget+" ";

		return result;
	}

	public String getParametersString(boolean printHeader)
	{
		String result = "";
		if (printHeader)
			result += ("polaritySwitching,polarity,ms1Resolution,ms2Resolution,ms1InjectionTime,"
					+ "ms2InjectionTime,ms2AGCTarget,posTopN,negTopN,isolationWidth,dynamicExclusion"
					+ ",minAGCTarget,gradientLength,excludeIsotopes,steppedCE\n");

		result += polaritySwitching+",";
		result += polarity+",";
		result += ms1Resolution+",";
		result += ms2Resolution+",";
		result += ms1InjectionTime+",";
		result += ms2InjectionTime+",";
		result += ms2AGCTarget+",";
		result += posTopN+",";
		result += negTopN+",";
		result += isolationWidth+",";
		result += dynamicExclusion+",";
		result += minAGCTarget+",";
		result += gradientLength+",";
		result += excludeIsotopes+",";
		result += steppedCE+",";

		return result;
	}
	
	public String[] getMutableParametersArray(int gen)
	{
		String[] result = new String[9];
		result[0] = String.valueOf(gen);
		result[1] = String.valueOf(fitnessScore);
		result[2] = String.valueOf(posTopN);
		result[3] = String.valueOf(isolationWidth);
		result[4] = String.valueOf(ms2Resolution);
		result[5] = String.valueOf(ms2InjectionTime);
		result[6] = String.valueOf(ms2AGCTarget);
		result[7] = String.valueOf(dynamicExclusion);
		result[8] = String.valueOf(minAGCTarget);

		return result;
	}
	
	//Sort method parameter sets by fitness weight
	public int compareTo(MethodParameterSet mps)
	{
		if (mps.fitnessScore>this.fitnessScore)
			return 1;
		else if (mps.fitnessScore<this.fitnessScore)
			return -1;
		else
			return 0;
	}

	public void writeParameters(String filename) throws FileNotFoundException
	{
		PrintWriter pw = new PrintWriter(filename);

		pw.println("polaritySwitching,polarity,ms1Resolution,ms2Resolution,ms1InjectionTime,"
				+ "ms2InjectionTime,ms2AGCTarget,posTopN,negTopN,isolationWidth,dynamicExclusion"
				+ ",minAGCTarget,gradientLength,excludeIsotopes,steppedCE");

		pw.print(polaritySwitching+",");
		pw.print(polarity+",");
		pw.print(ms1Resolution+",");
		pw.print(ms2Resolution+",");
		pw.print(ms1InjectionTime+",");
		pw.print(ms2InjectionTime+",");
		pw.print(ms2AGCTarget+",");
		pw.print(posTopN+",");
		pw.print(negTopN+",");
		pw.print(isolationWidth+",");
		pw.print(dynamicExclusion+",");
		pw.print(minAGCTarget+",");
		pw.print(gradientLength+",");
		pw.print(excludeIsotopes+",");
		pw.print(steppedCE+",");


		pw.close();
	}
}

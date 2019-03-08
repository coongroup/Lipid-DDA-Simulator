import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class InstrumentParameterSet 
{
	int scanOverhead = 6;
	int cTrapClearTime = 23;
	int switchingTime = 233;
	public Integer[] resolutionArray = {15000, 30000, 60000, 120000, 240000};
	public Integer[] transientArray = {32, 64, 128, 256, 512};
	public Integer[] agcTargetArray = {20000,50000,100000,200000,500000,1000000,3000000,5000000};
	double dutyCycle;	
	double posDutyCycle;
	double negDutyCycle;
	double gapTime;
	MethodParameterSet methodParameters;
	boolean useMZXMLScan = false;
	double negNoiseIntensity = 2500.0;
	double posNoiseIntensity = 50000.0;
	double fragmentationEfficiency = 0.1;
	double minMS2SN = 3;
	double minPIF = 0.5;
	boolean onlyLipids = false;
	double ppmTolerance = 15.0;
	boolean precursorIDOnly = true;

	public InstrumentParameterSet(MethodParameterSet methodParameters, String filename, boolean calcDutyCycle) throws NumberFormatException, IOException
	{
		//Load in method parameters
		this.methodParameters = methodParameters;
		
		//Read in global params from csv file
		if (filename != null)
			readParams(filename);
		
		//Calculate method scan time variables
		if (calcDutyCycle)
			calculateDutyCycle();
	}
	
	public Object[][] getTransientArray()
	{
		Object[][] result = new Object[resolutionArray.length][2];
		
		for (int i=0; i<transientArray.length; i++)
		{
			result[i][0] = resolutionArray[i];
			result[i][1] = transientArray[i];
		}
		
		return result;
	}
	
	public Integer[] getResolutionArray()
	{
		Integer[] result = new Integer[resolutionArray.length];
		
		for (int i=0; i<resolutionArray.length; i++)
		{
			result[i] = resolutionArray[i];
		}
		
		return result;
	}
	
	public void readParams(String filename) throws NumberFormatException, IOException
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
			
			//Read in parameters
			if (line.contains("Scan Overhead (ms)"))
				this.scanOverhead = Integer.valueOf(split[1]);
			
			else if (line.contains("C Trap Clear Time (ms)"))
				this.cTrapClearTime = Integer.valueOf(split[1]);
			
			else if (line.contains("Polarity Switching Time (ms)"))
				this.switchingTime = Integer.valueOf(split[1]);
			
			else if (line.contains("Resolution Settings"))
			{
				resolutionArray = new Integer[split.length-1];
				
				for (int i=0; i<split.length-1; i++)
				{
					resolutionArray[i] = Integer.valueOf(split[i+1]);
				}
			}
		
			else if (line.contains("AGC Target Options"))
			{
				agcTargetArray = new Integer[split.length-1];
				
				for (int i=0; i<split.length-1; i++)
				{
					agcTargetArray[i] = Integer.valueOf(split[i+1]);
				}
			}
			
			else if (line.contains("Transient Time (ms)"))
			{
				transientArray = new Integer[split.length-1];
				
				for (int i=0; i<split.length-1; i++)
				{
					transientArray[i] = Integer.valueOf(split[i+1]);
				}
			}
			
			else if (line.contains("Negative Noise Intensity"))
				this.negNoiseIntensity = Double.valueOf(split[1]);
			
			else if (line.contains("Positive Noise Intensity"))
				this.posNoiseIntensity = Double.valueOf(split[1]);
			
			else if (line.contains("Minimum MS2 SN"))
				this.minMS2SN = Double.valueOf(split[1]);
			
			else if (line.contains("Only Sample Lipids"))
				this.onlyLipids = Boolean.valueOf(split[1].toLowerCase());
			
			else if (line.contains("Mass Tolerance (ppm)"))
				this.ppmTolerance = Double.valueOf(split[1]);
			else if (line.contains("Precursor mz Lipid ID"))
				this.precursorIDOnly = Boolean.valueOf(split[1]);
			
		}

		reader.close();
	}

	//Calculate method scan time variables
	public void calculateDutyCycle()
	{
		double posDuration;
		double negDuration;
		double ms1Transient = -1.0;
		double ms2Transient = -1.0;
		double ms1IT;
		double ms2IT;

		//Get transient times
		for (int i=0; i<resolutionArray.length; i++)
		{
			if (resolutionArray[i] == methodParameters.ms1Resolution)
			{
				ms1Transient = transientArray[i];
			}
			if (resolutionArray[i] == methodParameters.ms2Resolution)
			{
				ms2Transient = transientArray[i];
			}
		}

		ms1IT = methodParameters.ms1InjectionTime + cTrapClearTime;
		ms2IT = methodParameters.ms2InjectionTime + cTrapClearTime;

		//Add in stepped CE delay
		if (methodParameters.steppedCE)
			ms2IT += 10.0;

		//Polarity switching calculation
		if (methodParameters.polaritySwitching)
		{
			//posDuration = switching time + ms1 injection time + overhead + (larger of ms1 transient or ms2 IT + overhead) + (larger of ms2 t or ms2 IT)*(topn -1) + ms2 transient
			posDuration = switchingTime + (ms1IT + scanOverhead) + (getMax(ms2IT, ms1Transient) + scanOverhead)
					+ (getMax(ms2IT, ms2Transient) + scanOverhead)*(methodParameters.posTopN-1)+ms2Transient;
			negDuration = switchingTime + (ms1IT + scanOverhead) + (getMax(ms2IT, ms1Transient) + scanOverhead)
					+ (getMax(ms2IT, ms2Transient) + scanOverhead)*(methodParameters.negTopN-1)+ms2Transient;
			posDutyCycle = posDuration;
			negDutyCycle = negDuration;
			dutyCycle = posDuration + negDuration;
			gapTime = ms2Transient+ms1IT+switchingTime+scanOverhead;
		}
		//Normal operation calculation
		else
		{
			if (methodParameters.polarity.equals("+"))
			{
				dutyCycle = (getMax(ms2IT, ms1Transient) + scanOverhead) + (getMax(ms2IT, ms2Transient) + scanOverhead)*(methodParameters.posTopN-1) + (getMax(ms1IT, ms2Transient) + scanOverhead);
				posDutyCycle = dutyCycle;
				gapTime = getMax(ms1IT, ms2Transient)+scanOverhead;
			}
			else
			{
				dutyCycle = (getMax(ms2IT, ms1Transient) + scanOverhead) + (getMax(ms2IT, ms2Transient) + scanOverhead)*(methodParameters.negTopN-1) + (getMax(ms1IT, ms2Transient) + scanOverhead);
				negDutyCycle = dutyCycle;
				gapTime = getMax(ms1IT, ms2Transient)+scanOverhead;
			}
		}
	}

	//Return maximum of two numbers
	public Double getMax(Double a, Double b)
	{
		if (a>b)
			return a;
		else
			return b;
	}
}

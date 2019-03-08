import java.util.ArrayList;


public class ScanCycle 
{
	Double time;
	String polarity;
	Spectrum ms1;
	ArrayList<MSMS> ms2;

	public ScanCycle (Double time, String polarity, Spectrum ms1)
	{
		this.time = time;
		this.polarity = polarity;
		this.ms1 = ms1;
		ms2 = new ArrayList<MSMS>();
	}

	//Populate scan cycle objects with  MS/MS scans
	public void generateMSMSScans(DataDependentAlgorithm dd, 
			ArrayList<ExclusionListEntry> exclusionList, double ppmDiff, 
			ArrayList<Double> maxMSMSTimes,  ArrayList<Double> minMSMSTimes, 
			Spectrum seedSpectrum, MethodParameterSet methodParameters,
			Double minMS2SN, Double minPIF, Double fragmentationEfficiency)
	{
		ms2 = dd.generateDDMS2Scans(exclusionList, seedSpectrum, ppmDiff, maxMSMSTimes, minMSMSTimes, ms1.time, 
				methodParameters.ms2InjectionTime*1.0, methodParameters.ms2AGCTarget, methodParameters.isolationWidth, 
				methodParameters.minAGCTarget, minMS2SN, minPIF, fragmentationEfficiency);
	}
	
	//Populate scan cycle objects with  MS/MS scan using a specific time and precursor
	public void generateSingleMSMSScan(DataDependentAlgorithm dd, Spectrum seedSpectrum, 
			Double injectionTime, Double precursor, String polarity, Double actualTime,
			Double isolationWidth, MethodParameterSet methodParameters, boolean useMZXMLScan, 
			double minMS2SN, Double minPIF, Double fragmentationEfficiency)
	{
		MSMS result = dd.generateSingleMSMSScan(seedSpectrum, actualTime, injectionTime, 
				precursor, polarity, isolationWidth, useMZXMLScan, minMS2SN, minPIF, fragmentationEfficiency);
		
		if (result != null)
			ms2.add(result);
	}

	public String msmsStatsString()
	{
		String result = "";

		for (int i=0; i<ms2.size(); i++)
		{
			result += ms2.get(i);
		}

		return result;
	}

	public String toString()
	{
		String result = "";

		result += "Time:"+time+"\n";

		if (ms1 != null)
		{
			result += "  "+polarity+"MS1: "+ms1.time+"\n";

			for (int i=0; i<ms2.size(); i++)
			{
				result += "    "+polarity+"MS2: "+ms2.get(i).time+" "+ms2.get(i).parentPeak.mz;
			}
		}

		return result;
	}
}	

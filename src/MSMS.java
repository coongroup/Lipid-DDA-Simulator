import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class MSMS 
{
	Double time;
	int sampleDepth;
	String polarity;
	Feature parentFeature;
	Peak parentPeak;
	Peak actualPeak;
	Double injectionTime;
	boolean successful;
	boolean lipidSampled;
	Double precursorIonFraction;
	Double precursorIonFractionNoIsotopes;
	Double signal;
	Double noise;
	Double sn;
	Double percentAGCUsed;
	Double theoreticalInjectionTime;
	boolean sufficientSN;
	boolean sufficientPIF;
	Double minimumInjectionTimeSN;
	int numSuccessIterations = 50;
	int successfulCount = 0;

	//Arrays
	ArrayList<Boolean> successfulArray;
	ArrayList<Double> snArray;

	public MSMS (Double time, int sampleDepth, String polarity, Feature parentFeature, 
			Peak parentPeak, Double injectionTime, Double precursorIonFraction,
			Double theoreticalInjectionTime, Double precursorIonFractionNoIsotopes,
			MethodParameterSet methodParameters, double minMS2SN, Double minPIF)
	{
		//Set class variables
		this.time = time;
		this.sampleDepth = sampleDepth;
		this.polarity = polarity;
		this.parentFeature = parentFeature;
		this.parentPeak = parentPeak;
		this.injectionTime = injectionTime;
		this.precursorIonFraction = precursorIonFraction;
		this.successful = false;
		this.sufficientSN = true;
		this.sufficientPIF = true;
		this.theoreticalInjectionTime = theoreticalInjectionTime;
		this.precursorIonFractionNoIsotopes = precursorIonFractionNoIsotopes;
		successfulArray = new ArrayList<Boolean>();
		snArray = new ArrayList<Double>();

		if (parentFeature != null && (parentFeature.id != null || parentFeature.mzID != null))
			lipidSampled = true;

		if (parentFeature != null)
		{
			parentFeature.sampled = true;
			parentFeature.msmsArray.add(this);
		}

		this.percentAGCUsed = injectionTime/theoreticalInjectionTime;
		this.noise = 15000000.0/(methodParameters.ms2InjectionTime*Math.sqrt(methodParameters.ms2Resolution));

		for (int i=0; i<numSuccessIterations; i++)
		{
			//RANDOM NUMBER FRAG EFF USING NORMAL DISTRIBUTION

			Random r = new Random();
			double result;

			if (this.polarity.equals("-"))
				result = r.nextGaussian()*0.3-1.4;
			else
				result = r.nextGaussian()*0.3-1.9;

			double fragEff = Math.pow(10,(result));

			this.signal = parentPeak.intensity * fragEff * precursorIonFraction;

			this.sn = signal/noise;

			snArray.add(signal/noise);

			if (sn > minMS2SN && precursorIonFraction > minPIF)
			{
				if (lipidSampled)
				{
					successfulArray.add(true);
					successfulCount++;
				}
				else
				{
					successfulArray.add(false);
				}
			}
			else
			{
				successfulArray.add(false);
			}
		}
		
		
		this.sn = findMedian(snArray);
		this.signal = this.sn*this.noise;
		this.minimumInjectionTimeSN = (this.noise*this.injectionTime*3.0)/this.signal;
		
		if (successfulCount*2>numSuccessIterations)
			this.successful = true;
		
		if (sn < minMS2SN)
			this.sufficientSN = false;
		if (precursorIonFraction < minPIF)
			this.sufficientPIF = false;
	
	}

	//Returns the median of an arrayList of doubles
	public double findMedian(ArrayList<Double> nums)
	{
		Collections.sort(nums);
		int middle = nums.size()/2;
		if (nums.size()%2 == 1) {
			return nums.get(middle);
		} else {
			return (nums.get(middle-1) + nums.get(middle)) / 2.0;
		}
	}

	public boolean isSuccesful(int key)
	{
		return successfulArray.get(key);
	}

	public String toString()
	{
		return time+","+polarity+","+parentPeak.mz+","+parentPeak.intensity+","+sampleDepth+
				","+injectionTime+","+theoreticalInjectionTime+","+this.minimumInjectionTimeSN+","+signal+","+noise+","+sn
				+","+precursorIonFraction+","+precursorIonFractionNoIsotopes+","+successful+","+lipidSampled+","+sufficientSN+","+sufficientPIF+"\n";
	}
}

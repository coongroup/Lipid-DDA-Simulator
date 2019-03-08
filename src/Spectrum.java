import java.util.ArrayList;
import java.util.Collections;


public class Spectrum 
{
	Double time;
	String polarity;
	ArrayList<ArrayList<Peak>> mzArray;
	ArrayList<Peak> intensitySortedPeakArray;
	int numPeaks;
	Double minMass;
	Double maxMass;
	Double binWidth = 1.0;
	Double tic = 0.0;
	int maxDepth = 0;
	int maxIDDepth = 0;

	public Spectrum(Double time, String polarity, Double minMass, Double maxMass)
	{
		//Initialize variables
		this.time = time;
		this.polarity = polarity;
		this.minMass = minMass;
		this.maxMass = maxMass;
		this.mzArray = new ArrayList<ArrayList<Peak>>();
		this.intensitySortedPeakArray = new ArrayList<Peak>();
		numPeaks = 0;

		//Populate blank spectrum array
		populateSpectrumArray();
	}

	public void calculateDepth(boolean excludeIsotopes)
	{
		ArrayList<Peak> peakArray = new ArrayList<Peak>();

		//Add all peaks
		for (int i=0; i<mzArray.size(); i++)
		{
			for (int j=0; j<mzArray.get(i).size(); j++)
			{	
				if (!mzArray.get(i).get(j).isotope)
				{
					//Add Peak
					peakArray.add(mzArray.get(i).get(j));
					
					//Update depth
					maxDepth ++;
				}
			}
		}
		
		//Sory in descending intensity order
		Collections.sort(peakArray);
		Collections.sort(intensitySortedPeakArray);
		
		//Label with appropriate depth
		for (int i=0; i<peakArray.size(); i++)
		{
			peakArray.get(i).depth = (i+1);
			
			if (peakArray.get(i).feature != null 
					&& peakArray.get(i).feature.id != null)
				maxIDDepth = (i+1);
		}

	}

	private void populateSpectrumArray()
	{
		int numBins = (int)((maxMass - minMass)/binWidth)+2;

		for (int i=0; i<numBins; i++)
		{
			this.mzArray.add(new ArrayList<Peak>());
		}
	}

	public double getPeakIntensity(Double mz, Double ppmTol)
	{
		double intSum = 0.0;

		for (int i=0; i<mzArray.get(calcBin(mz)).size(); i++)
		{
			if (calcPPMDiff(mz, mzArray.get(calcBin(mz)).get(i).mz)<ppmTol)
			{
				intSum += mzArray.get(calcBin(mz)).get(i).intensity;
			}
		}

		return intSum;
	}

	//Calculate the difference between two masses in ppm
	public  Double calcPPMDiff(Double mass1, Double mass2)
	{
		return (Math.abs(mass1 -  mass2)/(mass2))*1000000;
	}

	public void addPeak(Peak p, Double resolution, Double noiseLevel, boolean useMZXMLScan)
	{
		Peak targetPeak;
		boolean found = false;

		//Calculate hashmap index
		int index = calcBin(p.mz);

		//Find any isobaric peaks
		for (int i=0; i<mzArray.get(index).size(); i++)
		{
			//If within resolution mz range
			if (Math.abs(mzArray.get(index).get(i).mz-p.mz) < resolution)
			{
				found = true;

				//Associate with object
				targetPeak = mzArray.get(index).get(i);

				//Retain peak object which is higher in intensity but add intensities
				//If in mzxml mode, don't add
				if (p.intensity > targetPeak.intensity)
				{
					if (useMZXMLScan)
					{
						tic -= targetPeak.intensity;
						tic += p.intensity;
					}
					else
					{
						tic += p.intensity;
						p.intensity = p.intensity+targetPeak.intensity;
					}

					mzArray.get(index).remove(i);
					mzArray.get(index).add(p);
					intensitySortedPeakArray.add(p);
				}
			}
		}

		if (!found && p.intensity > noiseLevel)
		{
			mzArray.get(index).add(p);
			intensitySortedPeakArray.add(p);
			tic += p.intensity;
			numPeaks ++;
		}
	}

	private int calcBin(Double mz)
	{
		return (int)((mz-minMass)/binWidth);
	}

	public String toString()
	{
		String result = "";

		result += "Time:"+this.time+"\n";
		result += "Polarity:"+this.polarity+"\n";

		for (int i=0; i<mzArray.size(); i++)
		{
			for (int j=0; j<mzArray.get(i).size(); j++)
			{
				result += mzArray.get(i).get(j)+"\n";
			}
		}

		result += "-------";

		return result;
	}
}

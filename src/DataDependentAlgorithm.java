import java.util.ArrayList;
import java.util.Collections;


public class DataDependentAlgorithm 
{
	//MS Method Variables
	ArrayList<DynamicExclusionListEntry> dynamicExclusionList;
	boolean onlyLipids;
	MethodParameterSet methodParameters;

	//Constructor
	public DataDependentAlgorithm(MethodParameterSet methodParameters, boolean onlyLipids)
	{
		//Initialize class variables
		this.methodParameters = methodParameters;
		this.onlyLipids = onlyLipids;
		dynamicExclusionList = new ArrayList<DynamicExclusionListEntry>();
	}

	//Method which returns the precursor ion fraction for an msms given an isolation width, a spectrum, and a target mz
	public double calculatePIF(Spectrum spectrum, Double mzTarget, Double isolationWidth, boolean includeIsotopes)
	{
		double pif = 100.0;
		ArrayList<Double> peakListDouble = new ArrayList<Double>();
		double intensitySum = 0.0;
		double targetIntensity = 0.0;
		FlatToppedGaussianModel ftgm;



		//Find all peaks within a given mz range
		for (int i=0; i<spectrum.mzArray.size(); i++)
		{
			for (int j=0; j<spectrum.mzArray.get(i).size(); j++)
			{
				if (Math.abs(spectrum.mzArray.get(i).get(j).mz-mzTarget)<6.0)
				{
					peakListDouble.add(spectrum.mzArray.get(i).get(j).mz);

					ftgm = new FlatToppedGaussianModel(spectrum.mzArray.get(i).get(j).mz, isolationWidth, spectrum.mzArray.get(i).get(j).intensity);

					intensitySum += ftgm.getCalculatedHeight(mzTarget);

					//If isotopic peaks count towards target intensity
					if (includeIsotopes)
					{
						//If spectrum comes from a peak and is the target OR the spectrum comes from noise and is the target
						if ((spectrum.mzArray.get(i).get(j).feature != null && spectrum.mzArray.get(i).get(j).feature.mz.equals(mzTarget))
								|| spectrum.mzArray.get(i).get(j).feature == null && spectrum.mzArray.get(i).get(j).mz.equals(mzTarget))
							targetIntensity += ftgm.getCalculatedHeight(mzTarget);
					}
					//If isotopic peaks do not count towards target intensity
					else
					{
						//If spectrum comes from a peak and is the target OR the spectrum comes from noise and is the target
						if ((spectrum.mzArray.get(i).get(j).feature != null && spectrum.mzArray.get(i).get(j).feature.mz.equals(mzTarget) && !spectrum.mzArray.get(i).get(j).isotope)
								|| spectrum.mzArray.get(i).get(j).feature == null && spectrum.mzArray.get(i).get(j).mz.equals(mzTarget))
							targetIntensity += ftgm.getCalculatedHeight(mzTarget);
					}

				}
			}
		}

		pif = targetIntensity/intensitySum;

		return pif;
	}

	//Calculate the difference between two masses in ppm
	public static  Double calcPPMDiff(Double mass1, Double mass2)
	{
		return (Math.abs(mass1 -  mass2)/(mass2))*1000000;
	}

	//Method which returns a signle MS/MS scan based on method details, supplied spectrum object, and a specific RT and parent precursor
	public MSMS generateSingleMSMSScan(Spectrum spectrum, Double actualTime, 
			Double injectionTime, Double precursor, String polarity, Double isolationWidth,
			boolean useMZXMLScan, double minMS2SN, Double minPIF, Double fragmentationEfficiency)
	{
		MSMS result = null;
		Peak chromatographicPeak = null;
		Double minPPMDiff = 999.0;

		//Find matching chromatographic peak
		for (int i=0; i<spectrum.mzArray.size(); i++)
		{
			for (int j=0; j<spectrum.mzArray.get(i).size(); j++)
			{
				//If same polarity
				if (spectrum.mzArray.get(i).get(j).polarity.equals(polarity))
				{
					//If closer to target mass
					if (Math.abs(calcPPMDiff(spectrum.mzArray.get(i).get(j).mz,precursor))<minPPMDiff && Math.abs(calcPPMDiff(spectrum.mzArray.get(i).get(j).mz,precursor)) < 20.0)
					{
						minPPMDiff = Math.abs(calcPPMDiff(spectrum.mzArray.get(i).get(j).mz,precursor));
						chromatographicPeak = spectrum.mzArray.get(i).get(j);
					}
				}
			}
		}

		//Add peak if chromatographic peak found
		if (chromatographicPeak != null)
		{
			result = new MSMS(actualTime, chromatographicPeak.depth, 
					chromatographicPeak.polarity, chromatographicPeak.feature, chromatographicPeak, 
					injectionTime, calculatePIF(spectrum, chromatographicPeak.mz, isolationWidth, true), 
					injectionTime, calculatePIF(spectrum, chromatographicPeak.mz, isolationWidth, false)
					, methodParameters, minMS2SN, minPIF);
		}

		return result;
	}


	//Method which returns array of MSMS scans based on method details, exclusion list, and supplied spectrum object
	public ArrayList<MSMS> generateDDMS2Scans(ArrayList<ExclusionListEntry> exclusionList, Spectrum spectrum, Double
			ppmDiff, ArrayList<Double> maxMSMSTimes, ArrayList<Double> minMSMSTimes, Double actualTime, Double injectionTime,
			int ms2AGCTarget, Double isolationWidth, int minAGCTarget,Double minMS2SN, Double minPIF, Double fragmentationEfficiency)
			{
		ArrayList<MSMS> result = new ArrayList<MSMS>();
		ArrayList<Peak> potentialPeaks = new ArrayList<Peak>();
		boolean potentialMS2 = true;
		Peak currentPeak;
		Peak actualPeak;
		double currentTimeDeviation = 0.0;
		int currentTopN;
		double minInjectionTime = 0.0;
		double itDifference;
		double actualInjectionTime;
		int i=0;

		//Calculate topN and ms2 interval
		if (spectrum.polarity.equals("+"))
		{
			currentTopN = methodParameters.posTopN;
		}
		else
		{
			currentTopN = methodParameters.negTopN;
		}

		//Purge dynamic exclusion list of outdated entries
		purgeDynamicExclusionList(actualTime);

		while (i<spectrum.intensitySortedPeakArray.size() && potentialPeaks.size()<currentTopN*2)
		{				
			//Assign to temp peak object
			currentPeak = spectrum.intensitySortedPeakArray.get(i);

			//Reset trigger boolean
			potentialMS2 = true;

			//Check if isotope
			if (methodParameters.excludeIsotopes && currentPeak.isotope)
			{
				potentialMS2 = false;
			}

			//Check if lipids
			if (onlyLipids && (currentPeak.feature == null || currentPeak.feature.id == null))
			{
				potentialMS2 = false;
			}

			//Check if on exclusion list
			if (onExclusionList(currentPeak, exclusionList, ppmDiff))
			{
				potentialMS2 = false;
			}

			//Check if on dynamic exclusion list
			if (onDynamicExclusionList(currentPeak, ppmDiff))
			{
				potentialMS2 = false;
			}

			//If retained, add to potential list
			if (potentialMS2)
			{
				potentialPeaks.add(currentPeak);
			}
			//Increment j
			i++;
		}

		//for top n, create and add MSMS entries for spectrum
		for (int k=0; k<currentTopN; k++)
		{
			if (potentialPeaks.size()>k)
			{
				//Calculate actual Peak
				if (potentialPeaks.get(k).feature != null)
				{
					//Create actual peak object at time of scan
					actualPeak = new Peak(potentialPeaks.get(k).feature.mz, 
							potentialPeaks.get(k).feature.gModel.getCalculatedHeight(maxMSMSTimes.get(k)-currentTimeDeviation), 
							(maxMSMSTimes.get(k)+minMSMSTimes.get(k))/2.0, potentialPeaks.get(k).polarity, potentialPeaks.get(k).isotope, 
							potentialPeaks.get(k).feature, potentialPeaks.get(k).charge);

					//Calculate injection time
					actualInjectionTime = calculateMSMSInjectionTime(ms2AGCTarget, potentialPeaks.get(k).feature.mz, 
							isolationWidth, potentialPeaks.get(k).intensity,potentialPeaks.get(k).charge)
							*calculatePIF(spectrum, actualPeak.mz, isolationWidth, false);

					//Calculate theoretical injection time
					minInjectionTime = calculateMSMSInjectionTime(minAGCTarget, potentialPeaks.get(k).feature.mz, 
							isolationWidth, potentialPeaks.get(k).intensity, potentialPeaks.get(k).charge)
							*calculatePIF(spectrum, actualPeak.mz, isolationWidth, false);
				}

				//Calculate injection time for all other msms
				else
				{
					actualPeak = potentialPeaks.get(k);


					//Calculate injection time
					actualInjectionTime = calculateMSMSInjectionTime(ms2AGCTarget, potentialPeaks.get(k).mz, 
							isolationWidth, potentialPeaks.get(k).intensity, potentialPeaks.get(k).charge)
							*calculatePIF(spectrum, actualPeak.mz, isolationWidth, false);

					//Calculate theoretical injection time
					minInjectionTime = calculateMSMSInjectionTime(minAGCTarget, potentialPeaks.get(k).mz, 
							isolationWidth, potentialPeaks.get(k).intensity, potentialPeaks.get(k).charge)
							*calculatePIF(spectrum, actualPeak.mz, isolationWidth, false);
				}

				//Calculate IT difference
				itDifference = injectionTime - actualInjectionTime;

				//If actual difference
				if (itDifference > 0)
				{
					currentTimeDeviation += (maxMSMSTimes.get(k) - getMax(maxMSMSTimes.get(k)-(itDifference/1000.0)/60.0,minMSMSTimes.get(k)));
				}
				else
				{
					actualInjectionTime = injectionTime;
				}

				//If theoretical injection time is below max injection time
				if (minInjectionTime<injectionTime)
				{
					//Add peaks
					result.add(new MSMS(maxMSMSTimes.get(k)-currentTimeDeviation, potentialPeaks.get(k).depth, 
							potentialPeaks.get(k).polarity, potentialPeaks.get(k).feature, potentialPeaks.get(k), 
							actualInjectionTime, calculatePIF(spectrum, potentialPeaks.get(k).mz, isolationWidth, true),
							calculateMSMSInjectionTime(ms2AGCTarget, potentialPeaks.get(k).mz, 
									isolationWidth, potentialPeaks.get(k).intensity, potentialPeaks.get(k).charge)
									*calculatePIF(spectrum, actualPeak.mz, isolationWidth, false), calculatePIF(spectrum, 
											potentialPeaks.get(k).mz, isolationWidth, false), methodParameters, minMS2SN, minPIF));

					//Add peak to dynamic exclusion list
					dynamicExclusionList.add(new DynamicExclusionListEntry(potentialPeaks.get(k).mz, 
							potentialPeaks.get(k).polarity, maxMSMSTimes.get(k)-currentTimeDeviation, maxMSMSTimes.get(k)-currentTimeDeviation+(methodParameters.dynamicExclusion/60.0)));

					//If exclude isotopes on and peak is noise peak, exclude 13C isotopes
					if (methodParameters.excludeIsotopes && potentialPeaks.get(k).feature == null)
					{
						//1x13C
						dynamicExclusionList.add(new DynamicExclusionListEntry(potentialPeaks.get(k).mz + 1.003335, 
								potentialPeaks.get(k).polarity, maxMSMSTimes.get(k)-currentTimeDeviation, maxMSMSTimes.get(k)-currentTimeDeviation+(methodParameters.dynamicExclusion/60.0)));

						//2x13C
						dynamicExclusionList.add(new DynamicExclusionListEntry(potentialPeaks.get(k).mz + 2.00671, 
								potentialPeaks.get(k).polarity, maxMSMSTimes.get(k)-currentTimeDeviation, maxMSMSTimes.get(k)-currentTimeDeviation+(methodParameters.dynamicExclusion/60.0)));

						//3x13C
						dynamicExclusionList.add(new DynamicExclusionListEntry(potentialPeaks.get(k).mz + 3.010065, 
								potentialPeaks.get(k).polarity, maxMSMSTimes.get(k)-currentTimeDeviation, maxMSMSTimes.get(k)-currentTimeDeviation+(methodParameters.dynamicExclusion/60.0)));
					}
				}
			}
		}

		return result;
			}

	//Method returns a theoretical injection time given agc target, m/z, IW, and precursor ion intensity
	public double calculateMSMSInjectionTime(int agcTarget, Double mz, Double isolationWidth, Double intensity, int charge)
	{
		//Calculate efficiency parameters
		Double a = 0.00001;
		Double b = 0.000000133*mz+0.000841;
		Double c = 0.00057*mz+0.671;
		Double d = 0.0017*mz+1.3725;

		//Calculate efficiency
		Double efficiency = (a-b)/(1+Math.pow(isolationWidth/c,d)) + b;

		//Calculate injection time
		Double injectionTime = agcTarget/(intensity*efficiency);

		return injectionTime/charge;
	}

	//Method returns true iff a peak is on exclusion list
	public boolean onExclusionList(Peak p, ArrayList<ExclusionListEntry> exclusionList, Double ppmDiff)
	{
		for (int i=0; i<exclusionList.size(); i++)
		{
			if (exclusionList.get(i).isOnExclusionList(p, ppmDiff))
			{
				return true;
			}				
		}

		return false;
	}

	//Method to purge dynamic exclusion list of outdated entries
	public void purgeDynamicExclusionList(Double endTime)
	{
		for (int i=0; i<dynamicExclusionList.size(); i++)
		{
			if (endTime > dynamicExclusionList.get(i).endTime)
			{
				dynamicExclusionList.remove(i);
				i--;
			}				
		}
	}

	//Method returns true iff a peak is on dynamic exclusion list
	public boolean onDynamicExclusionList(Peak p, Double ppmDiff)
	{
		for (int i=0; i<dynamicExclusionList.size(); i++)
		{
			if (dynamicExclusionList.get(i).isOnExclusionList(p, ppmDiff))
			{
				return true;
			}				
		}

		return false;
	}

	//Return maximum of two numbers
	public static Double getMax(Double a, Double b)
	{
		if (a>b)
			return a;
		else
			return b;
	}
}

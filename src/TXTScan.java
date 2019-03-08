

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;

public class TXTScan {

	Double precursor;				//Precursor mass of isolated ion
	String file;					//Filename
	int scanNum;					//Number of scan
	boolean centroided;				//Boolean if data is centroided
	int msLevel;					//Integer of ms level
	String polarity;				//polarity of scan
	Double retentionTime;			//retention time of scan in seconds
	Double basePeakMZ;				//Mass to charge of most intense peak
	Double injectionTime;			//Injection time in ms
	ArrayList<ScanPeak> peakArray;	//Peak array storing mz and intensity info
	Double precursorIonFraction;	//Precursor ion fraction
	Double isolationWidth;			//Isolation width in Th
	Double parentIonIntensity;		//Intensity of parent ion selected for MS/MS
	boolean simScan;				//True iff scan is a SIM scan
	Double sn;						//Signal-to-noise ratio of MS/MS
	Double noise;					//Noise level of ms/ms


	//Constructor
	public TXTScan(int scanNum, boolean centroided, int msLevel, String polarity, 
			Double retentionTime, Double basePeakMZ, String file, Double precursor,
			String mzArray, String intArray, Double injectionTime, Double isolationWidth)
	{
		//Initialize variables
		peakArray = new ArrayList<ScanPeak>();
		this.scanNum = scanNum;
		this.centroided = centroided;
		this.msLevel = msLevel;
		this.polarity = polarity;
		this.retentionTime = retentionTime;
		this.basePeakMZ = basePeakMZ;
		this.precursor = precursor;
		this.file = file;
		this.injectionTime = injectionTime;
		this.precursorIonFraction = 0.0;
		this.isolationWidth = isolationWidth;
		parentIonIntensity = 0.0;
		simScan = false;

		//Parse bite array into m/z and intensity info
		parseMZArray(mzArray, intArray);
		
		//Calculate PIF
		calcPIF(40.0);
	}

	//Calculate sn
	public void calcSN()
	{
		//Sort by intensity, highest to lowest
		Collections.sort(peakArray);
		
		if (peakArray.size() > 0)
		{
			this.sn = peakArray.get(0).intensity/peakArray.get(peakArray.size()-1).intensity;
			this.noise = peakArray.get(peakArray.size()-1).intensity;
		}
		else
		{
			this.sn = 0.0;
			this.noise = 0.0;
		}
	}
	
	//Calculate precursor ion fraction if ms2
	public void calcPIF(Double ppmTol)
	{
		Double totalInt = 0.0;
		Double targetInt = 0.0;

		if (precursor>0.0)
		{
			for (int i=0; i<peakArray.size(); i++)
			{
				Double ppmDiff1 = (Math.abs(peakArray.get(i).mz-(precursor))/precursor)*1000000.0;
				
				totalInt += peakArray.get(i).intensity;
				
				if (ppmDiff1<ppmTol)
				{

					targetInt += peakArray.get(i).intensity;
				}
			}
			
			if (peakArray.size()>0)
				precursorIonFraction = targetInt/totalInt;
			else
				precursorIonFraction = 0.0;
		}

	}

	//Parse m/z array
	public void parseMZArray(String mzString, String intString)
	{
		String[] mzArray = mzString.substring(mzString.indexOf("]")+2).split(" ");
		String[] intArray = intString.substring(intString.indexOf("]")+2).split(" ");
		
		for (int i=0; i<intArray.length; i++)
		{
			if (i<mzArray.length)
			{
				Double mz = Double.valueOf(mzArray[i]);
				Double intensity = Double.valueOf(intArray[i]);
				peakArray.add(new ScanPeak(mz, intensity));
			}
		}
	}
	
	//String reperesentation of scan metadata
	public String toString()
	{
		String result = "";
		
		result += scanNum + ",";	
		result += retentionTime/60.0 + ",";	
		
		if (simScan)
			result += "SIM" + ",";
		else
			result += msLevel + ",";	
		
		result += polarity + ",";		
		result += precursor + ",";
		result += parentIonIntensity + ",";
		result += isolationWidth + ",";
		result += injectionTime + ",";			
		result += precursorIonFraction + ",";	
		result += basePeakMZ + ",";	
		result += sn + ",";
		result += noise + ",";
		
		return result;
	}
}

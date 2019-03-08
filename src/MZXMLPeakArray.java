import java.util.ArrayList;
import java.util.Collections;


public class MZXMLPeakArray {


	private  int[] countBin;			//Array which holds the number of spectra in each bin
	private  int[] addedBin; 			//Array storing the number of spectra added
	private  MZXMLScan[][] rtBin; 		//2D array to store spectra objects
	private  Double maxRT;				//Highest retention time in minutes
	private  Double minRT;				//Lowest retention time in minutes
	private Double rtBinSize;			//RT width of bins in minutes


	//Constructor
	public MZXMLPeakArray(Double minRT, Double maxRT, Double rtBinSize)
	{
		//Initialize Variables
		this.maxRT = maxRT;
		this.minRT = minRT;
		this.rtBinSize = rtBinSize;

		//Create array
		createBins(calcArraySize(this.rtBinSize, this.minRT, this.maxRT));
	}

	//A method to bin all scans by retention time
	public void binScans(ArrayList<MZXMLScan> scans)
	{
		//Check if any spectra have been created
		if (scans.size()>0)
		{
			//Populate count array to correctly initialize array size for positive library spectra
			for (int i=0; i<scans.size(); i++)
			{
				countBin[findBinIndex(scans.get(i).retentionTime,rtBinSize,minRT)] ++;
			}

			//Use count bin to initialize new arrays to place spectra into hash table
			for (int i=0; i<countBin.length; i++)
			{
				rtBin[i] = new MZXMLScan[countBin[i]];
			}


			//Populate spectrum arrays for  spectra
			for (int i=0; i<scans.size(); i++)
			{
				rtBin[findBinIndex(scans.get(i).retentionTime,rtBinSize,minRT)]
						[addedBin[findBinIndex(scans.get(i).retentionTime,rtBinSize,minRT)]] = scans.get(i);
				addedBin[findBinIndex(scans.get(i).retentionTime,rtBinSize,minRT)]++;
			}
		}
	}

	//Method which returns an array of MZXML Scans within a given polarity and retention time tolerance
	public ArrayList<MZXMLScan> getMZXMLScanPair(Double rt, String polarity)
	{
		//Generate array of scans in time area
		ArrayList<MZXMLScan> tempArray =  getMZXMLScans(rt-0.2, rt+0.2, polarity);
		ArrayList<MZXMLScan> resultArray = new ArrayList<MZXMLScan>();

		//Sort temp array by time
		Collections.sort(tempArray);
		
		//Find two closest
		for (int i=0; i<tempArray.size()-1; i++)
		{
			
			//If before first scan
			if (rt<tempArray.get(0).retentionTime)
			{
				resultArray.add(tempArray.get(i));
				break;
			}
			
			//Else
			if (rt-tempArray.get(i).retentionTime > 0 
					&& rt-tempArray.get(i+1).retentionTime < 0)
			{
				resultArray.add(tempArray.get(i));
				resultArray.add(tempArray.get(i+1));
				break;
			}
		}

		return resultArray;
	}

	//Method which returns an array of MZXML Scans within a given polarity and retention time tolerance
	public ArrayList<MZXMLScan> getMZXMLScans(Double minScanRT, Double maxScanRT, String polarity)
	{
		ArrayList<MZXMLScan> resultArray = new ArrayList<MZXMLScan>();

		// Find range of rt bins which need to be searched
		int minIndex = findBinIndex(minScanRT,rtBinSize,minRT);
		int maxIndex = findBinIndex(maxScanRT,rtBinSize,minRT);

		//Modify bins if they exceed array bounds
		if (minIndex<0) minIndex = 0;
		if (maxIndex>(rtBin.length-1)) maxIndex = rtBin.length-1;

		//Iterate through this rt bin range
		for (int i=minIndex; i<=maxIndex; i++)
		{
			//If the bin contains library spectra
			if (countBin[i]>0)
			{
				//For all spectra which are in the same mass bin
				for (int j=0; j<addedBin[i]; j++)
				{
					//If the correct polarity
					if (rtBin[i][j].polarity.equals(polarity))
						resultArray.add(rtBin[i][j]);
				}
			}
		}

		return resultArray;
	}


	/*
	 * Create array of bins in which to store objects
	 * Bins are indexed by retention time
	 */
	public  void createBins(int arraySize)
	{
		countBin = new int[arraySize]; //Array to store number of positive spectra stored in each bin
		addedBin = new int[arraySize]; //Array to store number of positive spectra stored in each bin

		//Fill all positive bins with zeroes
		for (int i=0; i<countBin.length; i++)
		{
			countBin[i] = 0;
			addedBin[i] = 0;
		}

		//Create the array to store the actual spectra objects
		rtBin = new MZXMLScan[arraySize][];
	}

	//Convert a precursor mass to the correct bin number
	public  int findBinIndex(Double rt, Double binSize, Double minRT)
	{
		return (int)((rt-minRT)/binSize);
	}

	//Calculate the correct array size based on a mass range
	public  int calcArraySize(Double binSize, Double minRT, Double maxRT)
	{
		return (int)((maxRT - minRT)/binSize)+1;
	}
}

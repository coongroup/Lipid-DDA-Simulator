import java.util.ArrayList;
import java.util.Collections;


public class FeatureArray {


	public  int[] countBin;			//Array which holds the number of spectra in each bin
	public  int[] addedBin; 			//Array storing the number of spectra added
	public  Feature[][] rtBin; 		//2D array to store feature objects
	public  Double maxRT;				//Highest retention time in minutes
	public  Double minRT;				//Lowest retention time in minutes
	public Double rtBinSize;			//RT width of bins in minutes


	//Constructor
	public FeatureArray(Double minRT, Double maxRT, Double rtBinSize)
	{
		//Initialize Variables
		this.maxRT = maxRT;
		this.minRT = minRT;
		this.rtBinSize = rtBinSize;

		//Create array
		createBins(calcArraySize(this.rtBinSize, this.minRT, this.maxRT));
	}

	//A method to bin all scans by retention time
	public void binScans(ArrayList<Feature> features)
	{
		//Check if any spectra have been created
		if (features.size()>0)
		{
			//Populate count array to correctly initialize array size for positive library spectra
			for (int i=0; i<features.size(); i++)
			{
				countBin[findBinIndex(features.get(i).apexRT,rtBinSize,minRT)] ++;
			}

			//Use count bin to initialize new arrays to place spectra into hash table
			for (int i=0; i<countBin.length; i++)
			{
				rtBin[i] = new Feature[countBin[i]];
			}


			//Populate spectrum arrays for  spectra
			for (int i=0; i<features.size(); i++)
			{
				rtBin[findBinIndex(features.get(i).apexRT,rtBinSize,minRT)]
						[addedBin[findBinIndex(features.get(i).apexRT,rtBinSize,minRT)]] = features.get(i);
				addedBin[findBinIndex(features.get(i).apexRT,rtBinSize,minRT)]++;
			}
		}
	}

	//Method which returns an array of features within a given polarity and retention time tolerance
	public ArrayList<Feature> getFeatures(Double minRT, Double maxRT, String polarity)
	{
		ArrayList<Feature> resultArray = new ArrayList<Feature>();

		// Find range of rt bins which need to be searched
		int minIndex = findBinIndex(minRT,rtBinSize,this.minRT);
		int maxIndex = findBinIndex(maxRT,rtBinSize,this.minRT);

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
		rtBin = new Feature[arraySize][];
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

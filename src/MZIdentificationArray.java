import java.util.ArrayList;
import java.util.Collections;


public class MZIdentificationArray {


	public  int[] countBin;						//Array which holds the number of spectra in each bin
	public  int[] addedBin; 					//Array storing the number of spectra added
	public  MZIdentification[][] mzBin; 		//2D array to store feature objects
	public  Double maxMZ;						//Highest mz value
	public  Double minMZ;						//Lowest mz value
	public Double mzBinSize;					//mz width of bins


	//Constructor
	public MZIdentificationArray(Double minMZ, Double maxMZ, Double mzBinSize)
	{
		//Initialize Variables
		this.maxMZ = maxMZ;
		this.minMZ = minMZ;
		this.mzBinSize = mzBinSize;

		//Create array
		createBins(calcArraySize(this.mzBinSize, this.minMZ, this.maxMZ));
	}

	//A method to bin all scans by retention time
	public void binScans(ArrayList<MZIdentification> identifications)
	{
		//Check if any spectra have been created
		if (identifications.size()>0)
		{
			//Populate count array to correctly initialize array size for positive library spectra
			for (int i=0; i<identifications.size(); i++)
			{
				countBin[findBinIndex(identifications.get(i).precursor,mzBinSize,minMZ)] ++;
			}

			//Use count bin to initialize new arrays to place spectra into hash table
			for (int i=0; i<countBin.length; i++)
			{
				mzBin[i] = new MZIdentification[countBin[i]];
			}


			//Populate spectrum arrays for  spectra
			for (int i=0; i<identifications.size(); i++)
			{
				mzBin[findBinIndex(identifications.get(i).precursor,mzBinSize,minMZ)]
						[addedBin[findBinIndex(identifications.get(i).precursor,mzBinSize,minMZ)]] = identifications.get(i);
				addedBin[findBinIndex(identifications.get(i).precursor,mzBinSize,minMZ)]++;
			}
		}
	}

	//Method which returns an array of features within a given polarity and retention time tolerance
	public ArrayList<MZIdentification> getIdentifications(Double minMZ, Double maxMZ, String polarity)
	{
		ArrayList<MZIdentification> resultArray = new ArrayList<MZIdentification>();

		// Find range of rt bins which need to be searched
		int minIndex = findBinIndex(minMZ,mzBinSize,this.minMZ);
		int maxIndex = findBinIndex(maxMZ,mzBinSize,this.minMZ);

		//Modify bins if they exceed array bounds
		if (minIndex<0) minIndex = 0;
		if (maxIndex>(mzBin.length-1)) maxIndex = mzBin.length-1;

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
					if (mzBin[i][j].polarity.equals(polarity))
						resultArray.add(mzBin[i][j]);
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
		mzBin = new MZIdentification[arraySize][];
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

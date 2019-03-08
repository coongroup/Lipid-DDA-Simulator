

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;

public class MZXMLScan implements Comparable<MZXMLScan>{

	Double precursor;				//Precursor mass
	String file;					//Filename
	int scanNum;					//Number of scan
	boolean centroided;				//Boolean if data is centroided
	int msLevel;					//Intever of ms level
	String polarity;				//polarity of scan
	Double retentionTime;			//retention time of scan in seconds
	Double basePeakMZ;				//Mass to charge of most intense peak
	int precision;					//precision used for mz array
	String byteOrder;				//byte order for mzArray
	String mzArray;					//Encoded array
	ArrayList<ScanPeak> peakArray;	//MZXML peak array storing mz and intensity info


	//Constructor
	public MZXMLScan(int scanNum, boolean centroided, int msLevel, String polarity, 
			Double retentionTime, Double basePeakMZ, int precision, String byteOrder, 
			String file, Double precursor, String mzArray)
	{
		//Initialize variables
		peakArray = new ArrayList<ScanPeak>();
		this.scanNum = scanNum;
		this.centroided = centroided;
		this.msLevel = msLevel;
		this.polarity = polarity;
		this.retentionTime = retentionTime;
		this.basePeakMZ = basePeakMZ;
		this.precision = precision;
		this.byteOrder = byteOrder;
		this.precursor = precursor;
		this.file = file;
		this.mzArray = mzArray;
		
		//Parse bite array into m/z and intensity info
		parseMZArray();
		
		//Sort array by mz
		Collections.sort(peakArray, new MZComparator());
	}
	
	//Method to return intensity of m/z value within ppm tol using binary search
	public double getIntensity(Double mz, Double ppmTol)
	{
		int index = binarySearch(0, peakArray.size()-1, mz, ppmTol);
		
		if (index > -1)
		{
			//mark as feature associated
			peakArray.get(index).featureAssociated = true;
			return peakArray.get(index).intensity;
		}
		else
			return 0.0;
	}
	
	// Returns index of x if it is present in arr[l.. 
    // r], else return -1 
    public int binarySearch(int l, int r, double mz, double ppmTol) 
    { 
        if (r>=l) 
        { 
            int mid = l + (r - l)/2; 
  
            // If the element is present at the  
            // middle itself 
            if (isMZ(peakArray.get(mid).mz, mz, ppmTol)) 
               return mid; 
  
            // If element is smaller than mid, then  
            // it can only be present in left subarray 
            if (peakArray.get(mid).mz > mz) 
               return binarySearch(l, mid-1, mz, ppmTol); 
  
            // Else the element can only be present 
            // in right subarray 
            return binarySearch(mid+1, r, mz, ppmTol); 
        } 
  
        // We reach here when element is not present 
        //  in array 
        return -1; 
    } 
    
    //Returns true iff the mz is within ppm tolerance of target mz
    public boolean isMZ(Double mz, Double targetMZ, Double ppmTol)
    {
    	return ((Math.abs(mz-targetMZ)/targetMZ)*1000000.0<ppmTol);
    }
    
	
	//Parse m/z array using byte buffer
	public void parseMZArray()
	{
		double[] values;
		byte[] decoded = Base64.getDecoder().decode(mzArray);
		ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
		byteBuffer.order(ByteOrder.BIG_ENDIAN);
		

		values = new double[byteBuffer.asDoubleBuffer().capacity()];
		byteBuffer.asDoubleBuffer().get(values);
		
		for (int peakIndex = 0; peakIndex < values.length - 1; peakIndex += 2)
		{
			Double mz = values[peakIndex];
			Double intensity = values[peakIndex + 1];
			
			peakArray.add(new ScanPeak(mz, intensity));
		}
	}
	
	public class MZComparator implements Comparator<ScanPeak>
	{
		 public int compare(ScanPeak s1, ScanPeak s2)
	     {
	         return s1.mz.compareTo(s2.mz);
	     }
	}
	
	public String toString()
	{
		return retentionTime+" "+polarity;
	}
	
	public int compareTo(MZXMLScan m)
	{
		return this.retentionTime.compareTo(m.retentionTime);
	}
}


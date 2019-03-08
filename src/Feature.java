import java.util.ArrayList;


public class Feature implements Comparable<Feature>
{
	String polarity;
	Double mz;
	Double apexRT;
	Double fwhm;
	Double area;
	GaussianModel gModel;
	Double minRT;
	Double maxRT;
	int charge;
	Identification id;
	MZIdentification mzID;
	boolean sampled;
	boolean identified;
	ArrayList<MSMS> msmsArray;
	
	public Feature(String polarity, Double mz, Double apexRT, Double fwhm, Double area, int charge)
	{
		//Initialize variables
		this.polarity = polarity;
		this.mz = mz;
		this.apexRT = apexRT;
		this.fwhm = fwhm;
		this.area = area;
		this.sampled = false;
		this.identified = false;
		this.charge = charge;
		id = null;
		msmsArray = new ArrayList<MSMS>();
		
		//Create gaussian model
		gModel = new GaussianModel(fwhm,area, null, apexRT);
		
		//Populate min and max RT
		minRT = apexRT - fwhm*6.0;
		
		//Populate min and max RT
		maxRT = apexRT + fwhm*6.0;
	}
	
	//Sort by retention time, lowest to highest
	public int compareTo(Feature f)
	{
		return this.apexRT.compareTo(f.apexRT);
	}
	
	public boolean isIdentified(int key)
	{
		for (int i=0; i<msmsArray.size(); i++)
		{
			if (msmsArray.get(i).isSuccesful(key))
				return true;
		}
		
		return false;
	}
	
	public void checkIdentification()
	{
		for (int i=0; i<msmsArray.size(); i++)
		{
			if (msmsArray.get(i).successful)
				this.identified = true;
		}
	}
	
	public String toString()
	{
		checkIdentification();
		
		String result = "";
		result += polarity+",";
		result += apexRT+",";
		result += mz+",";
		result += area+",";
		
		if (id != null)
			result += id.identification+",";
		else
			result += ",";
		
		if (mzID != null)
			result += mzID.identification+" "+mzID.rtIndex+",";
		else
			result += ",";
		
		result += sampled+",";
		result += identified+",";
		
		if (id != null)
			result += id.redundant+",";
		else
			result += ",";
		
		return result;
	}
}

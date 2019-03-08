
public class ScanPeak implements Comparable<ScanPeak>
{
	Double mz;
	Double intensity;
	boolean featureAssociated;
	
	public ScanPeak(Double mz, Double intensity)
	{
		this.mz = mz;
		this.intensity = intensity;
		featureAssociated = false;
	}
	
	public String toString()
	{
		return mz+", "+intensity;
	}
	
	public Peak toPeak(Double time, String polarity)
	{
		Peak result = new Peak(mz, intensity, time, polarity, false, null, 1);
		
		return result;
	}
	
	public int compareTo(ScanPeak s)
	{
		if (s.intensity>this.intensity)
			return 1;
		else if (s.intensity<this.intensity)
			return -1;
		else return 0;
	}
}

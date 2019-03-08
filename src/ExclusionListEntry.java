
public class ExclusionListEntry 
{
	Double mass;
	String polarity;
	Double startTime;
	Double endTime;
	
	public ExclusionListEntry(Double mass, String polarity, Double startTime, Double endTime)
	{
		this.mass = mass;
		this.polarity = polarity;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	//Returns true iff a peak shoudl be excluded from analysis
	public boolean isOnExclusionList(Peak p, Double ppmDiff)
	{
		if (calcPPMDiff(p.mz,this.mass) < ppmDiff 
				&& p.time > startTime 
				&& p.time < endTime
				&& p.polarity.equals(this.polarity))
			return true;
		
		return false;
	}
	
	//Calculate the difference between two masses in ppm
	public  Double calcPPMDiff(Double mass1, Double mass2)
	{
		return (Math.abs(mass1 -  mass2)/(mass2))*1000000;
	}
}

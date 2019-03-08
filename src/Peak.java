
public class Peak implements Comparable<Peak>
{
	Double mz;
	Double intensity;
	Double time;
	String polarity;
	Feature feature = null;
	int depth;
	boolean isotope;
	int charge;
	
	public Peak (Double mz, Double intensity, Double time, String polarity, boolean isotope, Feature feature, int charge)
	{
		this.mz = mz;
		this.intensity = intensity;
		this.time = time;
		this.polarity = polarity;
		this.isotope = isotope;
		this.feature = feature;
		this.charge = charge;
	}
	
	public String toString()
	{
		if (feature != null)
			return (mz+","+intensity+" *");
		else
			return (mz+","+intensity);
	}
	
	public int compareTo(Peak p)
	{
		if (p.intensity > this.intensity)
			return 1;
		else if (p.intensity < this.intensity)
			return -1;
		else return 0;
	}
}


public class Noise 
{
	Double retention;
	Double mz;
	Double intensity;
	String polarity;
	int charge;
	
	public Noise(Double retention, Double mz, Double intensity, String polarity)
	{
		this.charge = 1;
		this.retention = retention;
		this.mz = mz;
		this.intensity = intensity;
		this.polarity = polarity;
	}
}


public class Identification 
{
	Double retention;
	Double quantIon;
	String polarity;
	String identification;
	boolean redundant;
	
	public Identification(Double retention, Double quantIon, String polarity, String identification, boolean redundant)
	{
		this.retention = retention;
		this.quantIon = quantIon;
		this.polarity = polarity;
		this.identification = identification;
		this.redundant = redundant;
	}
}

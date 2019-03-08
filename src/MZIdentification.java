
public class MZIdentification 
{
	String identification;
	String lipidClass;
	String polarity;
	String adduct;
	Double precursor;
	String formula;
	int carbonNumber;
	int dbNumber;
	double rtIndex;
	
	public MZIdentification(String identification, String lipidClass, String polarity, 
			String adduct, Double precursor, String formula)
	{
		this.identification = identification;
		this.lipidClass = lipidClass;
		this.polarity = polarity;
		this.adduct = adduct;
		this.precursor = precursor;
		this.formula = formula;
		
		calcCarbonDBNumber();
	}
	
	public void calcCarbonDBNumber()
	{
		String[] split = identification.split(" ");
		String[] faSplit;
	
		//Split lipid fatty acids
		split = split[1].split("_");

		//For each fatty acid
		for (int i=0; i<split.length; i++)
		{
			faSplit = split[i].split(":");

			if (!faSplit[0].replaceAll("[0-9]", "").equals(""))
			{
				
			}

			//Update total carbon and db number
			carbonNumber += Integer.valueOf(faSplit[0].replaceAll("[^\\d.]", ""));
			dbNumber += Integer.valueOf(faSplit[1].replaceAll("[^\\d.]", ""));
		}
		
		rtIndex = ((double)carbonNumber)-((double)2*dbNumber);
		
	}
}

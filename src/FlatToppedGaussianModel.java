
import java.util.ArrayList;


public class FlatToppedGaussianModel 
{
	Double isolationWidth;	//Isolatino width in Th
	Double height;			//Peak height
	Double targetMZ;		//isolated m/z
	Double aTerm;			//A term of flat-topped gaussian equation
	Double bTerm;			//B term of flat-topped gaussian equation
	Double mzOffset;		//m/z offset for quad isolation peak
	ArrayList<Double> x;	//ArrayList of x coordinates (mz)
	ArrayList<Double> y;	//ArrayList of y coordinates (signal, normalize to 1)
	Double trapezoidSum;	//Sum of areas using trapezoid methof

	//Constructor for compound discoverer gaussian fitting
	public FlatToppedGaussianModel(Double targetMZ, Double isolationWidth, Double height)
	{
		//Initialize variables
		this.isolationWidth = isolationWidth;
		this.height = height;
		this.targetMZ = targetMZ;
		this.height = height;
		x = new ArrayList<Double>();
		y= new ArrayList<Double>();
		trapezoidSum = 0.0;

		//Calculate a and b term and m/z offset
		aTerm = (0.0031*targetMZ + 9.0691)*(Math.pow(Math.E, -1.271*isolationWidth));
		
		bTerm = 11.0*(Math.pow(Math.E, -1.271*isolationWidth));
		
		mzOffset = 0.0000832*isolationWidth*targetMZ;
		
		
		//Calculate gaussian profile for compound discoverer
			//Create time array
			populateArrays();

			//Integrate gaussian function
			integrateFunction();
	}
	
	//Populate mz and intensity arrays
	private void populateArrays()
	{
		double mz = 0-isolationWidth*2.0;

		while (mz<(isolationWidth*2.0))
		{
			x.add(mz+mzOffset);
			y.add(calcIntensity(mz));
			mz = mz+(isolationWidth/12.0);
		}
	}

	//Returns intensity of isolation peak at a given m/z value
	private Double calcIntensity(Double mz)
	{
		return Math.pow(Math.E, -aTerm*Math.pow(mz, 2)-bTerm*(Math.pow(mz, 4)));
	}
	
	
	//Integrate gaussian function using trapezoid method
	private void integrateFunction()
	{
		for (int i=0; i<x.size()-1; i++)
		{
			trapezoidSum += ((y.get(i)+y.get(i+1))/2.0)*(x.get(i+1)-x.get(i)); 
		}
	}


	//Returns the actual peak height at any timepoint along gaussian profile
	public Double getCalculatedHeight(Double mz)
	{
		//Convert mz to normalized mz
		Double normalizedMZ = mz-this.targetMZ;
		
		//Find closest 
		for (int i=0; i<x.size()-1; i++)
		{
			if (x.get(i)<normalizedMZ && x.get(i+1)>normalizedMZ)
			{
				if (Math.abs(normalizedMZ-x.get(i)) > Math.abs(normalizedMZ-x.get(i+1))) 
					return (height*y.get(i+1));
				else 
					return (height*y.get(i));
			}
		}

		return 0.001;
	}
	
	public String toString()
	{
		String result = "";
		
		for (int i=0; i<x.size(); i++)
		{
			result += x.get(i)+"	"+y.get(i)+"\n";
		}
		
		return result;
	}
}



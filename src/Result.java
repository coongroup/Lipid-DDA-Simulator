import java.text.SimpleDateFormat;
import java.util.Date;


public class Result 
{
	int numMSMS;
	int numSpectralMatches;
	int numFeatureIDs;
	int numLipidIDs;
	int experimentNumber;
	long executionTime;
	
	public Result(int numMSMS, int numSpectralMatches, int numFeatureIDs, int numLipidIDs, int experimentNumber)
	{
		this.numMSMS = numMSMS;
		this.numSpectralMatches = numSpectralMatches;
		this.numFeatureIDs = numFeatureIDs;
		this.numLipidIDs = numLipidIDs;
		this.experimentNumber = experimentNumber;
		executionTime = 0;
	}
	
	public Object[] toObjectArray()
	{
		Object[] result = new Object[5];
		
		result[0] = new SimpleDateFormat("HH:mm:ss").format(new Date());
		result[1] = numMSMS;
		result[2] = numSpectralMatches;
		result[3] = numFeatureIDs;
		result[4] = numLipidIDs;
		
		return result;
	}
}

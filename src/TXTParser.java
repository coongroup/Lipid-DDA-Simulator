

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.xml.sax.helpers.DefaultHandler;

public class TXTParser extends DefaultHandler {

	public ArrayList<TXTScan> scanList = new ArrayList<TXTScan>();

	//Constructor
	public TXTParser()
	{

	}

	//Method for writing spectral metadata to a file
	public void writeSpectra(String filename, boolean msmsOnly) throws FileNotFoundException
	{
		PrintWriter pw = new PrintWriter(filename);

		pw.println("Scan Number,Retention Time (min),MS Level, Polarity, Precursor m/z,"
				+ "Parent Ion Intensity, Isolation Width (Th),Injection Time (ms), PIF, "
				+ "Base Peak m/z,SN,Noise");

		for (int i=0; i<scanList.size(); i++)
		{
			if (msmsOnly & scanList.get(i).msLevel>1)
				pw.println(scanList.get(i));
			else if (!msmsOnly)
				pw.println(scanList.get(i));
		}

		pw.close();
	}

	//Method returns intensity of desired m/z in spectrum
	public double getIntensity(Double mz, Double ppm, TXTScan scan)
	{
		Double result = 0.0;

		if (scan != null)
		{
			for (int i=0; i<scan.peakArray.size(); i++)
			{
				Double ppmDiff = (Math.abs(scan.peakArray.get(i).mz-mz)/mz)*1000000.0;
				if (ppmDiff<ppm)
					return scan.peakArray.get(i).intensity;
			}
		}

		return result;
	}

	//Method for parsing TXT files and extracting spectral data
	public void readFile(String filepath) throws IOException
	{
		scanList.clear();

		TXTScan scan;					//Temp scan object
		String line = "";				//String holding currently read line
		int scanNum = 0;				//Number of scan;
		boolean centroided = false;		//Boolean if data is centroided
		int msLevel = -1;				//Intever of ms level
		String polarity = "";			//polarity of scan
		Double retentionTime = 0.0;		//retention time of scan in seconds
		Double basePeakMZ = 0.0;		//Mass to charge of most intense peak
		Double precursor = 0.0;			//Precursor mass if MS2
		Double injectionTime = 0.0;		//Ion injection time in milliseconds
		String mzArray = "";			//Mz array as string
		String intArray = "";			//Int array as string
		boolean scanStart = false;		//True iff a scan header is encountered
		boolean mzArrayStart = false;	//True iff mz Array is on next line
		boolean intArrayStart = false;	//True iff int Array is on next line
		boolean chromatogram = false;	//True iff the last scan has been reached
		boolean simScan = false;		//True iff a SIM scan is being read
		Double lowScanMZ = 0.0;			//lower mz bound of sim scan
		Double highScanMZ = 0.0;		//upper mz bound of sim scan
		Double isolationWidth = 0.0;	//Isolationwidth in Th
		Double parentIonIntensity = 0.0;//Parent Ion Intensity
		Double isolationWindowLow = 0.0;//Lower bound of isolation window
		Double isolationWindowHigh = 0.0;//Upper bound of isolation window
		TXTScan lastMS1 = null;			//Last read MS1

		BufferedReader reader = new BufferedReader(new FileReader(filepath));
		File file = new File(filepath);
		String filename = file.getName();

		//read line if not empty
		while ((line = reader.readLine()) != null)
		{
			//Parse scan number
			if (line.contains("index: "))
			{
				scanStart = true;
				scanNum = Integer.valueOf(line.substring(line.indexOf(":")+2));
			}

			//Parse ms level
			else if (line.contains("ms level") && scanStart)
				msLevel = Integer.valueOf(line.substring(line.indexOf(",")+2));

			//Parse ms level
			else if (line.contains("SIM ms") && scanStart)
			{
				msLevel = 2;
				simScan = true;
			}

			//Parse mz scan bound
			else if (line.contains("scan window lower limit") && scanStart && simScan)
				lowScanMZ = Double.valueOf(line.substring(line.indexOf(",")+2, line.lastIndexOf(",")-1));

			//Parse mz scan bound
			else if (line.contains("scan window upper limit") && scanStart && simScan)
			{
				highScanMZ = Double.valueOf(line.substring(line.indexOf(",")+2, line.lastIndexOf(",")-1));
				precursor = (lowScanMZ+highScanMZ)/2.0;
				isolationWidth = Math.round((highScanMZ - lowScanMZ)*10.0)/10.0;
			}

			//Parse isolation width
			else if (line.contains("isolation window lower offset") && scanStart && !simScan)
			{
				isolationWindowLow = Double.valueOf(line.substring(line.indexOf(",")+2, line.lastIndexOf(",")-1));
			}

			//Parse isolation width
			else if (line.contains("isolation window upper offset") && scanStart && !simScan)
			{
				isolationWindowHigh = Double.valueOf(line.substring(line.indexOf(",")+2, line.lastIndexOf(",")-1));
				isolationWidth = isolationWindowLow + isolationWindowHigh;
			}

			//parse polarity
			else if (line.contains("positive scan") && scanStart)
				polarity = "+";
			else if (line.contains("negative scan") && scanStart)
				polarity = "-";

			//Parse centroiding
			else if (line.contains("centroid spectrum") && scanStart)
				centroided = true;

			//Parse base peak
			else if (line.contains("base peak m/z") && scanStart)
				basePeakMZ = Double.valueOf(line.substring(line.indexOf("m/z, ")+5, line.lastIndexOf(",")-1));

			//Parse retention time
			else if (line.contains("scan start time") && scanStart)
				retentionTime = Double.valueOf(line.substring(line.indexOf("time, ")+6, line.lastIndexOf(",")-1))*60.0;

			//Parse peak intensity
			else if (line.contains("cvParam: peak intensity") && scanStart)
				parentIonIntensity = (double) Double.valueOf(line.substring(line.indexOf(",")+2, 
						line.lastIndexOf(","))).longValue();

			//Parse injection time
			else if (line.contains("ion injection time") && scanStart)
				injectionTime = Double.valueOf(line.substring(line.indexOf("time, ")+6, line.lastIndexOf(",")-1));

			//Parse target precursor
			else if (line.contains("selected ion m/z") && scanStart)
				precursor = Double.valueOf(line.substring(line.indexOf("m/z, ")+5, line.lastIndexOf(",")-1));

			//Parse target precursor
			else if (line.contains("selected ion m/z") && scanStart)
				precursor = Double.valueOf(line.substring(line.indexOf("m/z, ")+5, line.lastIndexOf(",")-1));

			//Parse start of mz Array
			else if (line.contains("m/z array") && scanStart)
			{
				mzArrayStart = true;
				intArrayStart = false;
			}

			//Parse start of int Array
			else if (line.contains("intensity array") && scanStart)
			{
				intArrayStart = true;
				mzArrayStart = false;
			}

			//Parse arrays and create spectrum
			else if (line.contains("binary: [") && scanStart)
			{
				if (mzArrayStart)
				{
					mzArray = line;
				}
				if (intArrayStart)
				{
					intArray = line;

					if (!chromatogram)
					{
						scan = new TXTScan(scanNum, centroided, msLevel, polarity, 
								retentionTime, basePeakMZ, filename, precursor,
								mzArray, intArray, injectionTime, isolationWidth);

						if (scan.msLevel==1)
							lastMS1 = scan;

						if (simScan)
							scan.simScan = true;

						if (scan.msLevel>1)
						{
							scan.parentIonIntensity = getIntensity(scan.precursor, 20.0, lastMS1);
							scan.calcSN();
							System.out.println(scan.retentionTime/60.0+" "+scan.precursorIonFraction);
						}

						scanList.add(scan);
					}
				}
			}

			//Parse end of spectrum
			if (line.contains("spectrum:"))
			{
				scanStart = false;
				mzArrayStart = false;
				intArrayStart = false;
				simScan = false;
				isolationWidth = 0.0;
				parentIonIntensity = 0.0;
			}

			//Parse chromatogram header
			if (line.contains("chromatogramList"))
				chromatogram = true;

		}

		reader.close();
	}
}

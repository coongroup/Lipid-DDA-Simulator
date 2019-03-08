import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.swing.JProgressBar;


public class Simulator 
{
	//Arrays
	ArrayList<Feature> potentialFeatures = new ArrayList<Feature>();
	ArrayList<Spectrum> posSpectra = new ArrayList<Spectrum>();
	ArrayList<Spectrum> negSpectra = new ArrayList<Spectrum>();
	ArrayList<ScanCycle> scanCycles = new ArrayList<ScanCycle>();
	ArrayList<ExclusionListEntry> exclusionList = new ArrayList<ExclusionListEntry>();
	ArrayList<Noise> noiseArray = new ArrayList<Noise>();
	ArrayList<Identification> identifications = new ArrayList<Identification>();

	//Objects
	DataDependentAlgorithm dda;
	TXTParser txtParser;
	MZIdentificationArray mzIDArray;
	MZXMLParser mzxmlParser;
	MZXMLPeakArray mzxmlPeakArray;
	FeatureArray featureArray;
	JProgressBar currentProgressBar;
	int progress = 0;
	Result simulationResult;
	long startTime;

	//MS Method Variables
	MethodParameterSet methodParameters;
	InstrumentParameterSet instrumentParameters;

	//Parameter from file simulation 
	public Simulator(MethodParameterSet mps, InstrumentParameterSet ips)
	{
		this.methodParameters = mps;
		instrumentParameters = ips;
	}

	public Result runSimulation(String exclusionListString, String mzxmlString, String negFeatureString,
			String posFeatureString, String lipidIDString, String posNoiseConstantString, String posNoiseRisingString,
			String negNoiseConstantString, String negNoiseRisingString,	String resultFolderString, boolean verbose,
			JProgressBar currentProgressBar) throws IOException	
	{		
		//Initialize Arrays
		potentialFeatures = new ArrayList<Feature>();
		posSpectra = new ArrayList<Spectrum>();
		negSpectra = new ArrayList<Spectrum>();
		scanCycles = new ArrayList<ScanCycle>();
		exclusionList = new ArrayList<ExclusionListEntry>();
		noiseArray = new ArrayList<Noise>();
		identifications = new ArrayList<Identification>();
		this.currentProgressBar = currentProgressBar;

		//Get startTime
		startTime = System.currentTimeMillis()/1000;

		//Check if mzxml is valid file
		if (mzxmlString.contains(".mzXML"))
			instrumentParameters.useMZXMLScan = true;
		else
			instrumentParameters.useMZXMLScan = false;

		//Read in exclusion list
		readExclusionList(exclusionListString);

		//Read in mxml files
		if (verbose)
			System.out.println("---Reading mzXML");
		if (instrumentParameters.useMZXMLScan)
		{
			System.out.println(instrumentParameters.useMZXMLScan);
			readMZXML(mzxmlString);
			mzxmlPeakArray = mzxmlParser.generateMZXMLPeakArray();
		}

		//Read in negative features
		if (verbose)
			System.out.println("---Reading Negative Features");
		readFeatures(negFeatureString, "-", true, false);

		//Read in positive features
		if (verbose)
			System.out.println("---Reading Positive Features");
		readFeatures(posFeatureString, "+", true, false);

		//Read in mz identifications
		if (instrumentParameters.precursorIDOnly)
		{
			readMZIdentifications("src/MZ_List.txt");
			associateMZIdentifications();
		}

		//Associate identifications
		if (!instrumentParameters.precursorIDOnly)
		{
			if (verbose)
				System.out.println("---Reading Identifications");
			readIdentifications(lipidIDString, instrumentParameters.ppmTolerance, 0.05);
		}

		//Create Feature Array
		if (verbose)
			System.out.println("---Creating Feature Dictionary");
		featureArray = generateFeatureArray(potentialFeatures);


		//Read in positive baseline chemical noise
		if (verbose)
			System.out.println("---Reading Chemical Noise");

		if (!instrumentParameters.useMZXMLScan)
		{
			readNoise(posNoiseConstantString, "+", true, null);
			//Read in negative baseline chemical noise
			readNoise(negNoiseConstantString, "-", true, null);

			//Read in positive gradient chemical noise
			readNoise(posNoiseRisingString, "+", false, 27.94);

			//Read in negative gradient chemical noise
			readNoise(negNoiseRisingString, "-", false, 27.90);
		}

		//Define data dependent algorithm
		if (verbose)
			System.out.println("---Creating Data Dependent Scans");
		dda = new DataDependentAlgorithm(methodParameters, instrumentParameters.onlyLipids);

		//Create scans
		if (verbose)
			System.out.println("---Creating Scans Iteratively");
		iterativeScanCreation(200.0, 1600.0, 200.0, 1200.0, true, instrumentParameters.useMZXMLScan);
		
		//Write dd scan stats
		if (verbose)
			System.out.println("---Writing Results");
		writeMSMSScanStats(resultFolderString+"MSMS_Stats.csv");
		writeFeatureStats(resultFolderString+"Feature_Stats.csv");	

		//Print stats
		printStats();

		return simulationResult;

	}

	//Print stats to console using single msms simulations
	public void printStats()
	{

		//Statistics
		int spectralMatches = 0;
		int featureIDs = 0;
		int uniqueLipids = 0;
		int totalSpectra = 0;

		//Iterate scan cycles
		for (int i=0; i<scanCycles.size(); i++)
		{
			//Iterate through msms scans
			for (int j=0; j<scanCycles.get(i).ms2.size(); j++)
			{
				totalSpectra ++;

				if (scanCycles.get(i).ms2.get(j).successful)
					spectralMatches ++;
			}
		}

		//Iterate through features
		for (int i=0; i<potentialFeatures.size(); i++)
		{
			if (potentialFeatures.get(i).identified)
			{
				featureIDs ++;
			}

			if (!instrumentParameters.precursorIDOnly && potentialFeatures.get(i).identified && !potentialFeatures.get(i).id.redundant)
			{
				uniqueLipids++;
			}

		}

		this.simulationResult = new Result(totalSpectra, spectralMatches, featureIDs, uniqueLipids, methodParameters.experimentNumber);
		this.simulationResult.executionTime = (System.currentTimeMillis()/1000)-this.startTime;
		System.out.println(totalSpectra+" "+spectralMatches+" "+featureIDs+" "+uniqueLipids);
	}

	//Print stats to console using multiple msms simulations
	public void printStatsMultipleMSMS()
	{
		//Statistics
		ArrayList<Integer> spectralMatchesArray = new ArrayList<Integer>();
		ArrayList<Integer> featureIDsArray = new ArrayList<Integer>();
		ArrayList<Integer> uniqueLipidsArray = new ArrayList<Integer>();
		ArrayList<Integer> totalSpectraArray = new ArrayList<Integer>();

		for (int i=0; i<50; i++)
		{
			int spectralMatches = 0;
			int featureIDs = 0;
			int uniqueLipids = 0;
			int totalSpectra = 0;

			//Iterate scan cycles
			for (int j=0; j<scanCycles.size(); j++)
			{
				//Iterate through msms scans
				for (int k=0; k<scanCycles.get(j).ms2.size(); k++)
				{
					totalSpectra ++;

					if (scanCycles.get(j).ms2.get(k).isSuccesful(i))
						spectralMatches ++;
				}
			}

			//Iterate through features
			for (int j=0; j<potentialFeatures.size(); j++)
			{
				if (potentialFeatures.get(j).isIdentified(i))
				{
					featureIDs ++;

					if (!instrumentParameters.precursorIDOnly && !potentialFeatures.get(j).id.redundant)
					{
						uniqueLipids++;
					}
				}
			}

			spectralMatchesArray.add(spectralMatches);
			featureIDsArray.add(featureIDs);
			uniqueLipidsArray.add(uniqueLipids);
			totalSpectraArray.add(totalSpectra);
		}

		this.simulationResult = new Result(findMedian(totalSpectraArray), findMedian(spectralMatchesArray), findMedian(featureIDsArray), findMedian(uniqueLipidsArray), methodParameters.experimentNumber);
		this.simulationResult.executionTime = (System.currentTimeMillis()/1000)-this.startTime;
	}

	//Iteratively creates scan cycles
	public void iterativeScanCreation(Double minNegMass, Double maxNegMass, Double minPosMass, Double maxPosMass, boolean addIsotopes, boolean useMZXMLScan)
	{
		Double currentTime = 0.0;
		Spectrum ms1Temp;
		ScanCycle scTemp;
		ArrayList<Double> maxMSMSTimes;
		ArrayList<Double> minMSMSTimes;

		if (methodParameters.polaritySwitching)
		{
			while (currentTime < methodParameters.gradientLength)
			{
				//Update progress bar
				updateProgress((int)(Math.round((currentTime/methodParameters.gradientLength)*100.0)), "% - Simulating Acquisition");


				//-----------Positive Spectra----------//
				//Generate MS1
				ms1Temp = generateMS1(minNegMass, maxNegMass, minPosMass, maxPosMass, currentTime, "+");

				posSpectra.add(ms1Temp);

				//Add in features to ms1
				addFeaturesToMS1(ms1Temp, addIsotopes, useMZXMLScan);

				//Add in noise
				addAllNoiseToMS1(ms1Temp);

				//Calculate depth
				ms1Temp.calculateDepth(true);

				//Generate Scan Cycle
				scTemp = generateScanCycle(ms1Temp);
				scanCycles.add(scTemp);

				//Add in MS/MS
				//Create array of ms/ms times assuming agc target not reached
				maxMSMSTimes = calculateMaxMS2Times(currentTime, "+");
				minMSMSTimes = calculateMinMS2Times(currentTime, "+");

				//Generate pos msms
				scTemp.generateMSMSScans(dda, exclusionList, 15.0, maxMSMSTimes, minMSMSTimes,scTemp.ms1, 
						methodParameters, instrumentParameters.minMS2SN, instrumentParameters.minPIF, 
						instrumentParameters.fragmentationEfficiency);

				//Adjust current time
				if (scTemp.ms2.size()>0)
					currentTime = scTemp.ms2.get(scTemp.ms2.size()-1).time+(instrumentParameters.gapTime/1000.0)/60.0;
				else
					currentTime = currentTime +(instrumentParameters.gapTime/1000.0)/60.0;

				//-----------Negative Spectra----------//

				//Generate MS1
				ms1Temp = generateMS1(minNegMass, maxNegMass, minPosMass, maxPosMass, currentTime, "-");

				negSpectra.add(ms1Temp);

				//Add in features to ms1
				addFeaturesToMS1(ms1Temp, addIsotopes, useMZXMLScan);

				//Add in noise
				addAllNoiseToMS1(ms1Temp);

				//Calculate depth
				ms1Temp.calculateDepth(true);

				//Generate Scan Cycle
				scTemp = generateScanCycle(ms1Temp);
				scanCycles.add(scTemp);

				//Add in MS/MS
				//Create array of ms/ms times assuming agc target not reached
				maxMSMSTimes = calculateMaxMS2Times(currentTime, "-");
				minMSMSTimes = calculateMinMS2Times(currentTime, "-");

				//Generate neg msms
				scTemp.generateMSMSScans(dda, exclusionList, 15.0, maxMSMSTimes, minMSMSTimes,scTemp.ms1,
						methodParameters, instrumentParameters.minMS2SN, instrumentParameters.minPIF,
						instrumentParameters.fragmentationEfficiency);


				//Adjust current time
				if (scTemp.ms2.size()>0)
					currentTime = scTemp.ms2.get(scTemp.ms2.size()-1).time+(instrumentParameters.gapTime/1000.0)/60.0;
				else
					currentTime = currentTime +(instrumentParameters.gapTime/1000.0)/60.0;
			}
		}
		//If negative polarity
		else if (methodParameters.polarity.equals("-"))
		{
			while (currentTime < methodParameters.gradientLength)
			{
				//Update progress bar
				updateProgress((int)(Math.round((currentTime/methodParameters.gradientLength)*100.0)), "% - Simulating Acquisition");

				//Generate MS1
				ms1Temp = generateMS1(minNegMass, maxNegMass, minPosMass, maxPosMass, currentTime, methodParameters.polarity);

				negSpectra.add(ms1Temp);

				//Add in features to ms1
				addFeaturesToMS1(ms1Temp, addIsotopes, useMZXMLScan);

				//Add in noise
				addAllNoiseToMS1(ms1Temp);

				//Calculate depth
				ms1Temp.calculateDepth(true);

				//Generate Scan Cycle
				scTemp = generateScanCycle(ms1Temp);
				scanCycles.add(scTemp);

				//Add in MS/MS
				//Create array of ms/ms times assuming agc target not reached
				maxMSMSTimes = calculateMaxMS2Times(currentTime, "-");
				minMSMSTimes = calculateMinMS2Times(currentTime, "-");

				//Generate neg msms
				scTemp.generateMSMSScans(dda, exclusionList, 15.0, maxMSMSTimes, minMSMSTimes,scTemp.ms1, 
						methodParameters, instrumentParameters.minMS2SN, instrumentParameters.minPIF, 
						instrumentParameters.fragmentationEfficiency);

				//Adjust current time
				if (scTemp.ms2.size()>0)
					currentTime = scTemp.ms2.get(scTemp.ms2.size()-1).time+(instrumentParameters.gapTime/1000.0)/60.0;
				else
					currentTime = currentTime +(instrumentParameters.gapTime/1000.0)/60.0;
			}
		}
		//If positive polarity
		else
		{
			while (currentTime < methodParameters.gradientLength)
			{
				//Update progress bar
				updateProgress((int)(Math.round((currentTime/methodParameters.gradientLength)*100.0)), "% - Simulating Acquisition");

				//Generate MS1
				ms1Temp = generateMS1(minNegMass, maxNegMass, minPosMass, maxPosMass, currentTime, methodParameters.polarity);

				posSpectra.add(ms1Temp);

				//Add in features to ms1
				addFeaturesToMS1(ms1Temp, addIsotopes, useMZXMLScan);

				//Add in noise
				addAllNoiseToMS1(ms1Temp);

				//Calculate depth
				ms1Temp.calculateDepth(true);

				//Generate Scan Cycle
				scTemp = generateScanCycle(ms1Temp);
				scanCycles.add(scTemp);

				//Add in MS/MS
				//Create array of ms/ms times assuming agc target not reached
				maxMSMSTimes = calculateMaxMS2Times(currentTime, "+");
				minMSMSTimes = calculateMinMS2Times(currentTime, "+");

				//Generate neg msms
				scTemp.generateMSMSScans(dda, exclusionList, 15.0, maxMSMSTimes, minMSMSTimes,scTemp.ms1,
						methodParameters, instrumentParameters.minMS2SN, instrumentParameters.minPIF, 
						instrumentParameters.fragmentationEfficiency);

				//Adjust current time
				if (scTemp.ms2.size()>0)
					currentTime = scTemp.ms2.get(scTemp.ms2.size()-1).time+(instrumentParameters.gapTime/1000.0)/60.0;
				else
					currentTime = currentTime +(instrumentParameters.gapTime/1000.0)/60.0;
			}
		}
	}

	//Method for reading in and parsing txt file
	public void readTXT(String filepath) throws IOException
	{
		txtParser = new TXTParser();
		txtParser.readFile(filepath);
	}

	//Method for reading in and parsing mzxml file
	public void readMZXML(String filepath) throws IOException
	{
		mzxmlParser = new MZXMLParser();
		mzxmlParser.readFile(filepath);
		mzxmlPeakArray = mzxmlParser.generateMZXMLPeakArray();
	}

	//Method for creating scan cycle using uploaded .txt file to create scans at fixed times
	public void fixedScanCreation(Double minNegMass, Double maxNegMass, Double minPosMass, Double maxPosMass, Double gradientTime,
			boolean polaritySwitchingBoolean, String polarityString, boolean addIsotopes, String filepath, boolean writeDataFile, boolean useMZXMLScan) throws IOException
	{
		//Method variables
		Spectrum ms1Temp = null;
		ScanCycle scTemp = null;

		//Read mzxml
		readTXT(filepath);

		//If writeDataFile, write scan metadata to file
		txtParser.writeSpectra(filepath.replace(".txt", "_spectra.csv"), true);

		for (int i=0; i<txtParser.scanList.size(); i++)
		{

			//If positive MS1
			if (txtParser.scanList.get(i).polarity.equals("+") 
					&& txtParser.scanList.get(i).msLevel == 1)
			{

				//Generate MS1
				ms1Temp = generateMS1(minNegMass, maxNegMass, minPosMass, maxPosMass, txtParser.scanList.get(i).retentionTime/60.0, "+");

				//Add to positive ms1 array
				posSpectra.add(ms1Temp);

				//Add in features to ms1
				addFeaturesToMS1(ms1Temp, addIsotopes, useMZXMLScan);

				//Add in noise
				addAllNoiseToMS1(ms1Temp);

				//Calculate depth
				ms1Temp.calculateDepth(true);

				scTemp = generateScanCycle(ms1Temp);
				scanCycles.add(scTemp);
			}

			//If negative MS1
			if (txtParser.scanList.get(i).polarity.equals("-") 
					&& txtParser.scanList.get(i).msLevel == 1)
			{

				//Generate MS1
				ms1Temp = generateMS1(minNegMass, maxNegMass, minPosMass, maxPosMass, txtParser.scanList.get(i).retentionTime/60.0, "-");

				//Add to negative ms1 array
				negSpectra.add(ms1Temp);

				//Add in features to ms1
				addFeaturesToMS1(ms1Temp, addIsotopes, useMZXMLScan);

				//Add in noise
				addAllNoiseToMS1(ms1Temp);

				//Calculate depth
				ms1Temp.calculateDepth(true);

				scTemp = generateScanCycle(ms1Temp);
				scanCycles.add(scTemp);
			}

			//If positive MS2
			if (txtParser.scanList.get(i).polarity.equals("+") 
					&& txtParser.scanList.get(i).msLevel == 2)
			{
				scTemp.generateSingleMSMSScan(dda, ms1Temp, txtParser.scanList.get(i).injectionTime, 
						txtParser.scanList.get(i).precursor, "+", txtParser.scanList.get(i).retentionTime/60.0,
						txtParser.scanList.get(i).isolationWidth, methodParameters, useMZXMLScan, instrumentParameters.minMS2SN, 
						instrumentParameters.minPIF, instrumentParameters.fragmentationEfficiency);
			}

			//If negative MS2
			if (txtParser.scanList.get(i).polarity.equals("-") 
					&& txtParser.scanList.get(i).msLevel == 2)
			{
				scTemp.generateSingleMSMSScan(dda, ms1Temp, txtParser.scanList.get(i).injectionTime,
						txtParser.scanList.get(i).precursor, "-", txtParser.scanList.get(i).retentionTime/60.0,
						txtParser.scanList.get(i).isolationWidth, methodParameters, useMZXMLScan, instrumentParameters.minMS2SN, 
						instrumentParameters.minPIF,instrumentParameters. fragmentationEfficiency);
			}
		}
	}

	//Write DD Scan stats
	public void writeMSMSScanStats(String filename) throws FileNotFoundException
	{		
		int numMSMS = 0;

		PrintWriter pw = new PrintWriter(filename);
		pw.println("Time(min),Polarity,Parent mz,Parent Intensity,Sample Depth,InjectionTime(ms),Theoretical AGC Injection Time(ms),Min. Required Injection Time(ms),"
				+ "Signal,Noise,S/N,Precursor Ion Fraction, Precursor Ion Fraction No Isotopes,Successful,LipidSampled,Sufficient SN,Sufficient PIF");
		for (int i=0; i<scanCycles.size(); i++)
		{
			pw.print(scanCycles.get(i).msmsStatsString());
			numMSMS += scanCycles.get(i).ms2.size();
		}
		pw.close();
	}

	//Write DD Scan stats
	public void writeFeatureStats(String filename) throws FileNotFoundException
	{
		int featureID = 0;

		PrintWriter pw = new PrintWriter(filename);
		pw.println("Polarity,Time(min),mz,Area,Identification,mz Identification,MS/MS Sampled,Identified Lipid Feature,Redundant ID");
		for (int i=0; i<potentialFeatures.size(); i++)
		{
			if (potentialFeatures.get(i).identified)
				featureID ++;

			pw.println(potentialFeatures.get(i));
		}

		pw.close();
	}

	//Return scanCycle object from spectrum
	public ScanCycle generateScanCycle(Spectrum spectrum)
	{
		return new ScanCycle(spectrum.time, spectrum.polarity, spectrum);
	}

	/*
	//Generate data dependent MS/MS scans
	public void createDataDependentScans()
	{
		//Iterate through scan cycle array
		for (int i=1; i<scanCycles.size(); i++)
		{
			//If polarity switching
			if (polaritySwitching)
			{
				//Generate pos msms
				scanCycles.get(i).generatePosMSMS(dda, exclusionList, 15.0, calculateMS2Times(posSpectra.get(i).time, "+"), scanCycles.get(i-1).posMS1);

				//Generate neg msms
				scanCycles.get(i).generateNegMSMS(dda, exclusionList, 15.0, calculateMS2Times(negSpectra.get(i).time, "-"), scanCycles.get(i-1).negMS1);
			}
			//If normal positive aquisition
			else if (polarity.equals("+"))
			{
				//Generate pos msms
				scanCycles.get(i).generatePosMSMS(dda, exclusionList, 15.0, calculateMS2Times(posSpectra.get(i).time, "+"), scanCycles.get(i-1).posMS1);
			}
			//If normal negative aquisition
			else
			{
				//Generate neg msms
				scanCycles.get(i).generateNegMSMS(dda, exclusionList, 15.0, calculateMS2Times(negSpectra.get(i).time, "-"), scanCycles.get(i-1).negMS1);
			}
		}
	}
	 */

	//Calculate scan depth
	public void calculateDepth(boolean excludeIsotopesBoolean)
	{
		for (int i=0; i<posSpectra.size(); i++)
		{
			posSpectra.get(i).calculateDepth(excludeIsotopesBoolean);
		}

		for (int i=0; i<negSpectra.size(); i++)
		{
			negSpectra.get(i).calculateDepth(excludeIsotopesBoolean);
		}	
	}

	/*
	//Populate scan cycle array with MS1
	public void addMS1ToScanCycleArray()
	{
		if (polaritySwitching)
		{
			for (int i=0; i<posSpectra.size(); i++)
			{
				//Add in both pos and neg MS1 scans
				scanCycles.add(new ScanCycle(posSpectra.get(i).time, true, true, posSpectra.get(i), negSpectra.get(i)));
			}
		}
		else if (polarity.equals("+"))
		{
			for (int i=0; i<posSpectra.size(); i++)
			{
				//Add in pos MS1 scans
				scanCycles.add(new ScanCycle(posSpectra.get(i).time, true, false, posSpectra.get(i), null));
			}
		}
		else if (polarity.equals("-"))
		{
			for (int i=0; i<negSpectra.size(); i++)
			{
				//Add in neg MS1 scans
				scanCycles.add(new ScanCycle(negSpectra.get(i).time, false, true, null, negSpectra.get(i)));
			}
		}
	}
	 */

	//Read in chemical noise files
	public void readNoise(String filename, String polarityString, boolean constant, Double retentionTime) throws IOException
	{
		String line;
		String[] split;

		//Create file buffer
		File file = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		//read line if not empty
		while ((line = reader.readLine()) != null)
		{
			if (!line.contains("-") && !line.contains(":") && !line.contains("_") && line.contains("."))
			{
				split = line.split(",");
				if (methodParameters.polaritySwitching || methodParameters.polarity.equals(polarityString))
				{
					//Double retention, Double mz, Double intensity, String polarity
					noiseArray.add(new Noise(retentionTime,Double.valueOf(split[0]), Double.valueOf(split[1]), polarityString));
				}
			}
		}

		reader.close();
	}

	//Associate identification with feature
	public void associateID(Identification id, Double mzTol, Double rtTol)
	{
		double rtDiff = 999.0;
		int index = -1;
		boolean found = false;

		for (int i=0; i<potentialFeatures.size(); i++)
		{
			if (potentialFeatures.get(i).polarity.equals(id.polarity)
					&& calcPPMDiff(potentialFeatures.get(i).mz, id.quantIon)<mzTol
					&& Math.abs(id.retention-potentialFeatures.get(i).apexRT)<rtTol)
			{
				if (rtDiff>Math.abs(id.retention-potentialFeatures.get(i).apexRT))
				{
					found = true;
					index = i;
				}
			}
		}
		if (found)
		{
			potentialFeatures.get(index).id = id;
		}
	}

	//Associate identification with feature based on precursor mz
	public void associateMZIdentifications()
	{
		double mzDiff = 999.0;
		int index = -1;
		int foundNum = 0;
		boolean found = false;
		ArrayList<MZIdentification> idArrayTemp;

		//For all features
		for (int i=0; i<potentialFeatures.size(); i++)
		{
			//Reset boolean
			found = false;

			//Generate array of potential lipid identifications
			idArrayTemp = mzIDArray.getIdentifications(potentialFeatures.get(i).mz-0.5, 
					potentialFeatures.get(i).mz+0.5, potentialFeatures.get(i).polarity);

			//Calc ppm diff for each id
			for (int j=0; j<idArrayTemp.size(); j++)
			{
				//If close in mass
				if (potentialFeatures.get(i).polarity.equals(idArrayTemp.get(j).polarity)
						&& calcPPMDiff(potentialFeatures.get(i).mz, idArrayTemp.get(j).precursor)<instrumentParameters.ppmTolerance)
				{
					//If closest mz found, retain
					if (mzDiff>calcPPMDiff(potentialFeatures.get(i).mz, idArrayTemp.get(j).precursor))
					{
						found = true;
						index = j;
					}
				}
			}

			//If found assign to feature
			if (found)
			{
				foundNum ++; 
				potentialFeatures.get(i).mzID = idArrayTemp.get(index);
			}
		}
	}

	//Read in chemical noise files
	public void readIdentifications(String filename, Double mzTol, Double rtTol) throws IOException
	{
		String line;
		String[] split;
		Identification idTemp;
		boolean redundant;

		//Create file buffer
		File file = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		//read line if not empty
		while ((line = reader.readLine()) != null)
		{
			if (!line.contains("Retention Time (min)"))
			{
				split = line.split(",");
				if (split.length>6 && split[6].contains("Redun"))
					redundant = true;
				else
					redundant = false;
				idTemp = new Identification(Double.valueOf(split[0]), Double.valueOf(split[1]), split[2], split[3],redundant);
				associateID(idTemp, mzTol, rtTol);
			}
		}


		reader.close();
	}

	//Read in mz only identifications
	public void readMZIdentifications(String filename) throws IOException
	{
		String line;
		String[] split;
		MZIdentification idTemp;
		ArrayList<MZIdentification> idArray = new ArrayList<MZIdentification>();

		//Create file buffer
		File file = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		//Create MZID array
		mzIDArray = new MZIdentificationArray(200.0, 1800.0, 1.0);

		//read line if not empty
		while ((line = reader.readLine()) != null)
		{
			split = line.split(" ");
			String identification = (split[0]+" "+split[1]+" "+split[2]).replace("Name=", "");
			String lipidClass = split[0].replace("Name=", "");
			String polarity = split[2].substring(split[2].length()-1);
			String adduct = split[2];
			Double precursor = Double.valueOf(split[3].replace("Mass=", ""));
			String formula = split[4].replace("Formula=", "");

			/*
			 * public MZIdentification(String identification, String lipidClass, String polarity, 
			String adduct, Double precursor, String formula)
			 */

			idTemp = new MZIdentification(identification, lipidClass, polarity, 
					adduct, precursor, formula);

			idArray.add(idTemp);
		}

		mzIDArray.binScans(idArray);

		reader.close();
	}


	//Read in chemical noise files
	public void readExclusionList(String filename) throws IOException
	{
		String line;
		String[] split;
		String polarityString;
		Double startTime;
		Double endTime;
		Double mz;

		//Create file buffer
		File file = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		//read line if not empty
		while ((line = reader.readLine()) != null)
		{
			if (!line.contains("Mass"))
			{
				//Parse polarity
				if (line.contains("Negative"))
					polarityString = "-";
				else
					polarityString = "+";

				split = line.split(",");

				//Parse mz
				mz = Double.valueOf(split[0]);

				//Parse start and end times
				if (split.length>6 && !split[6].equals(""))
					startTime = Double.valueOf(split[6]);
				else		
					startTime = 0.0;

				if (split.length>7 && !split[7].equals(""))
					endTime = Double.valueOf(split[7]);
				else
					endTime = methodParameters.gradientLength;

				exclusionList.add(new ExclusionListEntry(mz, polarityString, startTime, endTime));
			}
		}

		reader.close();
	}

	//Given method parameters and current time in minutes, calculate maximum time for subsequent ms2 scans in cycle
	public ArrayList<Double> calculateMaxMS2Times(Double time, String polarityString)
	{
		ArrayList<Double> times = new ArrayList<Double>();
		double ms1Transient = -1.0;
		double ms2Transient = -1.0;
		double ms1IT;
		double ms2IT;
		double timeTemp;

		//Get transient times
		for (int i=0; i<instrumentParameters.resolutionArray.length; i++)
		{
			if (instrumentParameters.resolutionArray[i] == methodParameters.ms1Resolution)
			{
				ms1Transient = instrumentParameters.transientArray[i];
			}
			if (instrumentParameters.resolutionArray[i] == methodParameters.ms2Resolution)
			{
				ms2Transient = instrumentParameters.transientArray[i];
			}
		}

		ms1IT = methodParameters.ms1InjectionTime + instrumentParameters.cTrapClearTime;
		ms2IT = methodParameters.ms2InjectionTime + instrumentParameters.cTrapClearTime;

		//Add in stepped CE delay
		if (methodParameters.steppedCE)
			ms2IT += 10.0;

		//Polarity switching calculation
		if (!methodParameters.polaritySwitching)
		{
			//If positive polarity
			if (polarityString.equals("+"))
			{
				//For top n
				for (int i=0; i<methodParameters.posTopN; i++)
				{
					//If first
					if (i==0)
					{
						timeTemp = getMax(ms2IT, ms1Transient) + instrumentParameters.scanOverhead;
						times.add(time+((timeTemp)/1000.0)/60.0);
					}
					//If later scans
					else
					{
						timeTemp = getMax(ms2IT, ms2Transient) + instrumentParameters.scanOverhead;
						times.add(times.get(times.size()-1)+((timeTemp)/1000.0)/60.0);
					}
				}
			}
			//If negative Polarity
			else
			{
				//For top n
				for (int i=0; i<methodParameters.negTopN; i++)
				{
					//If first
					if (i==0)
					{
						timeTemp = getMax(ms2IT, ms1Transient) + instrumentParameters.scanOverhead;
						times.add(time+((timeTemp)/1000.0)/60.0);
					}
					//If later scans
					else
					{
						timeTemp = getMax(ms2IT, ms2Transient) + instrumentParameters.scanOverhead;
						times.add(times.get(times.size()-1)+((timeTemp)/1000.0)/60.0);
					}
				}
			}
		}
		//If polarity switching
		else if (methodParameters.polaritySwitching)
		{
			//If positive polarity
			if (polarityString.equals("+"))
			{
				//For top n
				for (int i=0; i<methodParameters.posTopN; i++)
				{
					//If first scan
					if (i==0)
					{
						timeTemp = getMax(ms2IT, ms1Transient) + instrumentParameters.scanOverhead;
						times.add(time+((timeTemp)/1000.0)/60.0);
					}
					//If middle scans
					else
					{
						timeTemp = getMax(ms2IT, ms2Transient) + instrumentParameters.scanOverhead;
						times.add(times.get(times.size()-1)+((timeTemp)/1000.0)/60.0);
					}
				}
			}
			//If negative Polarity
			else
			{
				//For top n
				for (int i=0; i<methodParameters.negTopN; i++)
				{
					//If first scan
					if (i==0)
					{
						timeTemp = getMax(ms2IT, ms1Transient) + instrumentParameters.scanOverhead;
						times.add(time+((timeTemp)/1000.0)/60.0);
					}
					//If middle scans
					else
					{
						timeTemp = getMax(ms2IT, ms2Transient) + instrumentParameters.scanOverhead;
						times.add(times.get(times.size()-1)+((timeTemp)/1000.0)/60.0);
					}
				}
			}
		}

		return times;
	}

	//Given method parameters and current time in minutes, calculate minimum time for subsequent ms2 scans in cycle
	public ArrayList<Double> calculateMinMS2Times(Double time, String polarityString)
	{
		ArrayList<Double> times = new ArrayList<Double>();
		double ms1Transient = -1.0;
		double ms2Transient = -1.0;
		double ms1IT;
		double ms2IT;
		double timeTemp;

		//Get transient times
		for (int i=0; i<instrumentParameters.resolutionArray.length; i++)
		{
			if (instrumentParameters.resolutionArray[i] == methodParameters.ms1Resolution)
			{
				ms1Transient = instrumentParameters.transientArray[i];
			}
			if (instrumentParameters.resolutionArray[i] == methodParameters.ms2Resolution)
			{
				ms2Transient = instrumentParameters.transientArray[i];
			}
		}

		ms1IT = instrumentParameters.cTrapClearTime;
		ms2IT = instrumentParameters.cTrapClearTime;

		//Add in stepped CE delay
		if (methodParameters.steppedCE)
			ms2IT += 10.0;

		//Polarity switching calculation
		if (!methodParameters.polaritySwitching)
		{
			//If positive polarity
			if (polarityString.equals("+"))
			{
				//For top n
				for (int i=0; i<methodParameters.posTopN; i++)
				{
					//If first
					if (i==0)
					{
						timeTemp = getMax(ms2IT, ms1Transient) + instrumentParameters.scanOverhead;
						times.add(time+((timeTemp)/1000.0)/60.0);
					}
					//If later scans
					else
					{
						timeTemp = getMax(ms2IT, ms2Transient) + instrumentParameters.scanOverhead;
						times.add(times.get(times.size()-1)+((timeTemp)/1000.0)/60.0);
					}
				}
			}
			//If negative Polarity
			else
			{
				//For top n
				for (int i=0; i<methodParameters.negTopN; i++)
				{
					//If first
					if (i==0)
					{
						timeTemp = getMax(ms2IT, ms1Transient) + instrumentParameters.scanOverhead;
						times.add(time+((timeTemp)/1000.0)/60.0);
					}
					//If later scans
					else
					{
						timeTemp = getMax(ms2IT, ms2Transient) + instrumentParameters.scanOverhead;
						times.add(times.get(times.size()-1)+((timeTemp)/1000.0)/60.0);
					}
				}
			}
		}
		//If polarity switching
		else if (methodParameters.polaritySwitching)
		{
			//If positive polarity
			if (polarityString.equals("+"))
			{
				//For top n
				for (int i=0; i<methodParameters.posTopN; i++)
				{
					//If first scan
					if (i==0)
					{
						timeTemp = getMax(ms2IT, ms1Transient) + instrumentParameters.scanOverhead;
						times.add(time+((timeTemp)/1000.0)/60.0);
					}
					//If middle scans
					else
					{
						timeTemp = getMax(ms2IT, ms2Transient) + instrumentParameters.scanOverhead;
						times.add(times.get(times.size()-1)+((timeTemp)/1000.0)/60.0);
					}
				}
			}
			//If negative Polarity
			else
			{
				//For top n
				for (int i=0; i<methodParameters.negTopN; i++)
				{
					//If first scan
					if (i==0)
					{
						timeTemp = getMax(ms2IT, ms1Transient) + instrumentParameters.scanOverhead;
						times.add(time+((timeTemp)/1000.0)/60.0);
					}
					//If middle scans
					else
					{
						timeTemp = getMax(ms2IT, ms2Transient) + instrumentParameters.scanOverhead;
						times.add(times.get(times.size()-1)+((timeTemp)/1000.0)/60.0);
					}
				}
			}
		}

		return times;
	}

	//Populate scan matrix
	public void populateMS1Matrix(Double minNegMass, Double maxNegMass, Double minPosMass, Double maxPosMass, Double gradientLength)
	{
		int numSpectra = (int)((gradientLength*60.0) / (instrumentParameters.dutyCycle/1000.0))+1;

		for (int i=0; i<numSpectra; i++)
		{
			if (methodParameters.polaritySwitching)
			{
				//public Spectrum(Double time, String polarity, Double minMass, Double maxMass)
				posSpectra.add(new Spectrum(i*((instrumentParameters.dutyCycle/1000.0)/60.0),"+",minPosMass, maxPosMass));
				negSpectra.add(new Spectrum(i*((instrumentParameters.dutyCycle)/1000.0)/60.0 + ((instrumentParameters.posDutyCycle)/1000.0)/60.0,"-",minNegMass, maxNegMass));
			}
			else if (methodParameters.polarity.equals("+"))
			{
				posSpectra.add(new Spectrum(i*((instrumentParameters.dutyCycle/1000.0)/60.0),"+",minPosMass, maxPosMass));
			}
			else if (methodParameters.polarity.equals("-"))
			{
				negSpectra.add(new Spectrum(i*((instrumentParameters.dutyCycle/1000.0)/60.0),"-",minNegMass, maxNegMass));
			}
		}
	}

	//Generate a single ms1 based on parameters
	public Spectrum generateMS1(Double minNegMass, Double maxNegMass, Double minPosMass, Double maxPosMass, Double time, String polarityString)
	{
		if (polarityString.equals("+"))
		{
			return new Spectrum(time,"+",minPosMass, maxPosMass);
		}
		else
		{
			return new Spectrum(time,"-",minNegMass, maxNegMass);
		}
	}

	//Return maximum of two numbers
	public Double getMax(Double a, Double b)
	{
		if (a>b)
			return a;
		else
			return b;
	}

	//Return scan index for retention time in minutes
	public int getScanIndex(Double rt)
	{
		return (int)((rt*60.0)/(instrumentParameters.dutyCycle/1000.0));
	}

	//Add feature to ms1 scan matrix
	public void addNoiseToAllMS1(Double mz, Double intensity, String polarityString, Double retentionTime)
	{
		//If negative polarity
		if (polarityString.equals("-"))
		{
			//Iterate all neg spectra
			for (int i=0; i<negSpectra.size(); i++)
			{
				if (retentionTime != null)
				{
					if (negSpectra.get(i).time > retentionTime)
						negSpectra.get(i).addPeak(new Peak(mz, intensity, negSpectra.get(i).time,
								"-", false, null, 1), 0.005, instrumentParameters.negNoiseIntensity, 
								instrumentParameters.useMZXMLScan);
					else
						negSpectra.get(i).addPeak(new Peak(mz, intensity*(negSpectra.get(i).time/retentionTime), 
								negSpectra.get(i).time, "-", false, null, 1), 0.005, instrumentParameters.negNoiseIntensity, 
								instrumentParameters.useMZXMLScan);
				}
				else
				{
					negSpectra.get(i).addPeak(new Peak(mz, intensity, negSpectra.get(i).time, 
							"-", false, null, 1), 0.005, instrumentParameters.negNoiseIntensity, 
							instrumentParameters.useMZXMLScan);
				}
			}
		}
		else
		{
			//Iterate all pos spectra
			for (int i=0; i<posSpectra.size(); i++)
			{
				if (retentionTime != null)
				{
					if (posSpectra.get(i).time > retentionTime)
						posSpectra.get(i).addPeak(new Peak(mz, intensity, posSpectra.get(i).time, 
								"+", false, null, 1), 0.005, instrumentParameters.posNoiseIntensity, 
								instrumentParameters.useMZXMLScan);
					else
						posSpectra.get(i).addPeak(new Peak(mz, intensity*(posSpectra.get(i).time/retentionTime), 
								posSpectra.get(i).time, "+", false, null, 1), 0.005, instrumentParameters.posNoiseIntensity, 
								instrumentParameters.useMZXMLScan);	
				}
				else
				{
					posSpectra.get(i).addPeak(new Peak(mz, intensity, posSpectra.get(i).time,
							"+", false, null, 1), 0.005, instrumentParameters.posNoiseIntensity, 
							instrumentParameters.useMZXMLScan);
				}
			}
		}
	}

	//Add all possible noise peaks to a given spectrum
	public void addAllNoiseToMS1(Spectrum spectrum)
	{
		//For all noise objects
		for (int i=0; i<noiseArray.size(); i++)
		{
			//If correct polarity
			if (spectrum.polarity.equals(noiseArray.get(i).polarity))
			{
				addNoiseToMS1(noiseArray.get(i).mz, noiseArray.get(i).intensity, noiseArray.get(i).polarity, noiseArray.get(i).retention, spectrum);
			}
		}
	}

	//Add noise feature to ms1 scan matrix
	public void addNoiseToMS1(Double mz, Double intensity, String polarityString, Double retentionTime, Spectrum spectrum)
	{
		if (retentionTime != null)
		{
			if (spectrum.time > retentionTime)
				spectrum.addPeak(new Peak(mz, intensity, spectrum.time, polarityString, false, null, 1), 
						0.005, instrumentParameters.negNoiseIntensity, instrumentParameters.useMZXMLScan);
			else
				spectrum.addPeak(new Peak(mz, intensity*(spectrum.time/retentionTime),spectrum.time, polarityString, 
						false, null, 1), 0.005, instrumentParameters.negNoiseIntensity, instrumentParameters.useMZXMLScan);
		}
		else
		{
			spectrum.addPeak(new Peak(mz, intensity, spectrum.time, polarityString, false, null, 1), 0.005, 
					instrumentParameters.negNoiseIntensity, instrumentParameters.useMZXMLScan);
		}
	}


	//Add feature to ms1 scan matrix
	public void addFeatureToMS1(Feature f, boolean addIsotopes)
	{
		double rtIndex = 0.0;
		int index = -1;

		//If positive polarity
		if (f.polarity.equals("-"))
		{
			rtIndex = f.minRT;
			if (rtIndex < 0)
				rtIndex = 0;

			//Iterate through using duty cycle size 
			while (rtIndex < f.maxRT)
			{
				//Calculate array index
				index =  getScanIndex(rtIndex);

				//Add peak to spectrum at that index
				//Double mz, Double intensity, Double time, String polarity
				negSpectra.get(index).addPeak(new Peak(f.mz, f.gModel.getCalculatedHeight(rtIndex),rtIndex,
						"-", false, f, f.charge), 0.005, instrumentParameters.negNoiseIntensity, 
						instrumentParameters.useMZXMLScan);

				if (addIsotopes)
				{
					negSpectra.get(index).addPeak(new Peak(f.mz+1.003335/f.charge, 
							f.gModel.getCalculatedHeight(rtIndex)*0.4434,rtIndex, "-", true, f, f.charge),
							0.005, instrumentParameters.negNoiseIntensity, instrumentParameters.useMZXMLScan);
					negSpectra.get(index).addPeak(new Peak(f.mz+2.00671/f.charge, 
							f.gModel.getCalculatedHeight(rtIndex)*0.0959,rtIndex, "-", true, f, f.charge),
							0.005, instrumentParameters.negNoiseIntensity, instrumentParameters.useMZXMLScan);
					negSpectra.get(index).addPeak(new Peak(f.mz+3.010065/f.charge, 
							f.gModel.getCalculatedHeight(rtIndex)*0.01348,rtIndex, "-", true, f, f.charge),
							0.005, instrumentParameters.negNoiseIntensity, instrumentParameters.useMZXMLScan);
				}

				//Change rtIndex
				rtIndex += (instrumentParameters.dutyCycle/60000.0);
			}
		}
		else
		{
			rtIndex = f.minRT;
			if (rtIndex < 0)
				rtIndex = 0;

			//Iterate through using duty cycle size 
			while (rtIndex < f.maxRT)
			{
				//Calculate array index
				index =  getScanIndex(rtIndex);

				//Add peak to spectrum at that index
				//Double mz, Double intensity, Double time, String polarity
				posSpectra.get(index).addPeak(new Peak(f.mz, f.gModel.getCalculatedHeight(rtIndex),rtIndex,
						"+", false, f, f.charge), 0.005, instrumentParameters.posNoiseIntensity, 
						instrumentParameters.useMZXMLScan);

				if (addIsotopes)
				{
					posSpectra.get(index).addPeak(new Peak(f.mz+1.003335/f.charge, 
							f.gModel.getCalculatedHeight(rtIndex)*0.4434,rtIndex, "-", true, f, f.charge),
							0.005, instrumentParameters.posNoiseIntensity, instrumentParameters.useMZXMLScan);
					posSpectra.get(index).addPeak(new Peak(f.mz+2.00671/f.charge, 
							f.gModel.getCalculatedHeight(rtIndex)*0.0959,rtIndex, "-", true, f, f.charge),
							0.005, instrumentParameters.posNoiseIntensity, instrumentParameters.useMZXMLScan);
					posSpectra.get(index).addPeak(new Peak(f.mz+3.010065/f.charge, 
							f.gModel.getCalculatedHeight(rtIndex)*0.01348,rtIndex, "-", true, f, f.charge),
							0.005, instrumentParameters.posNoiseIntensity, instrumentParameters.useMZXMLScan);
				}

				//Change rtIndex
				rtIndex += (instrumentParameters.dutyCycle/60000.0);
			}
		}
	}

	//Recalibrate spectra based on mzxml spectrum
	public void recalibrateMS1FromMZXML(Spectrum s)
	{
		//For all feature peaks in spectrum
		for (int i=0; i<s.mzArray.size(); i++)
		{
			for (int j=0; j<s.mzArray.get(i).size(); j++)
			{
				if (s.mzArray.get(i).get(j).feature!= null)
				{
					s.mzArray.get(i).get(j).intensity = getIntensityFromMZXML(s.mzArray.get(i).get(j).mz, s.mzArray.get(i).get(j).time, s.polarity);
				}
			}
		}
	}

	//Add all features to a specific ms1
	public void addFeaturesToMS1(Spectrum spectrum, boolean addIsotopes, boolean useMZXML)
	{
		double noiseIntensity;

		//Generate potential feature array using lookup
		ArrayList<Feature> potentialFeatures = featureArray.getFeatures(spectrum.time-0.5, spectrum.time+0.5, spectrum.polarity);

		//Parse noise intensity
		if (spectrum.polarity.equals("+"))
			noiseIntensity = instrumentParameters.posNoiseIntensity;
		else
			noiseIntensity = instrumentParameters.negNoiseIntensity;

		//For all features
		for (int i=0; i<potentialFeatures.size(); i++)
		{
			//If correct polarity and retention time
			if (spectrum.polarity.equals(potentialFeatures.get(i).polarity) 
					&& spectrum.time > potentialFeatures.get(i).minRT 
					&& spectrum.time < potentialFeatures.get(i).maxRT)
			{
				//Add peak to spectrum
				spectrum.addPeak(new Peak(potentialFeatures.get(i).mz, potentialFeatures.get(i).gModel.getCalculatedHeight(spectrum.time),spectrum.time, 
						spectrum.polarity, false, potentialFeatures.get(i), potentialFeatures.get(i).charge), 0.005, noiseIntensity, instrumentParameters.useMZXMLScan);

				//Add isotope peaks
				if (addIsotopes)
				{
					spectrum.addPeak(new Peak(potentialFeatures.get(i).mz+1.003335/potentialFeatures.get(i).charge, 
							potentialFeatures.get(i).gModel.getCalculatedHeight(spectrum.time)*0.4434,spectrum.time, 
							spectrum.polarity, true, potentialFeatures.get(i), potentialFeatures.get(i).charge), 0.005, 
							noiseIntensity, instrumentParameters.useMZXMLScan);
					spectrum.addPeak(new Peak(potentialFeatures.get(i).mz+2.00671/potentialFeatures.get(i).charge, 
							potentialFeatures.get(i).gModel.getCalculatedHeight(spectrum.time)*0.0959,spectrum.time,
							spectrum.polarity, true, potentialFeatures.get(i), potentialFeatures.get(i).charge), 0.005, 
							noiseIntensity, instrumentParameters.useMZXMLScan);
					spectrum.addPeak(new Peak(potentialFeatures.get(i).mz+3.010065/potentialFeatures.get(i).charge, 
							potentialFeatures.get(i).gModel.getCalculatedHeight(spectrum.time)*0.01348,spectrum.time, 
							spectrum.polarity, true, potentialFeatures.get(i), potentialFeatures.get(i).charge), 0.005, 
							noiseIntensity, instrumentParameters.useMZXMLScan);
				}
			}
		}

		//Recalibrate spectrum intensities from mzxml
		if (instrumentParameters.useMZXMLScan)
		{
			recalibrateMS1FromMZXML(spectrum);
			addChemicalNoise(spectrum, noiseIntensity);
		}
	}

	//Returns pair of mzxml scans before and after a given r
	public double getIntensityFromMZXML(Double mz, Double rt, String polarity)
	{
		//Generate pair
		ArrayList<MZXMLScan> mzXMLPair = mzxmlPeakArray.getMZXMLScanPair(rt, polarity);

		//If only one exists return matching intensity
		if (mzXMLPair.size() == 1)
		{
			return mzXMLPair.get(0).getIntensity(mz, instrumentParameters.ppmTolerance);
		}
		else if (mzXMLPair.size() == 2)
		{
			//Get rt and intensity from bounding scans
			double time1 = mzXMLPair.get(0).retentionTime;
			double time2 = mzXMLPair.get(1).retentionTime;
			double int1 = mzXMLPair.get(0).getIntensity(mz, instrumentParameters.ppmTolerance);
			double int2 = mzXMLPair.get(1).getIntensity(mz, instrumentParameters.ppmTolerance);

			//Interpolate intensity value
			double interpIntensity = int1 + ((int2-int1)/(time2-time1))*(rt-time1);

			return interpIntensity;
		}
		else
		{
			return 0.0;
		}

	}

	//Adds all ms1 signal found in mzxml and not already added as a feature
	public void addChemicalNoise(Spectrum s, Double noiseLevel)
	{
		//Generate pair
		ArrayList<MZXMLScan> tempArray =  mzxmlPeakArray.getMZXMLScanPair(s.time, s.polarity);
		MZXMLScan mzXMLScan;

		if (tempArray.size()>0)
		{
			mzXMLScan = tempArray.get(0);

			for (int i=0; i<mzXMLScan.peakArray.size(); i++)
			{
				if (!mzXMLScan.peakArray.get(i).featureAssociated)
				{
					s.addPeak(mzXMLScan.peakArray.get(i).toPeak(s.time, s.polarity), 0.005, noiseLevel, true);
				}
			}
		}
	}

	//Print TIC for a given time range and polarity
	public void printTIC(String polarity, Double minRT, Double maxRT)
	{
		if (polarity.equals("+"))
		{
			for (int i=0; i<posSpectra.size(); i++)
			{
				if (posSpectra.get(i).time > minRT && posSpectra.get(i).time < maxRT)
					System.out.println(posSpectra.get(i).time+","+posSpectra.get(i).tic);
			}
		}
		else
		{
			for (int i=0; i<negSpectra.size(); i++)
			{
				if (negSpectra.get(i).time > minRT && negSpectra.get(i).time < maxRT)
					System.out.println(negSpectra.get(i).time+","+negSpectra.get(i).tic);
			}
		}
	}

	//Print depth of MS1 Spectrum
	public void printDepthtoConsol()
	{
		for (int i=0; i<posSpectra.size(); i++)
		{
			System.out.println(posSpectra.get(i).time+","+posSpectra.get(i).maxDepth
					+","+posSpectra.get(i).maxIDDepth);
		}

		for (int i=0; i<negSpectra.size(); i++)
		{
			System.out.println(negSpectra.get(i).time+","+negSpectra.get(i).maxDepth
					+","+negSpectra.get(i).maxIDDepth);
		}
	}

	//Print depth of MS1 Spectrum
	public void printDepthtoFileStacked(String filename) throws FileNotFoundException
	{
		PrintWriter pw = new PrintWriter(filename);

		for (int i=0; i<posSpectra.size(); i++)
		{
			pw.print(posSpectra.get(i).time+",");
			
			for (int j=0; j<posSpectra.get(i).intensitySortedPeakArray.size(); j++)
			{
				if (posSpectra.get(i).intensitySortedPeakArray.get(j).feature != null 
						&& posSpectra.get(i).intensitySortedPeakArray.get(j).feature.id != null)
					pw.print(posSpectra.get(i).intensitySortedPeakArray.get(j).depth+",");
			}
			
			pw.println();
		}

		for (int i=0; i<negSpectra.size(); i++)
		{
			pw.print(negSpectra.get(i).time+",");
			
			for (int j=0; j<negSpectra.get(i).intensitySortedPeakArray.size(); j++)
			{
				if (negSpectra.get(i).intensitySortedPeakArray.get(j).feature != null 
						&& negSpectra.get(i).intensitySortedPeakArray.get(j).feature.id != null)
					pw.print(negSpectra.get(i).intensitySortedPeakArray.get(j).depth+",");
			}
			
			pw.println();
		}


		pw.close();
	}

	
	//Print depth of MS1 Spectrum
	public void printDepthtoFileCollapsed(String filename) throws FileNotFoundException
	{
		PrintWriter pw = new PrintWriter(filename);

		for (int i=0; i<posSpectra.size(); i++)
		{		
			for (int j=0; j<posSpectra.get(i).intensitySortedPeakArray.size(); j++)
			{
				if (posSpectra.get(i).intensitySortedPeakArray.get(j).feature != null 
						&& posSpectra.get(i).intensitySortedPeakArray.get(j).feature.id != null)
					pw.println(posSpectra.get(i).time+","+posSpectra.get(i).intensitySortedPeakArray.get(j).depth+",");
			}
		}

		for (int i=0; i<negSpectra.size(); i++)
		{
			
			for (int j=0; j<negSpectra.get(i).intensitySortedPeakArray.size(); j++)
			{
				if (negSpectra.get(i).intensitySortedPeakArray.get(j).feature != null 
						&& negSpectra.get(i).intensitySortedPeakArray.get(j).feature.id != null)
					pw.println(negSpectra.get(i).time+","+negSpectra.get(i).intensitySortedPeakArray.get(j).depth+",");
			}
		}


		pw.close();
	}

	//Print XIC for a given time range and mz tolerance
	public void printXIC(Double mz, String polarity, Double minRT, Double maxRT, Double ppmTol)
	{
		if (polarity.equals("+"))
		{
			for (int i=0; i<posSpectra.size(); i++)
			{
				if (posSpectra.get(i).time > minRT && posSpectra.get(i).time < maxRT)
					System.out.println(posSpectra.get(i).time+","+posSpectra.get(i).getPeakIntensity(mz, ppmTol));
			}
		}
		else
		{
			for (int i=0; i<negSpectra.size(); i++)
			{
				if (negSpectra.get(i).time > minRT && negSpectra.get(i).time < maxRT)
					System.out.println(negSpectra.get(i).time+","+negSpectra.get(i).getPeakIntensity(mz, ppmTol));
			}
		}
	}

	public void printSpectrum(String polarity, Double retentionTime)
	{
		if (polarity.equals("+"))
		{
			for (int i=0; i<posSpectra.size()-1; i++)
			{
				if (posSpectra.get(i).time < retentionTime && posSpectra.get(i+1).time > retentionTime)
					System.out.println(posSpectra.get(i));
			}
		}
		else
		{
			for (int i=0; i<negSpectra.size()-1; i++)
			{
				if (negSpectra.get(i).time < retentionTime && negSpectra.get(i+1).time > retentionTime)
					System.out.println(negSpectra.get(i));
			}
		}
	}

	//Calculate the difference between two masses in ppm
	public  Double calcPPMDiff(Double mass1, Double mass2)
	{
		return (Math.abs(mass1 -  mass2)/(mass2))*1000000;
	}

	//Method to return a random double within an upper and lower bound
	public double generateRandomDouble(double leftLimit, double rightLimit)
	{
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	//Method to create feature dictionary for more efficient lookus
	public FeatureArray generateFeatureArray(ArrayList<Feature> featuresArray)
	{
		//Sory features by retention time
		Collections.sort(featuresArray);

		//Create hash table
		FeatureArray featureArray = new FeatureArray(featuresArray.get(0).apexRT,
				featuresArray.get(featuresArray.size()-1).apexRT, 0.1);

		//Populate hash table
		featureArray.binScans(featuresArray);

		return featureArray;
	}

	//Load features from .csv
	public void readFeatures(String filename, String polarityString, boolean addIsotopes, boolean randomSeed) throws IOException
	{
		String line;
		String[] split;
		Feature featureTemp;

		//Create file buffer
		File file = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		if (methodParameters.polaritySwitching || methodParameters.polarity.equals(polarityString))
		{
			//read line if not empty
			while ((line = reader.readLine()) != null)
			{
				if (!line.contains("Ion") && line.contains("["))
				{
					split = line.split(",");

					if (randomSeed)
					{
						Double randomRTMultiplier = 1.0-generateRandomDouble(-1.0, 1.0)*0.03;
						Double randomAreaMultiplier = 1.0-generateRandomDouble(-1.0, 1.0)*0.15;
						featureTemp = new Feature(polarityString, Double.valueOf(split[4]), Double.valueOf(split[5])*randomRTMultiplier, 
								Double.valueOf(split[6]), Double.valueOf(split[8])*randomAreaMultiplier, Math.abs(Integer.valueOf(split[2])));
					}
					else
						featureTemp = new Feature(polarityString, Double.valueOf(split[4]), Double.valueOf(split[5]), 
								Double.valueOf(split[6]), Double.valueOf(split[8]), Math.abs(Integer.valueOf(split[2])));

					potentialFeatures.add(featureTemp);
				}
			}
		}

		reader.close();
	}

	//Returns the median of an arrayList of doubles
	public int findMedian(ArrayList<Integer> nums)
	{
		Collections.sort(nums);
		int middle = nums.size()/2;
		if (nums.size()%2 == 1) {
			return nums.get(middle);
		} else {
			return (nums.get(middle-1) + nums.get(middle)) / 2;
		}
	}

	//Update  status bar
	public void updateProgress(int progress, String message)
	{
		if (this.progress != progress && currentProgressBar != null)
		{
			this.progress = progress;
			currentProgressBar.setValue(progress);
			currentProgressBar.setString(progress + message);
			Rectangle progressRect = currentProgressBar.getBounds();
			progressRect.x = 0;
			progressRect.y = 0;
			currentProgressBar.paintImmediately(progressRect);
		}
	}
}

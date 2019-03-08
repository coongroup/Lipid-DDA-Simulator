import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;


public class SimulationCoordinator 
{
	//Filename Strings
	String exclusionListString = "Exclusion.csv";				//String for file location of exclusion list
	String mzxmlString;													//String for file location of mzxml file
	String negFeatureString = "Neg_Features.csv";						//String for file location of negative features
	String posFeatureString = "Pos_Features.csv";						//String for file location of positive features
	String lipidIDString = "Lipid_Identifications.csv";		//String for file location of lipid identifications
	String posNoiseConstantString = "Pos_Noise_Constant.csv";	//String for file location of positive constant noise
	String posNoiseRisingString = "Pos_Noise_Rising.csv";		//String for file location of positive rising noise
	String negNoiseConstantString = "Neg_Noise_Constant.csv";	//String for file location of negative constant noise
	String negNoiseRisingString = "Neg_Noise_Rising.csv";		//String for file location of negative rising noise
	String resultFolderString = "";										//String for file location of result outputs
	boolean verbose = true;											//True iff status updates are printed to console
	boolean updateTable = true;
	int progress = 0;
	ExecutorService service;
	SwingWorker<Void, Void> parentWorker;
	Boolean isComplete = false;

	//Experiment Variables
	boolean useMZXMLScan = true;

	//Arrays
	ArrayList<Feature> potentialFeatures = new ArrayList<Feature>();
	ArrayList<ExclusionListEntry> exclusionList = new ArrayList<ExclusionListEntry>();
	ArrayList<Noise> noiseArray = new ArrayList<Noise>();
	ArrayList<Identification> identifications = new ArrayList<Identification>();
	TXTParser txtParser;
	MZXMLParser mzxmlParser;
	MZXMLPeakArray mzxmlPeakArray;
	FeatureArray featureArray;
	ArrayList<MethodParameterSet> methodParameters;

	public Result simulateSingleRun(MethodParameterSet mps, InstrumentParameterSet ips, 
			String mzXMLString, String folderString, JProgressBar progressBar) throws CustomException
	{
		//Create simulator
		Simulator s = new Simulator(mps, ips);

		//Run Simulation
		try 
		{
			return(s.runSimulation(folderString+exclusionListString, mzXMLString, folderString+negFeatureString,
					folderString+posFeatureString, folderString+lipidIDString, folderString+posNoiseConstantString, 
					folderString+posNoiseRisingString, folderString+negNoiseConstantString, folderString+negNoiseRisingString, 
					folderString+resultFolderString, verbose, progressBar));
		} 
		catch (IOException e) 
		{
			throw new CustomException(e);
		}
	}

	//Method to run genetic algorithm using parallelization
	public void runGeneticAlgorithm(int numProcesses, InstrumentParameterSet ips, 
			String mzXMLString, String folderString, MethodParameterSet mps,
			SwingWorker<Void, Void> parentWorker, MethodOptimizerSetting mops, 
			ArrayList<Integer> resolutionArray, ArrayList<Integer> agcTargetArray,
			JTable resultTable) 
					throws IOException, NumberFormatException, InterruptedException, ExecutionException
	{
		ArrayList<ArrayList<MethodParameterSet>> tempGeneration;
		ArrayList<MethodParameterSet> toCalculate;
		int generation = 1;
		this.parentWorker = parentWorker;
		isComplete = false;

		//Allow results to be written to table
		updateTable = true;
		
		//Format folder string
		folderString = folderString +"/";

		//Create Pool
		service = Executors.newFixedThreadPool(numProcesses);

		//Initialize genetic algorithm
		GeneticBreeder ga = new GeneticBreeder(mps, ips, mops, resolutionArray, agcTargetArray);
		ga.initializeGeneticAlgorithm();

		//Calculate fitness of initial population
		calculateFitness(ga.mpsPopulation, ips, mzXMLString, folderString);

		//Select initial survivors
		ga.mpsPopulation = ga.selectSurvivingMPS(ga.mpsPopulation);

		//While population has not converged
		while(!ga.hasConverged(ga.mpsPopulation))
		{
			//Print current best to table
			DefaultTableModel currentModel = (DefaultTableModel)resultTable.getModel();
			Object[] newRow = ga.bestMPS.getMutableParametersArray(generation);
			currentModel.addRow(newRow);

			System.out.println(ga.getFitnessScores(ga.mpsPopulation));
			//Perform crossover and mutation
			tempGeneration = ga.createNextGeneration(ga.mpsPopulation);

			//Get all unscored mps sets for simulation
			toCalculate = getUnscored(tempGeneration);

			//Get fitness score
			calculateFitness(toCalculate, ips, mzXMLString, folderString);
			
			//Perform selection
			ga.mpsPopulation = ga.performElitistSelection(tempGeneration);
			
			//Increment generation number
			generation ++;
		}



		//Shutdown system worker
		service.shutdown();
	}

	//Method to return array of mps sets which have no fitness score
	public ArrayList<MethodParameterSet> getUnscored(ArrayList<ArrayList<MethodParameterSet>> tempGeneration)
	{
		ArrayList<MethodParameterSet> toCalculate 
		= new ArrayList<MethodParameterSet>();

		for (int i=0; i<tempGeneration.size(); i++)
		{
			for (int j=0; j<tempGeneration.get(i).size(); j++)
			{
				if (tempGeneration.get(i).get(j).fitnessScore == 0)
					toCalculate.add(tempGeneration.get(i).get(j));
			}
		}

		return toCalculate;
	}

	//Method to calculate fitness of method parameter set array
	public void calculateFitness(ArrayList<MethodParameterSet> mpsArray, 
			InstrumentParameterSet ips,String mzXMLString, String folderString) 
					throws NumberFormatException, IOException, InterruptedException, ExecutionException
	{
		//Submit tasks
		ArrayList<Future<Result>> results = new ArrayList<Future<Result>>
		(submitCallableTasks(mpsArray, ips, mzXMLString, folderString));

		//For all results
		for (int i=0; i<results.size(); i++)
		{
			//Add fitness score to mps
			if (mpsArray.get(i).fitnessScore == 0)
			{
				if (ips.precursorIDOnly)	
					mpsArray.get(i).fitnessScore = results.get(i).get().numFeatureIDs;
				else
					mpsArray.get(i).fitnessScore = results.get(i).get().numLipidIDs;		
			}	
		}
	}

	//Method to submit and run callable tasks
	public List<Future<Result>> submitCallableTasks(ArrayList<MethodParameterSet> mpsArray, 
			InstrumentParameterSet ips,String mzXMLString, String folderString) throws NumberFormatException, IOException, InterruptedException
			{

		//Create list to hold future objects
		List<Future<Result>> simResult = new ArrayList<Future<Result>>();
		ArrayList<Task> tasks = new ArrayList<Task>();

		//Run simulation for all parameter sets
		for (int i=0; i<mpsArray.size(); i++)
		{
			//Update instrument parameters
			ips.methodParameters = mpsArray.get(i);
			ips.calculateDutyCycle();

			//Update method parameter
			mpsArray.get(i).experimentNumber = (i+1);

			//Create task
			Task taskTemp = new Task(mpsArray.get(i), ips, mzXMLString, folderString);
			tasks.add(taskTemp);
		}

		//Submit all task to be executed by thread pool and wait until shutdown
		simResult = service.invokeAll(tasks);

		return simResult;
			}

	//Method to run simulations in parallel
	public void simulateInParallel(int numProcesses, boolean closeOnFinish, InstrumentParameterSet ips, 
			String mzXMLString, String folderString, String methodParameterString, JProgressBar progressBar, JTable resultTable,
			JList<String> queueTable, SwingWorker<Void, Void> parentWorker) 
					throws NumberFormatException, IOException, InterruptedException, ExecutionException, CustomException
	{
		this.parentWorker = parentWorker;
		isComplete = false;

		//Allow results to be written to table
		updateTable = true;

		//Define queue model
		DefaultListModel<String> currentListModel = new DefaultListModel<String>();

		//Read in method parameters
		methodParameters = readMethodParameters(methodParameterString);


		//Add method parameter sets to queue
		for (int i=0; i<methodParameters.size(); i++)
		{
			currentListModel.addElement((i+1)+".  "+methodParameters.get(i));
		}
		queueTable.setModel(currentListModel);


		//Format folder string
		folderString = folderString +"/";

		//If only one processor, do not try parallel processing
		if (numProcesses<2)
		{
			//Update progress bar
			updateProgress(progressBar, 0, "% - Simulation Acquisition");

			int count = 0;

			while (!parentWorker.isCancelled() && count<methodParameters.size())
			{
				//Set Selection
				queueTable.setSelectedIndex(count);

				//Generate start time
				double timeBegin = System.currentTimeMillis()/1000.0;

				//Update instrument parameters
				ips.methodParameters = methodParameters.get(count);
				ips.calculateDutyCycle();

				System.out.println(methodParameters.get(count));

				//Simulate
				Result simResult = simulateSingleRun(methodParameters.get(count), ips, 
						mzXMLString, folderString, null);

				//Calculate time duration
				String timeDuration = String.valueOf(System.currentTimeMillis()/1000.0 - timeBegin);
				timeDuration = timeDuration.substring(0, timeDuration.indexOf(".")+2);

				if(!parentWorker.isCancelled())
				{
					//Add to JTable
					DefaultTableModel currentModel = (DefaultTableModel)resultTable.getModel();
					Object[] newRow = {simResult.experimentNumber,timeDuration,simResult.numMSMS,simResult.numSpectralMatches,simResult.numFeatureIDs,simResult.numLipidIDs};
					currentModel.addRow(newRow);

					//Update progress bar
					updateProgress(progressBar, (int)(Math.round((((count+1)*1.0)/(methodParameters.size()*1.0))*100.0)), "% - Simulation Acquisition");
				}

				count++;
			}

			this.isComplete = true;
		}
		else
		{	
			//Define result
			DefaultTableModel currentModel = (DefaultTableModel)resultTable.getModel();

			//Create Pool
			service = Executors.newFixedThreadPool(numProcesses);

			//Create list to hold future objects
			List<Future<Result>> simResult = new ArrayList<Future<Result>>();

			//Update progress bar
			updateProgress(progressBar, 0, "% - Simulation Acquisition");

			//Run simulation for all parameter sets
			for (int i=0; i<methodParameters.size(); i++)
			{
				//Update instrument parameters
				ips.methodParameters = methodParameters.get(i);
				ips.calculateDutyCycle();

				methodParameters.get(i).experimentNumber = (i+1);
				//Create task
				Task task = new Task(methodParameters.get(i), ips, mzXMLString, folderString);

				//Submit task to be executed by thread pool
				Future<Result> futureResult = service.submit(task);

				//Get result
				simResult.add(futureResult);
			}

			//Add to JTable
			for(Future<Result> fut : simResult)
			{
				try 
				{
					if (updateTable)
					{
						//Add new result row to JTable
						Object[] newRow = {fut.get().experimentNumber,fut.get().executionTime,
								fut.get().numMSMS,fut.get().numSpectralMatches,fut.get().numFeatureIDs, fut.get().numLipidIDs};

						currentModel.addRow(newRow);

						//Update progress bar
						updateProgress(progressBar, (int)(Math.round(((currentModel.getRowCount()*1.0)/(methodParameters.size()*1.0))*100.0)), "% - Simulation Acquisition");
					}
				} 
				catch (InterruptedException | ExecutionException e) 
				{
					updateTable = false;
					this.isComplete = true;
				}
			}

			if (closeOnFinish)
			{
				service.shutdown();
				this.isComplete = true;
			}
		}
	}

	class Task implements Callable<Result>
	{

		MethodParameterSet mps;
		InstrumentParameterSet ips;
		String mzXMLString;
		String folderString;
		JProgressBar progressBar;
		JList<String> queueTable;

		public Task(MethodParameterSet mps, InstrumentParameterSet ips, 
				String mzXMLString, String folderString) throws NumberFormatException, IOException
		{
			this.mps = mps;
			this.ips = ips;
			this.mzXMLString = mzXMLString;
			this.folderString = folderString;
		}

		@Override
		public Result call() throws Exception 
		{
			//Format folder string
			folderString = folderString +"/";

			//Create simulator
			Simulator s = new Simulator(mps, ips);

			//Run Simulation
			try 
			{
				//Run simulation
				return s.runSimulation(folderString+exclusionListString, mzXMLString, folderString+negFeatureString,
						folderString+posFeatureString, folderString+lipidIDString, folderString+posNoiseConstantString, 
						folderString+posNoiseRisingString, folderString+negNoiseConstantString, folderString+negNoiseRisingString, 
						folderString+resultFolderString, verbose, null);


			} catch (IOException e)
			{
				e.printStackTrace();
			}
			return null;
		}

	}

	public void loadDataFiles() throws IOException
	{
		//Initialize Arrays
		potentialFeatures = new ArrayList<Feature>();
		exclusionList = new ArrayList<ExclusionListEntry>();
		noiseArray = new ArrayList<Noise>();
		identifications = new ArrayList<Identification>();

		//Read in exclusion list
		readExclusionList(exclusionListString);

		//Read in mxml files
		if (verbose)
			System.out.println("---Reading mzXML");
		if (useMZXMLScan)
		{
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

		//Associate identifications
		if (verbose)
			System.out.println("---Reading Identifications");
		readIdentifications(lipidIDString, 15.0, 0.2);

		//Create Feature Array
		if (verbose)
			System.out.println("---Creating Feature Dictionary");
		featureArray = generateFeatureArray(potentialFeatures);

		//Read in positive baseline chemical noise
		if (verbose)
			System.out.println("---Reading Chemical Noise");
		readNoise(posNoiseConstantString, "+", true, null);
		//Read in negative baseline chemical noise
		readNoise(negNoiseConstantString, "-", true, null);

		//Read in positive gradient chemical noise
		readNoise(posNoiseRisingString, "+", false, 27.94);

		//Read in negative gradient chemical noise
		readNoise(negNoiseRisingString, "-", false, 27.90);

	}

	public ArrayList<MethodParameterSet> readMethodParameters(String filename) throws IOException
	{	
		ArrayList<MethodParameterSet> parameters = new ArrayList<MethodParameterSet>();
		String line;
		MethodParameterSet mpsTemp;

		//Create file buffer
		File file = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		//read line if not empty
		while ((line = reader.readLine()) != null)
		{
			//Read in parameters
			if (!line.contains("polarity") && line.contains(","))
			{
				mpsTemp = new MethodParameterSet(line);
				mpsTemp.experimentNumber = parameters.size()+1;
				parameters.add(mpsTemp);
			}
		}

		reader.close();

		return parameters;
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
					endTime = methodParameters.get(0).gradientLength;

				exclusionList.add(new ExclusionListEntry(mz, polarityString, startTime, endTime));
			}
		}

		reader.close();
	}

	//Method for reading in and parsing mzxml file
	public void readMZXML(String filepath) throws IOException
	{
		mzxmlParser = new MZXMLParser();
		mzxmlParser.readFile(filepath);

		mzxmlPeakArray = mzxmlParser.generateMZXMLPeakArray();
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

		if (methodParameters.get(0).polaritySwitching || methodParameters.get(0).polarity.equals(polarityString))
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

	//Method to return a random double within an upper and lower bound
	public double generateRandomDouble(double leftLimit, double rightLimit)
	{
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
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

	//Calculate the difference between two masses in ppm
	public  Double calcPPMDiff(Double mass1, Double mass2)
	{
		return (Math.abs(mass1 -  mass2)/(mass2))*1000000;
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
				if (methodParameters.get(0).polaritySwitching || methodParameters.get(0).polarity.equals(polarityString))
				{
					//Double retention, Double mz, Double intensity, String polarity
					noiseArray.add(new Noise(retentionTime,Double.valueOf(split[0]), Double.valueOf(split[1]), polarityString));
				}
			}
		}

		reader.close();
	}

	//Update  status bar
	public void updateProgress(JProgressBar progressBar, int progress, String message)
	{
		System.out.println(progress);
		if (this.progress != progress && progressBar != null)
		{

			this.progress = progress;
			progressBar.setValue(progress);
			progressBar.setString(progress + message);
			Rectangle progressRect = progressBar.getBounds();
			progressRect.x = 0;
			progressRect.y = 0;
			progressBar.paintImmediately(progressRect);
		}
	}
}

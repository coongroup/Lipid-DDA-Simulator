import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JSpinner;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.SpinnerNumberModel;
import javax.swing.JList;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;
import javax.swing.DefaultComboBoxModel;
import javax.swing.AbstractListModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.JFormattedTextField;



public class MainWindow extends JFrame {

	//Class objects
	static InstrumentParameterSet ips;
	static MethodParameterSet mps;
	static boolean allowDutyCycleUpdates = false;
	static SimulationCoordinator simulationCoordinator;
	private SwingWorker<Void, Void> worker;		
	static MethodOptimizerSetting mops;
	static GeneticBreeder geneticBreeder;

	//Window objects
	JPanel contentPane;

	//Single experiment pane objects
	final ButtonGroup buttonGroup = new ButtonGroup();
	static JTextField simFilesTextField;
	static JTextField mzxmlTextField;
	static JTable recentResultsTable;
	static JRadioButton polaritySwitchingRadio;
	static JRadioButton posPolarityRadio;
	static JRadioButton negPolarityRadio;
	static JComboBox<Integer> ms1ResComboBox;
	static JSpinner gradLengthSpinner;
	static JSpinner ms1ITSpinner;
	static JComboBox<Integer> ms2ResComboBox;
	static JComboBox<Integer> ms2AGCComboBox;
	static JSpinner ms2ITSpinner;
	static JSpinner TopNSpinner;
	static JSpinner isolationSpinner;
	static JSpinner dynamicExclusionSpinner;
	static JComboBox<String> excludeIsotopesComboBox;
	static JComboBox<String> steppedCEComboBox;
	static JButton methodSaveButton;
	static JButton methodResetButton;
	static JButton simFilesBrowseButton;
	static JButton mzxmlBrowseButton;
	static JButton runSimulationButton;
	static JProgressBar singleExperimentProgressBar;
	static JScrollPane singleExpResultsScroll;
	static private JTextField dutyCycleTextField;
	static private JLabel lblMaxDutyCycle;
	private static JSpinner minAGCSpinner;

	//Batch experiment pane objects
	static JTextField simFilesBatchTextField;
	static JTextField mzxmlFileBatchTextBox;
	static JTextField methodParamsBatchTextBox;
	static JTable batchResultsTable;
	static JScrollPane batchResultList;
	static JButton mzxmlFileBatchBrowseButton;
	static JButton methodParamsBatchBrowseButton;
	static JSpinner numCoresBatchSpinner;
	static JButton batchStartButton;
	static JButton batchStopButton;
	static JProgressBar batchProgressBar;
	static JScrollPane batchExpScrollPane;
	static JButton simFilesBatchBrowseButton;
	static JList<String> batchRunList;

	//Instrument setting objects
	static JTable transientTable;
	static JSpinner scanOverheadSpinner;
	static JSpinner cTrapClearSpinner;
	static JSpinner polaritySwitchingTimeSpinner;
	static JButton btnAddNewRow;
	static JButton btnNewButton;
	static JButton btnSave;
	static JScrollPane scrollPane;	
	static JLabel lblExperimentSettings;
	static JSeparator separator_7;
	static JSpinner posNoiseSpinner;
	static JSpinner negNoiseSpinner;
	static JSpinner mzTolSpinner;
	static JCheckBox lipidsOnlyCheckBox;
	static JLabel lblMsmsSuccessSettings;
	static JSeparator separator_8;
	static JLabel lblMinimumSn;
	static JSpinner minSNSpinner;
	static JLabel lblMinimumPif;
	static JSpinner minPIFSPinner;
	static JButton btnReset;
	static JCheckBox precursorIDCheckBox;

	//Method optimizer pane objects
	static JPanel optimizerPane;
	static JTable moResultTable;
	static JLabel lblFixedParameters;
	static JRadioButton moPolaritySwitchingRadio_1;
	static JRadioButton moPolarityRadio_1;
	static JRadioButton moNegPolarityRadio_1;
	static JSpinner moGradientLengthSpinner;
	static JComboBox<Integer> moMS1ResCombo;
	static JSpinner moMS1MaxITSpinner;
	static JComboBox<Integer> moMS2ResMinCombo;
	static JSpinner moMS2ITMinSpinner;
	static JSpinner moTopNMinSpinner;
	static JSpinner moIWMinSpinner;
	static JSpinner moDEMinSpinner;
	static JComboBox<String> moExcludeIsotopesCombo;
	static JComboBox<String> moSteppedCECombo;
	static JSpinner moTopNMaxSpinner;
	static JSpinner moTopNStepSpinner;
	static JSpinner moIWMaxSpinner;
	static JSpinner moIWStepSpinner;
	static JComboBox<Integer> moMS2ResMaxCombo;
	static JSpinner moMS2ITMaxSpinner;
	static JSpinner moMS2ITStepSpinner;
	static JSpinner moDEMaxSpinner;
	static JSpinner moDEStepSpinner;
	static JComboBox<Integer> moMinAGCMinCombo;
	static JComboBox<Integer> moMinAgcMaxCombo;
	static JButton moSaveButton;
	static JButton moResetButton;
	static JSpinner moDuplicateSpinner;
	static JSpinner moMutationRateSpinner;
	static JSpinner moNumSurvivorsSpinners;
	static JSpinner moMinIDSpinner;
	static JSpinner moMaxPopDiffSpinner;
	static JSpinner moMaxBestDiffSpinner;
	static JSpinner moMinGenerationSpinner;	
	static JSpinner moNumCoresSpinner;
	static JButton moStartButton;
	static JButton moStopButton;
	static JScrollPane scrollPane_1;
	static JSpinner moMaxDutyCycleSpinner;
	private final ButtonGroup buttonGroup_1 = new ButtonGroup();
	private JLabel lblMsAgcTarget;
	static JComboBox<Integer> ms2AGCMinSpinner;
	static JComboBox<Integer> ms2AGCMaxSpinner;
	static JTextField moSimFileFolderTextBox;
	static JTextField moMZXMLFileTextBox;
	static JButton moMZXMLFileBrowseButton;
	static JButton moSimFileBrowseButton;
	static JProgressBar moProgressBar;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws IOException 
	 */
	@SuppressWarnings({ "serial", "unchecked" })
	public MainWindow() throws IOException {
		setResizable(false);

		//Set look and feel to default for users computer
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			System.out.println("Error setting native LAF: " + e);
		}

		setTitle("Lipidomics Data Dependent Acquisition Simulator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 820, 437);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 0, 815, 409);
		contentPane.add(tabbedPane);

		JPanel singleExperimentPane = new JPanel();
		tabbedPane.addTab("Single Experiment Simulation", null, singleExperimentPane, null);
		singleExperimentPane.setLayout(null);

		JLabel lblMethodSettings = new JLabel("Method Settings");
		lblMethodSettings.setBounds(10, 11, 110, 14);
		singleExperimentPane.add(lblMethodSettings);

		JSeparator separator_3 = new JSeparator();
		separator_3.setBounds(10, 23, 172, 2);
		singleExperimentPane.add(separator_3);

		polaritySwitchingRadio = new JRadioButton("Polarity Switching");
		polaritySwitchingRadio.setSelected(true);
		polaritySwitchingRadio.addChangeListener(new ChangeListener() 
		{
			@Override
			public void stateChanged(ChangeEvent e) 
			{
				updateDutyCycle();
			}
		});
		buttonGroup.add(polaritySwitchingRadio);
		polaritySwitchingRadio.setBounds(10, 32, 109, 23);
		singleExperimentPane.add(polaritySwitchingRadio);

		posPolarityRadio = new JRadioButton("Positive Polarity");
		posPolarityRadio.addChangeListener(new ChangeListener() 
		{
			@Override
			public void stateChanged(ChangeEvent e) 
			{
				updateDutyCycle();
			}
		});
		buttonGroup.add(posPolarityRadio);
		posPolarityRadio.setBounds(10, 54, 109, 23);
		singleExperimentPane.add(posPolarityRadio);

		negPolarityRadio = new JRadioButton("Negative Polarity");
		negPolarityRadio.addChangeListener(new ChangeListener() 
		{
			@Override
			public void stateChanged(ChangeEvent e) 
			{
				updateDutyCycle();
			}
		});
		buttonGroup.add(negPolarityRadio);
		negPolarityRadio.setBounds(10, 77, 109, 23);
		singleExperimentPane.add(negPolarityRadio);

		gradLengthSpinner = new JSpinner();
		gradLengthSpinner.setModel(new SpinnerNumberModel(30, 0, 100, 1));
		gradLengthSpinner.setBounds(130, 104, 52, 20);
		singleExperimentPane.add(gradLengthSpinner);

		JLabel lblGradientLengthmin = new JLabel("Gradient Length (min)");
		lblGradientLengthmin.setBounds(10, 107, 110, 14);
		singleExperimentPane.add(lblGradientLengthmin);

		JLabel lblNewLabel_2 = new JLabel("MS Settings");
		lblNewLabel_2.setBounds(195, 11, 125, 14);
		singleExperimentPane.add(lblNewLabel_2);

		JSeparator separator_4 = new JSeparator();
		separator_4.setBounds(195, 24, 172, 2);
		singleExperimentPane.add(separator_4);

		ms1ResComboBox = new JComboBox<Integer>();
		ms1ResComboBox.addActionListener (new ActionListener () 
		{
			public void actionPerformed(ActionEvent e) 
			{
				updateDutyCycle();
			}
		});
		ms1ResComboBox.setBounds(293, 33, 74, 20);
		singleExperimentPane.add(ms1ResComboBox);

		JLabel lblMsResolution = new JLabel("MS1 Resolution");
		lblMsResolution.setBounds(195, 36, 88, 14);
		singleExperimentPane.add(lblMsResolution);

		JLabel lblMsMaxIt = new JLabel("MS1 Max. IT (ms)");
		lblMsMaxIt.setBounds(195, 67, 88, 14);
		singleExperimentPane.add(lblMsMaxIt);

		ms1ITSpinner = new JSpinner();
		ms1ITSpinner.addChangeListener(new ChangeListener() 
		{
			@Override
			public void stateChanged(ChangeEvent e) 
			{
				updateDutyCycle();
			}
		});
		ms1ITSpinner.setBounds(315, 64, 52, 20);
		singleExperimentPane.add(ms1ITSpinner);

		JLabel lblMsmsSettings = new JLabel("MS/MS Settings");
		lblMsmsSettings.setBounds(10, 135, 88, 14);
		singleExperimentPane.add(lblMsmsSettings);

		JSeparator separator_5 = new JSeparator();
		separator_5.setBounds(10, 149, 357, 2);
		singleExperimentPane.add(separator_5);

		ms2ResComboBox = new JComboBox<Integer>();
		ms2ResComboBox.addActionListener (new ActionListener () 
		{
			public void actionPerformed(ActionEvent e) 
			{
				updateDutyCycle();
			}
		});
		ms2ResComboBox.setBounds(108, 161, 74, 20);
		singleExperimentPane.add(ms2ResComboBox);

		JLabel lblMsResolution_1 = new JLabel("MS2 Resolution");
		lblMsResolution_1.setBounds(10, 164, 88, 14);
		singleExperimentPane.add(lblMsResolution_1);

		JLabel lblMsAgcTarget_1 = new JLabel("MS2 AGC Target");
		lblMsAgcTarget_1.setBounds(10, 226, 88, 14);
		singleExperimentPane.add(lblMsAgcTarget_1);

		ms2AGCComboBox = new JComboBox<Integer>();
		ms2AGCComboBox.addActionListener (new ActionListener () 
		{
			public void actionPerformed(ActionEvent e) 
			{
				updateDutyCycle();
			}
		});
		ms2AGCComboBox.setBounds(108, 225, 74, 20);
		singleExperimentPane.add(ms2AGCComboBox);

		ms2ITSpinner = new JSpinner();
		ms2ITSpinner.addChangeListener(new ChangeListener() 
		{
			@Override
			public void stateChanged(ChangeEvent e) 
			{
				updateDutyCycle();
			}
		});
		ms2ITSpinner.setModel(new SpinnerNumberModel(50, 0, 1500, 1));
		ms2ITSpinner.setBounds(130, 192, 52, 20);
		singleExperimentPane.add(ms2ITSpinner);

		JLabel lblMsMaxIt_1 = new JLabel("MS2 Max. IT (ms)");
		lblMsMaxIt_1.setBounds(10, 195, 94, 14);
		singleExperimentPane.add(lblMsMaxIt_1);

		TopNSpinner = new JSpinner();
		TopNSpinner.addChangeListener(new ChangeListener() 
		{
			@Override
			public void stateChanged(ChangeEvent e) 
			{
				updateDutyCycle();
			}
		});
		TopNSpinner.setModel(new SpinnerNumberModel(3, 1, 30, 1));
		TopNSpinner.setBounds(130, 256, 52, 20);
		singleExperimentPane.add(TopNSpinner);

		JLabel lblTopn = new JLabel("TopN");
		lblTopn.setBounds(10, 259, 46, 14);
		singleExperimentPane.add(lblTopn);

		isolationSpinner = new JSpinner();
		isolationSpinner.setModel(new SpinnerNumberModel(1.4, 0.4, 10.0, 0.1));
		isolationSpinner.setBounds(130, 287, 52, 20);
		singleExperimentPane.add(isolationSpinner);

		JLabel lblNewLabel_3 = new JLabel("Isolation Width (Th)");
		lblNewLabel_3.setBounds(10, 290, 110, 14);
		singleExperimentPane.add(lblNewLabel_3);

		dynamicExclusionSpinner = new JSpinner();
		dynamicExclusionSpinner.setModel(new SpinnerNumberModel(10.0, 0.0, 120.0, 0.1));
		dynamicExclusionSpinner.setBounds(300, 162, 67, 20);
		singleExperimentPane.add(dynamicExclusionSpinner);

		JLabel lblNewLabel_4 = new JLabel("Dynamic Exclusion (s)");
		lblNewLabel_4.setBounds(192, 165, 110, 14);
		singleExperimentPane.add(lblNewLabel_4);

		JLabel lblMinimumAgcTarget = new JLabel("Minimum AGC Target");
		lblMinimumAgcTarget.setBounds(192, 195, 109, 14);
		singleExperimentPane.add(lblMinimumAgcTarget);

		JLabel lblExcludeIsotopes = new JLabel("Exclude Isotopes");
		lblExcludeIsotopes.setBounds(192, 226, 88, 14);
		singleExperimentPane.add(lblExcludeIsotopes);

		excludeIsotopesComboBox = new JComboBox<String>();
		excludeIsotopesComboBox.addActionListener (new ActionListener () 
		{
			public void actionPerformed(ActionEvent e) 
			{
				updateDutyCycle();
			}
		});
		excludeIsotopesComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"On", "Off"}));
		excludeIsotopesComboBox.setBounds(300, 223, 67, 20);
		singleExperimentPane.add(excludeIsotopesComboBox);

		JLabel lblSteppedCe = new JLabel("Stepped CE");
		lblSteppedCe.setBounds(192, 257, 74, 14);
		singleExperimentPane.add(lblSteppedCe);

		steppedCEComboBox = new JComboBox<String>();
		steppedCEComboBox.addActionListener (new ActionListener () 
		{
			public void actionPerformed(ActionEvent e) 
			{
				updateDutyCycle();
			}
		});
		steppedCEComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"On", "Off"}));
		steppedCEComboBox.setBounds(300, 254, 67, 20);
		singleExperimentPane.add(steppedCEComboBox);

		methodSaveButton = new JButton("Save");
		methodSaveButton.setBounds(278, 347, 89, 23);
		methodSaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				try 
				{
					updateMethodParameters("src/Method_Parameters_Default.csv", true);
				} 
				catch (Exception e1) 
				{
					CustomError ce = new CustomError("Error saving method parameters", e1);
				}
			}
		});
		singleExperimentPane.add(methodSaveButton);

		methodResetButton = new JButton("Reset");
		methodResetButton.setBounds(179, 347, 89, 23);
		methodResetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				try 
				{
					allowDutyCycleUpdates = false;
					updateSingleExperimentPaneButtons("src/Method_Parameters_Default.csv");
					allowDutyCycleUpdates = true;
					updateDutyCycle();		
				} 
				catch (Exception e1) 
				{
					CustomError ce = new CustomError("Error resetting method parameters", e1);
				}
			}
		});
		singleExperimentPane.add(methodResetButton);

		JSeparator separator_6 = new JSeparator();
		separator_6.setBounds(10, 336, 357, 6);
		singleExperimentPane.add(separator_6);

		simFilesTextField = new JTextField();
		simFilesTextField.setBounds(398, 33, 306, 20);
		singleExperimentPane.add(simFilesTextField);
		simFilesTextField.setColumns(10);

		simFilesBrowseButton = new JButton("Browse");
		simFilesBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setCurrentDirectory(new File("src/"));
				chooser.setAcceptAllFileFilterUsed(false);
				int returnVal = chooser.showOpenDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) 
				{
					simFilesTextField.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		simFilesBrowseButton.setBounds(711, 32, 89, 23);
		singleExperimentPane.add(simFilesBrowseButton);

		JLabel lblSimulationFilesFolder = new JLabel("Simulation Files Folder");
		lblSimulationFilesFolder.setBounds(398, 11, 154, 14);
		singleExperimentPane.add(lblSimulationFilesFolder);

		JLabel lblMzxmlFileoptional = new JLabel("mzXML File (optional)");
		lblMzxmlFileoptional.setBounds(398, 63, 125, 14);
		singleExperimentPane.add(lblMzxmlFileoptional);

		mzxmlTextField = new JTextField();
		mzxmlTextField.setBounds(398, 80, 306, 20);
		singleExperimentPane.add(mzxmlTextField);
		mzxmlTextField.setColumns(10);

		mzxmlBrowseButton = new JButton("Browse");
		mzxmlBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("mzXML Files", "mzXML");
				chooser.setAcceptAllFileFilterUsed(true);
				chooser.setFileFilter(filter);
				chooser.setCurrentDirectory(new File("src/"));
				int returnVal = chooser.showOpenDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) 
				{
					mzxmlTextField.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		mzxmlBrowseButton.setBounds(711, 77, 89, 23);
		singleExperimentPane.add(mzxmlBrowseButton);

		runSimulationButton = new JButton("Run Simulation");
		runSimulationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				try 
				{

					worker = new SwingWorker<Void, Void>()
							{

						@SuppressWarnings("unused")
						@Override
						protected Void doInBackground() throws Exception
						{
							//Disable menu items
							setButtonStatus(false);

							//Update global settings
							updateGlobalSettings("src/Global_Settings.csv", false);

							//Update status
							updateGenerationProgress(singleExperimentProgressBar, 0, "% - Simulating Acquisition");

							//Get table model
							DefaultTableModel currentModel = (DefaultTableModel)recentResultsTable.getModel();

							//Run simulation
							Result resultTemp = simulationCoordinator.simulateSingleRun(mps, ips, mzxmlTextField.getText(), 
									simFilesTextField.getText()+"/", singleExperimentProgressBar);

							//Update table
							currentModel.addRow(resultTemp.toObjectArray());

							//Update status
							updateGenerationProgress(singleExperimentProgressBar, 100, "% - Finished");

							//Show menu items
							setButtonStatus(true);

							return null;
						}

						@Override
						protected void done()
						{

						}
							};
							worker.execute();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				} 
			}
		});

		runSimulationButton.setBounds(398, 115, 402, 23);
		singleExperimentPane.add(runSimulationButton);

		singleExperimentProgressBar = new JProgressBar();
		singleExperimentProgressBar.setStringPainted(true);
		singleExperimentProgressBar.setBounds(398, 143, 402, 23);
		singleExperimentPane.add(singleExperimentProgressBar);

		singleExpResultsScroll = new JScrollPane();
		singleExpResultsScroll.setBounds(398, 193, 402, 177);
		singleExperimentPane.add(singleExpResultsScroll);

		recentResultsTable = new JTable();
		recentResultsTable.setModel(new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
						"Timestamp", "Total MS/MS", "Spectral Matches", "All Lipid IDs", "Unique Lipid IDs"
				}
				) {
			Class[] columnTypes = new Class[] {
					String.class, Object.class, Object.class, Object.class, Object.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		recentResultsTable.getColumnModel().getColumn(0).setResizable(false);
		recentResultsTable.getColumnModel().getColumn(1).setResizable(false);
		recentResultsTable.getColumnModel().getColumn(2).setResizable(false);
		recentResultsTable.getColumnModel().getColumn(2).setPreferredWidth(99);
		recentResultsTable.getColumnModel().getColumn(3).setResizable(false);
		recentResultsTable.getColumnModel().getColumn(4).setResizable(false);
		recentResultsTable.getColumnModel().getColumn(4).setPreferredWidth(90);
		singleExpResultsScroll.setViewportView(recentResultsTable);

		JLabel lblRecentResults = new JLabel("Recent Results");
		lblRecentResults.setBounds(398, 177, 94, 14);
		singleExperimentPane.add(lblRecentResults);

		dutyCycleTextField = new JTextField();
		dutyCycleTextField.setEditable(false);
		dutyCycleTextField.setBounds(300, 287, 67, 20);
		singleExperimentPane.add(dutyCycleTextField);
		dutyCycleTextField.setColumns(10);

		lblMaxDutyCycle = new JLabel("Max. Duty Cycle (ms)");
		lblMaxDutyCycle.setBounds(192, 290, 110, 14);
		singleExperimentPane.add(lblMaxDutyCycle);

		minAGCSpinner = new JSpinner();
		minAGCSpinner.setModel(new SpinnerNumberModel(10000, 0, 1000000, 100));
		minAGCSpinner.setBounds(300, 192, 67, 20);
		singleExperimentPane.add(minAGCSpinner);

		JPanel batchExperimentPane = new JPanel();
		tabbedPane.addTab("Batch Experiment Simulation", null, batchExperimentPane, null);
		batchExperimentPane.setLayout(null);

		JLabel lblSimulationFilesFolder_1 = new JLabel("Simulation Files Folder");
		lblSimulationFilesFolder_1.setBounds(10, 11, 116, 14);
		batchExperimentPane.add(lblSimulationFilesFolder_1);

		simFilesBatchTextField = new JTextField();
		simFilesBatchTextField.setBounds(10, 29, 306, 20);
		batchExperimentPane.add(simFilesBatchTextField);
		simFilesBatchTextField.setColumns(10);

		simFilesBatchBrowseButton = new JButton("Browse");
		simFilesBatchBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setCurrentDirectory(new File("src/"));
				chooser.setAcceptAllFileFilterUsed(false);
				int returnVal = chooser.showOpenDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) 
				{
					simFilesBatchTextField.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		simFilesBatchBrowseButton.setBounds(326, 28, 89, 23);
		batchExperimentPane.add(simFilesBatchBrowseButton);

		JLabel lblMzxmlFileLocation = new JLabel("mzXML File Location (optional)");
		lblMzxmlFileLocation.setBounds(10, 60, 191, 14);
		batchExperimentPane.add(lblMzxmlFileLocation);

		mzxmlFileBatchTextBox = new JTextField();
		mzxmlFileBatchTextBox.setBounds(10, 73, 306, 20);
		batchExperimentPane.add(mzxmlFileBatchTextBox);
		mzxmlFileBatchTextBox.setColumns(10);

		mzxmlFileBatchBrowseButton = new JButton("Browse");
		mzxmlFileBatchBrowseButton.setBounds(326, 72, 89, 23);
		batchExperimentPane.add(mzxmlFileBatchBrowseButton);
		mzxmlFileBatchBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("mzXML Files", "mzXML");
				chooser.setAcceptAllFileFilterUsed(true);
				chooser.setFileFilter(filter);
				chooser.setCurrentDirectory(new File("src/"));
				int returnVal = chooser.showOpenDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) 
				{
					mzxmlFileBatchTextBox.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		JLabel lblMethodParametersList = new JLabel("Method Parameters List ");
		lblMethodParametersList.setBounds(10, 104, 165, 14);
		batchExperimentPane.add(lblMethodParametersList);

		methodParamsBatchTextBox = new JTextField();
		methodParamsBatchTextBox.setBounds(10, 119, 306, 20);
		batchExperimentPane.add(methodParamsBatchTextBox);
		methodParamsBatchTextBox.setColumns(10);

		methodParamsBatchBrowseButton = new JButton("Browse");
		methodParamsBatchBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("csv Files", "csv");
				chooser.setCurrentDirectory(new File("src/"));
				chooser.setAcceptAllFileFilterUsed(false);
				int returnVal = chooser.showOpenDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) 
				{
					methodParamsBatchTextBox.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		methodParamsBatchBrowseButton.setBounds(326, 118, 89, 23);
		batchExperimentPane.add(methodParamsBatchBrowseButton);

		JLabel lblNumberOfCores = new JLabel("Number of Cores to Use");
		lblNumberOfCores.setBounds(10, 154, 116, 14);
		batchExperimentPane.add(lblNumberOfCores);

		numCoresBatchSpinner = new JSpinner();
		numCoresBatchSpinner.setModel(new SpinnerNumberModel(1, 1, Runtime.getRuntime().availableProcessors(), 1));
		numCoresBatchSpinner.setBounds(10, 173, 116, 20);
		batchExperimentPane.add(numCoresBatchSpinner);

		batchStartButton = new JButton("Start");
		batchStartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				try 
				{
					worker = new SwingWorker<Void, Void>()
							{

						@SuppressWarnings("unused")
						@Override
						protected Void doInBackground() throws Exception
						{
							while (!isCancelled() && !simulationCoordinator.isComplete)
							{
								simulationCoordinator.isComplete = false;

								//Disable menu items
								setButtonStatus(false);

								//Update global settings
								updateGlobalSettings("src/Global_Settings.csv", false);

								simulationCoordinator.simulateInParallel((int)numCoresBatchSpinner.getValue(), true, ips, mzxmlFileBatchTextBox.getText(), 
										simFilesBatchTextField.getText(), methodParamsBatchTextBox.getText(), batchProgressBar, batchResultsTable, batchRunList, 
										worker);

								//Show menu items
								setButtonStatus(true);
							}

							//Reset value for later rerun
							simulationCoordinator.isComplete = false;

							return null;
						}

						@Override
						protected void done()
						{

						}
							};
							worker.execute();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				} 
			}
		});
		batchStartButton.setBounds(136, 172, 180, 23);
		batchExperimentPane.add(batchStartButton);

		batchStopButton = new JButton("Stop");
		batchStopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				if ((int)numCoresBatchSpinner.getValue()>1)
				{
					// Stop the swing worker thread
					simulationCoordinator.service.shutdown();
					try {
						if (!simulationCoordinator.service.awaitTermination(50, TimeUnit.MILLISECONDS)) {
							simulationCoordinator.service.shutdownNow();
						} 
					} catch (InterruptedException ie) {
						simulationCoordinator.service.shutdownNow();
					}
				}

				//Cancel Thread worker
				worker.cancel(true);

				//Clear queue table
				DefaultListModel<String> currentListModel = (DefaultListModel<String>)batchRunList.getModel();
				currentListModel.clear();

				DefaultTableModel currentTableModel = (DefaultTableModel)batchResultsTable.getModel();
				currentTableModel.setRowCount(0);
				simulationCoordinator.updateProgress(batchProgressBar, 0, "% - Stopped");

				//Unhide all buttons
				setButtonStatus(true);
			}
		});
		batchStopButton.setEnabled(false);
		batchStopButton.setBounds(326, 172, 89, 23);
		batchExperimentPane.add(batchStopButton);

		batchProgressBar = new JProgressBar();
		batchProgressBar.setStringPainted(true);
		batchProgressBar.setBounds(10, 205, 405, 23);
		batchExperimentPane.add(batchProgressBar);

		batchExpScrollPane = new JScrollPane();
		batchExpScrollPane.setBounds(10, 258, 405, 112);
		batchExperimentPane.add(batchExpScrollPane);

		batchRunList = new JList<String>();
		batchRunList.setModel(new AbstractListModel<String>() {
			String[] values = new String[] {};
			public int getSize() {
				return values.length;
			}
			public String getElementAt(int index) {
				return values[index];
			}
		});
		batchExpScrollPane.setViewportView(batchRunList);

		JLabel lblExperiment = new JLabel("Experiments");
		lblExperiment.setBounds(10, 239, 89, 14);
		batchExperimentPane.add(lblExperiment);

		JLabel lblResults = new JLabel("Results");
		lblResults.setBounds(425, 11, 46, 14);
		batchExperimentPane.add(lblResults);

		batchResultList = new JScrollPane();
		batchResultList.setBounds(425, 29, 375, 341);
		batchExperimentPane.add(batchResultList);

		batchResultsTable = new JTable();
		batchResultsTable.setModel(new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
						" #", "Time (s)", "MS/MS", "Spectral Matches", "IDs", "Unique IDs"
				}
				) {
			boolean[] columnEditables = new boolean[] {
					false, false, false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		batchResultsTable.getColumnModel().getColumn(0).setResizable(false);
		batchResultsTable.getColumnModel().getColumn(0).setPreferredWidth(34);
		batchResultsTable.getColumnModel().getColumn(1).setResizable(false);
		batchResultsTable.getColumnModel().getColumn(1).setPreferredWidth(57);
		batchResultsTable.getColumnModel().getColumn(2).setResizable(false);
		batchResultsTable.getColumnModel().getColumn(2).setPreferredWidth(51);
		batchResultsTable.getColumnModel().getColumn(3).setResizable(false);
		batchResultsTable.getColumnModel().getColumn(3).setPreferredWidth(95);
		batchResultsTable.getColumnModel().getColumn(4).setResizable(false);
		batchResultsTable.getColumnModel().getColumn(4).setPreferredWidth(40);
		batchResultsTable.getColumnModel().getColumn(5).setResizable(false);
		batchResultList.setViewportView(batchResultsTable);

		optimizerPane = new JPanel();
		tabbedPane.addTab("Method Optimizer", null, optimizerPane, null);
		optimizerPane.setLayout(null);

		JLabel lblFixedParameters = new JLabel("Fixed Parameters");
		lblFixedParameters.setBounds(10, 11, 89, 14);
		optimizerPane.add(lblFixedParameters);

		JSeparator separator_9 = new JSeparator();
		separator_9.setBounds(10, 30, 203, -16);
		optimizerPane.add(separator_9);

		moPolaritySwitchingRadio_1 = new JRadioButton("Polarity Switching");
		buttonGroup_1.add(moPolaritySwitchingRadio_1);
		moPolaritySwitchingRadio_1.setSelected(true);
		moPolaritySwitchingRadio_1.setBounds(10, 30, 109, 23);
		optimizerPane.add(moPolaritySwitchingRadio_1);

		moPolarityRadio_1 = new JRadioButton("Positive Polarity");
		buttonGroup_1.add(moPolarityRadio_1);
		moPolarityRadio_1.setBounds(10, 52, 109, 23);
		optimizerPane.add(moPolarityRadio_1);

		moNegPolarityRadio_1 = new JRadioButton("Negative Polarity");
		buttonGroup_1.add(moNegPolarityRadio_1);
		moNegPolarityRadio_1.setBounds(10, 75, 109, 23);
		optimizerPane.add(moNegPolarityRadio_1);

		JLabel label = new JLabel("Gradient Length (min)");
		label.setBounds(10, 105, 110, 14);
		optimizerPane.add(label);

		moGradientLengthSpinner = new JSpinner();
		moGradientLengthSpinner.setModel(new SpinnerNumberModel(30.0, 5.0, 120.0, 0.1));
		moGradientLengthSpinner.setBounds(130, 102, 52, 20);
		optimizerPane.add(moGradientLengthSpinner);

		JLabel label_1 = new JLabel("MS1 Resolution");
		label_1.setBounds(193, 56, 88, 14);
		optimizerPane.add(label_1);

		JLabel label_2 = new JLabel("MS1 Max. IT (ms)");
		label_2.setBounds(193, 34, 88, 14);
		optimizerPane.add(label_2);

		moMS1ResCombo = new JComboBox<Integer>();
		moMS1ResCombo.setBounds(291, 53, 67, 20);
		optimizerPane.add(moMS1ResCombo);

		moMS1MaxITSpinner = new JSpinner();
		moMS1MaxITSpinner.setModel(new SpinnerNumberModel(20, 1, 300, 1));
		moMS1MaxITSpinner.setBounds(291, 30, 67, 20);
		optimizerPane.add(moMS1MaxITSpinner);

		JLabel label_3 = new JLabel("MS2 Resolution");
		label_3.setBounds(10, 225, 88, 14);
		optimizerPane.add(label_3);

		moMS2ResMinCombo = new JComboBox<Integer>();
		moMS2ResMinCombo.setBounds(130, 222, 74, 20);
		optimizerPane.add(moMS2ResMinCombo);

		JLabel label_4 = new JLabel("MS2 Max. IT (ms)");
		label_4.setBounds(9, 249, 94, 14);
		optimizerPane.add(label_4);

		moMS2ITMinSpinner = new JSpinner();
		moMS2ITMinSpinner.setModel(new SpinnerNumberModel(10, 5, 1000, 1));
		moMS2ITMinSpinner.setBounds(130, 246, 73, 20);
		optimizerPane.add(moMS2ITMinSpinner);

		JLabel label_5 = new JLabel("TopN");
		label_5.setBounds(10, 178, 46, 14);
		optimizerPane.add(label_5);

		moTopNMinSpinner = new JSpinner();
		moTopNMinSpinner.setModel(new SpinnerNumberModel(2, 1, 30, 1));
		moTopNMinSpinner.setBounds(130, 175, 74, 20);
		optimizerPane.add(moTopNMinSpinner);

		JLabel label_6 = new JLabel("Isolation Width (Th)");
		label_6.setBounds(10, 201, 110, 14);
		optimizerPane.add(label_6);

		moIWMinSpinner = new JSpinner();
		moIWMinSpinner.setModel(new SpinnerNumberModel(0.7, 0.4, 10.0, 0.1));
		moIWMinSpinner.setBounds(130, 198, 74, 20);
		optimizerPane.add(moIWMinSpinner);

		moDEMinSpinner = new JSpinner();
		moDEMinSpinner.setModel(new SpinnerNumberModel(5.0, 1.0, 30.0, 0.5));
		moDEMinSpinner.setBounds(130, 294, 74, 20);
		optimizerPane.add(moDEMinSpinner);

		JLabel label_7 = new JLabel("Dynamic Exclusion (s)");
		label_7.setBounds(9, 293, 110, 14);
		optimizerPane.add(label_7);

		JLabel label_8 = new JLabel("Minimum AGC Target");
		label_8.setBounds(10, 323, 109, 14);
		optimizerPane.add(label_8);

		moExcludeIsotopesCombo = new JComboBox<String>();
		moExcludeIsotopesCombo.setModel(new DefaultComboBoxModel<String>(new String[] {"On", "Off"}));
		moExcludeIsotopesCombo.setBounds(291, 76, 67, 20);
		optimizerPane.add(moExcludeIsotopesCombo);

		JLabel label_9 = new JLabel("Exclude Isotopes");
		label_9.setBounds(193, 79, 88, 14);
		optimizerPane.add(label_9);

		JLabel label_10 = new JLabel("Stepped CE");
		label_10.setBounds(192, 105, 74, 14);
		optimizerPane.add(label_10);

		moSteppedCECombo = new JComboBox<String>();
		moSteppedCECombo.setModel(new DefaultComboBoxModel(new String[] {"On", "Off"}));
		moSteppedCECombo.setBounds(291, 100, 67, 20);
		optimizerPane.add(moSteppedCECombo);

		JSeparator separator_10 = new JSeparator();
		separator_10.setBounds(10, 23, 347, 2);
		optimizerPane.add(separator_10);

		JLabel lblParametersToOptimize = new JLabel("Parameters to Optimize");
		lblParametersToOptimize.setBounds(10, 133, 155, 14);
		optimizerPane.add(lblParametersToOptimize);

		JSeparator separator_11 = new JSeparator();
		separator_11.setBounds(10, 147, 348, 2);
		optimizerPane.add(separator_11);

		JLabel lblParameter = new JLabel("Parameter");
		lblParameter.setBounds(10, 158, 89, 14);
		optimizerPane.add(lblParameter);

		JLabel lblMinValue = new JLabel("Min. Value");
		lblMinValue.setBounds(130, 158, 59, 14);
		optimizerPane.add(lblMinValue);

		JLabel lblMaxValue = new JLabel("Max. Value");
		lblMaxValue.setBounds(227, 158, 69, 14);
		optimizerPane.add(lblMaxValue);

		JLabel lblStepSize = new JLabel("Step Size");
		lblStepSize.setBounds(306, 158, 46, 14);
		optimizerPane.add(lblStepSize);

		moTopNMaxSpinner = new JSpinner();
		moTopNMaxSpinner.setModel(new SpinnerNumberModel(5, 1, 50, 1));
		moTopNMaxSpinner.setBounds(214, 174, 75, 20);
		optimizerPane.add(moTopNMaxSpinner);

		moTopNStepSpinner = new JSpinner();
		moTopNStepSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		moTopNStepSpinner.setBounds(306, 175, 52, 20);
		optimizerPane.add(moTopNStepSpinner);

		moIWMaxSpinner = new JSpinner();
		moIWMaxSpinner.setModel(new SpinnerNumberModel(4.2, 0.4, 10.0, 0.1));
		moIWMaxSpinner.setBounds(214, 197, 74, 20);
		optimizerPane.add(moIWMaxSpinner);

		moIWStepSpinner = new JSpinner();
		moIWStepSpinner.setModel(new SpinnerNumberModel(0.7, 0.1, 4.0, 0.1));
		moIWStepSpinner.setBounds(306, 198, 52, 20);
		optimizerPane.add(moIWStepSpinner);

		moMS2ResMaxCombo = new JComboBox<Integer>();
		moMS2ResMaxCombo.setBounds(214, 221, 74, 20);
		optimizerPane.add(moMS2ResMaxCombo);

		moMS2ITMaxSpinner = new JSpinner();
		moMS2ITMaxSpinner.setModel(new SpinnerNumberModel(50, 5, 1000, 1));
		moMS2ITMaxSpinner.setBounds(214, 245, 74, 20);
		optimizerPane.add(moMS2ITMaxSpinner);

		moMS2ITStepSpinner = new JSpinner();
		moMS2ITStepSpinner.setModel(new SpinnerNumberModel(5, 1, 200, 1));
		moMS2ITStepSpinner.setBounds(305, 246, 52, 20);
		optimizerPane.add(moMS2ITStepSpinner);

		moDEMaxSpinner = new JSpinner();
		moDEMaxSpinner.setModel(new SpinnerNumberModel(5.0, 1.0, 30.0, 0.5));
		moDEMaxSpinner.setBounds(214, 293, 75, 20);
		optimizerPane.add(moDEMaxSpinner);

		moDEStepSpinner = new JSpinner();
		moDEStepSpinner.setModel(new SpinnerNumberModel(5.0, 1.0, 30.0, 0.5));
		moDEStepSpinner.setBounds(306, 294, 52, 20);
		optimizerPane.add(moDEStepSpinner);

		moMinAGCMinCombo = new JComboBox<Integer>();
		moMinAGCMinCombo.setModel(new DefaultComboBoxModel<Integer>
		(new Integer[] {0, 10, 100, 1000, 10000, 100000, 1000000}));
		moMinAGCMinCombo.setBounds(130, 318, 74, 20);
		optimizerPane.add(moMinAGCMinCombo);

		moMinAgcMaxCombo = new JComboBox<Integer>();
		moMinAgcMaxCombo.setModel(new DefaultComboBoxModel<Integer>
		(new Integer[] {0, 10, 100, 1000, 10000, 100000, 1000000}));
		moMinAgcMaxCombo.setBounds(215, 317, 74, 20);
		optimizerPane.add(moMinAgcMaxCombo);

		moSaveButton = new JButton("Save");
		moSaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				saveOptimizerSettings("src/Method_Optimizer_Settings.csv");
			}
		});
		moSaveButton.setBounds(269, 351, 89, 23);
		optimizerPane.add(moSaveButton);

		moResetButton = new JButton("Reset");
		moResetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				updateOptimizerSettingsFromFile("src/Method_Optimizer_Settings.csv");
			}
		});

		moResetButton.setBounds(178, 351, 89, 23);
		optimizerPane.add(moResetButton);

		JLabel lblGeneticAlgorithmParameters = new JLabel("Genetic Algorithm Parameters");
		lblGeneticAlgorithmParameters.setBounds(396, 11, 142, 14);
		optimizerPane.add(lblGeneticAlgorithmParameters);

		JSeparator separator_12 = new JSeparator();
		separator_12.setBounds(396, 23, 167, 2);
		optimizerPane.add(separator_12);

		JLabel lblMaxDutyCycle_1 = new JLabel("Max. Duty Cycle (ms)");
		lblMaxDutyCycle_1.setBounds(395, 34, 109, 14);
		optimizerPane.add(lblMaxDutyCycle_1);

		JLabel lblNewLabel_5 = new JLabel("Num. Survivors");
		lblNewLabel_5.setBounds(395, 56, 81, 14);
		optimizerPane.add(lblNewLabel_5);

		JLabel lblDuplivateInitialSets = new JLabel("Duplicate Initial Sets");
		lblDuplivateInitialSets.setBounds(395, 101, 119, 14);
		optimizerPane.add(lblDuplivateInitialSets);

		JLabel lblConvergenceParameters = new JLabel("Convergence Parameters");
		lblConvergenceParameters.setBounds(611, 11, 129, 14);
		optimizerPane.add(lblConvergenceParameters);

		JLabel lblMutationRate = new JLabel("Mutation Rate");
		lblMutationRate.setBounds(395, 79, 94, 14);
		optimizerPane.add(lblMutationRate);

		JSeparator separator_13 = new JSeparator();
		separator_13.setBounds(611, 23, 189, 2);
		optimizerPane.add(separator_13);

		moDuplicateSpinner = new JSpinner();
		moDuplicateSpinner.setModel(new SpinnerNumberModel(4, 1, 10, 1));
		moDuplicateSpinner.setBounds(514, 98, 49, 20);
		optimizerPane.add(moDuplicateSpinner);

		moMutationRateSpinner = new JSpinner();
		moMutationRateSpinner.setModel(new SpinnerNumberModel(0.2, 0.01, 0.9, 0.01));
		moMutationRateSpinner.setBounds(514, 76, 49, 20);
		optimizerPane.add(moMutationRateSpinner);

		moNumSurvivorsSpinners = new JSpinner();
		moNumSurvivorsSpinners.setModel(new SpinnerNumberModel(8, 2, 50, 2));
		moNumSurvivorsSpinners.setBounds(514, 53, 49, 20);
		optimizerPane.add(moNumSurvivorsSpinners);

		JLabel lblMinIds = new JLabel("Min. IDs");
		lblMinIds.setBounds(611, 34, 46, 14);
		optimizerPane.add(lblMinIds);

		JLabel lblMaxPop = new JLabel("Max. Pop. % Diff.");
		lblMaxPop.setBounds(611, 56, 89, 14);
		optimizerPane.add(lblMaxPop);

		JLabel lblMaxBest = new JLabel("Max. Best % Diff.");
		lblMaxBest.setBounds(611, 79, 89, 14);
		optimizerPane.add(lblMaxBest);

		JLabel lblMinGenerations = new JLabel("Min. Generations");
		lblMinGenerations.setBounds(611, 101, 89, 14);
		optimizerPane.add(lblMinGenerations);

		moMinIDSpinner = new JSpinner();
		moMinIDSpinner.setModel(new SpinnerNumberModel(400, 1, 10000, 10));
		moMinIDSpinner.setBounds(726, 31, 74, 20);
		optimizerPane.add(moMinIDSpinner);

		moMaxPopDiffSpinner = new JSpinner();
		moMaxPopDiffSpinner.setModel(new SpinnerNumberModel(0.001, 0.001, 1.0, 0.001));
		moMaxPopDiffSpinner.setBounds(726, 53, 74, 20);
		optimizerPane.add(moMaxPopDiffSpinner);

		moMaxBestDiffSpinner = new JSpinner();
		moMaxBestDiffSpinner.setModel(new SpinnerNumberModel(0.001, 0.001, 1.0, 0.001));
		moMaxBestDiffSpinner.setBounds(726, 76, 74, 20);
		optimizerPane.add(moMaxBestDiffSpinner);

		moMinGenerationSpinner = new JSpinner();
		moMinGenerationSpinner.setModel(new SpinnerNumberModel(9, 6, 500, 4));
		moMinGenerationSpinner.setBounds(726, 98, 74, 20);
		optimizerPane.add(moMinGenerationSpinner);

		JLabel label_11 = new JLabel("Number of Cores to Use");
		label_11.setBounds(396, 215, 116, 14);
		optimizerPane.add(label_11);

		moNumCoresSpinner = new JSpinner();
		moNumCoresSpinner.setModel(new SpinnerNumberModel(1, 1, Runtime.getRuntime().availableProcessors(), 1));
		moNumCoresSpinner.setBounds(396, 230, 116, 20);
		optimizerPane.add(moNumCoresSpinner);

		moStartButton = new JButton("Start");
		moStartButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				try 
				{
					//Start progress bar
					moProgressBar.setIndeterminate(true);
					worker = new SwingWorker<Void, Void>()
							{

						@SuppressWarnings("unused")
						@Override
						protected Void doInBackground() throws Exception
						{
							//Disable menu items
							setButtonStatus(false);

							//Update mops object
							updateOptimizerSettingsFromMenu();
							mops.checkValues();
							
							//Genetic algorith,
							simulationCoordinator.runGeneticAlgorithm((int)moNumCoresSpinner.getValue(), ips, 
									moMZXMLFileTextBox.getText(), moSimFileFolderTextBox.getText(), readDefaultMethodParameter("src/Method_Parameters_Default.csv"),
									worker, mops, new ArrayList<Integer>(Arrays.asList(ips.resolutionArray)), new ArrayList<Integer>(Arrays.asList(ips.agcTargetArray)), moResultTable);

							//Show menu items
							setButtonStatus(true);
							moProgressBar.setIndeterminate(false);

							return null;
						}

						@Override
						protected void done()
						{
							
						}
							};
							worker.execute();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				} 
			}
		});

		moStartButton.setBounds(522, 230, 81, 23);
		optimizerPane.add(moStartButton);

		moStopButton = new JButton("Stop");
		moStopButton.setEnabled(false);
		moStopButton.setBounds(611, 230, 89, 23);
		optimizerPane.add(moStopButton);

		scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(396, 261, 404, 113);
		optimizerPane.add(scrollPane_1);

		moResultTable = new JTable();
		moResultTable.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"Gen", "IDs", "TopN", "IW", "MS2 Res", "MS2 Max IT", "MS2 AGC", "DE", "Min AGC"
			}
		) {
			boolean[] columnEditables = new boolean[] {
				false, false, false, false, false, false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		moResultTable.getColumnModel().getColumn(0).setPreferredWidth(33);
		moResultTable.getColumnModel().getColumn(1).setPreferredWidth(37);
		moResultTable.getColumnModel().getColumn(2).setPreferredWidth(36);
		moResultTable.getColumnModel().getColumn(3).setPreferredWidth(33);
		moResultTable.getColumnModel().getColumn(4).setPreferredWidth(60);
		moResultTable.getColumnModel().getColumn(6).setPreferredWidth(71);
		moResultTable.getColumnModel().getColumn(7).setPreferredWidth(41);
		scrollPane_1.setViewportView(moResultTable);

		moMaxDutyCycleSpinner = new JSpinner();
		moMaxDutyCycleSpinner.setModel(new SpinnerNumberModel(1200.0, 100.0, 3000.0, 10.0));
		moMaxDutyCycleSpinner.setBounds(514, 30, 49, 20);
		optimizerPane.add(moMaxDutyCycleSpinner);

		lblMsAgcTarget = new JLabel("MS2 AGC Target");
		lblMsAgcTarget.setBounds(10, 274, 109, 14);
		optimizerPane.add(lblMsAgcTarget);

		ms2AGCMinSpinner = new JComboBox<Integer>();
		ms2AGCMinSpinner.setBounds(130, 270, 74, 20);
		optimizerPane.add(ms2AGCMinSpinner);

		ms2AGCMaxSpinner = new JComboBox<Integer>();
		ms2AGCMaxSpinner.setBounds(214, 269, 74, 20);
		optimizerPane.add(ms2AGCMaxSpinner);

		JLabel label_12 = new JLabel("Simulation Files Folder");
		label_12.setBounds(396, 133, 154, 14);
		optimizerPane.add(label_12);

		moSimFileFolderTextBox = new JTextField();
		moSimFileFolderTextBox.setColumns(10);
		moSimFileFolderTextBox.setBounds(396, 148, 306, 20);
		optimizerPane.add(moSimFileFolderTextBox);

		moSimFileBrowseButton = new JButton("Browse");
		moSimFileBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setCurrentDirectory(new File("src/"));
				chooser.setAcceptAllFileFilterUsed(false);
				int returnVal = chooser.showOpenDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) 
				{
					moSimFileFolderTextBox.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		moSimFileBrowseButton.setBounds(709, 147, 89, 23);
		optimizerPane.add(moSimFileBrowseButton);

		JLabel label_13 = new JLabel("mzXML File (optional)");
		label_13.setBounds(396, 173, 125, 14);
		optimizerPane.add(label_13);

		moMZXMLFileTextBox = new JTextField();
		moMZXMLFileTextBox.setColumns(10);
		moMZXMLFileTextBox.setBounds(396, 188, 306, 20);
		optimizerPane.add(moMZXMLFileTextBox);

		moMZXMLFileBrowseButton = new JButton("Browse");
		moMZXMLFileBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("mzXML Files", "mzXML");
				chooser.setAcceptAllFileFilterUsed(true);
				chooser.setFileFilter(filter);
				chooser.setCurrentDirectory(new File("src/"));
				int returnVal = chooser.showOpenDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) 
				{
					moMZXMLFileTextBox.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		moMZXMLFileBrowseButton.setBounds(709, 188, 89, 23);
		optimizerPane.add(moMZXMLFileBrowseButton);
		
		moProgressBar = new JProgressBar();
		moProgressBar.setBounds(710, 230, 90, 23);
		optimizerPane.add(moProgressBar);

		JPanel globalSettingsPane = new JPanel();
		tabbedPane.addTab("Global Settings", null, globalSettingsPane, null);
		globalSettingsPane.setLayout(null);

		JLabel lblInstrumentAcquisitionSettings = new JLabel("Instrument Parameters");
		lblInstrumentAcquisitionSettings.setBounds(17, 11, 160, 14);
		globalSettingsPane.add(lblInstrumentAcquisitionSettings);

		JSeparator separator = new JSeparator();
		separator.setBounds(17, 25, 191, 2);
		globalSettingsPane.add(separator);

		scanOverheadSpinner = new JSpinner();
		scanOverheadSpinner.setModel(new SpinnerNumberModel(6, 0, 1000, 1));
		scanOverheadSpinner.setBounds(158, 35, 51, 20);
		globalSettingsPane.add(scanOverheadSpinner);

		JLabel lblScanOverheadms = new JLabel("Scan Overhead (ms)");
		lblScanOverheadms.setBounds(17, 39, 103, 14);
		globalSettingsPane.add(lblScanOverheadms);

		cTrapClearSpinner = new JSpinner();
		cTrapClearSpinner.setModel(new SpinnerNumberModel(23, 0, 200, 1));
		cTrapClearSpinner.setBounds(158, 61, 51, 20);
		globalSettingsPane.add(cTrapClearSpinner);

		JLabel lblCTrapClearing = new JLabel("C Trap Clearing Time (ms)");
		lblCTrapClearing.setBounds(17, 65, 123, 14);
		globalSettingsPane.add(lblCTrapClearing);

		polaritySwitchingTimeSpinner = new JSpinner();
		polaritySwitchingTimeSpinner.setModel(new SpinnerNumberModel(233, 0, 1000, 1));
		polaritySwitchingTimeSpinner.setBounds(158, 86, 51, 20);
		globalSettingsPane.add(polaritySwitchingTimeSpinner);

		JLabel lblNewLabel = new JLabel("Polarity Swtiching Time (ms)");
		lblNewLabel.setBounds(17, 90, 133, 14);
		globalSettingsPane.add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Orbitrap Transient Times");
		lblNewLabel_1.setBounds(17, 129, 133, 14);
		globalSettingsPane.add(lblNewLabel_1);

		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(17, 142, 191, 2);
		globalSettingsPane.add(separator_1);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(17, 152, 191, 122);
		globalSettingsPane.add(scrollPane);

		transientTable = new JTable();
		transientTable.setModel(new DefaultTableModel(
				new Object[][] {
						{new Integer(15000), new Integer(32)},
						{new Integer(30000), new Integer(64)},
						{new Integer(60000), new Integer(128)},
						{new Integer(120000), new Integer(256)},
						{new Integer(240000), new Integer(512)},
				},
				new String[] {
						"Resolution", "Transient (ms)"
				}
				) {
			Class[] columnTypes = new Class[] {
					Integer.class, Integer.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		transientTable.getColumnModel().getColumn(0).setPreferredWidth(102);
		transientTable.getColumnModel().getColumn(1).setPreferredWidth(106);
		scrollPane.setViewportView(transientTable);

		btnAddNewRow = new JButton("Add Row");
		btnAddNewRow.setBounds(134, 285, 75, 23);
		globalSettingsPane.add(btnAddNewRow);

		btnNewButton = new JButton("Delete Selected");
		btnNewButton.setBounds(17, 285, 107, 23);
		globalSettingsPane.add(btnNewButton);

		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(17, 332, 448, 2);
		globalSettingsPane.add(separator_2);


		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				try 
				{
					updateGlobalSettings("src/Global_Settings.csv", true);
				} 
				catch (FileNotFoundException e1) {
					CustomError ce = new CustomError("Error global settings reading file", e1);
				}
			}
		});
		btnSave.setBounds(376, 343, 89, 23);
		globalSettingsPane.add(btnSave);


		btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				try 
				{
					ips = readDefaultInstrumentParameter("src/Global_Settings.csv");
					ips.methodParameters = mps;
					updateInstrumentSettingsItems();
				} catch (Exception e1) {
					CustomError ce = new CustomError("Error resetting global settings file", e1);
				}
			}
		});
		btnReset.setBounds(263, 343, 93, 23);
		globalSettingsPane.add(btnReset);

		lblExperimentSettings = new JLabel("Experiment Settings");
		lblExperimentSettings.setBounds(267, 11, 103, 14);
		globalSettingsPane.add(lblExperimentSettings);

		separator_7 = new JSeparator();
		separator_7.setBounds(267, 25, 198, 2);
		globalSettingsPane.add(separator_7);

		JLabel lblNoiseIntensity = new JLabel("Noise Intensity (+)");
		lblNoiseIntensity.setBounds(267, 38, 103, 14);
		globalSettingsPane.add(lblNoiseIntensity);

		JLabel lblNoiseIntensity_1 = new JLabel("Noise Intensity (-)");
		lblNoiseIntensity_1.setBounds(267, 64, 89, 14);
		globalSettingsPane.add(lblNoiseIntensity_1);

		JLabel lblMassToleranceppm = new JLabel("Mass Tolerance (ppm)");
		lblMassToleranceppm.setBounds(267, 89, 117, 14);
		globalSettingsPane.add(lblMassToleranceppm);

		posNoiseSpinner = new JSpinner();
		posNoiseSpinner.setModel(new SpinnerNumberModel(50000, 0, 1000000, 100));
		posNoiseSpinner.setBounds(390, 35, 75, 20);
		globalSettingsPane.add(posNoiseSpinner);

		negNoiseSpinner = new JSpinner();
		negNoiseSpinner.setModel(new SpinnerNumberModel(2500, 0, 1000000, 100));
		negNoiseSpinner.setBounds(390, 61, 75, 20);
		globalSettingsPane.add(negNoiseSpinner);

		mzTolSpinner = new JSpinner();
		mzTolSpinner.setModel(new SpinnerNumberModel(15.0, 1.0, 100.0, 1.0));
		mzTolSpinner.setBounds(390, 86, 75, 20);
		globalSettingsPane.add(mzTolSpinner);

		lipidsOnlyCheckBox = new JCheckBox("Only Sample Lipids");
		lipidsOnlyCheckBox.setBounds(267, 110, 117, 23);
		globalSettingsPane.add(lipidsOnlyCheckBox);

		lblMsmsSuccessSettings = new JLabel("MS/MS Success Settings");
		lblMsmsSuccessSettings.setBounds(267, 159, 123, 14);
		globalSettingsPane.add(lblMsmsSuccessSettings);

		separator_8 = new JSeparator();
		separator_8.setBounds(267, 178, 198, 2);
		globalSettingsPane.add(separator_8);

		lblMinimumSn = new JLabel("Minimum S/N");
		lblMinimumSn.setBounds(267, 187, 89, 14);
		globalSettingsPane.add(lblMinimumSn);

		minSNSpinner = new JSpinner();
		minSNSpinner.setModel(new SpinnerNumberModel(3.0, 0.0, 20.0, 1.0));
		minSNSpinner.setBounds(390, 184, 75, 20);
		globalSettingsPane.add(minSNSpinner);

		lblMinimumPif = new JLabel("Minimum PIF");
		lblMinimumPif.setBounds(267, 212, 89, 14);
		globalSettingsPane.add(lblMinimumPif);

		minPIFSPinner = new JSpinner();
		minPIFSPinner.setModel(new SpinnerNumberModel(0.5, 0.0, 1.0, 0.1));
		minPIFSPinner.setBounds(390, 209, 75, 20);
		globalSettingsPane.add(minPIFSPinner);

		precursorIDCheckBox = new JCheckBox("Use Precursor m/z Lipid IDs");
		precursorIDCheckBox.setBounds(263, 233, 192, 23);
		globalSettingsPane.add(precursorIDCheckBox);

		//Update params
		updateSingleExperimentPaneButtons("src/Method_Parameters_Default.csv");

		//Parse instrument parameters
		ips = readDefaultInstrumentParameter("src/Global_Settings.csv");
		ips.methodParameters = mps;
		updateInstrumentSettingsItems();

		//Update params
		updateSingleExperimentPaneButtons("src/Method_Parameters_Default.csv");

		//Update method optimizer parameters
		updateOptimizerSettingsFromFile("src/Method_Optimizer_Settings.csv");

		//Allow duty cycle updates
		allowDutyCycleUpdates = true;

		//Calculate duty cycle
		updateDutyCycle();

		//Create simulation coordinator object
		simulationCoordinator = new SimulationCoordinator();
	}

	//Method to calculate duty cycle after parameters are udpated
	public static void updateDutyCycle()
	{	
		if (allowDutyCycleUpdates)
		{
			try 
			{
				updateMethodParameters("", false);
				ips.methodParameters = mps;
				ips.calculateDutyCycle();
				dutyCycleTextField.setText(String.valueOf(ips.dutyCycle));
			} 
			catch (FileNotFoundException e) 
			{
				CustomError ce = new CustomError("Error updating method parameters", e);
			}
		}
	}

	//Method to write menu item values to file
	public static void saveOptimizerSettings(String filename)
	{
		try
		{
			//Update object
			updateOptimizerSettingsFromMenu();
			mops.checkValues();

			//Write file
			mops.writeMethodOptimizerSettings(filename);
		} 
		catch (IOException e) 
		{
			CustomError ce = new CustomError("Error saving optimization parameters", e);
			e.printStackTrace();
		}
		catch (CustomException e) 
		{
			CustomError ce = new CustomError(e.getMessage(), null);
		}
	}

	//Method to update current optimization parameter set from button value
	public static void updateOptimizerSettingsFromMenu()
	{
		mops.polaritySwitching = moPolaritySwitchingRadio_1.isSelected();

		if (moPolarityRadio_1.isSelected())
			mops.polarity = "+";
		else if (moNegPolarityRadio_1.isSelected())
			mops.polarity = "-";

		mops.polaritySwitching = moPolaritySwitchingRadio_1.isSelected();
		mops.gradientLength = (double)moGradientLengthSpinner.getValue();
		mops.ms1Resolution = (int)moMS1ResCombo.getSelectedItem();
		mops.ms1InjectionTime = (int)moMS1MaxITSpinner.getValue();
		mops.ms2ResMin = (int)moMS2ResMinCombo.getSelectedItem();
		mops.injectionTimeMin = (int)moMS2ITMinSpinner.getValue();
		mops.topNMin = (int)moTopNMinSpinner.getValue();
		mops.isolationMin = (double)moIWMinSpinner.getValue();
		mops.deMin = (double)moDEMinSpinner.getValue();
		mops.excludeIsotopes = String.valueOf(moExcludeIsotopesCombo.getSelectedItem()).equals("On");
		mops.steppedCE = String.valueOf(moSteppedCECombo.getSelectedItem()).equals("On");

		mops.topNMax = (int) moTopNMaxSpinner.getValue();
		mops.topNStep = (int) moTopNStepSpinner.getValue();
		mops.isolationMax = (double) moIWMaxSpinner.getValue();
		mops.isolationStep = (double) moIWStepSpinner.getValue();
		mops.ms2ResMax = (int) moMS2ResMaxCombo.getSelectedItem();
		mops.injectionTimeMax = (int) moMS2ITMaxSpinner.getValue();
		mops.injectionTimeStep = (int) moMS2ITStepSpinner.getValue();
		mops.ms2AGCMin = (int) ms2AGCMinSpinner.getSelectedItem();
		mops.ms2AGCMax = (int) ms2AGCMaxSpinner.getSelectedItem();
		mops.deMax = (double) moDEMaxSpinner.getValue();
		mops.deStep = (double) moDEStepSpinner.getValue();
		mops.minAGCMin = (int) moMinAGCMinCombo.getSelectedItem();
		mops.minAGCMax = (int) moMinAgcMaxCombo.getSelectedItem();
		mops.numDuplicates = (int) moDuplicateSpinner.getValue();
		mops.mutationRate = (double) moMutationRateSpinner.getValue();
		mops.numSurvivors = (int) moNumSurvivorsSpinners.getValue();
		mops.minFitness = (int) moMinIDSpinner.getValue();
		mops.maxPopPercentDiff = (double) moMaxPopDiffSpinner.getValue();
		mops.maxAllTimePercentDiff = (double) moMaxBestDiffSpinner.getValue();
		mops.minIterations = (int) moMinGenerationSpinner.getValue();
		mops.maxDutyCycle = (double) moMaxDutyCycleSpinner.getValue();
	}


	//Method to update method optimizer buttons from settings file
	public static void updateOptimizerSettingsFromFile(String filename)
	{
		//Read method optimizer settings
		try 
		{
			mops = new MethodOptimizerSetting("src/Method_Optimizer_Settings.csv");
		} 
		catch (IOException e) 
		{
			CustomError ce = new CustomError("Error updating optimization settings", e);
		}

		//Initialize menu dropdowns
		moMS2ResMinCombo.setModel(new DefaultComboBoxModel<Integer>(ips.getResolutionArray()));
		moMS2ResMaxCombo.setModel(new DefaultComboBoxModel<Integer>(ips.getResolutionArray()));
		moMS1ResCombo.setModel(new DefaultComboBoxModel<Integer>(ips.getResolutionArray()));
		ms2AGCMinSpinner.setModel(new DefaultComboBoxModel<Integer>(ips.agcTargetArray));
		ms2AGCMaxSpinner.setModel(new DefaultComboBoxModel<Integer>(ips.agcTargetArray));

		//Update buttons;
		moPolaritySwitchingRadio_1.setSelected(mops.polaritySwitching);

		if (!mops.polaritySwitching)
		{
			moPolarityRadio_1.setSelected(mops.polarity.equals("+"));
			moNegPolarityRadio_1.setSelected(mops.polarity.equals("-"));
		}

		moGradientLengthSpinner.setValue(mops.gradientLength);
		moMS1ResCombo.setSelectedItem(mops.ms1Resolution);
		moMS1MaxITSpinner.setValue(mops.ms1InjectionTime);
		moMS2ResMinCombo.setSelectedItem(mops.ms2ResMin);
		moMS2ITMinSpinner.setValue(mops.injectionTimeMin);
		moTopNMinSpinner.setValue(mops.topNMin);
		moIWMinSpinner.setValue(mops.isolationMin);
		moDEMinSpinner.setValue(mops.deMin);

		if (mops.excludeIsotopes)
			moExcludeIsotopesCombo.setSelectedItem("On");
		else
			moExcludeIsotopesCombo.setSelectedItem("Off");

		if (mops.steppedCE)
			moSteppedCECombo.setSelectedItem("On");
		else
			moSteppedCECombo.setSelectedItem("Off");

		moTopNMaxSpinner.setValue(mops.topNMax);
		moTopNStepSpinner.setValue(mops.topNStep);
		moIWMaxSpinner.setValue(mops.isolationMax);
		moIWStepSpinner.setValue(mops.isolationStep);
		moMS2ResMaxCombo.setSelectedItem(mops.ms2ResMax);
		moMS2ITMaxSpinner.setValue(mops.injectionTimeMax);
		moMS2ITStepSpinner.setValue(mops.injectionTimeStep);
		ms2AGCMinSpinner.setSelectedItem(mops.ms2AGCMin);
		ms2AGCMaxSpinner.setSelectedItem(mops.ms2AGCMax);
		moDEMaxSpinner.setValue(mops.deMax);
		moDEStepSpinner.setValue(mops.deStep);
		moMinAGCMinCombo.setSelectedItem(mops.minAGCMin);
		moMinAgcMaxCombo.setSelectedItem(mops.minAGCMax);
		moDuplicateSpinner.setValue(mops.numDuplicates);
		moMutationRateSpinner.setValue(mops.mutationRate);
		moNumSurvivorsSpinners.setValue(mops.numSurvivors);
		moMinIDSpinner.setValue(mops.minFitness);
		moMaxPopDiffSpinner.setValue(mops.maxPopPercentDiff);
		moMaxBestDiffSpinner.setValue(mops.maxAllTimePercentDiff);
		moMinGenerationSpinner.setValue(mops.minIterations);
		moMaxDutyCycleSpinner.setValue(mops.maxDutyCycle);
	}

	public static void updateGlobalSettings(String filename, boolean writeToFile) throws FileNotFoundException
	{
		ips.scanOverhead = (int)scanOverheadSpinner.getValue();
		ips.cTrapClearTime = (int)cTrapClearSpinner.getValue();
		ips.switchingTime = (int)polaritySwitchingTimeSpinner.getValue();
		ips.posNoiseIntensity = Double.valueOf(String.valueOf(posNoiseSpinner.getValue()));
		ips.negNoiseIntensity = Double.valueOf(String.valueOf(negNoiseSpinner.getValue()));
		ips.ppmTolerance = Double.valueOf(String.valueOf(mzTolSpinner.getValue()));
		ips.onlyLipids = lipidsOnlyCheckBox.isSelected();
		ips.minMS2SN = (double)minSNSpinner.getValue();
		ips.minPIF = (double)minPIFSPinner.getValue();
		ips.precursorIDOnly = precursorIDCheckBox.isSelected();

		if (writeToFile)
		{
			PrintWriter pw = new PrintWriter(filename);

			pw.println("Scan Overhead (ms),"+ips.scanOverhead);
			pw.println("C Trap Clear Time (ms),"+ips.cTrapClearTime);
			pw.println("Polarity Switching Time (ms),"+ips.switchingTime);

			pw.print("Resolution Settings,");
			for (int i=0; i<ips.resolutionArray.length; i++)
			{
				pw.print(ips.resolutionArray[i]);
				if (i<ips.resolutionArray.length-1)
					pw.print(",");
			}
			pw.println();

			pw.print("Transient Time (ms),");
			for (int i=0; i<ips.transientArray.length; i++)
			{
				pw.print(ips.transientArray[i]);
				if (i<ips.transientArray.length-1)
					pw.print(",");
			}
			pw.println();

			pw.print("AGC Target Options,");
			for (int i=0; i<ips.agcTargetArray.length; i++)
			{
				pw.print(ips.agcTargetArray[i]);
				if (i<ips.agcTargetArray.length-1)
					pw.print(",");
			}
			pw.println();

			pw.println("Negative Noise Intensity,"+ips.negNoiseIntensity);
			pw.println("Positive Noise Intensity,"+ips.posNoiseIntensity);
			pw.println("Minimum MS2 SN,"+ips.minMS2SN);
			pw.println("Minimum PIF,"+ips.minPIF);
			pw.println("Only Sample Lipids,"+ips.onlyLipids);
			pw.println("Mass Tolerance (ppm),"+ips.ppmTolerance);
			pw.println("Precursor mz Lipid ID,"+ips.precursorIDOnly);
			pw.close();
		}
	}

	public static void updateMethodParameters(String filename, boolean writeToFile) throws FileNotFoundException
	{
		//Update parameters
		mps.polaritySwitching = polaritySwitchingRadio.isSelected();

		if (posPolarityRadio.isSelected())
			mps.polarity = "+";
		else
			mps.polarity = "-";

		mps.ms1Resolution = ms1ResComboBox.getItemAt(ms1ResComboBox.getSelectedIndex());
		mps.ms2Resolution = ms2ResComboBox.getItemAt(ms2ResComboBox.getSelectedIndex());
		mps.ms1InjectionTime = (int)ms1ITSpinner.getValue();
		mps.ms2InjectionTime = (int)ms2ITSpinner.getValue();
		mps.ms2AGCTarget =  ms2AGCComboBox.getItemAt(ms2AGCComboBox.getSelectedIndex());
		mps.posTopN = (int)TopNSpinner.getValue();
		mps.negTopN = (int)TopNSpinner.getValue();
		mps.isolationWidth = (double)isolationSpinner.getValue();
		mps.dynamicExclusion = (double)dynamicExclusionSpinner.getValue();
		mps.minAGCTarget = (int)minAGCSpinner.getValue();
		mps.gradientLength = (double)(gradLengthSpinner.getValue());

		if (excludeIsotopesComboBox.getItemAt(excludeIsotopesComboBox.getSelectedIndex()).equals("On"))
			mps.excludeIsotopes = true;
		else
			mps.excludeIsotopes = false;

		if (steppedCEComboBox.getItemAt(steppedCEComboBox.getSelectedIndex()).equals("On"))
			mps.steppedCE = true;
		else
			mps.steppedCE = false;

		//Write to file
		if(writeToFile)
			mps.writeParameters(filename);
	}

	//Reads method parameter .csv and returns methodParameter Set
	public static MethodParameterSet readDefaultMethodParameter(String filename) throws IOException
	{
		ArrayList<MethodParameterSet> parameters = new ArrayList<MethodParameterSet>();
		String line;

		//Create file buffer
		File file = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		//read line if not empty
		while ((line = reader.readLine()) != null)
		{
			//Read in parameters
			if (!line.contains("polarity") && (line.contains(",")))
			{
				parameters.add(new MethodParameterSet(line));
			}
		}

		reader.close();

		return parameters.get(0);
	}

	//Reads instrument parameter .csv and returns InstrumentParameter Set
	public static InstrumentParameterSet readDefaultInstrumentParameter(String filename) throws IOException
	{
		return new InstrumentParameterSet(null, filename, false);
	}

	//Updates menu items in single experiment pane to reflect default
	@SuppressWarnings("serial")
	public void updateInstrumentSettingsItems() throws IOException
	{
		//Update transient table
		transientTable.setModel(new DefaultTableModel(
				ips.getTransientArray(),
				new String[] 
						{
					"Resolution", "Transient (ms)"
						}
				) {
			Class[] columnTypes = new Class[] {
					Integer.class, Integer.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});

		//Update other menu items
		scanOverheadSpinner.setValue(ips.scanOverhead);
		cTrapClearSpinner.setValue(ips.cTrapClearTime);
		polaritySwitchingTimeSpinner.setValue(ips.switchingTime);
		posNoiseSpinner.setValue(ips.posNoiseIntensity);
		negNoiseSpinner.setValue(ips.negNoiseIntensity);
		mzTolSpinner.setValue(ips.ppmTolerance);
		lipidsOnlyCheckBox.setSelected(ips.onlyLipids);
		minSNSpinner.setValue(ips.minMS2SN);
		minPIFSPinner.setValue(ips.minPIF);
		precursorIDCheckBox.setSelected(ips.precursorIDOnly);

		//Update single experiment menu items
		ms2AGCComboBox.setModel(new DefaultComboBoxModel<Integer>(ips.agcTargetArray));
		ms2ResComboBox.setModel(new DefaultComboBoxModel<Integer>(ips.getResolutionArray()));
		ms1ResComboBox.setModel(new DefaultComboBoxModel<Integer>(ips.getResolutionArray()));
	}

	//Updates menu items in single experiment pane to reflect default
	public static void updateSingleExperimentPaneButtons(String filename) throws IOException
	{

		mps = readDefaultMethodParameter(filename);

		if (mps.polaritySwitching)
			polaritySwitchingRadio.setSelected(true);
		else if (mps.polarity.equals("+"))
			posPolarityRadio.setSelected(true);
		else if (mps.polarity.equals("-"))
			negPolarityRadio.setSelected(true);

		ms1ResComboBox.setSelectedItem(mps.ms1Resolution);
		gradLengthSpinner.setValue(mps.gradientLength);
		ms1ITSpinner.setValue(mps.ms1InjectionTime);
		ms2ResComboBox.setSelectedItem(mps.ms2Resolution);
		ms2AGCComboBox.setSelectedItem(mps.ms2AGCTarget);
		ms2ITSpinner.setValue(mps.ms2InjectionTime);
		TopNSpinner.setValue(mps.negTopN);
		isolationSpinner.setValue(mps.isolationWidth);
		dynamicExclusionSpinner.setValue(mps.dynamicExclusion);

		if(mps.excludeIsotopes)
			excludeIsotopesComboBox.setSelectedItem("On");
		else
			excludeIsotopesComboBox.setSelectedItem("Off");

		if(mps.steppedCE)
			steppedCEComboBox.setSelectedItem("On");
		else
			steppedCEComboBox.setSelectedItem("Off");
	}

	//Sets button activation status when running
	private void setButtonStatus(Boolean status)
	{
		simFilesBatchTextField.setEnabled(status);
		mzxmlFileBatchTextBox.setEnabled(status);
		methodParamsBatchTextBox.setEnabled(status);
		mzxmlFileBatchBrowseButton.setEnabled(status);
		methodParamsBatchBrowseButton.setEnabled(status);
		numCoresBatchSpinner.setEnabled(status);
		batchStartButton.setEnabled(status);
		batchStopButton.setEnabled(!status);
		simFilesBatchBrowseButton.setEnabled(status);
		transientTable.setEnabled(status);
		scanOverheadSpinner.setEnabled(status);
		cTrapClearSpinner.setEnabled(status);
		polaritySwitchingTimeSpinner.setEnabled(status);
		btnAddNewRow.setEnabled(status);
		btnNewButton.setEnabled(status);
		btnSave.setEnabled(status);
		scrollPane.setEnabled(status);	
		separator_7.setEnabled(status);
		posNoiseSpinner.setEnabled(status);
		negNoiseSpinner.setEnabled(status);
		mzTolSpinner.setEnabled(status);
		lipidsOnlyCheckBox.setEnabled(status);
		separator_8.setEnabled(status);
		minSNSpinner.setEnabled(status);
		minPIFSPinner.setEnabled(status);
		simFilesTextField.setEnabled(status);
		mzxmlTextField.setEnabled(status);
		recentResultsTable.setEnabled(status);
		polaritySwitchingRadio.setEnabled(status);
		posPolarityRadio.setEnabled(status);
		negPolarityRadio.setEnabled(status);
		ms1ResComboBox.setEnabled(status);
		gradLengthSpinner.setEnabled(status);
		ms1ITSpinner.setEnabled(status);
		ms2ResComboBox.setEnabled(status);
		ms2AGCComboBox.setEnabled(status);
		ms2ITSpinner.setEnabled(status);
		TopNSpinner.setEnabled(status);
		isolationSpinner.setEnabled(status);
		dynamicExclusionSpinner.setEnabled(status);
		excludeIsotopesComboBox.setEnabled(status);
		steppedCEComboBox.setEnabled(status);
		methodSaveButton.setEnabled(status);
		methodResetButton.setEnabled(status);
		simFilesBrowseButton.setEnabled(status);
		mzxmlBrowseButton.setEnabled(status);
		runSimulationButton.setEnabled(status);
		singleExpResultsScroll.setEnabled(status);
		dutyCycleTextField.setEnabled(status);
		minAGCSpinner.setEnabled(status);
		btnReset.setEnabled(status);
		precursorIDCheckBox.setEnabled(status);
		moPolaritySwitchingRadio_1.setEnabled(status);
		moPolarityRadio_1.setEnabled(status);
		moNegPolarityRadio_1.setEnabled(status);
		moGradientLengthSpinner.setEnabled(status);
		moMS1ResCombo.setEnabled(status);
		moMS1MaxITSpinner.setEnabled(status);
		moMS2ResMinCombo.setEnabled(status);
		moMS2ITMinSpinner.setEnabled(status);
		moTopNMinSpinner.setEnabled(status);
		moIWMinSpinner.setEnabled(status);
		moDEMinSpinner.setEnabled(status);
		moExcludeIsotopesCombo.setEnabled(status);
		moSteppedCECombo.setEnabled(status);
		moTopNMaxSpinner.setEnabled(status);
		moTopNStepSpinner.setEnabled(status);
		moIWMaxSpinner.setEnabled(status);
		moIWStepSpinner.setEnabled(status);
		moMS2ResMaxCombo.setEnabled(status);
		moMS2ITMaxSpinner.setEnabled(status);
		moMS2ITStepSpinner.setEnabled(status);
		moDEMaxSpinner.setEnabled(status);
		moDEStepSpinner.setEnabled(status);
		moMinAGCMinCombo.setEnabled(status);
		moMinAgcMaxCombo.setEnabled(status);
		moSaveButton.setEnabled(status);
		moResetButton.setEnabled(status);
		moDuplicateSpinner.setEnabled(status);
		moMutationRateSpinner.setEnabled(status);
		moNumSurvivorsSpinners.setEnabled(status);
		moMinIDSpinner.setEnabled(status);
		moMaxPopDiffSpinner.setEnabled(status);
		moMaxBestDiffSpinner.setEnabled(status);
		moMinGenerationSpinner.setEnabled(status);
		moNumCoresSpinner.setEnabled(status);
		moStartButton.setEnabled(status);
		moStopButton.setEnabled(!status);
		moMaxDutyCycleSpinner.setEnabled(status);
		moSimFileFolderTextBox.setEnabled(status);
		moMZXMLFileTextBox.setEnabled(status);
		moMZXMLFileBrowseButton.setEnabled(status);
		moSimFileBrowseButton.setEnabled(status);
		ms2AGCMinSpinner.setEnabled(status);
		ms2AGCMaxSpinner.setEnabled(status);
	}

	//Update library generation status bar
	public static void updateGenerationProgress(JProgressBar progressBar, int progress, String message)
	{
		progressBar.setValue(progress);
		progressBar.setString(progress + message);
		Rectangle progressRect = progressBar.getBounds();
		progressRect.x = 0;
		progressRect.y = 0;
		progressBar.paintImmediately(progressRect);
	}
}

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class UserInterface {

	private JFrame frame;
	private JTextArea textArea;
	private File dataFile;
	private File headerFile;
	private JTextField dataFileField;
	private JTextField headerFileField;
	private JTextField minSupportTextField;
	private JTextField minConfidenceTextField;
	private Logic run;
	private JList<String> stableAttributesList;
	private JComboBox<String> decisionAttributeComboBox;
	private JComboBox<String> dToValueComboBox;
	private JComboBox<String> dInitialValueComboBox;
	private static final String SEPARATOR = System.getProperty("line.separator");

	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UserInterface window = new UserInterface();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
	}
	
	/**
	 * Create the application.
	 */
	public UserInterface() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 603, 585);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 321, 585, 217);
		frame.getContentPane().add(scrollPane);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		
		JButton btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boolean correctInput = true;
				textArea.append("Verifying Input...\n");
				
				HashSet<String> stable = new HashSet<String>();
				stable.addAll(stableAttributesList.getSelectedValuesList());		
				run.setStableFlexible(stable);
				
				if(stable.contains((String)decisionAttributeComboBox.getSelectedItem())){
					JOptionPane.showMessageDialog(null, "Decision attribute cannot be stable.", 
							"Decision attribute error", JOptionPane.ERROR_MESSAGE);
					correctInput = false;
				}
				
				try {
					if(Integer.parseInt(minSupportTextField.getText()) <= 0 || 
							Integer.parseInt(minConfidenceTextField.getText()) < 0) {
						correctInput = false;
						JOptionPane.showMessageDialog(null, "Support and confidence values must be greater than 0", 
								"Value error", JOptionPane.ERROR_MESSAGE);
					}
				}catch(NullPointerException err) {
					correctInput = false;
					JOptionPane.showMessageDialog(null, "Must enter support and confidence values", 
							"Value missing", JOptionPane.ERROR_MESSAGE);
				}catch(NumberFormatException err) {
					correctInput = false;
					JOptionPane.showMessageDialog(null, "Support and confidence values must be integers", 
							"Value error", JOptionPane.ERROR_MESSAGE);
				}
					
				if(correctInput) {
					run.setMinSupportConfidence(Integer.parseInt(minSupportTextField.getText()),
							Integer.parseInt(minConfidenceTextField.getText()));
					
					String decisionName = (String)decisionAttributeComboBox.getSelectedItem();
					textArea.append("Running LERs..." + SEPARATOR);
					run.runLers(decisionName + ((String)dInitialValueComboBox.getSelectedItem()),
							decisionName + (String)dToValueComboBox.getSelectedItem());
					
					String line;
					try(BufferedReader reader = new BufferedReader((new FileReader("output.txt")))) {
						while((line = reader.readLine()) != null) { 
							textArea.append(line + SEPARATOR);
						}
					
						textArea.append("Calculating action rules..." + SEPARATOR);
						run.calculateActionRules();
						run.printActionRules();		
						
						while((line = reader.readLine()) != null) { 
							textArea.append(line + SEPARATOR);
						}
					}catch(IOException e) {
						System.out.println(e.getMessage());
					}
				}				
			}
		});
		btnRun.setBounds(476, 283, 97, 25);
		frame.getContentPane().add(btnRun);
		
		JButton btnChooseDataFile = new JButton("Choose data file");
		btnChooseDataFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileFind = new JFileChooser();
				int returnVal = fileFind.showOpenDialog(null);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					dataFile = fileFind.getSelectedFile();
					dataFileField.setText(dataFile.getPath());
				}	
			}
		});
		btnChooseDataFile.setBounds(0, 0, 128, 25);
		frame.getContentPane().add(btnChooseDataFile);
		
		dataFileField = new JTextField();
		dataFileField.setEditable(false);
		dataFileField.setBounds(195, 1, 390, 22);
		frame.getContentPane().add(dataFileField);
		dataFileField.setColumns(10);
		
		JButton btnChooseAttributeName = new JButton("Choose attribute name file");
		btnChooseAttributeName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileFind = new JFileChooser();
				int returnVal = fileFind.showOpenDialog(null);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					headerFile = fileFind.getSelectedFile();
					headerFileField.setText(headerFile.getPath());
				}
				headerFileField.setText(headerFile.getPath());
			}
		});
		btnChooseAttributeName.setBounds(0, 26, 183, 25);
		frame.getContentPane().add(btnChooseAttributeName);
		
		headerFileField = new JTextField();
		headerFileField.setEditable(false);
		headerFileField.setColumns(10);
		headerFileField.setBounds(195, 27, 390, 22);
		frame.getContentPane().add(headerFileField);
		
		decisionAttributeComboBox = new JComboBox<String>();
		decisionAttributeComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if(arg0.getStateChange() == ItemEvent.SELECTED) {
					dInitialValueComboBox.removeAllItems();
					dToValueComboBox.removeAllItems();
					
					HashSet<String> distinctValues = run.getDistinctAttributeValues((String)arg0.getItem());
					
					for(String value : distinctValues) {
						dInitialValueComboBox.addItem(value);
						dToValueComboBox.addItem(value);
					}
					dInitialValueComboBox.setEnabled(true);
					dToValueComboBox.setEnabled(true);
				}else {
					dInitialValueComboBox.setEnabled(false);
					dToValueComboBox.setEnabled(false);
				}
			}
		});
		decisionAttributeComboBox.setBounds(0, 94, 153, 22);
		frame.getContentPane().add(decisionAttributeComboBox);
		
		JLabel lblChooseDecisionAttribute = new JLabel("Choose decision attribute: ");
		lblChooseDecisionAttribute.setBounds(0, 79, 153, 16);
		frame.getContentPane().add(lblChooseDecisionAttribute);
		
		JLabel lblInitialValue = new JLabel("Initial Value:");
		lblInitialValue.setBounds(235, 79, 72, 16);
		frame.getContentPane().add(lblInitialValue);
		
		JLabel lblEndValue = new JLabel("End Value:");
		lblEndValue.setBounds(420, 79, 62, 16);
		frame.getContentPane().add(lblEndValue);
		
		dInitialValueComboBox = new JComboBox<String>();
		dInitialValueComboBox.setBounds(235, 94, 88, 22);
		frame.getContentPane().add(dInitialValueComboBox);
		
		dToValueComboBox = new JComboBox<String>();
		dToValueComboBox.setBounds(420, 94, 88, 22);
		frame.getContentPane().add(dToValueComboBox);
		
		JLabel lblStableAttributes = new JLabel("Stable attributes:");
		lblStableAttributes.setBounds(0, 148, 99, 16);
		frame.getContentPane().add(lblStableAttributes);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(0, 166, 128, 142);
		frame.getContentPane().add(scrollPane_1);
		
		stableAttributesList = new JList<String>();
		scrollPane_1.setViewportView(stableAttributesList);
		
		JLabel lblMinimumSupport = new JLabel("Minimum Support:");
		lblMinimumSupport.setBounds(195, 148, 128, 16);
		frame.getContentPane().add(lblMinimumSupport);
		
		JLabel lblMinimumConfidence = new JLabel("Minimum Confidence:");
		lblMinimumConfidence.setBounds(195, 205, 128, 16);
		frame.getContentPane().add(lblMinimumConfidence);
		
		minSupportTextField = new JTextField();
		minSupportTextField.setBounds(335, 145, 44, 22);
		frame.getContentPane().add(minSupportTextField);
		minSupportTextField.setColumns(10);
		
		minConfidenceTextField = new JTextField();
		minConfidenceTextField.setToolTipText("Enter as a percentage value");
		minConfidenceTextField.setColumns(10);
		minConfidenceTextField.setBounds(335, 202, 44, 22);
		frame.getContentPane().add(minConfidenceTextField);
		
		JButton btnLoadFiles = new JButton("Load files...");
		btnLoadFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(!(dataFile == null) && !(headerFile == null)){
					if(dataFile.isFile() && headerFile.isFile()){
						textArea.append("Reading files..." + SEPARATOR);
						run = new Logic();
						run.readFile(headerFile, dataFile);
						textArea.append("Files read" + SEPARATOR);
						
						//set decision attribute choices
						initDecisionAttributes();
						//set stable attribute choices
						initStableAttributes();
					}else{
						JOptionPane.showMessageDialog(null, "Files could not be read. Please check files chosen.", 
								"File error", JOptionPane.ERROR_MESSAGE);
					}
				}else {
					JOptionPane.showMessageDialog(null, "Please choose a file for the attribute names and the data values.", 
							"File error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnLoadFiles.setBounds(476, 47, 109, 25);
		frame.getContentPane().add(btnLoadFiles);
		frame.setVisible(true);
	}

	/**
	 * Initialize all potential stable attributes in the list
	 */
	protected void initStableAttributes() {
		String[] attributeNames = (run.getAttributeNames().toArray(new String[0]));

		stableAttributesList.setListData(attributeNames);
		
	}

	/**
	 * Initializes all of the potential decision attribute values
	 */
	protected void initDecisionAttributes() {
		List<String> attributeNames = run.getAttributeNames();
		
		decisionAttributeComboBox.removeAllItems();
		
		for(String name : attributeNames) {
			decisionAttributeComboBox.addItem(name);
		}	
	}
}

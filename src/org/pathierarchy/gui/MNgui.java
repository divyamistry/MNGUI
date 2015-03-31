/**
 * @author Divya Mistry
 * Last Updated: 1/21/2010
 */
package org.pathierarchy.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.pathierarchy.xml.PathwaysToXML;

import edu.iastate.metnet.ExportHelper;
import edu.iastate.metnet.Network;
import edu.iastate.metnet.Organism;
import edu.iastate.metnet.Pathway;

/**
 * @author Divya
 * MNgui provides a UI to grab pathway class [ pathway {gene}] hierarchy 
 * for any of the available organisms in MetNetDB. MNgui uses MetNetAPI
 * to interface with MetNetDB.
 */
public class MNgui extends JFrame{
	private JPanel top_panel; //panel to hold all the ui elements
	private JPanel bot_panel; //panel to hold status bar
	private JPanel main_panel; //panel to house top & bottom panels
	private JButton genxmlbtn; //button to generate xml
	private JButton genxgmmlbtn; //button to generate xgmml files
	private JButton exitbtn; //button to exit from the program
	private JButton helpbtn; //button get some help
	private JComboBox listOfOrgs; //combobox showing list of available organismss
	private JCheckBox rnaCheckbox; //checkbox to select whether rna entities should be included in xml
	private JCheckBox geneCheckbox; //checkbox to select whether gene entities should be included in xml
	private JFileChooser fc; //file chooser to select the destination directory where xml will be stored
	private Vector<String> orgNames = new Vector<String>(); //vector to save the available organisms
	private String selectedOrg = null; //string to hold the name of organism chosen by user from combobox 
	private String genxmlbtnText = "Generate XML";
	private String genxgmmlbtnText = "Generate XGMML";
	private String dirloc = ".";
	/**
	 * constructor to display the UI
	 */
	public MNgui(){
		//populate the organism names
		for (Organism o:Organism.search()){
			this.orgNames.add(o.name);
		}
		
		//prepare the app window
		this.setTitle("MNgui");
		this.setSize(300, 132);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds((screen.width - 300)/2, (screen.height - 100)/2, 300, 132);
		//prepare the panel that will hold all the ui elements
		main_panel = new JPanel();
		main_panel.setLayout(new BorderLayout());
		top_panel = new JPanel();
		top_panel.setLayout(new GridLayout(4,2));
		bot_panel = new JPanel();
		bot_panel.setLayout(new GridLayout(1,2));
		
		//prepare file chooser to allow directory selection
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Select a Location to Save XML");
		fc.setApproveButtonText("Choose this Location");
		fc.setApproveButtonToolTipText("Finalize the location where XML file will be stored");
		
		//add generate xml button
		this.genxmlbtn = new JButton(this.genxmlbtnText);
		genxmlbtn.addActionListener(new ActionListener(){
			/*
			 * upon clicking the generate xml button:
			 *   - ask user to choose a directory where xml will be saved
			 *   - create the xml file
			 *   - show a confirmation dialogue that the file was created
			 */
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//if organism is not chosen from combobox, show an error message
				if (MNgui.this.selectedOrg == null){
					JOptionPane.showMessageDialog(MNgui.this, "Please select an organism from drop down menu", "Error", JOptionPane.ERROR_MESSAGE);
				} else { //if an organism was chosen
					//temporarily disable the generate xml button and indicate that xml generation is in progress
					MNgui.this.disableButtons();
					
					//prompt to choose a destination directory to store xml file
					int dirchosen = MNgui.this.fc.showOpenDialog(MNgui.this);
					if (dirchosen == JFileChooser.APPROVE_OPTION){
						try {
							//get the OS specific directory path
							MNgui.this.dirloc = fc.getSelectedFile().getCanonicalPath();
							
							//prepare the necessary file, and the tree that will hold PathwayClass -> Pathway -> gene hierarchy
							PathwaysToXML ptx = new PathwaysToXML(MNgui.this.dirloc, MNgui.this.selectedOrg,MNgui.this.rnaCheckbox.isSelected(),MNgui.this.geneCheckbox.isSelected(),false);
							//generate the tree that will hold PathwayClass -> Pathway -> gene hierarchy, and save that tree to the file
							ptx.generateXML();
							//show the confirmation that file was generated
							JOptionPane.showMessageDialog(MNgui.this, "XML files generated for " + MNgui.this.selectedOrg + " in\n" + MNgui.this.dirloc);
						} catch (IOException e) {
							System.err.println("Unable to access the directory location to retrieve CanonicalPath");
						}
					} else {
						System.out.println("Directory chooser cancelled by user");
					}
					//re-enable the button and reset the text as it should be
					MNgui.this.enableButtons();
				}
			}
		});
		this.genxgmmlbtn = new JButton (this.genxgmmlbtnText);
		genxgmmlbtn.addActionListener(new ActionListener(){
			/*
			 * (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 * Button action to fire xgmml creation
			 */
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (MNgui.this.selectedOrg == null) {
					JOptionPane.showMessageDialog(MNgui.this, "Please select an organism from drop down menu", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					//temporarily disable the generate xgmml button and indicate that xml generation is in progress
					MNgui.this.disableButtons();
					
					//allow user to pick which pathways to export as xgmml
					Vector<String> selPaths = invokePathwayPicker();
					if (selPaths.size()>0) { //user selected at least one pathway
						//prompt to choose a destination directory to store xml file
						int dirchosen = MNgui.this.fc.showOpenDialog(MNgui.this);
						if (dirchosen == JFileChooser.APPROVE_OPTION){
							try {
								//get the OS specific directory path
								MNgui.this.dirloc = fc.getSelectedFile().getCanonicalPath();
								//once the API starts giving correct output of toCytoscape() method, uncomment the following line
								this.prepXGMML(selPaths, dirloc + System.getProperty("file.separator"));
								//show the confirmation that file was generated
								JOptionPane.showMessageDialog(MNgui.this, "XGMML files generated for " + MNgui.this.selectedOrg + " in\n" + MNgui.this.dirloc);
							} catch (IOException e) {
								System.err.println("Unable to access the directory location to retrieve CanonicalPath");
							}
						} else {
							System.out.println("Directory chooser cancelled by user");
						}
					}
					//re-enable the buttons
					MNgui.this.enableButtons();
				}
			}
			
			/**
			 * method to create XGMML files for individual pathways
			 * @param pathwayNames names of pathways for which the xgmmls are to be created
			 * @param fname absolute path of the file where the xgmml is to be stored
			 */
			private void prepXGMML(Vector<String> pathwayNames, String fname) {
				Organism orgm = Organism.identify(MNgui.this.selectedOrg);
				for (int i=0; i<pathwayNames.size(); i++){
					ExportHelper.toCytoscape(new Network(Pathway.identify(pathwayNames.get(i), orgm)), 
											fname + pathwayNames.get(i) + ".xgmml.xml");
				}
			}
			
			/**
			 * Provide a UI to select the pathways a user would like to export to xgmml
			 * @return Vector of String containing the names of pathways chosen by the user
			 */
			private Vector<String> invokePathwayPicker(){
				//to store all the selected pathway names
				final Vector<String> chosenPaths = new Vector<String>();
				
				//prep the data model to show patway list
				final DefaultListModel listmod = new DefaultListModel();
				final JList list = new JList(listmod);
				final JScrollPane scroller = new JScrollPane(list);
				scroller.setMinimumSize(new Dimension(500, 100));
				list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				//populate the list of pathways
				for (Pathway p: Organism.identify(MNgui.this.selectedOrg).getPathways().toArray()){
					listmod.addElement(p.name);
				}
				
				//prep the window
				final JDialog pathwayPicker = new JDialog();
				pathwayPicker.setModalityType(ModalityType.APPLICATION_MODAL);
				pathwayPicker.setResizable(false);
				JPanel panel = new JPanel(new BorderLayout());
				pathwayPicker.setContentPane(panel);
				pathwayPicker.setDefaultCloseOperation(HIDE_ON_CLOSE);
				pathwayPicker.setTitle("Choose which pathways to export as XGMML...");
				pathwayPicker.setSize(500,200);
				
				//when user clicks "OK", prepare the list of pathway names and close the window
				JButton accept = new JButton("OK");
				accept.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						for (Integer i:list.getSelectedIndices()){
							chosenPaths.add((String) listmod.get(i));
						}
						pathwayPicker.setVisible(false); //close the window
					}
				});

				//add apropriate elements to the dialog window
				pathwayPicker.add(new JLabel("Select pathways to export..."));
				pathwayPicker.add(scroller, BorderLayout.NORTH);
				pathwayPicker.add(accept, BorderLayout.SOUTH);
				pathwayPicker.setVisible(true);
				
				return chosenPaths;
			}
		});
		//add the checkboxes to get user's choice on rna and/or gene entities
		this.rnaCheckbox = new JCheckBox("RNA", false);
		this.geneCheckbox = new JCheckBox("Gene", false);
		
		//add combobox with list of organisms
		listOfOrgs = new JComboBox(orgNames);
		listOfOrgs.setSelectedIndex(-1);
		listOfOrgs.addActionListener(new ActionListener() {
			//when user chooses an organism from the list, save the latest chosen organism
			@Override
			public void actionPerformed(ActionEvent e) {
				MNgui.this.selectedOrg = (String) MNgui.this.listOfOrgs.getSelectedItem();
			}
		});
		
		//add a help button
		helpbtn = new JButton("Help");
		helpbtn.addActionListener(new ActionListener() {
			//upon clicking the help button, show the basic steps to get a desired XML
			@Override
			public void actionPerformed(ActionEvent e) {
				String steps = "There are two main functions provided. \n" +
						"(A) Generate Ontology XML file for selected organism\n" +
						"(B) Generate XGMML file for pathways of selected organism.\n\n";
				
				steps += "To perform (A):\n==============\n" + 
						"1. Choose an organism from the list of available ones in the drop-down menu.\n" +
						"2. Choose which entities to add to the Ontology XML file.\n" +
						"3. Click on \"Generate XML\" button to generate Ontology XML file.\n" +
						"4. When prompted, select a folder to use for the generated XML file\n\n";
				
				steps += "To perform (B):\n=============\n" +
						"1. If not done so already, choose an organism from the list of available ones.\n" +
						"2. Click on \"Generate XGMML\" button. A new window should appear.\n" +
						"3. You can multi-select pathways from available pathways list.\n" +
						"4. Click \"OK\" to confirm the selection.\n" +
						"5. When prompted, select a folder to use for storing the generated XGMML file\n" +
						"If you wish to select all the pathways, press \n" +
						"Ctrl+A (on non-Mac) or Cmd+A (on Mac).";
				JOptionPane.showMessageDialog(MNgui.this, steps);
			}
		});
		
		//button to exit out of the application
		exitbtn = new JButton("Exit");
		exitbtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		
		//add all ui elements to the panel
		top_panel.add(new JLabel("Pick an Organism"));
		top_panel.add(listOfOrgs);
		top_panel.add(new JLabel("Entities to Include"));
		Panel chkbxPanel = new Panel(new BorderLayout());
		chkbxPanel.add(rnaCheckbox, BorderLayout.WEST);
		chkbxPanel.add(geneCheckbox, BorderLayout.EAST);
		top_panel.add(chkbxPanel);
		top_panel.add(genxgmmlbtn);
		top_panel.add(genxmlbtn);
		
		bot_panel.add(helpbtn);
		bot_panel.add(exitbtn);
		
		main_panel.add(bot_panel,BorderLayout.SOUTH);
		main_panel.add(top_panel,BorderLayout.NORTH);
		
		this.getContentPane().add(main_panel);
	}
	
	/**
	 * method to disable all the action buttons of the UI to show that app is in progress
	 */
	private void disableButtons(){
		this.exitbtn.setEnabled(false);
		this.helpbtn.setEnabled(false);
		this.genxgmmlbtn.setEnabled(false);
		this.genxmlbtn.setEnabled(false);
	}
	
	/**
	 * method to enable all the action buttons of the UI to show that app is ready for action
	 */
	private void enableButtons(){
		this.exitbtn.setEnabled(true);
		this.helpbtn.setEnabled(true);
		this.genxgmmlbtn.setEnabled(true);
		this.genxmlbtn.setEnabled(true);
	}
	
	/**
	 * @param args arguments used for nothing
	 * Method creates a ui and makes it visible
	 */
	public static void main(String[] args) {
		MNgui app = new MNgui();
		app.setVisible(true);
	}
}

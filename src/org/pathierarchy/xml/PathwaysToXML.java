package org.pathierarchy.xml;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.pathierarchy.data.PClassNode;
import org.pathierarchy.data.PClassTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.iastate.metnet.Entity;
import edu.iastate.metnet.EntityType;
import edu.iastate.metnet.ExportHelper;
import edu.iastate.metnet.Network;
import edu.iastate.metnet.Organism;
import edu.iastate.metnet.Pathway;
import edu.iastate.metnet.util.EntityVector;

/**
 * PathwaysToXML is used to create an XML file that has following hierarchy for a given organism
 *  -- pathway class
 *     |
 *     |-- pathway
 *     |   |
 *     |   |-- gene
 *     |   |
 *     |   |-- gene
 *     |    ...
 *     |-- pathway
 *     |   ...
 *  ...
 *  ...
 *  
 * @author Divya Mistry
 * last updated: 1/21/2010
 */
public class PathwaysToXML {
	//document, builder, and builder factory are all used to control the xml document
	private DocumentBuilderFactory dbfac;
	private DocumentBuilder docBuilder;
	private Document doc;
	//hold the root element of the xml dom structure
	private Element root;
	
	//transformer and transformer factory are used to control the tree structure resulting from processing XML doc
	private TransformerFactory transfac;
	private Transformer trans;
	
	//streamResult, file and filewriter are used to get the result of XML transformers and send it to a file
	private String dirloc; //location where all the files are to be created
	private File xmlfile;
	private FileWriter fw;
	private StreamResult result;
	
	//hold the DOM structure of XML that can later be spewed to the xml doc
	private DOMSource source;
	
	//hold the (pathway class -> pathway -> gene) hierarchy
	private PClassTree tree;
	
	//store the name of organism provided by user
	private String organism;
	
	//store the preference of which entities to include
	private boolean includeRNA;
	private boolean includeGene;
	
	//pereference to print empty pathway-classes in the hierarchy
	private boolean includeEmptyClasses;
	
	/**
	 * constructor
	 * @param dirLocation directory location where the generated xml file will be saved
	 * @param orgname name of the organism for which the pathway hierarchy is to be generated
	 * @param incrna if rna entities are to be included in xml set it to true, else false
	 * @param incgene if gene entities are to be included in xml set it to true, else false
	 * @param incemptyclasses if true, include empty pathways classes in xml, if false, do otherwise
	 */
	public PathwaysToXML(String dirLocation, String orgname, boolean incrna, boolean incgene, boolean incemptyclasses) {
		this.organism = orgname; // save the organism name for later use
		this.includeGene = incgene;
		this.includeRNA = incrna;
		this.includeEmptyClasses = incemptyclasses;
		this.dirloc = dirLocation;
		
		//prepare a Java File object with directory location and file name
//		this.xmlfile = new File(fileLocation,this.organism + new Date(Calendar.getInstance().getTimeInMillis()).toString() + ".xml");
		this.xmlfile = new File(this.dirloc,this.organism + Calendar.getInstance().getTimeInMillis() + ".xml");
		
		//create the document and all the transformers necessary to create String -> DOM -> XML file
		try {
			//create a new doc to hold content
			this.dbfac = DocumentBuilderFactory.newInstance();
			this.docBuilder = dbfac.newDocumentBuilder();
			this.doc = docBuilder.newDocument();
			
			/* 
			 * prepare the xml doc
			 */
			//prep the root of xml and add to the doc
			this.root = doc.createElement("Pathways");
			doc.appendChild(root);
			
			//transformers to conv xml to file writable stream
			this.transfac = TransformerFactory.newInstance();
			this.trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			
			//used to spew out xml to file
			this.fw = new FileWriter(this.xmlfile);
			this.result = new StreamResult(fw);
			this.source = new DOMSource(doc);
		} catch (Exception e){
			System.out.println("XML Document could not be successfully created in PathwaysToXML class.\n" +
					           "Please ensure the availability of the directory location and read/write permissions.\n");
		}
	}
	
	/**
	 * Add a Pathway Class to the DOM/XML hierarchy.
	 * Pathway Class XML elements are of form
	 * {@code <class name="Name Of Pathway Class">}
	 * @param parent XML element under which the new PathwayClass is to be added 
	 * @param name name of the pathway class
	 * @return child XML element that just got created for the given parent
	 */
	private Element addClass(Element parent, String name){
		//create an element for the document
		Element child = doc.createElement("class");
		//set the name attribute to the name of the pathway class
		child.setAttribute("name", name);
		//add the newly created class under given parent class
		parent.appendChild(child);
		return child;
	}
	
	/**
	 * Add a Pathway to the DOM/XML hierarchy
	 * Pathway XML elements are of form
	 * {@code <pathway name="Name of Pathway">}
	 * @param parent XML element under which the new Pathway is to be added
	 * @param name name of the pathway to be added
	 * @return the pathway XML element that just got created for given parent
	 */
	private Element addPathway(Element parent, String name){
		//create an element for the document
		Element child = doc.createElement("pathway");
		//set the name attribute of pathway to the given pathway name
		child.setAttribute("name", name);
		//add the newly created pathway under given parent
		parent.appendChild(child);
		return child;
	}
	/**
	 * Add a Gene to the DOM/XML hierarchy
	 * Gene XML elements are of form
	 * {@code <gene name="Name of Gene">}
	 * @param parent XML element under which the new Gene is to be added
	 * @param name name of the gene to be added
	 * @return the gene XML element that just got created for given parent
	 */
	private Element addGene(Element parent, String name){
		//create an element in the document
		Element child = doc.createElement("gene");
		//set the name attribute of gene to the given gene name
		child.setAttribute("name", name);
		//add the newly created gene under given parent
		parent.appendChild(child);
		return child;
	}
	
	/**
	 * Add an RNA to the DOM/XML hierarchy
	 * RNA XML elements are of form
	 * {@code <rna name="Name of RNA">}
	 * @param parent XML element under which the new RNA is to be added
	 * @param name name of the RNA to be added
	 * @return the RNA XML element that just got created for given parent
	 */
	private Element addRNA(Element parent, String name){
		//create an element in the document
		Element child = doc.createElement("rna");
		//set the name attribute of gene to the given gene name
		child.setAttribute("name", name);
		//add the newly created gene under given parent
		parent.appendChild(child);
		return child;
	}
	
	/**
	 * Method to create the XML file that contains the 
	 * (Pathway Class -> Pathway -> Gene) hierarchy
	 * for the currently chosen organism
	 */
	public void generateXML(){
		//prepare the hierarchy tree in the memory
		this.tree = new PClassTree(this.organism);
		
		//iterate through the tree to prepare the DOM/XML structure
		print_xmltree(this.tree.buildTree(),this.root);
		
		//spew out the document content to a file
		try {
			trans.transform(source, result);
			this.fw.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	/**
	 * Method to create XGMML files for each of the
	 * pathways in this organism. The file names are
	 * of format <pathway_name>.xgmml.xml
	 */
	public void generateXGMML(){
		Organism org = Organism.identify(this.organism);
		Pathway[] allpaths = org.getPathways().toArray();
		
		//cycle through all the paths and generate xgmml
		for (Pathway p:allpaths){
			ExportHelper.toCytoscape(new Network(p), this.dirloc + System.getProperty("file.separator") + p.name + ".xgmml.xml");
//			new ExportXGMML(new Network(p), this.dirloc + System.getProperty("file.separator") + p.name + ".xgmml.xml");
		}
	}

	/**
	 * Method to create XGMML files for given pathways
	 * in currently chosen organism. The file names are
	 * of format <pathway_name>.xgmml.xml
	 * 
	 * @param paths pathways for which XGMML files will be generated
	 */
	public void generateXGMMLs(Pathway[] paths){
		Organism orgm = Organism.identify(this.organism);
		
		//cycle through all the paths and generate xgmml
		for (Pathway p:paths){
			ExportHelper.toCytoscape(new Network(orgm.getPathways().get(p.id)), this.dirloc + System.getProperty("file.separator") + p.name + ".xgmml.xml");
//			new ExportXGMML(new Network(orgm.getPathways().get(p.id)), this.dirloc + "\\" + p.name + ".xgmml.xml");
		}
	}
	
	/**
	 * Method to create XGMML file for the given network
	 * in currently chosen organism. The file names are
	 * of format <network_name>.xgmml.xml
	 * @param nw network for which XGMML file will be generated
	 */
	public void generateXGMML (Network nw) {
		ExportHelper.toCytoscape(nw, this.dirloc + System.getProperty("file.separator") + "Network" + Calendar.getInstance().getTimeInMillis() + ".xgmml.xml");
//		new ExportXGMML(nw, this.dirloc + "\\" + "Network" + Calendar.getInstance().getTimeInMillis() + "xgmml.xml");
	}
	
	/**
	 * Iterate through a given hierarchy tree and prepare XML document
	 * @param treeRoot Pathway class whose subclasses and pathways are to be looked at
	 * @param xmlParent XML element under which other subelements are to be added
	 */
	private void print_xmltree(PClassNode treeRoot, Element xmlParent){
		//if this pathway class has its own child pathways
		// (i.e. not child pathways of its subclasses), print them
		if (treeRoot.getHasPaths() && treeRoot.getChildPathwayIds() != null){
			for (Integer pId:treeRoot.getChildPathwayIds()){ 
				Pathway path = new Pathway(pId);
				Element childPathway = this.addPathway(xmlParent, path.name);
				
				//print RNAs
				if (this.includeRNA){
					EntityVector rnaEntities = path.getEntities(EntityType.RNA);
					if (rnaEntities.size()>0) {
						//for every matching entity, add it to the dom hierarchy tree
						for (Entity e:rnaEntities.toArray()) {
							this.addRNA(childPathway, e.name);
						}
					}
				}
				
				//print Genes
				if (this.includeGene){
					EntityVector geneEntities = path.getEntities(EntityType.GENE);
					if (geneEntities.size()>0){
						for (Entity e:geneEntities.toArray()){
							this.addGene(childPathway, e.name);
						}
					}
				}
			}
		}
		//if this pathway class has subclasses, iterate through their hierarchies as well 
		if (treeRoot.getChildClasses() != null){
			for (PClassNode node:treeRoot.getChildClasses()){
				//if user chose to allow printing of empty pathway-classes
				//  include them in the printing hierarchy
				if (this.includeEmptyClasses){
					//print the class name
					Element childClass = this.addClass(xmlParent, node.getClassName());
					//check its children for pathways
					print_xmltree(node,childClass);
				}
				//only add pathway class to xml if it has been flagged 
				//  to contain pathways somewhere in its subtree
				else if (node.getHasPaths()){
					//print the class name
					Element childClass = this.addClass(xmlParent, node.getClassName());
					//check its children for pathways
					print_xmltree(node,childClass);
				}
			}
		}
	}
	
	public static void main(String[] args){
		if (args.length < 5){
			System.out.println("Usage: PathwaysToXML <dirloc> <orgname> <incRNA> <incGene> <incEmptyClasses>\n");
			System.out.println("\t<dirloc>  - directory location where ontology xml is to be stored\n" +
					           "\t\t  (e.g. c:\\temp)\n" +
					           "\t<orgname> - name of the organism for which ontology xml is to be generated\n" +
					           "\t\t  (e.g. Vitis)\n" +
					           "\t<incRNA>  - include RNA entities in the ontology xml\n" +
					           "\t\t  (true to include RNA, false otherwise)\n" +
					           "\t<incGene> - include Gene entities in the ontology xml\n" +
					           "\t\t  (true to include Genes, false otherwise)\n" +
					           "\t<incEmptyClasses> - include PathwayClasses without any child pathways\n" +
					           "\t\t  (true to include such pathway classes, false otherwise)\n");
		} else {
			PathwaysToXML ptx = new PathwaysToXML(args[0],args[1],Boolean.parseBoolean(args[2]),Boolean.parseBoolean(args[3]),Boolean.parseBoolean(args[4]));
			ptx.generateXML();
		}
	}
}

package org.pathierarchy.data;

import edu.iastate.metnet.Organism;
import edu.iastate.metnet.Pathway;
import edu.iastate.metnet.PathwayClass;


/**
 * a tree to maintain the pathwayClass->pathways hierarchy
 * based on selected organism
 * @author Divya Mistry
 *
 */
public class PClassTree {
	private PClassNode root; //root of the tree
	private Organism orgm; //organism for which the hierarchy is to be built
	
	/**
	 * constructor to initialize a tree with given organism
	 * @param org name of the organism in MetNetDB. e.g. "Vitis"
	 */
	public PClassTree(String org){
		this.orgm = Organism.identify(org);
		//make the root node with a default node from which all the major 
		//   pathway classes will branch
		this.root = new PClassNode(-1,null);
	}
	
	/**
	 * build the initialized tree for given organism
	 * @return root of the tree that was built to represent the pathwayclass->pathway hierarchy
	 */
	public PClassNode buildTree(){
		//Do there exist pathways that have not been classified in any of the pathway classes yet?
		//  if so, they need to be added to the root of the tree right here. 
		
		//build tree with all the main pathway classifications
		buildTree(PathwayClass.search(),this.root);
		return this.root;
	}
	/**
	 * build the subtree of node (i.e. pathway class) represented by given node <i>root</i>
	 * @param allRoots subclasses of the pathway class being looked at
	 * @param root node of the hierarchy tree whose subtree is to be built
	 * @return true if pathway class or any of its subclasses have pathways, false otherwise.
	 */
	private boolean buildTree(PathwayClass[] allRoots, PClassNode root){
		//of all the subclasses, currNode holds the class that will be processed
		PClassNode currNode = null;
		//flag to indicate whether the subtree under current node has any pathways
		boolean treeFlag = false;
		
		//iterate through all the subclasses to find pathways in their subtrees
		for (int i=0; i<allRoots.length; i++){
			//flag to indicate if there are pathways that belong to current subclass
			boolean subtreeFlag = false;
			//add the subclass to the tree as a child of given root node
			currNode = new PClassNode(allRoots[i].id,allRoots[i].name);
			root.addChildPathwayClass(currNode);
			//look for all the pathways that belong to current subclass 
			Pathway[] pwys = allRoots[i].getPathways(this.orgm).toArray();
			//if current node (i.e. pathway class) has pathways, indicate that in the node
			if (pwys.length > 0){
				for (Pathway p:pwys){
					currNode.addPathway(p.id);
				}
				//mark the node for having pathways
				currNode.setHasPaths(true);
				subtreeFlag = true;
			}
			//get all the subclasses of current class
			PathwayClass[] children = allRoots[i].getChildren();
			if (children != null && children.length>0){
				//build hierarchy for each of the subclasses
				subtreeFlag = buildTree(children, currNode);
				//if current subclass doesn't have pathways, 
				//  set the pathway status based on subclasses of 
				//  current subclass. So, if any of the subclasses in
				//  the subtree of current class have pathways, we would
				//  like to indicate that in the tree.
				if (!currNode.getHasPaths()){
					currNode.setHasPaths(subtreeFlag);
				}
			}
			//if any of the subclasses of current class have pathways
			//  indicate the existence of pathways in subtree for current class
			if (subtreeFlag){ treeFlag = true; }
		}
		return currNode==null ? false : treeFlag;
	}
}
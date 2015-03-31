package org.pathierarchy.data;

import java.util.Vector;


/**
 * node of a tree to hold a Pathway Class
 * @author Divya
 *
 */
public class PClassNode {
	private String pclassName; //pathway class name from db
	private Vector<PClassNode> childClasses; //hold all the subclasses of this class
	private Vector<Integer> childPathwayIds; //hold ids of pathways under this class
	private Boolean hasPaths; //indicator to indicate if class or any of its subclasses have pathways
	
	/**
	 * constructor to create a node for pathway class
	 * @param id Id number of pathway class (usually comes from MetNetDB)
	 * @param name name of the pathway class
	 */
	public PClassNode(Integer id, String name){
		pclassName = name;
		childClasses = null;
		childPathwayIds = null;
		hasPaths = false;
	}
	
	/**
	 * Accessor method to get the class name represented by this node 
	 * @return name of the class
	 */
	public String getClassName(){
		return this.pclassName;
	}
	
	/**
	 * Accessor method to get the subclasses of class represented by this node
	 * @return Vector of nodes holding all the subclasses
	 */
	public Vector<PClassNode> getChildClasses(){
		return this.childClasses;
	}
	
	/**
	 * Accessor method to get pathway ids of pathways under the class represented by this node 
	 * @return Vector of Integer pathway ids if there are any pathways in this class, null otherwise
	 */
	public Vector<Integer> getChildPathwayIds(){
		return this.childPathwayIds;
	}
	
	/**
	 * Check if this pathway class has any pathways in it 
	 * @return true if class has pathways in it, false otherwise
	 */
	public Boolean getHasPaths(){
		return this.hasPaths;
	}
	/**
	 * Indicate if this pathway class has any pathways in it
	 * @param status true if this pathway class has any pathways in it, false otherwise
	 */
	public void setHasPaths(Boolean status) {
		this.hasPaths = status;
	}
	
	/**
	 * Basic string representation of the node indicated by name of the pathway class
	 *   and if there are any pathways in it
	 */
	public String toString(){
		return "{Node: " + this.pclassName + " (hasPaths: " + this.hasPaths + ")}";
	}
	
	/**
	 * Add a pathway to the pathway class in hierarchy
	 * @param id pathwayId of the pathway to be added
	 */
	public void addPathway(Integer id){
		if (this.childPathwayIds == null){
			this.childPathwayIds = new Vector<Integer>();
		}
		this.childPathwayIds.add(id);
	}

	/**
	 * add a child pathwayclass to the class in hierarchy
	 * @param pcn subclass to be added to the current class
	 */
	public void addChildPathwayClass(PClassNode pcn){
		if (this.childClasses == null){
			this.childClasses = new Vector<PClassNode>();
		}
		this.childClasses.add(pcn);
	}
}

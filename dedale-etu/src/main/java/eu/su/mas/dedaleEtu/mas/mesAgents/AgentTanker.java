package eu.su.mas.dedaleEtu.mas.mesAgents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import dataStructures.tuple.Tuple3;
import dataStructures.tuple.Tuple4;
import eu.su.mas.dedale.env.EntityCharacteristics;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.mapElements.LockElement.LockType;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.mesBehaviours.TankerBehaviours.TankerFSMBehaviour;
import eu.su.mas.dedaleEtu.mas.mesBehaviours.PrintColor;
import eu.su.mas.dedaleEtu.princ.ConfigurationFile;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 *
 */


public class AgentTanker extends AbstractDedaleAgent {

	private static final long serialVersionUID = 6630930876590801476L;
	private MapRepresentation myMap;

	private HashMap<String, List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>>> treasures = new HashMap<>(); // Type de trésor : <Position, Date, Quantity, <LockPicking, Strength, LockIsOpen>>
	HashMap<String, Location> mesVoisins = new HashMap<String, Location> ();
	private List<String> visitedPos = new ArrayList<>(); // liste des noeuds visités

	private String myEntityType = null;
	
	private boolean isMoving = false;
	private boolean isPassed = false;
	private String forbidden_pos = "NULL";
	private boolean isProperlyPositioned = false;
	private String myPos = "NULL";
	
	private boolean isWaiting = false;
	
	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
		
		//get the parameters added to the agent at creation (if any)
		final Object[] args = getArguments();
		
		List<String> list_agentNames=new ArrayList<String>();
		
		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				list_agentNames.add((String)args[i]);
				i++;
			}
		}
		
		// Get the type of the agent
		Object [] obj = AbstractDedaleAgent.loadEntityCaracteristics(this.getLocalName(), ConfigurationFile.INSTANCE_CONFIGURATION_ENTITIES);
		Object ec = null;
		
		
		for (Object o : obj) {
			if (o instanceof EntityCharacteristics) {
				ec = o;
				break;
			}
		}
		
		this.myEntityType = ((EntityCharacteristics) ec).getMyEntityType().toString();
		//System.out.println("\n*** * * * ENTITY CHAR : " + this.myEntityType);

		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the behaviours
		 * 
		 ************************************************/
		
		// lb.add(new MonExploCoopBehaviour(this,this.myMap,list_agentNames));
		
		
		lb.add(new TankerFSMBehaviour(this,list_agentNames));
		
		
		/***
		if (this.myEntityType.equals("AgentCollect")) {
			lb.add(new CollectFSMBehaviour(this,list_agentNames,this.myMap));
		}
		***/
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		
		addBehaviour(new StartMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	
	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){
		super.takeDown();
	}

	protected void beforeMove(){
		super.beforeMove();
		//System.out.println("I migrate");
	}

	protected void afterMove(){
		super.afterMove();
		//System.out.println("I migrated");
	}
	
	public String getMyEntityType() {
		return myEntityType;
	}
	
	public MapRepresentation getMyMap() {
		return myMap;
	}

	public void setMyMap(MapRepresentation myMap) {
		this.myMap = myMap;
	}
	
	public boolean getIsWaiting() {
		return isWaiting;
	}
	
	public void setIsWaiting(boolean isWaiting) {
		this.isWaiting = isWaiting;
	}
	
	public List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>> getTreasures(String type) {
		return this.treasures.get(type);
	}
	
	public void setTreasures(String type, String pos, Long time, Integer quantity, Integer lockPicking, Integer strength, Boolean lockIsOpen) {	   
		Tuple3<Integer, Integer, Boolean> modalite = new Tuple3<>(lockPicking, strength, lockIsOpen);
		Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>> tuple_rec = new Tuple4<>(pos, time, quantity, modalite); // received tuple
	    
	    if (!this.treasures.containsKey(type)) { // Le type n'est pas dans le dico
	    	List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>> list = new ArrayList<>();
	    	list.add(tuple_rec);
	    	
	    	this.treasures.put(type, list);
	    	
	    } else {
	    	
		    boolean found = false;
		    
		    Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>> tuple = null;
		    List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>> list_data = this.treasures.get(type);
	    	
	    	for (Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>> data : list_data) {
			    
			    if (data.get_1().equals(pos)) { // un trésor est identifié par sa position
			    	found = true;
			    	tuple = data;
			    	
			    	break;
			    }
			    
			}
			
			if (!found) { // Le trésor n'est pas dans le dico
				list_data.add(tuple_rec);
				//PrintColor.print(this.getLocalName(), "Successful add !");
				
			} else {
				
				if (!tuple_rec.equals(tuple)) {
					list_data.remove(tuple);
					
					Integer new_quantity = tuple.get_3();
					Long new_time = tuple.get_2();
					Boolean new_lockIsOpen = tuple.get_4().getThird();
					
					if (quantity != new_quantity) {
						if (time > new_time) { // on garde l'info la plus récente
							new_quantity = quantity;
							new_time = time;
						}
					}
					
					if (!lockIsOpen.equals(new_lockIsOpen)) {
						if (time > new_time) { // on garde l'info la plus récente
							new_lockIsOpen = lockIsOpen;
							new_time = time;
						}
					}
					
					Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>> new_tuple = new Tuple4<>(pos, new_time, new_quantity, modalite);
					list_data.add(new_tuple);
					
					
					//PrintColor.print(this.getLocalName(), "Successful MAJ !");
					
				} // sinon, on ne fait rien
				
			}
	    }
		
	}
	
	public void mergeTreasures(HashMap<String, List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>>> treasures_rec) {
		
		for (String key : treasures_rec.keySet()) {
			List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>> list = treasures_rec.get(key);
			
			for (Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>> tuple : list) {
				this.setTreasures(key, tuple.get_1(), tuple.get_2(), tuple.get_3(), tuple.get_4().getFirst(), tuple.get_4().getSecond(), tuple.get_4().getThird());
				
			}
			
		}
		
	}
	
	public HashMap<String, List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>>> getTreasures() {
		return treasures;
	}
	
	public List<String> toListPos(Observation obs) {
		List<String> list_pos = new ArrayList<>();
		
		for (String key : this.treasures.keySet()) {
			if ((key.equals(obs.getName())) || (obs.equals(Observation.ANY_TREASURE))) {
				List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>> list= (List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>>) this.treasures.get(key);
				
				for (Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>> elem : list) {
					list_pos.add(elem.get_1());
				}
			}
		}
		
		return list_pos;
	}
	
	public String getForbiddenPos () {
		return forbidden_pos;
	}
	
	public void setForbiddenPos (String forbidden_pos) {
		this.forbidden_pos = forbidden_pos;
	}
	
	public boolean getIsProperlyPos () {
		return isProperlyPositioned;
	}
	
	public void setIsProperlyPos () {
		this.isProperlyPositioned = true;
	}
	
	public String getMyPos () {
		return myPos;
	}
	
	public void setMyPos (String pos) {
		myPos = pos;
	}
	
	public boolean getIsMoving() {
		return isMoving;
	}
	
	public void setIsMoving(boolean move) {
		this.isMoving = move;
	}
	
	public boolean getIsPassed() {
		return isPassed;
	}
	
	public void setIsPassed(boolean isPassed) {
		this.isPassed = isPassed;
	}
	
	public void setMesVoisins(HashMap<String, Location> mesVoisins) {
		this.mesVoisins = mesVoisins;
	}
	
	public HashMap<String, Location> getMesVoisins() {
		return mesVoisins;
	}
	
	public List<String> getVisitedPos () {
		return visitedPos;
	}
	
	public void addVisitedPos (String pos) {
		this.visitedPos.add(pos);
	}
	
	public void setVisitedPos(List<String> visited_pos) {
		this.visitedPos = visited_pos;
	}
	
	public void resetVisitedPos () {
		this.visitedPos = new ArrayList<>();
	}
	
	
}
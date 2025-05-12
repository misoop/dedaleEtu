package eu.su.mas.dedaleEtu.mas.mesAgents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import dataStructures.tuple.Couple;
import dataStructures.tuple.Tuple3;
import dataStructures.tuple.Tuple4;
import eu.su.mas.dedale.env.EntityCharacteristics;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.mesBehaviours.PrintColor;
import eu.su.mas.dedaleEtu.mas.mesBehaviours.CollectBehaviours.CollectFSMBehaviour;
import eu.su.mas.dedaleEtu.princ.ConfigurationFile;
import jade.core.behaviours.Behaviour;

/**
 *
 */


public class AgentCollect extends AbstractDedaleAgent {

	private static final long serialVersionUID = 6630930876590801476L;
	private MapRepresentation myMap;
	
	private List<String> mesReceivers = new ArrayList<String>(); // Nom
	private HashMap<String, Location> mesVoisins = new HashMap<>(); // Nom : Position
	private HashMap<String, List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>>> treasures = new HashMap<>(); // Type de trésor : <Position, Date, Quantity, <LockPicking, Strength>>

	private String myEntityType = null;
	private Set<Couple<Observation, Integer>> myExpertise = null;
	private boolean exploDone = false;
	private boolean blocked = false;
	
	private List<String> metAfterExplo = new ArrayList<>(); // agents met once the exploration is done
	private List<String> visitedPos = new ArrayList<>(); // liste des trésors visitiés | pour le Tanker: liste des noeuds visités
	private List<String> toVisit = new ArrayList<>();
	
	private List<Couple<String, Location>> tankerPos = new ArrayList<>();
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
		this.myExpertise = ((AbstractDedaleAgent) this).getMyExpertise();
		//System.out.println("MON EXPERTISE : " + this.myExpertise);

		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the behaviours
		 * 
		 ************************************************/
		
		// lb.add(new MonExploCoopBehaviour(this,this.myMap,list_agentNames));
		
		
		lb.add(new CollectFSMBehaviour(this,list_agentNames));
		
		
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
	
	public boolean getExploDone() {
		return exploDone;
	}
	
	public void setExploDone() {
		this.exploDone = true;
	}
	
	public boolean getIsBlocked() {
		return blocked;
	}
	
	public void setIsBlocked(boolean blocked) {
		this.blocked = blocked;
	}
	
	public boolean getIsWaiting() {
		return isWaiting;
	}
	
	public void setIsWaiting(boolean isWaiting) {
		this.isWaiting = isWaiting;
	}
	
	public Set<Couple<Observation, Integer>> getMonExpertise() {
		return myExpertise;
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
	
	public List<String> toListPos(Observation myTreasureType) {
		List<String> list_pos = new ArrayList<>();
		List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>> list= null;
		
		if (this.treasures.containsKey(myTreasureType.getName())) {
			list = this.treasures.get(myTreasureType.getName());
		}
		
		if (list != null) {
			for (Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>> elem : list) {
				if (elem.get_3() != 0) {
					if (elem.get_4().getThird()) {
						list_pos.add(elem.get_1());
						
					} else {
						
						Integer mylockpicking = null;
						Integer mystrength = null;
						
						for (Couple<Observation, Integer> c : this.myExpertise) {
							Observation o = c.getLeft();
							Integer v = c.getRight();
							
							if (o.equals(Observation.LOCKPICKING)) {
								mylockpicking = v;
							}
							
							if (o.equals(Observation.STRENGH)) {
								mystrength = v;
							}
						}
						
						if (mylockpicking >= elem.get_4().getFirst()) {
							if (mystrength >= elem.get_4().getSecond()) {
								list_pos.add(elem.get_1());
							}
						}
					}
					
				}
				
			}
			
		}
		
		
		return list_pos;
	}
	
	
	public List<String> toListPosTogether() {
		List<String> list_pos = new ArrayList<>();
		
		for (String key : this.treasures.keySet()) {
			for (Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>> elem : this.treasures.get(key)) {
				if (elem.get_3() != 0) {
					Integer mylockpicking = null;
					Integer mystrength = null;
					for (Couple<Observation, Integer> c : this.myExpertise) {
						Observation o = c.getLeft();
						Integer v = c.getRight();
						
						if (o.equals(Observation.LOCKPICKING)) {
							mylockpicking = v;
						}
						
						if (o.equals(Observation.STRENGH)) {
							mystrength = v;
						}
					}
					
					if (mylockpicking >= elem.get_4().getFirst()) {
						if (mystrength >= elem.get_4().getSecond()) {
							list_pos.add(elem.get_1());
						}
					}
					
					
				}
				
			}
		}
		
		
		
		
		return list_pos;
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
	
	public List<Couple<String, Location>> getTankerPos () {
		return tankerPos;
	}
	
	public void setTankerPos (Couple<String, Location> tank) {
		if (!this.tankerPos.contains(tank)) {
			PrintColor.print(this.getLocalName(), "Je rajoute le tank de position : " + tank.getRight().getLocationId());
			this.tankerPos.add(tank);
		}
	}
	
	public List<String> getToVisit (Observation myTreasureType) {
		
		for (String visited_pos : visitedPos) {
			if (toVisit.contains(visited_pos)) {
				toVisit.remove(visited_pos);
			}
		}
		
		if (toVisit.size() == 0) { // on recommence le parcours
			toVisit = toListPos(myTreasureType);
			
			if (toVisit.size() == 0) { // ils ne peuvent plus collecter tout seul
				toVisit = toListPosTogether();
				
				if (toVisit.size() == 0) {
					PrintColor.print(this.getLocalName(), "Pas assez d'infos.");
					
				} else {
					PrintColor.print(this.getLocalName(), "Il faut se mobiliser pour ramasser les trésors.");
				}
				
				
			} else {
				PrintColor.print(this.getLocalName(), "Il reste des trésors que je peux ramasser.");
			}
			
			resetVisitedPos();
		}
		
		return toVisit;
	}
	
	
	public List<String> getMetAfterExplo () {
		return metAfterExplo;
	}
	
	public void setMetAfterExplo (String agentName) {
		this.metAfterExplo.add(agentName);
	}
	
	
	public void setMesVoisins(HashMap<String, Location> mesVoisins) {
		this.mesVoisins = mesVoisins;
	}
	
	public HashMap<String, Location> getMesVoisins() {
		return mesVoisins;
	}
		
	
	public void setMesReceivers(List<String> mesReceivers) {
		this.mesReceivers = mesReceivers;
	}
	
	public List<String> getMesReceivers() {
		return mesReceivers;
	}
	
}
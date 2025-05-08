package eu.su.mas.dedaleEtu.mas.mesAgents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import dataStructures.tuple.Couple;
import dataStructures.tuple.Tuple3;
import dataStructures.tuple.Tuple4;
import eu.su.mas.dedale.env.EntityCharacteristics;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.mesBehaviours.ExploBehaviours.ExploFSMBehaviour;
import eu.su.mas.dedaleEtu.princ.ConfigurationFile;
import jade.core.behaviours.Behaviour;


public class AgentExplo extends AbstractDedaleAgent {

	private static final long serialVersionUID = 6630930876590801476L;
	private MapRepresentation myMap;
	
	private HashMap<String, Couple<MapRepresentation, Boolean>> dico = new HashMap<>(); // agentName : <Map, ExploDone>
	private List<String> mesReceivers = new ArrayList<String>(); // Nom
	private HashMap<String, Location> mesVoisins = new HashMap<>(); // Nom : Position
	private HashMap<String, List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>>> treasures = new HashMap<>(); // Type de trésor : <Position, Date, Quantity, <LockPicking, Strength, LockIsOpen>>

	private String myEntityType = null;
	
	private boolean exploDone = false;
	private boolean isBlocked = false;
	
	private List<String> metAfterExplo = new ArrayList<>(); // agents met once the exploration is done
	private List<String> visitedPos = new ArrayList<>(); // liste des trésors visitiés | pour le Tanker: liste des noeuds visités
	private List<String> toVisit = new ArrayList<>();
	
	private boolean isWaiting = false;
	private List<Couple<String, Location>> tankerPos = new ArrayList<>();
	
	
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
		
		
		lb.add(new ExploFSMBehaviour(this,list_agentNames));
	
		
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
	
	public boolean getIsWaiting() {
		return isWaiting;
	}
	
	public void setIsWaiting(boolean isWaiting) {
		this.isWaiting = isWaiting;
	}
	
	public boolean getIsBlocked() {
		return isBlocked;
	}
	
	public void setIsBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}
	
	
	public List<String> getToVisit (Observation obs) {
		
		for (String visited_pos : visitedPos) {
			if (toVisit.contains(visited_pos)) {
				toVisit.remove(visited_pos);
			}
		}
		
		if (toVisit.size() == 0) { // on recommence le parcours
			toVisit = toListPos(obs);
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
	
	public List<Couple<String, Location>> getTankerPos () {
		return tankerPos;
	}
	
	public void setTankerPos (Couple<String, Location> tank) {
		if (!this.tankerPos.contains(tank)) {
			this.tankerPos.add(tank);
		}
	}
	
	
	public HashMap<String, Couple<MapRepresentation, Boolean>> getMyDico() {
		return dico;
	}

	public void setMyDico() {
		
		// mettre a jour le dictionnaire
		for (Couple<MapRepresentation, Boolean> c : this.dico.values()) {
			MapRepresentation a_map = c.getLeft();
			
			List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this).observe();//myPosition
			Location myPosition=((AbstractDedaleAgent)this).getCurrentPosition();
			
			//1) remove the current node from openlist and add it to closedNodes.
			a_map.addNode(myPosition.getLocationId(), MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNodeId=null;
			Iterator<Couple<Location, List<Couple<Observation, String>>>> iter=lobs.iterator();
			
			while(iter.hasNext()){
				Location accessibleNode=iter.next().getLeft();
				boolean isNewNode=a_map.addNewNode(accessibleNode.getLocationId());
				//the node may exist, but not necessarily the edge
				if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
					a_map.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
					if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
				}
			}
		}	
		
	}
	
	
	public void setEntry(String a, MapRepresentation map) {
		MapRepresentation a_map = this.dico.get(a).getLeft();
		a_map.mergeMap(map.getSerializableGraph());
	}
	
	
	public void setEmptyInDico (String a) {
		Couple<MapRepresentation, Boolean> c = new Couple<>(new MapRepresentation(false), false);
		this.dico.put(a, c);
	}
	
	public boolean getExploDoneInDico (String a) {
		return this.dico.get(a).getRight();
	}
	
	public void setExploDoneInDico (String a) {
		Couple<MapRepresentation, Boolean> c = null;
		
		if (this.dico.containsKey(a)) {
			c = new Couple<>(this.dico.get(a).getLeft(), true);
		} else {
			c = new Couple<>(new MapRepresentation(false), true);
		}
		
		
		this.dico.put(a, c);
	}
}


package eu.su.mas.dedaleEtu.mas.mesBehaviours.ExploBehaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import dataStructures.tuple.Tuple3;
import dataStructures.tuple.Tuple4;
import eu.su.mas.dedale.env.EntityType;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.mesAgents.AgentExplo;
import eu.su.mas.dedaleEtu.mas.mesAgents.AgentExplo;
import eu.su.mas.dedaleEtu.mas.mesAgents.AgentExplo;
import eu.su.mas.dedaleEtu.mas.mesAgents.AgentExplo;
import eu.su.mas.dedaleEtu.mas.mesAgents.AgentExplo;
import eu.su.mas.dedaleEtu.mas.mesBehaviours.PrintColor;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


public class ExploObserveBehaviour extends SimpleBehaviour {
	
	private static final long serialVersionUID = -7858304553402842695L;
	private MapRepresentation myMap;
	private boolean finished = false;
	private int exit;
	
	public ExploObserveBehaviour(Agent a) {
		super(a);
	}
	
	
	public void action() {		

		String myEntityType = ((AgentExplo) this.myAgent).getMyEntityType().toString();
		
		//System.out.println("\n" + this.myAgent.getLocalName() + ": J'observe...\n");
		
		//List of observable from the agent's current position
		List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
		
		//List<String> accessible_nodes = new ArrayList<>(); // liste des noeuds accessibles
	
		List<String> mesReceivers = new ArrayList<>();
		
		HashMap<String, Location> mesVoisins = new HashMap<>(); // mes voisins actuellement
		HashMap<String, Location> mesVoisinsPrec = ((AgentExplo) this.myAgent).getMesVoisins(); // mes voisins d'avant
		
		String type = null;
		Integer quantity = null;
		Integer lockPicking = null;
		Integer strength = null;
		Boolean lockIsOpen = null;
		
		boolean isWaiting = ((AgentExplo) this.myAgent).getIsWaiting();
		
		if (this.myMap == null) {
			((AgentExplo) this.myAgent).setMyMap(new MapRepresentation());
		}
		
		this.myMap = ((AgentExplo) this.myAgent).getMyMap();
		
		receiveMsgTankerPos();
		
		List<Couple<String, Location>> list_tank = ((AgentExplo) this.myAgent).getTankerPos();
		
		
		String tankerName = "NULL";
		String tankerPos = "NULL";
		
		if (list_tank.size() != 0) {
			for (Couple<String, Location> tank : list_tank) {
				// il faudrait prendre le plus proche
				tankerName = tank.getLeft();
				tankerPos = tank.getRight().getLocationId();
			}
		}
		
		if (isWaiting) {
			receiveMsgBlocked();
		}
		
		lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
		
		exit = 1; // par défaut	
		
		// Parcourt les observations
		for (Couple<Location,List<Couple<Observation,String>>> o: lobs)  {
			Location loc = o.getLeft();
			List<Couple<Observation,String>> list = o.getRight();
			
			
			//System.out.println("Localisations observées par " + this.myAgent.getLocalName() + " : " + loc);
			//PrintColor.print(this.myAgent.getLocalName(), "Localisations observées : " + list);		
			
			for (Couple<Observation,String> c: list) {
				Observation obs = c.getLeft();
				String val = c.getRight();
				
				//System.out.println("	| Observation : " + obs);
				//System.out.println("	| Valeur : " + val);
				
				if ((obs.getName().equals("AgentName"))) {
					
					if (val.equals(EntityType.WUMPUS.getName())) {
						
					} else {
						mesVoisins.put(val, loc);
						
						// System.out.println("	| Agent à proximité de "+ this.myAgent.getLocalName() +" : " + val + ", Position : " + loc);
						//PrintColor.print(this.myAgent.getLocalName(), "Agent à côté : " + val + ", Position : " + loc);
						
						if (mesVoisinsPrec.containsKey(val)) {
							if (mesVoisinsPrec.get(val).equals(loc)) { // la position precedente et courante est la meme
								//PrintColor.print(this.myAgent.getLocalName(), "Position pareille !");
								
								boolean isBlocked = ((AgentExplo) this.myAgent).getIsBlocked();
								
								if (!isBlocked) {
									((AgentExplo) this.myAgent).setIsBlocked(true);
								}
								
								break;
								
							}
						
						}

						receiveMsgExploDone();
						
						if (!isWaiting) {
							mesReceivers = manageMsgExplo(val, myEntityType, mesReceivers, loc);		
							manageMsgTreasures(val);
						}
						
					}

				}		
					
				// MAJ des infos sur les trésors
				if ((obs.equals(Observation.GOLD)) || (obs.equals(Observation.DIAMOND))) {
					type = obs.getName();
					quantity = Integer.parseInt(val);
					
				}
				
				if (obs.equals(Observation.LOCKPICKING)) {
					lockPicking = Integer.parseInt(val);
				}
				
				if (obs.equals(Observation.STRENGH)) {
					strength = Integer.parseInt(val);
					
				}
				
				if (obs.equals(Observation.LOCKSTATUS)) {
					lockIsOpen = Boolean.parseBoolean(val);
				}
				
				
			}
			
			
			if (list.size() == 4) {

				// Mise a jour de la liste des trésors
				Long currentTimeMillis = System.currentTimeMillis();
				((AgentExplo) this.myAgent).setTreasures(type, loc.getLocationId(), currentTimeMillis, quantity, lockPicking, strength, lockIsOpen);
			
				HashMap<String, List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>>> treasures = ((AgentExplo) this.myAgent).getTreasures();
				//PrintColor.print(this.myAgent.getLocalName(), "Liste des trésors : " + treasures);
			
			}
			
			if (isWaiting) {
				Location curr_pos = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
				
				if ((loc.getLocationId().equals(tankerPos)) && (!loc.equals(curr_pos))) {
					if (list.size() == 0) { // la voie est libre
						((AbstractDedaleAgent)this.myAgent).moveTo(loc);
						((AgentExplo) this.myAgent).setIsWaiting(false);
						PrintColor.print(this.myAgent.getLocalName(), "La voie est libre, je prends la place du Tanker et je n'attends plus.");
						
					}
				}
				
			}
			
		}

		
		((AgentExplo) this.myAgent).setMesReceivers(mesReceivers);
		((AgentExplo) this.myAgent).setMesVoisins(mesVoisins);
		//System.out.println("\n" + this.myAgent.getLocalName() + " : Mes voisins -> " + ((AgentExplo) this.myAgent).getMyEntityType() + "; Equals ? -> " + ((((AgentExplo) this.myAgent).getMyEntityType().equals("AgentExplo"))));

		//System.out.println("\n" + this.myAgent.getLocalName() + " : Entity Type -> " + mesVoisins + "; Taille -> " + mesVoisins.size());
		
		// PrintColor.print(this.myAgent.getLocalName(), "receivers : " + mesReceivers + mesReceivers.size());
		// PrintColor.print(this.myAgent.getLocalName(), "vrais receivers : " + ((AgentExplo) this.myAgent).getMesReceivers());
		
		isWaiting = ((AgentExplo) this.myAgent).getIsWaiting();
		
		if (isWaiting) {
			exit = 0;
		}
		
		if (mesReceivers.size() > 0) {
			exit = 2;
		} 
		
		//PrintColor.print(this.myAgent.getLocalName(), "exit = " + exit);
		
		finished = true;
	}
	
	
	public List<String> manageMsgExplo (String val, String myEntityType, List<String> mesReceivers, Location loc) {

		// Envoi d'un message : son entityType
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO-ENTITY"); // protocole pour envoyer son entityType
		msg.setSender(this.myAgent.getAID());
		
		msg.addReceiver(new AID(val,AID.ISLOCALNAME));
		
		String ec = ((AgentExplo) this.myAgent).getMyEntityType().toString();
		
		try {					
			msg.setContent(ec);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//System.out.println(this.myAgent.getLocalName() + ": J'envoie un message ENTITY_TYPE");
		PrintColor.print(this.myAgent.getLocalName(), ">>> J'envoie un message ENTITY_TYPE à " + val);
		
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		
		
		// Vérifie la réception du message d'un type d'agent (à chaque pas)
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO-ENTITY"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		
		//while ((msgReceived = this.myAgent.receive(msgTemplate)) != null) {
		if (msgReceived != null) {

			String senderName = msgReceived.getSender().getLocalName();
			String sgreceived=null;
			try {
				sgreceived = (String)msgReceived.getContent();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//System.out.println(this.myAgent.getLocalName() + ": J'ai reçu un message ENTITY_TYPE");
			PrintColor.print(this.myAgent.getLocalName(), "<<< J'ai reçu un message ENTITY_TYPE de " + senderName + " : " + sgreceived);
			
			if (sgreceived.equals(EntityType.AGENT_COLLECTOR.getName())) { // si le msg provient d'un COLLECT
				mesReceivers.add(senderName);
				//PrintColor.print(this.myAgent.getLocalName(), "Je vais envoyer ma carte");
				
				if (!(((AgentExplo) this.myAgent).getExploDone())) {
					if ( !(((AgentExplo) this.myAgent).getMyDico().containsKey(senderName)) ) { // 1st meet
						//System.out.println("+++ " + this.myAgent.getLocalName() + ": Ajout d'un nouvel agent dans mon dico - " + senderName);
						PrintColor.print(this.myAgent.getLocalName(), "Ajout d'un nouvel agent dans mon dico : " + senderName);
						
						((AgentExplo) this.myAgent).setEmptyInDico(senderName);
						((AgentExplo) this.myAgent).setEntry(senderName, this.myMap);
						
					} 
					
					//System.out.println("~~~ " + this.myAgent.getLocalName() + ": Merge le dico d'un agent - " + senderName);
					//PrintColor.print(this.myAgent.getLocalName(), "Merge le dico d'un agent : " + senderName);		

				}
			}
			
			List<Couple<String, Location>> list_tank = ((AgentExplo) this.myAgent).getTankerPos();
			
			String tankerName = "NULL";
			String tankerPos = "NULL";
			
			if (list_tank.size() != 0) {
				for (Couple<String, Location> tank : list_tank) {
					// il faudrait prendre le plus proche
					tankerName = tank.getLeft();
					tankerPos = tank.getRight().getLocationId();
				}
			}
				
			if (sgreceived.equals(EntityType.AGENT_TANKER.getName())) {
				if (tankerPos.equals("NULL")) {
					Couple<String, Location> c = new Couple<>(senderName, loc);
					((AgentExplo) this.myAgent).setTankerPos(c);
				}
				
				// ((AgentExplo) this.myAgent).setBlocked(false);
				
			} else {
				if (!tankerPos.equals("NULL")) {
					sendMsgTankerPos(tankerName, tankerPos, val);
				}
			}

		}
		
		
		
		return mesReceivers;
		
	}
	
	
	public void manageMsgTreasures (String val) { 
		
		HashMap<String, List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>>> treasures = ((AgentExplo) this.myAgent).getTreasures();
		
		if (!treasures.isEmpty()) {
			
			// Envoi de la carte de trésors
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("SHARE-TREASURES");
			msg.setSender(this.myAgent.getAID());
			
			msg.addReceiver(new AID(val,AID.ISLOCALNAME));
			
			
			try {					
				msg.setContentObject(treasures);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//System.out.println(this.myAgent.getLocalName() + ": J'envoie un message ENTITY_TYPE");
			PrintColor.print(this.myAgent.getLocalName(), "***> J'envoie un message TREASURES à " + val);
			
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			
			
			// Vérifie la réception du message d'un type d'agent (à chaque pas)
			MessageTemplate msgTemplate=MessageTemplate.and(
					MessageTemplate.MatchProtocol("SHARE-TREASURES"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
			
			//while ((msgReceived = this.myAgent.receive(msgTemplate)) != null) {
			if (msgReceived != null) {

				String senderName = msgReceived.getSender().getLocalName();
				HashMap<String, List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>>> sgreceived=null;
				try {
					sgreceived = (HashMap<String, List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>>>) msgReceived.getContentObject();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//System.out.println(this.myAgent.getLocalName() + ": J'ai reçu un message ENTITY_TYPE");
				PrintColor.print(this.myAgent.getLocalName(), "<*** J'ai reçu un message TREASURES de " + senderName);
				PrintColor.print(this.myAgent.getLocalName(), "Message recu : " + sgreceived);
				
				
				PrintColor.print(this.myAgent.getLocalName(), "Avant la MAJ : " + treasures);
				((AgentExplo) this.myAgent).mergeTreasures(sgreceived);
				
				HashMap<String, List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>>> treasures2 = ((AgentExplo) this.myAgent).getTreasures();
				
				PrintColor.print(this.myAgent.getLocalName(), "Après la MAJ : " + treasures2);
			}
			
			
		}
	
		
	}
	
	private void sendMsgTankerPos(String tankerName, String tankerPos, String val) {
		// Envoi de la carte de trésors
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("TANKERPOS");
		msg.setSender(this.myAgent.getAID());
		
		msg.addReceiver(new AID(val,AID.ISLOCALNAME));
		
		ArrayList<String> l = new ArrayList<>();
		l.add(tankerName);
		l.add(tankerPos);
		
		
		try {					
			msg.setContentObject(l);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		PrintColor.print(this.myAgent.getLocalName(), "---> J'envoie un message TANKERPOS à " + val);
		
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		
	}
	
	
	public void receiveMsgMap() {
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		if (msgReceived!=null) {
			SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
			String senderName = msgReceived.getSender().getLocalName();
			try {
				sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//System.out.println(this.myAgent.getLocalName() + ": J'ai reçu un message SHARE-TOPO");
			PrintColor.print(this.myAgent.getLocalName(), "J'ai reçu un message SHARE-TOPO de " + senderName);
			this.myMap.mergeMap(sgreceived);
			
		}
	}

	
	public void receiveMsgExploDone() {
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-EXPLODONE"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		
		if (msgReceived != null) {

			String senderName = msgReceived.getSender().getLocalName();
			String sgreceived=null;
			try {
				sgreceived = (String) msgReceived.getContent();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			PrintColor.print(this.myAgent.getLocalName(), "<--- J'ai reçu un message EXPLODONE de " + senderName);
			PrintColor.print(this.myAgent.getLocalName(), "Message recu : " + sgreceived);
			
			((AgentExplo) this.myAgent).setExploDoneInDico(senderName);
			
			
		}
		
	}
	
	public void receiveMsgTankerPos() {
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("TANKERPOS"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		
		if (msgReceived != null) {

			String senderName = msgReceived.getSender().getLocalName();
			List<String> sgreceived=null;
			try {
				sgreceived = (List<String>) msgReceived.getContentObject();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			PrintColor.print(this.myAgent.getLocalName(), "<--- J'ai reçu un message TANKERPOS de " + senderName);
			PrintColor.print(this.myAgent.getLocalName(), "Message recu : " + sgreceived);
			
			Couple<String, Location> c = new Couple<>(sgreceived.getFirst(), new GsLocation(sgreceived.getLast()));
			
			((AgentExplo) this.myAgent).setTankerPos(c);
			
		}
		
	}
	
	public void receiveMsgBlocked() {
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("BLOCKED"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		
		if (msgReceived != null) {

			String senderName = msgReceived.getSender().getLocalName();
			String sgreceived=null;
			try {
				sgreceived = (String) msgReceived.getContent();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			PrintColor.print(this.myAgent.getLocalName(), "<--- J'ai reçu un message BLOCKED de " + senderName);
			PrintColor.print(this.myAgent.getLocalName(), "Message recu : " + sgreceived);
			
			// je dois me deplacer...
			List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
			List<String> available_pos_wagent = toListAvailablePosWAgent(lobs);
			
			if (available_pos_wagent.size() != 0) {
				PrintColor.print(this.myAgent.getLocalName(), "Je me décale.");
				((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation (available_pos_wagent.getFirst()));
				
				((AgentExplo) this.myAgent).setIsWaiting(false);
			}
			
		}
	}
	
	public List<String> toListAvailablePosWAgent(List<Couple<Location,List<Couple<Observation,String>>>> lobs) {
		
		List<String> lpos = new ArrayList<>();
		Location myPos = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
		List<String> visitedPos = ((AgentExplo) this.myAgent).getVisitedPos();
		
		//PrintColor.print(this.myAgent.getLocalName(), "ma position : " + myPos.getLocationId() + " | visités : " + visitedPos + " | lobs = " + lobs);
		
		for (Couple<Location,List<Couple<Observation,String>>> o: lobs) {
			Location loc = o.getLeft();
			List<Couple<Observation,String>> list = o.getRight();
			boolean something = false; // tresor ou agent
			
			if (!visitedPos.contains(loc.getLocationId())) { // pour ne pas faire d'aller-retour
				if (!loc.equals(myPos)) { // pas ma position
					
					for (Couple<Observation, String> c : list) {
						Observation obs = c.getLeft();
						
						if (obs.equals(Observation.DIAMOND) || obs.equals(Observation.GOLD) || obs.equals(Observation.AGENTNAME)) {
							something = true;
							break;
						}
					}
					
					if (!something) { // pas de tresors
						lpos.add(loc.getLocationId());
					}
				
				}
			}
			
		}
		
		
		return lpos;
	}
	


	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}
	
	@Override
	public int onEnd() {
		// TODO Auto-generated method stub
		return exit;
	}

	
	
}


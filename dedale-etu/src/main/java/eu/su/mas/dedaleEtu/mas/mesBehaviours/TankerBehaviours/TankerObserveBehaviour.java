package eu.su.mas.dedaleEtu.mas.mesBehaviours.TankerBehaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import dataStructures.tuple.Couple;
import dataStructures.tuple.Tuple3;
import dataStructures.tuple.Tuple4;
import eu.su.mas.dedale.env.EntityType;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.mesAgents.AgentTanker;
import eu.su.mas.dedaleEtu.mas.mesBehaviours.PrintColor;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


public class TankerObserveBehaviour extends SimpleBehaviour {
	
	private static final long serialVersionUID = -7858304553402842695L;
	private MapRepresentation myMap;
	private boolean finished = false;
	private int exit;
	
	public TankerObserveBehaviour(Agent a) {
		super(a);
		//this.myMap=mymap;
	}
	
	
	public void action() {

		String myEntityType = ((AgentTanker) this.myAgent).getMyEntityType().toString();
		HashMap<String, Location> mesVoisins = new HashMap<>(); // mes voisins actuellement
		boolean isWaiting = ((AgentTanker) this.myAgent).getIsWaiting();
		String myPos = ((AgentTanker) this.myAgent).getMyPos();
		boolean isProperlyPos = ((AgentTanker) this.myAgent).getIsProperlyPos();

		//List of observable from the agent's current position
		List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
		String current_pos = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition().getLocationId();
		
		List<String> available_pos = toListAvailablePos(lobs);
		
		exit = 0;
		
		if ((available_pos.size() >= 3) && (!isProperlyPos)) {
			PrintColor.print(this.myAgent.getLocalName(), "Initialement bien placé.");
			((AgentTanker) this.myAgent).setIsProperlyPos();
			((AgentTanker) this.myAgent).setMyPos(current_pos);
			
		} else {
			if (!isProperlyPos) {
				exit = 1;
			}
		}
		
		if (isProperlyPos) {
			// Parcourt les observations
			for (Couple<Location,List<Couple<Observation,String>>> o: lobs)  {
				Location loc = o.getLeft();
				List<Couple<Observation,String>> list = o.getRight();
				
				receiveMsgMove(); // check
				
				for (Couple<Observation,String> c: list) {
					Observation obs = c.getLeft();
					String val = c.getRight();
					
					if ((obs.getName().equals(Observation.AGENTNAME.getName()))) {
						
						if (!val.equals(EntityType.WUMPUS.getName())) {
							mesVoisins.put(val, loc);
							
							isWaiting = ((AgentTanker) this.myAgent).getIsWaiting();
							
							if (!isWaiting) {
								manageMsgExplo(val, myEntityType, loc);
								manageMsgTreasures(val);
								
							} 
							
							if (!((AgentTanker) this.myAgent).getIsMoving()) {
								if (isWaiting) {
									if ((!current_pos.equals(myPos)) && (loc.getLocationId().equals(myPos))) {
										//PrintColor.print(this.myAgent.getLocalName(), "Je vois quelqu'un à ma place.");
										((AgentTanker) this.myAgent).setIsPassed(true);
									}
								}
							}
						}
						
						
					}
				
				}
				
				if (!(((AgentTanker) this.myAgent).getIsMoving()) && (isWaiting) && ((AgentTanker) this.myAgent).getIsPassed()) {
					current_pos = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition().getLocationId();
					
					if ((!current_pos.equals(myPos)) && (loc.getLocationId().equals(myPos))) {
						if (list.size() == 0) {
							PrintColor.print(this.myAgent.getLocalName(), "Je peux retourner a ma place.");
							((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(myPos));
							((AgentTanker) this.myAgent).setIsWaiting(false);
							((AgentTanker) this.myAgent).setIsPassed(false);
							
							try {
								this.myAgent.doWait(400);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						
						
					}
				}
				
				
			}
			
			((AgentTanker) this.myAgent).setMesVoisins(mesVoisins);
		}
		
		if (((AgentTanker) this.myAgent).getIsMoving()) {
			exit = 1;
		}
		
		finished = true;
		
	} 


	public void manageMsgExplo (String val, String myEntityType, Location loc) {
		
		// Envoi d'un message : son entityType
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO-ENTITY"); // protocole pour envoyer son entityType
		msg.setSender(this.myAgent.getAID());
		
		msg.addReceiver(new AID(val,AID.ISLOCALNAME));
		
		String ec = ((AgentTanker) this.myAgent).getMyEntityType().toString();
		
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
			
			((AgentTanker)this.myAgent).setIsWaiting(true);
		}
	}
	
	
	public void manageMsgTreasures (String val) { 
		
		HashMap<String, List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>>> treasures = ((AgentTanker) this.myAgent).getTreasures();
		
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
				((AgentTanker) this.myAgent).mergeTreasures(sgreceived);
				
				HashMap<String, List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>>> treasures2 = ((AgentTanker) this.myAgent).getTreasures();
				
				PrintColor.print(this.myAgent.getLocalName(), "Après la MAJ : " + treasures2);
			}
			
			
		}
	
		
	}
	
	
	public void receiveMsgMove() {
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("MOVE"),
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
			
			HashMap<String, Location> mesVoisins = ((AgentTanker) this.myAgent).getMesVoisins();
			
			PrintColor.print(this.myAgent.getLocalName(), "<--- J'ai reçu un message MOVE de " + senderName);
			PrintColor.print(this.myAgent.getLocalName(), "Message recu : " + sgreceived);
			PrintColor.print(this.myAgent.getLocalName(), "Qui est à côté ? : " + mesVoisins);
			
			
			for (String a : mesVoisins.keySet()) {
				if (a.equals(senderName)) {
					((AgentTanker) this.myAgent).setIsMoving(true);
					((AgentTanker) this.myAgent).setForbiddenPos(sgreceived);
					break;
				}
			}	
			
		}
		
	}
	
	
	public List<String> toListAvailablePos(List<Couple<Location,List<Couple<Observation,String>>>> lobs) {
		
		List<String> lpos = new ArrayList<>();
		Location myPos = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
		List<String> visitedPos = ((AgentTanker) this.myAgent).getVisitedPos();
		
		//PrintColor.print(this.myAgent.getLocalName(), "ma position : " + myPos.getLocationId() + " | visités : " + visitedPos + " | lobs = " + lobs);
		
		for (Couple<Location,List<Couple<Observation,String>>> o: lobs) {
			Location loc = o.getLeft();
			List<Couple<Observation,String>> list = o.getRight();
			boolean something = false; // tresor ou agent
			
			if (!visitedPos.contains(loc.getLocationId())) { // pour ne pas faire d'aller-retour
				if (!loc.equals(myPos)) { // pas ma position
					
					for (Couple<Observation, String> c : list) {
						Observation obs = c.getLeft();
						
						if (obs.equals(Observation.DIAMOND) || obs.equals(Observation.GOLD)) {
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


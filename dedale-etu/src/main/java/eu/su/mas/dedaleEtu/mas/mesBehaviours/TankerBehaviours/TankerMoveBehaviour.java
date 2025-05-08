package eu.su.mas.dedaleEtu.mas.mesBehaviours.TankerBehaviours;

import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.mesAgents.AgentTanker;
import eu.su.mas.dedaleEtu.mas.mesBehaviours.PrintColor;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class TankerMoveBehaviour extends SimpleBehaviour {
	
	private static final long serialVersionUID = -4974690754221381792L;

	private boolean finished = false;
	private int exit;	

/**
 * 
 * @param myagent reference to the agent we are adding this behaviour to
 */
	public TankerMoveBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
		
	}

	@Override
	public void action() {
		
		List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
		boolean isProperlyPos = ((AgentTanker) this.myAgent).getIsProperlyPos();
		List<String> visitedPos = null;
		Location myPos = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
		String next_loc = null;
		String forbidden_pos = ((AgentTanker) this.myAgent).getForbiddenPos();
		List<String> available_pos = this.toListAvailablePos(lobs);
		PrintColor.print(this.myAgent.getLocalName(), "Ma position : " + myPos + " | Positions possibles = " + available_pos);
		
		
		if (!isProperlyPos) {
			PrintColor.print(this.myAgent.getLocalName(), "Je ne suis pas bien placé.");
			
			if ((available_pos.size() == 2) || (available_pos.size() == 1)) { // trou ou couloir
				PrintColor.print(this.myAgent.getLocalName(), "Je suis dans un trou/couloir");
				
				while ((available_pos.size() == 2) || (available_pos.size() == 1)) {
					visitedPos = ((AgentTanker) this.myAgent).getVisitedPos();
					myPos = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
					next_loc = null;
					String agentName = "";
					
					((AgentTanker) this.myAgent).addVisitedPos(myPos.getLocationId());
					
					// on evite les noeuds ou il y a un agent
					for (Couple<Location,List<Couple<Observation,String>>> o: lobs) {
						Location loc = o.getLeft();
						List<Couple<Observation,String>> list = o.getRight();
						
						if (available_pos.contains(loc.getLocationId())) {
							//PrintColor.print(this.myAgent.getLocalName(), "Possible ? : " + loc + list);
							
							if (list.size() == 0) {
								next_loc = loc.getLocationId();
								
							} else {
								for (Couple<Observation, String> obs : list) {
									Observation a = obs.getLeft();
									String val = obs.getRight();
									
									if (a.getName().equals("AgentName")) {
										agentName = val;
									}
								}
							}
							
						}
						
						if (next_loc != null) {
							break;
						}
					}
					
					if (next_loc == null) { // bloque dans tous les cotes par au moins 1 agent
						PrintColor.print(this.myAgent.getLocalName(), "Je suis bloqué par des agents");
						((AgentTanker) this.myAgent).setIsMoving(false); // pour rester dans Observe
						
						break;
						
					} else {
						((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(next_loc));
						
						lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
						available_pos = this.toListAvailablePos(lobs);
					}
				}
				
				myPos = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
				PrintColor.print(this.myAgent.getLocalName(), "J'ai atteri en " + myPos);
				
				// soit 0; soit >= 3
				
				if (available_pos.size() == 0) {
					visitedPos = ((AgentTanker) this.myAgent).getVisitedPos();
					int i = visitedPos.size() - 2; // avant dernier
					
					while (available_pos.size() == 0) {
						next_loc = available_pos.get(i);
						((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(next_loc));
				
						i = i - 1;
					}
					
					PrintColor.print(this.myAgent.getLocalName(), "Je suis revenu en " + ((AbstractDedaleAgent) this.myAgent).getCurrentPosition());
					
				} else { // >=3
					PrintColor.print(this.myAgent.getLocalName(), "Je suis dans un endroit OK");
					((AgentTanker) this.myAgent).setIsProperlyPos();
					((AgentTanker) this.myAgent).setMyPos(myPos.getLocationId());
					((AgentTanker) this.myAgent).resetVisitedPos();
				}
				
			} else {
				PrintColor.print(this.myAgent.getLocalName(), "Je suis complètement bloqué par des agents");
				
			}
		}
		
		
		if (((AgentTanker) this.myAgent).getIsMoving()) {
			PrintColor.print(this.myAgent.getLocalName(), "J'ai recu un message MOVE");
			//((AgentTanker) this.myAgent).setIsWaiting(true);
			
			lobs = ((AbstractDedaleAgent)this.myAgent).observe();
			List<String> available_pos_wagent = toListAvailablePosWAgent(lobs);
			
			if ((!forbidden_pos.equals("MOVE")) && (!forbidden_pos.equals("NULL"))) {
				PrintColor.print(this.myAgent.getLocalName(), "J'ai une position interdite");
				
				available_pos_wagent.remove(forbidden_pos);
				
				if (available_pos_wagent.size() != 0) {
					((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(available_pos_wagent.getFirst()));
					
					PrintColor.print(this.myAgent.getLocalName(), "Je me pousse en " + available_pos_wagent.getFirst());
					((AgentTanker) this.myAgent).setIsMoving(false);
					
				} else {
					PrintColor.print(this.myAgent.getLocalName(), "Help, je suis complètement bloqué par des agents");
				}
				
			} else {
				PrintColor.print(this.myAgent.getLocalName(), "Je peux me pousser où je veux");
				next_loc = null;
				List<String> mesVoisins = new ArrayList<>();
				
				// on evite les noeuds ou il y a un agent
				for (Couple<Location,List<Couple<Observation,String>>> o: lobs) {
					Location loc = o.getLeft();
					List<Couple<Observation,String>> list = o.getRight();
					
					if (available_pos.contains(loc.getLocationId())) {
						//PrintColor.print(this.myAgent.getLocalName(), "Possible ? : " + loc + list);
						
						if (list.size() == 0) {
							next_loc = loc.getLocationId();
						} else {
							for (Couple<Observation,String> c : list) {
								if (c.getLeft().equals(Observation.AGENTNAME)) {
									mesVoisins.add(c.getRight());
								}
							}
						}
						
					}
					
					if (next_loc != null) {
						break;
					}
				}
				
				if (next_loc != null) {
					PrintColor.print(this.myAgent.getLocalName(), "Je me pousse en " + next_loc);
					((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(next_loc));
					
					((AgentTanker) this.myAgent).setIsMoving(false);
					
				} else {
					PrintColor.print(this.myAgent.getLocalName(), "Je ne peux pas me pousser");
					for (String agentName : mesVoisins) {
						sendBlockedMsg(agentName);
					}
					
				}
				
			}
			
		} else {
			PrintColor.print(this.myAgent.getLocalName(), "Je n'ai pas recu de message MOVE");
		}
			
		
		exit = 1;
		finished = true;
		
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
	
	/***
	 * La meme chose mais sans les noeuds où il y a un agent
	 ***/
	public List<String> toListAvailablePosWAgent(List<Couple<Location,List<Couple<Observation,String>>>> lobs) {
		
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
	
	
	
	
	public void sendBlockedMsg(String agentName) {
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("BLOCKED");
		msg.setSender(this.myAgent.getAID());
		
		msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
		
		String blocked = "Blocked";
		
		try {					
			msg.setContent(blocked);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		PrintColor.print(this.myAgent.getLocalName(), "---> J'envoie un message BLOCKED à " + agentName);
		
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
	}
	

	@Override
	public boolean done() {
		return finished;
	}
	
	@Override
	public int onEnd() { // exécuté après que done() a renvoyé true
		// TODO Auto-generated method stub
		return exit;
	}
}

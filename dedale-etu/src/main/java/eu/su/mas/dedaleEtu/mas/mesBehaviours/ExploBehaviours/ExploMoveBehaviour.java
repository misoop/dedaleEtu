package eu.su.mas.dedaleEtu.mas.mesBehaviours.ExploBehaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import dataStructures.tuple.Tuple3;
import dataStructures.tuple.Tuple4;
import eu.su.mas.dedale.env.EntityType;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.mesAgents.AgentCollect;
import eu.su.mas.dedaleEtu.mas.mesAgents.AgentExplo;
import eu.su.mas.dedaleEtu.mas.mesAgents.AgentExplo;
import eu.su.mas.dedaleEtu.mas.mesBehaviours.PrintColor;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
//import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


/**
 * <pre>
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs. 
 * This (non optimal) behaviour is done until all nodes are explored. 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.
 * Warning, the sub-behaviour ShareMap periodically share the whole map
 * </pre>
 * @author hc
 *
 */
public class ExploMoveBehaviour extends SimpleBehaviour {
	
	private static final long serialVersionUID = -4974690754221381792L;

	private boolean finished = false;
	
	private int exit;	

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	//private List<String> list_agentNames;

/**
 * 
 * @param myagent reference to the agent we are adding this behaviour to
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public ExploMoveBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
		//this.myMap=myMap;
		
	}

	@Override
	public void action() {
		
		/***
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
			//this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent,500,this.myMap,list_agentNames));
			((AgentExplo) this.myAgent).setMyMap(this.myMap);
		}
		***/
		
		exit = 1;
		
		this.myMap = ((AgentExplo) this.myAgent).getMyMap();
		
		boolean exploDone = ((AgentExplo) this.myAgent).getExploDone();
		HashMap<String, List<Tuple4<String, Long, Integer, Tuple3<Integer,Integer,Boolean>>>> treasures = ((AgentExplo) this.myAgent).getTreasures();
		String myEntityType = ((AgentExplo) this.myAgent).getMyEntityType();
		
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
				
		//0) Retrieve the current position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			
			try {
				this.myAgent.doWait(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			String nextNodeId=null;
			
			if (!exploDone) {
				//1) remove the current node from openlist and add it to closedNodes.
				this.myMap.addNode(myPosition.getLocationId(), MapAttribute.closed);

				//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
				
				Iterator<Couple<Location, List<Couple<Observation, String>>>> iter=lobs.iterator();
				while(iter.hasNext()){
					Location accessibleNode=iter.next().getLeft();
					boolean isNewNode=this.myMap.addNewNode(accessibleNode.getLocationId());
					//the node may exist, but not necessarily the edge
					if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
						this.myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
						if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
					}
				}
				
				// chaque fois qu'on avance, je mets a jour le contenu de mon dico
				((AgentExplo) this.myAgent).setMyDico();
			}
			
			
			//3) while openNodes is not empty, continues.
			if (!this.myMap.hasOpenNode()){
				//Explo finished
				
				((AgentExplo) this.myAgent).setExploDone();
				//exit = 4; // RandomWalk
				// finished = true;
				
				//System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done.");
				//PrintColor.print(this.myAgent.getLocalName(), "Exploration successufully done.");
				
			}
			
			exploDone = ((AgentExplo) this.myAgent).getExploDone();
			
			//4) select next move.
			//4.1 If there exist one open node directly reachable, go for it,
			//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
			if (nextNodeId==null){
				//no directly accessible openNode
				//chose one, compute the path and take the first step.
				
				if (!exploDone) {
					// vers le noeud ouvert le plus proche
					nextNodeId=this.myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
					//System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode);
				
				} else {
					myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
					
					List<String> treasures_pos = getTreasuresPos(myEntityType, myPosition);
					
					PrintColor.print(this.myAgent.getLocalName(), "Liste de trésors finale : " + treasures_pos);
					
					if (treasures_pos.size() != 0) {
						PrintColor.print(this.myAgent.getLocalName(), "Curr_Pos : " + myPosition);
						nextNodeId = this.myMap.getShortestPathToClosestObject(myPosition.getLocationId(), treasures_pos).get(0);
					
					} else {
						if (nextNodeId == null) {
							List<Couple<Location,List<Couple<Observation,String>>>> l_obs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
							Random r= new Random();
							int moveId=1+r.nextInt(l_obs.size()-1);
							
							nextNodeId = l_obs.get(moveId).getLeft().getLocationId(); // random
						}
					}
					
				}
				
				if (nextNodeId.equals(tankerPos)) {
					PrintColor.print(this.myAgent.getLocalName(), "Il y a un tanker devant moi");
					((AgentExplo) this.myAgent).setIsWaiting(true);
					sendMsgMove(tankerName);
					
				} else {
				
					boolean isBlocked = ((AgentExplo) this.myAgent).getIsBlocked(); // ie nextNodeId renvoie toujours la meme position
					List<String> path = new ArrayList<>(); // liste de noeuds à ne pas prendre
					int dist = 0; 
					boolean foundFreeLoc = false;
					String agentName = null;
					
					if (isBlocked) {
						
						while (dist < 3) { // deplacement de 3 noeuds
							List<Couple<Location,List<Couple<Observation,String>>>> list_obs=((AbstractDedaleAgent)this.myAgent).observe();
							
							PrintColor.print(this.myAgent.getLocalName(), "Je cherche un endroit...");
							
							for (Couple<Location,List<Couple<Observation,String>>> o: list_obs)  {
								Location loc = o.getLeft();
								List<Couple<Observation,String>> list = o.getRight();
								
								// PrintColor.print(this.myAgent.getLocalName(), "Je regarde en " + loc.getLocationId());
								
								if ((list.size() == 0) && (!path.contains(loc.getLocationId())) && (!foundFreeLoc) && (!loc.getLocationId().equals(((AbstractDedaleAgent)this.myAgent).getCurrentPosition().getLocationId()))) {
									nextNodeId = loc.getLocationId();
									foundFreeLoc = true;
									
								} else {
									for (Couple<Observation,String> c: list) {
										Observation obs = c.getLeft();
										String val = c.getRight();
										
										if ((!(obs.getName().equals("AgentName"))) && (!path.contains(loc.getLocationId())) && (!foundFreeLoc) && (!loc.getLocationId().equals(((AbstractDedaleAgent)this.myAgent).getCurrentPosition().getLocationId()))) {
											nextNodeId = loc.getLocationId();
											foundFreeLoc = true;
										}
										
										if (obs.getName().equals("AgentName")) {
											agentName = val;
										}
										
									}
								}
								
								path.add(loc.getLocationId());
							}
							
							PrintColor.print(this.myAgent.getLocalName(), "Je vais en "+nextNodeId);
							
							if (foundFreeLoc) {
								((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId));
								
							} else {
								PrintColor.print(this.myAgent.getLocalName(), "ERREUR : je suis bloqué !");
								if (agentName != null) {
									sendBlockedMsg(agentName);
									break;
								}
								
							}
							
							dist = dist + 1;
							foundFreeLoc = false;
						}
						
						
						((AgentExplo) this.myAgent).setIsBlocked(false);
					}
				}

			}
			
			//5) At each time step, the agent check if he received a graph from a teammate. 	
			receiveMsgTopo();
			
			
			/***
			PrintColor.print(this.myAgent.getLocalName(), "***");
			for (String a : ((AgentExplo) this.myAgent).getMyDico().keySet()) {
				PrintColor.print(this.myAgent.getLocalName(), "Agent : " + a);
				PrintColor.print(this.myAgent.getLocalName(), ((AgentExplo) this.myAgent).getMyDico().get(a).getSerializableGraph().toString());
			}
			PrintColor.print(this.myAgent.getLocalName(), "***");
			***/
			
			boolean isWaiting = ((AgentExplo) this.myAgent).getIsWaiting();
			
			if (!isWaiting) {
				((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId));
			}
			
		}


		finished = true;
		
		
	}
	
	public List<String> getTreasuresPos (String myEntityType, Location myPosition) {
		// si c'est un agentExplo : vers le trésor le plus proche
		List<String>  treasures_pos = ((AgentExplo) this.myAgent).getToVisit(Observation.ANY_TREASURE);
		
		PrintColor.print(this.myAgent.getLocalName(), "Liste de trésors avant : " + treasures_pos);
		
		if (treasures_pos.contains(myPosition.getLocationId())) { // si je suis sur le trésor
			((AgentExplo) this.myAgent).addVisitedPos(myPosition.getLocationId());
			treasures_pos.remove(myPosition.getLocationId());	
			
			
			if (treasures_pos.size() == 0) {
				((AgentExplo) this.myAgent).resetVisitedPos();
				treasures_pos = ((AgentExplo) this.myAgent).getToVisit(Observation.ANY_TREASURE);

			}
			
			treasures_pos.remove(myPosition.getLocationId());	

		}
		
		
		return treasures_pos;
	}
	
	
	public void receiveMsgTopo () {
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
	
	public void sendMsgMove (String tankerName) {
		// Envoi d'un message
		ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
		msg2.setProtocol("MOVE"); // protocole pour envoyer son entityType
		msg2.setSender(this.myAgent.getAID());
		
		msg2.addReceiver(new AID(tankerName,AID.ISLOCALNAME));
		
		String ec2 = "MOVE";
		Location myPos = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
		List<String> path = null;
		
		if (this.myMap.hasOpenNode()) {
			try {
			    path = this.myMap.getShortestPathToClosestOpenNode(myPos.getLocationId());
			} catch (NoSuchElementException e) {
				PrintColor.print(this.myAgent.getLocalName(), "~ ~ ~ Je ne connais pas assez la carte !");
			}
		}
		
		
		if ((path != null) && (path.size() > 1)) { // le premier elem est là où y a le tanker
			ec2 = path.get(1); // le 2e elem est la où le tanker ne devrait pas aller
		}
		
		try {					
			msg2.setContent(ec2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		PrintColor.print(this.myAgent.getLocalName(), ">>> J'envoie un message MOVE à " + tankerName);
		
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg2);
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


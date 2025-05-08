package eu.su.mas.dedaleEtu.mas.mesBehaviours.ExploBehaviours;

import java.io.IOException;
import java.util.List;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.mesAgents.AgentExplo;
import eu.su.mas.dedaleEtu.mas.mesBehaviours.PrintColor;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

/**
 *
 */
public class ShareMapBehaviour extends SimpleBehaviour {
	
	private MapRepresentation myMap;
	//private Map<String, Location> receivers;
	
	private boolean finished = false;
	
	private int exit;

	
	public ShareMapBehaviour(Agent a) {
		super(a);
	}

	
	private static final long serialVersionUID = -568863390879327961L;


	@Override
	public void action() {
		
		this.myMap = ((AgentExplo) this.myAgent).getMyMap();
		
		List<String> list_voisins = ((AgentExplo) this.myAgent).getMesReceivers();
		
		//System.out.println("\n" + this.myAgent.getLocalName() + ": Je suis dans ShareMap...\n");
		//System.out.println("\n* * * " + this.myAgent.getLocalName() + ": " + list_voisins);
		//PrintColor.print(this.myAgent.getLocalName(), "Je suis dans ShareMap");
		
		//this.myMap = ((AgentExplo) this.myAgent).getMyMap(); // pour que la map != null
		// Map<String, SerializableSimpleGraph<String, MapAttribute>> myDico = ((AgentExplo) this.myAgent).getMyDico();

		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO"); // protocole pour partager ma map
		msg.setSender(this.myAgent.getAID());
		
		for (String agentName : list_voisins) {
			
			if (!((AgentExplo) this.myAgent).getExploDoneInDico(agentName)) {
				msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
				
				SerializableSimpleGraph<String, MapAttribute> sg=((AgentExplo) this.myAgent).getMyDico().get(agentName).getLeft().getSerializableGraph();
				
				//PrintColor.print(this.myAgent.getLocalName(), "Contenu du msg : " + sg.toString());
				
				if (sg.toString().equals("{}")) {
					PrintColor.print(this.myAgent.getLocalName(), "Dico vide");
					// on fait rien
					
				} else { // si c'est pas vide
					
					try {					
						msg.setContentObject(sg);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					//System.out.println(this.myAgent.getLocalName() + ": J'envoie un message SHARE-TOPO");
					PrintColor.print(this.myAgent.getLocalName(), "J'envoie un message SHARE-TOPO à " + agentName);
					
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
					
					((AgentExplo) this.myAgent).setEmptyInDico(agentName);
					
				}
				
				
			} else {
				PrintColor.print(this.myAgent.getLocalName(), agentName + " a fini son exploration.");
			}
		}
		
		exit = 2;
		finished = true;
		
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



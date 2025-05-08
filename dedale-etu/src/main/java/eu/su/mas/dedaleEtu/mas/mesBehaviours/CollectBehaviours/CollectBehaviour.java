package eu.su.mas.dedaleEtu.mas.mesBehaviours.CollectBehaviours;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import dataStructures.tuple.Tuple3;
import dataStructures.tuple.Tuple4;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.mesAgents.AgentCollect;
import eu.su.mas.dedaleEtu.mas.mesBehaviours.PrintColor;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;


class CollectBehaviour extends SimpleBehaviour {
	
	private static final long serialVersionUID = 7797723056022734911L;
	private boolean finished = true;
	private int exit;

	public CollectBehaviour(Agent a) {
		super(a);
		// TODO Auto-generated constructor stub
	}
	

	@Override
	public void action() {
		
		//Example to retrieve the current position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null && myPosition.getLocationId()!=""){
			
			List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			//System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);
			PrintColor.print(this.myAgent.getLocalName(), "Liste des obs : " + lobs);
			
			/***
			//Little pause to allow you to follow what is going on
			try {
			 	System.out.println("Press enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
				System.in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			***/
			
			//list of observations associated to the currentPosition
			List<Couple<Observation,String>> lObservations= lobs.get(0).getRight();
			//PrintColor.print(this.myAgent.getLocalName(), "Contenu : " + lObservations);

			//example related to the use of the backpack for the treasure hunt
			Boolean b=false;
			int picked=0;
			Integer lockpicking = null;
			Integer strength = null;
			String name = null;
			Integer quantity = null;

			
			for(Couple<Observation,String> o:lObservations){
				switch (o.getLeft()) {
				case DIAMOND:
					//System.out.println(this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
					PrintColor.print(this.myAgent.getLocalName(), "Mon treasure_type est : " + ((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
					//System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is :"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
					PrintColor.print(this.myAgent.getLocalName(), "Actuellement, ma capacité est : " + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
					//System.out.println(this.myAgent.getLocalName()+" - I try to open the safe : "+((AbstractDedaleAgent) this.myAgent).openLock(Observation.GOLD));
					//((AbstractDedaleAgent) this.myAgent).openLock(Observation.DIAMOND);
					//System.out.println(this.myAgent.getLocalName()+" - Value of the treasure on the current position : "+o.getLeft() +" : "+ o.getRight());
					picked = ((AbstractDedaleAgent) this.myAgent).pick();
					//System.out.println(this.myAgent.getLocalName()+" - The agent grabbed : "+picked);
					PrintColor.print(this.myAgent.getLocalName(), "J'ai ramassé : " + picked);
					//System.out.println(this.myAgent.getLocalName()+" - the remaining backpack capacity is : "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
					
					if (picked != 0) {
						b = true;
						name = Observation.GOLD.getName();
						quantity = Integer.parseInt(o.getRight()) - picked;
					}
					
					
				
				case GOLD:
					
					//System.out.println(this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
					PrintColor.print(this.myAgent.getLocalName(), "Mon treasure_type est : " + ((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
					//System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is :"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
					PrintColor.print(this.myAgent.getLocalName(), "Actuellement, ma capacité est : " + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
					//System.out.println(this.myAgent.getLocalName()+" - Value of the treasure on the current position : "+o.getLeft() +" : "+ o.getRight());
					picked = ((AbstractDedaleAgent) this.myAgent).pick();
					//System.out.println(this.myAgent.getLocalName()+" - The agent grabbed : "+picked);
					PrintColor.print(this.myAgent.getLocalName(), "J'ai ramassé : " + picked);
					//System.out.println(this.myAgent.getLocalName()+" - the remaining backpack capacity is : "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
					
					if (picked != 0) {
						b = true;
						name = Observation.GOLD.getName();
						quantity = Integer.parseInt(o.getRight()) - picked;
					}
					
					break;
				
				case LOCKPICKING:
					lockpicking = Integer.parseInt(o.getRight());
					
				case STRENGH:
					strength = Integer.parseInt(o.getRight());
					
				default:
					break;
				}
			}

			//If the agent picked (part of) the treasure
			if (b){
				List<Couple<Location,List<Couple<Observation,String>>>> lobs2=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
				//System.out.println("\n>>> State of the observations after picking "+lobs2+"\n");
				
				Long currentTimeMillis = System.currentTimeMillis();
				Boolean lockIsOpen = Boolean.TRUE;
				
				((AgentCollect) this.myAgent).setTreasures(name, myPosition.getLocationId(), currentTimeMillis, quantity, lockpicking, strength, lockIsOpen);

				HashMap<String, List<Tuple4<String, Long, Integer, Tuple3<Integer, Integer, Boolean>>>> treasures = ((AgentCollect) this.myAgent).getTreasures();
				PrintColor.print(this.myAgent.getLocalName(), "Liste des trésors : " + treasures);
				
				//Trying to store everything in the tanker
				//System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());
				//System.out.println(this.myAgent.getLocalName()+" - The agent tries to transfer is load into the Silo (if reachable); succes ? : "+((AbstractDedaleAgent)this.myAgent).emptyMyBackPack("Tank"));
				//System.out.println(this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace());
				
			} 

			
			
			//Random move from the current position
			Random r= new Random();
			int moveId=1+r.nextInt(lobs.size()-1);//removing the current position from the list of target to accelerate the tests, but not necessary as to stay is an action
			
			//The move action (if any) should be the last action of your behaviour
			//((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
			
			
			exit = 2;
			finished = true;
			//System.out.println("\n" + this.myAgent.getLocalName() + ": Je vais vers Explore...");
		}
		
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
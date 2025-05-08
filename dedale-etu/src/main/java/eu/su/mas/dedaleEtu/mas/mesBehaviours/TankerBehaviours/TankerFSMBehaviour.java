package eu.su.mas.dedaleEtu.mas.mesBehaviours.TankerBehaviours;

import java.util.List;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.SimpleBehaviour;

public class TankerFSMBehaviour extends FSMBehaviour {
	
	private static final long serialVersionUID = 8840215878185249279L;
	
	private static final String ETAT_MOVE = "Move";
	private static final String ETAT_OBSERVE = "Observe";
			
	public TankerFSMBehaviour(final AbstractDedaleAgent a, List<String> agentNames) {
		SimpleBehaviour move = new TankerMoveBehaviour (a);
		SimpleBehaviour observe = new TankerObserveBehaviour(a);
		
		registerFirstState(observe, ETAT_OBSERVE);
		registerState(move, ETAT_MOVE);
		
		registerTransition(ETAT_OBSERVE, ETAT_OBSERVE, 0);
		
		registerTransition(ETAT_MOVE, ETAT_OBSERVE, 1);
		registerTransition(ETAT_OBSERVE, ETAT_MOVE, 1);
		
	}

}


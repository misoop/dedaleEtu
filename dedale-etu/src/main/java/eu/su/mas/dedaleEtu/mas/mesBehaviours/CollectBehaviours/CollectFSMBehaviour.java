package eu.su.mas.dedaleEtu.mas.mesBehaviours.CollectBehaviours;

import java.util.List;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.SimpleBehaviour;

public class CollectFSMBehaviour extends FSMBehaviour {
	
	private static final long serialVersionUID = 8840215878185249279L;
	
	private static final String ETAT_MOVE = "Move";
	private static final String ETAT_OBSERVE = "Observe";
	private static final String ETAT_COLLECT = "Collect";
			
	public CollectFSMBehaviour(final AbstractDedaleAgent a, List<String> agentNames) {
		SimpleBehaviour move = new CollectMoveBehaviour (a);
		SimpleBehaviour observe = new CollectObserveBehaviour(a);
		SimpleBehaviour shareMap = new CollectBehaviour (a);
		
		registerFirstState(observe, ETAT_OBSERVE);
		registerState(move, ETAT_MOVE);
		registerState(shareMap, ETAT_COLLECT);
		
		registerTransition(ETAT_OBSERVE, ETAT_OBSERVE, 0);
		
		registerTransition(ETAT_MOVE, ETAT_OBSERVE, 1);
		registerTransition(ETAT_OBSERVE, ETAT_MOVE, 1);
		
		registerTransition(ETAT_OBSERVE, ETAT_COLLECT, 2);
		registerTransition(ETAT_COLLECT, ETAT_MOVE, 2);
		
	}

}


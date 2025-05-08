package eu.su.mas.dedaleEtu.mas.mesBehaviours.ExploBehaviours;

import java.util.List;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.SimpleBehaviour;

public class ExploFSMBehaviour extends FSMBehaviour {
	
	private static final long serialVersionUID = 8840215878185249279L;
	
	private static final String ETAT_MOVE = "Move";
	private static final String ETAT_OBSERVE = "Observe";
	private static final String ETAT_SHAREMAP = "ShareMap";
			
	public ExploFSMBehaviour(final AbstractDedaleAgent a, List<String> agentNames) {
		SimpleBehaviour move = new ExploMoveBehaviour (a);
		SimpleBehaviour observe = new ExploObserveBehaviour(a);
		SimpleBehaviour shareMap = new ShareMapBehaviour (a);
		
		registerFirstState(observe, ETAT_OBSERVE);
		registerState(move, ETAT_MOVE);
		registerState(shareMap, ETAT_SHAREMAP);
		
		registerTransition(ETAT_OBSERVE, ETAT_OBSERVE, 0);
		
		registerTransition(ETAT_MOVE, ETAT_OBSERVE, 1);
		registerTransition(ETAT_OBSERVE, ETAT_MOVE, 1);
		
		registerTransition(ETAT_OBSERVE, ETAT_SHAREMAP, 2);
		registerTransition(ETAT_SHAREMAP, ETAT_MOVE, 2);
		
	}

}


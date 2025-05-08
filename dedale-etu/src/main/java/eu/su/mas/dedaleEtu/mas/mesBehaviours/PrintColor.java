package eu.su.mas.dedaleEtu.mas.mesBehaviours;

import java.util.HashMap;
import java.util.Map;

/***
public class PrintColor {
	// Codes ANSI pour les couleurs
    private static final String RESET = "\u001B[0m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String YELLOW = "\u001B[33m";

    // Associer une couleur à chaque agent
    public static String getColorForAgent(String agentName) {
        switch (agentName) { // Distribuer les couleurs
            case "Elsa": return MAGENTA;
            case "Tim": return GREEN;
            case "Robert": return BLUE;
            // case 3: return YELLOW;
            default: return RESET;
        }
    }
***/


public class PrintColor {
    private static final String RESET = "\u001B[0m";
    private static final Map<String, String> agentColors = new HashMap<>();
    private static int nextColorIndex = 31; 

    public static String getColorForAgent(String agentName) {
        if (!agentColors.containsKey(agentName)) {
            int colorCode = nextColorIndex;
            nextColorIndex = nextColorIndex + 1;
            String ansiColor = "\u001B[" + colorCode + "m";
            agentColors.put(agentName, ansiColor);
        }
        return agentColors.get(agentName);
    }


    public static void print(String agentName, String message) {
        String color = getColorForAgent(agentName);
        System.out.println(color + "[" + agentName + "] " + message + RESET);
    }
}

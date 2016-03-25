package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.*;
import java.util.*;

/**
 * Created by  on 3/15/15.
 */
public class PlannerAgent extends Agent {

    final int requiredWood;
    final int requiredGold;
    final boolean buildPeasants;

    // Your PEAgent implementation. This prevents you from having to parse the text file representation of your plan.
    PEAgent peAgent;

    public PlannerAgent(int playernum, String[] params) {
        super(playernum);

        if(params.length < 3) {
            System.err.println("You must specify the required wood and gold amounts and whether peasants should be built");
        }

        requiredWood = Integer.parseInt(params[0]);
        requiredGold = Integer.parseInt(params[1]);
        buildPeasants = Boolean.parseBoolean(params[2]);


        System.out.println("required wood: " + requiredWood + " required gold: " + requiredGold + " build Peasants: " + buildPeasants);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {

        Stack<StripsAction> plan = AstarSearch(new GameState(stateView, playernum, requiredGold, requiredWood, buildPeasants));

        if(plan == null) {
            System.err.println("No plan was found");
            System.exit(1);
            return null;
        }

        // write the plan to a text file
        savePlan(plan);


        // Instantiates the PEAgent with the specified plan.
        peAgent = new PEAgent(playernum, plan);

        return peAgent.initialStep(stateView, historyView);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        if(peAgent == null) {
            System.err.println("Planning failed. No PEAgent initialized.");
            return null;
        }

        return peAgent.middleStep(stateView, historyView);
    }

    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }

    /**
     * Perform an A* search of the game graph. This should return your plan as a stack of actions. This is essentially
     * the same as your first assignment. The implementations should be very similar. The difference being that your
     * nodes are now GameState objects not MapLocation objects.
     *
     * @param startState The state which is being planned from
     * @return The plan or null if no plan is found.
     */
    private Stack<StripsAction> AstarSearch(GameState startState) {
//        System.out.println("Inside AStar");
//        List<MapLocation> closedSet = new ArrayList<MapLocation>();
//        List<MapLocation> openSet = new ArrayList<MapLocation>();
//
//        openSet.add(start);
//        List<MapLocation> exploredPrev = new ArrayList<MapLocation>();
//
//        HashMap<MapLocation, Double> fScore = new HashMap<MapLocation, Double>();
//        HashMap<MapLocation, MapLocation> explored = new HashMap<MapLocation, MapLocation>();
//        HashMap<MapLocation, Double> gScore = new HashMap<MapLocation, Double>();
//
//        MapLocation current = start;
//        fScore.put(current, calculateHeuristic(current, goal)); // initial estimate to goal
//        gScore.put(current, 0.0);                               // initial cost of optimal path
//
//        while (!openSet.isEmpty()) {
//            System.out.println("CURRENT NODE IS: " + current.toString());
//
//            MapLocation smallest = openSet.get(0);
//            //finding node with smallest fScore value
//            for (MapLocation loc : openSet) {
//                if (fScore.get(loc) < fScore.get(smallest)) {
//                    smallest = loc;
//                    System.out.println("NEW NODE IS: " + smallest.toString());
//                }
//            }
//            current = smallest;
//            //goal node is found, return path
//            if (current.equals(goal)) {
//                System.out.println(current.toString()+" GOAL FOUND!!!!!!!!!!!!!!!!!!!!");
//                return reconstructPath(explored, current);
//            }
//
//            //finished with the current node, so move it from Open to Closed
//            openSet.remove(current);
//            closedSet.add(current);
//
//            //examine the current nodes neighbors for the next most valid candidate
//            //examines all neighbor nodes that aren't closed or resources
//            List<MapLocation> neighbors = getNeighbors(current, xExtent, yExtent, resourceLocations, closedSet);
//
//            for (MapLocation neighbor : neighbors) {
//                if (gScore.get(neighbor) == null) {
//                    //gScore not found yet, initialize to VERY big
//                    gScore.put(neighbor, Double.MAX_VALUE);
//                }
//
//                //get distance between two nodes
//                double distance = Math.sqrt(Math.pow((neighbor.x - current.x), 2) + Math.pow((neighbor.y - current.y), 2));
//                double tentativeGScore = gScore.get(current) + distance;
//
//                //check if neighbor is in openSet
//                if (!openSet.contains(neighbor)) {
//                    System.out.println("Adding (" + neighbor.x + ", " + neighbor.y + ") to OpenSet");
//                    openSet.add(neighbor);
//                }
//                else if (tentativeGScore >= gScore.get(neighbor)) {
//                    continue;
//                }
//
//                exploredPrev.add(neighbor);
//                //use map instead of array to show explored from
//                explored.put(neighbor, current);
//                gScore.put(neighbor, tentativeGScore);
//                fScore.put(neighbor, (gScore.get(neighbor)+ calculateHeuristic(neighbor, goal)));
//            }
//        }
//        // return an empty path
//        System.out.println("Returning NOTHING");
//        return new Stack<MapLocation>();
        return null;
    }

    /**
     * This has been provided for you. Each strips action is converted to a string with the toString method. This means
     * each class implementing the StripsAction interface should override toString. Your strips actions should have a
     * form matching your included Strips definition writeup. That is <action name>(<param1>, ...). So for instance the
     * move action might have the form of Move(peasantID, X, Y) and when grounded and written to the file
     * Move(1, 10, 15).
     *
     * @param plan Stack of Strips Actions that are written to the text file.
     */
    private void savePlan(Stack<StripsAction> plan) {
        if (plan == null) {
            System.err.println("Cannot save null plan");
            return;
        }

        File outputDir = new File("saves");
        outputDir.mkdirs();

        File outputFile = new File(outputDir, "plan.txt");

        PrintWriter outputWriter = null;
        try {
            outputFile.createNewFile();

            outputWriter = new PrintWriter(outputFile.getAbsolutePath());

            Stack<StripsAction> tempPlan = (Stack<StripsAction>) plan.clone();
            while(!tempPlan.isEmpty()) {
                outputWriter.println(tempPlan.pop().toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputWriter != null)
                outputWriter.close();
        }
    }
}

package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to represent the state of the game after applying one of the avaiable actions. It will also
 * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
 * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
 * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
 * add any other methods and member variables you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
 * I recommend storing the actions that generated the instance of the GameState in this class using whatever
 * class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {

    private int playerNum;

    private boolean buildPeasants;
    private int requiredGold;
    private int requiredWood;

    private List<Position> goldLocations;
    private List<Position> treeLocations;

    /**
     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
     * nodes should be constructed from the another constructor you create or by factory functions that you create.
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
        this.playerNum = playernum;
        this.buildPeasants = buildPeasants;
        this.requiredGold = requiredGold;
        this.requiredWood = requiredWood;

        goldLocations = new ArrayList<>();
        treeLocations = new ArrayList<>();

        for (ResourceNode.ResourceView resource : state.getAllResourceNodes()) {
            if (resource.getType().equals(ResourceNode.Type.GOLD_MINE)) {
                goldLocations.add(new Position(resource.getXPosition(), resource.getYPosition()));
            } else if (resource.getType().equals(ResourceNode.Type.TREE)) {
                treeLocations.add(new Position(resource.getXPosition(), resource.getYPosition()));
            }
        }
    }

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        // TODO: Implement me!
        return false;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
        // TODO: Implement me!
        return null;
    }

    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * Add a description here in your submission explaining your heuristic.
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {
        // TODO: Implement me!
        return 0.0;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        // TODO: Implement me!
        return 0.0;
    }

    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
        if (this.getCost() > o.getCost()) {
            return 1;
        } else if (this.getCost() < o.getCost()) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GameState gameState = (GameState) o;

        if (playerNum != gameState.playerNum) return false;
        if (buildPeasants != gameState.buildPeasants) return false;
        if (requiredGold != gameState.requiredGold) return false;
        if (requiredWood != gameState.requiredWood) return false;
        if (!goldLocations.equals(gameState.goldLocations)) return false;
        return treeLocations.equals(gameState.treeLocations);
    }

    @Override
    public int hashCode() {
        int result = playerNum;
        result = 31 * result + (buildPeasants ? 1 : 0);
        result = 31 * result + requiredGold;
        result = 31 * result + requiredWood;
        result = 31 * result + goldLocations.hashCode();
        result = 31 * result + treeLocations.hashCode();
        return result;
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public boolean isBuildPeasants() {
        return buildPeasants;
    }

    public int getRequiredGold() {
        return requiredGold;
    }

    public int getRequiredWood() {
        return requiredWood;
    }

    public List<Position> getGoldLocations() {
        return goldLocations;
    }

    public List<Position> getTreeLocations() {
        return treeLocations;
    }
}

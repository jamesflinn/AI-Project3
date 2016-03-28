package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.DepositAction;
import edu.cwru.sepia.agent.planner.actions.HarvestAction;
import edu.cwru.sepia.agent.planner.actions.MoveAction;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import javafx.geometry.Pos;

import java.util.*;

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
    private int currentGold;
    private int currentWood;

    private List<ResourceLocation> goldLocations;
    private List<ResourceLocation> treeLocations;

    private Position townhall;
    private Map<Integer, Peasant> peasants;

    private Stack<StripsAction> previousActions;

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
        currentGold = 0;
        currentWood = 0;
        goldLocations = new ArrayList<>();
        treeLocations = new ArrayList<>();
        peasants = new HashMap<>();

        // add resource locations
        for (ResourceNode.ResourceView resource : state.getAllResourceNodes()) {
            Position position = new Position(resource.getXPosition(), resource.getYPosition());
            if (resource.getType().equals(ResourceNode.Type.GOLD_MINE)) {
                goldLocations.add(new ResourceLocation(position, ResourceNode.Type.GOLD_MINE,resource.getAmountRemaining()));
            } else if (resource.getType().equals(ResourceNode.Type.TREE)) {
                treeLocations.add(new ResourceLocation(position, ResourceNode.Type.TREE,resource.getAmountRemaining()));
            }
        }

        // add existing units
        for (Unit.UnitView unit : state.getUnits(playernum)) {
            String unitType = unit.getTemplateView().getName().toLowerCase();

            if (unitType.equals("townhall")) {
                townhall = new Position(unit.getXPosition(), unit.getYPosition());
            } else if (unitType.equals("peasant")) {
                peasants.put(unit.getID(), new Peasant(unit.getID(), new Position(unit.getXPosition(), unit.getYPosition())));
            }
        }

        this.previousActions = new Stack<>();
    }

    /**
     * Construct a GameState from a previous GameState
     * @param state the previous GameState
     * @param goldLocations the locations of gold mines
     * @param treeLocations the locations of trees
     * @param peasants the peasants
     * @param currentGold the current amount of gold a player has
     * @param currentWood the current amount of wood a player has
     * @param action the previous action that led to this GameState
     */
    public GameState(GameState state , List<ResourceLocation> goldLocations, List<ResourceLocation> treeLocations, Map<Integer, Peasant> peasants, int currentGold, int currentWood, Stack<StripsAction> actions) {
        this.playerNum = state.getPlayerNum();
        this.buildPeasants = state.isBuildPeasants();
        this.requiredGold = state.getRequiredGold();
        this.requiredWood = state.getRequiredWood();
        this.townhall = state.getTownhall();

        this.goldLocations = goldLocations;
        this.treeLocations = treeLocations;
        this.peasants = peasants;
        this.currentGold = currentGold;
        this.currentWood = currentWood;

        this.previousActions = actions;
    }

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        return currentGold >= requiredGold && currentWood >= requiredWood;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
        List<GameState> children = new ArrayList<>();
        for (Peasant peasant : peasants.values()) {
            HarvestAction harvestAction = new HarvestAction(peasant.getID());
            if (harvestAction.preconditionsMet(this)) {
                children.add(harvestAction.apply(this));
            }

            DepositAction depositAction = new DepositAction(peasant.getID());
            if (depositAction.preconditionsMet(this)) {
                children.add(depositAction.apply(this));
            }

            for (Position position : getAllAdjacentPositions(peasant.getPosition())) {
                MoveAction moveAction = new MoveAction(peasant.getID(), peasant.getPosition(), position);
                if (moveAction.preconditionsMet(this)) {
                    children.add(moveAction.apply(this));
                }
            }
        }
        return children;
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
            // TODO: Assumes each action is cost 1, probably need to change this.
            return previousActions.size();
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

    /**
     * Finds all positions adjacent to the given position
     * @param position the given position
     * @return A list of all adjacent positions
     */
    private List<Position> getAllAdjacentPositions(Position position) {
        int x = position.x;
        int y = position.y;
        return Arrays.asList(
                new Position(x - 1, y - 1),
                new Position(x, y - 1),
                new Position(x + 1, y - 1),
                new Position(x - 1, y),
                new Position(x + 1, y),
                new Position(x - 1, y+ 1),
                new Position(x, y + 1),
                new Position(x + 1, y + 1)
        );
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public boolean isBuildPeasants() {
        return buildPeasants;
    }

    public int getCurrentGold() {
        return currentGold;
    }

    public int getCurrentWood() {
        return currentWood;
    }

    public int getRequiredGold() {
        return requiredGold;
    }

    public int getRequiredWood() {
        return requiredWood;
    }

    public List<ResourceLocation> getGoldLocations() {
        return goldLocations;
    }

    public List<ResourceLocation> getTreeLocations() {
        return treeLocations;
    }

    public List<ResourceLocation> getAllResourceLocations() {
        List<ResourceLocation> allResources = new ArrayList<>(goldLocations);
        allResources.addAll(treeLocations);
        return allResources;
    }

    public Peasant getPeasant(int peasantID) {
        return peasants.get(peasantID);
    }

    public Map<Integer, Peasant> getPeasantsMap() {
        return peasants;
    }

    public Position getTownhall() {
        return townhall;
    }

    public Stack<StripsAction> getPreviousActions() {
        return previousActions;
    }
}

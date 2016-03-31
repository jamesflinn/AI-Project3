package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

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

    private int xExtent;
    private int yExtent;

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
        this(state, playernum, requiredGold, requiredWood, 0, 0, buildPeasants);
    }

    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, int currentGold, int currentWood, boolean buildPeasants) {
        this.playerNum = playernum;
        this.xExtent = state.getXExtent();
        this.yExtent = state.getYExtent();
        this.buildPeasants = buildPeasants;
        this.requiredGold = requiredGold;
        this.requiredWood = requiredWood;
        this.currentGold = currentGold;
        this.currentWood = currentWood;
        goldLocations = new ArrayList<>();
        treeLocations = new ArrayList<>();
        peasants = new HashMap<>();

        // add resource locations
        for (ResourceNode.ResourceView resource : state.getAllResourceNodes()) {
            Position position = new Position(resource.getXPosition(), resource.getYPosition());
            if (resource.getType().equals(ResourceNode.Type.GOLD_MINE)) {
                goldLocations.add(new ResourceLocation(position, ResourceNode.Type.GOLD_MINE, resource.getAmountRemaining()));
            } else if (resource.getType().equals(ResourceNode.Type.TREE)) {
                treeLocations.add(new ResourceLocation(position, ResourceNode.Type.TREE, resource.getAmountRemaining()));
            }
        }

        int peasantNumber = 1;

        // add existing units
        for (Unit.UnitView unit : state.getUnits(playernum)) {
            String unitType = unit.getTemplateView().getName().toLowerCase();

            if (unitType.equals("townhall")) {
                townhall = new Position(unit.getXPosition(), unit.getYPosition());
            } else if (unitType.equals("peasant")) {
                if (unit.getCargoAmount() > 0) {

                    ResourceNode.Type resourceType;
                    if (unit.getCargoType().equals(ResourceType.GOLD)) {
                        resourceType = ResourceNode.Type.GOLD_MINE;
                    } else {
                        resourceType = ResourceNode.Type.TREE;
                    }
                    peasants.put(peasantNumber, new Peasant(peasantNumber, new Position(unit.getXPosition(), unit.getYPosition()), resourceType));

                } else {
                    peasants.put(peasantNumber, new Peasant(peasantNumber, new Position(unit.getXPosition(), unit.getYPosition())));
                }
                peasantNumber += 1;
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
     */
    public GameState(GameState state , List<ResourceLocation> goldLocations, List<ResourceLocation> treeLocations, Map<Integer, Peasant> peasants, int currentGold, int currentWood, Stack<StripsAction> actions) {
        this.playerNum = state.getPlayerNum();
        this.xExtent = state.getxExtent();
        this.yExtent = state.getyExtent();
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
        List<List<StripsAction>> allPeasantActions = new ArrayList<>();

        BuildPeasantAction buildPeasantAction = new BuildPeasantAction();
        if (buildPeasants && buildPeasantAction.preconditionsMet(this)) {
            allPeasantActions.add(Arrays.asList(buildPeasantAction));
        }

        for (Peasant peasant : peasants.values()) {
            List<StripsAction> peasantActions = new ArrayList<>();

            HarvestAction harvestAction = new HarvestAction(peasant.getID());
            if (harvestAction.preconditionsMet(this)) {
                peasantActions.add(harvestAction);
            }


            if (peasant.isCarrying()) {
                // Move to townhall
                MoveAction moveAction = new MoveAction(peasant.getID(), peasant.getPosition(), getClosestAdjacentPosition(peasant.getPosition(), townhall), xExtent, yExtent);
                if (moveAction.preconditionsMet(this)) {
                    peasantActions.add(moveAction);
                }

                // Deposit resources
                DepositAction depositAction = new DepositAction(peasant.getID());
                if (depositAction.preconditionsMet(this)) {
                    peasantActions.add(depositAction);
                }
            } else if (!peasant.isCarrying() && peasant.getPosition().isAdjacent(townhall)){
                // Move to all resource locations
                if (currentGold < requiredGold) {
                    for (ResourceLocation resource : goldLocations) {
                        MoveAction moveAction = new MoveAction(peasant.getID(), peasant.getPosition(), getClosestAdjacentPosition(peasant.getPosition(), resource.getPosition()), xExtent, yExtent);
                        if (moveAction.preconditionsMet(this)) {
                            peasantActions.add(moveAction);
                        }
                    }
                }

                if (currentWood < requiredWood) {
                    for (ResourceLocation resource : treeLocations) {
                        MoveAction moveAction = new MoveAction(peasant.getID(), peasant.getPosition(), getClosestAdjacentPosition(peasant.getPosition(), resource.getPosition()), xExtent, yExtent);
                        if (moveAction.preconditionsMet(this)) {
                            peasantActions.add(moveAction);
                        }
                    }
                }
            }

            // Add all of this peasant's actions to the list of all peasant's actions.
            allPeasantActions.add(peasantActions);
        }

        List<List<StripsAction>> combinedActions = cartesianProduct(allPeasantActions);

        for (List<StripsAction> combinedAction : combinedActions) {
            ParallelAction parallelAction = new ParallelAction(combinedAction);
            if (parallelAction.preconditionsMet(this)) {
                children.add(parallelAction.apply(this));
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
        double heuristic = 0;
        int goldNeeded = requiredGold - currentGold;
        int woodNeeded = requiredWood - currentWood;
        int tripsGold = goldNeeded/100;
        int tripsWood = woodNeeded/100;
        int tripsLeft = tripsGold + tripsWood;

        //Setting 4 as the threshold for deciding whether peasants are necessary

        for (Peasant peasant : peasants.values()) {
            if (peasant.isCarrying() && peasant.getResourceType().equals(ResourceNode.Type.GOLD_MINE)) {
                int distanceToTownhall= peasant.getPosition().chebyshevDistance(townhall);
                heuristic += 2 * (distanceToTownhall * (tripsGold - 1)) + distanceToTownhall;
                if(requiredGold/2 > currentGold){
                    heuristic = heuristic  / 2;
                }
            } else {
                int distanceToResource = peasant.getPosition().chebyshevDistance(findClosestResourcePosition(peasant.getPosition(), ResourceNode.Type.GOLD_MINE));
                heuristic += 2 * distanceToResource * tripsGold;

            }
        }

        for (Peasant peasant : peasants.values()) {
            if (peasant.isCarrying() && peasant.getResourceType().equals(ResourceNode.Type.TREE)) {
                int distanceToTownhall= peasant.getPosition().chebyshevDistance(townhall);
                heuristic += 2 * (distanceToTownhall * (tripsWood - 1)) + distanceToTownhall;
                if(requiredGold/2 > currentGold){
                    heuristic = heuristic * 2;
                }
            } else {
                int distanceToResource = peasant.getPosition().chebyshevDistance(findClosestResourcePosition(peasant.getPosition(), ResourceNode.Type.TREE));
                heuristic += 2 * distanceToResource * tripsWood;
            }
        }

        heuristic /= peasants.values().size();

        return heuristic;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        StripsAction previousAction = previousActions.peek();

        if (previousAction instanceof MoveAction) {
            MoveAction moveAction = (MoveAction) previousAction;
            return moveAction.getCurrentPosition().chebyshevDistance(moveAction.getTargetPosition());
        } else {
            return 1;
        }
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
        return (int) (this.heuristic() - o.heuristic());
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
        if (currentGold != gameState.currentGold) return false;
        if (currentWood != gameState.currentWood) return false;
        if (!goldLocations.equals(gameState.goldLocations)) return false;
        if (!treeLocations.equals(gameState.treeLocations)) return false;
        return peasants.equals(gameState.peasants);

    }

    @Override
    public int hashCode() {
        int result = playerNum;
        result = 31 * result + (buildPeasants ? 1 : 0);
        result = 31 * result + requiredGold;
        result = 31 * result + requiredWood;
        result = 31 * result + currentGold;
        result = 31 * result + currentWood;
        result = 31 * result + goldLocations.hashCode();
        result = 31 * result + treeLocations.hashCode();
        result = 31 * result + peasants.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "GameState {" + "\n\t" +
                "currentGold=" + currentGold + ",\n\t" +
                "currentWood=" + currentWood + ",\n\t" +
                "goldLocations=" + Arrays.toString(goldLocations.toArray()) + ",\n\t" +
                "treeLocations=" + Arrays.toString(treeLocations.toArray()) + ",\n\t" +
                "peasants=" + Arrays.toString(peasants.values().toArray()) + ",\n\t" +
                "previousActions=" + Arrays.toString(previousActions.toArray()) +
                '}';
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

    public int getxExtent() {
        return xExtent;
    }

    public int getyExtent() {
        return yExtent;
    }

    /**
     * Finds the closest resource to the given position
     * @param position The given position
     * @return the closest resource position
     */
    private Position findClosestResourcePosition(Position position, ResourceNode.Type resourceType) {
        Position closestResourcePosition = new Position(Integer.MAX_VALUE, Integer.MAX_VALUE);

        for (ResourceLocation resourceLocation : getAllResourceLocations()) {
            if (resourceLocation.getResourceType().equals(resourceType) && position.chebyshevDistance(resourceLocation.getPosition()) < position.chebyshevDistance(closestResourcePosition)) {
                closestResourcePosition = resourceLocation.getPosition();
            }
        }
        return closestResourcePosition;
    }

    /**
     * Given a start and end position, finds a new position such that the it is adjacent to end and
     * closest to the start position.
     *
     * @param start The start position
     * @param end   The end position
     * @return The closest adjacent position to start
     */
    private Position getClosestAdjacentPosition(Position start, Position end) {
        List<Position> adjacentPositions = end.getAdjacentPositions();
        Position closestAdjacentPosition = new Position(Integer.MAX_VALUE, Integer.MAX_VALUE);
        int closestAdjacentPositionDistance = start.chebyshevDistance(closestAdjacentPosition);

        for (Position adjacentPosition : adjacentPositions) {
            if (start.chebyshevDistance(adjacentPosition) < closestAdjacentPositionDistance) {
                closestAdjacentPosition = adjacentPosition;
                closestAdjacentPositionDistance = start.chebyshevDistance(adjacentPosition);
            }
        }

        return closestAdjacentPosition;
    }

    /**
     * Finds the cartesian product of each units's actions
     *
     * @param actionsList A list of each unit's actions
     * @return The cartesian product of actions
     */
    private List<List<StripsAction>> cartesianProduct(List<List<StripsAction>> actionsList) {
        List<List<StripsAction>> combinations = new ArrayList<>();
        for (List<StripsAction> actions : actionsList) {
            List<List<StripsAction>> extraColumnCombinations = new ArrayList<>();
            for (StripsAction action : actions) {
                if (combinations.isEmpty()) {
                    extraColumnCombinations.add(Collections.singletonList(action));
                } else {
                    for (List<StripsAction> productList : combinations) {
                        List<StripsAction> newProductList = new ArrayList<>(productList);
                        newProductList.add(action);
                        extraColumnCombinations.add(newProductList);
                    }
                }
            }
            combinations = extraColumnCombinations;
        }
        return combinations;
    }
}

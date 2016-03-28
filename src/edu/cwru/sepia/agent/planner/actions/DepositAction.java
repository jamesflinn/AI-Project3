package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.util.Direction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Represents the Deposit action.
 */
public class DepositAction implements StripsAction {

    private int peasantID;
    private Direction townhallDirection;

    public DepositAction(int peasantID) {
        this.peasantID = peasantID;
    }

    /**
     * Preconditions are met if the peasant is next to the town hall and it is carrying a resource.
     * @param state GameState to check if action is applicable
     * @return true if preconditions are met
     */
    @Override
    public boolean preconditionsMet(GameState state) {
        Peasant peasant = state.getPeasant(peasantID);
        return peasant.getPosition().isAdjacent(state.getTownhall()) && peasant.isCarrying();
    }

    /**
     * Returns a new GameState such that the peasant is no longer carrying anything, and the resource that
     * peasant was carrying was added to the player's current resource count.
     * @param state State to apply action to
     * @return a new GameState
     */
    @Override
    public GameState apply(GameState state) {
        Peasant peasant = state.getPeasant(peasantID);
        Peasant newPeasant = new Peasant(peasantID, peasant.getPosition());
        townhallDirection = peasant.getPosition().getDirection(state.getTownhall());

        int newGoldAmount = state.getCurrentGold();
        int newWoodAmount = state.getCurrentWood();

        if (peasant.getResourceType() == ResourceNode.Type.GOLD_MINE) {
            newGoldAmount += 100;
        } else {
            newWoodAmount += 100;
        }

        Map<Integer, Peasant> newPeasantMap = new HashMap<>(state.getPeasantsMap());
        newPeasantMap.put(peasantID, newPeasant);

        Stack<StripsAction> actions = (Stack<StripsAction>) state.getPreviousActions().clone();
        actions.push(this);

        return new GameState(state, state.getGoldLocations(), state.getTreeLocations(), newPeasantMap, newGoldAmount, newWoodAmount, actions);
    }

    public int getPeasantID() {
        return peasantID;
    }

    public Direction getTownhallDirection() {
        return townhallDirection;
    }
}

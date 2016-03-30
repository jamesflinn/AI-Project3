package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Represents the Move action.
 */
public class MoveAction implements StripsAction {

    private int peasantID;
    private Position currentPosition;
    private Position targetPosition;

    public MoveAction(int peasantID, Position currentPosition, Position targetPosition) {
        this.peasantID = peasantID;
        this.currentPosition = currentPosition;
        this.targetPosition = targetPosition;
    }

    /**
     * Preconditions are met if the peasant is located at currentPosition,
     * and if there is nothing at targetPosition. Also check to
     * @param state GameState to check if action is applicable
     * @return true if preconditions are met
     */
    @Override
    public boolean preconditionsMet(GameState state) {
        Peasant peasant = state.getPeasant(peasantID);
        return peasant.getPosition().equals(currentPosition) &&
                !state.getPeasantsMap().values().stream().anyMatch((p) -> p.getPosition().equals(targetPosition));
    }

    /**
     * Returns a new GameState such that the peasant is now located at targetPosition.
     * @param state State to apply action to
     * @return the new GameState
     */
    @Override
    public GameState apply(GameState state) {
        Peasant peasant = state.getPeasant(peasantID);
        Peasant newPeasant;
        if (peasant.isCarrying()) {
            newPeasant = new Peasant(peasantID, targetPosition, peasant.getResourceType());
        } else {
            newPeasant = new Peasant(peasantID, targetPosition);
        }

        Map<Integer, Peasant> newPeasantMap = new HashMap<>(state.getPeasantsMap());
        newPeasantMap.put(peasantID, newPeasant);

        Stack<StripsAction> actions = (Stack<StripsAction>) state.getPreviousActions().clone();
        actions.push(this);

        return new GameState(state, state.getGoldLocations(), state.getTreeLocations(), newPeasantMap, state.getCurrentGold(), state.getCurrentWood(), actions);
    }

    public int getPeasantID() {
        return peasantID;
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public Position getTargetPosition() {
        return targetPosition;
    }


    public boolean isAdjacent(Position targetPosition, Position currentPosition) {
        return Math.abs(targetPosition.x - currentPosition.x) <= 1 && Math.abs(targetPosition.y - currentPosition.y) <= 1;
    }
}

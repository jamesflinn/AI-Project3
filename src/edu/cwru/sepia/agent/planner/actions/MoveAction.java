package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;

import java.util.HashMap;
import java.util.Map;

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
     * and if there is nothing at targetPosition.
     * @param state GameState to check if action is applicable
     * @return true if preconditions are met
     */
    @Override
    public boolean preconditionsMet(GameState state) {
        Peasant peasant = state.getPeasant(peasantID);
        return peasant.getPosition().equals(currentPosition) &&
                !state.getPeasantsMap().values().stream().anyMatch((p) -> p.getPosition().equals(targetPosition));
    }

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

        return new GameState(state, state.getGoldLocations(), state.getTreeLocations(), newPeasantMap, state.getCurrentGold(), state.getCurrentWood());
    }
}

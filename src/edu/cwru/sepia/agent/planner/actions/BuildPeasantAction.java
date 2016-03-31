package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by jamesflinn on 3/29/16.
 */
public class BuildPeasantAction implements StripsAction {

    private int newID;

    /**
     * Preconditions are met if the player has at least 400 gold,
     * and if there are less than 3 peasants already in existance.
     * @param state GameState to check if action is applicable
     * @return true if preconditions are met
     */
    @Override
    public boolean preconditionsMet(GameState state) {
        return state.getCurrentGold() >= 400 && state.getPeasantsMap().size() < 3;
    }

    /**
     * Creates a new state such that there is an additional peasant, and that the player has 400 less gold.
     *
     * @param state State to apply action to
     * @return A new GameState with an additional peasant.
     */
    @Override
    public GameState apply(GameState state) {
        newID = state.getPeasantsMap().size() + 1;
        Position newPosition = new Position(state.getTownhall().x + 1, state.getTownhall().y);
        Peasant newPeasant = new Peasant(newID, newPosition);

        Map<Integer, Peasant> newPeasantMap = new HashMap<>(state.getPeasantsMap());
        newPeasantMap.put(newID, newPeasant);

        Stack<StripsAction> actions = (Stack<StripsAction>) state.getPreviousActions().clone();
        actions.push(this);

        return new GameState(state, state.getGoldLocations(), state.getTreeLocations(), newPeasantMap, state.getCurrentGold() - 400, state.getCurrentWood(), actions);
    }

    public int getNewID() {
        return newID;
    }
}

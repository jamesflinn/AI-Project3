package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.environment.model.state.ResourceNode;

import java.util.Arrays;

/**
 * Represents the Deposit action.
 */
public class DepositAction implements StripsAction {
    /**
     * Preconditions are met if the peasant is next to the town hall and it is carrying a resource.
     * @param state GameState to check if action is applicable
     * @return true if preconditions are met
     */
    @Override
    public boolean preconditionsMet(GameState state) {
        if (state.getPeasants().size() != 1) {
            return false;
        }

        Peasant peasant = state.getPeasants().get(0);
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
        Peasant peasant = state.getPeasants().get(0);
        Peasant newPeasant = new Peasant(peasant.getPosition());

        int newGoldAmount = state.getCurrentGold();
        int newWoodAmount = state.getCurrentWood();

        if (peasant.getResourceType() == ResourceNode.Type.GOLD_MINE) {
            newGoldAmount += 100;
        } else {
            newWoodAmount += 100;
        }

        return new GameState(state, state.getGoldLocations(), state.getTreeLocations(), Arrays.asList(newPeasant), newGoldAmount, newWoodAmount);
    }
}

package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;

/**
 * Represents the HarvestWood action.
 */
public class HarvestWoodAction implements StripsAction {
    @Override
    public boolean preconditionsMet(GameState state) {
        return false;
    }

    @Override
    public GameState apply(GameState state) {
        return null;
    }
}

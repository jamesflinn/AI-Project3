package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;

import java.util.List;

/**
 * Represents a parallel action.
 * Parallel actions can consist of multiple actions for many different peasants.
 */
public class ParallelAction implements StripsAction {

    List<StripsAction> actions;

    public ParallelAction(List<StripsAction> actions) {
        this.actions = actions;
    }

    /**
     * The preconditions of each action must be met.
     *
     * @param state GameState to check if action is applicable
     * @return true if every action's preconditions are met
     */
    @Override
    public boolean preconditionsMet(GameState state) {
        return actions.stream().allMatch(a -> a.preconditionsMet(state));
    }

    /**
     * Applies each action to the given state
     *
     * @param state State to apply action to
     * @return A new GameState with each action applied to it.
     */
    @Override
    public GameState apply(GameState state) {
        GameState newState = state;
        for (StripsAction action : actions) {
            newState = action.apply(newState);
        }
        return newState;
    }
}
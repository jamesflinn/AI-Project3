package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Template;
import edu.cwru.sepia.environment.model.state.Unit;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * This is an outline of the PEAgent. Implement the provided methods. You may add your own methods and members.
 */
public class PEAgent extends Agent {

    private static final int MAX_PEASANTS = 3;

    // The plan being executed
    private Stack<StripsAction> plan = null;

    // maps the real unit Ids to the plan's unit ids
    // when you're planning you won't know the true unit IDs that sepia assigns. So you'll use placeholders (1, 2, 3).
    // this maps those placeholders to the actual unit IDs.
    private Map<Integer, Integer> peasantIdMap;
    private int townhallId;
    private int peasantTemplateId;

    // Maps peasant ids to their respective action stack
    private Map<Integer, Stack<StripsAction>> peasantActionMap;
    private Map<Integer, StripsAction> previousActionMap;

    public PEAgent(int playernum, Stack<StripsAction> plan) {
        super(playernum);
        peasantIdMap = new HashMap<Integer, Integer>();
        List<StripsAction> stackList = new ArrayList<>(plan);
        Stack<StripsAction> reversePlan = new Stack<StripsAction>();
        for(int i = stackList.size()-1; i>=0;i--){
            reversePlan.push(stackList.get(i));
        }
        this.plan = reversePlan;

    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {
       Map<Integer, Stack<StripsAction>> peasantActionMap = new HashMap<>();

        // gets the townhall ID and the peasant ID
        for(int unitId : stateView.getUnitIds(playernum)) {
            Unit.UnitView unit = stateView.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall")) {
                townhallId = unitId;
            } else if(unitType.equals("peasant")) {
                peasantIdMap.put(unitId, unitId);
            }
        }

        // Gets the peasant template ID. This is used when building a new peasant with the townhall
        for(Template.TemplateView templateView : stateView.getTemplates(playernum)) {
            if(templateView.getName().toLowerCase().equals("peasant")) {
                peasantTemplateId = templateView.getID();
                break;
            }
        }

        List<Stack<StripsAction>> stackList = Arrays.asList(new Stack<>(), new Stack<>(), new Stack<>());

        // The first parallel action will always only have one peasant. Initialize that peasant in the peasantActionMap
        StripsAction firstAction = plan.peek();
        ParallelAction firstParallelAction = (ParallelAction) firstAction;
        peasantActionMap.put(findIdByAction(firstParallelAction.getActions().get(0)), stackList.get(0));

        int currentStackIndex = 1;
        for(StripsAction action : plan){
            ParallelAction parallelAction = (ParallelAction) action;

            // There must be a BuildPeasantAction!!!!!!!!
            if (parallelAction.getActions().size() > currentStackIndex) {
                for (StripsAction stripsAction : parallelAction.getActions()) {
                    if (stripsAction instanceof BuildPeasantAction) {
                        BuildPeasantAction buildPeasantAction = (BuildPeasantAction) stripsAction;
                        stackList.get(currentStackIndex).push(buildPeasantAction);
                        peasantActionMap.put(buildPeasantAction.getNewID(), stackList.get(currentStackIndex));
                        currentStackIndex += 1;
                    }
                }
            }

            // For every action in parallelAction, add that action to the specific unit's action stack
            for (StripsAction stripsAction : parallelAction.getActions()) {
                // this was already dealt with, skip it
                if (stripsAction instanceof BuildPeasantAction) {
                    continue;
                }

                peasantActionMap.get(findIdByAction(stripsAction)).push(stripsAction);
            }

            // Push null before a peasant is built
            for (int i = currentStackIndex; i < MAX_PEASANTS; i++) {
                stackList.get(i).push(null);
            }
        }

        this.peasantActionMap = peasantActionMap;
        this.previousActionMap = new HashMap<>();

        return middleStep(stateView, historyView);
    }

    /**
     * Finds a unit's ID in the specified action
     *
     * @param action the StripsAction containing the ID
     * @return the ID of the action
     */
    private int findIdByAction(StripsAction action) {
        if (action instanceof MoveAction) {
            MoveAction moveAction = (MoveAction) action;
            return moveAction.getPeasantID();
        } else if (action instanceof HarvestAction) {
            HarvestAction harvestAction = (HarvestAction) action;
            return harvestAction.getPeasantID();
        } else if (action instanceof DepositAction) {
            DepositAction depositAction = (DepositAction) action;
            return depositAction.getPeasantID();
        }

        return -1;
    }

    /**
     * This is where you will read the provided plan and execute it. If your plan is correct then when the plan is empty
     * the scenario should end with a victory. If the scenario keeps running after you run out of actions to execute
     * then either your plan is incorrect or your execution of the plan has a bug.
     *
     * You can create a SEPIA deposit action with the following method
     * Action.createPrimitiveDeposit(int peasantId, Direction townhallDirection)
     *
     * You can create a SEPIA harvest action with the following method
     * Action.createPrimitiveGather(int peasantId, Direction resourceDirection)
     *
     * You can create a SEPIA build action with the following method
     * Action.createPrimitiveProduction(int townhallId, int peasantTemplateId)
     *
     * You can create a SEPIA move action with the following method
     * Action.createCompoundMove(int peasantId, int x, int y)
     *
     * these actions are stored in a mapping between the peasant unit ID executing the action and the action you created.
     *
     * For the compound actions you will need to check their progress and wait until they are complete before issuing
     * another action for that unit. If you issue an action before the compound action is complete then the peasant
     * will stop what it was doing and begin executing the new action.
     *
     * To check an action's progress you can call getCurrentDurativeAction on each UnitView. If the Action is null nothing
     * is being executed. If the action is not null then you should also call getCurrentDurativeProgress. If the value is less than
     * 1 then the action is still in progress.
     *
     * Also remember to check your plan's preconditions before executing!
     */
    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        Map<Integer, Action> actionMap = new HashMap<>();

        for (Integer peasantID : peasantActionMap.keySet()) {
            Stack<StripsAction> actionStack = peasantActionMap.get(peasantID);
            if (isActionComplete(previousActionMap.get(peasantID), stateView, historyView)) {
                StripsAction stripsAction = actionStack.pop();
                previousActionMap.put(findIdByAction(stripsAction), stripsAction);
                Action action = createSepiaAction(stripsAction);
                actionMap.put(action.getUnitId(), action);
            }
        }

        return actionMap;
    }

    /**
     * Returns a SEPIA version of the specified Strips Action.
     * @param action StripsAction
     * @return SEPIA representation of same action
     */
    private Action createSepiaAction(StripsAction action) {
        if (action instanceof MoveAction) {
            MoveAction moveAction = (MoveAction) action;
            return Action.createCompoundMove(moveAction.getPeasantID(), moveAction.getTargetPosition().x, moveAction.getTargetPosition().y);
        } else if (action instanceof HarvestAction) {
            HarvestAction harvestAction = (HarvestAction) action;
            return Action.createPrimitiveGather(harvestAction.getPeasantID(), harvestAction.getResourceDirection());
        } else if (action instanceof DepositAction) {
            DepositAction depositAction = (DepositAction) action;
            return Action.createPrimitiveDeposit(depositAction.getPeasantID(), depositAction.getTownhallDirection());
        } else if (action instanceof BuildPeasantAction) {
            return Action.createPrimitiveProduction(townhallId, peasantTemplateId);
        }

        return null;
    }

    private boolean isActionComplete(StripsAction action, State.StateView stateView, History.HistoryView historyView) {
        if (action == null) {
            return true;
        }

        if (action instanceof MoveAction) {
            Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
            for (ActionResult result : actionResults.values()) {
                if (result.getFeedback() == ActionFeedback.COMPLETED) {
                    return true;
                }
            }
        } else if (action instanceof DepositAction || action instanceof HarvestAction) {
            for (int peasantId : peasantIdMap.keySet()) {
                return stateView.getUnit(peasantIdMap.get(peasantId)).getCurrentDurativeAction() == null || stateView.getUnit(peasantIdMap.get(peasantId)).getCurrentDurativeProgress() <= 1;
            }
        } else {
            return stateView.getUnit(townhallId).getCurrentDurativeAction() == null || stateView.getUnit(townhallId).getCurrentDurativeProgress() <= 1;
        }

        return false;
    }

    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }
}

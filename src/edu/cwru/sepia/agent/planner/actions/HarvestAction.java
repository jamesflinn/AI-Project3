package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.ResourceLocation;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.util.Direction;

import java.util.*;

/**
 * Represents the HarvestWood action.
 */
public class HarvestAction implements StripsAction {

    private int peasantID;
    private Direction resourceDirection;

    public HarvestAction(int peasantID) {
        this.peasantID = peasantID;
    }

    /**
     * Preconditions are met if the peasant is adjacent to a resource, if that resource has an amount > 0,
     * and if the peasant is not already carrying anything.
     * @param state GameState to check if action is applicable
     * @return true if preconditions are met
     */
    @Override
    public boolean preconditionsMet(GameState state) {
        Peasant peasant = state.getPeasant(peasantID);
        ResourceLocation adjResource = findAdjResource(peasant, state.getGoldLocations(), state.getTreeLocations());
        return adjResource != null && adjResource.getAmount() > 0 && !peasant.isCarrying();
    }

    /**
     * Creates a new GameState such that the peasant is now carrying resources,
     * and the resource location now has 100 less resources
     * @param state State to apply action to
     * @return the new GameState
     */
    @Override
    public GameState apply(GameState state) {
        Peasant oldPeasant = state.getPeasant(peasantID);
        ResourceLocation adjResource = findAdjResource(oldPeasant, state.getGoldLocations(), state.getTreeLocations());
        resourceDirection = oldPeasant.getPosition().getDirection(adjResource.getPosition());
        System.out.println("Resource Direction: "+ resourceDirection.toString());
        Peasant newPeasant = new Peasant(peasantID, oldPeasant.getPosition(), adjResource.getResourceType());

        List<ResourceLocation> newGoldLocations = new ArrayList<>(state.getGoldLocations());
        List<ResourceLocation> newTreeLocations = new ArrayList<>(state.getTreeLocations());

        if (adjResource.getResourceType() == ResourceNode.Type.GOLD_MINE) {
            addNewResourceToList(newGoldLocations, adjResource);
        } else {
            addNewResourceToList(newTreeLocations, adjResource);
        }

            Map<Integer, Peasant> newPeasantMap = new HashMap<>(state.getPeasantsMap());
            newPeasantMap.put(peasantID, newPeasant);

        Stack<StripsAction> actions = (Stack<StripsAction>) state.getPreviousActions().clone();
        actions.push(this);

        return new GameState(state, newGoldLocations, newTreeLocations, newPeasantMap, state.getCurrentGold(), state.getCurrentWood(), actions);
    }

    /**
     * Adds a new resource to the list, with 100 less resources. If there are no more resources, it is deleted from the list.
     * @param resources The list to be changed
     * @param resource The resource to be changed
     * @return A new list
     */
    private List<ResourceLocation> addNewResourceToList(List<ResourceLocation> resources, ResourceLocation resource) {
        if (resource.getAmount() - 100 > 0) {
            resources.set(
                    resources.indexOf(resource),
                    new ResourceLocation(resource.getPosition(), resource.getResourceType(), resource.getAmount() - 100)
            );
        } else {
            resources.remove(resource);
        }

        return resources;
    }

    /**
     * Finds the resource that is adjacent to the given peasant. Null if there is no such resource.
     * @param peasant The specified peasant
     * @param goldLocations The locations of gold mines
     * @param treeLocations The locations of forests
     * @return The adjacent resource, null if there is no such resource
     */
    private ResourceLocation findAdjResource(Peasant peasant, List<ResourceLocation> goldLocations, List<ResourceLocation> treeLocations) {
        for (ResourceLocation gold : goldLocations) {
            if (peasant.getPosition().isAdjacent(gold.getPosition())) {
                return gold;
            }
        }

        for (ResourceLocation tree : treeLocations) {
            if (peasant.getPosition().isAdjacent(tree.getPosition())) {
                return tree;
            }
        }
        // no adjacent resource found
        return null;
    }

    public int getPeasantID() {
        return peasantID;
    }

    public Direction getResourceDirection() {
        return resourceDirection;
    }

    @Override
    public String toString() {
        return "HarvestAction{" +
                "peasantID=" + peasantID +
                ", resourceDirection=" + resourceDirection +
                '}';
    }
}

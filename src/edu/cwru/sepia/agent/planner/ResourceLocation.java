package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode;

/**
 * Represents a resource on the map.
 */
public class ResourceLocation {
    private int amount;
    private Position position;
    private ResourceNode.Type resourceType;

    public ResourceLocation(Position position, ResourceNode.Type resourceType, int amount) {
        this.position = position;
        this.resourceType = resourceType;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceLocation that = (ResourceLocation) o;

        if (amount != that.amount) return false;
        if (!position.equals(that.position)) return false;
        return resourceType == that.resourceType;

    }

    @Override
    public int hashCode() {
        int result = amount;
        result = 31 * result + position.hashCode();
        result = 31 * result + (resourceType != null ? resourceType.hashCode() : 0);
        return result;
    }

    public int getAmount() {
        return amount;
    }

    public Position getPosition() {
        return position;
    }

    public ResourceNode.Type getResourceType() {
        return resourceType;
    }
}

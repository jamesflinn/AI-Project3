package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode;

/**
 * Holds information about a specific peasant.
 */
public class Peasant {

    private int id;
    private Position position;

    private boolean isCarrying;
    private ResourceNode.Type resourceType;

    public Peasant(int id, Position position) {
        this.id = id;
        this.isCarrying = false;
        this.position = position;
    }

    public Peasant(int id, Position position, ResourceNode.Type resourceType) {
        this.id = id;
        this.position = position;

        this.isCarrying = true;
        this.resourceType = resourceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Peasant peasant = (Peasant) o;

        if (id != peasant.id) return false;
        if (isCarrying != peasant.isCarrying) return false;
        if (!position.equals(peasant.position)) return false;
        return resourceType == peasant.resourceType;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + position.hashCode();
        result = 31 * result + (isCarrying ? 1 : 0);
        result = 31 * result + (resourceType != null ? resourceType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Peasant{" +
                "id=" + id +
                ", position=" + position +
                ", isCarrying=" + isCarrying +
                ", resourceType=" + resourceType +
                '}';
    }

    public boolean isCarrying() {
        return isCarrying;
    }

    public int getID() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public ResourceNode.Type getResourceType() {
        return resourceType;
    }
}

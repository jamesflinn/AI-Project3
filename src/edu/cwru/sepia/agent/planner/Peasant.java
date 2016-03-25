package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode;

/**
 * Holds information about a specific peasant.
 */
public class Peasant {

    private Position position;

    private boolean isCarrying;
    private ResourceNode.Type resourceType;

    public Peasant(Position position) {
        this.isCarrying = false;
        this.position = position;
    }

    public Peasant(Position position, ResourceNode.Type resourceType) {
        this.position = position;

        this.isCarrying = true;
        this.resourceType = resourceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Peasant peasant = (Peasant) o;

        if (isCarrying != peasant.isCarrying) return false;
        return position.equals(peasant.position);

    }

    @Override
    public int hashCode() {
        int result = (isCarrying ? 1 : 0);
        result = 31 * result + position.hashCode();
        return result;
    }

    public boolean isCarrying() {
        return isCarrying;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setCarrying(boolean carrying) {
        isCarrying = carrying;
    }
}

package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode;

/**
 * Represents a resource on the map.
 */
public class ResourceLocation {
    private int amount;
    private Position position;

    public ResourceLocation(Position position, int amount) {
        this.position = position;
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public Position getPosition() {
        return position;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}

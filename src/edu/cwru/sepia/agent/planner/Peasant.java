package edu.cwru.sepia.agent.planner;

/**
 * Holds information about a specific peasant.
 */
public class Peasant {

    private boolean isCarrying;
    private Position position;

    public Peasant(Position position) {
        this.isCarrying = false;
        this.position = position;
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

package routing;

import org.opencv.core.Point;

/**
 * Enum for Driving commands. All enums take the value for the next point so that we
 * know where we should rotate to or drive to.
 */
public enum DriveCommand {
    DRIVE_STRAIGHT(null) ,
    ROTATE(null);

    private Point nextPoint;
    DriveCommand(Point nextPoint) {
        this.nextPoint = nextPoint;
    }

    public Point getNextPoint() {
        return nextPoint;
    }

    public Point setNextPoint(Point nextPoint) { return nextPoint; }
}



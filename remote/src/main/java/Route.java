import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for adding a route from one point to another point.
 * It takes a list of commands from the enum to execute in sequence.
 */
public class Route {
    private int turns;
    private final List<DriveCommand> driveCommands = new ArrayList<>();

    private BallCommand endingCommand;


    public int getTurns() {
        return turns;
    }

    public void setTurns(int turns) {
        this.turns = turns;
    }

    public List<DriveCommand> getDriveCommands() {
        return driveCommands;
    }

    public void addDriveCommandToRoute(DriveCommand driveCommand) {
        this.driveCommands.add(driveCommand);
    }

    public BallCommand getEndingCommand() {
        return endingCommand;
    }

    public void setEndingCommand(BallCommand endingCommand) {
        this.endingCommand = endingCommand;
    }
}

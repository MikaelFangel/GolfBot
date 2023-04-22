import exceptions.MissingArgumentException;

public class Main {
    public static void main(String[] args) throws InterruptedException, MissingArgumentException {
        /*if (args.length < 1) {
            throw new MissingArgumentException("Please provide an IP and port number (e.g 192.168.0.97:50051)");
        }*/

        RobotController controller = new RobotController("192.168.1.12:50051");

        try {
            //controller.driveStraight(30);
            controller.rotate(-45);

        } catch (RuntimeException e) {
            System.out.println("Robot was probably not reached");

        } finally {
            controller.stopController();
        }
    }
}

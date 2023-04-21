public class Main {
    public static void main(String[] args) throws InterruptedException {
        RobotController controller = new RobotController();

        controller.startController("192.168.1.12:50051");

        try {
            controller.driveBackwards(30);

        } catch (Exception e) {
            System.out.println(e.getMessage());

        } finally {
            controller.stopController();
        }
    }
}

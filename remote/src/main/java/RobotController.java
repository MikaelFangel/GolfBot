import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import proto.*;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RobotController {
    private ManagedChannel channel;
    private MotorsGrpc.MotorsBlockingStub client;

    private final int defaultSpeed = 100;

    /**
     * Initializes channel and client to connect with the robot.
     * @param ip_port the ip and port of the robot on the subnet. e.g. 192.168.1.12:50051
     */
    public RobotController(String ip_port) {
        channel = Grpc.newChannelBuilder(ip_port, InsecureChannelCredentials.create()).build();
        client = MotorsGrpc.newBlockingStub(channel);
    }

    /**
     * Use this method to stop the controller before ending the program.
     * @throws InterruptedException if shutdown was interrupted
     */
    public void stopController() throws InterruptedException {
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Makes the robot drive straight either forward or backwards for the distance given as argument
     * @param distance Positive values in cm for forward and negative for backwards
     * @throws RuntimeException if the robot was not reached
     */
    public void driveStraight(double distance) throws RuntimeException {
       ArrayList<MotorRequest> motorsRequest = createMotorRequests(Type.l, Port.A, Port.D);

        DriveRequest driveRequest = DriveRequest.newBuilder()
                .addAllMotors(motorsRequest)
                .setDistance((float) distance)
                .setSpeed(defaultSpeed)
                .build();

        client.drive(driveRequest);
    }

    /**
     * Rotates the robot with itself as its center
     * @param degrees postive values for counter-clockwise and negative for clockwise
     * @throws RuntimeException if the robot was not reached
     */
    public void rotate(double degrees) throws RuntimeException {
        ArrayList<MotorRequest> motorsRequest = createMotorRequests(Type.l, Port.A, Port.D);

        RotateRequest rotateRequest = RotateRequest.newBuilder()
                .addAllMotors(motorsRequest)
                .setDegrees((int) degrees)
                .setSpeed(defaultSpeed)
                .build();

        client.rotate(rotateRequest);

    }

    private ArrayList<MotorRequest> createMotorRequests(Type motorType, Port... ports) {
        ArrayList<MotorRequest> motorRequests = new ArrayList<>();

        for (Port port : ports) {
            motorRequests.add(MotorRequest.newBuilder()
                    .setMotorPort(port)
                    .setMotorType(motorType)
                    .build());
        }

        return motorRequests;
    }
}
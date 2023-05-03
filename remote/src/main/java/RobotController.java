import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import proto.*;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RobotController {
    private final ManagedChannel CHANNEL;
    private final MotorsGrpc.MotorsBlockingStub CLIENT;

    private final int DEFAULT_SPEED = 100;

    /**
     * Initializes channel and client to connect with the robot.
     * @param ip_port the ip and port of the robot on the subnet. e.g. 192.168.1.12:50051
     */
    public RobotController(String ip_port) {
        CHANNEL = Grpc.newChannelBuilder(ip_port, InsecureChannelCredentials.create()).build();
        CLIENT = MotorsGrpc.newBlockingStub(CHANNEL);
    }

    /**
     * Use this method to stop the controller before ending the program.
     * @throws InterruptedException if shutdown was interrupted
     */
    public void stopController() throws InterruptedException {
        CHANNEL.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Makes the robot drive straight either forward or backwards for the distance given as argument
     * @param distance Positive values in cm for forward and negative for backwards
     * @throws RuntimeException if the robot was not reached
     */
    public void driveStraight(double distance) throws RuntimeException {
       ArrayList<MotorRequest> motorsRequest = createMultipleMotorRequest(DEFAULT_SPEED, Type.l, OutPort.A, OutPort.D);

        DriveRequest driveRequest = DriveRequest.newBuilder()
                .addAllMotors(motorsRequest)
                .setDistance((float) distance)
                .setSpeed(DEFAULT_SPEED)
                .build();

        CLIENT.drive(driveRequest);
    }

    /**
     * Rotates the robot with itself as its center
     * @param degrees postive values for counter-clockwise and negative for clockwise
     * @throws RuntimeException if the robot was not reached
     */
    public void rotate(double degrees) throws RuntimeException {
        ArrayList<MotorRequest> motorsRequest = createMultipleMotorRequest(DEFAULT_SPEED, Type.l, OutPort.A, OutPort.D);

        RotateRequest rotateRequest = RotateRequest.newBuilder()
                .addAllMotors(motorsRequest)
                .setDegrees((int) degrees)
                .setSpeed(DEFAULT_SPEED)
                .build();

        CLIENT.rotate(rotateRequest);

    }

    /**
     * Creates an array of motor request.
     * @param motorSpeed Assuming that the wheels always run with the same speed
     * @param motorType Small, Medium or Large
     * @param outPorts A, B, C, D
     * @return an arraylist of motor requests
     */
    private ArrayList<MotorRequest> createMultipleMotorRequest(int motorSpeed, Type motorType, OutPort... outPorts) {
        ArrayList<MotorRequest> motorRequests = new ArrayList<>();

        for (OutPort port : outPorts) {
            motorRequests.add(MotorRequest.newBuilder()
                    .setMotorSpeed(motorSpeed)
                    .setMotorType(motorType)
                    .setMotorPort(port)
                    .build());
        }

        return motorRequests;
    }
}

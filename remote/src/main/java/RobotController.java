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
       ArrayList<MotorRequest> motorsRequest = createMultipleMotorRequests(Type.l, Port.A, Port.D);

        DriveRequest driveRequest = DriveRequest.newBuilder()
                .addAllMotors(motorsRequest)
                .setDistance((float) distance)
                .setSpeed(DEFAULT_SPEED)
                .build();

        CLIENT.drive(driveRequest);
    }

    /**
     * Rotates the robot with itself as its center
     * @param degrees positive values for counter-clockwise and negative for clockwise
     * @throws RuntimeException if the robot was not reached
     */
    public void rotate(double degrees) throws RuntimeException {
        ArrayList<MotorRequest> motorsRequest = createMultipleMotorRequests(Type.l, Port.A, Port.D);

        RotateRequest rotateRequest = RotateRequest.newBuilder()
                .addAllMotors(motorsRequest)
                .setDegrees((int) degrees)
                .setSpeed(DEFAULT_SPEED)
                .build();

        CLIENT.rotate(rotateRequest);

    }

    /**
     * Runs a Grab routine, by first grabbing and the lowering again.
     * @throws InterruptedException if sleep thread is interrupted
     */
    public void grab() throws InterruptedException {
        MotorRequest motorRequest = createSingleMotorRequest(Type.m, Port.B);

        GrabRequest grabRequest = GrabRequest.newBuilder()
                .setMotor(motorRequest)
                .setDegreesOfRotation(-1300)
                .setSpeed(DEFAULT_SPEED)
                .build();
        GrabRequest unGrabRequest = GrabRequest.newBuilder()
                .setMotor(motorRequest)
                .setDegreesOfRotation(1200)
                .setSpeed(DEFAULT_SPEED)
                .build();

        CLIENT.grab(grabRequest);
        Thread.sleep(8000);
        CLIENT.grab(unGrabRequest);

    }

    /**
     * Create ArrayList of MotorRequests.
     * @param motorType the type of motor that will be sat for all ports
     * @param ports the ports which the motors are attached to
     * @return ArrayList of MotorRequests, same length as ports provided.
     */
    private ArrayList<MotorRequest> createMultipleMotorRequests(Type motorType, Port... ports) {
        ArrayList<MotorRequest> motorRequests = new ArrayList<>();

        for (Port port : ports) {
            motorRequests.add(createSingleMotorRequest(motorType, port));
        }

        return motorRequests;
    }

    /**
     * Creates a single Motor Request
     * @param motorType the type of motor that the port connect to.
     * @param port the port which the motor is on.
     * @return MotorRequest
     */
    private MotorRequest createSingleMotorRequest(Type motorType, Port port) {
        return MotorRequest.newBuilder()
                .setMotorType(motorType)
                .setMotorPort(port)
                .setMotorSpeed(DEFAULT_SPEED)
                .build();
    }
}

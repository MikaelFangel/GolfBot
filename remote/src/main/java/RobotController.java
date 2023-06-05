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
       MultipleMotors motorsRequest = createMultipleMotorRequest(Type.l, new MotorPair(OutPort.A, DEFAULT_SPEED),
               new MotorPair(OutPort.D, DEFAULT_SPEED));

        DriveRequest driveRequest = DriveRequest.newBuilder()
                .setMotors(motorsRequest)
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
        MultipleMotors motorsRequest = createMultipleMotorRequest(Type.l, new MotorPair(OutPort.A, DEFAULT_SPEED),
                new MotorPair(OutPort.D, DEFAULT_SPEED));

        RotateRequest rotateRequest = RotateRequest.newBuilder()
                .setMotors(motorsRequest)
                .setDegrees((int) degrees)
                .setSpeed(DEFAULT_SPEED)
                .build();

        try {
            CLIENT.rotate(rotateRequest);
        }
        catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Either collects or releases balls depending on the boolean parameter given
     * @param isCollecting collects if true and releases if false
     */
    public void collectRelease(boolean isCollecting) {
        MultipleMotors motorRequests;
        if (isCollecting) {
            // Just used the greatest speed
            int motorSpeed = -1200;
            motorRequests = createMultipleMotorRequest(Type.m, new MotorPair(OutPort.B, motorSpeed), new MotorPair(OutPort.C, motorSpeed));
        }
        else {
            /* If the front motor is slow, the balls will hit each other and will thereby deviate from expected course.
             * This is because they won't be able to leave the space between the motors before the next ball is
             * released from storage
             */
            int speedFrontMotor = 1200;
            /* The motor releasing the balls from storage should be slow. Otherwise, the balls would hit each other and
             * thereby deviate from expected course
             */
            int speedSideMotors = 300;
            motorRequests = createMultipleMotorRequest(Type.m, new MotorPair(OutPort.B, speedSideMotors), new MotorPair(OutPort.C, speedFrontMotor));
        }

        CLIENT.collectRelease(motorRequests);
    }
    public void releaseOneBall() {
        int speed = 1200;
        MultipleMotors motorRequests = createMultipleMotorRequest(Type.m, new MotorPair(OutPort.B, speed), new MotorPair(OutPort.C, speed));

        CLIENT.releaseOneBall(motorRequests);
    }

    /**
     * Stops the motors collecting/releasing the balls, which currently is port B and C
     */
    public void stopCollectRelease() {
        int motorSpeed = 0;
        MultipleMotors motorRequests = createMultipleMotorRequest(Type.m, new MotorPair(OutPort.B, motorSpeed), new MotorPair(OutPort.C, motorSpeed));
        CLIENT.stopMotors(motorRequests);
    }

    /**
     * Creates an array of motor request.
     * @param motorType Small, Medium or Large
     * @param motorPairs One should be given for each motor you wish to create
     * @return an arraylist of motor requests
     */
    private MultipleMotors createMultipleMotorRequest(Type motorType, MotorPair... motorPairs) {
        ArrayList<MotorRequest> motorRequests = new ArrayList<>();

        for (MotorPair motorPair : motorPairs) {
            motorRequests.add(MotorRequest.newBuilder()
                    .setMotorSpeed(motorPair.motorSpeed)
                    .setMotorType(motorType)
                    .setMotorPort(motorPair.outPort)
                    .build());
        }

        return MultipleMotors.newBuilder()
                .addAllMotor(motorRequests)
                .build();
    }

        /**
         * A record consisting of an outputPort (A, B, C, D) and a speed associated with the port
         */
        private record MotorPair(OutPort outPort, int motorSpeed) {
    }
}

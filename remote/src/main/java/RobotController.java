import courseObjects.Course;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import proto.*;
import vision.Algorithms;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RobotController {
    private final ManagedChannel CHANNEL;
    private final MotorsGrpc.MotorsBlockingStub CLIENT;
    private final MotorsGrpc.MotorsStub ASYNCCLIENT;

    private final int DEFAULT_SPEED = 100;

    /**
     * Initializes channel and client to connect with the robot.
     * @param ip_port the ip and port of the robot on the subnet. e.g. 192.168.1.12:50051
     */
    public RobotController(String ip_port) {
        CHANNEL = Grpc.newChannelBuilder(ip_port, InsecureChannelCredentials.create()).build();
        CLIENT = MotorsGrpc.newBlockingStub(CHANNEL);
        ASYNCCLIENT = MotorsGrpc.newStub(CHANNEL);
    }

    /**
     * Use this method to stop the controller before ending the program.
     * @throws InterruptedException if shutdown was interrupted
     */
    public void stopController() throws InterruptedException {
        CHANNEL.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Makes the robot drive straight either forward or backwards by using the gyro
     * //@param distance Positive values in cm for forward and negative for backwards
     * @throws RuntimeException if the robot was not reached
     */
    public void driveWGyro(Course course) throws RuntimeException {
        int speed = 200;
        MultipleMotors motorsRequest = createMultipleMotorRequest(Type.l, new MotorPair(OutPort.A, speed),
                new MotorPair(OutPort.D, speed));

        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<StatusReply> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(StatusReply statusReply) {
                System.out.println("Ok " + statusReply.getReplyMessage());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Something failed...!" + Status.fromThrowable(t));
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Finished");
                finishLatch.countDown();
            }
        };

        StreamObserver<DriveRequest> requestObserver = ASYNCCLIENT.driveWGyro(responseObserver);

        double distance = Algorithms.findRobotsDistanceToBall(course.getRobot(), course.getBalls().get(0));
        int failsave = 0;
        try {
            // Countdown distance, simulates distance to target.
            while (distance > 0 && failsave < 15){

                distance = Algorithms.findRobotsDistanceToBall(course.getRobot(), course.getBalls().get(0));

                DriveRequest drivePIDRequest = DriveRequest.newBuilder()
                        .setMotors(motorsRequest)
                        .setDistance((float) distance) // Note: Currently not used on the robot
                        .setSpeed(speed) // This speed worked well, other speeds could be researched
                        .build();

                requestObserver.onNext(drivePIDRequest);
                System.out.println("Send distance value " + distance);
                // Sleep for a bit before sending the next one.
                Thread.sleep(700);
                if (finishLatch.getCount() == 0) {
                    // RPC completed or errored before we finished sending.
                    // Sending further requests won't error, but they will just be thrown away.
                    return;
                }
                failsave++;
            }
        } catch (RuntimeException e) {
            // Cancel RPC
            requestObserver.onError(e);
            throw e;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Mark the end of requests
        requestObserver.onCompleted();

        try {
            // Receiving happens asynchronously
            if (!finishLatch.await(1, TimeUnit.MINUTES)) {
                System.out.println("recordRoute can not finish within 1 minutes");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopMotors() {
        int motorSpeed = 0;
        MultipleMotors motorRequests = createMultipleMotorRequest(Type.l, new MotorPair(OutPort.A, motorSpeed), new MotorPair(OutPort.D, motorSpeed));
        CLIENT.stopMotors(motorRequests);
    }

    /**
     * Rotates the robot with itself as its center
     * @param degrees postive values for counter-clockwise and negative for clockwise
     * @throws RuntimeException if the robot was not reached
     */
    public void rotateWGyro(double degrees) throws RuntimeException {
        int speed = 5;
        MultipleMotors motorsRequest = createMultipleMotorRequest(Type.l, new MotorPair(OutPort.A, speed),
                new MotorPair(OutPort.D, speed));

        RotateRequest rotateRequest = RotateRequest.newBuilder()
                .setMotors(motorsRequest)
                .setDegrees((int) degrees)
                .setSpeed(speed)
                .build();

        try {
            CLIENT.rotateWGyro(rotateRequest);
        }
        catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Recalibrates the
     * @throws RuntimeException if the robot was not reached
     */
    public void recalibrateGyro() throws RuntimeException {
        EmptyRequest emptyRequest = EmptyRequest.newBuilder().build();

        try {
            CLIENT.recalibrateGyro(emptyRequest);
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

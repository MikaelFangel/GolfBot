package routing;

import configs.GlobalConfig;

import courseObjects.Robot;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.opencv.core.Point;
import proto.*;
import vision.Algorithms;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RobotController {
    private final ManagedChannel CHANNEL;
    private final MotorsGrpc.MotorsBlockingStub CLIENT;
    private final MotorsGrpc.MotorsStub ASYNCCLIENT;
    private final Robot robot;

    private int numberOfBallsOnCourseBeforeRoutine;
    /**
     * Initializes channel and client to connect with the robot.
     */
    public RobotController(Robot robot) {
        this.CHANNEL = Grpc.newChannelBuilder(
                GlobalConfig.getConfigProperties().getProperty("ipPort"),
                InsecureChannelCredentials.create()
        ).build();
        this.CLIENT = MotorsGrpc.newBlockingStub(CHANNEL);
        this.ASYNCCLIENT = MotorsGrpc.newStub(CHANNEL);

        this.numberOfBallsOnCourseBeforeRoutine = 0;
        this.robot = robot;
    }

    /**
     * Use this method to stop the controller before ending the program.
     *
     * @throws InterruptedException if shutdown was interrupted
     */
    public void stopController() throws InterruptedException {
        CHANNEL.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Drive robot straight either forwardby using the gyro and streaming distance to the robot
     *
     * @param target Destination
     * @param calculateFromFront if true, distances are calculated from front marker, otherwise from the center marker
     * @param defaultSpeed speed to drive when distance < 5. Use low speed when accuracy is needed.
     * @param powerFactor multiplier on default speed when far away from target
     * @throws RuntimeException If the robot was not reached
     * @see <a href="https://github.com/grpc/grpc-java/blob/master/examples/src/main/java/io/grpc/examples/routeguide/RouteGuideClient.java">Example streaming client</a>
     */
    public void drive(Point target, boolean calculateFromFront, int defaultSpeed, int powerFactor) throws RuntimeException {
        int MAX_ITERATIONS = 100; // Used for failsafe

        MultipleMotors motorsRequest = createMultipleMotorRequest(Type.l, new MotorPair(OutPort.A, defaultSpeed),
                new MotorPair(OutPort.D, defaultSpeed));

        // Use gRPCs StreamObserver interface
        StreamObserver<DrivePIDRequest> requestObserver = initStreamObserver();

        try {

            double distanceToTarget = Algorithms.findRobotsDistanceToPoint(this.robot, target, calculateFromFront);
            Point robotStartPos = calculateFromFront ? this.robot.getFront() : this.robot.getCenter();
            double drivenDistance;
            int distanceLeft;
            int offset = calculateFromFront ? 3 : 0; // Offset from front marker to the front collector

            /* Continue to stream messages until reaching target
             * Iterator used as a failsafe */
            for (int i = 0; i < MAX_ITERATIONS; i++) {
                // Distance from starting point of the robot and where the robot currently is
                drivenDistance = Algorithms.findRobotsDistanceToPoint(this.robot, robotStartPos, calculateFromFront);
                distanceLeft = (int) (distanceToTarget - drivenDistance) - offset; // TODO: Test with in and double -> Precision in when we break from the loop

                if (distanceToTarget <= drivenDistance || distanceLeft <= 0)
                    break;

                DrivePIDRequest drivePIDRequest = DrivePIDRequest.newBuilder()
                        .setMotors(motorsRequest)
                        .setDistance((float) (distanceLeft))
                        .setSpeed(setPowerInDrive(distanceLeft, powerFactor, defaultSpeed))
                        .build();

                // Send request
                requestObserver.onNext(drivePIDRequest);

                // Sleep for a bit before sending the next one.
                // TODO: Research timing of the delay with the robot. ALSO check if still needed
                Thread.sleep(300);
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
    }

    /**
     * Reverse robot for 1 second, applicable for corner, wall, and cross collection cases
     * Method is based on drive()
     */
    public void reverse() {
        int speed = -100;
        double distance = 2;
        MultipleMotors motorsRequest = createMultipleMotorRequest(Type.l, new MotorPair(OutPort.A, speed),
                new MotorPair(OutPort.D, speed));
        StreamObserver<DrivePIDRequest> requestObserver = initStreamObserver();

        recalibrateGyro();
        try {
            do { DrivePIDRequest drivePIDRequest = DrivePIDRequest.newBuilder()
                    .setMotors(motorsRequest)
                    .setDistance((float) distance)
                    .setSpeed(speed)
                    .build();

                // Send request
                requestObserver.onNext(drivePIDRequest);
                Thread.sleep(1000);
                distance--;
            } while (distance != 0);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }


        // End of requests
        requestObserver.onCompleted();
    }

    /**
     * Calculates the power to motors, power is capped at 400
     * @param distance distance left to drive. Used to power down to default speed when close to target
     * @param powerFactor multiplier on default speed when far away from target
     * @param defaultSpeed speed to drive when distance < 5. Use low speed when accuracy is needed.
     * @return power value on motors, capped at 400
     */
    private int setPowerInDrive(double distance, int powerFactor, int defaultSpeed) {
        int power = defaultSpeed;
        // For reverse
        if (distance < 0) {
            power *= -1;
        }

        // Power down in 3 steps
        if (Math.abs(distance) > 40) {
            power *= powerFactor;
        } else if (Math.abs(distance) > 15) {
            power *= (double) powerFactor / 2;
        } else if (Math.abs(distance) > 5) {
            power *= (double) powerFactor / 4;
        }

        // Failsafe to avoid too much power
        int powerCap = 400;
        if(power > powerCap) power = powerCap;
        if(power < -powerCap) power = -powerCap;

        return power;
    }

    /**
     * Uses gRPCs StreamObserver interface
     * @return requestObserver for sending requests
     */
    private StreamObserver<DrivePIDRequest> initStreamObserver() {
        StreamObserver<DrivePIDRequest> requestObserver = ASYNCCLIENT.drive(new StreamObserver<>() {
            @Override
            public void onNext(StatusReply statusReply) {
                System.out.println("Ok " + statusReply.getReplyMessage());
            }
            @Override
            public void onError(Throwable t) {
                System.out.println("Something failed...!" + Status.fromThrowable(t));
            }
            @Override
            public void onCompleted() {
                System.out.println("Finished");
            }
        });
        return requestObserver;
    }

    /**
     * Stops the Motor A and D and thus the driving
     */
    public void stopMotors() {
        int motorSpeed = 0;
        MultipleMotors motorRequests = createMultipleMotorRequest(Type.l, new MotorPair(OutPort.A, motorSpeed), new MotorPair(OutPort.D, motorSpeed));
        CLIENT.stopMotors(motorRequests);
    }

    /**
     * Rotates the robot with itself as its center
     *
     * @param degrees postive values for counter-clockwise and negative for clockwise
     * @throws RuntimeException if the robot was not reached
     */
    public void rotate(double degrees) throws RuntimeException {
        int speed = 5;
        MultipleMotors motorsRequest = createMultipleMotorRequest(Type.l, new MotorPair(OutPort.A, speed),
                new MotorPair(OutPort.D, speed));

        RotateRequest rotateRequest = RotateRequest.newBuilder()
                .setMotors(motorsRequest)
                .setDegrees((int) degrees)
                .setSpeed(speed)
                .build();

        try {
            CLIENT.rotate(rotateRequest);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Recalibrates the gyro
     *
     * @throws RuntimeException if the robot was not reached
     */
    public void recalibrateGyro() throws RuntimeException {
        EmptyRequest emptyRequest = EmptyRequest.newBuilder().build();

        try {
            CLIENT.recalibrateGyro(emptyRequest);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Either collects or releases balls depending on the boolean parameter given
     *
     * @param isCollecting collects if true and releases if false
     */
    public void collectRelease(boolean isCollecting) {
        MultipleMotors motorRequests;
        if (isCollecting) {
            // Just used the greatest speed
            int motorSpeed = -1200;
            motorRequests = createMultipleMotorRequest(Type.m, new MotorPair(OutPort.B, motorSpeed), new MotorPair(OutPort.C, motorSpeed));
        } else {
            // Empty robot magazine counter
            robot.setNumberOfBallsInMagazine(0);

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
     * Collect balls located in the corners by shooting the corner ball with another ball and collect balls returning
     *
     * @throws InterruptedException Can happen when sleeping
     * TODO! Move to Routine class to made yet
     */
    public void collectCornerBalls() throws InterruptedException {
        releaseOneBall();
        collectRelease(true);
        Thread.sleep(2000); // Can be adjusted. How long we collect
        stopCollectRelease();
    }

    public void releaseOneBall() {
        int speed = 1200;
        MultipleMotors motorRequests = createMultipleMotorRequest(Type.m, new MotorPair(OutPort.B, speed), new MotorPair(OutPort.C, speed));

        this.CLIENT.releaseOneBall(motorRequests);

        // Remove one ball from magazine
        this.robot.addOrRemoveNumberOfBallsInMagazine(-1);
        this.numberOfBallsOnCourseBeforeRoutine++;
    }

    /**
     * Stops the motors collecting/releasing the balls, which currently is port B and C
     */
    public void stopCollectRelease() {
        int motorSpeed = 0;
        MultipleMotors motorRequests = createMultipleMotorRequest(Type.m, new MotorPair(OutPort.B, motorSpeed), new MotorPair(OutPort.C, motorSpeed));
        try {
            StatusReply reply = this.CLIENT.stopMotors(motorRequests);
            if (!reply.getReplyMessage())
                System.out.println("An error occurred");
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Needs to be called before starting the collection routine.
     * @param numOfCourseBalls The number of balls on the course
     */
    public void startMagazineCounting(int numOfCourseBalls) {
        this.numberOfBallsOnCourseBeforeRoutine = numOfCourseBalls;
    }

    /**
     * Should be called after the collection routine.
     * @param numOfCourseBalls The number of balls on the course.
     */
    public void endMagazineCounting(int numOfCourseBalls) {

        if (numOfCourseBalls < this.numberOfBallsOnCourseBeforeRoutine) {
            int diff = this.numberOfBallsOnCourseBeforeRoutine - numOfCourseBalls;

            // Add diff to magazine counter
            this.robot.addOrRemoveNumberOfBallsInMagazine(diff);
        }
    }

    /**
     * Creates an array of motor request.
     *
     * @param motorType  Small, Medium or Large
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

    public Robot getRobot() {
        return robot;
    }
}

package routing;

import configs.GlobalConfig;
import courseObjects.Ball;
import courseObjects.Course;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RobotController {
    private final ManagedChannel CHANNEL;
    private final MotorsGrpc.MotorsBlockingStub CLIENT;
    private final MotorsGrpc.MotorsStub ASYNCCLIENT;
    private final int MAX_ITERATIONS;
    private final Course course;
    private final Robot robot;

    private int numberOfBallsOnCourseBeforeRoutine;
    /**
     * Initializes channel and client to connect with the robot.
     */
    public RobotController(Course course) {
        this.CHANNEL = Grpc.newChannelBuilder(
                GlobalConfig.getConfigProperties().getProperty("ipPort"),
                InsecureChannelCredentials.create()
        ).build();
        this.CLIENT = MotorsGrpc.newBlockingStub(CHANNEL);
        this.ASYNCCLIENT = MotorsGrpc.newStub(CHANNEL);

        this.MAX_ITERATIONS = 100; // TODO: Make local if only used in drive?

        this.numberOfBallsOnCourseBeforeRoutine = 0;
        this.course = course;
        this.robot = course.getRobot();
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
     * @throws RuntimeException If the robot was not reached
     * @see <a href="https://github.com/grpc/grpc-java/blob/master/examples/src/main/java/io/grpc/examples/routeguide/RouteGuideClient.java">Example streaming client</a>
     */
    public void drive(Point target, boolean calculateFromFront) throws RuntimeException {
        System.out.println("Target: " + target);
        int speed = 30; // TODO: Experiment with speeds -> precision
        MultipleMotors motorsRequest = createMultipleMotorRequest(Type.l, new MotorPair(OutPort.A, speed),
                new MotorPair(OutPort.D, speed));

        // Use gRPCs StreamObserver interface
        StreamObserver<DrivePIDRequest> requestObserver = initStreamObserver();

        try {
            // Margin from the border in CM, that always should stop the robot from hitting the border
            double borderMargin = calculateFromFront ? 11 : 20; // TODO: Might need to be adjusted and make one for (right and left) and one for (top and bottom)
            // Get corners, TopLeft, BottomRight
            Point[] corners = this.course.getBorder().getCornersAsArray();
            Point TL = corners[0];
            Point BR = corners[3];


            double distanceToTarget = Algorithms.findRobotsDistanceToPoint(this.robot, target, calculateFromFront);
            Point robotStartPos = calculateFromFront ? this.robot.getFront() : this.robot.getCenter();
            Point robotNewPos;
            double drivenDistance;
            double distanceLeft;
            // Offset from front marker to the front collector
            double offset = calculateFromFront ? 2 : 0;


            /* Continue to stream messages until reaching target
             * Iterator used as a failsafe */
            for (int i = 0; i < MAX_ITERATIONS; i++) {
                drivenDistance = Algorithms.findRobotsDistanceToPoint(this.robot, robotStartPos, calculateFromFront);
                distanceLeft = (distanceToTarget - drivenDistance) - offset; // TODO: Test with int and double -> precision in when we break from the loop


                System.err.println("DistanceLeft: " + distanceLeft); // TODO: Delete


                // Distance from starting point of the robot and where the robot currently is
                System.out.println("Distance driven so far: " + drivenDistance); // TODO: Delete
                System.out.println("Distance to go: " + (distanceToTarget - drivenDistance)); // TODO: Delete

                robotNewPos = calculateFromFront ? this.robot.getFront() : this.robot.getCenter();
                // Stop before top border or left border
                if (robotNewPos.y <= TL.y + borderMargin || robotNewPos.x <= TL.x + borderMargin) {
                    System.out.println("TOP or LEFT border"); // TODO: Delete
                    break;
                }
                // Stop before bottom border or right border
                if (robotNewPos.y >= BR.y - borderMargin || robotNewPos.x >= BR.x - borderMargin) {
                    System.out.println("BOTTOM or RIGHT border"); // TODO: Delete
                    break;
                }

                if (distanceToTarget <= drivenDistance) { // Breaks here if distanceLeft didn't make it break a loop earlier
                    System.out.println("THREE"); // TODO: Delete
                    break;
                }

                if (distanceLeft <= 0) { // Usually breaks here unless stopping before hitting the border
                    System.out.println("FOUR"); // TODO: Delete
                    break;
                }

                DrivePIDRequest drivePIDRequest = DrivePIDRequest.newBuilder()
                        .setMotors(motorsRequest)
                        .setDistance((float) (distanceLeft))
                        .setSpeed(speed) // This speed worked well, other speeds could be researched
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

        System.out.println("STOP");
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

        CLIENT.releaseOneBall(motorRequests);

        // Remove one ball from magazine
        robot.addOrRemoveNumberOfBallsInMagazine(-1);
    }

    /**
     * Stops the motors collecting/releasing the balls, which currently is port B and C
     */
    public void stopCollectRelease() {
        int motorSpeed = 0;
        MultipleMotors motorRequests = createMultipleMotorRequest(Type.m, new MotorPair(OutPort.B, motorSpeed), new MotorPair(OutPort.C, motorSpeed));
        try {
            StatusReply reply = CLIENT.stopMotors(motorRequests);
            if (!reply.getReplyMessage())
                System.out.println("An error occurred");
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Needs to be called before starting the collection routine.
     * @param courseBalls The List of Balls from Course.
     */
    public void startMagazineCounting(List<Ball> courseBalls) {
        this.numberOfBallsOnCourseBeforeRoutine = courseBalls.size();
    }

    /**
     * Should be called after the collection routine.
     * @param courseBalls The List of Balls from Course.
     */
    public void endMagazineCounting(List<Ball> courseBalls) {
        int numberOfBallsOnCourseAfterRoutine = courseBalls.size();

        if (numberOfBallsOnCourseAfterRoutine < this.numberOfBallsOnCourseBeforeRoutine) {
            int diff = this.numberOfBallsOnCourseBeforeRoutine - numberOfBallsOnCourseAfterRoutine;

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

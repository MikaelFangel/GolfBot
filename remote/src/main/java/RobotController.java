import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import proto.*;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RobotController {
    private ManagedChannel channel;
    private MotorsGrpc.MotorsBlockingStub client;


    private final int speed = 200;

    /**
     * Used thsi method to start the controller before calling any other methods
     * @param ip_port e.g. "192.168.1.12:50051"
     */
    public void startController(String ip_port) {
        channel = Grpc.newChannelBuilder(ip_port, InsecureChannelCredentials.create()).build();
        client = MotorsGrpc.newBlockingStub(channel);
    }

    /**
     * Use this method to stop the controller before ending the program.
     * @throws InterruptedException
     */
    public void stopController() throws InterruptedException {
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void driveStraight(double distance) throws Exception {
        if (client == null) throw new Exception();

        ArrayList<MotorRequest> motorsRequest = createMotorRequests(Type.l, Port.A, Port.D);

        DriveRequest driveRequest = DriveRequest.newBuilder()
                .setDistance((float) distance)
                .addAllMotors(motorsRequest)
                .setSpeed(speed)
                .build();

        client.drive(driveRequest);
    }

    public void driveBackwards(double distance) throws Exception {
        driveStraight(-distance);
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

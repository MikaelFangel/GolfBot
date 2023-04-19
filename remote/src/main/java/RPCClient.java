import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import proto.*;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RPCClient {
    public static void main(String[] args) throws InterruptedException, MissingArgumentException {
        if (args.length < 1) {
            throw new MissingArgumentException("Please provide an IP and port number (e.g 192.168.0.97:50051)");
        }

        // Needed setup to communicate with the robot
        ManagedChannel channel = Grpc.newChannelBuilder(args[0], InsecureChannelCredentials.create()).build();
        MotorsGrpc.MotorsBlockingStub client = MotorsGrpc.newBlockingStub(channel);

        // Make MotorRequest object
        int speed = -250;
        MotorRequest reqA = MotorRequest.newBuilder().setMotorPort(Port.A).setMotorType(Type.l).setMotorSpeed(speed).build();
        MotorRequest reqD = MotorRequest.newBuilder().setMotorPort(Port.D).setMotorType(Type.l).setMotorSpeed(speed).build();

        MotorRequest reqA2 = MotorRequest.newBuilder().setMotorPort(Port.A).setMotorType(Type.l).setMotorSpeed(-speed).build();
        MotorRequest reqD2 = MotorRequest.newBuilder().setMotorPort(Port.D).setMotorType(Type.l).setMotorSpeed(-speed).build();

        // Put motorRequests into an array
        ArrayList<MotorRequest> requests = new ArrayList<>();
        requests.add(reqA);
        requests.add(reqD);

        ArrayList<MotorRequest> requests2 = new ArrayList<>();
        requests2.add(reqA2);
        requests2.add(reqD2);

        // Make MultipleMotors object
        MultipleMotors motors = MultipleMotors.newBuilder().addAllMotor(requests).build();
        MultipleMotors motors2 = MultipleMotors.newBuilder().addAllMotor(requests2).build();

        // Make RotateRequest object
        RotateRequest rotateRequest = RotateRequest.newBuilder().setMultipleMotors(motors).setDegrees(360).build();

        // Test the robot
        try {
            StatusReply a = client.runMotors(motors);

            //client.rotate(rotateRequest);

            Thread.sleep(2000);

            client.stopMotors(motors);

            //Thread.sleep(1000);

            //client.runMotors(motors2);

            //Thread.sleep(2000);

            //client.stopMotors(motors2);

        }
        // Catch exceptions returned from the robot
        catch (StatusRuntimeException e) {
            System.out.println(e.getMessage());
            System.exit(20);
        }
        finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    static class MissingArgumentException extends Exception {
        public MissingArgumentException(String errMsg) {
            super(errMsg);
        }
    }
}

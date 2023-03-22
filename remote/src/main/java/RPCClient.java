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

        ManagedChannel channel = Grpc.newChannelBuilder(args[0], InsecureChannelCredentials.create()).build();

        int speed = 500;
        MotorRequest reqA = MotorRequest.newBuilder().setMotorPort(Port.A).setMotorSpeed(speed).build();
        MotorRequest reqD = MotorRequest.newBuilder().setMotorPort(Port.D).setMotorSpeed(speed).build();

        speed = 0;
        MotorRequest reqA2 = MotorRequest.newBuilder().setMotorPort(Port.A).setMotorSpeed(speed).build();
        MotorRequest reqD2 = MotorRequest.newBuilder().setMotorPort(Port.D).setMotorSpeed(speed).build();

        ArrayList<MotorRequest> requests = new ArrayList<>();
        requests.add(reqA);
        requests.add(reqD);

        ArrayList<MotorRequest> requests2 = new ArrayList<>();
        requests2.add(reqA2);
        requests2.add(reqD2);

        MultipleMotors motors = MultipleMotors.newBuilder().addAllMotor(requests).build();

        MultipleMotors motors2 = MultipleMotors.newBuilder().addAllMotor(requests2).build();

        MotorsGrpc.MotorsBlockingStub client = MotorsGrpc.newBlockingStub(channel);

        try {
            StatusReply a = client.runMotors(motors);

            Thread.sleep(1000);

            client.runMotors(motors2);

            //client.stopMotors(motors);

        } catch (StatusRuntimeException e) {
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

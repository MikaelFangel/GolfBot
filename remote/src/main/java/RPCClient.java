import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import proto.*;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RPCClient {
    public static void main(String[] args) throws InterruptedException, MissingArgumentException {
        if (args.length < 1) {
            throw new MissingArgumentException("Please provide an IP and port number (e.g 192.168.0.97:50051)");
        }

        ManagedChannel channel = Grpc.newChannelBuilder(args[0], InsecureChannelCredentials.create()).build();

        MotorRequest reqA = MotorRequest.newBuilder().setMotorPort(Port.A).setMotorSpeed(500).setMotorType(Type.medium).build();
        MotorRequest reqB = MotorRequest.newBuilder().setMotorPort(Port.A).setMotorSpeed(500).setMotorType(Type.medium).build();

        ArrayList<MotorRequest> requests = new ArrayList<>();
        requests.add(reqA);
        requests.add(reqB);

        MultipleMotors motors = MultipleMotors.newBuilder().addAllMotor(requests).build();

        MotorsGrpc.MotorsBlockingStub client = MotorsGrpc.newBlockingStub(channel);

        try {
            StatusReply a = client.runMotors(motors);

            Thread.sleep(1000);

            client.stopMotors(motors);

        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    static class MissingArgumentException extends Exception {
        public MissingArgumentException(String errMsg) {
            super(errMsg);
        }
    }
}

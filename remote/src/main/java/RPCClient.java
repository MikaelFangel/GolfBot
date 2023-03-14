import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import proto.MotorRequest;
import proto.MotorsGrpc;
import java.util.concurrent.TimeUnit;

public class RPCClient {
    public static void main(String[] args) throws InterruptedException, MissingArgumentException {
        if (args.length < 1) {
            throw new MissingArgumentException("Please provide an IP and port number (e.g 192.168.0.97:50051)");
        }

        ManagedChannel channel = Grpc.newChannelBuilder(args[0], InsecureChannelCredentials.create()).build();

        MotorRequest reqA = MotorRequest.newBuilder().setMotorPort("A").build();
        MotorRequest reqB = MotorRequest.newBuilder().setMotorPort("B").build();

        MotorsGrpc.MotorsBlockingStub client = MotorsGrpc.newBlockingStub(channel);

        try {
            client.runMotor(reqA);
            client.runMotor(reqB);

            Thread.sleep(1000);

            client.stopMotor(reqA);
            client.stopMotor(reqB);

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

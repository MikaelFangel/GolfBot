import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import proto.MotorRequest;
import proto.MotorsGrpc;

import java.util.concurrent.TimeUnit;

public class RPCClient {
    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = Grpc.newChannelBuilder("192.168.0.97:50051", InsecureChannelCredentials.create()).build();

        MotorRequest reqA = MotorRequest.newBuilder().setMotor("A").build();
        MotorRequest reqB = MotorRequest.newBuilder().setMotor("D").build();

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
}

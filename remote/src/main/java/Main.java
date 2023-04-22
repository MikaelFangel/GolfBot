import exceptions.MissingArgumentException;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import proto.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, MissingArgumentException {
        /*if (args.length < 1) {
            throw new MissingArgumentException("Please provide an IP and port number (e.g 192.168.0.97:50051)");
        }*/

        RobotController controller = new RobotController("192.168.1.12:50051");

        // TODO DELETE
        Channel channel = Grpc.newChannelBuilder("192.168.1.12:50051", InsecureChannelCredentials.create()).build();
        MotorsGrpc.MotorsBlockingStub client = MotorsGrpc.newBlockingStub(channel);

        MotorRequest reqB = MotorRequest.newBuilder().setMotorPort(Port.B).setMotorType(Type.m).setMotorSpeed(100).build();
        GrabRequest grabRequest = GrabRequest.newBuilder().setSpeed(300).setMotor(reqB).setDegreesOfRotation(-200).build();
        GrabRequest unGrabRequest = GrabRequest.newBuilder().setSpeed(300).setMotor(reqB).setDegreesOfRotation(1200).build();

        try {
            client.grab(unGrabRequest);
            //controller.rotate(-190);

        } catch (RuntimeException e) {
            System.out.println("Robot was probably not reached");

        } finally {
            controller.stopController();
        }
    }
}

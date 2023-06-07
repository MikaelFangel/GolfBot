import exceptions.MissingArgumentException;
import courseObjects.*;
import vision.*;

import java.util.Scanner;

import static vision.Algorithms.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, MissingArgumentException {
        if (args.length < 1) {
            throw new MissingArgumentException("Please provide an IP and port number (e.g 192.168.1.12:50051)");
        }
    }
}

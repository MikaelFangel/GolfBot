import courseObjects.Border;
import courseObjects.Course;
import nu.pattern.OpenCV;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import vision.Algorithms;

import java.util.Scanner;

public class AlgorithmsTest {
    Course course;

    @BeforeEach
    public void setup(){
        course = new Course(200);
    }

    @Test
    public void correctedCoordinatesOfObjectTest1(){
        Point  newPoint = Algorithms.correctedCoordinatesOfObject(
                new Point(20,20),
                new Point(150, 150),
                40,
                165
        );
        System.out.println(newPoint.toString());
    }

    @Test
    public void correctedCoordinatesOfObjectTest2(){
        Point  newPoint = Algorithms.correctedCoordinatesOfObject(
                new Point(200,200),
                new Point(150, 150),
                40,
                165
        );
        System.out.println(newPoint.toString());
    }

    @Test
    public void transformToRectangleTest1(){
        //System.out.println("Working Directory = " + System.getProperty("user.dir"));
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //String filename = System.getProperty("user.dir") + "/src/test/java/resources/courseTest.png";
        //Mat src = Imgcodecs.imread(filename);

        OpenCV.loadLocally();

        VideoCapture vc = new VideoCapture();
        vc.open(2);

        // Set capture resolution
        vc.set(Videoio.CAP_PROP_FRAME_WIDTH, course.getResolutionWidth());
        vc.set(Videoio.CAP_PROP_FRAME_HEIGHT, course.getResolutionHeight());

        if (!vc.isOpened()) System.exit(0);

        Mat src = new Mat();
        vc.read(src);


        /*if (src.empty()){
            System.out.println("Cannot read image:" + filename);
            System.exit(0);
        }
         */

        Point[] corners = new Point[4];
        corners[0] = new Point(197,42);
        corners[1] = new Point(820,50);
        corners[2] = new Point(195,505);
        corners[3] = new Point(850,525);

        //Offset for testing
        /*int offset = 100;
        corners[0].x -= offset;
        corners[0].y -= offset;
        corners[1].x += offset;
        corners[1].y -= offset;
        corners[2].x -= offset;
        corners[2].y += offset;


         */
        Border border = new Border(
                corners[0],
                corners[1],
                corners[2],
                corners[3]
        );

        Course course = new Course(167);
        course.setBorder(border);

        Imgproc.circle(src,corners[3],4, new Scalar(255,0,0));

        Mat dst = Algorithms.transformToRectangle(src,course);

        HighGui.imshow("original",src);
        //HighGui.imshow("warp", dst);


        HighGui.waitKey();


    }
}

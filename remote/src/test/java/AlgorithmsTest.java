import courseObjects.Border;
import courseObjects.Course;
import nu.pattern.OpenCV;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import vision.Algorithms;

public class AlgorithmsTest {
    Course course;

    @BeforeEach
    public void setup(){
        course = new Course();
    }

    @Test
    public void correctedCoordinatesOfObjectTest1(){
        Point  newPoint = Algorithms.correctedCoordinatesOfObject(
                new Point(20,20),
                new Point(150, 150),
                40,
                165
        );
        Assertions.assertEquals((int) newPoint.x, 51);
        Assertions.assertEquals((int) newPoint.y, 51);
    }

    @Test
    public void correctedCoordinatesOfObjectTest2(){
        Point  newPoint = Algorithms.correctedCoordinatesOfObject(
                new Point(200,200),
                new Point(150, 150),
                40,
                165
        );
        Assertions.assertEquals((int) newPoint.x, 187);
        Assertions.assertEquals((int) newPoint.y, 187);
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


        Course course = new Course();
        Border border = course.getBorder();
        border.setTopLeft(new Point(197,42));
        border.setTopRight(new Point(820,50));
        border.setBottomLeft(new Point(195,505));
        border.setBottomRight(new Point(850,525));

        Imgproc.circle(src,border.getBottomRight(),4, new Scalar(255,0,0));

        Mat dst = Algorithms.transformToRectangle(src, course.getBorder());

        HighGui.imshow("original",src);
        //HighGui.imshow("warp", dst);

        HighGui.waitKey();
    }
}

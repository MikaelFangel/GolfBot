package vision.detection;

import org.opencv.core.Mat;
import vision.helperClasses.MaskSet;

import java.util.List;

public interface SubDetector {
    List<MaskSet> getMaskSets();
}

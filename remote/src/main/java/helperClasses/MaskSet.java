package helperClasses;

import org.opencv.core.Mat;

public class MaskSet {
    private String maskName;
    private Mat mask;

    public MaskSet(String maskName, Mat mask) {
        this.maskName = maskName;
        this.mask = mask;
    }

    public Mat getMask() {
        return mask;
    }

    public String getMaskName() {
        return maskName;
    }
}

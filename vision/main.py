import numpy as np
import cv2 as cv

# https://docs.opencv.org/4.x/da/d53/tutorial_py_houghcircles.html

video = cv.VideoCapture(0)


def getCirclesFromFrames(frame):
    # TODO: Explore different blurs
    blurFrame = cv.GaussianBlur(frame, (9, 9), 0)
    # blurFrame = cv.medianBlur(frame, 11)
    grayFrame = cv.cvtColor(blurFrame, cv.COLOR_BGR2GRAY)

    # These configurations works okay with the current course setup
    circles = cv.HoughCircles(image=grayFrame,
                              method=cv.HOUGH_GRADIENT,
                              dp=1,
                              minDist=50,
                              param1=25,  # gradient value used in the edge detection
                              param2=17,  # lower values allow more circles to be detected (false positives)
                              minRadius=4,  # limits the smallest circle to this size (via radius)
                              maxRadius=8  # similarly sets the limit for the largest circles
                              )

    if circles is not None:
        # Round the values to unsigned integers
        circles = np.uint16(np.around(circles))
        for (x, y, r) in circles[0, :]:
            # draw the circle
            cv.circle(frame, (x, y), r, (0, 255, 0), 2)


while True:
    # grab the current frame
    ret, frame = video.read()
    if not ret:
        break

    getCirclesFromFrames(frame)
    #getCourseFromFramesWithContours(frame)
    #getCourseFromFramesWithHoughP(frame)

    cv.imshow("frame", frame)
    if cv.waitKey(1) & 0xFF == ord('q'):
        break

video.release()
cv.destroyAllWindows()

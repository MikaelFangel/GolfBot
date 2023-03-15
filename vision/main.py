import numpy as np
import cv2 as cv

video = cv.VideoCapture(0)


def getCourseFromFramesWithContours(frame):
    # Convert to hsv color space to better detect the red color
    hsvFrame = cv.cvtColor(frame, cv.COLOR_BGR2HSV)

    # Lower and upper RGB values for detection
    lower = np.array([0, 150, 150])
    upper = np.array([10, 255, 255])

    red_mask = cv.inRange(hsvFrame, lower, upper)

    # Remove everything other than the mask
    courseFrame = cv.bitwise_and(frame, frame, mask=red_mask)

    # Apply grayframe and blurs for noise reduction
    grayFrame = cv.cvtColor(courseFrame, cv.COLOR_BGR2GRAY)
    blurFrame = cv.GaussianBlur(grayFrame, (9, 9), 0)

    contours, _ = cv.findContours(blurFrame, cv.RETR_LIST, cv.CHAIN_APPROX_SIMPLE)

    # Check if inner rectangle is detected
    inner_rectangle = False

    for contour in contours:

        # Check if inner rectangle is detected
        if inner_rectangle is True:
            continue

        # Approximate contour to a polygon
        approx = cv.approxPolyDP(contour, 0.01 * cv.arcLength(contour, True), True)

        # Check if polygon has four vertices (i.e., is a rectangle)
        if len(approx) == 4:
            # The inner rectangle is the first rectangle detected with 4 vertices
            inner_rectangle = True
            # Draw rectangle around the polygon
            cv.drawContours(frame, [approx], 0, (0, 255, 0), 2)


# https://docs.opencv.org/4.x/da/d53/tutorial_py_houghcircles.html
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

import numpy as np
import cv2 as cv

# https://docs.opencv.org/4.x/da/d53/tutorial_py_houghcircles.html

video = cv.VideoCapture(0)

while True:
    # grab the current frame
    ret, frame = video.read()
    if not ret:
        break

    blurred = cv.GaussianBlur(frame, (11, 11), 0)
    grayFrame = cv.cvtColor(blurred, cv.COLOR_BGR2GRAY)

    # Play around with the arguments to get it to work
    circles = cv.HoughCircles(grayFrame, cv.HOUGH_GRADIENT, 1, 20, param1=100, param2=30, minRadius=20, maxRadius=50)

    if circles is not None:
        circles = np.uint16(np.around(circles))
        for i in circles[0, :]:
            # draw the outer circle
            cv.circle(frame, (i[0], i[1]), i[2], (0, 255, 0), 2)
            # draw the center of the circle
            cv.circle(frame, (i[0], i[1]), 2, (0, 0, 255), 3)


    cv.imshow("frame", frame)
    if cv.waitKey(1) & 0xFF == ord('q'):
        break

video.release()
cv.destroyAllWindows()

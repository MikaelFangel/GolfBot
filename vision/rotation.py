import cv2
import numpy as np
import cv2 as cv

# https://stackoverflow.com/questions/59363937/opencv-detecting-an-object-and-its-rotation

video = cv.VideoCapture(0)

while True:
    ret, frame = video.read()
    if not ret:
        break

    # Get frame size
    frameWidth = video.get(cv.CAP_PROP_FRAME_WIDTH)
    frameHeight = video.get(cv.CAP_PROP_FRAME_HEIGHT)

    hsvFrame = cv.cvtColor(frame, cv.COLOR_BGR2HSV)

    # Optional
    hsv_blur = cv.GaussianBlur(hsvFrame, (11, 11), 0)

    # Create mask
    green_upper = np.array([255, 255, 255])
    green_lower = np.array([50, 90, 90])
    green_mask = cv.inRange(hsv_blur, green_lower, green_upper)

    # Inverting
    mask = (255-green_mask)

    # Get threshold
    # ret, thresh = cv2.threshold(green_mask, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)

    # Get contours
    contours, heirarchy = cv.findContours(mask, cv.RETR_TREE, cv.CHAIN_APPROX_SIMPLE)

    # Find contours with an area within a threshold
    ls = []
    for cnt in contours:
        area = cv.contourArea(cnt)
        if 1000 < area < 30000:
            ls.append([area, cnt])

    # Sort to the biggest item first
    ls.sort(reverse=True)

    centerCoords = []
    directionCoords = []

    # Get centers for 2 biggest contours
    if len(ls) >= 2:
        for i in range(2):  # Change to 2
            contour = ls[i][1]
            xRect, yRect, wRect, hRect = cv.boundingRect(contour)  # get bounding rectangle

            # Visual only
            markerRect = cv.rectangle(frame, (xRect, yRect), (xRect + wRect, yRect + hRect), (255, 0, 0), 2)
            cv.rectangle(frame, (xRect+int(wRect/2)-2, yRect+int(hRect/2)-2), (xRect+int(wRect/2)+2, yRect+int(hRect/2)+2), (255, 0, 0), 2)

            if i == 0:
                centerCoords = [xRect + int(wRect/2), yRect + int(hRect/2)]
            if i == 1:
                directionCoords = [xRect + int(wRect / 2), yRect + int(hRect / 2)]

    # Show frame
    cv.imshow("frame", frame)
    cv.imshow("mask", mask)
    # cv.imshow("hsvframe", hsvFrame)

    if cv.waitKey(1) & 0xFF == ord('q'):
        break

video.release()
cv.destroyAllWindows()

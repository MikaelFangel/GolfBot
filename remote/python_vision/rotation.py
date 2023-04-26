import cv2
import numpy as np
import cv2 as cv

# https://stackoverflow.com/questions/59363937/opencv-detecting-an-object-and-its-rotation

video = cv.VideoCapture(0)

def getCenterAndDirectionCoords(frame):
    hsvFrame = cv.cvtColor(frame, cv.COLOR_BGR2HSV)

    # Optional
    hsv_blur = cv.GaussianBlur(hsvFrame, (7, 7), 0)

    # Create mask
    blue_upper = np.array([255, 255, 255])
    blue_lower = np.array([100, 100, 50])
    mask = cv.inRange(hsv_blur, blue_lower, blue_upper)

    # Inverting
    # mask = (255 - blue_mask)

    # Get contours
    contours, heirarchy = cv.findContours(mask, cv.RETR_TREE, cv.CHAIN_APPROX_SIMPLE)

    # Find contours with an area within a threshold
    ls = []
    for cnt in contours:
        area = cv.contourArea(cnt)
        if 50 < area < 350:
            ls.append([area, cnt])

    centerCoords = []
    directionCoords = []

    # Get centers for 2 biggest contours
    if len(ls) >= 2:
        # Sort to the biggest item first
        ls.sort(key=lambda x: x[0], reverse=True)

        for i in range(2):  # Change to 2
            contour = ls[i][1]
            print(cv.contourArea(contour))

            xRect, yRect, wRect, hRect = cv.boundingRect(contour)  # get bounding rectangle

            # Visual only
            cv.rectangle(frame, (xRect, yRect), (xRect + wRect, yRect + hRect), (255, 0, 0), 2)
            cv.rectangle(frame, (xRect + int(wRect / 2) - 2, yRect + int(hRect / 2) - 2),
                         (xRect + int(wRect / 2) + 2, yRect + int(hRect / 2) + 2), (255, 0, 0), 2)

            if i == 0:
                centerCoords = [xRect + int(wRect / 2), yRect + int(hRect / 2)]
            if i == 1:
                directionCoords = [xRect + int(wRect / 2), yRect + int(hRect / 2)]

    # cv.imshow("mask", mask)
    return centerCoords, directionCoords

while True:
    ret, frame = video.read()
    if not ret:
        break

    centerCoords, dirCoords = getCenterAndDirectionCoords(frame)

    # Show frame
    cv.imshow("frame", frame)

    # cv.imshow("hsvframe", hsvFrame)

    if cv.waitKey(1) & 0xFF == ord('q'):
        break

video.release()
cv.destroyAllWindows()

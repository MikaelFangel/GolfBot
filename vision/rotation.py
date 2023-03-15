import cv2
import numpy as np
import cv2 as cv

# https://stackoverflow.com/questions/59363937/opencv-detecting-an-object-and-its-rotation

video = cv.VideoCapture(0)

while True:
    ret, frame = video.read()
    if not ret:
        break

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

    for cnt in contours:
        area = cv.contourArea(cnt)

        if 3000 < area < 30000:  # Contour area size
            x, y, w, h = cv2.boundingRect(cnt)  # get bounding rectangle around biggest contour to crop to
            markerRect = cv2.rectangle(frame, (x, y), (x + w, y + h), (255, 0, 0), 2)
            crop = mask[y:y + h, x:x + w]  # crop to size

    cv.imshow("frame", frame)
    cv.imshow("mask", mask)

    # cv.imshow("hsvframe", hsvFrame)
    # cv.imshow("frame", frame);
    if cv.waitKey(1) & 0xFF == ord('q'):
        break

video.release()
cv.destroyAllWindows()

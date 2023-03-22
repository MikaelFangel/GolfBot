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

    for cnt in contours:
        area = cv.contourArea(cnt)

        if 3000 < area < 30000:  # Contour area size
            xRect, yRect, wRect, hRect = cv2.boundingRect(cnt)  # get bounding rectangle around biggest contour to crop to
            markerRect = cv2.rectangle(frame, (xRect, yRect), (xRect + wRect, yRect + hRect), (255, 0, 0), 2)

            crop = mask[yRect:yRect + hRect, xRect:xRect + wRect]  # crop to size

            # Find line
            edges = cv.Canny(crop, 50, 150, apertureSize=3)
            lines = cv.HoughLines(edges, 1, np.pi/180, 50)

            img = cv.cvtColor(crop, cv2.COLOR_GRAY2BGR)  # Convert cropped black and white image to color to draw the red line

            # lines[0]'s theta is the rotation.
            if lines is not None:
                for rho, theta in lines[0]:
                    # Get line coefficients
                    a = np.cos(theta)
                    b = np.sin(theta)

                    # Calculate line position
                    x0 = a * rho
                    y0 = b * rho

                    # !!! The lines positions !!!
                    x1 = int(x0 + (-b) + xRect)
                    y1 = int(y0 + (a) + yRect)
                    x2 = int(x0 - wRect * (-b) + xRect)
                    y2 = int(y0 - hRect * (a) + yRect)

                    cv.line(frame, (x1, y1), (x2, y2), (0, 0, 255), 2)  # draw line

    cv.imshow("frame", frame)
    cv.imshow("mask", mask)
    # cv.imshow("hsvframe", hsvFrame)

    if cv.waitKey(1) & 0xFF == ord('q'):
        break

video.release()
cv.destroyAllWindows()

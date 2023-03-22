import math

import numpy as np
import cv2 as cv

video = cv.VideoCapture(0)


class CourseFrameNotFoundException(Exception):
    """Camera Exception raised for errors detecting the course frame.

        Attributes:
            data -- the intercepted data of the camera
            message -- explanation of the error
        """

    def __init__(self, data,
                 message="Camera expected to detect 4 lines from the course frame to be able to calculate a coordinate system"):
        self.data = data
        self.message = message


def getCourseFromFramesWithHoughP(frame):
    hsvFrame = cv.cvtColor(frame, cv.COLOR_BGR2HSV)

    lower = np.array([0, 170, 170])
    upper = np.array([10, 255, 255])
    red_mask = cv.inRange(hsvFrame, lower, upper)

    courseFrame = cv.bitwise_and(frame, frame, mask=red_mask)
    grayFrame = cv.cvtColor(courseFrame, cv.COLOR_BGR2GRAY)
    blurFrame = cv.GaussianBlur(grayFrame, (9, 9), 0)

    cv.imshow('blurframe', blurFrame)

    lines = cv.HoughLinesP(blurFrame, 1, np.pi / 180, 10, minLineLength=300, maxLineGap=50)

    # Merge the detected lines that are close to each other into a single line
    merged_lines = []
    for line in lines:
        x1, y1, x2, y2 = line[0]
        if len(merged_lines) == 0:
            merged_lines.append(line[0])
        else:
            last_x1, last_y1, last_x2, last_y2 = merged_lines[-1]
            if abs(x1 - last_x2) < 50 and abs(y1 - last_y2) < 50:
                merged_lines[-1] = [last_x1, last_y1, x2, y2]
            else:
                merged_lines.append(line[0])

    # Draw the detected lines on the original image
    for line in merged_lines:
        x1, y1, x2, y2 = line
        cv.line(frame, (x1, y1), (x2, y2), (0, 255, 0), 1)


def getCourseLinesFromFramesWithContours(frame):
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

    for contour in contours:

        # Approximate contour to a polygon
        approx = cv.approxPolyDP(contour, 0.01 * cv.arcLength(contour, True), True)

        # Check if polygon has four vertices (i.e., is a rectangle)
        if len(approx) == 4:
            # Draw rectangle around the polygon
            cv.drawContours(frame, [approx], 0, (0, 255, 0), 2)
            # Return the array of lines with x and y coordinates
            return approx
        else:
            raise CourseFrameNotFoundException([approx])


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


# Calculate the distance given two points
distance_in_pixels = lambda x1, y1, x2, y2: math.sqrt(math.pow((x2 - x1), 2) + math.pow((y2 - y1), 2))


# A quick accuracy test with the height of the frame
def accuracy_test():
    # The length of the course frame calculated with pixels
    pixel_length = distance_in_pixels(top_right[0], top_right[1], bottom_right[0], bottom_right[1])
    calc = pixel_length * conversion_factor
    print(calc)
    return calc


# Width and height of course frame on the inner side in cm
real_width = 167
real_heigth = 122

while True:
    # grab the current frame
    ret, frame = video.read()
    if not ret:
        break

    getCirclesFromFrames(frame)

    try:
        lines = getCourseLinesFromFramesWithContours(frame)
    except CourseFrameNotFoundException:
        print("Course frame not found. Skip this frame")
        continue

    # Origin
    offset = lines[0][0]

    # Calculate corners x and y with offset
    corners = [point - offset for point in lines]

    # Unpack a layer of nested array that is not needed. TODO: Check where this extra array comes from
    [corners] = [corners]

    # Unpack all subarrays for each point that contains x and y coordinates
    [top_left], [top_right], [bottom_right], [bottom_left] = corners

    # Find conversion factor by real-world width and calculated pixel width
    conversion_factor = real_width / distance_in_pixels(top_left[0], top_left[1], top_right[0], top_right[1])

    accuracy_test()

    cv.imshow("frame", frame)
    if cv.waitKey(1) & 0xFF == ord('q'):
        break

video.release()
cv.destroyAllWindows()

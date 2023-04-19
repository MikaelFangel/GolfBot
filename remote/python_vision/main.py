import math

import numpy as np
import cv2 as cv


class BallsNotFoundException(Exception):
    """Camera Exception raised for when not detecting any balls.

        Attributes:
            data -- the intercepted data of the camera
            message -- explanation of the error
        """

    def __init__(self, data,
                 message="Camera expected to detect at least 1 lines to be able to calculate coordinates"):
        self.data = data
        self.message = message


class CourseFrameNotFoundException(Exception):
    """Camera Exception raised for not detecting the course frame.

        Attributes:
            data -- the intercepted data of the camera
            message -- explanation of the error
        """

    def __init__(self, data,
                 message="Camera expected to detect exactly 4 lines from the course frame to be able to calculate a coordinate system"):
        self.data = data
        self.message = message


def getCourseLinesFromFramesWithContours(frame_):
    # Convert to hsv color space to better detect the red color
    hsvFrame = cv.cvtColor(frame_, cv.COLOR_BGR2HSV)

    # Lower and upper RGB values for detection
    lower = np.array([0, 150, 150])
    upper = np.array([10, 255, 255])

    red_mask = cv.inRange(hsvFrame, lower, upper)

    # Remove everything other than the mask
    courseFrame = cv.bitwise_and(frame_, frame_, mask=red_mask)

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
            cv.drawContours(frame_, [approx], 0, (0, 255, 0), 2)
            # Return the array of lines with x and y coordinates
            return approx
        elif len(approx) == 16 or len(approx) == 15: # TODO A bit hacky way to detect the cross in the middle, but kinda works. Need to revisit this to be more stable.
            cv.drawContours(frame_, [approx], 0, (0, 255, 0), 2)
        else:
            print(len(approx))


# https://docs.opencv.org/4.x/da/d53/tutorial_py_houghcircles.html
def getCirclesFromFrames(frame_):
    # Apply grayFrame
    grayFrame = cv.cvtColor(frame_, cv.COLOR_BGR2GRAY)

    # Pixel values under 175 is ignored
    _, binary_frame = cv.threshold(grayFrame, 180, 255, cv.THRESH_BINARY)

    mask = np.zeros_like(binary_frame)
    mask[:] = 255

    # Apply the mask binary mask
    masked_frame = cv.bitwise_and(binary_frame, mask)

    # Apply blur for better edge detection
    blurFrame = cv.GaussianBlur(masked_frame, (7, 7), 0)

    # These configurations works okay with the current course setup
    white_circles = cv.HoughCircles(image=blurFrame,
                                    method=cv.HOUGH_GRADIENT,
                                    dp=1,
                                    minDist=5,
                                    param1=25,  # gradient value used in the edge detection
                                    param2=15,  # lower values allow more circles to be detected (false positives)
                                    minRadius=1,  # limits the smallest circle to this size (via radius)
                                    maxRadius=7  # similarly sets the limit for the largest circles
                                    )

    if white_circles is not None:
        white_circles = np.uint(white_circles)
        for (x, y, r) in white_circles[0, :]:
            # draw the circle
            cv.circle(frame, (x, y), r, (0, 255, 0), 1)
        return white_circles


# Calculate the distance given two points. Mostly used for accuracy tests
def distance_in_pixels(x1, y1, x2, y2):
    return math.sqrt(math.pow((x2 - x1), 2) + math.pow((y2 - y1), 2))


# Test distance between 2 balls
def accuracy_test(balls_test):
    if len(balls) == 2:
        one = balls_test[0]
        two = balls_test[1]
        dist = distance_in_pixels(one[0], one[1], two[0], two[1])
        print(dist)


# Width and height of course frame on the inner side in cm
real_width = 167
real_height = 122

video = cv.VideoCapture(0)

while True:
    # grab the current frame
    ret, frame = video.read()

    # If no frame is read, return
    if not ret:
        break

    lines = getCourseLinesFromFramesWithContours(frame)
    if lines is None:
        print("No lines found. Skipping frame")
        continue

    if len(lines) != 4:
        print("Course frame not found. Skip this frame")
        continue

    # Origin
    offset = lines[0][0]

    # Calculate corners x and y with offset
    corners = [point - offset for point in lines]

    # Unpack a layer of nested array that is not needed. TODO: Check where this extra array comes from
    [corners] = [corners]

    # Unpack two points for each point that of the length of the course frame contains x and y coordinates
    [top_left], [top_right], [bottom_right], [bottom_left] = corners

    # Find conversion factor by real-world width and calculated pixel width
    conversion_factor = real_width / distance_in_pixels(top_left[0], top_left[1], top_right[0], top_right[1])

    # Calculate irl-coordinates for the corners
    irl_corners = [corner * conversion_factor for corner in corners]

    print(corners)

    circles = getCirclesFromFrames(frame)

    if circles is not None:
        # Calculate irl_coordinates for the balls
        balls = [ball * conversion_factor for ball in circles[0, :]]

        # Unpack outer array of nested array that is not needed
        [balls] = [balls]

        # Print irl-coordinates for all white balls
        print("White balls: ")
        print(balls)

    cv.imshow("frame", frame)

    if cv.waitKey(1) & 0xFF == ord('q'):
        break

video.release()
cv.destroyAllWindows()

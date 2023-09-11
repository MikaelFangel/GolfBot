# GolfBot
A project in developing a robot for collecting golf balls using Lego Mindstorms.

The robot competed in a competition where points are given for each succesfully delivered ball. There was 12 balls to collect within 8 minutes. Minus point were given when the robot collided with the course objects. 

<div>
      <a href="https://www.youtube.com/watch?v=hqltufc165o">
         <img src="https://img.youtube.com/vi/hqltufc165/0.jpg" style="width:100%;">
      </a>
</div>

Tech stack: Go language for the software to the robot using gRPC to communicate with a remote machine running Java. The remote machine calculates the coordinates for the robot to go, and runs a camera with OpenCV.

## Project Structure
A description of how the project is structured

### ev3dev
All configuration for setting up the ev3dev environment, so it reproducible.

### remote
The project for controlling the robot using a remote machine to do all the heavy calculations. This is in its own a java maven project.

### robot
The software written for the robot. The software written for the robot is a small go project and is communicated with using gRPC.

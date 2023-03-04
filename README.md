# GolfBot
A project in developing a robot for collecting golf balls using Lego Mindstorms.

## Project Structure
A description of how the project is structured

### ev3dev
All configuration for setting up the ev3dev environment, so it reproducible.

### remote
The project for controlling the robot using a remote machine to do all the heavy calculations. This is in its own a java maven project.

### robot
The software written for the robot. The software written for the robot is a small go project and is communicated with using gRPC.

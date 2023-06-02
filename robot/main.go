package main

import (
	"context"
	"fmt"
	"log"
	"math"
	"net"
	"strconv"

	"github.com/ev3go/ev3dev"
	"google.golang.org/grpc"

	pBuff "main/proto"
)

// Motor commands
const (
	run    = "run-forever"
	stop   = "stop"
	relPos = "run-to-rel-pos" // Not working..?
	absPos = "run-to-abs-pos"
	reset  = "reset"
)

type motorServer struct {
	pBuff.UnimplementedMotorsServer
}

type motorRequest struct {
	request *pBuff.MotorRequest
	motor   *ev3dev.TachoMotor
}

// Radius values are in centimeters and the wheelBaseRadius is measured from the inner sides of the wheels.
const wheelRadius = 6.88 / 2
const wheelBaseRadius = 13.25 / 2 // For diameter/2

const wheelCircumference = 2 * wheelRadius * math.Pi
const wheelBaseCircumference = 2 * wheelBaseRadius * math.Pi

/*
 * main Setup of the server to listen for requests from clients.
 */
func main() {
	lis, err := net.Listen("tcp", ":50051")
	if err != nil {
		log.Printf("%v", err)
	}

	server := grpc.NewServer()
	pBuff.RegisterMotorsServer(server, &motorServer{})
	err = server.Serve(lis)
	if err != nil {
		log.Printf("%v", err)
	}
}

// getMotorHandle Returns the TachoMotor equivalent to the port given (e.g. port "A").
func getMotorHandle(port string, motor string) (*ev3dev.TachoMotor, error) {
	return ev3dev.TachoMotorFor("ev3-ports:out"+port, "lego-ev3-"+motor+"-motor")
}

// isRunning Returns true if the speed of the given motor is not zero, otherwise false.
func isRunning(motor *ev3dev.TachoMotor) bool {
	speed, _ := motor.Speed()

	return speed != 0
}

// RunMotors Starts the motors given, and sets them to the speed specified on client side.
func (s *motorServer) RunMotors(_ context.Context, in *pBuff.MultipleMotors) (*pBuff.StatusReply, error) {
	// motorRequests stores the request and the motor, so we don't need to get them again in the 2nd loop
	var motorRequests [2]motorRequest

	// Gets the motors and sets their speeds, which is specified on client side.
	for i, request := range in.GetMotor() {
		motor, err := getMotorHandle(request.GetMotorPort().String(), request.GetMotorType().String())
		if err != nil {
			return &pBuff.StatusReply{ReplyMessage: false}, err
		}
		motor.SetSpeedSetpoint(int(request.GetMotorSpeed()))
		motorRequests[i] = motorRequest{request: request, motor: motor}
	}

	// Start each motor
	for i, motorRequest := range motorRequests {
		motorRequest.motor.Command(run)

		// Error handling
		_, b, _ := ev3dev.Wait(motorRequests[i].motor, ev3dev.Running, ev3dev.Running, ev3dev.Stalled, false, -1)
		if !b {
			return &pBuff.StatusReply{ReplyMessage: false}, fmt.Errorf("motor %s is not running", motorRequest.request.MotorType)
		}
	}

	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

// StopMotors Stops the motors given, e.g. cleaning their MotorState.
func (s *motorServer) StopMotors(_ context.Context, in *pBuff.MultipleMotors) (*pBuff.StatusReply, error) {
	// motorRequests stores the request and the motor, so we don't need to get them again in the 2nd loop
	var motorRequests [2]motorRequest

	// Gets the motors
	for i, request := range in.GetMotor() {
		motor, err := getMotorHandle(request.GetMotorPort().String(), request.GetMotorType().String())

		if err != nil {
			return &pBuff.StatusReply{ReplyMessage: false}, err
		}
		motorRequests[i] = motorRequest{request: request, motor: motor}
	}

	// Stop each motor
	for i := 0; i < len(in.Motor); i++ {
		motorRequests[i].motor.Command(stop)
	}

	// Can't use ev3dev.Wait() to make sure motors stop. This is the alternative used...
	for i := 0; i < len(in.Motor); i++ {
		for {
			if !isRunning(motorRequests[i].motor) {
				break
			}
			motorRequests[i].motor.Command(stop)
		}
	}
	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

// Rotate Rotates the robot with a static speed, x degrees, which is specified on client side.
func (s *motorServer) Rotate(_ context.Context, in *pBuff.RotateRequest) (*pBuff.StatusReply, error) {
	// motorRequests stores the request and the motor, so we don't need to get them again in the 2nd loop
	var motorRequests [2]motorRequest

	wheelRotations := convertRobotRotationToWheelRotations(in.Degrees)
	fmt.Printf("Rotation in degrees: %d\n", wheelRotations)

	// Gets the motors and sets their speeds to a static speed, making the wheels turn in different directions
	for i := 0; i < 2; i++ {
		request := in.GetMotors()[i]

		motor, err := getMotorHandle(request.GetMotorPort().String(), request.GetMotorType().String())
		if err != nil {
			return &pBuff.StatusReply{ReplyMessage: false}, err
		}

		motor.Command(reset) // Reset motors
		if i%2 == 0 {
			motor.SetPositionSetpoint(wheelRotations)
			motor.SetSpeedSetpoint(int(in.Speed))
		} else {
			motor.SetPositionSetpoint(-wheelRotations)
			motor.SetSpeedSetpoint(int(-in.Speed))
		}
		motorRequests[i] = motorRequest{request: request, motor: motor}
	}
	gyro, _ := getSensor(pBuff.InPort_in1.String(), pBuff.Sensor_gyro.String())
	gyroAngle1, _ := gyro.Value(0)
	fmt.Printf("Before: %s\n", gyroAngle1)

	// Make the robot rotate the wheelRotations calculated above
	for _, motorRequest := range motorRequests {
		motorRequest.motor.Command(absPos)
	}

	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

func (s *motorServer) Drive(_ context.Context, in *pBuff.DriveRequest) (*pBuff.StatusReply, error) {
	var motorRequests [2]motorRequest

	numberOfRotations := -convertDistanceToWheelRotation(float64(in.Distance))
	fmt.Printf("Drive degrees of rotation: %d\n", numberOfRotations)

	// Fetch each motor
	for i := 0; i < 2; i++ {
		request := in.GetMotors()[i]

		motor, err := getMotorHandle(request.GetMotorPort().String(), request.GetMotorType().String())
		if err != nil {
			return &pBuff.StatusReply{ReplyMessage: false}, err
		}

		// Change speed value if distance is negative
		dir := 1
		if in.Speed < 0 {
			dir = -1
		}

		// Set values for request
		motor.Command(reset)
		motor.SetSpeedSetpoint(dir * int(in.Speed))
		motor.SetPositionSetpoint(numberOfRotations)
		motorRequests[i] = motorRequest{request: request, motor: motor}
	}

	// Give requests to motors
	for _, motorRequest := range motorRequests {
		motorRequest.motor.Command(absPos)
	}

	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

// convertRobotRotationToWheelRotations Converts an input of degrees to how many degrees a wheel should rotate.
func convertRobotRotationToWheelRotations(degrees int32) int {
	rotationInCm := (wheelBaseCircumference * float64(degrees)) / 360.
	return int(rotationInCm / (wheelCircumference / 360))
}

func convertDistanceToWheelRotation(distance float64) int {
	return int((distance / wheelCircumference) * 360)
}

// getSensor Returns the requested sensor from the input ports of the robot
func getSensor(inPort string, sensor string) (*ev3dev.Sensor, error) {
	return ev3dev.SensorFor("ev3-ports:"+inPort, "lego-ev3-"+sensor)
}

// GetDistanceInCm Returns the distance to the closest object from the ultrasonic sensor
func GetDistanceInCm() float64 {
	ultraSonicSensor, err := getSensor(pBuff.InPort_in1.String(), pBuff.Sensor_us.String())
	if err != nil {
		return -1
	}

	distanceString, _ := ultraSonicSensor.Value(0)
	distance, _ := strconv.ParseFloat(distanceString, 64)
	return distance / 10
}

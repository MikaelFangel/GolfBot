package main

import (
	"context"
	"errors"
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
	run  = "run-forever"
	stop = "stop"
	// relPos = "run-to-rel-pos" // Not working..?
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

// getSensor Returns the requested sensor from the input ports of the robot
func getSensor(inPort string, sensor string) (*ev3dev.Sensor, error) {
	return ev3dev.SensorFor("ev3-ports:"+inPort, "lego-ev3-"+sensor)
}

// isRunning Returns true if the speed of the given motor is not zero, otherwise false.
func isRunning(motor *ev3dev.TachoMotor) bool {
	speed, _ := motor.Speed()

	return speed != 0
}

// convertRobotRotationToWheelRotations Converts an input of degrees to how many degrees a wheel should rotate.
func convertRobotRotationToWheelRotations(degrees int32) int {
	rotationInCm := (wheelBaseCircumference * float64(degrees)) / 360.
	return int(rotationInCm / (wheelCircumference / 360))
}

func convertDistanceToWheelRotation(distance float64) int {
	return int((distance / wheelCircumference) * 360)
}

// StopMotors Stops the motors given, e.g. cleaning their MotorState and setting speed to zero
func (s *motorServer) StopMotors(_ context.Context, in *pBuff.MultipleMotors) (*pBuff.StatusReply, error) {
	// motorRequests stores the request and the motor, so we don't need to get them again in the 2nd loop
	var motorRequests []motorRequest

	// Gets the motors
	for _, request := range in.GetMotor() {
		motor, err := getMotorHandle(request.GetMotorPort().String(), request.GetMotorType().String())

		if err != nil {
			return &pBuff.StatusReply{ReplyMessage: false}, err
		}
		motor.SetSpeedSetpoint(0)
		motorRequests = append(motorRequests, motorRequest{request: request, motor: motor})
	}

	// Stop each motor
	for _, motorRequest := range motorRequests {
		motorRequest.motor.Command(stop)
	}

	// Can't use ev3dev.Wait() to make sure motors stop. This is the alternative used...
	for _, motorRequest := range motorRequests {
		for {
			if !isRunning(motorRequest.motor) {
				break
			}
			motorRequest.motor.Command(stop)
		}
	}
	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

// Rotate Rotates the robot with a static speed, x degrees, which is specified on client side.
func (s *motorServer) Rotate(_ context.Context, in *pBuff.RotateRequest) (*pBuff.StatusReply, error) {
	// motorRequests stores the request and the motor, so we don't need to get them again in the 2nd loop
	var motorRequests []motorRequest

	wheelRotations := convertRobotRotationToWheelRotations(in.Degrees)
	fmt.Printf("Rotation in degrees: %d\n", wheelRotations)

	// Gets the motors and sets their speeds to a static speed, making the wheels turn in different directions
	for i, request := range in.GetMotors().GetMotor() {
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
		motorRequests = append(motorRequests, motorRequest{request: request, motor: motor})
	}

	// Make the robot rotate the wheelRotations calculated above
	for _, motorRequest := range motorRequests {
		motorRequest.motor.Command(absPos)
	}

	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

// RotateWGyro Rotates the robot given a speed using a gyro. This function has the side effect that it recalibrates the gyro.
func (s *motorServer) RotateWGyro(_ context.Context, in *pBuff.RotateRequest) (*pBuff.StatusReply, error) {
	gyro, err := recalibrateGyro()
	if err != nil {
		return nil, err
	}

	// Change the rotation direction
	var rotateSpeed = int(in.Speed)
	if in.Degrees > 0 {
		rotateSpeed = int(in.Speed * -1)
	}

	// Prepare the motors for running
	var motorRequests []motorRequest
	for _, request := range in.GetMotors().GetMotor() {
		motor, err := getMotorHandle(request.GetMotorPort().String(), request.GetMotorType().String())
		if err != nil {
			return nil, err
		}

		switch request.GetMotorPort() {
		case pBuff.OutPort_A:
			motor.SetSpeedSetpoint(rotateSpeed)
		case pBuff.OutPort_D:
			motor.SetSpeedSetpoint(-rotateSpeed)
		default:
			return &pBuff.StatusReply{ReplyMessage: false}, errors.New("Not a valid motor")
		}

		motorRequests = append(motorRequests, motorRequest{request: request, motor: motor})
	}

	// Start the motors
	for _, motorRequest := range motorRequests {
		motorRequest.motor.Command(run)
	}

	// Busy wait until the robot has completed the rotation or have superseded the given degrees
	var gyroValStr, _ = gyro.Value(0)
	var gyroVal, _ = strconv.Atoi(gyroValStr)
	for math.Abs(float64(gyroVal)) <= math.Abs(float64(in.Degrees)) {
		gyroValStr, _ = gyro.Value(0)
		gyroVal, _ = strconv.Atoi(gyroValStr)
	}

	// Stop the motors
	for _, motorRequest := range motorRequests {
		motorRequest.motor.Command(stop)
	}

	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

func recalibrateGyro() (*ev3dev.Sensor, error) {
	gyro, err := getSensor(pBuff.InPort_in1.String(), pBuff.Sensor_gyro.String())
	if err != nil {
		return nil, err
	}

	// Trigger the recalibration using a mode switch
	gyro.SetMode("GYRO-CAL")
	gyro.SetMode("GYRO-ANG")

	return gyro, nil
}

// Drive Makes the robot drive straight forward or backward with a speed and distance specified client side
func (s *motorServer) Drive(_ context.Context, in *pBuff.DriveRequest) (*pBuff.StatusReply, error) {
	var motorRequests []motorRequest

	numberOfRotations := -convertDistanceToWheelRotation(float64(in.Distance))
	fmt.Printf("Drive degrees of rotation: %d\n", numberOfRotations)

	// Fetch each motor
	for _, request := range in.GetMotors().GetMotor() {
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
		motorRequests = append(motorRequests, motorRequest{request: request, motor: motor})
	}

	// Give requests to motors
	for _, motorRequest := range motorRequests {
		motorRequest.motor.Command(absPos)
	}

	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

// CollectRelease Either collects or releases balls. Whether it is collecting or releasing is handled client side.
//
//	If speed > 0 then the balls are released
//	If speed < 0 then the balls are collected
func (s *motorServer) CollectRelease(_ context.Context, in *pBuff.MultipleMotors) (*pBuff.StatusReply, error) {
	// motorRequests stores the request and the motor, so we don't need to get them again in the 2nd loop
	var motorRequests []motorRequest

	// Gets the motors and sets their speeds, which is specified on client side.
	for _, request := range in.GetMotor() {
		var motorOutPort = request.GetMotorPort()
		motor, err := getMotorHandle(motorOutPort.String(), request.GetMotorType().String())
		if err != nil {
			return &pBuff.StatusReply{ReplyMessage: false}, err
		}
		switch motorOutPort {
		case pBuff.OutPort_B:
			motor.SetSpeedSetpoint(int(request.GetMotorSpeed()))
		case pBuff.OutPort_C:
			motor.SetSpeedSetpoint(int(-request.GetMotorSpeed()))
		default:
			return &pBuff.StatusReply{ReplyMessage: false}, errors.New("warning! Motor with wrong output port detected. Expected output ports are port B and C")
		}
		if motorOutPort == pBuff.OutPort_B {
			motor.SetSpeedSetpoint(int(request.GetMotorSpeed()))
		} else if motorOutPort == pBuff.OutPort_C {
			motor.SetSpeedSetpoint(int(-request.GetMotorSpeed()))
		}
		motorRequests = append(motorRequests, motorRequest{request: request, motor: motor})
	}

	// Start each motor
	for _, motorRequest := range motorRequests {
		motorRequest.motor.Command(run)

		// Error handling
		_, b, _ := ev3dev.Wait(motorRequest.motor, ev3dev.Running, ev3dev.Running, ev3dev.Stalled, false, -1)
		if !b {
			return &pBuff.StatusReply{ReplyMessage: false}, fmt.Errorf("motor %s is not running", motorRequest.request.MotorType)
		}
	}

	return &pBuff.StatusReply{ReplyMessage: true}, nil
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

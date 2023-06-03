package main

import (
	"context"
	"errors"
	"fmt"
	"github.com/ev3go/ev3dev"
	"google.golang.org/grpc"
	"log"
	pBuff "main/proto"
	"main/util"
	"math"
	"net"
	"strconv"
	"time"
)

// Motor commands
const (
	run    = "run-forever"
	stop   = "stop"
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

// main Setup of the server to listen for requests from clients.
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

// Drive Makes the robot drive straight forward or backward with a speed and distance specified client side
func (s *motorServer) Drive(_ context.Context, in *pBuff.DriveRequest) (*pBuff.StatusReply, error) {
	var motorRequests []motorRequest

	numberOfRotations := -util.ConvertDistanceToWheelRotation(float64(in.Distance))
	fmt.Printf("Drive degrees of rotation: %d\n", numberOfRotations)

	// Fetch each motor
	for _, request := range in.GetMotors().GetMotor() {
		motor, err := util.GetMotorHandle(request.GetMotorPort().String(), request.GetMotorType().String())
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

	return stopMotorsIfNotAllAreRunning(motorRequests)
}

// DriveWGyro Rotates the robot given a speed using a gyro. This function has the side effect that it recalibrates the gyro.
func (s *motorServer) DriveWGyro(_ context.Context, in *pBuff.DriveRequest) (*pBuff.StatusReply, error) {
	gyro, err := util.RecalibrateGyro()
	if err != nil {
		return nil, err
	}

	// PID constant, how much we want to correct errors of each term
	var kp = 2.0
	var ki = 1.0
	var kd = 1.0

	// the running sum of errors
	var integral = 0.0

	// Used to calculate error derivative
	var lastError = 0.0

	// Prepare the motors for running
	var motorRequests []motorRequest

	// TODO: For testing purposes run until i < 20, but should be until length to target <= 0. Research RPC client streaming
	for i := 0; i < 20; i++ {

		// Read gyro values, eg. the current error
		var gyroValStr, _ = gyro.Value(0)
		var gyroVal, _ = strconv.Atoi(gyroValStr)
		var gyroErr = float64(gyroVal)
		var target = gyroErr

		integral += gyroErr

		var derivative = gyroErr - lastError

		lastError = gyroErr

		// "P term", how much we want to change the motors' power in proportion with the error
		// "I term", the running sum of errors to correct for
		var turn = (kp * target) + (ki * integral) + (kd * derivative)

		for _, request := range in.GetMotors().GetMotor() {
			motor, err := util.GetMotorHandle(request.GetMotorPort().String(), request.GetMotorType().String())
			if err != nil {
				return &pBuff.StatusReply{ReplyMessage: false}, err
			}

			// Change speed value if distance is negative
			dir := 1
			if in.Speed < 0 {
				dir = -1
			}

			var power = int(in.Speed)

			// TODO: Should ramp be set for all iterations or only first and last? I found that the these values worked well
			motor.SetRampUpSetpoint(3 * time.Second)
			motor.SetRampDownSetpoint(3 * time.Second)
			switch request.GetMotorPort() {
			case pBuff.OutPort_A:
				motor.SetSpeedSetpoint(-(dir * power) + int(turn))
			case pBuff.OutPort_D:
				motor.SetSpeedSetpoint(-(dir * power) - int(turn))
			default:
				return &pBuff.StatusReply{ReplyMessage: false}, errors.New("not a valid motor")
			}

			motorRequests = append(motorRequests, motorRequest{request: request, motor: motor})
		}

		// TODO: Maybe we could switch around the motor order for each iteration, so that the start/stop delay is evened out
		// Start the motors
		for _, motorRequest := range motorRequests {
			motorRequest.motor.Command(run)
		}

		// Handle error if both motors does not run
		status, err := stopMotorsIfNotAllAreRunning(motorRequests)
		if err != nil {
			return status, err
		}

		// Run the motors with these settings for 0.4 second then adjust
		time.Sleep(400 * time.Millisecond)
	}

	stopAllMotors(motorRequests)

	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

// StopMotors Stops the motors given, e.g. cleaning their MotorState and setting speed to zero
func (s *motorServer) StopMotors(_ context.Context, in *pBuff.MultipleMotors) (*pBuff.StatusReply, error) {
	// motorRequests stores the request and the motor, so we don't need to get them again in the 2nd loop
	var motorRequests []motorRequest

	// Gets the motors
	for _, request := range in.GetMotor() {
		motor, err := util.GetMotorHandle(request.GetMotorPort().String(), request.GetMotorType().String())

		if err != nil {
			return &pBuff.StatusReply{ReplyMessage: false}, err
		}
		motor.SetSpeedSetpoint(0)
		motorRequests = append(motorRequests, motorRequest{request: request, motor: motor})
	}

	stopAllMotors(motorRequests)

	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

// RotateWGyro Rotates the robot given a speed using a gyro. This function has the side effect that it recalibrates the gyro.
func (s *motorServer) RotateWGyro(_ context.Context, in *pBuff.RotateRequest) (*pBuff.StatusReply, error) {
	gyro, err := util.RecalibrateGyro()
	if err != nil {
		return nil, err
	}

	// Change the rotation direction
	var rotateSpeed = int(in.Speed)
	if in.Degrees > 0 {
		rotateSpeed *= -1
	}

	// PD constant, how much we want to correct errors of each term
	var kp = 0.5
	var kd = 0.25

	// Read gyro values
	var gyroValStr, _ = gyro.Value(0)
	var gyroValInt, _ = strconv.Atoi(gyroValStr)
	var gyroValF = float64(gyroValInt)

	// Target is the degrees left to rotate
	var target = gyroValF - float64(in.Degrees)

	// Used to calculate error derivative
	var lastError = 0.0

	// Prepare the motors for running
	var motorRequests []motorRequest

	// Busy wait until the robot has completed the rotation or have superseded the given degrees
	for math.Abs(gyroValF) <= math.Abs(float64(in.Degrees)) {
		gyroValStr, _ = gyro.Value(0)
		gyroValInt, _ = strconv.Atoi(gyroValStr)
		gyroValF = float64(gyroValInt)
		target = gyroValF - float64(in.Degrees)

		// Error derivative try to predict the next error, from the previous error
		var derivative = target - lastError

		lastError = target

		// "P term", how much we want to change the motors' power in proportion with the error
		// "D term", correcting for the next error
		var turn = (kp * target) + (kd * derivative)

		for _, request := range in.GetMotors().GetMotor() {
			motor, err := util.GetMotorHandle(request.GetMotorPort().String(), request.GetMotorType().String())
			if err != nil {
				return nil, err
			}

			var power = rotateSpeed + int(turn)

			switch request.GetMotorPort() {
			case pBuff.OutPort_A:
				motor.SetSpeedSetpoint(power)
			case pBuff.OutPort_D:
				motor.SetSpeedSetpoint(-power)
			default:
				return &pBuff.StatusReply{ReplyMessage: false}, errors.New("not a valid motor")
			}

			motorRequests = append(motorRequests, motorRequest{request: request, motor: motor})
		}

		// Start the motors
		for _, motorRequest := range motorRequests {
			motorRequest.motor.Command(run)
		}

		status, err := stopMotorsIfNotAllAreRunning(motorRequests)
		if err != nil {
			return status, err
		}
	}

	stopAllMotors(motorRequests)

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
		motor, err := util.GetMotorHandle(motorOutPort.String(), request.GetMotorType().String())
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

		motorRequests = append(motorRequests, motorRequest{request: request, motor: motor})
	}

	// Start each motor
	for _, motorRequest := range motorRequests {
		motorRequest.motor.Command(run)
	}

	return stopMotorsIfNotAllAreRunning(motorRequests)
}

func stopMotorsIfNotAllAreRunning(motorRequests []motorRequest) (*pBuff.StatusReply, error) {
	for _, motorRequest := range motorRequests {
		// Block until running
		_, b, _ := ev3dev.Wait(motorRequest.motor, ev3dev.Running, ev3dev.Running, ev3dev.Stalled, false, -1)
		if !b {
			stopAllMotors(motorRequests)

			return &pBuff.StatusReply{ReplyMessage: false}, fmt.Errorf("motor %s is not running", motorRequest.request.MotorType)
		}
	}

	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

func stopAllMotors(motorRequests []motorRequest) {
	// Can't use ev3dev.Wait() to make sure motors stop. This is the alternative used...
	for _, motorRequest := range motorRequests {
		motorRequest.motor.Command(stop)

		for {
			if !util.IsRunning(motorRequest.motor) {
				break
			}
		}
	}
}

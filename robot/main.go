package main

import (
	"context"
	"errors"
	"fmt"
	"log"
	"math"
	"net"
	"strconv"
	"time"

	"github.com/ev3go/ev3dev"
	"google.golang.org/grpc"

	pBuff "main/proto"
	"main/util"
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

// DriveWGyro Rotates the robot given a speed using the gyro. This function has the side effect that it recalibrates the gyro.
func (s *motorServer) DriveWGyro(in pBuff.Motors_DriveWGyroServer) error {
	// PID constant, how much we want to correct errors of each term
	kp := 0.5
	ki := 0.25
	kd := 0.1

	// the running sum of errors
	integral := 0.0

	// Used to calculate error derivative
	lastError := 0.0

	// Prepare the motors for running
	var motorRequests []motorRequest

	// Begin stream from client
	driveRequest, _ := in.Recv()

	distance := int(driveRequest.Distance)

	// Change speed value if distance is negative
	speed := -int(driveRequest.Speed)
	if speed < 0 {
		speed *= -1
	}

	gyro, err := util.GetSensor(pBuff.InPort_in1.String(), pBuff.Sensor_gyro.String())
	if err != nil {
		return err
	}

	// TODO: For testing purposes run until i < 20, but should be until length to target <= 0. Research RPC client streaming
	for distance > 0 {
		println("Distance ", distance)
		var message, _ = in.Recv()
		distance = int(message.Distance)

		// Read gyro values, eg. the current error
		gyroValStr, _ := gyro.Value(0)
		gyroErr, _ := strconv.ParseFloat(gyroValStr, 64)
		integral += gyroErr

		// Error derivative try to predict the next error from the previous error
		derivative := gyroErr - lastError
		lastError = gyroErr

		// "P term", how much we want to change the motors' power in proportion with the error
		// "I term", the running sum of errors to correct for
		// "D term", trying to predict next error
		turn := (kp * gyroErr) + (ki * integral) + (kd * derivative)

		for _, request := range driveRequest.GetMotors().GetMotor() {
			motor, err := util.GetMotorHandle(request.GetMotorPort().String(), request.GetMotorType().String())
			if err != nil {
				return err
			}

			// TODO: Should ramp be set for all iterations or only first and last? I found that the these values worked well
			motor.SetRampUpSetpoint(3 * time.Second)
			motor.SetRampDownSetpoint(3 * time.Second)
			switch request.GetMotorPort() {
			case pBuff.OutPort_A:
				motor.SetSpeedSetpoint(speed + int(turn))
			case pBuff.OutPort_D:
				motor.SetSpeedSetpoint(speed - int(turn))
			default:
				return err
			}

			motorRequests = append(motorRequests, motorRequest{request: request, motor: motor})
		}

		// TODO: Maybe we could switch around the motor order for each iteration, so that the start/stop delay is evened out
		// Start the motors
		for _, motorRequest := range motorRequests {
			motorRequest.motor.Command(run)
		}

		// Handle error if both motors does not run
		_, err := stopMotorsIfNotAllAreRunning(motorRequests)
		if err != nil {
			return err
		}
	}

	stopAllMotors(motorRequests)

	// Stop streaming connection
	err = in.SendAndClose(&pBuff.StatusReply{ReplyMessage: true})
	if err != nil {
		return err
	}

	return err
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
	// Change the rotation direction
	var rotateSpeed = int(in.Speed)
	if in.Degrees > 0 {
		rotateSpeed *= -1
	}

	// Prepare the motors for running
	var motorRequests []motorRequest
	for _, request := range in.GetMotors().GetMotor() {
		motor, err := util.GetMotorHandle(request.GetMotorPort().String(), request.GetMotorType().String())
		if err != nil {
			return nil, err
		}

		switch request.GetMotorPort() {
		case pBuff.OutPort_A:
			motor.SetSpeedSetpoint(rotateSpeed)
		case pBuff.OutPort_D:
			motor.SetSpeedSetpoint(-rotateSpeed)
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

	// Busy wait until the robot has completed the rotation or have superseded the given degrees
	gyro, err := util.GetSensor(pBuff.InPort_in1.String(), pBuff.Sensor_gyro.String())
	if err != nil {
		return nil, err
	}
	gyroValStr, err := gyro.Value(0)
	var gyroVal, _ = strconv.ParseFloat(gyroValStr, 64)
	for math.Abs(gyroVal) <= math.Abs(float64(in.Degrees)) {
		gyroValStr, err = gyro.Value(0)
		gyroVal, _ = strconv.ParseFloat(gyroValStr, 64)
	}

	// Return if there was a gyro reading error
	if err != nil {
		return &pBuff.StatusReply{ReplyMessage: false}, err
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

func (s *motorServer) RecalibrateGyro(_ context.Context, _ *pBuff.EmptyRequest) (*pBuff.StatusReply, error) {
	gyro, err := util.GetSensor(pBuff.InPort_in1.String(), pBuff.Sensor_gyro.String())
	if err != nil {
		return nil, err
	}
	// Sleep before calibration to settle possible vibrations
	time.Sleep(500 * time.Millisecond)

	// Trigger the recalibration using a mode switch
	gyro.SetMode("GYRO-CAL")
	gyro.SetMode("GYRO-ANG")

	// Sleep to ensure recalibration has finished before any other commands are run
	time.Sleep(500 * time.Millisecond)
	return &pBuff.StatusReply{ReplyMessage: true}, nil
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
	}
}

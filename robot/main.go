package main

import (
	"context"
	"fmt"
	"log"
	"math"
	"net"

	"github.com/ev3go/ev3dev"
	"google.golang.org/grpc"

	pBuff "main/proto"
)

// Motor types
const (
	mediumMotor = "lego-ev3-m-motor"
	largeMotor  = "lego-ev3-l-motor"
)

// Motor commands
const (
	run     = "run-forever"
	stop    = "stop"
	rel_pos = "run-to-rel-pos" // Not working..?
	absPos  = "run-to-abs-pos"
	reset   = "reset"
)

type motorServer struct {
	pBuff.UnimplementedMotorsServer
}

type motorRequest struct {
	request *pBuff.MotorRequest
	motor   *ev3dev.TachoMotor
}

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
func getMotorHandle(port string) (*ev3dev.TachoMotor, error) {
	fmt.Println("Hej")
	fmt.Println(pBuff.Type_large.String())
	return ev3dev.TachoMotorFor("ev3-ports:out"+port, largeMotor)
}

// isRunning Returns true if the speed of the given motor is not zero, otherwise false.
func isRunning(motor *ev3dev.TachoMotor) bool {
	speed, _ := motor.Speed()

	return speed != 0
}

// RunMotors Starts the motors given, and sets them to the speed specified on client side.
func (s *motorServer) RunMotors(ctx context.Context, in *pBuff.MultipleMotors) (*pBuff.StatusReply, error) {
	// motorRequests stores the request and the motor, so we don't need to get them again in the 2nd loop
	var motorRequests [2]motorRequest

	// Gets the motors and sets their speeds, which is specified on client side.
	for i, request := range in.GetMotor() {
		fmt.Println("Noget sjovt")
		motor, err := getMotorHandle(request.GetMotorPort().String())
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
func (s *motorServer) StopMotors(ctx context.Context, in *pBuff.MultipleMotors) (*pBuff.StatusReply, error) {
	// motorRequests stores the request and the motor, so we don't need to get them again in the 2nd loop
	var motorRequests [2]motorRequest

	// Gets the motors
	for i, request := range in.GetMotor() {
		motor, err := getMotorHandle(request.GetMotorPort().String())

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
func (s *motorServer) Rotate(ctx context.Context, in *pBuff.RotateRequest) (*pBuff.StatusReply, error) {
	// motorRequests stores the request and the motor, so we don't need to get them again in the 2nd loop
	var motorRequests [2]motorRequest

	wheelRotations := convertRobotRotationToWheelRotations(in.Degrees)
	fmt.Println(wheelRotations) // For debugging TODO: delete

	// Gets the motors and sets their speeds to a static speed, making the wheels turn in different directions
	for i, request := range in.MultipleMotors.GetMotor() {
		motor, err := getMotorHandle(request.GetMotorPort().String())
		if err != nil {
			return &pBuff.StatusReply{ReplyMessage: false}, err
		}

		motor.Command(reset) // Reset motors
		if i%2 == 0 {
			motor.SetPositionSetpoint(wheelRotations)
			motor.SetSpeedSetpoint(500)
		} else {
			motor.SetPositionSetpoint(-wheelRotations)
			motor.SetSpeedSetpoint(-500)
		}
		motorRequests[i] = motorRequest{request: request, motor: motor}
	}

	// Make the robot rotate the wheelRotations calculated above
	for _, motorRequest := range motorRequests {
		motorRequest.motor.Command(absPos)
	}

	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

// convertRobotRotationToWheelRotations Converts an input of degrees to how many degrees a wheel should rotate.
func convertRobotRotationToWheelRotations(degrees int32) int {
	// Radius values are in centimeters and the wheelBaseRadius is measured from the inner sides of the wheels.
	wheelRadius := 3.4
	wheelBaseRadius := 8.75

	wheelCircumference := 2 * wheelRadius * math.Pi
	wheelBaseCircumference := 2 * wheelBaseRadius * math.Pi

	// Calculate the distance move by rotating 1 degree on a wheel
	rotationInCm := (wheelBaseCircumference * float64(degrees)) / 360.
	return int(rotationInCm / (wheelCircumference / 360))
}

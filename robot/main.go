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
	rel_pos = "run-to-rel-pos"
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

func getMotorHandle(port string) (*ev3dev.TachoMotor, error) {
	fmt.Println("Hej")
	fmt.Println(pBuff.Type_large.String())
	return ev3dev.TachoMotorFor("ev3-ports:out"+port, largeMotor)
}

func isRunning(motor *ev3dev.TachoMotor) bool {
	speed, _ := motor.Speed()

	return speed != 0
}

func (s *motorServer) RunMotors(ctx context.Context, in *pBuff.MultipleMotors) (*pBuff.StatusReply, error) {
	var motorRequests [2]motorRequest
	for i, request := range in.GetMotor() {
		fmt.Println("Noget sjovt")
		motor, err := getMotorHandle(request.GetMotorPort().String())
		if err != nil {
			return &pBuff.StatusReply{ReplyMessage: false}, err
		}
		motorRequests[i] = motorRequest{request: request, motor: motor}
		motor.SetSpeedSetpoint(int(request.GetMotorSpeed()))
		fmt.Println(request)
		fmt.Println(motor.String())
	}

	for i, motorRequest := range motorRequests {
		motorRequest.motor.Command(run)
		_, b, _ := ev3dev.Wait(motorRequests[i].motor, ev3dev.Running, ev3dev.Running, ev3dev.Stalled, false, -1)
		if !b {
			return &pBuff.StatusReply{ReplyMessage: false}, fmt.Errorf("motor %s is not running", motorRequest.request.MotorType)
		}
	}

	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

func (s *motorServer) StopMotors(ctx context.Context, in *pBuff.MultipleMotors) (*pBuff.StatusReply, error) {
	var motorRequests [2]motorRequest
	for i, request := range in.GetMotor() {
		motor, err := getMotorHandle(request.GetMotorPort().String())
		if err != nil {
			return &pBuff.StatusReply{ReplyMessage: false}, err
		}
		motorRequests[i] = motorRequest{request: request, motor: motor}
	}

	for i := 0; i < len(in.Motor); i++ {
		motorRequests[i].motor.Command(stop)
	}

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

func (s *motorServer) Rotate(ctx context.Context, in *pBuff.RotateRequest) (*pBuff.StatusReply, error) {
	var motorRequests [2]motorRequest // for keeping track of motors
	// check whether motors are available
	// var arr [2]string
	// arr[0] = "A"
	// arr[1] = "D"
	wheelRotations := convertRobotRotationToWheelRotations(in.Degrees)
	fmt.Println(wheelRotations)

	for i, request := range in.MultipleMotors.GetMotor() {
		// motor, err := getMotorHandle(arr[i])
		motor, err := getMotorHandle(request.GetMotorPort().String())
		if err != nil {
			return &pBuff.StatusReply{ReplyMessage: false}, err
		}
		motorRequests[i] = motorRequest{request: request, motor: motor}
		//motor.SetPosition(0)
		motor.Command(reset)
		if i%2 == 0 {
			motor.SetPositionSetpoint(wheelRotations)
			motor.SetSpeedSetpoint(500)
		} else {
			motor.SetPositionSetpoint(-wheelRotations)
			motor.SetSpeedSetpoint(-500)
		}
	}

	for _, motorRequest := range motorRequests {
		motorRequest.motor.Command(absPos)
	}

	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

// convertRobotRotationToWheelRotations Converts a input of degrees to how many degrees a wheel should rotate.
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

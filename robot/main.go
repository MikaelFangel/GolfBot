package main

import (
	"context"
	"fmt"
	"log"
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
	run  = "run-forever"
	stop = "stop"
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
	return ev3dev.TachoMotorFor("ev3-ports:out"+port, largeMotor)
}

func isRunning(motor *ev3dev.TachoMotor) bool {
	speed, _ := motor.Speed()

	return speed != 0
}

func (s *motorServer) RunMotors(ctx context.Context, in *pBuff.MultipleMotors) (*pBuff.StatusReply, error) {
	var motorRequests [2]motorRequest
	for i, request := range in.GetMotor() {
		motor, err := getMotorHandle(request.GetMotorPort().String())
		if err != nil {
			return &pBuff.StatusReply{ReplyMessage: false}, err
		}
		motorRequests[i] = motorRequest{request: request, motor: motor}
		motor.SetSpeedSetpoint(int(request.GetMotorSpeed()))
	}

	for _, motorRequest := range motorRequests {
		motorRequest.motor.Command(run)
		// _, b, _ := ev3dev.Wait(motorRequests[i].motor, ev3dev.Running, ev3dev.Running, ev3dev.Stalled, false, -1)
		// if !b {
		// 	return &pBuff.StatusReply{ReplyMessage: false}, fmt.Errorf("motor %s is not running", motorRequest.request.MotorType)
		// }
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
	for i, request := range in.MultipleMotors.GetMotor() {
		motor, err := getMotorHandle(string(request.GetMotorPort()))
		if err != nil {
			return &pBuff.StatusReply{ReplyMessage: false}, err
		}
		motorRequests[i] = motorRequest{request: request, motor: motor}
		if i == 0 {
			motor.SetSpeedSetpoint(int(request.GetMotorSpeed()))
		} else {
			motor.SetSpeedSetpoint(int(-request.GetMotorSpeed()))
		}
	}

	for _, motorRequest := range motorRequests {
		motorRequest.motor.Command(run)
		isRunning := isRunning(motorRequest.motor)
		if !isRunning {
			return &pBuff.StatusReply{ReplyMessage: false}, fmt.Errorf("motor %s is not running", motorRequest.request.MotorType)
		}
	}

	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

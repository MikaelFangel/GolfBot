package main

import (
	"context"
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

func getMotorHandle(port string) *ev3dev.TachoMotor {
	out, err := ev3dev.TachoMotorFor("ev3-ports:out"+port, largeMotor)
	if err != nil {
		log.Printf("%v", err)
	}

	return out
}

func (s *motorServer) RunMotor(ctx context.Context, in *pBuff.MotorRequest) (*pBuff.StatusReply, error) {
	motor, err := getMotorHandle(in.GetMotorType())
	motor.SetSpeedSetpoint(int(in.GetMotorSpeed()))
	motor.Command(run)

	return &pBuff.StatusReply{ReplyMessage: true}, err
}

func (s *motorServer) StopMotor(ctx context.Context, in *pBuff.MotorRequest) (*pBuff.StatusReply, error) {
	motor, err := getMotorHandle(in.GetMotorType())
	motor.Command(stop)

	return &pBuff.StatusReply{ReplyMessage: true}, err
}

func (s *motorServer) DriveStraight(ctx context.Context, in *pBuff.MultipleMotors) *pBuff.StatusReply {
	var motors [3]*ev3dev.TachoMotor

	for i, request := range in.Motor {
		motor, err := getMotorHandle(request.GetMotorType())
		if err == nil {
			return &pBuff.StatusReply{ReplyMessage: false, ErrorMsg: err.Error()}
		}
		motors[i] = motor
		motor.SetSpeedSetpoint(int(request.GetMotorSpeed()))
	}

	for _, motor := range motors {
		motor.Command(run)
	}

	return &pBuff.StatusReply{ReplyMessage: true}
}

func (s *motorServer) Turn(ctx context.Context, in *pBuff.MultipleMotors) *pBuff.StatusReply {
	for _, request := range in.Motor {
		motor, err := getMotorHandle(request.GetMotorType())
		motor.SetSpeedSetpoint(int(request.GetMotorSpeed()))
		motor.Command(run)
	}

	return &pBuff.StatusReply{ReplyMessage: true, ErrorMsg: err}

}

func (s *motorServer) StopMultiple(ctx context.Context, in *pBuff.MultipleMotors) *pBuff.StatusReply {
	for _, request := range in.Motor {
		motor, err := getMotorHandle(request.GetMotorType())
		motor.SetSpeedSetpoint(int(request.GetMotorSpeed()))
		motor.Command(run)
	}

	return &pBuff.StatusReply{ReplyMessage: true, ErrorMsg: err}
}

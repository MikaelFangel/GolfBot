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
	motor := getMotorHandle(in.GetMotorType())
	motor.SetSpeedSetpoint(int(in.GetMotorSpeed()))
	motor.Command(run)

	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

func (s *motorServer) StopMotor(ctx context.Context, in *pBuff.MotorRequest) (*pBuff.StatusReply, error) {
	motor := getMotorHandle(in.GetMotorType())
	motor.Command(stop)

	return &pBuff.StatusReply{ReplyMessage: true}, nil
}

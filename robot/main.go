package main

import (
	"context"
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
	lis, _ := net.Listen("tcp", ":50051")
	server := grpc.NewServer()
	pBuff.RegisterMotorsServer(server, &motorServer{})
	server.Serve(lis)
}

func getMotorHandle(port string) *ev3dev.TachoMotor {
	out, _ := ev3dev.TachoMotorFor("ev3-ports:out"+port, largeMotor)

	return out
}

func (s *motorServer) RunMotor(ctx context.Context, in *pBuff.MotorRequest) (*pBuff.StatusReply, error) {
	motor := getMotorHandle(in.GetMotor())
	motor.SetSpeedSetpoint(motor.MaxSpeed())
	motor.Command(run)

	return &pBuff.StatusReply{Message: in.GetMotor() + " Running"}, nil
}

func (s *motorServer) StopMotor(ctx context.Context, in *pBuff.MotorRequest) (*pBuff.StatusReply, error) {
	motor := getMotorHandle(in.GetMotor())
	motor.Command(stop)

	return &pBuff.StatusReply{Message: in.GetMotor() + " Motor stopped"}, nil
}

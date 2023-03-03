package main

import (
	"context"
	"log"
	"time"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"

	pBuff "main/proto"
)

func main() {
	clientConn, _ := grpc.Dial("192.168.0.97:50051", grpc.WithTransportCredentials(insecure.NewCredentials()))
	defer clientConn.Close()
	client := pBuff.NewMotorsClient(clientConn)

	ctx := context.Background()

	// RUN BOTH MOTORS
	reply, _ := client.RunMotor(ctx, &pBuff.MotorRequest{Motor: "A"})
	log.Printf("Motor status: %s", reply.GetMessage())
	reply, _ = client.RunMotor(ctx, &pBuff.MotorRequest{Motor: "D"})
	log.Printf("Motor status: %s", reply.GetMessage())

	// Run motors for 2 seconds
	time.Sleep(2 * time.Second)

	// STOP BOTH MOTORS
	reply, _ = client.StopMotor(ctx, &pBuff.MotorRequest{Motor: "A"})
	log.Printf("Motor status: %s", reply.GetMessage())
	reply, _ = client.StopMotor(ctx, &pBuff.MotorRequest{Motor: "D"})
	log.Printf("Motor status: %s", reply.GetMessage())
}

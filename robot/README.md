# The Robot

## Prerequisites 
1. Get protobuf compiler
2. Run `go install google.golang.org/protobuf/cmd/protoc-gen-go@v1.28`
3. Run `go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@v1.2`

## How to build and run
1. Build the proto files for go using the below command in the proto directory.  
```
protoc --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative robot.proto
```

2. Build the project when standing at the robot root
```
GOOS=linux GOARCH=arm GOARM=5 go build main
```

3. ssh into robot and disable robot service
```
ssh robot@<ip-addr>

sudo systemctl stop robot
```

4. Transfer the file to the robot using scp
```
scp <main file> <ip-addr>:<port>:.
```

6. ssh into robot and start robot server
```
ssh robot@<ip-addr>

sudo systemctl start robot
```

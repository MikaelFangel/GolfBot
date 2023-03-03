# The Robot

## How to build and run
1. Build the proto files for go using the below command in the proto directory.  
```
rotoc --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative robot.proto
```
2. Build the project when standing at the robot root
```
GOOS=linux GOARCH=arm GOARM=5 go build main
```
3. Transfer the file to the robot using scp
4. Run the go server using ssh

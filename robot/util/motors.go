package util

import (
	"github.com/ev3go/ev3dev"
	"math"
)

// GetMotorHandle Returns the TachoMotor equivalent to the port given (e.g. port "A").
func GetMotorHandle(port string, motor string) (*ev3dev.TachoMotor, error) {
	return ev3dev.TachoMotorFor("ev3-ports:out"+port, "lego-ev3-"+motor+"-motor")
}

// IsRunning Returns true if the speed of the given motor is not zero, otherwise false.
func IsRunning(motor *ev3dev.TachoMotor) bool {
	speed, _ := motor.Speed()

	return speed != 0
}

// ConvertDistanceToWheelRotation converters a wheel radius into the number of motor pulses.
func ConvertDistanceToWheelRotation(distance float64, wheelRadius float64) int {
	// Radius values are in centimeters and is measured from the inner sides of the wheels.
	wheelCircumference := 2 * wheelRadius * math.Pi

	return int((distance / wheelCircumference) * 360)
}

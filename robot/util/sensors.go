package util

import (
	"github.com/ev3go/ev3dev"
	pBuff "main/proto"
	"strconv"
)

// GetSensor Returns the requested sensor from the input ports of the robot
func GetSensor(inPort string, sensor string) (*ev3dev.Sensor, error) {
	return ev3dev.SensorFor("ev3-ports:"+inPort, "lego-ev3-"+sensor)
}

// GetDistanceInCm Returns the distance to the closest object from the ultrasonic sensor
func GetDistanceInCm() float64 {
	ultraSonicSensor, err := GetSensor(pBuff.InPort_in1.String(), pBuff.Sensor_us.String())
	if err != nil {
		return -1
	}

	distanceString, _ := ultraSonicSensor.Value(0)
	distance, _ := strconv.ParseFloat(distanceString, 64)
	mmToCmConversionFactor := 10.
	return distance / mmToCmConversionFactor
}

// GetGyroValue Return the current gyro value as a float64
func GetGyroValue(gyro *ev3dev.Sensor) (float64, error) {
	gyroValStr, _ := gyro.Value(0)
	return strconv.ParseFloat(gyroValStr, 64)
}

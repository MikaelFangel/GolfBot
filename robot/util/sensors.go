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

func RecalibrateGyro() (*ev3dev.Sensor, error) {
	gyro, err := GetSensor(pBuff.InPort_in1.String(), pBuff.Sensor_gyro.String())
	if err != nil {
		return nil, err
	}

	// Trigger the recalibration using a mode switch
	gyro.SetMode("GYRO-CAL")
	gyro.SetMode("GYRO-ANG")

	return gyro, nil
}

// GetDistanceInCm Returns the distance to the closest object from the ultrasonic sensor
func GetDistanceInCm() float64 {
	ultraSonicSensor, err := GetSensor(pBuff.InPort_in1.String(), pBuff.Sensor_us.String())
	if err != nil {
		return -1
	}

	distanceString, _ := ultraSonicSensor.Value(0)
	distance, _ := strconv.ParseFloat(distanceString, 64)
	return distance / 10
}

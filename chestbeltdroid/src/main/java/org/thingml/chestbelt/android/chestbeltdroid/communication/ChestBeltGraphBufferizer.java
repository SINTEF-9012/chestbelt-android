package org.thingml.chestbelt.android.chestbeltdroid.communication;

import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphBuffer;

public class ChestBeltGraphBufferizer implements ChestBeltListener {

	private GraphBuffer bufferHeartrate = new GraphBuffer();
	//private GraphBuffer bufferHeartrateConfidence = new GraphBuffer();
	
	private GraphBuffer bufferTemperature = new GraphBuffer();
	private GraphBuffer bufferBattery = new GraphBuffer();
	private GraphBuffer bufferActivityLevel = new GraphBuffer();
	
	private GraphBuffer bufferECG = new GraphBuffer(400);
	//private GraphBuffer bufferECGSignalQuality = new GraphBuffer();
	
	private GraphBuffer bufferGyroPitch = new GraphBuffer(250);
	private GraphBuffer bufferGyroRoll = new GraphBuffer(250);
	private GraphBuffer bufferGyroYaw = new GraphBuffer(250);
	
	private GraphBuffer bufferAccLateral = new GraphBuffer(250);
	private GraphBuffer bufferAccLongitudinal = new GraphBuffer(250);
	private GraphBuffer bufferAccVertical = new GraphBuffer(250);
	
	public GraphBuffer getBufferHeartrate() {
		return bufferHeartrate;
	}
	
	public GraphBuffer getBufferTemperature() {
		return bufferTemperature;
	}
	
	public GraphBuffer getBufferBattery() {
		return bufferBattery;
	}
	
	public GraphBuffer getBufferActivityLevel() {
		return bufferActivityLevel;
	}
	
	public GraphBuffer getBufferECG() {
		return bufferECG;
	}

//	public GraphBuffer getBufferECGSignalQuality() {
//		return bufferECGSignalQuality;
//	}

	public GraphBuffer getBufferGyroPitch() {
		return bufferGyroPitch;
	}

	public GraphBuffer getBufferGyroRoll() {
		return bufferGyroRoll;
	}

	public GraphBuffer getBufferGyroYaw() {
		return bufferGyroYaw;
	}

//	public GraphBuffer getBufferHeartrateConfidence() {
//		return bufferHeartrateConfidence;
//	}

	public GraphBuffer getBufferAccLateral() {
		return bufferAccLateral;
	}

	public GraphBuffer getBufferAccLongitudinal() {
		return bufferAccLongitudinal;
	}

	public GraphBuffer getBufferAccVertical() {
		return bufferAccVertical;
	}

	@Override
	public void cUSerialNumber(long value, int timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cUFWRevision(long value, int timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void batteryStatus(final int value, int timestamp) {
		bufferBattery.insertData(value);
	}

	@Override
	public void indication(int value, int timestamp) {
		if (value >= 10 && value <= 13) {
			bufferActivityLevel.insertData(value - 10);
		}
	}

	@Override
	public void status(int value, int timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageOverrun(int value, int timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void referenceClockTime(long value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fullClockTimeSync(long value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void heartRate(final int value, int timestamp) {
		bufferHeartrate.insertData(value/10);
	}

	@Override
	public void heartRateConfidence(int value, int timestamp) {
		//bufferHeartrateConfidence.insertData(value);
	}

	@Override
	public void eCGData(final int value) {
		bufferECG.insertData(value);
	}

	@Override
	public void eCGSignalQuality(int value, int timestamp) {
		//bufferECGSignalQuality.insertData(value);
	}

	@Override
	public void eCGRaw(int value, int timestamp) {
		bufferECG.insertData(value);	
	}

	@Override
	public void gyroPitch(int value, int timestamp) {
		bufferGyroPitch.insertData(value);
	}

	@Override
	public void gyroRoll(int value, int timestamp) {
		bufferGyroRoll.insertData(value);
	}

	@Override
	public void gyroYaw(int value, int timestamp) {
		bufferGyroYaw.insertData(value);
	}

	@Override
	public void accLateral(int value, int timestamp) {
		bufferAccLateral.insertData(value);
	}

	@Override
	public void accLongitudinal(int value, int timestamp) {
		bufferAccLongitudinal.insertData(value);
	}

	@Override
	public void accVertical(int value, int timestamp) {
		bufferAccVertical.insertData(value);
	}

	@Override
	public void rawActivityLevel(int value, int timestamp) {
		bufferActivityLevel.insertData(value - 10);
	}

	@Override
	public void combinedIMU(int ax, int ay, int az, int gx, int gy, int gz, int timestamp) {
		bufferAccLateral.insertData(ay);
		bufferAccLongitudinal.insertData(az);
		bufferAccVertical.insertData(ax);
		bufferGyroPitch.insertData(gy);
		bufferGyroRoll.insertData(gx);
		bufferGyroYaw.insertData(gz);
	}

	@Override
	public void skinTemperature(final int value, int timestamp) {
		bufferTemperature.insertData(value/10);	
	}

}

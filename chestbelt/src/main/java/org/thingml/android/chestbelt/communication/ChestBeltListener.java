package org.thingml.android.chestbelt.communication;

public interface ChestBeltListener {
	
		// Control and status messages
		void cUSerialNumber(long value, int timestamp);// 		@code "110"; 
		void cUFWRevision(long value, int timestamp);// 			@code "117";
		void batteryStatus(int value, int timestamp);// 		@code "98";
		void indication(int value, int timestamp);// 			@code "105";
		void status(int value, int timestamp);// 			@code "109";
		void messageOverrun(int value, int timestamp);//  		@code "100";
		
		// Time and clock synchronization messages
		void referenceClockTime(long value);// 						@code "106";
		void fullClockTimeSync(long value);// 						@code "107";
		
		// ECG and Heart rate messages
		void heartRate(int value, int timestamp);//  			@code "104"; //12 bit value in 0.1 bpm, ie 0.0-409.5 bpm
		void heartRateConfidence(int value, int timestamp);//  	@code "99";
		void eCGData(int value);//  			@code "101";
		void eCGSignalQuality(int value, int timestamp);//  	@code "102";
		void eCGRaw(int value, int timestamp);//  				@code "103"; // Not sure which data type
		
		// Gyroscope messages
		void gyroPitch(int value, int timestamp);//  			@code "112";
		void gyroRoll(int value, int timestamp);// 			@code "114";
		void gyroYaw(int value, int timestamp);//  			@code "121";
		
		// Accelerometer and activity messages
		void accLateral(int value, int timestamp);//  			@code "115";
		void accLongitudinal(int value, int timestamp);//  	@code "119";
		void accVertical(int value, int timestamp);//  		@code "118";
		void rawActivityLevel(int value, int timestamp);//  		@code "97";
		
		void combinedIMU(int ax, int ay, int az, int gx, int gy, int gz, int timestamp);//  		@code "120";
		
		// IR Temperature sensor messages
		void skinTemperature(int value, int timestamp);//  	@code "116";
		
}

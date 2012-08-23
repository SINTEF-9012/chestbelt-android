package org.thingml.chestbelt.android.chestbeltdroid.communication;


public enum ChestBeltMode {
	RawGyroMode (29),
	Extracted(30),
	FullECG(31),
	Raw(32),
	Test(33),
	RawAccelerometer(34);
	
	private final int code;
        
        public static ChestBeltMode fromCode(int code) {
            switch(code) {
                case 29: return ChestBeltMode.RawGyroMode;
                case 30: return ChestBeltMode.Extracted;
                case 31: return ChestBeltMode.FullECG;
                case 32: return ChestBeltMode.Raw;
                case 33: return ChestBeltMode.Test;
                case 34: return ChestBeltMode.RawAccelerometer;
                default: return null;
            }
        }
	
	private ChestBeltMode(int c) {
		this.code = c;
	}
	
	public int getCode() {
		return this.code;
	}
}

package com.android.server;


import android.os.ICanbusService;

public class CanbusService extends ICanbusService.Stub {
	
	class CarInfo {
		boolean bluetooth = false;
		boolean keyIn = false;
		boolean park = false;
	}

  private static final String TAG = "CanbusService";

  CanbusService() {
	  init();
  }
  
  public boolean getBluetooth() {
	  return get_bluetooth();
  }
  
  
  private static native boolean init();
  
  //interface
  //1 mean bluetooth is open
  //0 mean bluetooth is closed
  // device error ?
  private static native boolean get_bluetooth();
  
//public CarInfo getCarInfo() {
//	  return get_car_info();
//}
  
//  private static native CarInfo get_car_info();
  
}

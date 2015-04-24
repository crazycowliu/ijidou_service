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
	  System.out.println("CanbusService.java: init");
	  init();
	  System.out.println("CanbusService.java: finished");
  }
  
  public boolean getBluetooth() {
	  return get_bluetooth();
  }
  
  public boolean isActiveBluetooth(){
		return is_active_bluetooth();
  }
  
  private static native boolean init();
  
  //interface
  //1 mean bluetooth is open
  //0 mean bluetooth is closed
  // device error ?
  private static native boolean get_bluetooth();

  private static native boolean is_active_bluetooth();
  
//public CarInfo getCarInfo() {
//	  return get_car_info();
//}

//  private static native CarInfo get_car_info();

}

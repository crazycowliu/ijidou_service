package com.android.server;

import android.os.IHelloService;
import android.os.Person;
import android.os.RemoteException;
import android.util.Log;

public class HelloService extends IHelloService.Stub {
  private static final String TAG = "HelloService";

  HelloService() {
    init_native();
  }

  public void setVal(int val, char num) {
    setVal_native(val, num);
  }

  public int getVal(char num) {
    return getVal_native(num);
  }
  
  public String greet(Person person) throws RemoteException {  
	Log.i(TAG, "greet(Person person) called");
	
	String greeting = "hello, " + person.getName();  
	switch (person.getSex()) {  
	case 0:  
	    greeting = greeting + ", you're handsome.";  
	    break;  
	case 1:  
	    greeting = greeting + ", you're beautiful.";  
	    break;  
	}  
	return greeting;  
  }
  
  private static native boolean init_native();
  private static native void setVal_native(int val, char num);
  private static native int getVal_native(char num);
}

package com.android.server;

import android.os.IHelloService;

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
  
  private static native boolean init_native();
  private static native void setVal_native(int val, char num);
  private static native int getVal_native(char num);
}

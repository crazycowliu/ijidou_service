/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\workspace\\eclipse\\AidlService\\src\\android\\os\\ICanbusService.aidl
 */
package android.os;
public interface ICanbusService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements android.os.ICanbusService
{
private static final java.lang.String DESCRIPTOR = "android.os.ICanbusService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an android.os.ICanbusService interface,
 * generating a proxy if needed.
 */
public static android.os.ICanbusService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof android.os.ICanbusService))) {
return ((android.os.ICanbusService)iin);
}
return new android.os.ICanbusService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getBluetooth:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.getBluetooth();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements android.os.ICanbusService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public boolean getBluetooth() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getBluetooth, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getBluetooth = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public boolean getBluetooth() throws android.os.RemoteException;
}

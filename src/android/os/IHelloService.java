/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\workspace\\eclipse\\AidlService\\src\\android\\os\\IHelloService.aidl
 */
package android.os;
public interface IHelloService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements android.os.IHelloService
{
private static final java.lang.String DESCRIPTOR = "android.os.IHelloService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an android.os.IHelloService interface,
 * generating a proxy if needed.
 */
public static android.os.IHelloService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof android.os.IHelloService))) {
return ((android.os.IHelloService)iin);
}
return new android.os.IHelloService.Stub.Proxy(obj);
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
case TRANSACTION_setVal:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
char _arg1;
_arg1 = (char)data.readInt();
this.setVal(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_getVal:
{
data.enforceInterface(DESCRIPTOR);
char _arg0;
_arg0 = (char)data.readInt();
int _result = this.getVal(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements android.os.IHelloService
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
@Override public void setVal(int val, char num) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(val);
_data.writeInt(((int)num));
mRemote.transact(Stub.TRANSACTION_setVal, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int getVal(char num) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((int)num));
mRemote.transact(Stub.TRANSACTION_getVal, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_setVal = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getVal = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public void setVal(int val, char num) throws android.os.RemoteException;
public int getVal(char num) throws android.os.RemoteException;
}

/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\workspace\\eclipse\\hello\\src\\android\\os\\ICanbusService.aidl
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
case TRANSACTION_queryMessage:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
byte[] _arg1;
_arg1 = data.createByteArray();
byte[] _result = this.queryMessage(_arg0, _arg1);
reply.writeNoException();
reply.writeByteArray(_result);
return true;
}
case TRANSACTION_addListener:
{
data.enforceInterface(DESCRIPTOR);
android.os.ICanbusListener _arg0;
_arg0 = android.os.ICanbusListener.Stub.asInterface(data.readStrongBinder());
this.addListener(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_removeListener:
{
data.enforceInterface(DESCRIPTOR);
android.os.ICanbusListener _arg0;
_arg0 = android.os.ICanbusListener.Stub.asInterface(data.readStrongBinder());
this.removeListener(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_addListener2:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
android.os.ICanbusListener _arg1;
_arg1 = android.os.ICanbusListener.Stub.asInterface(data.readStrongBinder());
this.addListener2(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_removeListener2:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
android.os.ICanbusListener _arg1;
_arg1 = android.os.ICanbusListener.Stub.asInterface(data.readStrongBinder());
this.removeListener2(_arg0, _arg1);
reply.writeNoException();
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
/**
	   * Its invoker should make sure the given bufs has enough size
	   */
@Override public byte[] queryMessage(int comId, byte[] queryMsg) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
byte[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(comId);
_data.writeByteArray(queryMsg);
mRemote.transact(Stub.TRANSACTION_queryMessage, _data, _reply, 0);
_reply.readException();
_result = _reply.createByteArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
      * Register a callback.
      */
@Override public void addListener(android.os.ICanbusListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_addListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
      * Unregister a callback.
      */
@Override public void removeListener(android.os.ICanbusListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_removeListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
      * Register a callback, according to comId
      */
@Override public void addListener2(int comId, android.os.ICanbusListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(comId);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_addListener2, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
      * Unregister a callback, according to comId
      */
@Override public void removeListener2(int comId, android.os.ICanbusListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(comId);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_removeListener2, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_queryMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_addListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_removeListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_addListener2 = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_removeListener2 = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
/**
	   * Its invoker should make sure the given bufs has enough size
	   */
public byte[] queryMessage(int comId, byte[] queryMsg) throws android.os.RemoteException;
/**
      * Register a callback.
      */
public void addListener(android.os.ICanbusListener listener) throws android.os.RemoteException;
/**
      * Unregister a callback.
      */
public void removeListener(android.os.ICanbusListener listener) throws android.os.RemoteException;
/**
      * Register a callback, according to comId
      */
public void addListener2(int comId, android.os.ICanbusListener listener) throws android.os.RemoteException;
/**
      * Unregister a callback, according to comId
      */
public void removeListener2(int comId, android.os.ICanbusListener listener) throws android.os.RemoteException;
}

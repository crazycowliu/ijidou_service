/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\workspace\\eclipse\\hello2\\src\\android\\os\\ICanbusListener.aidl
 */
package android.os;
public interface ICanbusListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements android.os.ICanbusListener
{
private static final java.lang.String DESCRIPTOR = "android.os.ICanbusListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an android.os.ICanbusListener interface,
 * generating a proxy if needed.
 */
public static android.os.ICanbusListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof android.os.ICanbusListener))) {
return ((android.os.ICanbusListener)iin);
}
return new android.os.ICanbusListener.Stub.Proxy(obj);
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
case TRANSACTION_actionPerformed:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.actionPerformed(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_messageArrived:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
byte[] _arg1;
_arg1 = data.createByteArray();
int _arg2;
_arg2 = data.readInt();
this.messageArrived(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements android.os.ICanbusListener
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
@Override public void actionPerformed(int actionId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(actionId);
mRemote.transact(Stub.TRANSACTION_actionPerformed, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
//call back, so message is in-type

@Override public void messageArrived(int msgId, byte[] message, int length) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(msgId);
_data.writeByteArray(message);
_data.writeInt(length);
mRemote.transact(Stub.TRANSACTION_messageArrived, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_actionPerformed = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_messageArrived = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public void actionPerformed(int actionId) throws android.os.RemoteException;
//call back, so message is in-type

public void messageArrived(int msgId, byte[] message, int length) throws android.os.RemoteException;

}

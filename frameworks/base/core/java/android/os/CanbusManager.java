package android.os;

import java.text.NumberFormat;

import android.content.Context;
import android.util.Log;

public class CanbusManager {
	private static final String TAG = "CanbusManager";

	/********************* CanBus signal **************************/
	public final int CARSIGNAL_BASICINFO = 0x72;
	public final int CARSIGNAL_AIRCONDITIONER = 0x73;
	public final int CARSIGNAL_SETINFO = 0x47;
	public final int CARSIGNAL_DETAILS_ONE = 0x12;
	public final int CARSIGNAL_DETAILS_TWO = 0x13;
	public final int CARSIGNAL_RADAR = 0x42;

	/********************* define bit **********************/
	private final int BIT_0 = 0x01;
	private final int BIT_1 = 0x02;
	private final int BIT_2 = 0x04;
	private final int BIT_3 = 0x08;
	private final int BIT_4 = 0x10;
	private final int BIT_5 = 0x20;
	private final int BIT_6 = 0x40;
	private final int BIT_7 = 0x80;

	ICanbusService mService;
	private final Binder mToken = new Binder();

	private CarBasicInfoListener mCarBasicInfoListener = null;
	private CarInfoDetailsListener mCarInfoDetailsListener = null;
	private AirconditonerListener mAirconditonerListener = null;
	private CarRadarListener mCarRadarListener = null;

	private Handler mHandler;
	private int type;

	/**
	 * �յ���Ϣ����
	 */
	private static byte[] airconditioner_data = null;

	/**
	 * �����̰�������
	 * 
	 * @author zj
	 * 
	 */
	public enum SteeringWheelKeyCode {
		NULL, VOLUME_ADD, VOLUME_REDUCE, MUTE, ANSWER_CALL, HANGUP_CALL, PREVIEW, NEXT, MENU
	}

	/**
	 * �յ�ѭ��״̬����
	 * 
	 * @author zj
	 */
	public enum AirConditionerLoopStyle {
		OUTCYCLE, MANUAL_INCYCLE, AUTO_INCYCLE
	}

	public void openClient(Handler mHandler, int type, int platform) {
		//finish it!  using handler to transmit  data with canbus;
	};

	public void onDestory() {
		try {
			mService.removeListener(CanDataChangedListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void registerCarBasciInfoListener(
			CarBasicInfoListener mCarBasicInfoListener) {
		if (mCarBasicInfoListener == null) {
			Log.e(TAG, "CarBasicInfoListener is null");
			return;
		}
		this.mCarBasicInfoListener = mCarBasicInfoListener;
	}

	public void registerCarInfoDetailsListener(
			CarInfoDetailsListener mCarInfoDetailsListener) {
		if (mCarInfoDetailsListener == null) {
			Log.e(TAG, "CarInfoDetailsListener is null");
		}
		this.mCarInfoDetailsListener = mCarInfoDetailsListener;
	}

	public void registerCarRadarListener(CarRadarListener mCarRadarListener) {
		if (mCarRadarListener == null) {
			Log.e(TAG, "CarRadarListener is null");
		}
		this.mCarRadarListener = mCarRadarListener;
	}

	public void registerAirconditonerListener(
			AirconditonerListener mAirconditonerListener) {
		if (mAirconditonerListener == null) {
			Log.e(TAG, "AirconditonerListener is null");
		}
		this.mAirconditonerListener = mAirconditonerListener;
	}

	/**
	 * ���������Ϣ
	 * 
	 * @author zj
	 */
	public interface CarBasicInfoListener {

		/**
		 * Acc״̬
		 * 
		 * @param i
		 */
		public abstract void accOn(boolean state);

		/**
		 * ���״̬
		 * 
		 * @param i
		 */
		public abstract void illOn(boolean state);

		/**
		 * ��ɲ����
		 * 
		 * @param i
		 */
		public abstract void revOn(boolean state);

		/**
		 * �Զ�����
		 * 
		 * @param state
		 */
		public abstract void parkOn(boolean state);

		/**
		 * Կ�ײ���
		 * 
		 * @param state
		 */
		public abstract void keyIn(boolean state);

		/**
		 * �״���Ч
		 * 
		 * @param state
		 */
		public abstract void raderAvailable(boolean state);

		/**
		 * ������Ч
		 * 
		 * @param state
		 */
		public abstract void bluetoothAvailable(boolean state);

		/**
		 * �����̰���
		 * 
		 * @param keyCode
		 */
		public abstract void steeringWheelBtn(SteeringWheelKeyCode keyCode);

		/**
		 * ��Ƶƹ�����
		 * 
		 * @param value
		 */
		public abstract void illValue(int value);
	}

	/**
	 * �״�״̬
	 * 
	 * @author zj
	 */
	public interface CarRadarListener {

		/**
		 * ��ת��
		 * 
		 * @param angel
		 */
		public abstract void leftAngel(int angel);

		/**
		 * ��ת��
		 * 
		 * @param angel
		 */
		public abstract void rightAngel(int angel);

		/**
		 * �״��ϰ���������
		 * 
		 * @param state
		 */
		public abstract void radarState(int[] state);

		/**
		 * �������״�������
		 * 
		 * @param state
		 */
		public abstract void radarAssitState(int[] state);
	}

	/**
	 * �յ�״̬
	 * 
	 * @author zj
	 */
	public interface AirconditonerListener {

		/**
		 * �յ�������Ϣ��ʾ data��0��
		 * 
		 * @param auto2
		 * @param acmax
		 * @param dual
		 * @param auto
		 * @param loopStyle
		 *            ѭ��ģʽ
		 * @param acswtich
		 *            �յ�����
		 * @param acdisplay
		 *            �յ���ʾ
		 */
		public abstract void acBasicDataChanged(boolean auto2, boolean acmax,
				boolean dual, boolean auto, AirConditionerLoopStyle loopStyle,
				boolean acswtich, boolean acdisplay);

		/**
		 * �ڲ�һЩ��Ϣ�ı� data��1��
		 * 
		 * @param leftHeating
		 * @param rightHeating
		 * @param frontDemist
		 * @param rearDemist
		 * @param acOn
		 * @param rearAcEnable
		 */
		public abstract void acInnerDataChanged(int leftHeating,
				int rightHeating, boolean frontDemist, boolean rearDemist,
				boolean acOn, boolean rearAcEnable);

		/**
		 * �趨�¶� data��2��,data��3��
		 * 
		 * @param lefttemp
		 * @param righttemp
		 */
		public abstract void frontSettemp(int lefttemp, int righttemp);

		/**
		 * ����յ��󴵷� data��4��
		 * 
		 * @param state
		 * @param windSpeed
		 */
		public abstract void leftHairdryer(int state, int windSpeed);

		/**
		 * ����յ��Ҵ��� data��5��
		 * 
		 * @param state
		 * @param windSpeed
		 */
		public abstract void rightHairdryer(int state, int windSpeed);

		/**
		 * �����¶� data��6��
		 * 
		 * @param temp
		 */
		public abstract void outCarTemp(int temp);

		/**
		 * ����״̬ data��7��
		 * 
		 * @param available
		 * @param hoodOpen
		 * @param trunkOpen
		 * @param behindRightDoor
		 * @param behindLeftDoor
		 * @param driverDoor
		 * @param paseendardoor
		 */
		public abstract void doorState(boolean available, boolean hoodOpen,
				boolean trunkOpen, boolean behindRightDoor,
				boolean behindLeftDoor, boolean driverDoor,
				boolean paseendardoor);
	}

	/**
	 * ������Ϣ
	 * 
	 * @author zj
	 */
	public interface CarInfoDetailsListener {
		/**
		 * ʣ������
		 * 
		 * @param oil
		 */
		public abstract void oil(int oil);

		/**
		 * ��ѹ
		 * 
		 * @param battery
		 */
		public abstract void battery(double battery);

		/**
		 * ��ʻ���
		 * 
		 * @param msb
		 */
		public abstract void mileage(int msb);

		/**
		 * ������ת��
		 */
		public abstract void engineSpeed(int speed);

		/**
		 * ��ϴҺ������
		 */
		public abstract void liquidWarning(boolean state);

		/**
		 * ��ȫ��״̬
		 * 
		 * @param state
		 */
		public abstract void safetybeltWaring(boolean state);

		/**
		 * ��ص�ѹ������
		 * 
		 * @param state
		 */
		public abstract void batteryWarning(boolean state);

		/**
		 * ����������
		 * 
		 * @param state
		 */
		public abstract void oilWarning(boolean state);

		/**
		 * ˲ʱ�ͺ�
		 * 
		 * @param ifc
		 */
		public abstract void ifc(double ifcdata);

		/**
		 * ��׼����
		 */
		public abstract void speed(double speed);

		/**
		 * �����ٶ�
		 * 
		 * @param kmh
		 */
		public abstract void basicspeed(int kmh);
	}

	/**
	 * @hide
	 */
	public CanbusManager(ICanbusService service, Context ctx) {
		mService = service;
		try {
			service.addListener(CanDataChangedListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return version
	 */
	public String getCanDeviceVersion() {
		return "demo-version";
	}

	private ICanbusListener CanDataChangedListener = new ICanbusListener.Stub() {
		@Override
		public void messageArrived(final int msgId, byte[] message, int length)
				throws RemoteException {
			switch (msgId) {
			case CARSIGNAL_BASICINFO:
				final byte basic_signal = message[4];
				if (mCarBasicInfoListener != null) {
					mCarBasicInfoListener
							.accOn((basic_signal & BIT_0) == BIT_0);
					mCarBasicInfoListener
							.illOn((basic_signal & BIT_1) == BIT_1);
					mCarBasicInfoListener
							.revOn((basic_signal & BIT_2) == BIT_2);
					mCarBasicInfoListener
							.parkOn((basic_signal & BIT_3) == BIT_3);
					mCarBasicInfoListener
							.keyIn((basic_signal & BIT_4) == BIT_4);
					mCarBasicInfoListener
							.raderAvailable((basic_signal & BIT_5) == BIT_5);
					mCarBasicInfoListener
							.bluetoothAvailable((basic_signal & BIT_6) == BIT_6);

					if (mCarInfoDetailsListener != null) {
						mCarInfoDetailsListener.basicspeed(message[5]);
					}

					SteeringWheelKeyCode swk = SteeringWheelKeyCode.NULL;
					switch (message[6]) {
					case 0x00:
						break;

					case 0x01:
						// ����������
						swk = SteeringWheelKeyCode.VOLUME_ADD;
						break;

					case 0x02:
						// ����������
						swk = SteeringWheelKeyCode.VOLUME_REDUCE;
						break;

					case 0x03:
						// �����̾�����ť
						swk = SteeringWheelKeyCode.MUTE;
						break;

					case 0x04:
						break;

					case 0x05:
						// �ӵ绰
						swk = SteeringWheelKeyCode.ANSWER_CALL;
						break;

					case 0x06:
						// �ҵ绰
						swk = SteeringWheelKeyCode.HANGUP_CALL;
						break;

					case 0x07:
						break;

					case 0x08:
						// ��һ��
						swk = SteeringWheelKeyCode.PREVIEW;
						break;
					case 0x09:
						// ��һ��
						swk = SteeringWheelKeyCode.NEXT;
						break;

					case 0x0A:
						// �˵���ť
						swk = SteeringWheelKeyCode.MENU;
						break;
					}

					mCarBasicInfoListener.steeringWheelBtn(swk);
					mCarBasicInfoListener.illValue(message[7]);

					if (mCarRadarListener != null) {
						mCarRadarListener.leftAngel(message[8]);
						mCarRadarListener.rightAngel(message[9]);
						mCarRadarListener.radarState(new int[] { message[10],
								message[11], message[12], message[13],
								message[14], message[15], message[16],
								message[17] });
					}
				} else {
					Log.e(TAG,
							" CarBasicInfoListener Null:Byte [] handle error");
				}
				break;
			case CARSIGNAL_AIRCONDITIONER:
				if (airconditioner_data != null) {
					if (mAirconditonerListener != null) {
						if (airconditioner_data[4] != message[4]) {
							// �����ʾdata��0��״̬�ı�,��������
							final byte data = message[4];
							final int byte4 = (data & BIT_4) | (data & BIT_5);
							AirConditionerLoopStyle loopStyle = AirConditionerLoopStyle.OUTCYCLE;
							switch (byte4) {
							case 16:
								// �ֶ���ѭ��
								loopStyle = AirConditionerLoopStyle.MANUAL_INCYCLE;
								break;
							case 48:
								// �Զ���ѭ��
								loopStyle = AirConditionerLoopStyle.AUTO_INCYCLE;
								break;
							}
							mAirconditonerListener.acBasicDataChanged(
									(data & BIT_0) == BIT_0,
									(data & BIT_1) == BIT_1,
									(data & BIT_2) == BIT_2,
									(data & BIT_3) == BIT_3, loopStyle,
									(data & BIT_6) == BIT_6,
									(data & BIT_7) == BIT_7);
						}

						if (airconditioner_data[5] != message[5]) {
							// �����ʾdata��1��״̬�ı�,��������
							final byte data = message[5];
							mAirconditonerListener.acInnerDataChanged(
									(data & BIT_0) | (data & BIT_1),
									(data & BIT_2) | (data & BIT_3),
									(data & BIT_4) == BIT_4,
									(data & BIT_5) == BIT_5,
									(data & BIT_6) == BIT_6,
									(data & BIT_7) == BIT_7);
						}

						if (airconditioner_data[6] != message[6]
								|| airconditioner_data[7] != message[7]) {
							// ǰ�������趨�¶�,�����ʾdata��2��,data��3��״̬�ı�,��������
							final byte dataleft = message[6];
							final byte dataright = message[7];
							mAirconditonerListener.frontSettemp(dataleft,
									dataright);
						}

						if (airconditioner_data[8] != message[8]) {
							// �󴵷�
							final byte data = message[8];
							final int type1 = data & BIT_4;
							final int type2 = data & BIT_5;
							final int type3 = data & BIT_6;
							/**
							 * ������ 0��1��2����״̬�� 0��ʾ������1��ʾ����2��ʾ����
							 */
							mAirconditonerListener.leftHairdryer(
									type1 == BIT_4 ? 0 : type2 == BIT_5 ? 1
											: type3 == BIT_6 ? 2 : 0,
									(data & BIT_0) | (data & BIT_1)
											| (data & BIT_2) | (data & BIT_3));
						}

						if (airconditioner_data[9] != message[9]) {
							// �Ҵ���
							final byte data = message[9];
							final int type1 = data & BIT_4;
							final int type2 = data & BIT_5;
							final int type3 = data & BIT_6;
							/**
							 * ������ 0��1��2����״̬�� 0��ʾ������1��ʾ����2��ʾ����
							 */
							mAirconditonerListener.rightHairdryer(
									type1 == BIT_4 ? 0 : type2 == BIT_5 ? 1
											: type3 == BIT_6 ? 2 : 0,
									(data & BIT_0) | (data & BIT_1)
											| (data & BIT_2) | (data & BIT_3));
						}

						if (airconditioner_data[10] != message[10]) {
							// �����¶�
							final byte data = message[10];
							mAirconditonerListener
									.outCarTemp((int) (data * 0.5 - 40));
						}

						if (airconditioner_data[11] != message[11]) {
							// ��״̬
							final byte data = message[10];
							mAirconditonerListener.doorState(
									(data & BIT_0) == BIT_0,
									(data & BIT_2) == BIT_2,
									(data & BIT_3) == BIT_3,
									(data & BIT_4) == BIT_4,
									(data & BIT_5) == BIT_5,
									(data & BIT_6) == BIT_6,
									(data & BIT_7) == BIT_7);
						}
					}
				} else {

					/***************************** DATA��0�� ************************************/
					final byte data4 = message[4];
					final int byte4 = (data4 & BIT_4) | (data4 & BIT_5);
					AirConditionerLoopStyle loopStyle = AirConditionerLoopStyle.OUTCYCLE;
					switch (byte4) {
					case 16:
						// �ֶ���ѭ��
						loopStyle = AirConditionerLoopStyle.MANUAL_INCYCLE;
						break;
					case 48:
						// �Զ���ѭ��
						loopStyle = AirConditionerLoopStyle.AUTO_INCYCLE;
						break;
					}
					mAirconditonerListener.acBasicDataChanged(
							(data4 & BIT_0) == BIT_0, (data4 & BIT_1) == BIT_1,
							(data4 & BIT_2) == BIT_2, (data4 & BIT_3) == BIT_3,
							loopStyle, (data4 & BIT_6) == BIT_6,
							(data4 & BIT_7) == BIT_7);

					/****************************** DATA��1�� ***********************************/
					final byte data5 = message[5];
					mAirconditonerListener.acInnerDataChanged((data5 & BIT_0)
							| (data5 & BIT_1), (data5 & BIT_2)
							| (data5 & BIT_3), (data5 & BIT_4) == BIT_4,
							(data5 & BIT_5) == BIT_5, (data5 & BIT_6) == BIT_6,
							(data5 & BIT_7) == BIT_7);

					/******************************* DATA��2�� DATA[3] ****************************/
					final byte dataleft = message[6];
					final byte dataright = message[7];
					mAirconditonerListener.frontSettemp(dataleft, dataright);

					/******************************* DATA��4�� *********************************/
					final byte data8 = message[8];
					final int type1 = data8 & BIT_4;
					final int type2 = data8 & BIT_5;
					final int type3 = data8 & BIT_6;
					/**
					 * ������ 0��1��2����״̬�� 0��ʾ������1��ʾ����2��ʾ����
					 */
					mAirconditonerListener.leftHairdryer(type1 == BIT_4 ? 0
							: type2 == BIT_5 ? 1 : type3 == BIT_6 ? 2 : 0,
							(data8 & BIT_0) | (data8 & BIT_1) | (data8 & BIT_2)
									| (data8 & BIT_3));

					/******************************* DATA��5�� *********************************/
					// �Ҵ���
					final byte data9 = message[9];
					final int utype1 = data9 & BIT_4;
					final int utype2 = data9 & BIT_5;
					final int utype3 = data9 & BIT_6;
					/**
					 * ������ 0��1��2����״̬�� 0��ʾ������1��ʾ����2��ʾ����
					 */
					mAirconditonerListener.rightHairdryer(utype1 == BIT_4 ? 0
							: utype2 == BIT_5 ? 1 : utype3 == BIT_6 ? 2 : 0,
							(data9 & BIT_0) | (data9 & BIT_1) | (data9 & BIT_2)
									| (data9 & BIT_3));

					/******************************* DATA��6�� *********************************/
					// �����¶�
					final byte data10 = message[10];
					mAirconditonerListener
							.outCarTemp((int) (data10 * 0.5 - 40));

					/******************************* DATA��7�� *********************************/
					// ��״̬
					final byte data11 = message[10];
					mAirconditonerListener.doorState((data11 & BIT_0) == BIT_0,
							(data11 & BIT_2) == BIT_2,
							(data11 & BIT_3) == BIT_3,
							(data11 & BIT_4) == BIT_4,
							(data11 & BIT_5) == BIT_5,
							(data11 & BIT_6) == BIT_6,
							(data11 & BIT_7) == BIT_7);
				}
				airconditioner_data = message;
				break;
			case CARSIGNAL_SETINFO:

				break;
			case CARSIGNAL_DETAILS_ONE:
				final byte byte3 = message[7];
				final byte byte4 = message[8];
				final byte byte6 = message[10];
				final byte byte7 = message[11];
				final byte byte8 = message[12];
				final byte byte9 = message[13];
				if (mCarInfoDetailsListener != null) {
					mCarInfoDetailsListener.ifc(Double.parseDouble(byte3 + "."
							+ byte4));
					mCarInfoDetailsListener
							.liquidWarning((BIT_4 & byte6) == BIT_4);
					mCarInfoDetailsListener
							.batteryWarning((BIT_6 & byte6) == BIT_6);
					mCarInfoDetailsListener
							.safetybeltWaring((BIT_5 & byte6) == BIT_5);
					mCarInfoDetailsListener
							.oilWarning((BIT_7 & byte6) == BIT_7);
					mCarInfoDetailsListener.oil(byte7);
					mCarInfoDetailsListener.battery(Double.parseDouble(byte8
							+ "." + byte9));
				} else {
					Log.e(TAG,
							" mCarInfoDetailsListener Null:Byte [] handle error");
				}
				break;
			case CARSIGNAL_DETAILS_TWO:
				final byte dbyte0 = message[4];
				final byte dbyte1 = message[5];
				final byte dbyte2 = message[6];

				final byte dbyte3 = message[7];
				final byte dbyte4 = message[8];

				final byte dbyte8 = message[12];
				final byte dbyte9 = message[13];
				if (mCarInfoDetailsListener != null) {

					// int miles = Integer.parseInt(coverNumber(dbyte0)
					// + coverNumber(dbyte1) + coverNumber(dbyte2), 16);

					int miles = coverNumber(dbyte0, dbyte1, dbyte2);

					mCarInfoDetailsListener.mileage(miles);
					mCarInfoDetailsListener.engineSpeed(Integer.parseInt(
							coverNumber(dbyte8) + coverNumber(dbyte9), 16));
					mCarInfoDetailsListener
							.speed((double) Integer.parseInt(
									coverNumber(dbyte3) + coverNumber(dbyte4),
									16) / 10);
				}
				break;
			case CARSIGNAL_RADAR:
				if (mCarRadarListener != null) {
					mCarRadarListener.radarAssitState(new int[] { message[4],
							message[5], message[6], message[7], message[8],
							message[9], message[10], message[11] });
				}
				break;
			}
		}
	};

	/**
	 * λ������
	 * 
	 * @param byte0
	 * @param byte1
	 * @param byte2
	 * @return
	 */
	private int coverNumber(byte byte0, byte byte1, byte byte2) {
		int rc = (byte0 << 16 | byte1 << 8 | byte2) & 0x00FFFFFF;
		return rc;
	};

	/**
	 * 0x
	 * 
	 * @param number
	 * @return
	 */
	private String coverNumber(int number) {
		NumberFormat formatter = NumberFormat.getNumberInstance();
		formatter.setMinimumIntegerDigits(2);
		formatter.setGroupingUsed(false);
		return formatter.format(number);
	}
}

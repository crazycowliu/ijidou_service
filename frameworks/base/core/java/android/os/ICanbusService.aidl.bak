package android.os;

interface ICanbusService {
	
	
	/*** ComId: 0x72, Canbus -> Host,   data length = 0x0E: Car Basic Info 					***/
	/*** ComId: 0x73, Canbus -> Host,   data length = 0x08: AC info 						***/
	/*** ComId: 0x12, Canbus -> Host, 	data length = 0x0A: Car Info 1 						***/
	/*** ComId: 0x13, Canbus -> Host, 	data length = 0x0A: Car Info 2 						***/
	/*** ComId: 0x42, Canbus -> Host, 	data length = 0x10: LR-Radar 						***/
	/*** ComId: 0xF0, Canbus -> Host, 	data length = 0x11: Canbus version 					***/
	/*** ComId: 0x47, Canbus -> Host, 	data length = 0x0E: Driver Assistant System Info 	***/
	
	/*** ComId: 0xD2, Host   -> Canbus, data length = 0x0D: Host Status 					***/
	/*** ComId: 0xE2, Host   -> Canbus, data length = 0x0D: Display 1 						***/
	/*** ComId: 0xE3, Host   -> Canbus, data length = 0x0D: Display 2 						***/
	/*** ComId: 0xEC, Host   -> Canbus, data length = 0x02: Driver Assistant System Setting ***/
	/*** ComId: 0xE4, Host   -> Canbus, data length = 0x13: Navigator Info 					***/
	/*** ComId: 0xE5, Host   -> Canbus, data length = 0x14: Current Position				***/
	
	/*** ComId: 0x72, Canbus -> Host,   data length = 0x0E: Car Basic Info 					***/
	
	//ActiveInfo getActiveInfo();
	//will be removed
	boolean getBluetooth();
	
	boolean isActiveBluetooth();
	
	//LR or FB radar ?
	boolean isActiveRadar();
	
	boolean isActiveKeyIn();
	
	boolean isActivePark();
	
	boolean isActiveRev();
	
	boolean isActiveILL();
	
	boolean isActiveACC();
	
	/**
	 * limit too 256 km/h ?
	 */
	char getSpeed();
	
	char getWheelKey();
	
	char getLight();
	
	char getWheelLeftAngle();
	
	char getWheelRightAngle();
	
	/**
	 * Front-Back-Radar
	 */
	char getRadar(char raderDirection);
	
	/*** ComId: 0x73, Canbus -> Host,   data length = 0x08: AC info 						***/
}
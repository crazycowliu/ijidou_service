package android.os;

import android.os.Person;

interface IHelloService {

	void setVal(int val, char num);
	
	int getVal(char num);
	
	String greet(in Person person);
}
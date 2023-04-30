package com.usman.csudh.util;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class UniqueCounter {
	private static int counterState = 1000;

	public static int nextValue() {
		return counterState++;
	}


}

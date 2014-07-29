package com.vng;

import java.io.*;
import java.math.*;

public class ConvertBigIntToInteger {
	public static void main(String[] args) {
		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"utf-8"));
			bufferedWriter = new BufferedWriter(new FileWriter(args[1]));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String line = "";
		try {
			while ((line = bufferedReader.readLine()) != null) {
				String[] slitStrings = line.split("\\|");
                                System.out.println(line);
				String decimalStr = slitStrings[1];
				BigInteger bigInt = new BigInteger(decimalStr);
				BigInteger binaryBigInteger = new BigInteger("0");
				BigInteger coefficient = new BigInteger("1");
				for (int i = 0; i < 32; i++) {
					BigInteger temp = bigInt.mod(new BigInteger("2"));
					bigInt = bigInt.divide(new BigInteger("2"));
					binaryBigInteger = binaryBigInteger.add(coefficient.multiply(temp));
					coefficient = coefficient.multiply(new BigInteger("10"));
				}
				bufferedWriter.write(line.replace(slitStrings[1], (new BigInteger(binaryBigInteger.toString(), 2)).toString()) + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			bufferedReader.close();
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

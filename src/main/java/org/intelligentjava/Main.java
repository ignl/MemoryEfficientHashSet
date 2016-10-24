package org.intelligentjava;

import java.util.Set;
import java.util.UUID;

import objectexplorer.MemoryMeasurer;

public class Main {

	private static final int SET_SIZE = 1000000;

	public static void main(String[] args) {
		Set<String> javaSet = new java.util.HashSet<>(SET_SIZE);
		Set<String> memoryEfficientSet = new org.intelligentjava.HashSet(SET_SIZE);
		String[] simpleArray = new String[SET_SIZE];
		for (int i = 0; i < SET_SIZE; i++) {
			String randomHexString = UUID.randomUUID().toString().replaceAll("-", "");
			javaSet.add(randomHexString);
			memoryEfficientSet.add(randomHexString);
			simpleArray[i] = randomHexString;
		}
		
		System.out.println(MemoryMeasurer.measureBytes(javaSet));
		System.out.println(MemoryMeasurer.measureBytes(simpleArray));
		System.out.println(MemoryMeasurer.measureBytes(memoryEfficientSet));
	}
	
}

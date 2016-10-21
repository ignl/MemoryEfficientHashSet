package org.intelligentjava;

import java.util.Set;
import java.util.UUID;

import objectexplorer.MemoryMeasurer;

public class Main {

	private static final int SET_SIZE = 1000000;

	public static void main(String[] args) {
//		Set<String> javaSet = new java.util.HashSet<>(SET_SIZE);
//		
//		for (int i = 0; i < SET_SIZE; i++) {
//			String randomHexString = UUID.randomUUID().toString().replaceAll("-", "");
//			javaSet.add(randomHexString);
//		}
//		
//		System.out.println(MemoryMeasurer.measureBytes(javaSet));
		System.out.println(MemoryMeasurer.measureBytes('a'));
		System.out.println(MemoryMeasurer.measureBytes(UUID.randomUUID().toString().replaceAll("-", "")));
		System.out.println(MemoryMeasurer.measureBytes(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}));
	}

}

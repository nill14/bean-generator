package com.github.nill14.generator.c2;

public class Main {

	public static void main(String[] args) {
		String model = "/Papyrus2/model-app.uml";
		String folder = "src/main/java";
		
		GenerateJava.main(new String[]{model, folder});

	}

}

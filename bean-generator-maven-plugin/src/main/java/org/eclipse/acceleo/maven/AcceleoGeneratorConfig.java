
package org.eclipse.acceleo.maven;

import java.util.List;

public class AcceleoGeneratorConfig {

	/**
	 * The classpath entries.
	 */
	private List<GenerationUnit> generationUnits;
	private String outputDirectory;

	public List<GenerationUnit> getGenerationUnits() {
		return generationUnits;
	}

	public void setGenerationUnits(List<GenerationUnit> generationUnits) {
		this.generationUnits = generationUnits;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

}

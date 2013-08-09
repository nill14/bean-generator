
package org.eclipse.acceleo.maven;

import java.util.List;

public class AcceleoGeneratorConfig {

	/**
	 * The classpath entries.
	 */
	private List<GenerationUnit> generationUnits;

	public List<GenerationUnit> getGenerationUnits() {
		return generationUnits;
	}

	public void setGenerationUnits(List<GenerationUnit> generationUnits) {
		this.generationUnits = generationUnits;
	}

}

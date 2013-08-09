package org.eclipse.acceleo.maven;

import java.util.List;

public class GenerationUnit {
	private String model;
	private String module;
	private List<String> templates;
	
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public List<String> getTemplates() {
		return templates;
	}
	public void setTemplates(List<String> templates) {
		this.templates = templates;
	}
	
	
	
}

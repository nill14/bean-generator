/*******************************************************************************
 * Copyright (c) 2008, 2012 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.acceleo.maven;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.acceleo.common.internal.utils.AcceleoPackageRegistry;
import org.eclipse.acceleo.internal.parser.compiler.AcceleoParser;
import org.eclipse.acceleo.internal.parser.compiler.AcceleoProject;
import org.eclipse.acceleo.internal.parser.compiler.AcceleoProjectClasspathEntry;
import org.eclipse.acceleo.internal.parser.compiler.IAcceleoParserURIHandler;
import org.eclipse.acceleo.internal.parser.compiler.IParserListener;
import org.eclipse.acceleo.parser.AcceleoParserProblem;
import org.eclipse.acceleo.parser.AcceleoParserWarning;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.URI;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;



/**
 * The Acceleo Parser MOJO is used to call the Acceleo Parser from Maven.
 * 
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 * @since 3.2
 */
@Mojo(name = "acceleo-compile", defaultPhase = LifecyclePhase.COMPILE, 
		requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class AcceleoParserMojo extends AbstractMojo {

	@Component
	private MavenProject project;

	/**
	 * Indicates if we are compiling the Acceleo modules as binary resources.
	 */
	@Parameter(required = true, defaultValue = "${acceleo-compile.binaryResource}")
	private boolean useBinaryResources;

	/**
	 * The class to use for the uri converter.
	 * 
	 */
	@Parameter(defaultValue = "${acceleo-compile.uriHandler}")
	private String uriHandler;

	/**
	 * The list of packages to register.
	 * 
	 */
	@Parameter(required = true, defaultValue = "${acceleo-compile.packagesToRegister}")
	private List<String> packagesToRegister;

	/**
	 * The Acceleo project that should be built.
	 * 
	 */
	@Parameter(required = true, defaultValue = "${acceleo-compile.acceleoProject}")
	private AcceleoProjectConfig acceleoProject;
	
	/**
	 * The Acceleo generator that should be built.
	 * 
	 */
	@Parameter(required = false)
	private AcceleoGeneratorConfig acceleoGenerator;

	/**
	 * Indicates if we are compiling the Acceleo modules as binary resources.
	 * 
	 */
	@Parameter(required = true, defaultValue = "${acceleo-compile.usePlatformResourcePath}")
	private boolean usePlatformResourcePath = true;

	/**
	 * Indicates if we should fail on errors.
	 * 
	 */
	@Parameter(defaultValue = "${acceleo-compile.failOnError}")
	private boolean failOnError = true;

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		info("Acceleo maven stand alone build...");

		info("Starting packages registration...");

		URLClassLoader newLoader = getPackageClassloader();

		registerPackages(newLoader);

		info("Starting the build sequence for the project '%s'...", this.acceleoProject.getRoot());
		info("Mapping the pom.xml to AcceleoProject...");

		Preconditions.checkNotNull(this.acceleoProject);
		Preconditions.checkNotNull(this.acceleoProject.getRoot());
		Preconditions.checkNotNull(this.acceleoProject.getEntries());
		Preconditions.checkState(this.acceleoProject.getEntries().size() >= 1);

		File root = this.acceleoProject.getRoot();

		AcceleoProject aProject = new AcceleoProject(root);
		mapClasspathEntries(aProject);

		info("Adding jar dependencies...");
		addJarDependencies(aProject, newLoader);

		info("Starting parsing...");
		AcceleoParser parser = startParsing(aProject, newLoader);  

		Set<File> builtFiles = parser.buildAll(new BasicMonitor());

		boolean errorFound = checkErrorsAndWarnings(parser, builtFiles);

		if (errorFound && failOnError) {
			throw new MojoExecutionException("Errors have been found during the build of the generator");
		}

		// Removing everything
		AcceleoPackageRegistry.INSTANCE.clear();
		info("Build completed.");
		
		doGenerate(newLoader);
	}

	private void doGenerate(URLClassLoader newLoader) throws MojoFailureException, MojoExecutionException {
    	File basedir = project.getBasedir();
    	File outputDir = new File(project.getBuild().getOutputDirectory());
    	
    	try {
    		if(acceleoGenerator != null) {
    			for(GenerationUnit unit : acceleoGenerator.getGenerationUnits()) {
    				
    				URI modelURI = URI.createFileURI(new File(basedir, unit.getModel()).toString());
    				ImmutableList<String> templateNames = ImmutableList.copyOf(unit.getTemplates());
    				String moduleName = unit.getModule();
    				
    				File outputFolder = new File(basedir, "src/main/java");
    				
    				List<String> arguments = Lists.newArrayList();
    				
//        log.info("Starting parsing...");
//		AcceleoParser parser = new AcceleoParser(aProject, this.useBinaryResources,
//				this.usePlatformResourcePath);
//		AcceleoParserListener listener = new AcceleoParserListener();
    				
    				
    				GeneratorJob generator = new GeneratorJob(newLoader, moduleName, templateNames);
    				
    				generator.initialize(modelURI, outputFolder, arguments);
//        		generator.initialize(element, outputFolder, arguments);
//           	generator.addPropertiesFile(args[i]);
    				
    				generator.doGenerate(new BasicMonitor());
    				
    			}
    		}
		} catch (IOException e) {
			throw new MojoExecutionException("Generation failed", e);
		}
    	
    	
	}

	private boolean checkErrorsAndWarnings(AcceleoParser parser, Set<File> builtFiles) {
		boolean errorFound = false;
		for (File builtFile : builtFiles) {
			Collection<AcceleoParserProblem> problems = parser.getProblems(builtFile);
			Collection<AcceleoParserWarning> warnings = parser.getWarnings(builtFile);

			if (problems.size() > 0) {
				info("Errors for file '%s': %s", builtFile.getName(), problems);
				errorFound = true;
			}
			if (warnings.size() > 0) {
				info("Warnings for file '%s': %s", builtFile.getName(), warnings);
			}
		}
		return errorFound;
	}

	private void addJarDependencies(AcceleoProject aProject, URLClassLoader newLoader) {
		List<String> jars = this.acceleoProject.getJars();
		if (jars != null) {
			Set<URI> newDependencies = new LinkedHashSet<URI>();
			for (String jar : jars) {
				info("Resolving jar: '" + jar + "'...");
				File jarFile = new File(jar);
				if (jarFile.isFile()) {
					URI uri = URI.createFileURI(jar);
					newDependencies.add(uri);
					info("Found jar for '%s' on the filesystem: '%s'.", jar,  jarFile.getAbsolutePath());
				} else {
					StringTokenizer tok = new StringTokenizer(jar, ":");

					String groupdId = null;
					String artifactId = null;
					String version = null;

					int c = 0;
					while (tok.hasMoreTokens()) {
						String nextToken = tok.nextToken();
						if (c == 0) {
							groupdId = nextToken;
						} else if (c == 1) {
							artifactId = nextToken;
						} else if (c == 2) {
							version = nextToken;
						}

						c++;
					}

					Set<?> artifacts = this.project.getArtifacts();
					for (Object object : artifacts) {
						if (object instanceof Artifact) {
							Artifact artifact = (Artifact)object;
							if (groupdId != null && groupdId.equals(artifact.getGroupId())
									&& artifactId != null && artifactId.equals(artifact.getArtifactId())) {
								if (version != null && version.equals(artifact.getVersion())) {
									File artifactFile = artifact.getFile();
									if (artifactFile != null && artifactFile.exists()) {
										URI uri = URI.createFileURI(artifactFile.getAbsolutePath());
										newDependencies.add(uri);
										info("Found jar for '" + jar + "' on the filesystem: '"
												+ uri.toString() + "'.");
									}
								} else if (version == null) {
									File artifactFile = artifact.getFile();
									if (artifactFile != null && artifactFile.exists()) {
										URI uri = URI.createFileURI(artifactFile.getAbsolutePath());
										newDependencies.add(uri);
										info("Found jar for '" + jar + "' on the filesystem: '"
												+ uri.toString() + "'.");
									}
								}
							}
						}
					}

					List<?> mavenDependencies = this.project.getDependencies();
					for (Object object : mavenDependencies) {
						if (object instanceof Dependency) {
							Dependency dependency = (Dependency)object;
							if (groupdId != null && groupdId.equals(dependency.getGroupId())
									&& artifactId != null && artifactId.equals(dependency.getArtifactId())) {
								if (version != null && version.equals(dependency.getVersion())) {
									String systemPath = dependency.getSystemPath();
									if (systemPath != null && new File(systemPath).exists()) {
										URI uri = URI.createFileURI(systemPath);
										newDependencies.add(uri);
										info("Found jar for '" + jar + "' on the filesystem: '"
												+ uri.toString() + "'.");
									}
								} else if (version == null) {
									String systemPath = dependency.getSystemPath();
									if (systemPath != null && new File(systemPath).exists()) {
										URI uri = URI.createFileURI(systemPath);
										newDependencies.add(uri);
										info("Found jar for '" + jar + "' on the filesystem: '"
												+ uri.toString() + "'.");
									}
								}
							}
						}
					}
				}
			}
			aProject.addDependencies(newDependencies);
		}
	}
	
	private URLClassLoader getPackageClassloader() throws MojoFailureException {
		try {
			LinkedHashSet<?> classpathElements = Sets.newLinkedHashSet();
			classpathElements.addAll(project.getRuntimeClasspathElements());
			classpathElements.addAll(project.getCompileClasspathElements());
			
			URL[] runtimeUrls = FluentIterable.from(classpathElements).transform(new Function<Object, URL>() {

				@Override
				public URL apply(Object input) {
					try {
						String str = (String) input;
						debug("Adding the dependency %s to the classloader for the package resolution", str);
						return new File(str).toURI().toURL();
					} catch (MalformedURLException e) {
						throw new RuntimeException(e);
					}
				}
			}).toArray(URL.class);
			
			return new URLClassLoader(runtimeUrls, Thread.currentThread().getContextClassLoader());
			
		} catch (DependencyResolutionRequiredException e) {
			throw new MojoFailureException("Cannot resolve dependencies", e);
		}
	}
	
	private void registerPackages(URLClassLoader newLoader) throws MojoFailureException {

		try {
			for (String packageToRegister : this.packagesToRegister) {
				Class<?> forName = Class.forName(packageToRegister, true, newLoader);
				Field nsUri = forName.getField("eNS_URI");
				Field eInstance = forName.getField("eINSTANCE");

				String nsURIInvoked = (String) nsUri.get(null);
				Object eInstanceInvoked = eInstance.get(null);
				
				info("Registering package '%s'.",  packageToRegister);
				AcceleoPackageRegistry.INSTANCE.put(nsURIInvoked, eInstanceInvoked);

			}
		} catch (ClassNotFoundException e) {
			throw new MojoFailureException("Cannot register packages", e);
		} catch (IllegalAccessException e) {
			throw new MojoFailureException("Cannot register packages", e);
		} catch (SecurityException e) {
			throw new MojoFailureException("Cannot register packages", e);
		} catch (NoSuchFieldException e) {
			throw new MojoFailureException("Cannot register packages", e);
		} catch (ClassCastException e) {
			throw new MojoFailureException("Cannot register packages", e);
		} 

	}
	
	private Set<AcceleoProjectClasspathEntry> getClasspathEntries(List<Entry> entries, final File root) {
		return Sets.newLinkedHashSet(FluentIterable.from(entries).transform(

			new Function<Entry, AcceleoProjectClasspathEntry>() {

				@Override
				public AcceleoProjectClasspathEntry apply(Entry entry) {
					File inputDirectory = new File(root, entry.getInput());
					File outputDirectory = new File(root, entry.getOutput());

					debug("Input: " + inputDirectory.getAbsolutePath());
					debug("Output: " + outputDirectory.getAbsolutePath());

					return new AcceleoProjectClasspathEntry(inputDirectory, outputDirectory);
				}
		}));
	}
	
	private void mapClasspathEntries(AcceleoProject aProject) {

		aProject.addClasspathEntries(getClasspathEntries(
				this.acceleoProject.getEntries(), this.acceleoProject.getRoot()));

		List<AcceleoProjectConfig> dependencies = this.acceleoProject.getDependencies();
		if (dependencies != null) {
			for (AcceleoProjectConfig dependingAcceleoProject : dependencies) {
				File dependingProjectRoot = dependingAcceleoProject.getRoot();
				Preconditions.checkNotNull(dependingProjectRoot);

				AcceleoProject aDependingProject = new AcceleoProject(dependingProjectRoot);

				aDependingProject.addClasspathEntries(getClasspathEntries(dependingAcceleoProject.getEntries(), dependingProjectRoot));
				aProject.addProjectDependencies(ImmutableSet.of(aDependingProject));
			}
		}
		
	}
	
	private AcceleoParser startParsing(AcceleoProject aProject, URLClassLoader newLoader) {
		AcceleoParser parser = new AcceleoParser(aProject, this.useBinaryResources,
				this.usePlatformResourcePath);
		AcceleoParserListener listener = new AcceleoParserListener();
		parser.addListeners(listener);

		// Load and plug the uri resolver
		if (this.uriHandler != null && newLoader != null) {
			try {
				Class<?> forName = Class.forName(this.uriHandler, true, newLoader);
				Object newInstance = forName.newInstance();
				if (newInstance instanceof IAcceleoParserURIHandler) {
					IAcceleoParserURIHandler resolver = (IAcceleoParserURIHandler)newInstance;
					parser.setURIHandler(resolver);
				}
			} catch (ClassNotFoundException e) {
				getLog().error(e);
			} catch (InstantiationException e) {
				getLog().error(e);
			} catch (IllegalAccessException e) {
				getLog().error(e);
			}
		}
		return parser;
	}
	
	private void debug(String format, Object... args) {
		getLog().debug(String.format(format, args));
	}
	
	private void info(String format, Object... args) {
		getLog().info(String.format(format, args));
	}

	/**
	 * The listener used to log message for maven.
	 * 
	 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
	 * @since 3.2
	 */
	private class AcceleoParserListener implements IParserListener {

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.eclipse.acceleo.internal.parser.compiler.IParserListener#endBuild(java.io.File)
		 */
		public void endBuild(File file) {
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.eclipse.acceleo.internal.parser.compiler.IParserListener#fileSaved(java.io.File)
		 */
		public void fileSaved(File file) {
			info("Saving ouput file for '%s'.", file.getAbsolutePath());
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.eclipse.acceleo.internal.parser.compiler.IParserListener#loadDependency(java.io.File)
		 */
		public void loadDependency(File file) {
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.eclipse.acceleo.internal.parser.compiler.IParserListener#loadDependency(org.eclipse.emf.common.util.URI)
		 */
		public void loadDependency(URI uri) {
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see org.eclipse.acceleo.internal.parser.compiler.IParserListener#startBuild(java.io.File)
		 */
		public void startBuild(File file) {
		}
	}
}

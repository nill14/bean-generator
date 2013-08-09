package org.eclipse.acceleo.maven;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.acceleo.engine.generation.strategy.DefaultStrategy;
import org.eclipse.acceleo.engine.generation.strategy.IAcceleoGenerationStrategy;
import org.eclipse.acceleo.engine.service.AbstractAcceleoGenerator;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class GeneratorJob extends AbstractAcceleoGenerator {

	private ClassLoader classLoader;

	private String moduleName;

	private ImmutableList<String> templateNames;



/**
	 * Allows the public constructor to be used. Note that a generator created this way cannot be used to
	 * launch generations before one of {@link #initialize(EObject, File, List)} or
	 * {@link #initialize(URI, File, List)} is called.
	 * <p>
	 * The main reason for this constructor is to allow clients of this generation to call it from another
	 * Java file, as it allows for the retrieval of {@link #getProperties()} and
	 * {@link #getGenerationListeners()}.
	 * </p>
	 * 
	 * @generated
	 */
	public GeneratorJob(ClassLoader classLoader, String moduleName, ImmutableList<String> templateNames) {
        this.classLoader = classLoader;
		this.moduleName = moduleName;
        this.templateNames = templateNames;
        
    }



	/**
	 * Updates the registry used for looking up a package based namespace, in the resource set.
	 * 
	 * @param resourceSet
	 *            is the resource set
	 */
	@Override
	public void registerPackages(ResourceSet resourceSet) {
        super.registerPackages(resourceSet);

        resourceSet.getPackageRegistry().put(UMLPackage.eINSTANCE.getNsURI(), UMLPackage.eINSTANCE);
    }

	/**
	 * Updates the registry used for looking up resources factory in the given resource set.
	 * 
	 * @param resourceSet
	 *            The resource set that is to be updated.
	 * @generated NOT
	 */
	@Override
	public void registerResourceFactories(ResourceSet resourceSet) {
        super.registerResourceFactories(resourceSet);
        
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
        	.put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
    }

	/**
	 * The main method.
	 * 
	 * @param args
	 *            are the arguments
	 * @generated
	 */
	public static void main(String[] args) {
        try {
        	File basedir = new File("C:/dev/git/bean-generator/bean-generator-test");
        	
        	String moduleName = "/org/eclipse/acceleo/module/example/uml2java/helios/generateJava";
        	ImmutableList<String> templateNames = ImmutableList.of("generateClass", "generateInterface");
        	
            URI modelURI = URI.createFileURI(new File(basedir, "model/example.uml").toString());
            File outputFolder = new File(basedir, "src/main/java");
            
            List<String> arguments = Lists.newArrayList();
            
//            log.info("Starting parsing...");
//    		AcceleoParser parser = new AcceleoParser(aProject, this.useBinaryResources,
//    				this.usePlatformResourcePath);
//    		AcceleoParserListener listener = new AcceleoParserListener();
            
            ClassLoader loader = GeneratorJob.class.getClassLoader();
            GeneratorJob generator = new GeneratorJob(loader, moduleName, templateNames);
            
            generator.initialize(modelURI, outputFolder, arguments);
//            generator.initialize(element, outputFolder, arguments);
//                generator.addPropertiesFile(args[i]);
            
            generator.doGenerate(new BasicMonitor());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	


	/**
	 * If you need to change the way files are generated, this is your entry point.
	 * <p>
	 * The default is {@link org.eclipse.acceleo.engine.generation.strategy.DefaultStrategy}; it generates
	 * files on the fly. If you only need to preview the results, return a new
	 * {@link org.eclipse.acceleo.engine.generation.strategy.PreviewStrategy}. Both of these aren't aware of
	 * the running Eclipse and can be used standalone.
	 * </p>
	 * <p>
	 * If you need the file generation to be aware of the workspace (A typical example is when you wanna
	 * override files that are under clear case or any other VCS that could forbid the overriding), then
	 * return a new {@link org.eclipse.acceleo.engine.generation.strategy.WorkspaceAwareStrategy}.
	 * <b>Note</b>, however, that this <b>cannot</b> be used standalone.
	 * </p>
	 * <p>
	 * All three of these default strategies support merging through JMerge.
	 * </p>
	 */
	public IAcceleoGenerationStrategy getGenerationStrategy() {
		return new DefaultStrategy();
	}

	/**
	 * This will be called in order to find and load the module that will be launched through this launcher.
	 * We expect this name not to contain file extension, and the module to be located beside the launcher.
	 * 
	 * @return The name of the module that is to be launched.
	 * @generated
	 */
	@Override
	public String getModuleName() {
        return moduleName;
    }

	/**
	 * If the module(s) called by this launcher require properties files, return their qualified path from
	 * here.Take note that the first added properties files will take precedence over subsequent ones if they
	 * contain conflicting keys.
	 * <p>
	 * Properties need to be in source folders, the path that we expect to get as a result of this call are of
	 * the form &lt;package>.&lt;properties file name without extension>. For example, if you have a file
	 * named "messages.properties" in package "org.eclipse.acceleo.sample", the path that needs be returned by
	 * a call to {@link #getProperties()} is "org.eclipse.acceleo.sample.messages".
	 * </p>
	 * 
	 * @return The list of properties file we need to add to the generation context.
	 * @see java.util.ResourceBundle#getBundle(String)
	 * @generated
	 */
	@Override
	public List<String> getProperties() {
        return propertiesFiles;
    }

	/**
     * Adds a properties file in the list of properties files.
     * 
     * @param propertiesFile
     *            The properties file to add.
     * @generated
     * @since 3.1
     */
    @Override
    public void addPropertiesFile(String propertiesFile) {
        this.propertiesFiles.add(propertiesFile);
    }

  /**
	 * This will be used to get the list of templates that are to be launched by this launcher.
	 * 
	 * @return The list of templates to call on the module {@link #getModuleName()}.
	 * @generated
	 */
	@Override
	public String[] getTemplateNames() {
        return templateNames.toArray(new String[0]);
    }
	
	@Override
	protected URL findModuleURL(String moduleName) {
		return classLoader.getResource(moduleName);
	}

}

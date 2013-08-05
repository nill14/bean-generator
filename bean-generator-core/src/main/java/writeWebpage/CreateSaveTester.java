//package writeWebpage;
//
//import java.io.IOException;
//import java.util.Collections;
//import java.util.Map;
//
//import org.eclipse.emf.common.util.URI;
//import org.eclipse.emf.ecore.EClass;
//import org.eclipse.emf.ecore.resource.Resource;
//import org.eclipse.emf.ecore.resource.ResourceSet;
//import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
//import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
//import org.eclipse.uml2.uml.UMLPackage;
//
//public class CreateSaveTester {
//
//  
///** * @param args */
//
//  public static void main(String[] args) {
//    // Initialize the model
////	  org.eclipse.uml2.uml.UMLPackage
//    UMLPackage.eINSTANCE.eClass();
//    // Retrieve the default factory singleton
//    UMLPackage factory = UMLPackage.eINSTANCE;
//
//    // Create the content of the model via this program
//    EClass eClass = factory.eClass();
//    EPackage
//    Webpage page = factory.ePcreateWebpage();
//    page.setName("index");
//    page.setDescription("Main webpage");
//    page.setKeywords("Eclipse, EMF");
//    page.setTitle("Eclipse EMF");
//    myWeb.getPages().add(page);
//
//    // As of here we preparing to save the model content
//
//    // Register the XMI resource factory for the .website extension
//
//    Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
//    Map<String, Object> m = reg.getExtensionToFactoryMap();
//    m.put("website", new XMIResourceFactoryImpl());
//
//    // Obtain a new resource set
//    ResourceSet resSet = new ResourceSetImpl();
//
//    // Create a resource
//    Resource resource = resSet.createResource(URI
//        .createURI("website/My2.website"));
//    // Get the first model element and cast it to the right type, in my
//    // example everything is hierarchical included in this first node
//    resource.getContents().add(myWeb);
//
//    // Now save the content.
//    try {
//      resource.save(Collections.EMPTY_MAP);
//    } catch (IOException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//  }
//} 
package com.github.nill14.generator.services;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Stereotype;

import com.github.nill14.generator.services.guava.CommentTextFunction;
import com.github.nill14.generator.services.guava.StereotypeNamePredicate;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

public class UML2Services {

	public boolean hasStereotype(Class clazz, String stereotypeName) {
		List<Stereotype> stereotypes = clazz.getAppliedStereotypes();
		return FluentIterable.from(stereotypes)
				.anyMatch(new StereotypeNamePredicate(stereotypeName));
	}
	
	public boolean hasComment(Class clazz) {
		EList<Comment> comments = clazz.getOwnedComments();
		return !comments.isEmpty();
	}

	public String getComment(Class clazz) {
		EList<Comment> comments = clazz.getOwnedComments();
		FluentIterable<String> parts = FluentIterable.from(comments)
				.transform(new CommentTextFunction());
		
		return Joiner.on("\n *").join(parts);
	}
	
//	[query public classFileName(aClass: Class): String =
//			aClass.name.toUpperFirst().concat('.java')/] 
//
//		[query public qualifiedName(aClass: Class): String =
//			aClass.containingPackages().name->sep('.')->including('.')->including(aClass.name)->toString()/]
//
//		[query public containingPackages(aClass: Class) : Sequence(Package) = 
//			aClass.ancestors(Package)->reject(oclIsKindOf(Model))->reverse() /]
	
	public String getPackageName(Class clazz) {
		return clazz.getPackage().getName();
		
	}

	public String getJavaFileName(Class clazz) {
		ImmutableList<String> parts = ImmutableList.of(getPackageName(clazz), clazz.getName(), "java");
		return Joiner.on(".").join(parts);
	}
	
}

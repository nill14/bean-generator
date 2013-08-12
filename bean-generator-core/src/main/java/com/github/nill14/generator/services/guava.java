package com.github.nill14.generator.services;

import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Stereotype;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class guava {

	public static class StereotypeNamePredicate implements Predicate<Stereotype> {

		private final String stereotypeName;

		public StereotypeNamePredicate(String stereotypeName) {
			this.stereotypeName = stereotypeName;
		}

		@Override
		public boolean apply(Stereotype stereotype) {
			return stereotypeName.equals(stereotype.getName());
		}
	}
	
	public static class CommentTextFunction implements Function<Comment, String> {

		@Override
		public String apply(Comment comment) {
			return comment.getBody();
		}
		
	}
}

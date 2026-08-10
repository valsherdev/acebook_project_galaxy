package com.makersacademy.acebook.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class PostTest {

	private User user = new User("testuser");
	private Post post = new Post("hello", user);

	@Test
	public void postHasContent() {
		assertThat(post.getContent(), containsString("hello"));
	}

	@Test
	public void postHasAuthor() {
		assertThat(post.getUser().getUsername(), containsString("testuser"));
	}
}

package com.makersacademy.acebook.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class PostTest {

	private User user = new User("testuser");
	private Post post = new Post("hello", user, null);

	@Test
	public void postHasContent() {
		assertThat(post.getContent(), containsString("hello"));
	}

	@Test
	public void postHasAuthor() {
		assertThat(post.getUser().getUsername(), containsString("testuser"));
	}

	@Test
	public void testNoArgsAndSetters() {
		Post post = new Post();
		User user = new User("testuser");
		LocalDateTime now = LocalDateTime.now();

		post.setId(1L);
		post.setContent("New Content");
		post.setUser(user);
		post.setImages("image1.jpg,image2.jpg");
		post.setCreatedAt(now);

		assertThat(post.getId(), is(1L));
		assertThat(post.getContent(), is("New Content"));
		assertThat(post.getUser().getUsername(), is("testuser"));
		assertThat(post.getImages(), is("image1.jpg,image2.jpg"));
		assertThat(post.getCreatedAt(), is(now));
	}

	@Test
	public void testAllArgsAndGetters() {
		User user = new User("user1");
		Post post = new Post("Hello World", user, "image1.jpg");

		assertThat(post.getContent(), is("Hello World"));
		assertThat(post.getUser(), is(user));
		assertThat(post.getUser().getUsername(), is("user1"));
		assertThat(post.getImages(), is("image1.jpg"));
		assertThat(post.getCreatedAt(), notNullValue());
	}

	@Test
	public void testPostWithNullValues() {
		Post post = new Post(null, null, null);

		assertThat(post.getContent(), nullValue());
		assertThat(post.getUser(), nullValue());
		assertThat(post.getImages(), nullValue());
	}
}


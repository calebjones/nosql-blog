package org.nosql.blog.mongo;

import org.junit.runner.RunWith;
import org.nosql.blog.dao.BlogDaoIT;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:mongo-IT-config.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class MongoBlogDaoIT extends BlogDaoIT {

	@Override
	public void before() {
		// no-op - handled by spring
	}

	@Override
	public void after() {
		// no-op - handled by dirtying the context
	}
	
}

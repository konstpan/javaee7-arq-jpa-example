package com.konstpan.examples.arq.jpa.model;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JPAMapTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create(JavaArchive.class).addPackage(Employee.class.getPackage())
				.addAsManifestResource("test-persistence.xml", "persistence.xml")
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	private static final String[] EMPLOYEES = { "John", "George", "Oscar" };

	@PersistenceContext
	EntityManager em;

	@Inject
	UserTransaction utx;

	@Before
	public void setUp() throws Exception {
		insertData();
		utx.begin();
		em.joinTransaction();
	}

	@After
	public void commitTransaction() throws Exception {
		utx.commit();
	}

	private void insertData() throws Exception {
		utx.begin();
		for (String firstName : EMPLOYEES) {
			Employee emp = new Employee();
			emp.setFirstName(firstName);
			em.persist(emp);
		}
		utx.commit();
	}

	@Test
	public void shouldFindEmployee() {
		// given
		TypedQuery<Employee> query = em.createQuery("SELECT e FROM Employee e WHERE e.firstName = :firstName",
				Employee.class);
		query.setParameter("firstName", "John");

		// when
		Employee emp = query.getSingleResult();

		// then
		assertEquals("John", emp.getFirstName());
	}

}

package com.konstpan.examples.arq.jpa.model;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
	EntityManagerFactory entityManagerFactory;
	
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
	public void shouldFindEmployeeByJPQL() {
		// given
		TypedQuery<Employee> query = em.createQuery("SELECT e FROM Employee e WHERE e.firstName = :firstName",
				Employee.class);
		query.setParameter("firstName", "John");

		// when
		Employee emp = query.getSingleResult();

		// then
		assertEquals("John", emp.getFirstName());
	}
	
	@Test
	public void shouldFindEmployeeByCriteria() {
		// given
		CriteriaBuilder builder = entityManagerFactory.getCriteriaBuilder();
		CriteriaQuery<Employee> criteria = builder.createQuery(Employee.class);
		Root<Employee> root = criteria.from(Employee.class);
		criteria.select(root);
		criteria.where(builder.equal(root.get("firstName"), "George"));
		
		// when
		Employee emp = em.createQuery(criteria).getSingleResult();
		
		// then
		assertEquals("George", emp.getFirstName());
	}

}

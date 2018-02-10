package com.konstpan.examples.arq.jpa.model;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;
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
import org.junit.Ignore;
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
		TypedQuery<Employee> query = em.createQuery("SELECT e FROM Employee e", Employee.class);

		// code below is required because we cannot bulk delete with sql an
		// ElementCollection!!!
		for (Employee emp : query.getResultList()) {
			em.remove(emp);
		}

		utx.commit();
	}

	private void insertData() throws Exception {
		utx.begin();
		for (String firstName : EMPLOYEES) {
			Employee emp = new Employee();
			emp.setFirstName(firstName);

			emp.getMetaData().put("key1", "value1");
			emp.getMetaData().put("key2", "value2");

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
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Employee> criteria = builder.createQuery(Employee.class);
		Root<Employee> root = criteria.from(Employee.class);
		criteria.select(root);
		criteria.where(builder.equal(root.get("firstName"), "George"));

		// when
		Employee emp = em.createQuery(criteria).getSingleResult();

		// then
		assertEquals("George", emp.getFirstName());
	}
	
	@Test
	@Ignore
	public void shouldFindEmployeeByCriteriaAndMetadata() {
		// given
		Map<String, String> elementToSearchFor = new HashMap<>();
		elementToSearchFor.put("key2", "value2");
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Employee> criteria = builder.createQuery(Employee.class);
		Root<Employee> root = criteria.from(Employee.class);
		criteria.select(root);
		criteria.where(builder.isMember(elementToSearchFor, root.get("metaData")));
		
		// when
		List<Employee> emps = em.createQuery(criteria).getResultList();

		// then
		assertEquals(3, emps.size());
	}

}

package com.mayab.quality.Integration;

import java.io.File;

import java.io.FileInputStream;
import java.util.List;

import org.dbunit.Assertion;
import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mayab.quality.integrationtest.dao.IDAOUser;
import com.mayab.quality.integrationtest.dao.UserMysqlDAO;
import com.mayab.quality.integrationtest.model.User;
import com.mayab.quality.integrationtest.service.UserService;

class ServiceTest5 extends DBTestCase {
	
	private IDAOUser dao;
	private UserService service;
	
	public ServiceTest5() {
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS,"com.mysql.cj.jdbc.Driver");
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL,"jdbc:mysql://localhost:3306/calidad");
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME,"root");
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD,"123456");	
	}
	
	@BeforeEach
	void setup() throws Exception {
		dao = new UserMysqlDAO();
		service = new UserService(dao);
		IDatabaseConnection connection = getConnection(); 
		if (connection == null) {
	        fail("Failed to establish a connection to the database.");
	    } else {
	        System.out.println("Connection established successfully.");
	    }
		
		try {
			DatabaseOperation.TRUNCATE_TABLE.execute(connection,getDataSet());
			DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
			
		} catch(Exception e) {
			fail("Error in setup: "+ e.getMessage()); 
		} finally {
			connection.close(); 
		}
	}
	
	protected IDataSet getDataSet() throws Exception
    {
        return new FlatXmlDataSetBuilder().build(new FileInputStream("src/resources/initDB.xml"));
    }

	
	@Test
	public void testFindAllUsers() {
	    service.createUser("Juan", "correo1@correo.com", "789456123");
	    service.createUser("Gerardo", "correo2@correo.com", "789456123");
	    service.createUser("Rodrigo", "correo3@correo.com", "789456123");

	    try {
	        List<User> users = service.findAllUsers();
	        assertNotNull(users);
	        assertEquals(3, users.size());

	        IDatabaseConnection conn = getConnection();
	        conn.getConfig().setProperty(DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES, true);
	        IDataSet databaseDataSet = conn.createDataSet();
	        ITable actualTable = databaseDataSet.getTable("usuarios");

	        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/resources/findall.xml"));
	        ITable expectedTable = expectedDataSet.getTable("usuarios");

	        Assertion.assertEquals(expectedTable, actualTable);

	    } catch (Exception e) {
	        fail("Error in findAll test: " + e.getMessage());
	    }
	}



}
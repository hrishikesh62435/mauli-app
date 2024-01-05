package com.mauli.app.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mauli.app.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

	@Query("SELECT e from Employee e where e.employeeBranch = :employeeBranch")
	public List<Employee> getEmployeeByEmployeeBranch(@Param("employeeBranch") String employeeBranch);
	
	@Transactional
	@Modifying
	@Query("update Employee set employeePhoto = :imageName where employeeId = :employeeId")
	public void uploadProfilePicture(int employeeId, String imageName);
}

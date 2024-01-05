package com.mauli.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mauli.app.model.Transactions;

public interface TransactionRepository extends JpaRepository<Transactions, Integer> {
	
	@Query("SELECT e from Transactions e where e.employeeBranch = :employeeBranch and e.month = :month and e.year = :year")
	public List<Transactions> getTransactionsByEmployeeBranchAndMonthAndYear(@Param("employeeBranch") String employeeBranch,
			@Param("month") String month, @Param("year") String year);
	
	@Query("SELECT e from Transactions e where e.employeeBranch = :employeeBranch and e.employeeId = :employeeId and e.month = :month and e.year = :year")
	public List<Transactions> getTransactionsByEmployeeBranchAndEmployeeIdAndMonthAndYear(@Param("employeeBranch") String employeeBranch,
			@Param("employeeId") int employeeId, @Param("month") String month, @Param("year") String year);
	
	@Query(value = "select e.* from tbl_transactions e where e.employee_branch = :employeeBranch and DATE_FORMAT(STR_TO_DATE(e.tran_date, '%d-%m-%Y'),  '%Y-%m-%d') "
			+ "	BETWEEN DATE_FORMAT(STR_TO_DATE(:month, '%d-%m-%Y'),  '%Y-%m-%d') AND DATE_FORMAT(STR_TO_DATE(:year, '%d-%m-%Y'),  '%Y-%m-%d')", nativeQuery = true)
	public List<Transactions> getBranchTransactionsByEmployeeBranchAndMonthAndYear(@Param("employeeBranch") String employeeBranch,
			 @Param("month") String month, @Param("year") String year);
	
	@Query(value = "select e.* from tbl_transactions e where e.employee_branch = :employeeBranch and e.employee_id = :employeeId and DATE_FORMAT(STR_TO_DATE(e.tran_date, '%d-%m-%Y'),  '%Y-%m-%d') "
			+ "	BETWEEN DATE_FORMAT(STR_TO_DATE(:month, '%d-%m-%Y'),  '%Y-%m-%d') AND DATE_FORMAT(STR_TO_DATE(:year, '%d-%m-%Y'),  '%Y-%m-%d')", nativeQuery = true)
	public List<Transactions> getEmployeeTransactionsByEmployeeBranchAndEmployeeIdAndMonthAndYear(@Param("employeeBranch") String employeeBranch,
			@Param("employeeId") int employeeId, @Param("month") String month, @Param("year") String year);
}
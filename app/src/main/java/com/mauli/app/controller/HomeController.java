package com.mauli.app.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mauli.app.config.DateTimeUtil;
import com.mauli.app.model.ActiveLinkMenu;
import com.mauli.app.model.Branch;
import com.mauli.app.model.Employee;
import com.mauli.app.model.Transactions;
import com.mauli.app.model.User;
import com.mauli.app.repository.BranchRepository;
import com.mauli.app.repository.EmployeeRepository;
import com.mauli.app.repository.TransactionRepository;
import com.mauli.app.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class HomeController {

	 public HomeController() {
	
	 }
	
	 private final String EMPLOYEE_UPLOAD_DIR     = "D:\\PICTURES\\";
	 private final String SERVER_URL			  = "http://localhost/img/"; //"http://3.111.116.152/img/";
	 private final String EMPLOYEE_DEFAULT_IMG	  = "user.png";

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BranchRepository branchRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private TransactionRepository transRepository;
	
	Logger logger = LoggerFactory.getLogger(HomeController.class);

	
	@GetMapping("/")
	public String stsexam(Model model) {

		return "redirect:/login";
	}

	@GetMapping("/login")
	public String login(Model model) {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "login";
		}

		return "login";
	}
	
	@GetMapping("/dashboard")
	public String dashboard2(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal) {
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		menu.setDashboardLink("active");			
		model.addAttribute("menu", menu);
		model.addAttribute("filePath", SERVER_URL);
		return "dashboard2";
	}	

	@GetMapping("/add-user")
	public String addUser(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal) {
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
	
		menu.setUserLink("active");
		menu.setAddUserLink("active");
		menu.setUserMenuOpen("menu-open");
		model.addAttribute("menu", menu);
		model.addAttribute("filePath", SERVER_URL);
		return "add-user";
	}
	
	@PostMapping("/save-user")
    public String saveUser(@ModelAttribute ActiveLinkMenu menu, @RequestParam("userPhoto") MultipartFile userPhoto, @ModelAttribute("user") User user, 
    		BindingResult result, Model model, Principal principal, RedirectAttributes redirectAttrs) {
				
		String pwd = user.getPassword();
		
		String fileName1 = EMPLOYEE_DEFAULT_IMG;
		   
	     if(userPhoto.getSize() != 0 && user.getId() == 0) {
	    	     String passport  = userPhoto.getOriginalFilename();
			     long   timestmp  = new Timestamp(System.currentTimeMillis()).getTime();
	  		     
			      fileName1 = "dp"+timestmp+passport.substring(passport.lastIndexOf("."));
			      // save the file on the local file system
		          File file1 = new  File(EMPLOYEE_UPLOAD_DIR+fileName1); 
				  try {  FileOutputStream fos = new  FileOutputStream(file1); fos.write(userPhoto.getBytes()); fos.close(); } catch (IOException e) {  e.printStackTrace(); }
	              
	     }
		
		try {
			String encryptPwd = passwordEncoder.encode(pwd);
			user.setPassword(encryptPwd);		
			user.setEnabled(true);
			user.setUserPasswordRead(pwd);
			user.setUserPhoto(fileName1);
			userRepository.save(user);
			
			redirectAttrs.addFlashAttribute("messageSuccess", "Success.");
			return "redirect:/view-users";
		} catch (Exception e) {
			// TODO: handle exception
			
			redirectAttrs.addFlashAttribute("messageError", "Mobile number already registered.");
			return "redirect:/add-user";
		}		
	}
	
	@PostMapping("/update-user")
    public String updateUser(@ModelAttribute ActiveLinkMenu menu,@ModelAttribute("user") User user, 
    		BindingResult result, Model model, Principal principal, RedirectAttributes redirectAttrs) {
		
		String pwd = user.getPassword();
		try {
			String encryptPwd = passwordEncoder.encode(pwd);
			user.setPassword(encryptPwd);
			user.setUserPasswordRead(pwd);			
			userRepository.save(user);
			
			redirectAttrs.addFlashAttribute("messageSuccess", "Success.");
			return "redirect:/view-users";
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("update-user : " + e.toString());
			redirectAttrs.addFlashAttribute("messageError", "Mobile number already registered.");
			return "redirect:/add-user";
		}		
	}
	
	@GetMapping("/view-users")
	public String viewUsers(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal) {
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		model.addAttribute("usersList", userRepository.findAll());
		menu.setUserLink("active");
		menu.setViewUserLink("active");
		menu.setUserMenuOpen("menu-open");
		model.addAttribute("menu", menu);
		model.addAttribute("filePath", SERVER_URL);
		return "view-users";
	}
	
	 @GetMapping("/edit-user/{id}") 
     public String editUser(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal, @PathVariable int id) {
		 
		    String userName = principal.getName();
			User user = userRepository.getUserByUserName(userName);
			
			User usr = userRepository.getById(id);
			
			model.addAttribute("user", user);
			model.addAttribute("usr", usr);
			menu.setUserLink("active");
			menu.setAddUserLink("active");
			menu.setUserMenuOpen("menu-open");
			model.addAttribute("menu", menu);
			model.addAttribute("filePath", SERVER_URL);
			return "edit-user";
	 }
	
	 
	 @GetMapping("/delete-user/{id}/{enabled}") 
     public String editUser(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal, 
    		 @PathVariable int id, @PathVariable boolean enabled, RedirectAttributes redirectAttrs) {
		 
		 
			userRepository.updateUserStatus(enabled, id);
			if(enabled == false)
			   redirectAttrs.addFlashAttribute("messageError", "Account Deactivated.");
			else
			   redirectAttrs.addFlashAttribute("messageSuccess", "Account Activated.");
			
			return "redirect:/view-users";
	 }
	 
	 
	@GetMapping("/add-branch")
	public String addBranch(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal) {
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		menu.setBranchLink("active");
		menu.setAddBranchLink("active");
		menu.setBranchMenuOpen("menu-open");
		model.addAttribute("menu", menu);
		model.addAttribute("filePath", SERVER_URL);
		return "add-branch";
	}
	
	@PostMapping("/save-branch")
    public String saveBranch(@ModelAttribute ActiveLinkMenu menu, @ModelAttribute("branch") Branch branch, 
    		BindingResult result, Model model, Principal principal, RedirectAttributes redirectAttrs) {
				
			 String userName = principal.getName();
			 User user = userRepository.getUserByUserName(userName);
		     int userId = user.getId();
		
		     branch.setUserId(userId);
		     branch.setCreatedDatetime(DateTimeUtil.getSysDateTime());
		     
		     branchRepository.save(branch);
			
			 redirectAttrs.addFlashAttribute("messageSuccess", "Success.");
			 return "redirect:/view-branches";			
	}
	
	@GetMapping("/edit-branch/{branchId}") 
    public String editBranch(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal, @PathVariable int branchId) {
		 
		    String userName = principal.getName();
			User user = userRepository.getUserByUserName(userName);
			
			Branch branch = branchRepository.getById(branchId);
			
			model.addAttribute("user", user);
			model.addAttribute("branch", branch);
			menu.setBranchLink("active");
			menu.setAddBranchLink("active");
			menu.setBranchMenuOpen("menu-open");
			model.addAttribute("menu", menu);
			model.addAttribute("filePath", SERVER_URL);
			return "edit-branch";
	 }
	
	@GetMapping("/view-branches")
	public String viewBranches(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal) {
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		model.addAttribute("branchList", branchRepository.findAll());
		menu.setBranchLink("active");
		menu.setViewBranchLink("active");
		menu.setBranchMenuOpen("menu-open");
		model.addAttribute("menu", menu);
		model.addAttribute("filePath", SERVER_URL);
		return "view-branches";
	}	
	
	@GetMapping("/add-employee")
	public String addEmployee(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal) {
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		model.addAttribute("branches", branchRepository.findAll());
		menu.setEmployeeLink("active");
		menu.setAddEmployeeLink("active");
		menu.setEmployeeMenuOpen("menu-open");
		model.addAttribute("menu", menu);
		model.addAttribute("filePath", SERVER_URL);
		return "add-employee";
	}
	
	@PostMapping("/save-employee")
    public String saveEmployee(@ModelAttribute ActiveLinkMenu menu, @RequestParam("employeePhoto") MultipartFile employeePhoto, @ModelAttribute("employee") Employee employee, 
    		BindingResult result, Model model, Principal principal, RedirectAttributes redirectAttrs) {
				
			 String userName = principal.getName();
			 User user = userRepository.getUserByUserName(userName);
		     int userId = user.getId();
		     
		     String fileName1 = EMPLOYEE_DEFAULT_IMG;
		   
		     if(employeePhoto.getSize() != 0 && employee.getEmployeeId() == 0) {
		    	     String passport  = employeePhoto.getOriginalFilename();
				     long   timestmp  = new Timestamp(System.currentTimeMillis()).getTime();
		  		     
				      fileName1 = "dp"+timestmp+passport.substring(passport.lastIndexOf("."));
				      // save the file on the local file system
			          File file1 = new  File(EMPLOYEE_UPLOAD_DIR+fileName1); 
					  try {  FileOutputStream fos = new  FileOutputStream(file1); fos.write(employeePhoto.getBytes()); fos.close(); } catch (IOException e) {  e.printStackTrace(); }
		              
		     }
		   
		     
		     if(employee.getEmployeeId() == 0) {
		    	 long count = employeeRepository.count()+1;
			     String empCode = "";
			     if(count <= 9) {
			    	 empCode = "MMCCS00"+count;
			     }else if(count >= 10 && count <= 99) {
			    	 empCode = "MMCCS0"+count;
			     }else if(count >= 100) {
			    	 empCode = "MMCCS"+count;
			     }		     
			     employee.setEmployeeCode(empCode);
		     }		    
		     
		     employee.setEmployeePhoto(fileName1);
		     employee.setUserId(userId);
		     employee.setCreatedDatetime(DateTimeUtil.getSysDateTime());
		     
		     employeeRepository.save(employee);
			
			 redirectAttrs.addFlashAttribute("messageSuccess", "Success.");
			 return "redirect:/view-employee";			
	}
	
	@PostMapping("/update-employee")
    public String saveEmployee(@ModelAttribute ActiveLinkMenu menu, @ModelAttribute("employee") Employee employee, 
    		BindingResult result, Model model, Principal principal, RedirectAttributes redirectAttrs) {
				
			 String userName = principal.getName();
			 User user = userRepository.getUserByUserName(userName);
		     int userId = user.getId();
		       
		     employee.setUserId(userId);
		     employee.setCreatedDatetime(DateTimeUtil.getSysDateTime());
		     
		     employeeRepository.save(employee);
			
			 redirectAttrs.addFlashAttribute("messageSuccess", "Success.");
			 return "redirect:/view-employee";			
	}
	
	@GetMapping("/view-employee")
	public String viewEmployee(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal) {
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		model.addAttribute("employeeList", employeeRepository.findAll());
		menu.setEmployeeLink("active");
		menu.setViewEmployeeLink("active");
		menu.setEmployeeMenuOpen("menu-open");
		model.addAttribute("menu", menu);
		model.addAttribute("filePath", SERVER_URL);
		return "view-employee";
	}
	
	@GetMapping("/edit-employee/{employeeId}") 
    public String editEmployee(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal, @PathVariable int employeeId) {
		 
		    String userName = principal.getName();
			User user = userRepository.getUserByUserName(userName);
			
			Employee emp = employeeRepository.getById(employeeId);
			
			model.addAttribute("filePath", SERVER_URL);
			model.addAttribute("user", user);
			model.addAttribute("employee", emp);
			model.addAttribute("branches", branchRepository.findAll());
			menu.setEmployeeLink("active");
			menu.setAddEmployeeLink("active");
			menu.setEmployeeMenuOpen("menu-open");
			model.addAttribute("menu", menu);
			
			return "edit-employee";
	 }
	
	@GetMapping("/view-profile/{employeeId}") 
    public String viewProfile(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal, @PathVariable int employeeId) {
		 
		    String userName = principal.getName();
			User user = userRepository.getUserByUserName(userName);
			
			Employee emp = employeeRepository.getById(employeeId);
			
			model.addAttribute("filePath", SERVER_URL);
			model.addAttribute("user", user);
			model.addAttribute("employee", emp);
			menu.setEmployeeLink("active");
			menu.setViewEmployeeLink("active");
			menu.setEmployeeMenuOpen("menu-open");
			model.addAttribute("menu", menu);
			model.addAttribute("filePath", SERVER_URL);
			return "view-profile";
	 }
	
	@GetMapping("/add-transaction")
	public String addTransaction(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal) {
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		model.addAttribute("branches", branchRepository.findAll());
		model.addAttribute("months", DateTimeUtil.getMonths());	
		
		List<Integer> years = new ArrayList<Integer>();
		int year = 2021;
		for (int i = year; i < (year+11); i++) {
			years.add(i);			
		} 
		model.addAttribute("years", years);	
		
		menu.setTransactionLink("active");
		menu.setAddTransactionLink("active");
		menu.setTransactionMenuOpen("menu-open");
		model.addAttribute("menu", menu);
		model.addAttribute("filePath", SERVER_URL);
		return "add-transaction";
	}
	
	@PostMapping("/save-transaction")
    public String saveTransaction(@ModelAttribute ActiveLinkMenu menu, @ModelAttribute("transactions") Transactions transactions, 
    		BindingResult result, Model model, Principal principal, RedirectAttributes redirectAttrs) {
				
			 String userName = principal.getName();
			 User user = userRepository.getUserByUserName(userName);
		     int userId = user.getId();
		     String monthName = transactions.getMonth();
		     String year = transactions.getYear();
		     
		     transactions.setTranDate("01-"+monthName+"-"+year);
		     transactions.setMonth(DateTimeUtil.getMonths().get(monthName));
		     transactions.setUserId(userId);
		     transactions.setCreatedDatetime(DateTimeUtil.getSysDateTime());
		     
		     transRepository.save(transactions);
			
			 redirectAttrs.addFlashAttribute("messageSuccess", "Success.");
			 return "redirect:/add-transaction";			
	}
	
	@GetMapping("/getEmployeesByBranch")
	public @ResponseBody List<Employee> getEmployeesByBranch(@RequestParam String employeeBranch, Model model) {

		List<Employee> employeeList = employeeRepository.getEmployeeByEmployeeBranch(employeeBranch);
		
		return employeeList;
	}
	
	@GetMapping("/view-transaction")
	public String viewTransaction(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal) {
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		model.addAttribute("transList", transRepository.findAll());
		
		menu.setTransactionLink("active");
		menu.setViewTransactionLink("active");
		menu.setTransactionMenuOpen("menu-open");
		model.addAttribute("menu", menu);
		model.addAttribute("filePath", SERVER_URL);
		
		return "view-transaction";
	}
	
	@GetMapping("/delete-transaction/{transactionId}")
	public String deleteTransaction(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal, 
			RedirectAttributes redirectAttrs, @PathVariable int transactionId) {
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		transRepository.deleteById(transactionId);
		
		menu.setTransactionLink("active");
		menu.setViewTransactionLink("active");
		menu.setTransactionMenuOpen("menu-open");
		model.addAttribute("menu", menu);
		model.addAttribute("filePath", SERVER_URL);
		
		redirectAttrs.addFlashAttribute("messageSuccess", "Delete record.");
		 return "redirect:/view-transaction";
	}
	
	@GetMapping("/branch-transaction-report")
	public String branchTransactionReport(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal,
			@ModelAttribute("transactions") Transactions transactions, BindingResult result) {
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		model.addAttribute("branches", branchRepository.findAll());
		model.addAttribute("months", DateTimeUtil.getMonths());	
		
		List<Integer> years = new ArrayList<Integer>();
		int fixedYear = 2021;
		for (int i = fixedYear; i < (fixedYear+11); i++) {
			years.add(i);			
		} 
		model.addAttribute("years", years);	
		
		String reportType = transactions.getReportId();
		if(reportType != null) {
			
			//System.out.println(reportType);
			if(reportType.equals("Monthly")) {
				
				String monthName = DateTimeUtil.getMonths().get(transactions.getMonth());
				List<Transactions> transList = transRepository.getTransactionsByEmployeeBranchAndMonthAndYear(transactions.getEmployeeBranch(), monthName, transactions.getYear());
				model.addAttribute("transList", transList);
				model.addAttribute("period",  monthName+"-"+transactions.getYear());
				
			} else if(reportType.equals("Annual")) {
				
				String finYear  = transactions.getAnnual();
				
				String startYear = finYear.split("-")[0];
				String endYear   = finYear.split("-")[1];
				
				String month  = "01-04-"+startYear;
				String year   = "31-03-"+endYear;
				
				List<Transactions> transList = transRepository.getBranchTransactionsByEmployeeBranchAndMonthAndYear(transactions.getEmployeeBranch(), month, year);
				model.addAttribute("transList", transList);	
				model.addAttribute("period", finYear);
				
			}else if(reportType.equals("Half Yearly")) {
				
				String finYear  = transactions.getAnnual();
				
				String startYear = finYear.split("-")[0];
				String endYear   = finYear.split("-")[1];
				
				String halfYearly = transactions.getHalfYearly();
				String month = "01-04-"+startYear;
				String year   = "31-03-"+endYear;
				
				if(halfYearly.equals("First Half-Year")) {
					
					 month = "01-04-"+startYear;
					 year  = "30-09-"+startYear;
					
				} else if(halfYearly.equals("Second Half-Year")) {
					 month = "01-10-"+startYear;
					 year  = "31-03-"+endYear;
				}
				
				List<Transactions> transList = transRepository.getBranchTransactionsByEmployeeBranchAndMonthAndYear(transactions.getEmployeeBranch(), month, year);
				model.addAttribute("transList", transList);	
				model.addAttribute("period", finYear+": "+halfYearly);
				
			}else if(reportType.equals("Quarterly")) {
				
				String finYear  = transactions.getAnnual();
				
				String startYear = finYear.split("-")[0];
				String endYear   = finYear.split("-")[1];
				
				String quarterly = transactions.getQuarterly();
				String month = "01-04-"+startYear;
				String year   = "31-03-"+endYear;
				
				if(quarterly.equals("Quarter-1")) {
					
					 month = "01-04-"+startYear;
					 year  = "30-06-"+startYear;
					
				} else if(quarterly.equals("Quarter-2")) {
					
					 month = "01-07-"+startYear;
					 year  = "31-09-"+startYear;
					 
				} else if(quarterly.equals("Quarter-3")) {
					
					 month = "01-10-"+startYear;
					 year  = "31-12-"+startYear;
					 
				} else if(quarterly.equals("Quarter-4")) {
					
					 month = "01-01-"+endYear;
					 year  = "31-03-"+endYear;
				}
				
				List<Transactions> transList = transRepository.getBranchTransactionsByEmployeeBranchAndMonthAndYear(transactions.getEmployeeBranch(), month, year);
				model.addAttribute("transList", transList);	
				model.addAttribute("period", finYear+": "+quarterly);
			}			
		}		
		
		model.addAttribute("reportType", reportType);				
		menu.setTransactionLink("active");
		menu.setBranchReportTransactionLink("active");
		menu.setTransactionMenuOpen("menu-open");
		model.addAttribute("menu", menu);
		model.addAttribute("filePath", SERVER_URL);
		
		return "branch-transaction-report";
	}
	
	@GetMapping("/employee-transaction-report")
	public String employeeTransactionReport(@ModelAttribute ActiveLinkMenu menu, Model model, Principal principal,
			@ModelAttribute("transactions") Transactions transactions, BindingResult result) {
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
		model.addAttribute("branches", branchRepository.findAll());
		model.addAttribute("months", DateTimeUtil.getMonths());	
		
		List<Integer> years = new ArrayList<Integer>();
		int fixedYear = 2021;
		for (int i = fixedYear; i < (fixedYear+11); i++) {
			years.add(i);			
		} 
		model.addAttribute("years", years);	
		
		String reportType = transactions.getReportId();
		if(reportType != null) {
			
			//System.out.println(reportType);
			if(reportType.equals("Monthly")) {
				
				String monthName = DateTimeUtil.getMonths().get(transactions.getMonth());
				List<Transactions> transList = transRepository.getTransactionsByEmployeeBranchAndEmployeeIdAndMonthAndYear(transactions.getEmployeeBranch(), transactions.getEmployeeId(), monthName, transactions.getYear());
				model.addAttribute("transList", transList);
				model.addAttribute("period",  monthName+"-"+transactions.getYear());
				
			} else if(reportType.equals("Annual")) {
				
				String finYear  = transactions.getAnnual();
				
				String startYear = finYear.split("-")[0];
				String endYear   = finYear.split("-")[1];
				
				String month  = "01-04-"+startYear;
				String year   = "31-03-"+endYear;
				
				List<Transactions> transList = transRepository.getEmployeeTransactionsByEmployeeBranchAndEmployeeIdAndMonthAndYear(transactions.getEmployeeBranch(), transactions.getEmployeeId(), month, year);
				model.addAttribute("transList", transList);	
				model.addAttribute("period", finYear);
				
			}else if(reportType.equals("Half Yearly")) {
				
				String finYear  = transactions.getAnnual();
				
				String startYear = finYear.split("-")[0];
				String endYear   = finYear.split("-")[1];
				
				String halfYearly = transactions.getHalfYearly();
				String month = "01-04-"+startYear;
				String year   = "31-03-"+endYear;
				
				if(halfYearly.equals("First Half-Year")) {
					
					 month = "01-04-"+startYear;
					 year  = "30-09-"+startYear;
					
				} else if(halfYearly.equals("Second Half-Year")) {
					 month = "01-10-"+startYear;
					 year  = "31-03-"+endYear;
				}
				
				List<Transactions> transList = transRepository.getEmployeeTransactionsByEmployeeBranchAndEmployeeIdAndMonthAndYear(transactions.getEmployeeBranch(), transactions.getEmployeeId(), month, year);
				model.addAttribute("transList", transList);	
				model.addAttribute("period", finYear+": "+halfYearly);
				
			}else if(reportType.equals("Quarterly")) {
				
				String finYear  = transactions.getAnnual();
				
				String startYear = finYear.split("-")[0];
				String endYear   = finYear.split("-")[1];
				
				String quarterly = transactions.getQuarterly();
				String month = "01-04-"+startYear;
				String year   = "31-03-"+endYear;
				
				if(quarterly.equals("Quarter-1")) {
					
					 month = "01-04-"+startYear;
					 year  = "30-06-"+startYear;
					
				} else if(quarterly.equals("Quarter-2")) {
					
					 month = "01-07-"+startYear;
					 year  = "31-09-"+startYear;
					 
				} else if(quarterly.equals("Quarter-3")) {
					
					 month = "01-10-"+startYear;
					 year  = "31-12-"+startYear;
					 
				} else if(quarterly.equals("Quarter-4")) {
					
					 month = "01-01-"+endYear;
					 year  = "31-03-"+endYear;
				}
				
				List<Transactions> transList = transRepository.getEmployeeTransactionsByEmployeeBranchAndEmployeeIdAndMonthAndYear(transactions.getEmployeeBranch(), transactions.getEmployeeId(), month, year);
				model.addAttribute("transList", transList);	
				model.addAttribute("period", finYear+": "+quarterly);
			}			
		}		
		
		model.addAttribute("reportType", reportType);				
		menu.setTransactionLink("active");
		menu.setEmployeeReportTransactionLink("active");
		menu.setTransactionMenuOpen("menu-open");
		model.addAttribute("menu", menu);
		model.addAttribute("filePath", SERVER_URL);
		
		return "employee-transaction-report";
	}
	
	@PostMapping("/uploadDashboardProfilePicture")
	public @ResponseBody void uploadLogo(@RequestParam("file") MultipartFile logo, @RequestParam("employeeId") int employeeId) {
					
		try {
			
			String passport  = logo.getOriginalFilename();
			String fileName1 = "dp"+DateTimeUtil.getTimeStampInMiliseconds()+passport.substring(passport.lastIndexOf("."));

			File file = new File(EMPLOYEE_UPLOAD_DIR + fileName1);
			try {
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(logo.getBytes());
				fos.close();
				
			} catch (IOException e) {
				e.printStackTrace();
				
			}
			employeeRepository.uploadProfilePicture(employeeId, fileName1);

		} catch (Exception e) {
			logger.error("Exceptin while uploadDashboardProfilePicture : " + e.toString());
		}
	}
	
	@PostMapping("/uploadUserProfilePicture")
	public @ResponseBody void uploadUserlogo(@RequestParam("file") MultipartFile logo, @RequestParam("userId") int userId) {
					
		try {
			//System.out.println("userId : "+userId);
			
			String passport  = logo.getOriginalFilename();
			String fileName1 = "dp"+DateTimeUtil.getTimeStampInMiliseconds()+passport.substring(passport.lastIndexOf("."));

			File file = new File(EMPLOYEE_UPLOAD_DIR + fileName1);
			try {
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(logo.getBytes());
				fos.close();
				
			} catch (IOException e) {
				e.printStackTrace();
				
			}
			
			userRepository.uploadProfilePicture(userId, fileName1);
			
		} catch (Exception e) {
			logger.error("Exception while uploadUserProfilePicture : " + e.toString());
		}
	}
	
}
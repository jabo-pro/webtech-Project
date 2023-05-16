package com.smart.controller;

import javax.servlet.http.HttpSession;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home()
	{
		
		return "index";
	}

	
	@RequestMapping("/about")
	public String about()
	{
		
		return "index";
	}
	

	
	@RequestMapping("/signup")
	public String signup(Model m)
	{
		m.addAttribute("title", "Register - Smart Contact Manger");
		m.addAttribute("user", new User());
		return "signup";
	}
	
	
	
	
	//this handler for user registration
	@PostMapping("/do_register")
	public String registerUser(  @ModelAttribute("user") User user ,@RequestParam(value = "agreement" ,defaultValue = "false")boolean agreement  , Model model , 
			HttpSession session ,BindingResult res)
	{
		try {
		if(!agreement)
		{
			System.out.println("agreeemt is not fulfiled");
			throw new Exception("agreeemt is not fulfiled");
		}
		
		
		if(res.hasErrors())
		{
			System.out.println("error"+res.toString());
			model.addAttribute("user", user);
			return "signup";
		}
		
		user.setRole("ROLE_USER");
		user.setEnabled(true);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		
		
		System.out.println("agreement"+agreement);
		System.out.println("user"+user);
		
		User result = this.userRepository.save(user);
		System.out.println("result :"+result);
		
		model.addAttribute("user", new User());
		
		session.setAttribute("message", new Message("successfully register ", "alert-error"));
		}catch (Exception e) {
			// TODO: handle exception
			
			e.printStackTrace();
			model.addAttribute("user" , user);
			session.setAttribute("message", new Message("something went wrong"+e.getMessage(), "alert-danger"));
			return "signup";
		}
		
		return "signup";
	}
	
	
	@GetMapping("/signin")
	public String customLogin(Model model)
	{
		model.addAttribute("title", "login page");
		
		return "login";
		
	}
}

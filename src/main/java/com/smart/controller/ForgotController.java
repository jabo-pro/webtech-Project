package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.EmailService;
import com.smart.dao.UserRepository;
import com.smart.entities.User;

@Controller
public class ForgotController {

	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;

	@Autowired
	private UserRepository userRepository;
	

	@RequestMapping("/forgot")
public String openEmailForm()
{

return "forgot_email_form";
}
	
	@PostMapping("/send-otp")
public String sendOTP(@RequestParam("email") String email,
		HttpSession session
		)
{

		//generating otp of four digit
		

		
		int otp = random.nextInt(99999);
		
		String subject = "OTP form SCM";
		String message="<h1> OTP="+otp+"<h1>";
		String to= email;
		
		boolean sendEmail = this.emailService.sendEmail(message, subject, to);

		if(sendEmail)
		{
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}
		else
		{
			session.setAttribute("message", "Check your email!!");
			return "forgot_email_form";
		
		}
		
		
		
}
	
	
	//pulbic string verigy otp
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session)
	{
	
		int myOtp= (Integer) session.getAttribute("myotp");
		String email= (String) session.getAttribute("email");
		
		if(myOtp==otp)
		{
			
			User user = this.userRepository.getUserByUserName(email);
			
			if(user==null)
			{
				//send error
				session.setAttribute("message","Your are does not exist with this email !!");
				return "forgot_email_form";
			}
			else
			{
				
			}
			
			return "password_change_form";
		}
		else {
			session.setAttribute("message","you have entered wrong otp");
			return "verify_otp";
		}
	
		
		
	}

}


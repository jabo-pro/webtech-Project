package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println(userName);

		User user = userRepository.getUserByUserName(userName);
		System.out.println(user);

		model.addAttribute("user", user);
	}

	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard");
		return "user/user_dashboard";
	}

	// open add form handler
	@GetMapping("/add_contact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title", "Add contact");
		model.addAttribute("contact", new Contact());
		return "add_contact_form";
	}

	// procesing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profile") MultipartFile file,
			Principal principal,
			HttpSession session
			) {
		try {
			// getting user
			String name = principal.getName();

			// adding contact to database
		
			  User user = this.userRepository.getUserByUserName(name);
			  System.out.println("checking");
			  //procesisng and uploading file
			  if(file.isEmpty())
			  {
				  System.out.println("file is empty");
				  
				  contact.setImageUrl("contact.png");
				  //if file is empty then try our message
			  }else
			  {
				  //upload the file to folder and update the contact
				  contact.setImageUrl(file.getOriginalFilename());
				  File saveFile = new ClassPathResource("static/image").getFile();
				  //getting path where to store the file
				  Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				  
				  Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING);
				 System.out.println("file is uploaded");
				  
			  }
			  
			  contact.setUser(user); 
			  
			  
			  user.getContacts().add(contact);
			  
			  this.userRepository.save(user);
		
			  
			System.out.println("data" + contact);
			
			//message sucees
			
			session.setAttribute("message", new Message("your contact is added !! add more","success"));
			
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
			session.setAttribute("message", new Message("something went wrong","danger"));
		}
		return "add_contact_form";
	}
	
	
	//show contact handler
	//per page =5[n]
	//current page =0[apge]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page")Integer page , Model m, Principal principal)
	{
		m.addAttribute("titel","show user contacts");

		//contact ki list bhejni hai
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		Pageable pageable = PageRequest.of(page, 5);
	 Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(),pageable);
		m.addAttribute("contacts", contacts);

		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());

		return "show_contacts";
	}



	//shoing particular contact details
	@RequestMapping("/{id}/contact")
	public String showContactDetails(@PathVariable("id") Integer id, Model model, Principal principal)
	{

		System.out.println("CID"+id);
		Optional<Contact> contactOptional = this.contactRepository.findById(id);
		Contact contact = contactOptional.get();

		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);

		if(user.getId()==contact.getUser().getId())
		{	model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}




		return "user/contact_details";
	}



	//delete contact handler
	@GetMapping("/delete/{id}")
	@Transactional
	public String deleteContact(@PathVariable("id") Integer id, Model model, HttpSession session)
	{
		System.out.println("contact is deleted");
		Optional<Contact> contactOptional = this.contactRepository.findById(id);
		Contact contact = contactOptional.get();

		contact.setUser(null);

		this.contactRepository.delete(contact);


		session.setAttribute("message", new Message("contact deleted succesfuly", "success"));

		return "redirect:/user/show-contacts/0";
	}
	
	
	//open update from handler
	
	@PostMapping("/update-contact/{id}")
	public String updateForm(@PathVariable("id") Integer id,Model m)
	{
		
		m.addAttribute("title","Update Contact");
		Contact contact = this.contactRepository.findById(id).get();
		m.addAttribute("contact", contact);
		
		
		return "user/update_form";
	}
	
	
	//update the form
	
	@PostMapping("/process-update")
	public String updateContact(@ModelAttribute Contact contact ,
			Principal principal,
			@RequestParam("profile") MultipartFile file ,
			Model m, HttpSession session)
	{
		System.out.println("contact name"+contact.getName());
		try {
			//old contact detail
			Contact oldContactDetail = this.contactRepository.findById(contact.getId()).get();
			
			
			//image processing...
			if(!file.isEmpty())
			{
				//file work here
				//rewrite
				//delete old photo

				  File deleteFile = new ClassPathResource("static/image").getFile();
				  File file1= new File(deleteFile, oldContactDetail.getImageUrl());
				
				  file1.delete();
				
				//update new photo
				
				  File saveFile = new ClassPathResource("static/image").getFile();
				  //getting path where to store the file
				  Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				  
				  Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING);
				
				  contact.setImageUrl(file.getOriginalFilename());
				
			}else
			{
				contact.setImageUrl(oldContactDetail.getImageUrl());
			}
			User user= this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
		
			session.setAttribute("message", new Message("Your contact is updated", "success"));
		
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		
		
		
		
		return "redirect:/user/"+contact.getId()+"/contact";
	}
	
	
	//your profile handler
	@RequestMapping("/profile")
	public String yourProfile(Model m)
	{
	
		m.addAttribute("title", "your profile picture");
		
		return "user/profile";
		
	}
	
	
	//open setting 
	
	
	@GetMapping("/settings")
	public String openSetting()
	{
		
		return "user/settings";
	}
	
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("old")String oldPassword, 
			@RequestParam("new")String newPassword,
			Principal principal,
			HttpSession session)
	{
		
		System.out.println("old password"+oldPassword);
		System.out.println("new password"+newPassword);
		User currentUser = this.userRepository.getUserByUserName(principal.getName());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message",new Message("Your Password is successfully chage", "success"));
		}else
		{
			session.setAttribute("message",new Message("You enterd wrong old password", "danger"));
			return "redirect:/user/settings";
		}
		
		
		
		return "redirect:/user/index";
	}

}

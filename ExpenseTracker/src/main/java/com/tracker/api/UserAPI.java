package com.tracker.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.cloud.vision.spi.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.tracker.domain.project.Project;
import com.tracker.domain.receipt.Receipt;
import com.tracker.domain.users.User;
import com.tracker.services.ProjectsService;
import com.tracker.services.ReceiptsService;

@Controller
@RequestMapping("/user")
@Secured("ROLE_USER")
public class UserAPI {

	@Autowired
	private ReceiptsService receiptService;

	@Autowired
	private ProjectsService projectService;
	
	@RequestMapping( value="", method=RequestMethod.GET )
	public String getAdminHome(	@AuthenticationPrincipal User user, Model model ) {
		System.out.println("Returning user's home page");
		return "redirect:#/user";
	}
	
	/* User browser all his receipts */
	@RequestMapping( value="/{uid}/receipts", method=RequestMethod.GET )
	@ResponseBody
	public Page<Receipt> getReceipts(	@AuthenticationPrincipal User user ,
										@PathVariable String uid,
										@RequestParam( required=false, defaultValue="" ) String place,
										@RequestParam( required=false, defaultValue="" ) String project,
										@RequestParam( required=false, defaultValue="" ) String upperLimit,
										@RequestParam( required=false, defaultValue="" ) String lowerLimit,
										@RequestParam( required=false, defaultValue="" ) String category,
										@RequestParam( required=false, defaultValue="0" ) String page,
										@RequestParam( required=false, defaultValue="10" ) String size,
										HttpServletResponse response ) throws IOException {
		if( !uid.equals( user.getId())) {
			response.sendError(403,"You are not allowed to browse other user's data!");
		}
		Pageable pageable = new PageRequest(  Integer.valueOf( page ), Integer.valueOf( size ) );
		
		if( place.length() > 0 || project.length() > 0 || category.length() > 0
				|| upperLimit.length() > 0 || lowerLimit.length() > 0 ) {
			System.out.println("Searching receipt");
			Page<Receipt> result = receiptService.searchReceipt( uid, place, project, upperLimit, lowerLimit , category, pageable);
			return result;
		}
		
		Page< Receipt > result = receiptService.findByOwnerId(user.getId(), pageable);
		return result;
	}
	
	/* User create a receipt */
	@RequestMapping( value="/{uid}/receipts", method=RequestMethod.POST )
	@ResponseBody
	public Receipt createReceipts( 	@AuthenticationPrincipal User user ,
									@PathVariable String uid, Model model,
									@RequestBody Receipt receipt,
									HttpServletResponse response ) throws ParseException, IOException {
		if( !uid.equals( user.getId() ) ) {
			response.sendError(403,"You are not allowed to browse other user's data!");
		}
		
		receipt.setOwnerId( user.getId() );
		System.out.println( "Creating new receipt:\n"
							+ " time: " + receipt.getTime() 
							+ "\n place: " + receipt.getPlace() 
							+ "\n note: " + receipt.getNote() 
							+ "\n category: " + receipt.getCategory()[0]
							+ "\n total: " + receipt.getTotal() );
		receiptService.save( receipt );
		return receipt;
	}
	
	/*
	 * This area is for "share receipt" function.
	 * */
	
	/* User edit a receipt */
	@RequestMapping( value="/{uid}/receipts/{rid}", method=RequestMethod.PUT )
	@ResponseBody
	public Receipt updateReceipts( 	@AuthenticationPrincipal User user ,
									@PathVariable String uid, 
									@PathVariable String rid, 
									@RequestBody Receipt receipt,
									HttpServletResponse response ) throws ParseException, IOException {
		if( !uid.equals( user.getId())) {
			response.sendError(403,"You are not allowed to browse other user's data!");
		}

		System.out.println("update receipt with ID: " + receipt.getId() );
		receiptService.update(receipt);
		return receipt;
	}

	@RequestMapping( value="/{uid}/receipts/{rid}", method=RequestMethod.GET )
	@ResponseBody
	public Receipt findReceiptById( @AuthenticationPrincipal User user ,
									@PathVariable String uid, 
									@PathVariable String rid,
									HttpServletResponse response ) throws ParseException, IOException {
		if( !uid.equals( user.getId())) {
			response.sendError(403,"You are not allowed to browse other user's data!");
		}
		return receiptService.findOne( rid );
	}
	
	@RequestMapping( value="/{uid}/receipts/{rid}", method=RequestMethod.DELETE )
	@ResponseBody
	public void deleteReceiptById( @AuthenticationPrincipal User user ,
									@PathVariable String uid, 
									@PathVariable String rid,
									HttpServletResponse response ) throws ParseException, IOException {
		if( !uid.equals( user.getId())) {
			response.sendError(403,"You are not allowed to browse other user's data!");
		}
		receiptService.delete( rid );
	}
	
	/* Create a new project */
	@RequestMapping( value="/{uid}/projects", method=RequestMethod.POST )
	@ResponseBody
	public Project saveProject( 	@AuthenticationPrincipal User user ,
									@PathVariable String uid,
									@RequestBody Project project,
									HttpServletResponse response ) throws ParseException, IOException {
		if( !uid.equals( user.getId())) {
			response.sendError(403,"You are not allowed to browse other user's data!");
		}
		
		projectService.save(project);
		return projectService.findByOwnerIdAndName( uid, project.getName() );
	}
	
	/* User browse all his projects */
	@RequestMapping( value="/{uid}/projects", method=RequestMethod.GET )
	@ResponseBody
	public Page<Project> getProjects( 	@AuthenticationPrincipal User user ,
										@PathVariable String uid,
										@RequestParam( required=false, defaultValue="0" ) String page,
										@RequestParam( required=false, defaultValue="10" ) String size,
										HttpServletResponse response ) throws ParseException, IOException {
		if( !uid.equals( user.getId())) {
			response.sendError(403,"You are not allowed to browse other user's data!");
		}
		Pageable pageable = new PageRequest(  Integer.valueOf( page ), Integer.valueOf( size ) );

		Page<Project> result = projectService.findByOwnerId( uid, pageable );
		return result;
	}
	
	/* Get one project */
	@RequestMapping( value="/{uid}/projects/{pid}", method=RequestMethod.GET )
	@ResponseBody
	public Project getProjectById( 	@AuthenticationPrincipal User user ,
									@PathVariable String uid, 
									@PathVariable String pid,
									HttpServletResponse response ) throws ParseException, IOException {
		if( !uid.equals( user.getId()) ) {
			response.sendError(403,"You are not allowed to browse other user's data!");
		}
		Project result = projectService.findOne( pid );
		return result;
	}
	
	/* Edit a project */
	@RequestMapping( value="/{uid}/projects/{pid}", method=RequestMethod.PUT )
	@ResponseBody
	public Project updateProjectById(	@AuthenticationPrincipal User user ,
										@PathVariable String uid, 
										@RequestBody Project project,
										HttpServletResponse response ) throws ParseException, IOException {
		if( !uid.equals( user.getId()) ) {
			response.sendError(403,"You are not allowed to browse other user's data!");
		}
		projectService.update( project );
		return project;
	}
	
	/* Delete a project */
	@RequestMapping( value="/{uid}/projects/{pid}", method=RequestMethod.DELETE )
	@ResponseBody
	public void deleteProjectById( 	@AuthenticationPrincipal User user ,
									@PathVariable String uid, 
									@PathVariable String pid,
									HttpServletResponse response ) throws ParseException, IOException {
		if( !uid.equals( user.getId()) ) {
			response.sendError(403,"You are not allowed to browse other user's data!");
		}
		projectService.delete( pid );
	}
	
	@RequestMapping( value="/{uid}/picture", method=RequestMethod.GET )
	@ResponseBody
	public void analyseImage() throws IOException {
		FileOutputStream fs = new FileOutputStream( new File("/Users/chenxia/Downloads/results.txt"));
		PrintStream ps = new PrintStream( fs );
		Detect app = new Detect(ImageAnnotatorClient.create());
		app.detectText("/Users/chenxia/Downloads/kwiktrip.JPG", ps );
		ps.close();
	}
	
//	@RequestMapping( value="/{uid}/newPicture", method=RequestMethod.GET )
//	 public void doGet() throws IOException {
//	   AppIdentityCredential credential =
//	       new AppIdentityCredential(Arrays.asList(UrlshortenerScopes.URLSHORTENER));
//	   Urlshortener shortener =
//	       new Urlshortener.Builder(new UrlFetchTransport(), new JacksonFactory(), credential)
//	       .build();
//	   UrlHistory history = shortener.URL().list().execute();
//	   
//	 }

	
	@RequestMapping( value="/{uid}/google", method=RequestMethod.GET )
	@ResponseBody
	public List<EntityAnnotation> analyseImage2() throws IOException {
		FileOutputStream fs = new FileOutputStream( new File("/Users/chenxia/Downloads/Information of picture.txt"));
		PrintStream ps = new PrintStream( fs );
		Detect app = new Detect(ImageAnnotatorClient.create());
		return Detect.detectText("/Users/chenxia/Downloads/kwiktrip.JPG", System.out );
	}
	
}

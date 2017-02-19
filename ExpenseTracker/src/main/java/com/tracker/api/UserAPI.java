package com.tracker.api;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;

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
import org.springframework.web.multipart.MultipartFile;

import com.tracker.domain.pictures.Picture;
import com.tracker.domain.project.Project;
import com.tracker.domain.receipt.Receipt;
import com.tracker.domain.users.User;
import com.tracker.services.PictureService;
import com.tracker.services.ProjectsService;
import com.tracker.services.ReceiptsService;
import com.tracker.services.VisionService;

@Controller
@RequestMapping("/user")
@Secured("ROLE_USER")
public class UserAPI {

	@Autowired
	private ReceiptsService receiptService;

	@Autowired
	private ProjectsService projectService;

	@Autowired
    private VisionService visionService;
	
	@Autowired
	private PictureService pictureService;
	
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
		if( !uid.equals( user.getId() ) ) {
			response.sendError(403,"You are not allowed to browse other user's data!");
		}
		receiptService.delete( rid );
	}
	
	@RequestMapping( value="/{uid}/projects/{pid}/receipts", method=RequestMethod.GET )
	@ResponseBody
	public Page<Receipt> getAllReceiptsInOneProject( @AuthenticationPrincipal User user ,
									@PathVariable String uid, 
									@PathVariable String pid,
									@RequestParam( required=false, defaultValue="0" ) int page,
									@RequestParam( required=false, defaultValue="10" ) int size,
									HttpServletResponse response ) throws ParseException, IOException {
		if( !uid.equals( user.getId() ) ) {
			response.sendError(403,"You are not allowed to browse other user's data!");
		}
		Pageable pageable = new PageRequest(  Integer.valueOf( page ), Integer.valueOf( size ) );
		return receiptService.findByProject(pid, pageable);
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
			if( projectService.findByOwnerIdAndName(uid, project.getName()) != null) {
				response.sendError(400,"Project already exists, please use another name.");
				return null;
			} else {
				return projectService.save(project);
			}
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
		receiptService.deleteReceiptsWithOneProject( pid );
		projectService.delete( pid );
	}
	
	/* Upload a picture for a new receipt*/
	@RequestMapping( value="/{uid}/projects/{projectId}/pictures", method=RequestMethod.POST )
	@ResponseBody
	public Receipt uploadPicture(	@PathVariable String uid, 
									@PathVariable String projectId,
									@RequestParam("file") MultipartFile file,
									HttpServletResponse response ) throws IOException {
		if ( !file.isEmpty() ) {
			try {
				/* Create a new receipt */
				Receipt receipt = new Receipt();
				receipt.setOwnerId( uid );
				receipt.setProjectId( projectId );
				String rid = receiptService.save(receipt).getId();
				
				Picture picture = new Picture();
				picture.setOwnerId(uid);
				picture.setReceiptId(rid);
				picture = pictureService.save(picture);
				String pid = picture.getId();
				receipt.setPicId(pid);
				receiptService.update(receipt);
				System.out.println("Newly created picture: " + pid);
				byte[] bytes = file.getBytes();

				// Creating the directory to store file
				File dir = new File("src/main/pictures/" + uid );
				if ( !dir.exists() )
					dir.mkdirs();

				// Create the file on server
				File serverFile = new File( dir.getAbsolutePath() + File.separator +pid );
				System.out.println( "Init new serverFile, saving to: " + dir.getAbsolutePath() );
				BufferedOutputStream stream = new BufferedOutputStream(
						new FileOutputStream(serverFile) );
				stream.write(bytes);
				stream.close();
				
				String fileName = "../ExpenseTracker/src/main/pictures/" + uid + "/" + pid;
				
		        picture.setTextAnnotation( visionService.detect( fileName ).getResponses().get(0) );
		        
		        pictureService.update(picture);
				return receipt;
			} catch (Exception e) {
				response.sendError( 500, "Fail to save picture, check if your picture is smaller than 4MB." );
				return null;
			}
		} else {
			response.sendError( 400, "Error occurs when uploading picture.");
			return null;
		}
	}
	
	@RequestMapping( value="/{uid}/receipts/{rid}/pictures/{pid}", method=RequestMethod.GET )
	@ResponseBody
	public File analyseImage( 	@PathVariable String pid ,
									@PathVariable String uid ) throws Exception {
		return new File("../ExpenseTracker/src/main/pictures/" + uid + "/" + pid);
	}
	
}

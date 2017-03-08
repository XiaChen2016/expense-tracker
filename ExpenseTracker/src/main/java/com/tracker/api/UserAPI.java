package com.tracker.api;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BoundingPoly;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.tracker.domain.pictures.DetailBox;
import com.tracker.domain.pictures.Picture;
import com.tracker.domain.project.Project;
import com.tracker.domain.receipt.Item;
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
		
		Page< Receipt > result = receiptService.findByOwnerId( user.getId(), pageable );
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
//		System.out.println( "Creating new receipt:\n"
//							+ " time: " + receipt.getTime() 
//							+ "\n place: " + receipt.getPlace() 
//							+ "\n note: " + receipt.getNote() 
//							+ "\n category: " + receipt.getCategory()[0]
//							+ "\n total: " + receipt.getTotal() );
		receipt = receiptService.save( receipt );
		Project project = projectService.findOne( receipt.getProjectId() );
		List<Receipt> receipts = project.getReceipts();
		if( receipts == null ) {
			receipts = new ArrayList<Receipt>();
		}
		receipts.add(receipt);
		project.setReceipts(receipts);
		project.setNumOfReceipts( project.getNumOfReceipts() +1 );
		projectService.update(project);
		return receipt;
	}
	
	/* At the second step of creating receipts, user would type a list of receipts. */
	@RequestMapping( value="/{uid}/receipts/{rid}/items", method=RequestMethod.POST )
	@ResponseBody
	public Receipt addItems( 	@AuthenticationPrincipal User user ,
									@PathVariable String uid, 
									@PathVariable String rid, 
									@RequestBody List< Item > list_of_items,
									HttpServletResponse response ) throws ParseException, IOException {
		if( !uid.equals( user.getId()) ) {
			response.sendError(403,"You are not allowed to browse other user's data!");
		}

		Receipt receipt = receiptService.findOne( rid );
		System.out.println("Add items to reciept: " + receipt.getId() );
		double total = 0;
		for( int i=0; i< list_of_items.size(); i++ ){
			total += list_of_items.get(i).getPrice()* list_of_items.get(i).getQuantity();
		}
		receipt.setTotal(total);
		receipt.setList_of_items( list_of_items );
		receiptService.update(receipt);
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
		Receipt receipt = receiptService.findOne( rid );
		Project project = projectService.findOne( receipt.getProjectId() );
		List<Receipt> receipts = project.getReceipts();
		receipts.remove(receipt);
		project.setReceipts(receipts);
		project.setNumOfReceipts( project.getNumOfReceipts() -1 );
		projectService.update(project);
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
				project.setNumOfReceipts(0);
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
		if( !uid.equals( user.getId() ) ) {
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
		if( !uid.equals( user.getId() ) ) {
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
		if( !uid.equals( user.getId() ) ) {
			response.sendError(403,"You are not allowed to browse other user's data!");
		}
		receiptService.deleteReceiptsWithOneProject( pid );
		projectService.delete( pid );
	}
	
	/* Upload a picture for a receipt*/
	@RequestMapping( value="/{uid}/receipts/{rid}/pictures", method=RequestMethod.POST )
	@ResponseBody
	public List<Item> pic(	@PathVariable String uid, 
									@PathVariable String rid,
									@RequestParam("file") MultipartFile file,
									HttpServletResponse response ) throws IOException {
		try {
			/* Check the type of file */
			InputStream input = file.getInputStream();
			try {
		        /* It's an image (only BMP, GIF, JPG and PNG are recognized). */
		        ImageIO.read(input).toString();
		    } catch (Exception e) {
		        /* It's not an image. */
		    	response.sendError( 400, "Please upload a picture.");
		    }				
			
			byte[] bytes = file.getBytes();
			List<DetailBox> detailUnits = visionService.detect1( bytes );
			System.out.println();
			String pid = savePictureToDBAndServer( uid, rid, detailUnits, bytes );
			System.out.println( pictureService.findOne(pid) );
			return getItemsFromPicture( detailUnits );

		} catch (Exception e) {
			response.sendError( 400, "Fail to upload picture, check if your picture is smaller than 4MB, and make sure there is text in it." );
			return null;
		}
		
	}
	
	@RequestMapping( value="/{pid}/items", method=RequestMethod.GET )
	@ResponseBody
	private List<Item> getItemsFromPicture( List<DetailBox> detailUnits ) {
		String priceFormat =  "[0-9]+[,.][0-9]{2}";
		try{
			List<Item> list_of_items = new ArrayList<Item>();
//			List<DetailUnit> descriptionWithDollar = new ArrayList<DetailUnit>();
			/* Assume that the receipt is not oblique. */
			List<Integer> dollars = new ArrayList<Integer>(); 
			for( int i = 0; i < detailUnits.size(); i++ ) {
				if( detailUnits.get(i).getDescription().toCharArray()[0] == '$') {
					if( !dollars.contains( detailUnits.get(i).getVertices()[0][1]) ){
						dollars.add( detailUnits.get(i).getVertices()[0][1] );
					}
				}
			}
//			boolean quantityFirst = true;
			List< List<DetailBox> > blockWithDollar = new ArrayList< List<DetailBox> > (); 
			for( int i = 0; i< dollars.size(); i++ ) {
				
				List<DetailBox> tempLine = new ArrayList<DetailBox>();
				for( int j = 0; j < detailUnits.size(); j++ ) {
					if( Math.abs(detailUnits.get(j).getVertices()[0][1] - dollars.get(i)) < 3 ) {
						tempLine.add( detailUnits.get(j) );
					}
				}
				if( !tempLine.isEmpty() ) {
//					if( !isInteger(tempLine.get(0).getDescription()) ) {
//						quantityFirst = false;
//					}
					
					/* Ignore total, tax, debit and change. */
					String descript = "";
					for( int k = 0; k < tempLine.size(); k++ ) {
						descript += tempLine.get(k).getDescription().toLowerCase();
					}
					if( !descript.contains("total") &&! descript.contains("tax")
							&& !descript.contains("debit") && !descript.contains("change") ) {
						blockWithDollar.add( tempLine );
					}
					
				}
			}
			System.out.println("Items in blockWithDollar:");
			for( int i = 0 ; i< blockWithDollar.size();i++ ) {
				for( int j = 0 ; j < blockWithDollar.get(i).size();j++ ){
					System.out.print(blockWithDollar.get(i).get(j).getDescription());
				}
				System.out.println();
			}
			
//			if( quantityFirst ) {
//				for( int i = 0; i < blockWithDollar.size(); i++ ) {
//					System.out.println("pattern: /* QUANTITY NAME PRICE */");
//					
//				}
//			} else {
				for( int i = 0; i < blockWithDollar.size(); i++ ) {
					System.out.println( i + "th row of blockWithDollar");
					Item item = new Item();
					item.setQuantity(1);
					int size = blockWithDollar.get(i).size();
					String name = "";
					for( int j = 0; j < size; j++ ) {
						if( blockWithDollar.get(i).get(j).getDescription().toCharArray()[0] == '$' ) {
							String price = blockWithDollar.get(i).get(j).getDescription().substring(1);
							item.setPrice( Float.valueOf(price));
						} else {
							name += (name.length() < 1? "":" ") + blockWithDollar.get(i).get(j).getDescription();
						}
					}
					System.out.println("name of item "+i+": " +name);
					item.setName(name);
					list_of_items.add(item);
				}
//			}
			
			return list_of_items;
		} catch( Exception e ) {
			System.out.println("Error occurs in getItemsFromPicture(): "+e);
			List<Item> items = new ArrayList<Item>();
			Item i = new Item();
			i.setName("Default Item");
			i.setQuantity(2);
			i.setPrice(4f);
			items.add(i);
			return items;
		}
		
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    // only got here if we didn't return false
	    return true;
	}

	private String savePictureToDBAndServer( String uid, String rid, List<DetailBox> detailUnits, byte[] bytes) throws IOException{
		Picture picture = new Picture();
		picture.setOwnerId(uid);
		picture.setReceiptId(rid);
		picture.setDetailUnits(detailUnits);
        picture = pictureService.save(picture);
        String pid = picture.getId();
        
        Receipt receipt = receiptService.findOne(rid);
        if( receipt.getPicId().isEmpty() ) {
        	receipt.setPicId(pid);
    	} else {
    		String oldPid = receipt.getPicId();
    		pictureService.delete( uid, oldPid );
    		receipt.setPicId( pid );
    	}
		receiptService.update(receipt);
		System.out.println("Set receipt: " + receipt.getId()+" with picture: " + pid);
		// Creating the directory to store file
		File dir = new File("src/main/pictures/" + uid );
		if ( !dir.exists() )
			dir.mkdirs();

		// Create the file on server
		File serverFile = new File( dir.getAbsolutePath() + File.separator + pid );
		BufferedOutputStream stream = new BufferedOutputStream(
				new FileOutputStream(serverFile) );
		stream.write(bytes);
		stream.close();
		return pid;
	}
	
	/* Get a picture by receipt's ID. */
	@RequestMapping( value="/{uid}/receipts/{rid}/pictures", method=RequestMethod.GET )
	@ResponseBody
	public ResponseEntity<byte[]>  getOnePicture( 	@PathVariable String rid ,
													@PathVariable String uid ,
													HttpServletResponse response) throws Exception {
		try{
		
		String pid = receiptService.findOne(rid).getPicId();
		String fileName = "../ExpenseTracker/src/main/pictures/" + uid + "/" + pid;
		Path path = Paths.get(fileName);
		byte[] data = Files.readAllBytes(path);
		return ResponseEntity.ok().contentType( getType( data ) ).body( data );
			
		} catch( Exception e ) {
			System.out.println(e);
			response.sendError( 400,"There is no picture for the receipt.");
			return null;
		}
	}
	
	private MediaType getType( byte[] array ) throws IOException {
		ImageInputStream iis = new MemoryCacheImageInputStream( new ByteArrayInputStream( array ) );
		Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
		
		if( readers.hasNext() ) {
			ImageReader reader = readers.next();
			reader.setInput(iis, true);
			String name = reader.getFormatName();
			if( "jpeg".equals( name.toLowerCase() ) ) {
				return MediaType.IMAGE_JPEG;
			} else if( "png".equals( name.toLowerCase()) ) {
				return MediaType.IMAGE_PNG;
			} else if( "gif".equals( name.toLowerCase())) {
				return MediaType.IMAGE_GIF;
			} else {
				throw new IllegalArgumentException("not an image");
			}
		} else {
			throw new IllegalArgumentException("not an image");
		}
	}
}

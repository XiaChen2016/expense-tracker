package com.tracker.api;

import static org.mockito.Matchers.endsWith;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
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
										@RequestParam( required=false, defaultValue="" ) String maxTotal,
										@RequestParam( required=false, defaultValue="" ) String minTotal,
										@RequestParam( required=false, defaultValue="" ) String maxDate,
										@RequestParam( required=false, defaultValue="" ) String minDate,
										@RequestParam( required=false, defaultValue="" ) String category,
										@RequestParam( required=false, defaultValue="" ) String item,
										@RequestParam( required=false, defaultValue="0" ) String page,
										@RequestParam( required=false, defaultValue="10" ) String size,
										HttpServletResponse response ) throws IOException {
		if( !uid.equals( user.getId() ) ) {
			response.sendError(403,"You are not allowed to browse other user's data!");
		}
		Pageable pageable = new PageRequest(  Integer.valueOf( page ), Integer.valueOf( size ) );
		
		if( place.length() > 0 || project.length() > 0 || category.length() > 0 || item.length() > 0 || maxDate.length() > 0
				|| minDate.length() > 0 || maxTotal.length() > 0 || minTotal.length() > 0 ) {
			Page<Receipt> result = receiptService.searchReceipt( uid, place, project, maxTotal, minTotal , maxDate, minDate, category,item, pageable);
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
			System.out.println( e + ": " + e.getStackTrace()[0].getLineNumber() );
			response.sendError( 400, "There is no picture for the receipt.");
			return null;
		}
	}
	
	/* Delete a picture by receipt's ID. */
	@RequestMapping( value="/{uid}/receipts/{rid}/pictures", method=RequestMethod.DELETE )
	@ResponseBody
	public void  deleteOnePicture( 	@PathVariable String rid ,
									@PathVariable String uid ,
									HttpServletResponse response )  throws Exception  {
		try{
			Receipt receipt = receiptService.findOne(rid); 
			if( receipt.getPicId() == null ) {
				response.sendError( 400, "There is no picture for the receipt." );
			} else {
				pictureService.delete(uid, receipt.getPicId());
				receipt.setPicId(null);
				receiptService.update(receipt);
			}
		} catch( Exception e ) {
			System.out.println( "Catch an error when delete picture " + e + ": " + e.getStackTrace()[0].getLineNumber() );
			response.sendError( 400, "There is no picture for the receipt.");
		}

	}
	
	/* Upload a picture for a receipt
	 * 209 means that we think our data is incomplete or not correct. 
	 * try to find GPS info */
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
			Date date = null;
			
			try{
				InputStream is = new ByteArrayInputStream(bytes);
//				Metadata metadata = ImageMetadataReader.readMetadata(is);
				Metadata metadata = ImageMetadataReader.readMetadata(is);

				for (Directory directory : metadata.getDirectories()) {
				    for (Tag tag : directory.getTags()) {
				        System.out.format("[%s] - %s = %s",
				            directory.getName(), tag.getTagName(), tag.getDescription());
				    }
				    if (directory.hasErrors()) {
				        for (String error : directory.getErrors()) {
				            System.err.format("ERROR: %s", error);
				        }
				    }
				}
				ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
				date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
				System.out.println("Date from image: "+ date.toString() );
			} catch( Exception e ) {
				System.out.println(e + "Can't read date from image upload");
			}
			
			List<DetailBox> detailUnits = visionService.detect1( bytes );
			String pid = savePictureToDBAndServer( uid, rid, detailUnits, bytes );
			System.out.println("save picture as: "+pid);
			return getItemsFromPicture( detailUnits, date );

		} catch (Exception e) {
			System.out.println( e + ": " + e.getStackTrace()[0].getLineNumber() );
			response.sendError( 400, "Failed to upload picture, check if your picture is smaller than 4MB, and make sure there is text in it." );
			return null;
		}
		
	}

	private List<Item> getItemsFromPicture( List<DetailBox> detailBoxes, Date dateFromImage ) {
		String priceFormat =  "\\(?([$])?(-)?[0-9]+[,.][0-9]{2}+([A-Z])?\\)?";
		String location = "";
		Long time = 0l;
		String tempDate = "";
		String tempTime = "";
		int lineNumber = 0;
		try{
			List<Item> list_of_items = new ArrayList<Item>();
			List< List<DetailBox> > copyOfReceipt = new ArrayList< List<DetailBox> >();
			List<Integer> linesWithPrice = new ArrayList<Integer>(); 
			/* 
			 * Get the line height and Y coordinate of centroid of first box in the receipt.
			 * */
			int[][] vertices = detailBoxes.get(1).getVertices();
			int i = 0;
			int averageY = 0;
			while( i < 4 ) {
				averageY+= vertices[i++][1];
			}
			averageY /= 4;
			int lineHeight = vertices[3][1] - vertices[0][1];
			System.out.println("line height: " + lineHeight);
			List<DetailBox> tempLine = new ArrayList<DetailBox>();
			
			for( i = 1; i < detailBoxes.size(); i++ ) {
//				if( copyOfReceipt.size() < 4 ) {
//					System.out.println( detailBoxes.get(i).getDescription());
//				}
				vertices = detailBoxes.get(i).getVertices();
				int j = 0, centroidY = 0;
				while( j < 4 ){
					centroidY += vertices[j++][1];
				}
				centroidY /= 4;
				if( Math.abs( centroidY - averageY ) <= lineHeight*2.0/3.0 ) {
					tempLine.add( detailBoxes.get(i) );
				} else {
					tempLine = sortBoxes( tempLine );
//					System.out.print("\nAdding line "+ lineNumber +": ");
//					for( int k = 0; k< tempLine.size(); k++ ){
//						System.out.print(tempLine.get(k).getDescription() +" ");
//					}
//					System.out.println();
					
					copyOfReceipt.add( tempLine );
					if( location.length() < 1 && lineNumber < 2 ) {
						String tempLocation = getLocation( tempLine );
						if( tempLocation.length() > 1 ) {
							location = tempLocation;
							System.out.println("************************* location: "+ tempLocation +" *************************");
						}
					}
					if( tempDate.length() < 1 ) {
						tempDate = getDate( tempLine, dateFromImage );
					}
					if( tempTime.length() < 1 ) {
						tempTime = getTime( tempLine );
					}
					lineNumber++;
					
					tempLine = new ArrayList<DetailBox>();
					tempLine.add( detailBoxes.get(i));
					averageY = centroidY;
					lineHeight = vertices[3][1] - vertices[0][1];
				}
				if( detailBoxes.get(i).getDescription().matches( priceFormat )
						&& !linesWithPrice.contains(copyOfReceipt.size()) ) {
//					System.out.println("\nAdding "+ copyOfReceipt.size() 
//						+" to the lines with price for "+detailBoxes.get(i).getDescription());
					linesWithPrice.add( copyOfReceipt.size() );	
				}
				
			}
			for( i = 0; i < linesWithPrice.size(); i++ ) {
				int index = linesWithPrice.get(i);
				Item item = new Item();
				item.setQuantity(1); 
				String name = "";
				for( int j = 0; j < copyOfReceipt.get( index ).size(); j++ ) {
					String temp = copyOfReceipt.get(index).get(j).getDescription();
					if( temp.matches(priceFormat) ) {
					    Pattern pattern = Pattern.compile("(-)?\\d{1,7}[,\\.](\\d{1,2})?");
					    Matcher matcher = pattern.matcher(temp);
					    if ( matcher.find() )
					    {
					    	/*
					    	 * Replace "," with "." in prices
					    	 * */
					    	String priceFound = matcher.group(0);
					    	if( priceFound.indexOf(",") > 0 ) {
					    		String[] parts = priceFound.split(",");
					    		priceFound = parts[0] + "." + parts[1];
					    	}
					        item.setPrice( Float.valueOf( priceFound ) );
					    }
					} else {
						name += temp + " ";
					}
				}
				
				System.out.println("name of item "+i+": " +name);
				item.setName(name);
				list_of_items.add(item);
			}
			
			return list_of_items;
		} catch( Exception e ) {
			System.out.println( "Error occurs in getItemsFromPicture(): " + e + ": line " + e.getStackTrace()[0].getLineNumber()  );
			List<Item> items = new ArrayList<Item>();
			Item i = new Item();
			i.setName("Default Item");
			i.setQuantity(2);
			i.setPrice(4f);
			items.add(i);
			return items;
		}
		
	}
	
	/* 
	 * Check if there is date in current line,
	 * and return date in "MM-DD-YYYY" format. 
	 * */
	private String getDate( List<DetailBox> line,  Date dateFromImage ) {
		try{
			Pattern number = Pattern.compile("[0-9]{1,4}");
			Pattern date1 = Pattern.compile("([0-9]{1,2}(-)[0-9]{1,2}(-)([0-9]{4}|[0-9]{2}))"
											+ "|([0-9]{1,2}(/)[0-9]{1,2}(/)([0-9]{4}|[0-9]{2}))"
											+ "|([0-9]{1,2}(\\.)[0-9]{1,2}(\\.)([0-9]{4}|[0-9]{2}))");
			Pattern date2 = Pattern.compile("((jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec|january|february|march|april|may|june|july|august|september|october|november|december)[ ]?[0-9]{1,2}((,)?\\s|(('|’)( )?))[0-9]{2,4})"
											+ "|([0-9]{1,2}(-)[a-zA-Z]{3}-[0-9]{2,4})",Pattern.CASE_INSENSITIVE );
			Pattern word = Pattern.compile("[a-zA-Z]{3,9}");
			String result = "";
			String sum = "";
			
			for( int i = 0; i < line.size(); i++ ) {
				sum += line.get(i).getDescription() + " ";
			}
			Matcher matcherDate1 = date1.matcher(sum);
			Matcher matcherDate2 = date2.matcher(sum);
			if( matcherDate1.find() ) {
				System.out.println("Consider \""+sum+"\" contains date with format __-__-yyyy.");
				/* dateCase 0: MM-DD-YYYY
				 * dateCase 1: DD-MM-YYYY
				 * */
				int dateCase;
				int[] nums = new int[3];
				int j = 0;
				Matcher matcherNum = number.matcher( matcherDate1.group(0) );
				while( matcherNum.find() ) {
					nums[j++] = Integer.valueOf( matcherNum.group(0) );
				}
				if( nums[0] > 12 ) {
					dateCase = 1;
				} else if( nums[1] > 12 ) {
					dateCase = 0;
				} else if( dateFromImage != null ) {
					
					Long dateInImage = dateFromImage.getTime();
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
					Long monthFirst = sdf.parse( String.format( "%02d", nums[0])
							+"/" + String.format( "%02d", nums[1] )
							+ "/" + String.format( "%4d", nums[2] )).getTime();
					Long dayFirst = sdf.parse( String.format( "%02d", nums[1])
							+"/" + String.format( "%02d", nums[0] )
							+ "/" + String.format( "%4d", nums[2] )).getTime();
					
					if( Math.abs( monthFirst - dateInImage) < Math.abs( dayFirst - dateInImage) ) {
						dateCase = 0;
					//	Integer month = nums[0];
					//	Integer day = nums[1];
					} else {
					
						dateCase = 1;
					}
				} else 
					dateCase = 0;
				
				String patch = "";
				
				if( nums[2] < 100 ) {
					/* 2000 ~ 2079 */
					patch = "20";
				}
				switch( dateCase ) {
				case 0:
					result = String.format( "%02d", nums[0])
										+"-" + String.format( "%02d", nums[1] )
										+ "-" + patch + nums[2] ;
					System.out.println( "Date---->" + result);
					return result;
				default:
					result =  String.format( "%02d", nums[1] )
										+"-" + String.format( "%02d", nums[0] )
										+ "-" + patch + nums[2];
					System.out.println( "Date---->" + result );
					return result;
				}
				
			
			} else if( matcherDate2.find() ) {
				System.out.println("Consider \""+sum+"\" contains date with format 01-Apr-2014");
				Matcher matcherWord = word.matcher( matcherDate2.group(0));
				System.out.println( "matcherDate2.group(0)" +matcherDate2.group(0));
				matcherWord.find();
				String month = matcherWord.group(0);
				
				System.out.println("month: " + month);
				if( month.toLowerCase().matches("jan.*") ) {
					month = "01";
				} else if( month.toLowerCase().matches("feb.*") ) {
					month = "02";
				} else if( month.toLowerCase().matches("mar.*") ) {
					month = "03";
				} else if( month.toLowerCase().matches("apr.*") ) {
					month = "04";
				} else if( month.toLowerCase().matches("may.*") ) {
					month = "05";
				} else if( month.toLowerCase().matches("jun.*") ) {
					month = "06";
				} else if( month.toLowerCase().matches("jul.*") ) {
					month = "07";
				} else if( month.toLowerCase().matches("aug.*") ) {
					month = "08";
				} else if( month.toLowerCase().matches("sep.*") ) {
					month = "09";
				} else if( month.toLowerCase().matches("oct.*") ) {
					month = "10";
				} else if( month.toLowerCase().matches("nov.*") ) {
					month = "11";
				} else if( month.toLowerCase().matches("dec.*") ) {
					month = "12";
				} else
					return "";
				
				Matcher matcherNum = number.matcher( matcherDate2.group(0) );
				matcherNum.find();
				String day = String.format("%02d",Integer.valueOf( matcherNum.group(0)));
				matcherNum.find();
				String patch ="";
				
				if( Integer.valueOf( matcherNum.group(0)) < 80 ) {
					/* 2000 ~ 2079 */
					patch = "20";
				} else if( Integer.valueOf( matcherNum.group(0)) <100 ) {
					/* 1980 ~ 1999 */
					patch = "19";
				} 

				String year = patch + matcherNum.group(0);	
				result = month + "-"+ day +"-" + year;
				System.out.println("~~~~Found date: " + result);
				return result;
			} else
				return "";
		} catch( Exception e ) {
			System.out.println("Exception occurs when extracting date from receipt: \n\t" + e +" at line"+ e.getStackTrace()[0].getLineNumber()  );
			return "";
		}
		
	}
	
	/* 
	 * Check if there is time in current line,
	 * and return date in "HH:MM:SS" format. 
	 * */
	private String getTime ( List<DetailBox> line ) {
		try{
			String sum = "";
			for( int i = 0; i < line.size(); i++ ) {
				sum += line.get(i).getDescription() + " ";
			}
			Pattern longPattern = Pattern.compile( "[0-9]{1,2}: ?[0-9]{2}: ?[0-9]{2} ?(p|a|P|A)?" );
			Pattern shortPattern = Pattern.compile( "[0-9]{1,2}: ?[0-9]{1,2} ?(p|a|P|A)?" );
			Pattern number = Pattern.compile("[0-9]{1,2}");
			Matcher longMatcher = longPattern.matcher( sum );
			Matcher shortMatcher = shortPattern.matcher( sum );
			boolean isAfternoon = false;
			
			if( longMatcher.find() ) {
				System.out.println("Consider \""+ sum +"\" contains time with format 00:00:00");
				if( longMatcher.group(0).toLowerCase().contains("p"))
					isAfternoon = true;
				
				String[] splitedTime = longMatcher.group(0).split(":");
				Matcher hourMatch = number.matcher( splitedTime[0]);
				Matcher minuteMatch = number.matcher( splitedTime[1]);
				Matcher secondMatch = number.matcher( splitedTime[2]);
				hourMatch.find();
				minuteMatch.find();
				secondMatch.find();
				String result = String.format("%02d",(Integer.valueOf(hourMatch.group(0)) + (isAfternoon? 12:0 )))
									+":" + minuteMatch.group(0) +":" + secondMatch.group(0); 
				System.out.println("~~~~Found time: "+result);
				return result;
			} else if ( shortMatcher.find() ) {
				System.out.println("Consider \""+ sum +"\" contains time with format 00:00");
				if( shortMatcher.group(0).toLowerCase().contains("p"))
					isAfternoon = true;
				String[] splitedTime = shortMatcher.group(0).split(":");
				Matcher hourMatch = number.matcher( splitedTime[0]);
				Matcher minuteMatch = number.matcher( splitedTime[1]);
				hourMatch.find();
				minuteMatch.find();
				String result = String.format("%02d",(Integer.valueOf(hourMatch.group(0)) + (isAfternoon? 12:0 )))
									+":" + minuteMatch.group(0); 
				System.out.println("~~~~Found time: "+result);
				return result;
			} else
				return "";
		} catch( Exception e ) {
			System.out.println("Exception occurs when extracting time from receipt: \n\t" + e +" at line"+ e.getStackTrace()[0].getLineNumber()  );
			return "";
		}
		
	}
	private String getLocation( List<DetailBox> line ) {
		String onlyNumbers = "[0-9]+\\s";
		String containAlphanumeric = "^.*[a-zA-Z].*$";
		String certainPunctuations = "(.*)?(\\*|\\?|:|!|%|,|/).*";
		String sum = "";
		for( int i = 0; i < line.size(); i++ ) {
			sum += line.get(i).getDescription() + " ";
		}
		if( sum.matches( onlyNumbers )
				|| sum.toLowerCase().contains("receipt")
				|| sum.toLowerCase().contains("thank")
				|| !sum.matches( containAlphanumeric )
				|| sum.matches( certainPunctuations ) ) {
			return "0";
		}
		
		/* If the interval of descriptions upsurges,
		 * then this line is not considered to have name of stores */
		if( line.size() > 1) {
			int interval = line.get(1).getVertices()[0][0] - line.get(0).getVertices()[1][0];
			System.out.println("first interval: " + interval);
			for( int i = 1; i < line.size()-1 ; i++ ){
				int temp = line.get(i+1).getVertices()[0][0] - line.get(i).getVertices()[1][0];
				if( temp/interval > 5) {
					System.out.println("Find big interval after " + line.get(i).getDescription());
					return "0";
				}
			}
		}
		
		/* If there is "welcome to" remove it and return the description*/
		String arr[] = sum.split("(?i)welcome to ");
		return arr[ arr.length-1 ];
	}
	
	private List<DetailBox> sortBoxes( List<DetailBox> boxes) {
		Collections.sort( boxes, new Comparator<DetailBox>(){
			public int compare( DetailBox d1, DetailBox d2 ) {
				if( d1.getVertices()[0][0] > d2.getVertices()[0][0])
					return 1;
				else if( d1.getVertices()[0][0] < d2.getVertices()[0][0] )
					return -1;
				else
					return 0;
			}
		});
		return boxes;
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

	private String savePictureToDBAndServer( String uid, String rid, List<DetailBox> detailUnits, byte[] bytes) throws IOException {
		Picture picture = new Picture();
		picture.setOwnerId(uid);
		picture.setReceiptId(rid);
		picture.setDetailUnits(detailUnits);
        picture = pictureService.save(picture);
        String pid = picture.getId();
        
        Receipt receipt = receiptService.findOne(rid);
        if( receipt.getPicId() == null ) {
        	receipt.setPicId(pid);
    	} else {
    		String oldPid = receipt.getPicId();
    		pictureService.delete( uid, oldPid );
    		receipt.setPicId( pid );
    	}
		receiptService.update(receipt);
		System.out.println("Set receipt: " + receipt.getId() + " with picture: " + pid);
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

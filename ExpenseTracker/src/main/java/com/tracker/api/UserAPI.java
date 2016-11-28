package com.tracker.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tracker.domain.receipt.Item;
import com.tracker.domain.receipt.Receipt;
import com.tracker.domain.users.Role;
import com.tracker.domain.users.User;
import com.tracker.services.ReceiptsService;


@Controller
@RequestMapping("/user")
@Secured("ROLE_USER")
public class UserAPI {

	@Autowired
	private ReceiptsService receiptService;
	
	@RequestMapping( value="", method=RequestMethod.GET )
	public String getAdminHome(	@AuthenticationPrincipal User user, Model model ) {
		System.out.println("Returning user's home page");
		return "redirect:#/user";
	}
	
	/* User browser all his receipts */
	@RequestMapping( value="/{uid}/receipt", method=RequestMethod.GET )
	@ResponseBody
	public Page<Receipt> getReceipts(	@AuthenticationPrincipal User user ,
										@PathVariable String uid, Model model,
										@RequestParam( required=false, defaultValue="0" ) String page,
										@RequestParam( required=false, defaultValue="10" ) String size) {
		
		Pageable pageable = new PageRequest(  Integer.valueOf( page ), Integer.valueOf( size ) );
		Page<Receipt> result = receiptService.findByOwnerId(user.getId(), pageable);
		return result;
	}
	
	/* User create a receipt */
	@RequestMapping( value="/{uid}/receipt", method=RequestMethod.POST )
	@ResponseBody
	public Receipt createReceipts( 	@AuthenticationPrincipal User user ,
									@PathVariable String uid, Model model,
									@RequestBody MultiValueMap<String, String> data) {
		System.out.println("Creating receipt with time: "+ data.get("time").get(0)
				+" place: " +data.get("place").get(0)
				);
		
		
		Receipt receipt = new Receipt();
		receipt.setOwnerId( user.getId() );

		List<Item> list_of_items = new ArrayList<Item>();	
		if( data.containsKey("items") ) {
			int numberOfItems = data.get("items").size();
			for( int i=0; i< numberOfItems; i++ ) {
				// Not sure what the data structure is like
			}
			receipt.setList_of_items(list_of_items);
		} else {
			
		}
		
		receipt.setNote( data.get("note").get(0));
//			receipt.setTags();
		receipt.setTotal( Double.valueOf( data.get("total").get(0) ) );
		
		
//		receiptService.save(receipt);
		return null;
	}
		
}

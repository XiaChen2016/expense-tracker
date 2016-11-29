var tracker = angular.module('Tracker', [ 'ngRoute', 'ngResource' ])

tracker.factory('userService',function(){

	var user = {};
	var editUser = {};
	var receipt = {};
	var searchKeyWord = "blank";

	return{
		getUser: function(){return user;},
		setUser: function(currentUser){user = currentUser;},
		getEditUser: function(){return editUser;},
		setEditUser: function(currentUser){editUser = currentUser;},
		getEditReceipt: function(){return receipt;},
		setEditReceipt: function(r){receipt = r;},
		getSearchKey: function(){return searchKeyWord;},
		setSearchKey: function(key){searchKeyWord = key;}
	};
}

)



tracker.controller('Login.Controller', ['$scope', 'userService', function( $scope,userService ) {

   $scope.login = function() {
     var url = '/login';
     var username = $scope.username;
     var password = $scope.password;

     $.ajax( url, {  type : 'POST',data:{
       username : username,
       password : password
     },success : function( loggedUser, responseHeaders ){
    	 if(loggedUser.username){

         $scope.user = loggedUser;
         console.log( $scope.user.admin  );
         userService.setUser(loggedUser);
         console.log("Username: " + userService.getUser().username);

           if($scope.user.admin){
             console.log("usertype is admin");
             window.location.href = '/#/admin';
           }
           else {
             console.log("usertype is not admin");

             window.location.href = '/#/user';
           }
    	 }
    	 else{
             alert('Incorrect username or password. Please try again.', 'ERROR');
             }}} );

   }

} ] );




tracker.controller('adminHome.Controller', ['$scope', '$resource','userService','$filter', function( $scope, $resource, userService,$filter ) {
	$scope.currentPage = 0;
	$scope.userPerPage = 10;

	$scope.sizeList = [5,10,20];


  $scope.logout = function(){
    $.ajax('/logout',{type : 'POST'});
    window.location.href = '/#/';
  }

  var updateListOfUser = function ( result ) {

    console.log("updateListOfUser");
    $scope.userList = result.content;
	$scope.totalPage = result.totalPages;
	$scope.responseContent = result;
	$scope.currentPage = result.number;
	$scope.userPerPage = result.size;
    $scope.$digest();
  }

  var getCurrentUser = function(){
	  console.log("name = " +userService.getUser().username )
	  if(userService.getUser().username)
	  {
	    $scope.user = userService.getUser();
	    getUserList();
	  }
	  else
	  {
		$.ajax(  '/home',{ type : 'GET', success: function( loggedUser, responseHeaders ){


	             $scope.user = loggedUser;
	             userService.setUser(loggedUser);
	             console.log("Username: " + userService.getUser().username);
	             getUserList();

		}});

	  }
	    }

  var getUserList = function(){
    console.log("hello from getUserList!");
    $.ajax(  '/admin/'+ $scope.user.id+'/users?size='+$scope.userPerPage+ '&page='+$scope.currentPage,{ type : 'GET', success: updateListOfUser  });
}

  getCurrentUser();
	userService.setEditUser($scope.user);



//-----------------------------search--------------------------------
$scope.searchBy = function(searchKey){
	if(searchKey == "name")
		$scope.searchType = "name";
	if(searchKey == "email")
		$scope.searchType = "email";

	getUserListWithSearch();

}
$scope.searchByAdmin = function(){

		$scope.searchType = "isAdmin";
		$scope.searchKeyWord = "true";
		  $.ajax(  '/admin/'+ $scope.user.id+'/users?size='+$scope.userPerPage+
		    		'&page='+$scope.currentPage+
		    		'&'+$scope.searchType+'=true',{ type : 'GET', success: updateListOfUser });

}
$scope.searchByUser = function(){

	$scope.searchType = "isAdmin";
	$scope.searchKeyWord = "false";
	  $.ajax(  '/admin/'+ $scope.user.id+'/users?size='+$scope.userPerPage+
	    		'&page='+$scope.currentPage+
	    		'&'+$scope.searchType+'=false',{ type : 'GET', success: updateListOfUser });

}
var getUserListWithSearch = function(){
    console.log("hello from getUserListWithSearch");
    $.ajax(  '/admin/'+ $scope.user.id+'/users?size='+$scope.userPerPage+
    		'&page='+$scope.currentPage+
    		'&'+$scope.searchType+'='+$scope.searchKeyWord,{ type : 'GET', success: updateListOfUser });
}


//----------------------------paging------------------------

$scope.prevPage = function(){
	if($scope.searchType)
	$.ajax(  '/admin/'+ $scope.user.id+'/users?size='+$scope.userPerPage+
			'&page='+ ($scope.currentPage - 1)+
			'&'+$scope.searchType+'='+$scope.searchKeyWord,{ type : 'GET', success: updateListOfUser  });
	else
		$.ajax(  '/admin/'+ $scope.user.id+'/users?size='+$scope.userPerPage+
			'&page='+ ($scope.currentPage - 1),{ type : 'GET', success: updateListOfUser  });

}

$scope.nextPage = function(){
	if($scope.searchType)
		$.ajax(  '/admin/'+ $scope.user.id+'/users?size='+$scope.userPerPage+
				'&page='+ ($scope.currentPage + 1)+
				'&'+$scope.searchType+'='+$scope.searchKeyWord,{ type : 'GET', success: updateListOfUser  });
		else
			$.ajax(  '/admin/'+ $scope.user.id+'/users?size='+$scope.userPerPage+
				'&page='+ ($scope.currentPage + 1),{ type : 'GET', success: updateListOfUser  });
}

$scope.setPage = function()
{
	var page = document.getElementById("targetPage").value - 1;
	if(Number.isInteger(page)&& page<= $scope.totalPage && page>=0)
		{
			if($scope.searchType)
			{
				$scope.currentPage = page;
				$.ajax(  '/admin/'+ $scope.user.id+'/users?size='+$scope.userPerPage+
						'&page='+ $scope.currentPage+
						'&'+$scope.searchType+'='+$scope.searchKeyWord,{ type : 'GET', success: updateListOfUser  });

			}
			else{
			$scope.currentPage = page;
			$.ajax(  '/admin/'+ $scope.user.id+'/users?size='+$scope.userPerPage+ '&page='+$scope.currentPage,{ type : 'GET', success: updateListOfUser  });

			}
		}
	else
	alert('Please type in right number', 'ERROR');

}

$scope.setSize = function(){
	if($scope.searchType)
	getUserListWithSearch();
	else
	getUserList();
}

//--------------------------edit user---------------------------

$scope.editUser = function(user){
	userService.setEditUser(user);
	window.location.href = '/#/editUser';
}

//-------------------------update role---------------------------------

$scope.updateRole = function(id,role){
	if(role)var isAdmin = true;
	else var isAdmin = false;
	$.ajax( {
			 url : '/admin/'+ $scope.user.id +'/users/'+id+'/isAdmin',
			 type : 'PUT',
			 contentType: "application/json; charset=utf-8",
			 data : ""+ isAdmin
		} );

	}


//-----------------------active status---------------------------------

$scope.activeUser = function(id,status){
	if(status == "enabled")
	{
		$.ajax( {
				 url : '/admin/'+ $scope.user.id +'/users/'+id+'/enable',
				 type : 'PUT',
			} );
	}
	else
	{
		$.ajax( {
				 url : '/admin/'+ $scope.user.id +'/users/'+id+'/disable',
				 type : 'PUT',
			} );
	}

	}


} ] );




tracker.controller('createUser.Controller', ['$scope', '$resource','userService', function( $scope, $resource, userService ) {

	  var getCurrentUser = function(){
		    $scope.user = userService.getUser;
		    }

	  $scope.logout = function(){
		    $.ajax('/logout',{type : 'POST'});
		    window.location.href = '/#/';
		  }

    $scope.createUser = function(){
    	if($scope.newUserType)
    		var isAdmin = true;
    	else
    		var isAdmin = false;
      $.ajax( {
           url : '/admin/'+ $scope.user.id +'/users',
           type : 'POST',

           data : { username: $scope.newUserName,
                    name: $scope.newName,
                    password: $scope.newPasword,
                    newPhoneNumber : $scope.newPhoneNumber,
                    email: $scope.newEmailAddress,
                    isAdmin : isAdmin
           },
           success:function(){
             window.location.href = '/#/admin'
           }
        } );
    }
    getCurrentUser();

} ] );


tracker.controller('editUser.Controller', ['$scope', 'userService', function( $scope, userService ) {

    var editUser = userService.getEditUser();
    	$scope.user = userService.getUser();
    	$scope.newUserName = editUser.username;
    	$scope.newEmailAddress = editUser.email;
		$scope.newName = editUser.name;
		$scope.newUserType = editUser.admin;

		if(editUser.phone)$scope.newPhoneNumber = editUser.phone;
		else $scope.newPhoneNumber = ["0"];


		$scope.PN =
		{
				add: function(){
					$scope.newPhoneNumber.push(" ");
				},

				del: function(key){
					$scope.newPhoneNumber.splice(key,1);
				}
		}


		$scope.confirmEditUser = function(){
    	if($scope.newUserType)
    		var isAdmin = true;
    	else
    		var isAdmin = false;


      $.ajax( {
           url : '/admin/'+ $scope.user.id +'/users/' + editUser.id,
           type : 'POST',

           data : {
					id:editUser.id,
					username: editUser.username,
                    name: $scope.newName,
                    newPhoneNumber : $scope.newPhoneNumber,
                    email: editUser.email,
                    isAdmin : isAdmin
           },
           success:function(){
             window.location.href = '/#/admin'
           }
        } );
    }


} ] );



tracker.controller('userHome.Controller', ['$scope', '$resource','userService', function( $scope, $resource, userService ) {
	$scope.currentPage = 0;
	$scope.userPerPage = 10;

	$scope.sizeList = [5,10,20];


  $scope.logout = function(){
    $.ajax('/logout',{type : 'POST'});
    window.location.href = '/#/';
  }

  var updateListOfReceipt = function ( result ) {

    console.log("updateListOfUser");
    $scope.receiptList = result.content;
	$scope.totalPage = result.totalPages;
	$scope.responseContent = result;
	$scope.currentPage = result.number;
	$scope.userPerPage = result.size;
    $scope.$digest();
  }

  var getCurrentUser = function(){
	  console.log("name = " +userService.getUser().username )
	  if(userService.getUser().username)
	  {
	    $scope.user = userService.getUser();
	    getReceiptList();
	  }
	  else
	  {
		$.ajax(  '/home',{ type : 'GET', success: function( loggedUser, responseHeaders ){


	             $scope.user = loggedUser;
	             userService.setUser(loggedUser);
	             console.log("Username: " + userService.getUser().username);
	             getReceiptList();

		}});

	  }
	    }

  var getReceiptList = function(){
    console.log("hello from getUserList!");
    $.ajax(  '/user/'+ $scope.user.id+'/receipt?size='+$scope.userPerPage+ '&page='+$scope.currentPage,{ type : 'GET', success: updateListOfReceipt  });
}

  getCurrentUser();
	userService.setEditUser($scope.user);



//-----------------------------search--------------------------------
$scope.searchBy = function(searchKey){
	var url = "";
	if($scope.receiptLocation)
		url = url + "&location=" + $scope.receiptLocation;
	if($scope.receiptCategory)
		url = url + "&project=" = $scope.receiptCategory;
	if($scope.receiptTag)
		url = url + "&tag=" = $scope.receiptTag;
	
	userService.setSearchKey(url);

	getUserListWithSearch();

}

var getUserListWithSearch = function(){
    console.log("hello from getUserListWithSearch");
    $.ajax(  '/admin/'+ $scope.user.id+'/users?size='+$scope.userPerPage+
    		'&page='+$scope.currentPage+
    		userService.getSearchKey,{ type : 'GET', success: updateListOfUser });
}


//----------------------------paging------------------------

$scope.prevPage = function(){
	if(userService.getSearchKey)
	$.ajax(  '/admin/'+ $scope.user.id+'/receipt?size='+$scope.userPerPage+
			'&page='+ ($scope.currentPage - 1)+
			userService.getSearchKey,{ type : 'GET', success: updateListOfUser  });
	else
		$.ajax(  '/user/'+ $scope.user.id+'/receipt?size='+$scope.userPerPage+
			'&page='+ ($scope.currentPage - 1),{ type : 'GET', success: updateListOfReceipt  });

}

$scope.nextPage = function(){
	if(userService.getSearchKey)
		$.ajax(  '/admin/'+ $scope.user.id+'/receipt?size='+$scope.userPerPage+
				'&page='+ ($scope.currentPage + 1)+
				userService.getSearchKey,{ type : 'GET', success: updateListOfUser  });
		else
			$.ajax(  '/user/'+ $scope.user.id+'/receipt?size='+$scope.userPerPage+
				'&page='+ ($scope.currentPage + 1),{ type : 'GET', success: updateListOfReceipt  });
}

$scope.setPage = function()
{
	var page = document.getElementById("targetPage").value - 1;
	if(Number.isInteger(page)&& page<= $scope.totalPage && page>=0)
		{
			if(userService.getSearchKey)
			{
				$scope.currentPage = page;
				$.ajax(  '/user/'+ $scope.user.id+'/receipt?size='+$scope.userPerPage+
						'&page='+ $scope.currentPage+
						userService.getSearchKey,{ type : 'GET', success: updateListOfUser  });

			}
			else{
			$scope.currentPage = page;
			$.ajax(  '/user/'+ $scope.user.id+'/receipt?size='+$scope.userPerPage+ '&page='+$scope.currentPage,{ type : 'GET', success: updateListOfReceipt  });

			}
		}
	else
	alert('Please type in right number', 'ERROR');

}

$scope.setSize = function(){
	if(userService.getSearchKey)
	getUserListWithSearch();
	else
	getReceiptList();
}

//--------------------------edit/view receipt---------------------------

$scope.viewDetail = function(r){
	userService.setEditReceipt(r);
	window.location.href = '/#/editReceipt';
}

} ] );


tracker.controller('editReceipt.Controller', ['$scope', 'userService', function( $scope, userService ) {

    var receipt = userService.getEditReceipt();
    	$scope.user = userService.getUser();
    	$scope.receiptID = receipt.id;
			var date = new Date(receipt.time);
    	$scope.receiptTime = date;
		$scope.receiptLocation = receipt.place;
		$scope.receiptCategory = receipt.project;
		$scope.receiptNote = receipt.note;

		if(receipt.list_of_items)$scope.items = receipt.list_of_items;
		else $scope.items = [{name:" ",quantity:" ",price:" "}];

		if(receipt.tags)$scope.tags = receipt.tags;
		else $scope.tags = [" "];


		$scope.editItem =
		{
				add: function(){
					$scope.items.push({name:" ",quantity:" ",price:" "});
				},

				del: function(key){
					$scope.items.splice(key,1);
				}
		}

		$scope.tag =
		{
				add: function(){
					$scope.tags.push(" ");
				},

				del: function(key){
					$scope.tags.splice(key,1);
				}
		}


		Number.prototype.padLeft = function(base,chr){
			   var  len = (String(base || 10).length - String(this).length)+1;
			   return len > 0? new Array(len).join(chr || '0')+this : this;
			}
		
		
		$scope.confirmEditUser = function(){
			var d = $scope.receiptTime;
			var dformat = [ (d.getMonth()+1).padLeft(),
		                    d.getDate().padLeft(),
		                    d.getFullYear()].join('-')+
		                    ' ' +
		                  [ d.getHours().padLeft(),
		                    d.getMinutes().padLeft(),
		                    d.getSeconds().padLeft()].join(':');
			var total = 0;
			var x;
			for(x in $scope.items){
				total += $scope.items[x].quantity * $scope.items[x].price;
			}


   $.ajax( {
        url : '/user/'+ $scope.user.id +'/receipt/'+$scope.receiptID,
        type : 'POST',

        data : {
						 				time:dformat,
						 				place: $scope.receiptLocation,
                 project: $scope.receiptCategory,
                 note : $scope.receiptNote,
                 category: $scope.tags,
                 list_of_items : $scope.items,
                 total : total
        },
        success:function(){
          window.location.href = '/#/user'
        }
     } );
 }


		$scope.cancel = function(){
    	window.location.href = '/#/user'
    }




} ] );





tracker.controller('createReceipt.Controller', ['$scope', 'userService', function( $scope, userService ) {


    	$scope.user = userService.getUser();

			var date = new Date();
    $scope.receiptTime = date;
		$scope.receiptLocation = " ";
		$scope.receiptCategory = " ";
		$scope.receiptNote = " ";

 		$scope.items = [{name:" ",quantity:" ",price:" "}];

		$scope.tags = [" "];


		$scope.editItem =
		{
				add: function(){
					$scope.items.push({name:" ",quantity:" ",price:" "});
				},

				del: function(key){
					$scope.items.splice(key,1);
				}
		}

		$scope.tag =
		{
				add: function(){
					$scope.tags.push(" ");
				},

				del: function(key){
					$scope.tags.splice(key,1);
				}
		}

Number.prototype.padLeft = function(base,chr){
			   var  len = (String(base || 10).length - String(this).length)+1;
			   return len > 0? new Array(len).join(chr || '0')+this : this;
			}
		
		
		$scope.confirmEditUser = function(){
			var d = $scope.receiptTime;
			var dformat = [ (d.getMonth()+1).padLeft(),
		                    d.getDate().padLeft(),
		                    d.getFullYear()].join('-')+
		                    ' ' +
		                  [ d.getHours().padLeft(),
		                    d.getMinutes().padLeft(),
		                    d.getSeconds().padLeft()].join(':');
			var total = 0;
			var x;
			for(x in $scope.items){
				total += $scope.items[x].quantity * $scope.items[x].price;
			}


      $.ajax( {
           url : '/user/'+ $scope.user.id +'/receipt',
           type : 'POST',

           data : {
						 				time:dformat,
						 				place: $scope.receiptLocation,
                    project: $scope.receiptCategory,
                    note : $scope.receiptNote,
                    category: $scope.tags,
                    list_of_items : $scope.items,
                    total : total
           },
           success:function(){
             window.location.href = '/#/user'
           }
        } );
    }


		$scope.cancel = function(){
    	window.location.href = '/#/user'
    }




} ] );

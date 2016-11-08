var tracker = angular.module('Tracker', [ 'ngRoute', 'ngResource' ])

tracker.factory('userService',function(){

	var user = {};
	var editUser = {};

	return{
		getUser: function(){return user;},
		setUser: function(currentUser){user = currentUser;},
		getEditUser: function(){return editUser;},
		setEditUser: function(currentUser){editUser = currentUser;}
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
			 type : 'POST',

			 data : { isAdmin : isAdmin}
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



tracker.controller('userHome.Controller', ['$scope', '$resource', function( $scope, $resource ) {

    $.ajax( {
       url : '/user',
       type : 'GET',
       success: function(response) { $scope.user = response; $scope.$digest();}

  });


} ] );

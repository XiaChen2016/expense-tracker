var tracker = angular.module('Tracker', [ 'ngRoute', 'ngResource' ])



tracker.controller('Login.Controller', ['$scope', '$resource', function( $scope, $resource ) {
   var Login = $resource('/login');
   $scope.login = function( ) {
     var url = '/login';
     var username = $scope.username;
     var password = $scope.password;

     $.ajax( url, {  type : 'POST',data:{
       username : username,
       password : password
     },success : function( loggedUser, responseHeaders ){
    	 if(loggedUser.username){
           
    	 for (j = 0 ; j < loggedUser.roles.length; j++)
         {

             if(loggedUser.roles[j].getName == "ROLE_ADMIN")
             loggedUser.userType = true;

         }
    	 
         $scope.user = loggedUser;
         console.log( $scope.user.admin  );
         
           if($scope.user.admin){
             console.log("usertype is admin");
             window.location.href = '/#/admin';
           }
           else {
             console.log("usertype is not admin");
          
             window.location.href = '/#/user';
           }
         $scope.user = loggedUser;
//         console.log( $scope.user.username  )
    	 }
    	 else{
             alert('Incorrect username or password. Please try again.', 'ERROR');
             }}} );

   }

} ] );




tracker.controller('adminHome.Controller', ['$scope', '$resource', function( $scope, $resource ) {

  var updateListOfUser = function ( result ) {
    console.log("updateListOfUser");
      // console.log("typeof result is:"+ typeof result+"  result:"+ result);

    $scope.userList = result;
    $scope.$digest();
  }

  var getCurrentUser = function(){
	    $.ajax(  '/home',{ type : 'GET', success: function(loggedUser, responseHeaders){
	    	$scope.user = loggedUser;
	    	getUserList();
	    }
	    
	    } );
	    }
	
  var getUserList = function(){
    console.log("hello from getUserList!");
    $.ajax(  '/admin/'+ $scope.user.id+'/users',{ type : 'GET', success: updateListOfUser  });
}

  getCurrentUser();

} ] );




tracker.controller('createUser.Controller', ['$scope', '$resource', function( $scope, $resource ) {

	  var getCurrentUser = function(){
		    $.ajax(  '/home',{ type : 'GET', success: function(loggedUser, responseHeaders){
		    	$scope.user = loggedUser;
		    }
		    
		    } );
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






tracker.controller('userHome.Controller', ['$scope', '$resource', function( $scope, $resource ) {

    $.ajax( {
       url : '/user',
       type : 'GET',
       success: function(response) { $scope.user = response; $scope.$digest();}

  });


} ] );

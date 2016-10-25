var tracker = angular.module('Tracker', [ 'ngRoute', 'ngResource' ])

tracker.factory('userService',function(){
	
	var user = {};
	
	return{
		getUser: function(){return user;},
		setUser: function(currentUser){user = currentUser;}
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
	$scope.userPerPage = 2;
	
	
  $scope.logout = function(){
    $.ajax('/logout',{type : 'POST'});
  }

  var updateListOfUser = function ( result ) {
    console.log("updateListOfUser");
      // console.log("typeof result is:"+ typeof result+"  result:"+ result);

    $scope.userList = result.content;
    $scope.$digest();
  }

  var getCurrentUser = function(){
	  console.log("name = " +userService.getUser().username )
	    $scope.user = userService.getUser();
	    getUserList();
	    } 

  var getUserList = function(){
    console.log("hello from getUserList!");
    $.ajax(  '/admin/'+ $scope.user.id+'/users?size='+$scope.userPerPage+ '&page='+$scope.currentPage,{ type : 'GET', success: updateListOfUser  });
}

  getCurrentUser();
  

//----------------------------paging------------------------


} ] );




tracker.controller('createUser.Controller', ['$scope', '$resource','userService', function( $scope, $resource, userService ) {

	  var getCurrentUser = function(){
		    $scope.user = userService.getUser;
		    }

    $scope.logout = function(){
          $.ajax('/logout',{type : 'POST'});
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

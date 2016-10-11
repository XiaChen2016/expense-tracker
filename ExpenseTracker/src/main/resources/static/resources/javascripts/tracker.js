var user = null;
var tracker = angular.module('Tracker', [ 'ngRoute', 'ngResource' ]);



tracker.controller('Login.Controller', ['$scope', '$resource', function( $scope, $resource ) {
   var Login = $resource('/login');
   $scope.login = function( ) {
     var url = '/login';
     var username = $scope.username;
     var password = $scope.password;

     $.ajax( url, {  type : 'POST',data:{
       username : username,
       password : password
     },success : function(loggedUser, responseHeaders){
      //  console.log("loggedUser.userType: "+ loggedUser.userType +" typeof it: "+ typeof loggedUser.userType);
      //  if(loggedUser.userType == "admin"){
      //    console.log("usertype is admin");
      //    window.location.href = '/#/adminHome';
      //  }
      //  else {
      //    console.log("usertype is not admin");
       //
      //    window.location.href = '/#/userHome';
      //  }
      user = loggedUser;
     }} );

   }

} ] );





tracker.controller('adminHome.Controller', ['$scope', '$resource', function( $scope, $resource ) {



  var updateListOfUser = function ( result ) {
    console.log("updateListOfUser");
    var i;
     for ( i = 0 ; i < result.length; i++ ) {
      if( result[i].userType == "user") {
            console.log( "result[i].role"+result[i].userType +" role = 0");
            result[i].userType = false;
          } else {
            console.log( "result[i].role"+result[i].userType  + "role !=0 ");
            result[i].userType = true;
          }
      }
      console.log("typeof result is:"+ typeof result+"  result:"+ result);
    $scope.userList = result;
    $scope.$digest();
  }


  var getUserList = function(){
    console.log("hello from getUserList!");
    $.ajax(  'api/admin'+$scope.user.userid+'/users',{ type : 'GET', success: updateListOfUser  });
}

getUserList();

} ] );




tracker.controller('createUser.Controller', ['$scope', '$resource', function( $scope, $resource ) {


    $scope.createUser = function(){
      if( $scope.newUserType == true) {
        var role = "Admin";
      } else {
        var role = "user";
      }
      $.ajax( {
           url : 'api/users/createUser',
           type : 'POST',

           data : { userName: $scope.newFirstName,
                    name: $scope.newName,
                    phoneNumber: $scope.newPhoneNumber,
                    password: $scope.newUserPassword,
                    email: $scope.newEmailAddress,
                    userType : role },
           success : getUserList
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

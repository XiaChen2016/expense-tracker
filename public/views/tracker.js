
var tracker = angular.module('Tracker', [ 'ngRoute', 'ngResource' ]);

var keyStr = "ABCDEFGHIJKLMNOP" + "QRSTUVWXYZabcdef" + "ghijklmnopqrstuv"
            + "wxyz0123456789+/" + "=";

    function encode64(input) {

        var output = "";
        var chr1, chr2, chr3 = "";
        var enc1, enc2, enc3, enc4 = "";
        var i = 0;
        do {
            chr1 = input.charCodeAt(i++);
            chr2 = input.charCodeAt(i++);
            chr3 = input.charCodeAt(i++);
            enc1 = chr1 >> 2;
            enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
            enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
            enc4 = chr3 & 63;
            if (isNaN(chr2)) {
                enc3 = enc4 = 64;
            } else if (isNaN(chr3)) {
                enc4 = 64;
            }
            output = output + keyStr.charAt(enc1) + keyStr.charAt(enc2)
                    + keyStr.charAt(enc3) + keyStr.charAt(enc4);
            chr1 = chr2 = chr3 = "";
            enc1 = enc2 = enc3 = enc4 = "";
        } while (i < input.length);

        return output;
    }


tracker.controller('Login.Controller', ['$scope', '$resource', function( $scope, $resource ) {
   var Login = $resource('/login');
   $scope.login = function( ) {
     var url = 'api/login';
     var securedUsername = encode64($scope.username);
     var securedPassword = encode64($scope.password);

     $.ajax( url, {  type : 'POST',headers:{
       "attached":securedUsername + '&' + securedPassword;
     },success : function(loggedUser, responseHeaders){
       console.log("loggedUser.userType: "+ loggedUser.userType +" typeof it: "+ typeof loggedUser.userType);
       if(loggedUser.userType == "admin"){
         console.log("usertype is admin");
         window.location.href = '/#/adminHome';
       }
       else {
         console.log("usertype is not admin");

         window.location.href = '/#/userHome';
       }
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

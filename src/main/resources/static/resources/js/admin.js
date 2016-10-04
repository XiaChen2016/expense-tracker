var user = null;

var updateUser = function( ) {
   $('#profile').text( user.username );
}

var createNewUser = function( ) {
    if( $('#newUserIsAdmin').is(':checked') ) {
        var role = "ROLE_ADMIN";
      } else { 
        var role = "ROLE_USER"; 
      }
    var username = $('#newUserName').val()
    var password = $('#newUserPassword').val();
 
   if( username.length <1 || password.length<1 || password.indexOf(" ") !=-1 || username.indexOf(" ") !=-1 ){
      alert("Please enter both username and password!");
    } else {
      $('#newUserName').val("");
      $('#newUserPassword').val("");
      $('#newUserIsAdmin').removeAttr( "checked" , false );
     var body = { username : username, password : password, role : role };
     var url = '/tasker/user';
     $.ajax( url, {  type : 'POST',  data : body, success : getAll, error: function( response ) {
		  if( response.status== 400) {
			  alert( "This user already exist!" );
		  }  else
			  redirectToLogin();
	  } } );
  
    }
}

var changeUserInfo = function( item ) {
	// change user's information
	var parentTR =  $(item).parents("tr");
	var username =  $(parentTR).find('td:eq(0)').children("input").val() ;
	var password = $(parentTR).find('td:eq(1)').children("input").val();
	var tid = $(item).parents("tr").attr("id");
	var checkbox =  $(parentTR).find('td:eq(2)').children("input");
	var role;
	if( checkbox.is(':checked') )
		role = "ROLE_ADMIN"
		else
			role = "ROLE_USER";
	var url = '/tasker/user/'+ tid;
	var body = { newUsername:username , password: password , role: role, tid:tid} ;
	  $.ajax( url, {  type : 'PUT', data: body , success : getAll, error: function( response ) {
		  if( response.status== 400) {
			  {
				  alert("Invalid username or password, please check if they are missing or duplicated!");
				  getAll();
			  }
		  }  else if(response.status== 403)
			  { 
				  alert("You can't edit Bilbo's information!");
				  getAll();
			  }
		  else
			  redirectToLogin();
	  } } );
}
var listenDelete = function(clickedItem) {
  var tid = $(clickedItem).parents("tr").attr("id");
  var url = '/tasker/user/' + tid;
  $.ajax( url, {  type : 'DELETE',  success : getAll, error: function( response ) {
	  if( response.status== 400) {
		  alert("Failed to delete user, this user may not exist.");
	  }  else if(  response.status== 403 )
		  alert( "You can't delete Bilbo.");
	  else
		  redirectToLogin();
  } } );
}

var redirectToLogin = function( xhr, msg, err ) {
   window.location.replace('/');   
}

var getPrincipal = function( ) {
	// get user's information
   $.ajax( '/api/user',
           { type : 'GET',
             success : function( apiUser ) { user = apiUser; updateUser();getAll();   },
             error : redirectToLogin
           } );
   
}

var updateResult = function( result ) {
 $('#resultTable').text("");
 $('#resultTable').append("<thead style = \" font-weight:bold;\" ><td>Username</td><td>Password</td>  <td>Admin</td><td></td></thead> <tbody>");
  for(var i=0 ; i<result.length; i++){
	  if(result[i].username == user.username)
		  continue
    if( Boolean(result[i].roles[0].name ==  "ROLE_ADMIN") ){
        $('#resultTable').append(
            "<tr id=\"" + result[i].id + "\"> <td> <input type=\"text\" value=\"" + result[i].username + "\" style=\"border:none;\" onblur=\"changeUserInfo(this)\"></input></td>" +
            "<td> <input type=\"text\" value=\"" + result[i].password + "\" style=\"border:none;\" onblur=\"changeUserInfo(this)\"></input></td>"+
            "<td><input type=\"checkbox\" onchange=\"changeUserInfo(this)\" checked ></td>" +
            "<td><button class=\"btn btn-danger\" type=\"button\"onclick=\"listenDelete(this)\"><span class=\"glyphicon glyphicon-trash\"></span></button></td> </tr>" 
         );    
      }else {
        $('#resultTable').append(
        		 "<tr id=\"" + result[i].id + "\"> <td> <input type=\"text\" value=\"" + result[i].username + "\" style=\"border:none;\" onblur=\"changeUserInfo(this)\"></input></td>" +
                 "<td> <input type=\"text\" value=\"" + result[i].password + "\" style=\"border:none;\" onblur=\"changeUserInfo(this)\"></input></td>"+
                 "<td><input type=\"checkbox\" onchange=\"changeUserInfo(this)\" ></td>" +
                 "<td><button class=\"btn btn-danger\" type=\"button\"onclick=\"listenDelete(this)\"><span class=\"glyphicon glyphicon-trash\"></span></button></td> </tr>" 
        ) ;
      }
  }
  $('#resultTable').append("</tbody>");

}


var getAll = function() {
	// Get all users
   var url = 'tasker/users';
   $.ajax( url, {  type : 'GET',  success : updateResult,error : redirectToLogin  } );
}

var logout = function() {
   user = null;
   var url = '/logout';
   $.ajax( url, { type : 'GET', success : redirectToLogin, error : redirectToLogin } );
}


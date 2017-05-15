var tracker = angular.module('Tracker', [ 'ngRoute', 'ngResource', 'ngTagsInput' ])

tracker.factory('userService',function(){
	var user = {};
	var editUser = {};
	var receipt = {};

	return{
		getUser: function(){return user;},
		setUser: function(currentUser){user = currentUser;},
		getEditUser: function(){return editUser;},
		setEditUser: function(currentUser){editUser = currentUser;}
	};
});

tracker.factory('pagingService',function(){
	var currentPage = {};
	var size = {};
	var sizeList = [5,10,20];

	return{
		getSize: function(){return size;},
		setSize: function(targetSize){size = targetSize;},
		getCurrentPage: function(){return currentPage;},
		setCurrentPage: function(page){currentPage = page;},
		getSizeList: function(){return sizeList;}
	};
});

tracker.factory('receiptService',function(){
	var receipt = {};

	return{
		getEditReceipt: function(){return receipt;},
		setEditReceipt: function(r){receipt = r;}
	};
});

tracker.factory('searchService',function(){
	var state = { username :null, email: null, name : null, admin : null };
	var flag = 0;
	return{
		getState: function(){return state;},
		setState: function(s){state = s;},
		getFlag: function(){return flag;},
		setFlag: function(f){flag = f;}
	};
});

tracker.factory('Receipts', function($resource) {
	return $resource('/admin/:uid/users/:id', { },{ 'update' : { method : 'PUT'}});
} );

tracker.factory('Users', function($resource) {
	return $resource('/admin/:aid/users/:uid/:active/:isAdmin',{ }, { 'update' : { method : 'PUT'}});
} );

tracker.factory('home', function($resource) {
	return $resource('/home');
} );

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

tracker.controller('adminHome.Controller', ['$scope', '$resource','userService','pagingService','Users','home', 'searchService', function( $scope, $resource, userService,pagingService,Users,home, searchService ) {
	pagingService.setCurrentPage(0);
	pagingService.setSize(10);
	$scope.currentPage = pagingService.getCurrentPage();
	$scope.userPerPage = pagingService.getSize();
	$scope.sizeList = pagingService.getSizeList();
	console.log( $scope.search );
	$scope.logout = function(){
		$.ajax('/logout',{type : 'POST'});
		window.location.href = '/#/';
	}

	var updateListOfUser = function ( result ) {
		console.log("updateListOfUser");
		$scope.responseContent = result;
		$scope.userList = result.content;
		$scope.totalPage = result.totalPages;
		$scope.currentPage = result.number;
		pagingService.setCurrentPage(result.number);
		$scope.userPerPage = result.size;
		pagingService.setSize(result.size);
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
			home.get(function(loggedUser){
				$scope.user = loggedUser;
				userService.setUser(loggedUser);
				userService.setEditUser(loggedUser);
				console.log("Username: " + userService.getUser().username);
				getUserList();
			});	
		}
	}

	var getUserList = function(){
		console.log("hello from getUserList!");
		Users.get({ aid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() },updateListOfUser);
	}
	getCurrentUser();
	
//	-----------------------------search--------------------------------
	$scope.searchUsers = function(){
		var state = {username: $scope.search.username, email: $scope.search.email, name : $scope.search.name, admin : $scope.search.admin};
		searchService.setState(state);
		Users.get({aid : $scope.user.id, page : 0 , size : pagingService.getSize() ,username: $scope.search.username, email: $scope.search.email, name : $scope.search.name, isAdmin : $scope.search.admin} ,updateListOfUser );
	}
//	----------------------------paging------------------------

	$scope.prevPage = function(){
		pagingService.setCurrentPage(pagingService.getCurrentPage() - 1);
		Users.get({aid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() ,username: searchService.getState().username, email: searchService.getState().email, name : searchService.getState().name, isAdmin : searchService.getState().admin} ,updateListOfUser );
	}

	$scope.nextPage = function(){
		pagingService.setCurrentPage(pagingService.getCurrentPage() + 1);
		Users.get({aid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() ,username: searchService.getState().username, email: searchService.getState().email, name : searchService.getState().name, isAdmin : searchService.getState().admin} ,updateListOfUser );
	}

	$scope.setPage = function()
	{
	var page = document.getElementById("targetPage").value - 1;
	if(Number.isInteger(page)&& (page < $scope.totalPage) && page>=0)
	{
	$scope.currentPage = page;
	pagingService.setCurrentPage(page);
	Users.get({aid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() ,username: searchService.getState().username, email: searchService.getState().email, name : searchService.getState().name, isAdmin : searchService.getState().admin} ,updateListOfUser );
	}
	else{
	alert('Please type in right number', 'ERROR');
	}
	}

	$scope.setSize = function(){
		pagingService.setSize($scope.userPerPage);
		Users.get({aid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() ,username: searchService.getState().username, email: searchService.getState().email, name : searchService.getState().name, isAdmin : searchService.getState().admin} ,updateListOfUser );
		
	}
	$scope.jumpToCreateUser = function(){
		window.location.href = '/#/createUser';
	}

//	-------------------------update role---------------------------------
	$scope.updateRole = function(id,role){
		if(role)var isAdmin = true;
		else var isAdmin = false;
		Users.update({aid : $scope.user.id, uid : id,isAdmin:"isAdmin"},{isAdmin},null);
	}
//	-----------------------active status---------------------------------

	$scope.activeUser = function(id,status){
		if(status == "enabled"){
			Users.update({aid : $scope.user.id, uid : id, active : "enable"},null,null);
		}
		else{
			Users.update({aid : $scope.user.id, uid : id, active : "disable"},null,null);
		}
	}

	$scope.editUser = function(selectedUser){
		userService.setEditUser(selectedUser);
		window.location.href = '/#/editUser';
	}
} ] );


tracker.controller('createUser.Controller', ['$scope', '$resource','userService','Users', function( $scope, $resource, userService, Users ) {

	var getCurrentUser = function(){
		console.log("name = " +userService.getUser().username )
		if(userService.getUser().username)
		{
			$scope.user = userService.getUser();
		}
		else
		{	
			home.get(function(loggedUser){
				$scope.user = loggedUser;
				userService.setUser(loggedUser);
				userService.setEditUser($scope.user);
			});	
		}
	}

	$scope.logout = function(){
		$.ajax('/logout',{type : 'POST'});
		window.location.href = '/#/';
	}


	function validateEmail(email) {
    		var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    		return re.test(email);
		};

	$scope.createUser = function(){
		if($scope.newUserType)
			var isAdmin = true;
		else
			var isAdmin = false;
		
		var newUser = {
				username: $scope.newUserName,
				name: $scope.newName,
				password: $scope.newPasword,
				newPhoneNumber : $scope.newPhoneNumber,
				email: $scope.newEmailAddress,
				isAdmin : isAdmin
		}
		Users.save({aid:$scope.user.id},newUser,function(){window.location.href = '/#/admin';});
	};

	$scope.cancel = function(){
		window.location.href = '/#/admin'
	}


	getCurrentUser();
} ] );


tracker.controller('editUser.Controller', ['$scope', 'userService','Users','home', function( $scope, userService,Users,home ) {
	var getCurrentEditUser = function(){
		var editUser = userService.getEditUser();
		$scope.newUserName = editUser.username;
		$scope.newEmailAddress = editUser.email;
		$scope.newName = editUser.name;
		$scope.newUserType = editUser.admin;

		if(editUser.phone)$scope.newPhoneNumber = editUser.phone;
		else $scope.newPhoneNumber = [];
	};

	var getCurrentUser = function(){
		console.log("name = " +userService.getUser().username )
		if(userService.getUser().username)
		{
			$scope.user = userService.getUser();
			getCurrentEditUser();
		}
		else
		{	
			home.get(function(loggedUser){
				$scope.user = loggedUser;
				userService.setUser(loggedUser);
				userService.setEditUser($scope.user);
				getCurrentEditUser();
			});	
		}
	};

	$scope.PN =
	{
			add: function(){
				$scope.newPhoneNumber.push(" ");
			},

			del: function(key){
				$scope.newPhoneNumber.splice(key,1);
			}
	}

	function validateEmail(email) {
    		var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    		return re.test(email);
		};
	
	$scope.confirmEditUser = function(){

		if($scope.newUserType)
			var isAdmin = true;
		else
			var isAdmin = false;
		console.log(validateEmail($scope.newEmailAddress));
		if(!validateEmail($scope.newEmailAddress)){
			$("#errorMessage").fadeIn();
		}else{
			var updatedUser = {
				// id:$scope.user.id,
				username: $scope.newUserName,
				newPhoneNumber : $scope.newPhoneNumber,
				isAdmin : isAdmin
				}
			Users.save({aid:$scope.user.id},updatedUser,function(){window.location.href = '/#/admin';});
			}
	};

	$scope.cancel = function(){
		if($scope.user.admin){
		window.location.href = '/#/admin';
		}else{
			window.location.href = '/#/user';
		}
	}
		
	getCurrentUser();
} ] );


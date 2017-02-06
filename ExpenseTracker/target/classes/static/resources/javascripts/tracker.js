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
	return $resource('/admin/:aid/users/:uid/:active',{ }, { 'update' : { method : 'PUT'}});
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
	userService.setEditUser($scope.user);
	
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
	if(Number.isInteger(page)&& page<= $scope.totalPage && page>=0)
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

//	--------------------------edit user---------------------------
	$scope.editUser = function(user){
		userService.setEditUser(user);
		window.location.href = '/#/editUser';
	}
//	-------------------------update role---------------------------------
	$scope.updateRole = function(id,role){
		if(role)var isAdmin = true;
		else var isAdmin = false;
//		$.ajax( {
//		url : '/admin/'+ $scope.user.id +'/users/'+id+'/isAdmin',
//		type : 'PUT',
//		contentType: "application/json; charset=utf-8",
//		data : ""+ isAdmin
//		} );
		Users.update({aid : $scope.user.id, uid : id, isAdmin : (""+isAdmin)},null,null);
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
} ] );


tracker.controller('createUser.Controller', ['$scope', '$resource','userService','Users', function( $scope, $resource, userService, Users ) {

	var getCurrentUser = function(){
		$scope.user = userService.getUser();
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
		var newUser = {
				username: $scope.newUserName,
				name: $scope.newName,
				password: $scope.newPasword,
				newPhoneNumber : $scope.newPhoneNumber,
				email: $scope.newEmailAddress,
				isAdmin : isAdmin
		}
		Users.save({aid:$scope.user.id},newUser,function(){window.location.href = '/#/admin';});
	}
	
	
	getCurrentUser();

} ] );


tracker.controller('editUser.Controller', ['$scope', 'userService','Users', function( $scope, userService,Users ) {

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
//		$.ajax( {
//			url : '/admin/'+ $scope.user.id +'/users/' + editUser.id,
//			type : 'POST',
//
//			data : {
//				id:editUser.id,
//				username: editUser.username,
//				name: $scope.newName,
//				newPhoneNumber : $scope.newPhoneNumber,
//				email: editUser.email,
//				isAdmin : isAdmin
//			},
//			success:function(){
//				window.location.href = '/#/admin'
//			}
//		} );
		var updatedUser = {
				id:editUser.id,
				username: editUser.username,
				name: $scope.newName,
				newPhoneNumber : $scope.newPhoneNumber,
				email: editUser.email,
				isAdmin : isAdmin
		}
		Users.save({aid:$scope.user.id},updatedUser,function(){window.location.href = '/#/admin';});
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



//	-----------------------------search--------------------------------
	$scope.searchBy = function(searchKey){
		var url = "";
		if($scope.receiptLocation)
			url = url + "&location=" + $scope.receiptLocation;
		if($scope.receiptCategory)
			url = url + "&project=" + $scope.receiptCategory;
		if($scope.receiptTag)
			url = url + "&tag=" + $scope.receiptTag;

		userService.setSearchKey(url);

		getUserListWithSearch();

	}

	var getUserListWithSearch = function(){
		console.log("hello from getUserListWithSearch");
		$.ajax(  '/admin/'+ $scope.user.id+'/users?size='+$scope.userPerPage+
				'&page='+$scope.currentPage+
				userService.getSearchKey,{ type : 'GET', success: updateListOfUser });
	}


//	----------------------------paging------------------------

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

//	--------------------------edit/view receipt---------------------------

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

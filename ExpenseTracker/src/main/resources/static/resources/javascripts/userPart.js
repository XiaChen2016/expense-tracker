var tracker = angular.module('Tracker')

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

tracker.factory('projectService',function(){
	var projectList = {};

	return{
		setProjectList: function(p){projectList = p;},
		getProjectList: function(){return projectList;}
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
	var state = { project :null };
	var flag = 0;
	return{
		getState: function(){return state;},
		setState: function(s){state = s;},
		getFlag: function(){return flag;},
		setFlag: function(f){flag = f;}
	};
});

tracker.factory('Receipts', function($resource) {
	return $resource('/user/:uid/receipt/:rid', { },{ 'update' : { method : 'PUT'}});
} );

tracker.factory('Projects', function($resource) {
	return $resource('/user/:uid/project/:rid', { },{ 'update' : { method : 'PUT'}});
} );

tracker.factory('home', function($resource) {
	return $resource('/home');
} );

tracker.controller('userHome.Controller', ['$scope', '$resource','userService','pagingService','Receipts','home', 'searchService','receiptService','projectService','Projects', function( $scope, $resource, userService,pagingService,Receipts,home, searchService, receiptService, projectService,Projects  ) {

	pagingService.setCurrentPage(0);
	pagingService.setSize(10);
	$scope.currentPage = pagingService.getCurrentPage();
	$scope.userPerPage = pagingService.getSize();
	$scope.sizeList = pagingService.getSizeList();
	$scope.projectList = projectService.getProjectList();
	$scope.project = "All";

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
		pagingService.setCurrentPage(result.number);
		$scope.userPerPage = result.size;
		pagingService.setSize(result.size);
	}

	var getAllProject = function () {
		Projects.get({ uid : $scope.user.id },function(result){
		projectService.setProjectList(result);
		var rawProjectList = [];
		for(var i = 0 ; i< result; i++){
			rawProjectList.push(result[i].name);
			console.log(result[i].name);
		}
		$scope.projectList = rawProjectList;
		});

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
			home.get(function(loggedUser){
				$scope.user = loggedUser;
				userService.setUser(loggedUser);
				console.log("Username: " + userService.getUser().username);
				getReceiptList();
			});	
		}
	}

	var getReceiptList = function(){
		console.log("hello from getUserList!");
		Receipts.get({ uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() },updateListOfReceipt);
	}

	getCurrentUser();
	userService.setEditUser($scope.user);
	getAllProject();



//	-----------------------------search--------------------------------
	$scope.searchReceipts = function(){
		var state = {project : $scope.project};
		searchService.setState(state);
		Users.get({aid : $scope.user.id, page : 0 , size : pagingService.getSize() ,username: $scope.search.username, email: $scope.search.email, name : $scope.search.name, isAdmin : $scope.search.admin} ,updateListOfReceipt );
	}


//	----------------------------paging------------------------

	$scope.prevPage = function(){
		pagingService.setCurrentPage(pagingService.getCurrentPage() - 1);
		Receipts.get({uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() , project : searchService.getState().project} ,updateListOfReceipt );
	}

	$scope.nextPage = function(){
		pagingService.setCurrentPage(pagingService.getCurrentPage() + 1);
		Receipts.get({uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() , project : searchService.getState().project} ,updateListOfReceipt );
	}

	$scope.setPage = function()
	{
	var page = document.getElementById("targetPage").value - 1;
	if(Number.isInteger(page)&& page<= $scope.totalPage && page>=0)
	{
	$scope.currentPage = page;
	pagingService.setCurrentPage(page);
	Receipts.get({uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() , project : searchService.getState().project} ,updateListOfReceipt );
	}
	else{
	alert('Please type in right number', 'ERROR');
	}
	}

	$scope.setSize = function(){
		pagingService.setSize($scope.userPerPage);
		Receipts.get({uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() , project : searchService.getState().project} ,updateListOfReceipt );
		
	}

	// $scope.prevPage = function(){
	// 	if(userService.getSearchKey)
	// 		$.ajax(  '/admin/'+ $scope.user.id+'/receipt?size='+$scope.userPerPage+
	// 				'&page='+ ($scope.currentPage - 1)+
	// 				userService.getSearchKey,{ type : 'GET', success: updateListOfUser  });
	// 	else
	// 		$.ajax(  '/user/'+ $scope.user.id+'/receipt?size='+$scope.userPerPage+
	// 				'&page='+ ($scope.currentPage - 1),{ type : 'GET', success: updateListOfReceipt  });

	// }

	// $scope.nextPage = function(){
	// 	if(userService.getSearchKey)
	// 		$.ajax(  '/admin/'+ $scope.user.id+'/receipt?size='+$scope.userPerPage+
	// 				'&page='+ ($scope.currentPage + 1)+
	// 				userService.getSearchKey,{ type : 'GET', success: updateListOfUser  });
	// 	else
	// 		$.ajax(  '/user/'+ $scope.user.id+'/receipt?size='+$scope.userPerPage+
	// 				'&page='+ ($scope.currentPage + 1),{ type : 'GET', success: updateListOfReceipt  });
	// }

	// $scope.setPage = function()
	// {
	// 	var page = document.getElementById("targetPage").value - 1;
	// 	if(Number.isInteger(page)&& page<= $scope.totalPage && page>=0)
	// 	{
	// 		if(userService.getSearchKey)
	// 		{
	// 			$scope.currentPage = page;
	// 			$.ajax(  '/user/'+ $scope.user.id+'/receipt?size='+$scope.userPerPage+
	// 					'&page='+ $scope.currentPage+
	// 					userService.getSearchKey,{ type : 'GET', success: updateListOfUser  });

	// 		}
	// 		else{
	// 			$scope.currentPage = page;
	// 			$.ajax(  '/user/'+ $scope.user.id+'/receipt?size='+$scope.userPerPage+ '&page='+$scope.currentPage,{ type : 'GET', success: updateListOfReceipt  });

	// 		}
	// 	}
	// 	else
	// 		alert('Please type in right number', 'ERROR');

	// }

	// $scope.setSize = function(){
	// 	if(userService.getSearchKey)
	// 		getUserListWithSearch();
	// 	else
	// 		getReceiptList();
	// }

//	--------------------------edit/view receipt---------------------------
	$scope.viewDetail = function(r){
		console.log("hello from viewDetail");
		receiptService.setEditReceipt(r);
		window.location.href = '/#/editReceipt';

	}

} ] );


tracker.controller('editReceipt.Controller', ['$scope', 'userService','receiptService', function( $scope, userService ,receiptService) {
	
	var receipt = receiptService.getEditReceipt();
	$scope.user = userService.getUser();
	$scope.receiptID = receipt.id;
	var date = new Date(receipt.time);
	$scope.receiptTime = date;
	$scope.receiptLocation = receipt.place;
	$scope.receiptProject = receipt.projectId;
	$scope.receiptNote = receipt.note;
	var rawTags = [];
	if(receipt.list_of_items)$scope.items = receipt.list_of_items;
	else $scope.items = [{name:" ",quantity:" ",price:" "}];

	if(receipt.category){
		for(var i = 0; i<receipt.category.length;i++ )
		rawTags.push({"text" : receipt.category[i]});
		$scope.tags = rawTags;
		console.log(rawTags);
	}
	else {
		$scope.tags = [" "];
		console.log("trueeee");
	}

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

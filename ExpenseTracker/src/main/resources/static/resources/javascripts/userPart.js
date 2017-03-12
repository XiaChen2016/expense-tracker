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
	var currentProject = "";

	return{
		setProjectList: function(p){projectList = p;},
		getProjectList: function(){return projectList;},
		setCurrentProjectWithName: function(p){currentProject = p;},
		getCurrentProjectInId: function(){
			for(var i = 0 ; i< projectList.length; i++){
				if(currentProject == projectList[i].name){
					return projectList[i].id;
				}
			}
			return null;
		},
		getCurrentProjectInName: function(p){
			for(var i = 0 ; i< projectList.length; i++){
				if(p == projectList[i].id){
					return projectList[i].name;
				}
			}
			return null;
		},
		getCurrentProjectWithName: function(pname){
			for(var i = 0 ; i< projectList.length; i++){
				if(pname == projectList[i].name){
					return projectList[i].id;
				}
			}
			return null;
		}

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
	return $resource('/user/:uid/receipts/:rid/:items/:pictures', { },{ 'update' : { method : 'PUT'}});
} );

tracker.factory('Projects', function($resource) {
	return $resource('/user/:uid/projects/:pid/:receipts', { },{ 'update' : { method : 'PUT'}});
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
		var receipts = result.content;
		for(var i = 0; i < receipts.length; i ++){
			receipts[i].projectName = projectService.getCurrentProjectInName(receipts[i].projectId);
		}
		$scope.receiptList = receipts;
		$scope.totalPage = result.totalPages;
		$scope.responseContent = result;
		$scope.currentPage = result.number;
		pagingService.setCurrentPage(result.number);
		$scope.userPerPage = result.size;
		pagingService.setSize(result.size);
	}

	var getCurrentUser = function(){
		console.log("name = " +userService.getUser().username )
		if(userService.getUser().username) {
			$scope.user = userService.getUser();
			getReceiptList();
			getAllProject();
		} else {	
				home.get(function(loggedUser){
					$scope.user = loggedUser;
					userService.setUser(loggedUser);
					console.log("Username: " + userService.getUser().username);
					getReceiptList();
					getAllProject();
					userService.setEditUser($scope.user);
				});	
		}
	}

	var getReceiptList = function(){
		console.log("hello from getUserList!");
		Receipts.get({ uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() },updateListOfReceipt);
	}

	var getAllProject = function () {
		Projects.get({ uid : userService.getUser().id },function(result){
		projectService.setProjectList(result.content);
		$scope.projectList = result.content;
		});
	}
	getCurrentUser();
// -----------------------------getSingleProject--------------------
	$scope.getSingleProject = function(project){
		Projects.get({uid : userService.getUser().id , pid : project.id, receipts : "receipts"},function(result){
		})
	};
// -----------------------------search--------------------------------
	$scope.searchReceipts = function(){
		var state = {project : $scope.project};
		searchService.setState(state);
		Users.get({aid : $scope.user.id, page : 0 , size : pagingService.getSize() ,username: $scope.search.username, email: $scope.search.email, name : $scope.search.name, isAdmin : $scope.search.admin} ,updateListOfReceipt );
	}
// ----------------------------paging------------------------

	$scope.prevPage = function(){
		pagingService.setCurrentPage(pagingService.getCurrentPage() - 1);
		Receipts.get({uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() , project : searchService.getState().project} ,updateListOfReceipt );
	}

	$scope.nextPage = function(){
		pagingService.setCurrentPage(pagingService.getCurrentPage() + 1);
		Receipts.get({uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() , project : searchService.getState().project} ,updateListOfReceipt );
	}

	$scope.setPage = function(){
	var page = document.getElementById("targetPage").value - 1;
		if(Number.isInteger(page)&& page<= $scope.totalPage && page>=0){
			$scope.currentPage = page;
			pagingService.setCurrentPage(page);
			Receipts.get({uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() , project : searchService.getState().project} ,updateListOfReceipt );
		} else {
			alert('Please type in right number', 'ERROR');
		}
	}

	$scope.setSize = function(){
		pagingService.setSize($scope.userPerPage);
		Receipts.get({uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() , project : searchService.getState().project} ,updateListOfReceipt );
		
	}

	$scope.setProject = function(){
		projectService.setCurrentProjectWithName($scope.project);
		Receipts.get({uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() , project : $scope.project.id} ,updateListOfReceipt );
		
	}

// --------------------------edit/view receipt---------------------------
	$scope.viewDetail = function(r){
		console.log("hello from viewDetail");
		receiptService.setEditReceipt(r);
		window.location.href = '/#/editReceipt';

	}

	$scope.jumpToCreateReceipt = function (){
		window.location.href = "/#/createReceipt";
	}
	
	$scope.editUser = function(selectedUser){
		userService.setEditUser(selectedUser);
		window.location.href = '/#/editUser';
	}

	$scope.deleteReceipt = function(receipt){
		Receipts.delete({uid:receipt.ownerId,rid:receipt.id},function(){
			window.location.href = '/#/user';
		})
	}
} ] );


tracker.controller('editReceipt.Controller', ['$scope', 'userService','receiptService','projectService','Receipts', function( $scope, userService ,receiptService,projectService,Receipts) {
	$scope.receipt = receiptService.getEditReceipt();
	var getCurrentUser = function(){
		console.log("name = " +userService.getUser().username )
		if(userService.getUser().username){
			$scope.user = userService.getUser();
		} else {	
			home.get(function(loggedUser){
				$scope.user = loggedUser;
				userService.setUser(loggedUser);
				userService.setEditUser($scope.user);
			});	
		}
	}
	getCurrentUser();
	
	// var receipt = receiptService.getEditReceipt();
	// $scope.receiptObejct = receipt;
	// console.log(receipt);
	// getPictures(receipt);
	// $scope.receiptID = receipt.id;
	// var date = new Date(receipt.time);
	// $scope.receiptTime = new Date(receipt.time);
	// $scope.receiptLocation = receipt.place;
	$scope.receipt.time = new Date( $scope.receipt.time );

	$scope.projectList = projectService.getProjectList();
	var p = {id : $scope.receipt.projectId,
			name : projectService.getCurrentProjectInName($scope.receipt.projectId)};
	$scope.receiptProject = p;

	console.log($scope.receiptProject);

	$scope.receiptNote = $scope.receipt.note;
	var rawTags = [];
	if($scope.receipt.list_of_items) {
		$scope.items = $scope.receipt.list_of_items;
	} else {
		$scope.items = [{name:" ",quantity:" ",price:" "}];
	}

	if($scope.receipt.category){
		for(var i = 0; i<$scope.receipt.category.length;i++ )
		rawTags.push({"text" : $scope.receipt.category[i]});
		$scope.tags = rawTags;
	}
	else {
		$scope.tags = [" "];
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

	Number.prototype.padLeft = function(base,chr){
		var  len = (String(base || 10).length - String(this).length)+1;
		return len > 0? new Array(len).join(chr || '0')+this : this;
	}

	$scope.confirmEditReceipt = function(){
		var d = $scope.receipt.time;
		var dformat = [ (d.getMonth()+1).padLeft(),
		                d.getDate().padLeft(),
		                d.getFullYear()].join('-')+
		                ' ' +
		                [ d.getHours().padLeft(),
		                  d.getMinutes().padLeft(),
		                  d.getSeconds().padLeft()].join(':');
		var  date = new Date(dformat);
		var timestamp=Math.round(date.getTime());
		var total = 0;
		var x;
		for(x in $scope.items){
			total += $scope.items[x].quantity * $scope.items[x].price;
		}
		var allTag = [];
		for(var y in $scope.tags){
			allTag.push($scope.tags[y].text);
		}
		var data =  {
				time:timestamp,
				place: $scope.receiptLocation,
				project: $scope.receiptProject.id,
				note : $scope.receiptNote,
				category: allTag,
				list_of_items : $scope.items,
				total : total
			};
		Receipts.update({uid:$scope.user.id,rid:$scope.receiptID},data,function(){window.location.href = '/#/user';});
	}

	$scope.cancel = function(){
		window.location.href = '/#/user';
	}

	$('#pictureBody').click(function () {
            $('#pictureBody').not(this).animate({height: "250px"}, 'fast');
            var $this = $(this),
            flag = !$this.data('flag');
            $this.stop().animate({height: (flag ? "500px" : "250px")}, 'fast')
                 .data('flag', flag);
          });

} ] );


tracker.controller('createReceipt.Controller', ['$scope', 'userService','projectService','Receipts','receiptService', function( $scope, userService ,projectService,Receipts,receiptService) {

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
	getCurrentUser();

	$scope.projectList = projectService.getProjectList();
	console.log($scope.projectList);

	var date = new Date();
	$scope.receiptTime = date;
	$scope.receiptLocation = "";
	$scope.receiptCategory = "";
	$scope.receiptNote = "";
	$scope.items = [];
	$scope.tags = [];
	
	var createNewItem = function() { return {name :'', quantity : '', price : ''}; };
	$scope.newItem = createNewItem();

	$scope.addItem = function( ) {
		$scope.items.push( $scope.newItem );
		$scope.newItem = createNewItem();
	};

	$scope.deleteItem = function(index) {
		$scope.items = $scope.items.filter( (item,i) => i != index );
	};

	Number.prototype.padLeft = function(base,chr){
		var  len = (String(base || 10).length - String(this).length)+1;
		return len > 0? new Array(len).join(chr || '0')+this : this;
	}

	$scope.createReceipt = function(){
		$("#hideOne").hide();
		$("#hideTwo").show();
		$("#time").prop('disabled', true);
		$("#location").prop('disabled', true);
		$("#selectProject").prop('disabled', true);
		$("#finishedTags").prop('disabled', true);
		$("#note").prop('disabled', true);

		var d = $scope.receiptTime;
		var dformat = [ (d.getMonth()+1).padLeft(),
		                d.getDate().padLeft(),
		                d.getFullYear()].join('-')+
		                ' ' +
		                [ d.getHours().padLeft(),
		                  d.getMinutes().padLeft(),
		                  d.getSeconds().padLeft()].join(':');
		var  date = new Date(dformat);
		var timestamp=Math.round(date.getTime());
		var total = 0;
		var x;
		for(x in $scope.items){
			total += $scope.items[x].quantity * $scope.items[x].price;
		}
		var allTag = [];
		for(var y in $scope.tags){
			allTag.push($scope.tags[y].text);
		}
		var data =  {
				time:timestamp,
				place: $scope.receiptLocation,
				projectId: $scope.project.id,
				note : $scope.receiptNote,
				category: allTag
			};
		Receipts.save({uid:$scope.user.id},data,function(result){
		alert('Create successful and more information required', 'Success');
		receiptService.setEditReceipt(result);
		});
	}
 
   $scope.upload = function() {
      var formData = new FormData();
      if(!$('#photoFile').val()){
			return;
	  	}
      formData.append("file", $('#photoFile')[0].files[0]);
	  console.log($scope.project);
      $.ajax( {
		 url : '/user/' + $scope.user.id +'/receipts/'+receiptService.getEditReceipt().id+'/pictures',
         type : 'POST',
         data : formData,
         processData : false,
         contentType : false,
         success : function( response ) {
			 if(!response.list_of_items){
				 $("#hideThree").show();
			 }
         }
      } );
      $('#photoFile').val('');
	  $("#delete-0").hide();
	  $('#hideFour').show();
   }

   $scope.updateSingleItem = function(key){
		$("#item-"+key).hide();
	};

	$scope.cancel = function(){
		window.location.href = '/#/user'
	};

	$scope.noImage = function(){
		$("#hideThree").show();
		$('#hideFour').show();
		$("#delete-0").hide();
	};

	$scope.confirmEditReceipt = function(){
		var data = {
				list_of_items : $scope.items,
			};
		Receipts.update({uid:$scope.user.id,rid:receiptService.getEditReceipt().id,items : "items"},data,function(){window.location.href = '/#/user';});
	}
} ] );

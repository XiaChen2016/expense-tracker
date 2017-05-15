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
	var editProject = {};

	return{
		setProjectList: function(p){projectList = p;},
		getProjectList: function(){return projectList;},
		setEditProject: function(p){editProject = p;},
		getEditProject: function(){return editProject;},
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

	$scope.logout = function(){
		$.ajax('/logout',{type : 'POST'});
		window.location.href = '/#/';
	}

	var updateListOfReceipt = function ( result ) {

		console.log("updateListOfUser");
		var receipts = result.content;
		for(let i = 0; i < receipts.length; i ++){
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
			getAllProject();
			
		} else {	
				home.get(function(loggedUser){
					$scope.user = loggedUser;
					userService.setUser(loggedUser);
					console.log("Username: " + userService.getUser().username);
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
		getReceiptList();
		});
	}
	getCurrentUser();
// -----------------------------getSingleProject--------------------
	$scope.getSingleProject = function(project){
		Projects.get({uid : userService.getUser().id , pid : project.id, receipts : "receipts"},function(result){
		})
	};
// -----------------------------search--------------------------------
	Number.prototype.padLeft = function(base,chr){
		var  len = (String(base || 10).length - String(this).length)+1;
		return len > 0? new Array(len).join(chr || '0')+this : this;
	}

	var transferToTimestamp = function(timelocal){
		var d = timelocal;
		var dformat = [ (d.getMonth()+1).padLeft(),
		                d.getDate().padLeft(),
		                d.getFullYear()].join('-')+
		                ' ' +
		                [ d.getHours().padLeft(),
		                  d.getMinutes().padLeft(),
		                  d.getSeconds().padLeft()].join(':');
		var  date = new Date(dformat);
		var timestamp=Math.round(date.getTime());
		return timestamp;
	}

	$scope.searchReceipts = function(){
		var state = {};
		if($scope.search.startTime && $scope.search.endTime){
			if($scope.search.project){
				state = {project: $scope.search.project.name, tag: $scope.search.tag, item : $scope.search.wordsInItem,minDate : transferToTimestamp($scope.search.startTime), maxDate : transferToTimestamp($scope.search.endTime) , minTotal: $scope.search.minTotal, maxTotal: $scope.search.maxTotal };
			} else {
				state = {tag: $scope.search.tag, item : $scope.search.wordsInItem,minDate : transferToTimestamp($scope.search.startTime), maxDate : transferToTimestamp($scope.search.endTime) , minTotal: $scope.search.minTotal, maxTotal: $scope.search.maxTotal };
			}
			searchService.setState(state);
		} else if($scope.search.project){
			state = {project: $scope.search.project.name, tag: $scope.search.tag, item : $scope.search.wordsInItem, minTotal: $scope.search.minTotal, maxTotal: $scope.search.maxTotal };
			searchService.setState(state);
		} else {
			state = {tag: $scope.search.tag, item : $scope.search.wordsInItem, minTotal: $scope.search.minTotal, maxTotal: $scope.search.maxTotal };
			searchService.setState(state);
		}
		Receipts.get({uid : $scope.user.id, page : 0 , size : pagingService.getSize() ,project: state.project, category: state.tag, minDate : state.minDate, maxDate : state.maxDate, item : state.item, minTotal:state.minTotal, maxTotal : state.maxTotal} ,updateListOfReceipt );
	}
// ----------------------------paging------------------------

	$scope.prevPage = function(){
		pagingService.setCurrentPage(pagingService.getCurrentPage() - 1);
		var state = searchService.getState();
		Receipts.get({uid : $scope.user.id, page : pagingService.getCurrentPage()  , size : pagingService.getSize() ,project: state.project, category: state.tag, minDate : state.minDate, maxDate : state.maxDate, item : state.item, minTotal:state.minTotal, maxTotal : state.maxTotal} ,updateListOfReceipt );
	}

	$scope.nextPage = function(){
		pagingService.setCurrentPage(pagingService.getCurrentPage() + 1);
		var state = searchService.getState();
		Receipts.get({uid : $scope.user.id, page : pagingService.getCurrentPage() , size : pagingService.getSize() ,project: state.project, category: state.tag, minDate : state.minDate, maxDate : state.maxDate, item : state.item, minTotal:state.minTotal, maxTotal : state.maxTotal} ,updateListOfReceipt );
	}

	// $scope.setPage = function(){
	// var page = document.getElementById("targetPage").value - 1;
	// 	if(Number.isInteger(page)&& page<= $scope.totalPage && page>=0){
	// 		$scope.currentPage = page;
	// 		pagingService.setCurrentPage(page);
	// 	Receipts.get({uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() , project : searchService.getState().project.name , tag : searchService.getState().tag , upperLimit : searchService.getState().upperLimit , lowerLimit : searchService.getState().lowerLimit , item : searchService.getState().item} ,updateListOfReceipt );		} else {
	// 		alert('Please type in right number', 'ERROR');
	// 	}
	// }

	$scope.setPage = function(){
	var page = document.getElementById("targetPage").value - 1;
		if(Number.isInteger(page)&& page< $scope.totalPage && page>=0){
			$scope.currentPage = page;
			pagingService.setCurrentPage(page);
			var state = searchService.getState();
			Receipts.get({uid : $scope.user.id, page : pagingService.getCurrentPage() , size : pagingService.getSize() ,project: state.project, tag: state.tag, minDate : state.minDate, maxDate : state.maxDate, item : state.item, minTotal:state.minTotal, maxTotal : state.maxTotal} ,updateListOfReceipt );		
		} else {
			alert('Please type in right number', 'ERROR');
		}
		$("#targetPage").val('');
	}

	$scope.setSize = function(){
		pagingService.setSize($scope.userPerPage);
		Receipts.get({uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize() , project : searchService.getState().project , tag : searchService.getState().tag , upperLimit : searchService.getState().upperLimit , lowerLimit : searchService.getState().lowerLimit , item : searchService.getState().item} ,updateListOfReceipt );		
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

	$scope.jumpToProjects = function (){
		window.location.href = "/#/project";
	}
	
	$scope.jumpToAdmin = function (user){
		userService.setUser(user);
		window.location.href = '/#/admin';
	}
	
	$scope.editUser = function(selectedUser){
		userService.setEditUser(selectedUser);
		window.location.href = '/#/editUser';
	}

	$scope.deleteReceipt = function(receipt){
		Receipts.delete({uid:receipt.ownerId,rid:receipt.id},function(){
			location.reload();
		})
	}
} ] );


tracker.controller('editReceipt.Controller', ['$scope', 'userService','receiptService','projectService','Receipts', function( $scope, userService ,receiptService,projectService,Receipts) {
	$scope.receipt = receiptService.getEditReceipt();
	console.log($scope.receipt);
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
	var createNewItem = function() { return {name :'', quantity : '', price : ''}; };
	$scope.receipt.time = new Date( $scope.receipt.time );

	$scope.projectList = projectService.getProjectList();
	var p = {id : $scope.receipt.projectId,
			name : projectService.getCurrentProjectInName($scope.receipt.projectId)};
	$scope.receiptProject = p;
	$scope.receiptNote = $scope.receipt.note;
	var rawTags = [];
	if($scope.receipt.list_of_items) {
		$scope.items = $scope.receipt.list_of_items;
	} else {
		$scope.newItem = createNewItem();
	}

	if($scope.receipt.category){
		for(var i = 0; i<$scope.receipt.category.length;i++ )
		rawTags.push({"text" : $scope.receipt.category[i]});
		$scope.tags = rawTags;
	}
	else {
		$scope.tags = [];
	}

	var watchChange = function(){
		return angular.equals($scope.receipt , receiptService.getEditReceipt());
	}

	$scope.addItem = function( ) {
		$scope.items.push( $scope.newItem );
		$scope.newItem = createNewItem();
		$scope.receiptWatch = true;
	};

	$scope.updateItem = function(item,index) {
		var x;
		for(x in $scope.items){
			if (x == index){
				$scope.items[x] = item;
				$('#item-'+x).hide();
			}
		}
		$scope.receiptWatch = true;
	};

	$scope.showUpdate = function(){
		$scope.receiptWatch = true;
	}

	$scope.deleteItem = function(index) {
		$scope.items = $scope.items.filter( (item,i) => i != index );
		$scope.receiptWatch =true;
	};

	Number.prototype.padLeft = function(base,chr){
		var  len = (String(base || 10).length - String(this).length)+1;
		return len > 0? new Array(len).join(chr || '0')+this : this;
	}

	$scope.confirmEditReceipt = function(){
		var d = $scope.receipt.time;
		if(typeof(d) == 'number'){
			var timestamp=d;
		}else{
			var dformat = [ (d.getMonth()+1).padLeft(),
			                d.getDate().padLeft(),
			                d.getFullYear()].join('-')+
			                ' ' +
			                [ d.getHours().padLeft(),
			                  d.getMinutes().padLeft(),
			                  d.getSeconds().padLeft()].join(':');
			var  date = new Date(dformat);
			var timestamp=Math.round(date.getTime());
		}
		var total = 0;
		var x;
		for(x in $scope.items){
			total += $scope.items[x].price;
		}
		total = (Math.round(total * 100) / 100);
		var allTag = [];
		for(var y in $scope.tags){
			allTag.push($scope.tags[y].text);
		}
		if(!$scope.receiptProject.id){
			alert("Please choose a project for this receipt");
		} else {
			var data =  {
					id: $scope.receipt.id,
					ownerId:$scope.user.id,
					time:timestamp,
					place: $scope.receipt.place,
					picId:$scope.receipt.picId,
					projectId: $scope.receiptProject.id,
					note : $scope.receiptNote,
					category: allTag,
					list_of_items : $scope.items,
					total : total
				};
			Receipts.update({uid:$scope.user.id,rid:$scope.receipt.id},data,function(){window.location.href = '/#/user';});
		}
		}

	$scope.cancel = function(){
		window.location.href = '/#/user';
	}

	var processReturnData = function(result){
//		$('#picModal').modal('hide');
//		$scope.receipt.picId = undefined;
//		alert("update success");
//		if(!$scope.keepOldItems){
//			$scope.items = result.list_of_items;
//			$scope.itemArea =true;
//			$scope.confirmArea = true;
//			$scope.receipt = {};
//			$scope.receipt.time = new Date( result.time );
//			$scope.tags = undefined;
//			$scope.receiptNote = undefined;
//			$scope.projectList = projectService.getProjectList();
//			$scope.receipt.place = result.place;
			$scope.receipt = result;
//			$('#picModal').modal('hide');
			$scope.receiptWatch = true;
//		}
//	   $scope.itemArea =true;
//	   $scope.confirmArea = true;
   }
	

	$scope.upload = function() {
      var formData = new FormData();
      if(!$('#photoFile').val()){
			return;
	  	}
      formData.append("file", $('#photoFile')[0].files[0]);
      $.ajax( {
    	  url : '/user/' + $scope.user.id +'/receipts/'+$scope.receipt.id+'/pictures',
      
//		 url : '/user/' + $scope.user.id +'/receipts/'+receiptService.getEditReceipt().id+'/pictures',
         type : 'PUT',
         data : formData,
         async: false,
         processData : false,
         contentType : false,
         success : processReturnData
      } );
      $('#photoFile').val('');
   }

   $scope.$on('$locationChangeStart', function( event ) {
    var answer = confirm("Are you sure you want to leave this page? Data not uploaded will not be saved")
    if (!answer) {
        event.preventDefault();
    }
   });

   var getReceipt = function(receiptID){
	   Receipts.get({uid : $scope.user.id, rid : receiptID}, function(result){

	   });
   }

   $scope.deletePctures = function(receiptID) {
      Receipts.delete({uid:$scope.user.id,rid:receiptID , pictures : "pictures"}, function(){
		  $('#picModal').modal('hide');
		$scope.receipt.picId = undefined;
	  })
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
	$scope.imageArea = true;
	$scope.imageButtonArea = true;
	$scope.imageSelectArea = true;

	function isNormalInteger(str) {
    	return /^\+?(0|[1-9]\d*)$/.test(str);
	}

	var checkPrice = function(num){
		if(!isNaN(num) && num.toString().indexOf('.') != -1){
			return true;
		}
		if(isNormalInteger(num)){
			return true;
		}
		return false;
	}
	
	var createNewItem = function() { return {name :'', quantity : '', price : ''}; };
	$scope.newItem = createNewItem();

	$scope.addItem = function( ) {
		if(isNormalInteger($scope.newItem.quantity)){
			if(checkPrice($scope.newItem.price)){
				$scope.items.push( $scope.newItem );
				$scope.newItem = createNewItem();
			} else {
				alert("Wrong price , Please check");
			}
		} else {
			alert("Wrong quantity , Please check");
		}
	};

	$scope.deleteItem = function(index) {
		$scope.items = $scope.items.filter( (item,i) => i != index );
	};

	Number.prototype.padLeft = function(base,chr){
		var  len = (String(base || 10).length - String(this).length)+1;
		return len > 0? new Array(len).join(chr || '0')+this : this;
	}
//
//	$scope.createReceipt = function(){
//		$scope.createArea = false;
//		$scope.imageArea = true;
////		$scope.imageSelectArea = true;
//		$scope.imageButtonArea = true;
//		$scope.basicProfile = false;
//		$("#time").prop('disabled', true);
//		$("#location").prop('disabled', true);
//		$("#selectProject").prop('disabled', true);
//		$("#finishedTags").prop('disabled', true);
//		$("#note").prop('disabled', true);
//
//		var d = $scope.receiptTime;
//		var dformat = [ (d.getMonth()+1).padLeft(),
//		                d.getDate().padLeft(),
//		                d.getFullYear()].join('-')+
//		                ' ' +
//		                [ d.getHours().padLeft(),
//		                  d.getMinutes().padLeft(),
//		                  d.getSeconds().padLeft()].join(':');
//		var  date = new Date(dformat);
//		var timestamp=Math.round(date.getTime());
//		var total = 0;
//		var x;
//		for(x in $scope.items){
//			total += $scope.items[x].quantity * $scope.items[x].price;
//		}
//		var allTag = [];
//		for(var y in $scope.tags){
//			allTag.push($scope.tags[y].text);
//		}
//		var data =  {
//				time:timestamp,
//				place: $scope.receiptLocation,
//				projectId: $scope.project.id,
//				note : $scope.receiptNote,
//				category: allTag
//			};
//		Receipts.save({uid:$scope.user.id},data,function(result){
//		receiptService.setEditReceipt(result);
//		$scope.receipt = result;
//		});
//	}
 
   $scope.upload = function() {	   
      var formData = new FormData();
      if(!$('#photoFile').val()){
			return;
	  	}
      formData.append("file", $('#photoFile')[0].files[0]);
	  console.log($scope.project);
      $.ajax( {
		 url : '/user/' + $scope.user.id +'/pictures',
         type : 'POST',
         data : formData,
		 async: false,
         processData : false,
         contentType : false,
         success : processReturnData
      } );
      $('#photoFile').val('');
	  $scope.imageButtonArea = false;
	  $scope.basicProfile = true;
	  $scope.imageShowArea = true;
	  $scope.imageSelectArea = false;
   }

   var processReturnData = function(result){
//	   $scope.loadingPic = false;
	   $scope.items = result.list_of_items;
	   $scope.itemArea =true;
	   $scope.confirmArea = true;
	   $scope.basicFile = {};
	   $scope.basicFile.receiptTime = new Date( result.time );
	   console.log( typeof result.time);
		$scope.basicFile.receiptCategory = undefined;
		$scope.basicFile.receiptNote = undefined;
		$scope.projectList = projectService.getProjectList();
		$scope.basicFile.receiptLocation = result.place;
		$scope.receipt = result;
   }

   $scope.$on('$locationChangeStart', function( event ) {
    var answer = confirm("Are you sure you want to leave this page? Data not uploaded will not be saved")
    if (!answer) {
        event.preventDefault();
    }
});

	$scope.cancel = function(){
		window.location.href = '/#/user'
	};

	$scope.noImage = function(){
		$scope.imageSelectArea = false;
		$scope.itemArea = true;
		$scope.confirmArea = true;
		$scope.imageButtonArea = false;
		$scope.basicProfile = true;
		$scope.projectList = projectService.getProjectList();
		var date = new Date();
		$scope.basicFile = {};
		$scope.basicFile.receiptTime = date;
		$scope.basicFile.receiptLocation = undefined;
		$scope.basicFile.receiptCategory = undefined;
		$scope.basicFile.receiptNote = undefined;
		$scope.items = [];
		$scope.basicFile.tags = [];
		
	};

	$scope.confirmEditReceipt = function(){
		var d = $scope.basicFile.receiptTime;
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
			total += $scope.items[x].price;
		}
		total = (Math.round(total * 100) / 100);
		var allTag = [];
		for(var y in $scope.basicFile.tags){
			allTag.push($scope.basicFile.tags[y].text);
		}
		if(!$scope.basicFile.project){
			alert("Please choose a project for this receipt");
		}
		var data =  {
				ownerId:$scope.user.id,
				time:timestamp,
				place: $scope.basicFile.receiptLocation,
				projectId: $scope.basicFile.project.id,
				note : $scope.basicFile.receiptNote,
				picId : $scope.receipt.picId,
				category: allTag,
				list_of_items : $scope.items,
				total : total
			};
		if($scope.basicFile.project.id){
			if($scope.receipt){
				data.id = $scope.receipt.id;
				Receipts.update({uid:$scope.user.id,rid:$scope.receipt.id},data,function(){window.location.href = '/#/user';});
			}else{
				Receipts.save({uid:$scope.user.id},data,function(){window.location.href = '/#/user';});
			}
		}
	}

} ] );

tracker.controller('project.Controller', ['$scope', '$resource','userService','pagingService','Receipts','home', 'searchService','receiptService','projectService','Projects', function( $scope, $resource, userService,pagingService,Receipts,home, searchService, receiptService, projectService,Projects  ) {

	$scope.logout = function(){
		$.ajax('/logout',{type : 'POST'});
		window.location.href = '/#/';
	}

	var getCurrentUser = function(){
		console.log("name = " +userService.getUser().username )
		if(userService.getUser().username) {
			$scope.user = userService.getUser();
			getAllProject();
			
		} else {	
				home.get(function(loggedUser){
					$scope.user = loggedUser;
					userService.setUser(loggedUser);
					console.log("Username: " + userService.getUser().username);
					getAllProject();
					userService.setEditUser($scope.user);
				});	
		}
	}

	var getAllProject = function () {
		Projects.get({ uid : userService.getUser().id },function(result){
		projectService.setProjectList(result.content);
		$scope.projectList = result.content;
		$scope.responseContent = result;
		$scope.totalPage = result.totalPages;
		$scope.currentPage = result.number;
		});
	}
	getCurrentUser();

	$scope.currentPage = pagingService.getCurrentPage();
	$scope.userPerPage = pagingService.getSize();
	$scope.sizeList = pagingService.getSizeList();
	$scope.projectList = projectService.getProjectList();
	$scope.user = userService.getUser();

	var updateProjectList = function(result){
		projectService.setProjectList(result.content);
		$scope.projectList = result.content;
		$scope.responseContent = result;
		$scope.totalPage = result.totalPages;
		$scope.currentPage = result.number;
	}

	$scope.editUser = function(selectedUser){
		userService.setEditUser(selectedUser);
		window.location.href = '/#/editUser';
	}

	$scope.editProject = function(selectedProject){
		projectService.setEditProject(selectedProject);
		window.location.href = '/#/editProject';
	}

	$scope.createProject = function(){
		window.location.href = '/#/createProject';
	}

	$scope.jumpToReceipts = function(){
		window.location.href = '/#/user';
	}

	$scope.deleteProject = function(project){
		Projects.delete({uid:$scope.user.id,pid:project.id},function(){
			getAllProject();
		})
	}

//---------------paging ----------------------------------
$scope.prevPage = function(){
		pagingService.setCurrentPage(pagingService.getCurrentPage() - 1);
		Projects.get({uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize()} ,updateProjectList );	
	}

	$scope.nextPage = function(){
		pagingService.setCurrentPage(pagingService.getCurrentPage() + 1);
		Projects.get({uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize()} ,updateProjectList );		}

	$scope.setPage = function(){
	var page = document.getElementById("targetPage").value - 1;
		if(Number.isInteger(page)&& page<= $scope.totalPage && page>=0){
			$scope.currentPage = page;
			pagingService.setCurrentPage(page);
		Projects.get({uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize()} ,updateProjectList );			} else {
			alert('Please type in right number', 'ERROR');
		}
		$("#targetPage").val('');
	}

	$scope.setSize = function(){
		pagingService.setSize($scope.userPerPage);
		Projects.get({uid : $scope.user.id, page : pagingService.getCurrentPage(), size : pagingService.getSize()} ,updateProjectList );		
	}

}]);


tracker.controller('createProject.Controller', ['$scope', '$resource','userService','pagingService','Receipts','home', 'searchService','receiptService','projectService','Projects', function( $scope, $resource, userService,pagingService,Receipts,home, searchService, receiptService, projectService,Projects  ) {


	var getCurrentUser = function(){
		console.log("name = " +userService.getUser().username )
		if(userService.getUser().username) {
			$scope.user = userService.getUser();
			getAllProject();
			
		} else {	
				home.get(function(loggedUser){
					$scope.user = loggedUser;
					userService.setUser(loggedUser);
					console.log("Username: " + userService.getUser().username);
					getAllProject();
					userService.setEditUser($scope.user);
				});	
		}
	}

	var getAllProject = function () {
		Projects.get({ uid : userService.getUser().id },function(result){
		projectService.setProjectList(result.content);
		$scope.projectList = result.content;
		});
	}
	getCurrentUser();

	$scope.cancel = function(){
		window.location.href = '/#/project';
	}

	Number.prototype.padLeft = function(base,chr){
		var  len = (String(base || 10).length - String(this).length)+1;
		return len > 0? new Array(len).join(chr || '0')+this : this;
	}

	var transferToTimestamp = function(timelocal){
		var d = timelocal;
		var dformat = [ (d.getMonth()+1).padLeft(),
		                d.getDate().padLeft(),
		                d.getFullYear()].join('-')+
		                ' ' +
		                [ d.getHours().padLeft(),
		                  d.getMinutes().padLeft(),
		                  d.getSeconds().padLeft()].join(':');
		var  date = new Date(dformat);
		var timestamp=Math.round(date.getTime());
		return timestamp;
	}

	$scope.createProject = function(){
		
		var data = {
			ownerId : $scope.user.id,
			name : $scope.newProject.name,
			startDate : transferToTimestamp($scope.newProject.startDate),
			endDate : transferToTimestamp($scope.newProject.endDate)
		}
		Projects.save({uid:$scope.user.id},data,function(){window.location.href = '/#/project';});
	}



}]);


tracker.controller('editProject.Controller', ['$scope', '$resource','userService','pagingService','Receipts','home', 'searchService','receiptService','projectService','Projects', function( $scope, $resource, userService,pagingService,Receipts,home, searchService, receiptService, projectService,Projects  ) {

	Number.prototype.padLeft = function(base,chr){
		var  len = (String(base || 10).length - String(this).length)+1;
		return len > 0? new Array(len).join(chr || '0')+this : this;
	}

	var transferToTimestamp = function(timelocal){
		var d = timelocal;
		var dformat = [ (d.getMonth()+1).padLeft(),
		                d.getDate().padLeft(),
		                d.getFullYear()].join('-')+
		                ' ' +
		                [ d.getHours().padLeft(),
		                  d.getMinutes().padLeft(),
		                  d.getSeconds().padLeft()].join(':');
		var  date = new Date(dformat);
		var timestamp=Math.round(date.getTime());
		return timestamp;
	}

	var getCurrentUser = function(){
		console.log("name = " +userService.getUser().username )
		if(userService.getUser().username) {
			$scope.user = userService.getUser();
			getAllProject();
			
		} else {	
				home.get(function(loggedUser){
					$scope.user = loggedUser;
					userService.setUser(loggedUser);
					console.log("Username: " + userService.getUser().username);
					getAllProject();
					userService.setEditUser($scope.user);
				});	
		}
	}

	var getAllProject = function () {
		Projects.get({ uid : userService.getUser().id },function(result){
		projectService.setProjectList(result.content);
		$scope.projectList = result.content;
		});
	}
	getCurrentUser();

	$scope.projectList = projectService.getProjectList();
	$scope.user = userService.getUser();
	console.log(projectService.getEditProject());
	var project = projectService.getEditProject();
	$scope.pname = project.name;
	console.log($scope.pname);
	$scope.psdate = new Date(project.startDate);
	$scope.pedate = new Date(project.endDate);


	$scope.updateProject = function(){
		
		var data = {
			name : $scope.pname,
			startDate : transferToTimestamp($scope.psdate),
			endDate : transferToTimestamp($scope.pedate)
		}
		Projects.update({uid:$scope.user.id,pid:project.id},data,function(){window.location.href = '/#/project';});
	}

	$scope.cancel = function(){
		window.location.href = '/#/project';
	}

}]);

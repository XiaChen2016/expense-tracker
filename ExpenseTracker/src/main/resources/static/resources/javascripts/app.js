var app = angular.module('App', [
   'ngRoute',
   'Tracker'
]);


app.config(['$routeProvider',
  function($routeProvider) {

	$routeProvider.
      when('/', {
        templateUrl: 'resources/views/login.html',
        controller: 'Login.Controller'
      }).
      when('/createProject', {
        templateUrl: 'resources/views/createProject.html',
        controller: 'createProject.Controller'
      }).
      when('/project', {
        templateUrl: 'resources/views/project.html',
        controller: 'project.Controller'
      }).
      when('/editProject', {
        templateUrl: 'resources/views/editProject.html',
        controller: 'editProject.Controller'
      }).
      when('/user', {
        templateUrl: 'resources/views/userHome.html',
        controller: 'userHome.Controller'
      }).

      when('/admin', {
        templateUrl: 'resources/views/adminHome.html',
        controller: 'adminHome.Controller'
      }).
      when('/createUser', {
        templateUrl: 'resources/views/createUser.html',
        controller: 'createUser.Controller'
      }).
      when('/editUser', {
        templateUrl: 'resources/views/editUser.html',
        controller: 'editUser.Controller'
      }).
      when('/editReceipt', {
        templateUrl: 'resources/views/editReceipt.html',
        controller: 'editReceipt.Controller'
      }).
      when('/createReceipt', {
        templateUrl: 'resources/views/createReceipt.html',
        controller: 'createReceipt.Controller'
      }).
      otherwise({
        redirectTo: '/'
      });
  }]);

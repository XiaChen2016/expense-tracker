var app = angular.module('App', [
   'ngRoute',
   'Tracker'
]);

//app.use( session( {
//   cookieName : 'session',
//   secret : 'A#*BAKRAABLGOA@G!ej%d>d3hGOAbkao35DF',
//   duration : 60 * 1000, // 60 seconds
//   activeDuration : 60*1000
//} ) );

app.config(['$routeProvider',
  function($routeProvider) {    
	
	$routeProvider.
      when('/', {
        templateUrl: 'resources/views/login.html',
        controller: 'Login.Controller'
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

      otherwise({
        redirectTo: '/'
      });
  }]);

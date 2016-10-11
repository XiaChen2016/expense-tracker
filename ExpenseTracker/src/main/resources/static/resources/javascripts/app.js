var app = angular.module('App', [
   'ngRoute',
   'Tracker'
]);

app.use( session( {
   cookieName : 'session',
   secret : 'A#*BAKRAABLGOA@G!ej%d>d3hGOAbkao35DF',
   duration : 60 * 1000, // 60 seconds
   activeDuration : 60*1000
} ) );

app.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/', {
        templateUrl: 'views/login.html',
        controller: 'Login.Controller'
      }).
      when('/home', {
        templateUrl: 'views/userHome.html',
        controller: 'userHome.Controller'
      }).

      when('/adminHome', {
        templateUrl: 'views/adminHome.html',
        controller: 'adminHome.Controller'
      }).
      when('/createUser', {
        templateUrl: 'views/createUser.html',
        controller: 'createUser.Controller'
      }).

      otherwise({
        redirectTo: '/'
      });
  }]);

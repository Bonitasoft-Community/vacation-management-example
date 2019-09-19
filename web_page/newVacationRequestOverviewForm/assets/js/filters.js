angular.module('bonitasoft.ui.extensions',[])
    .filter('fromNow', [function (format) {
       return function fromNow(input) {
     	    return moment(input,format).fromNow();
       };
    }]);

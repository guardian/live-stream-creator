import angular from 'angular';
import './new-stream-link.css!';
import template from './new-stream-link.html!text';

export const newStreamLink = angular.module('lv.newStream', []);

newStreamLink.directive('newStreamLink', [function () {
    return {
        restrict: 'E',
        template: template
    };
}]);

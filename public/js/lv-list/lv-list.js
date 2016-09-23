import angular from 'angular';

import './lv-list.css!';
import template from './lv-list.html!text';

export const lvList = angular.module('lv.list', [

]);

lvList.controller('lvListController', ['$scope', '$http', function ($scope, $http) {
    const ctrl = this;

    $http.get('/api/youtube/channel/list')
        .success((data) => {
            ctrl.channels = data;
        });
}]);

lvList.directive('lvList', [function () {
    return {
        restrict: 'E',
        controller: 'lvListController',
        controllerAs: 'ctrl',
        bindToController: true,
        template: template
    };
}]);

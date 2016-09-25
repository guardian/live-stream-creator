import angular from 'angular';

import './lv-list.css!';
import template from './lv-list.html!text';

export const lvList = angular.module('lv.list', []);

lvList.controller('lvListCtrl', ['streamApi', function (streamApi) {
    const ctrl = this;

    streamApi.list().then(streams => {
        ctrl.streams = streams.data;
    });
}]);

lvList.directive('lvList', [function () {
    return {
        restrict: 'E',
        controller: 'lvListCtrl',
        controllerAs: 'ctrl',
        bindToController: true,
        template: template
    };
}]);

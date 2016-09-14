import angular from 'angular';

import './lv-create.css!';
import template from './lv-create.html!text';

export const lvList = angular.module('lv.create', [

]);

lvList.controller('lvCreateController', ['$scope', '$http', function ($scope, $http) {
    const ctrl = this;

    $http.get('/api/wowza/incoming/list/live')
        .success((data) => {
            ctrl.wowzaStreams = data;
        });

    $http.get('/api/youtube/channel/list')
        .success((data) => {
            ctrl.channels = data;
        });

    ctrl.submit = () => {
        $scope.formData.wowzaApp = 'live';

        const requestData = {
            data: $scope.formData
        };

        $http.post('/api/youtube/stream/create', requestData)
            .success((data) => {
                console.log(data);
            });
    }
}]);

lvList.directive('lvCreate', [function () {
    return {
        restrict: 'E',
        controller: 'lvCreateController',
        controllerAs: 'ctrl',
        bindToController: true,
        template: template
    }
}]);

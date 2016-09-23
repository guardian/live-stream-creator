import angular from 'angular';

import './lv-create.css!';
import template from './lv-create.html!text';

export const lvList = angular.module('lv.create', [

]);

lvList.controller('lvCreateController', ['$scope', '$http', '$q', '$sce', 'apiPoll', 'streamApi', function ($scope, $http, $q, $sce, apiPoll, streamApi) {
    const ctrl = this;

    streamApi.root.follow('incoming-wowza', {id: 'live'})
        .get()
        .then((resp) => {
            ctrl.wowzaStreams = resp.data;
        })
        .catch((resp) => {
            if (resp.status === 404 && resp.body.errorKey === 'not-found') {
                ctrl.incomingFail = true;
            }
        });

    streamApi.root.follow('outgoing-youtube')
        .get()
        .then((resp) => {
            ctrl.channels = resp.data;
        });

    ctrl.submit = () => {
        $scope.formData.wowzaApp = 'live';

        streamApi.create($scope.formData)
            .then((resp) => {
                console.log(resp);
                ctrl.monitored = true;
                ctrl.stream = resp.data.data;
                ctrl.monitorEmbed = $sce.trustAsResourceUrl(`https://www.youtube.com/embed/${ctrl.stream.videoId}?autoplay=1`)
            })
            .catch((resp) => {
                console.log(resp);
            });
    };
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

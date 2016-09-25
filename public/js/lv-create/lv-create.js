import angular from 'angular';

import './lv-create.css!';
import template from './lv-create.html!text';

export const lvList = angular.module('lv.create', [

]);

lvList.controller('lvCreateCtrl', [
    '$scope',
    '$http',
    '$q',
    '$sce',
    'apiPoll',
    'streamApi',
    'youtubeChannelApi',
    'wowzaIncomingApi',
    function ($scope, $http, $q, $sce, apiPoll, streamApi, youtubeChannelApi, wowzaIncomingApi) {
        const ctrl = this;

        wowzaIncomingApi.list().then(resp => ctrl.wowzaStreams = resp)
            .catch (resp => {
                if (resp.status === 404 && resp.body.errorKey === 'not-found') {
                    ctrl.incomingFail = true;
                }
            });

        youtubeChannelApi.list().then(resp => ctrl.channels = resp);

        ctrl.submit = () => {
            $scope.formData.wowzaApp = 'live';

            streamApi.create($scope.formData)
                .then((resp) => {
                    ctrl.monitored = true;
                    ctrl.stream = resp.data.data;
                    ctrl.monitorEmbed = $sce.trustAsResourceUrl(`https://www.youtube.com/embed/${ctrl.stream.videoId}?autoplay=1`);
                })
                .catch(() => {

                });
        };
    }
]);

lvList.directive('lvCreate', [function () {
    return {
        restrict: 'E',
        controller: 'lvCreateCtrl',
        controllerAs: 'ctrl',
        bindToController: true,
        template: template
    };
}]);

import angular from 'angular';

import './lv-create.css!';
import template from './lv-create.html!text';

import '../components/wz-stream-preview/wz-stream-preview';

export const lvList = angular.module('lv.create', [
    'wz.preview'
]);

lvList.controller('lvCreateCtrl', [
    '$rootScope',
    '$scope',
    '$location',
    'streamApi',
    'youtubeChannelApi',
    'wowzaIncomingApi',
    function ($rootScope, $scope, $location, streamApi, youtubeChannelApi, wowzaIncomingApi) {
        const ctrl = this;

        wowzaIncomingApi.list()
            .then(resp => ctrl.wowzaStreams = resp.data)
            .catch (resp => {
                if (resp.status === 404 && resp.body.errorKey === 'not-found') {
                    ctrl.wowzaStreams = [];
                    ctrl.incomingFailure = true;
                }
            });

        youtubeChannelApi.list()
            .then(resp => ctrl.channels = resp.data);

        ctrl.creatingStream = false;

        ctrl.logs = [];

        ctrl.submit = () => {
            ctrl.logs.push('creating YouTube stream');
            ctrl.creatingStream = true;
            streamApi.create(ctrl.newStream);
        };

        $rootScope.$on('stream-active', () => {
            ctrl.logs.push('waiting for YouTube to receive stream');
        });

        $rootScope.$on('stream-in-testing', () => {
            ctrl.logs.push('YouTube looks healthy');
            ctrl.logs.push('starting YouTube Live Video');
        });

        $rootScope.$on('stream-started', (event, stream) => {
            $location.path(`stream/${stream.data.id}`);
        });
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

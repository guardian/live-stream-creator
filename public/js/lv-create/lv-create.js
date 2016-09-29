import angular from 'angular';

import './lv-create.css!';
import template from './lv-create.html!text';

import '../components/wz-stream-preview/wz-stream-preview';

export const lvList = angular.module('lv.create', [
    'wz.preview'
]);

lvList.controller('lvCreateCtrl', [
    '$scope',
    '$location',
    'streamApi',
    'youtubeChannelApi',
    'wowzaIncomingApi',
    function ($scope, $location, streamApi, youtubeChannelApi, wowzaIncomingApi) {
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

        ctrl.submit = () => {
            streamApi.create(ctrl.newStream)
                .then(resp => {
                    ctrl.stream = resp.data;
                    $location.path(`stream/${ctrl.stream.data.id}`);
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

import angular from 'angular';

import './lv-list.css!';
import template from './lv-list.html!text';

import '../components/yt-channel/yt-channel';
import '../components/new-stream-link/new-stream-link';

export const lvList = angular.module('lv.list', ['yt.channel', 'lv.newStream']);

lvList.controller('lvListCtrl', ['streamApi', function (streamApi) {
    const ctrl = this;

    streamApi.list()
        .then(streams => {
            ctrl.streams = streams.data;
            //TODO be better, don't mutate!
            ctrl.streams.forEach(s => streamApi.performHealthcheck(s).then(hc => s.healthcheck = hc));
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

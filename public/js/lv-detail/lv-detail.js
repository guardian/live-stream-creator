import angular from 'angular';

import '../components/yt-channel/yt-channel';

import './lv-detail.css!';
import template from './lv-detail.html!text';

export const lvDetail = angular.module('lv.detail', ['yt.channel']);

lvDetail.controller('lvDetailCtrl', ['$sce', '$routeParams', '$location', '$rootScope', 'streamApi', function ($sce, $routeParams, $location, $rootScope, streamApi) {
    const ctrl = this;

    ctrl.streamId = $routeParams.id;

    const getEmbedCode = (videoId, isLive) => {
        const base = `https://www.youtube.com/embed/${videoId}`;
        return isLive ? `${base}?autoplay=1` : base;
    };

    const performHealthcheck = () => {
        streamApi.performHealthcheck(ctrl.stream)
            .then(healthcheck => {
                ctrl.healthcheck = healthcheck.data;
                ctrl.isCurrentlyLive = ctrl.healthcheck.broadcastStatus === 'live';
                ctrl.embedSrc = $sce.trustAsResourceUrl(getEmbedCode(ctrl.stream.data.videoId, ctrl.isCurrentlyLive));
            });
    };

    ctrl.stopStream = () => streamApi.stop(ctrl.stream);

    streamApi.get(ctrl.streamId).then(stream => {
        ctrl.stream = stream;
        performHealthcheck();
    });

    $rootScope.$on('stream-stopped', (event, stream) => {
        $location.path(`stream/${stream.data.id}`);
    });
}]);

lvDetail.directive('lvDetail', [function () {
    return {
        restrict: 'E',
        controller: 'lvDetailCtrl',
        controllerAs: 'ctrl',
        bindToController: true,
        template: template
    };
}]);

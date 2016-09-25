import angular from 'angular';

import '../components/yt-channel/yt-channel';

import './lv-detail.css!';
import template from './lv-detail.html!text';

export const lvDetail = angular.module('lv.detail', ['yt.channel']);

lvDetail.controller('lvDetailController', ['$sce', 'streamApi', function ($sce, streamApi) {
    const ctrl = this;

    const getEmbedCode = (videoId, isLive) => {
        const base = `https://www.youtube.com/embed/${videoId}`;
        return isLive ? `${base}?autoplay=1` : base;
    };

    ctrl.stopStream = () => {
        streamApi.stop(ctrl.stream)
            .then((resp) => {
                console.log(resp)
            })
            .catch(x => {
                console.log(x);
            });
    };

    streamApi.performHealthcheck(ctrl.stream)
        .then(healthcheck => {
            ctrl.healthcheck = healthcheck.data;
            ctrl.isCurrentlyLive = ctrl.healthcheck.broadcastStatus === 'live';
            ctrl.embedSrc = $sce.trustAsResourceUrl(getEmbedCode(ctrl.stream.data.videoId, ctrl.isCurrentlyLive));
        });
}]);

lvDetail.directive('lvDetail', [function () {
    return {
        restrict: 'E',
        controller: 'lvDetailController',
        controllerAs: 'ctrl',
        bindToController: true,
        template: template,
        scope: {
            stream: '='
        }
    };
}]);

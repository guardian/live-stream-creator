import angular from 'angular';

import './lv-create.css!';
import template from './lv-create.html!text';

export const lvList = angular.module('lv.create', [

]);

lvList.controller('lvCreateController', ['$scope', '$http', '$q', 'apiPoll', 'streamApi', function ($scope, $http, $q, apiPoll, streamApi) {
    const ctrl = this;

    streamApi.list()
        .then((x) => {
            console.log(x);
        });

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
                debugger;
            })
            .catch((resp) => {
                debugger;
            });
    };


    // $http.get('/api/incoming/live')
    //     .success((data) => {
    //         ctrl.wowzaStreams = data;
    //     });
    //
    // $http.get('/api/destinations/youtube')
    //     .success((data) => {
    //         ctrl.channels = data;
    //     });
    //
    // ctrl.submit = () => {
    //     $scope.formData.wowzaApp = 'live';
    //
    //     const requestData = {
    //         data: $scope.formData
    //     };
    //
    //     $http.post('/api/streams', requestData).success((data) => {
    //         const monitorData = {
    //             data: { monitor: true }
    //         };
    //
    //         const healthUrl = data.data.links.find((x) => x.rel === 'health').href;
    //
    //         const untilStreamIsActive = () => {
    //             $http.get(healthUrl).success((resp) => resp.data.data.status === 'active' ? true : $q.reject())
    //         };
    //
    //         debugger;
    //         apiPoll(() => untilStreamIsActive());
    //         debugger;
    //         // $http.put(url, monitorData)
    //         //     .success((data) => {
    //         //         console.log(data);
    //         //     });
    //     });
    // }
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

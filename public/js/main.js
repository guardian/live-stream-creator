import angular from 'angular';
import './lv-list/lv-list';
import './lv-create/lv-create';

const app = angular.module('liveVideo', [
    'lv.list',
    'lv.create'
]);

app.directive('testDir', [function () {
    return {
        restrict: 'E',
        template: `<lv-create></lv-create>`
    }
}]);


angular.element(document).ready(function() {
    angular.bootstrap(document, ['liveVideo']);
});

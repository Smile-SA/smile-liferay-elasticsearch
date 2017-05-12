(function(Smile, ElasticsearchManagementPortlet) {

    var REINDEX_ACTION = 'REINDEX';
    var GET_MAPPINGS_ACTION = 'GET_MAPPINGS';
    var GET_SETTINGS_ACTION = 'GET_SETTINGS';

    indexReactor.registerEvent('executeAction');

    ElasticsearchManagementPortlet.init = function(url, namespace) {

        AUI().use(['node'], function (A) {
            function initButton(buttonType, actionType) {
                A.all('button.' + buttonType).on('click', function (e) {
                    var $this = A.one(e.currentTarget);
                    if (!$this.hasClass('disabled')) {
                        $this.addClass('disabled');
                        dispatchAction($this, actionType);
                    }
                });
            }

            function dispatchAction(node, action) {
                indexReactor.dispatchEvent('executeAction', {
                    name: action,
                    namespace: namespace,
                    index: node.getAttribute('data-index-name'),
                    node: node,
                    url: url
                });
            }

            initButton('reindex', REINDEX_ACTION);
            initButton('mappings', GET_MAPPINGS_ACTION);
            initButton('settings', GET_SETTINGS_ACTION);
        });
    };

    ElasticsearchManagementPortlet.doAction = function(action, callback) {
        var data = {};
        data[action.namespace + 'index'] = action.index;
        data[action.namespace + 'action'] = action.name;

        AUI().use(['aui-io-request'], function(A) {
            A.io.request(action.url, {
                method: 'post',
                dataType: 'json',
                data: data,
                on: {
                    success: function () {
                        if (typeof callback === 'function') {
                            callback(this.get('responseData'));
                        }
                    }
                }
            });
        });
    };

    ElasticsearchManagementPortlet.resetButton = function(node) {
        node.removeClass('disabled');
    };

}(window.Smile = window.Smile || {}, window.Smile.ElasticsearchManagementPortlet = window.Smile.ElasticsearchManagementPortlet || {}));


(function(Smile, ElasticsearchManagementPortlet) {

    var rowIndexClass = '#row-index-';
    var processedItemsClass = '.processed-items';
    var totalItemsClass = '.total-items';

    function updateIndexRow(index, data) {
        AUI().use(['node'], function(A) {
            var $row = A.one(rowIndexClass + index);

            $row.all(processedItemsClass).setHTML(data.processedItems);
            $row.all(totalItemsClass).setHTML(data.totalItems);
        });
    }

    indexReactor.addEventListener('executeAction', function(action) {
        if (action.name === 'REINDEX') {
            ElasticsearchManagementPortlet.doAction(action, function callback(data) {
                var pendingInterval = setInterval(function() {
                    if (data.status === 'PENDING') {
                        ElasticsearchManagementPortlet.doAction(action, function(updatedData) {
                            data = updatedData;
                            updateIndexRow(action.index, data);
                        });
                    } else {
                        clearInterval(pendingInterval);
                        updateIndexRow(action.index, data);
                        ElasticsearchManagementPortlet.resetButton(action.node);
                    }
                }, 2000);
            });
        }
    });

}(window.Smile, window.Smile.ElasticsearchManagementPortlet));
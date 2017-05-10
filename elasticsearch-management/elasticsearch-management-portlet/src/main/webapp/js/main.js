(function(Smile, ElasticsearchManagementPortlet, undefined) {

    var url;

    ElasticsearchManagementPortlet.init = function(urlInput) {
        url = urlInput;

        AUI().use(['node', 'aui-io-request'], function(A) {

            var reindexButtons = A.all("button.run");
            reindexButtons.each(function (node, index) {

                node.on("click", function(em) {
                    node.addClass("disabled");
                    runJob(node, 'run');
                });
            });
        });
    };

    function reindex(node, action){
        AUI().use('aui-io-request', function(A){

            A.io.request(
                url,
                {
                    method: 'post',
                    dataType: 'json',
                    data: {
                        jobId: job,
                        action: action
                    },
                    on: {
                        success: function() {
                            var jobStatus = this.get('responseData').status;
                            var total = this.get('responseData').total;
                            var processed = this.get('responseData').processed;

                            totalItemsElement.set('text', total);
                            processedItemsElement.set('text', processed);

                            if (jobStatus == 'PENDING') {
                                setTimeout(function(){ runJob(node, 'check_status'); }, 1000);
                            } else {
                                node.removeClass("disabled");
                            }
                        }
                    }
                }
            );
        });
    }
}(window.Smile = window.Smile || {}, window.Smile.ElasticsearchManagementPortlet = window.Smile.ElasticsearchManagementPortlet || {}));


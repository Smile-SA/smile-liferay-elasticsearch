(function(Smile, ElasticsearchManagementPortlet) {

    indexReactor.addEventListener('executeAction', function(action) {
        if (action.name === 'GET_SETTINGS') {
            AUI().use(['aui-modal'], function(A) {

                var $form = A.one('#' + action.namespace + 'form-settings-' + action.index);
                var $inputFile = $form.one('[type=file]');

                $inputFile.on('change', function() {
                    $form.submit();
                });

                var modal = new A.Modal({
                    headerContent: 'Informations',
                    bodyContent: 'Chargement en cours',
                    modal: true,
                    centered: true,
                    render: '#index-modal',
                    width: 450,
                    toolbars: {
                        footer: [
                            {
                                label: 'Fermer',
                                cssClass: 'pull-left',
                                on: {
                                    click: function() {
                                        modal.hide();
                                    }
                                }
                            },
                            {
                                label: 'Import setting file',
                                cssClass: 'pull-right',
                                on: {
                                    click: function() {
                                        $inputFile.simulate('click');
                                    }
                                }
                            }
                        ]
                    }
                }).render();

                ElasticsearchManagementPortlet.doAction(action, function(data) {
                    if (data.settings) {
                        var renderHtml = Prism.highlight(JSON.stringify(data.settings), Prism.languages.json);

                        modal.set('bodyContent', renderHtml);
                        modal.hide();
                        modal.show();
                    }

                    ElasticsearchManagementPortlet.resetButton(action.node);
                });
            });
        }
    });

}(window.Smile, window.Smile.ElasticsearchManagementPortlet));
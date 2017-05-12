(function(Smile, ElasticsearchManagementPortlet) {

    indexReactor.addEventListener('executeAction', function(action) {
        if (action.name === 'GET_MAPPINGS') {
            AUI().use(['node', 'aui-modal'], function(A) {

                var $form = A.one('#' + action.namespace + 'form-mappings-' + action.index);
                var $inputFile = $form.one('[type=file]');

                $inputFile.on('change', function() {
                    $form.submit();
                });

                var modal = new A.Modal({
                    headerContent: '<strong>Informations</strong>',
                    bodyContent: 'Chargement en cours',
                    modal: true,
                    centered: true,
                    draggable: false,
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
                                label: 'Import mapping file',
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

                /*modal.getToolbar('footer').on('click'*/

                ElasticsearchManagementPortlet.doAction(action, function(data) {
                    if (data.mappings) {
                        var renderHtml = Prism.highlight(JSON.stringify(data.mappings), Prism.languages.json);

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
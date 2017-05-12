<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%--@elvariable id="indices" type="lava.lang.String[]"--%>
<portlet:defineObjects />
<portlet:resourceURL var="resourceURL"/>

<liferay-ui:success key="update-mappings-success" message="update.mappings.success" translateMessage="true" />
<liferay-ui:error key="update-mappings-error" message="update.mappings.error" translateMessage="true" />
<liferay-ui:error key="no-mappings-error" message="update.mappings.none" translateMessage="true" />

<liferay-ui:success key="update-settings-success" message="update.settings.success" translateMessage="true" />
<liferay-ui:error key="update-settings-error" message="update.settings.error" translateMessage="true" />
<liferay-ui:error key="no-settings-error" message="update.settings.none" translateMessage="true" />

<h2><liferay-ui:message key="list.job.scheduler" /></h2>

<table class="table table-striped table-bordered">
    <thead>
    <tr>
        <td><liferay-ui:message key="index.name" /></td>
        <%--<td><liferay-ui:message key="index.type" /></td>--%>
        <td><liferay-ui:message key="index.totaldocuments" /></td>
        <td><liferay-ui:message key="index.progression" /></td>
        <td><liferay-ui:message key="index.actions" /></td>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${indices}" var="index">
        <tr id="row-index-${index.name}">
            <td class="name-item">${index.name}</td>
            <%--<td></td>--%>
            <td class="total-items">${index.totalHits}</td>
            <td>
                <span class="processed-items">0</span> / <span class="total-items">${index.totalHits}</span>
            </td>
            <td class="actions">
                <div class="btn-group">
                    <aui:button cssClass="btn btn-primary reindex" icon="icon-play" value="index.reindex" data-index-name="${index.name}" />
                    <aui:button cssClass="btn btn-primary mappings" icon="icon-edit" value="index.mappings" data-index-name="${index.name}" />
                    <aui:button cssClass="btn btn-primary settings" icon="icon-edit" value="index.settings" data-index-name="${index.name}" />
                </div>

                <portlet:actionURL name="updateMappings" var="updateMappingsURL">
                    <portlet:param name="index" value="${index.name}" />
                </portlet:actionURL>
                <portlet:actionURL name="updateSettings" var="updateSettingsURL">
                    <portlet:param name="index" value="${index.name}" />
                </portlet:actionURL>

                <aui:form name="form-mappings-${index.name}" action="${updateMappingsURL}" cssClass="hidden" enctype="multipart/form-data">
                    <aui:input type="file" name="file" />
                </aui:form>
                <aui:form name="form-settings-${index.name}" action="${updateSettingsURL}" cssClass="hidden" enctype="multipart/form-data">
                    <aui:input type="file" name="file" />
                </aui:form>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<div id="index-modal"></div>

<script>
    AUI().ready(function() {
        Smile.ElasticsearchManagementPortlet.init('${resourceURL}', '<portlet:namespace />');
    });
</script>

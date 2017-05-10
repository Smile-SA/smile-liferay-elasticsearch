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

<h2><liferay-ui:message key="list.job.scheduler" /></h2>

<table class="table table-striped table-bordered">
    <thead>
    <tr>
        <td><liferay-ui:message key="index.name" /></td>
        <td><liferay-ui:message key="index.type" /></td>
        <td><liferay-ui:message key="index.totaldocuments" /></td>
        <td><liferay-ui:message key="index.progression" /></td>
        <td><liferay-ui:message key="index.actions" /></td>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${indices}" var="index">
        <tr>
            <td>${index.name}</td>
            <td></td>
            <td>${index.totalHits}</td>
            <td>
                <span class="processed-items"></span> / <span class="total-items"></span>
            </td>
            <td class="actions">
                <div class="btn-group">
                    <aui:button cssClass="btn btn-primary reindex" icon="icon-play" value="index.reindex" />
                    <aui:button cssClass="btn btn-primary mappings" icon="icon-edit" value="index.mappings" />
                    <aui:button cssClass="btn btn-primary settings" icon="icon-edit" value="index.settings" />
                </div>
            </td>
        </tr>

    </c:forEach>
    </tbody>
</table>

<script>
    AUI().ready(function() {
        Smile.ElasticsearchManagementPortlet.init('${resourceURL}');
    });
</script>

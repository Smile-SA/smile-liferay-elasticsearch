<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%--@elvariable id="index" type="java.util.List"--%>
<portlet:defineObjects />
<portlet:resourceURL var="resourceURL"/>

<h2><liferay-ui:message key="list.job.scheduler" /></h2>

    <table class="table table-striped table-bordered">
        <thead>
        <tr>
            <td><liferay-ui:message key="index.name" /></td>
            <td><liferay-ui:message key="index.type" /></td>
            <td><liferay-ui:message key="index.totaldocuments" /></td>
            <td><liferay-ui:message key="index.last-call" /></td>
            <td><liferay-ui:message key="index.progression" /></td>
            <td><liferay-ui:message key="index.actions" /></td>
        </tr>
        </thead>
        <tbody>
            <tr>
                <td>${index.name}</td>
                <td>${index.type}</td>
                <td>${index.type}</td>
                <td>
                </td>
                <td>
                    <span class="processed-items"></span> / <span class="total-items"></span>
                </td>
                <td class="actions">
                    <div class="btn-group">
                        <c:if test="${index.status != 'READY'}">
                            <c:set var="runButtonCssClass" value="disabled"/>
                        </c:if>

                        <aui:button cssClass="btn btn-primary reindex ${runButtonCssClass}" icon="icon-play" value="index.reindex" />
                    </div>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <script>
        AUI().ready(function() {
            Smile.BatchProcessingPortlet.init('${resourceURL}');
        });
    </script>
</c:if>

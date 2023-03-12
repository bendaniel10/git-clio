<!doctype html>
<html>

<head>
    <#include "/include/general_page_style.ftl">
    <title>git-clio</title>
</head>

<body>
    <div class="container">
        <#include "/include/general_page_nav.ftl">
            <h1>Reports</h1>
            <br />
            <div class="row float-end">
                <div class="col">
                    <a href="/create_report" class="btn btn-primary btn-lg" role="button" aria-pressed="true">New Report</a>
                </div>
            </div>

            <br />
            <table class="table table-striped table-hover">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Status</th>
                        <th>Issues</th>
                        <th>Pull Requests</th>
                    </tr>
                </thead>
                <tbody>
                    <#list reports as report>
                        <tr>
                            <td>
                                <a href="/view_report_details?report_id=${report.id}">${report.name}</a>
                            </td>
                            <td>
                                ${report.status}
                            </td>
                            <td>
                                ${report.issues}
                            </td>
                            <td>
                                ${report.pullRequests}
                            </td>
                        </tr>
                    </#list>
                </tbody>
            </table>
    </div>
    <#include "/include/general_page_script.ftl">
</body>

</html>
<!doctype html>
<html>

<head>
    <#include "/include/general_page_style.ftl">
        <title>git-clio</title>
</head>

<body>
    <div class="container">
        <#include "/include/general_page_nav.ftl">
            <h1>Create Report</h1>
            <br /> <br />
            <div class="row">
                <form method="post" action="/create_report" class="col-6">
                    <div class="mb-3">
                        <label for="report_name" class="form-label">Report Name</label>
                        <input id="report_name" type="text" class="form-control" name="report_name">
                    </div>
                    <div class="mb-3 row">
                        <div class="col">
                            <div class="form-group">
                                <label for="github_username" class="form-label">GitHub Username</label>
                                <input id="github_username" type="text" class="form-control" name="github_username">
                            </div>
                        </div>
                        <div class="col">
                            <div class="form-group">
                                <label for="github_pat" class="form-label">GitHub Personal Access Token</label>
                                <input id="github_pat" type="password" class="form-control" name="github_pat">
                                <small class="form-text text-muted">Require scopes: read:org, read:user, read:project &
                                    repo.</small>
                            </div>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label for="github_org" class="form-label">GitHub Organization</label>
                        <input id="github_org" type="text" class="form-control" name="github_org">
                    </div>
                    <div class="mb-3">
                        <label for="github_repo" class="form-label">GitHub Repository</label>
                        <input id="github_repo" type="text" class="form-control" name="github_repo">
                    </div>
                    <div class="mb-3 row">
                        <div class="col">
                            <div class="form-group">
                                <label for="analytics_start" class="form-label">Start</label>
                                <input id="analytics_start" type="date" name="analytics_start" class="form-control">
                            </div>
                        </div>
                        <div class="col">
                            <div class="form-group">
                                <label for="analytics_end" class="form-label">End</label>
                                <input id="analytics_end" type="date" name="analytics_end" class="form-control">
                            </div>
                        </div>
                    </div>

                    <br /> <br />
                    <div class="float-end">
                        <button class="btn btn-primary btn-lg active" aria-pressed="true" type="submit"
                            name="action">Create Report</button>
                    </div>
                </form>
            </div>
    </div>
    <#include "/include/general_page_script.ftl">
</body>

</html>
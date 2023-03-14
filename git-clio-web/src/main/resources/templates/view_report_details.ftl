<!doctype html>
<html>

<head>
    <#include "/include/general_page_style.ftl">
        <title>git-clio</title>
</head>

<body>
    <div class="container">
        <#include "/include/general_page_nav.ftl">
            <h1>${details.reportName}</h1>
            <br /> <br />
            <ul class="nav nav-tabs" id="myTab" role="tablist">
                <li class="nav-item" role="presentation">
                    <button class="nav-link active" id="pull-request-tab" data-bs-toggle="tab"
                        data-bs-target="#pull-request" type="button" role="tab" aria-controls="pull-request"
                        aria-selected="true">Pull Requests</button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="issues-tab" data-bs-toggle="tab" data-bs-target="#issues" type="button"
                        role="tab" aria-controls="issues" aria-selected="false">Issues</button>
                </li>
            </ul>

            <div class="tab-content">
                <div class="tab-pane active" id="pull-request" role="tabpanel" aria-labelledby="pull-request-tab"
                    tabindex="0">
                    <div class="row">
                        <div class="col-5 mt-5">
                            <div class="row">
                                <div class="col">
                                    <div class="card">
                                        <div class="card-body">
                                            <div class="float-end">
                                                <i class="bi bi-graph-up"></i>
                                            </div>
                                            <h6 class="card-subtitle text-secondary">Total PRs</h6>
                                            <h2 class="mt-3 mb-3">${details.prs}</h2>
                                            <p class="mb-0 text-muted">
                                                <span class="text-success">${details.averagePrPerDay}</span>
                                                <span class="text-nowrap">per day</span>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                                <div class="col">
                                    <div class="card">
                                        <div class="card-body">
                                            <div class="float-end">
                                                <i class="bi bi-file-diff"></i>
                                            </div>
                                            <h6 class="card-subtitle text-secondary">Open PRs</h6>
                                            <h2 class="mt-3 mb-3">${details.openPrs}</h2>
                                            <p class="mb-0 text-muted">
                                                <span class="text-danger">${details.openPrsPercentage}%</span>
                                                <span class="text-nowrap">of total</span>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col mt-5">
                                    <div class="card">
                                        <div class="card-body">
                                            <div class="float-end">
                                                <i class="bi bi-check2"></i>
                                            </div>
                                            <h6 class="card-subtitle text-secondary">Merged PRs</h6>
                                            <h2 class="mt-3 mb-3">${details.merged}</h2>
                                            <p class="mb-0 text-muted">
                                                <span class="text-success">${details.mergedPrsPercentage}%</span>
                                                <span class="text-nowrap">of total</span>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                                <div class="col mt-5">
                                    <div class="card">
                                        <div class="card-body">
                                            <div class="float-end">
                                                <i class="bi bi-trash"></i>
                                            </div>
                                            <h6 class="card-subtitle text-secondary">Dismissed PRs</h6>
                                            <h2 class="mt-3 mb-3">${details.dismissedPrs}</h2>
                                            <p class="mb-0 text-muted">
                                                <span class="text-success">${details.dismissedPrsPercentage}%</span>
                                                <span class="text-nowrap">of total</span>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-7 mt-5">
                            <div class="card">
                                <div class="card-header">
                                    <h4 class="header-title">PRs created by month</h4>
                                </div>
                                <div class="card-body">
                                    <canvas class="mt-3 ms-3" id="prByMonthChart"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="row mt-5">
                        <div class="col-4">
                            <div class="card">
                                <div class="card-header">
                                    <h4 class="header-title">PRs status</h4>
                                </div>
                                <div class="card-body">
                                    <canvas class="mt-3 ms-3" id="openVsMergedVsDismissedPrs"></canvas>
                                </div>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="card">
                                <div class="card-header">
                                    <h4 class="header-title">Top creators</h4>
                                </div>
                                <div class="card-body">
                                    <div class="table-responsive">
                                        <table class="table table-striped table-hover">
                                            <tbody>
                                                <#list details.topPRCreators as prCreator>
                                                    <tr>
                                                        <td>${prCreator.position}</td>
                                                        <td>${prCreator.name}</td>
                                                        <td>${prCreator.value}</td>
                                                    </tr>
                                                </#list>
                                            </tbody>
                                        </table>
                                        <small>Only showing the top 8</small>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="card">
                                <div class="card-header">
                                    <h4 class="header-title">Auto-merged PRs</h4>
                                </div>
                                <div class="card-body">
                                    <canvas class="mt-3 ms-3" id="manualVsAutoMergePrs"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="tab-pane" id="issues" role="tabpanel" aria-labelledby="issues-tab" tabindex="0">
                    <div class="row">
                        <p>Hello issues</p>
                    </div>
                </div>
            </div>
    </div>
    <br /> <br />
    <#include "/include/general_page_script.ftl">
        <#include "/include/view_report_details_chart_script.ftl">
</body>

</html>
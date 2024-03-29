<!doctype html>
<html>
<head>
    <#include "/include/general_page_style.ftl">
    <title>git-clio</title>
</head>

<body>
    <div class="container">
        <#include "/include/general_page_nav.ftl">
            <h1>${reportName}</h1>
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
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="vanity-tab" data-bs-toggle="tab" data-bs-target="#vanity" type="button"
                        role="tab" aria-controls="vanity" aria-selected="false">Vanity Awards</button>
                </li>
            </ul>

            <div class="tab-content">
                <div class="tab-pane active" id="pull-request" role="tabpanel" aria-labelledby="pull-request-tab"
                    tabindex="0">
                    <div class="row mt-5">
                        <div class="col-9">
                            <#if prCategory == "pr_overview">
                                <#include "/reportdetails/pr_overview.ftl">
                            <#elseif prCategory == "prs_by_month">
                                <#include "/reportdetails/prs_by_month.ftl">
                            <#elseif prCategory == "pr_status">
                                <#include "/reportdetails/pr_status.ftl">
                            <#elseif prCategory == "pr_creators">
                                <#include "/reportdetails/pr_creators.ftl">
                            <#elseif prCategory == "pr_auto_merge_status">
                                <#include "/reportdetails/pr_auto_merge_status.ftl">
                            <#elseif prCategory == "pr_changes_overview">
                                <#include "/reportdetails/pr_changes_overview.ftl">
                            <#elseif prCategory == "pr_comments_overview">
                                <#include "/reportdetails/pr_comments_overview.ftl">
                            <#elseif prCategory == "pr_commits_overview">
                                <#include "/reportdetails/pr_commits_overview.ftl">
                            <#elseif prCategory == "pr_merge_duration">
                                <#include "/reportdetails/pr_merge_duration.ftl">
                            <#elseif prCategory == "pr_created_by_time_of_day">
                                <#include "/reportdetails/pr_created_by_time_of_day.ftl">
                            <#else>
                                <#include "/reportdetails/pr_overview.ftl">
                            </#if>
                        </div>
                        <div class="col-3">
                            <div class="card">
                                <div class="card-header">
                                  Categories
                                </div>
                                <ul class="list-group list-group-flush">
                                  <li class="list-group-item"><a href="/view_report_details?report_id=${reportId}&pr_category=pr_overview">Overview</a></li>
                                  <li class="list-group-item"><a href="/view_report_details?report_id=${reportId}&pr_category=prs_by_month">PRs by month</a></li>
                                  <li class="list-group-item"><a href="/view_report_details?report_id=${reportId}&pr_category=pr_status">PR Status</a></li>
                                  <li class="list-group-item"><a href="/view_report_details?report_id=${reportId}&pr_category=pr_merge_duration">Merge duration</a></li>
                                  <li class="list-group-item"><a href="/view_report_details?report_id=${reportId}&pr_category=pr_creators">PR Creators</a></li>
                                  <li class="list-group-item"><a href="/view_report_details?report_id=${reportId}&pr_category=pr_changes_overview">Code Changes</a></li>
                                  <li class="list-group-item"><a href="/view_report_details?report_id=${reportId}&pr_category=pr_auto_merge_status">Auto-Merge PRs</a></li>
                                  <li class="list-group-item"><a href="/view_report_details?report_id=${reportId}&pr_category=pr_comments_overview">Comments</a></li>
                                  <li class="list-group-item"><a href="/view_report_details?report_id=${reportId}&pr_category=pr_commits_overview">Commits</a></li>
                                  <li class="list-group-item"><a href="/view_report_details?report_id=${reportId}&pr_category=pr_created_by_time_of_day">Created by Hour</a></li>
                                </ul>
                              </div>
                        </div>
                    </div>
                </div>
                <div class="tab-pane" id="issues" role="tabpanel" aria-labelledby="issues-tab" tabindex="0">
                    <div class="row mt-5">
                        <div class="col-9">

                        </div>
                        <div class="col-3">
                            <div class="card">
                                <div class="card-header">
                                  Categories
                                </div>
                                <ul class="list-group list-group-flush">
                                  <li class="list-group-item"><a href="">Overview</a></li>
                                  <li class="list-group-item"><a href="">Overview</a></li>
                                  <li class="list-group-item"><a href="">Overview</a></li>
                                  <li class="list-group-item"><a href="">Overview</a></li>
                                </ul>
                              </div>
                        </div>
                    </div>
                </div>
                <div class="tab-pane" id="vanity" role="tabpanel" aria-labelledby="vanity-tab" tabindex="0">
                    <div class="row mt-5">
                        <div class="col-9">

                        </div>
                        <div class="col-3">
                            <div class="card">
                                <div class="card-header">
                                  Categories
                                </div>
                                <ul class="list-group list-group-flush">
                                  <li class="list-group-item"><a href="">Overview</a></li>
                                  <li class="list-group-item"><a href="">Overview</a></li>
                                  <li class="list-group-item"><a href="">Overview</a></li>
                                  <li class="list-group-item"><a href="">Overview</a></li>
                                </ul>
                              </div>
                        </div>
                    </div>
                </div>
            </div>
    </div>
    <br /> <br />
    <#include "/include/general_page_script.ftl">
</body>
</html>
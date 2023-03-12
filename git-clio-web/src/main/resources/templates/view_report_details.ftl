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
                        <div class="col-5 mt-4">
                            <div class="row">
                                <div class="col mt-3 mb-3">
                                    <div class="card">
                                        <div class="card-body">
                                            <div class="float-end">
                                                <i class="bi bi-sign-merge-right-fill"></i>
                                            </div>
                                            <h6 class="card-subtitle text-secondary">Total PRs</h6>
                                            <h2 class="mt-3 mb-3">${details.prs}</h2>
                                            <p class="mb-0 text-muted">
                                                <span class="text-danger">&nbsp;</span>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                                <div class="col mt-3 mb-3">
                                    <div class="card">
                                        <div class="card-body">
                                            <div class="float-end">
                                                <i class="bi bi-file-diff"></i>
                                            </div>
                                            <h6 class="card-subtitle text-secondary">Open</h6>
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
                                <div class="col mt-3 mb-3">
                                    <div class="card">
                                        <div class="card-body">
                                            <div class="float-end">
                                                <i class="bi bi-check2"></i>
                                            </div>
                                            <h6 class="card-subtitle text-secondary">Merged</h6>
                                            <h2 class="mt-3 mb-3">${details.merged}</h2>
                                            <p class="mb-0 text-muted">
                                                <span class="text-success">${details.mergedPrsPercentage}%</span>
                                                <span class="text-nowrap">of total</span>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                                <div class="col mt-3 mb-3">
                                    <div class="card">
                                        <div class="card-body">
                                            <div class="float-end">
                                                <i class="bi bi-check2-all"></i>
                                            </div>
                                            <h6 class="card-subtitle text-secondary">Auto-merged</h6>
                                            <h2 class="mt-3 mb-3">${details.autoMerged}</h2>
                                            <p class="mb-0 text-muted">
                                                <span class="text-success">${details.autoMergedPrsPercentage}%</span>
                                                <span class="text-nowrap">of total</span>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-7">
                            <canvas class="mt-3 ms-5" id="prByMonthChart"></canvas>
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
    <#include "/include/general_page_script.ftl">
    <script>
      const ctx = document.getElementById('prByMonthChart');

      new Chart(ctx, {
        type: 'line',
        data: {
          labels: [${details.monthToPrsPair.labels}],
          datasets: [{
            label: ${details.monthToPrsPair.title},
            data: [${details.monthToPrsPair.values}],
            borderWidth: 1
          }]
        },
        options: {
          scales: {
            y: {
              beginAtZero: true
            }
          }
        }
      });
    </script>
</body>

</html>
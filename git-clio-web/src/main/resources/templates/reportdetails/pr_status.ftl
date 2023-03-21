<div class="row">
    <div class="col-4">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-file-diff"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Open PRs</h6>
                <h2 class="mt-3 mb-3">${details.openPrs}</h2>
            </div>
        </div>
        <div class="card mt-3">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-check2"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Merged PRs</h6>
                <h2 class="mt-3 mb-3">${details.merged}</h2>
            </div>
        </div>
        <div class="card mt-3">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-trash"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Dismissed PRs</h6>
                <h2 class="mt-3 mb-3">${details.dismissedPrs}</h2>
            </div>
        </div>
    </div>
    <div class="col-8">
        <div class="card">
            <div class="card-header">
                <h4 class="header-title">PRs status</h4>
            </div>
            <div class="card-body">
                <canvas id="openVsMergedVsDismissedPrs"></canvas>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
  const openVsMergedVsDismissedPrs = document.getElementById('openVsMergedVsDismissedPrs');

  new Chart(openVsMergedVsDismissedPrs, {
    type: 'doughnut',
    data: {
      labels: [${details.openVsMergedVsDismissedPrs.labels}],
      datasets: [{
        label: ${details.openVsMergedVsDismissedPrs.title},
        data: [${details.openVsMergedVsDismissedPrs.values}],
        backgroundColor: [${details.openVsMergedVsDismissedPrs.backgroundColors}],
        hoverOffset: 4
      }]
    }
  });
</script>
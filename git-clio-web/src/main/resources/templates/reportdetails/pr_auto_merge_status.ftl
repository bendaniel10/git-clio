<div class="row">
    <div class="col-4">
        <div class="card">
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
                    <i class="bi bi-hand-index"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Manual Merged PRs</h6>
                <h2 class="mt-3 mb-3">${details.manualMerge}</h2>
            </div>
        </div>
        <div class="card mt-3">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-check2-all"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Auto-Merged PRs</h6>
                <h2 class="mt-3 mb-3">${details.autoMerged}</h2>
            </div>
        </div>
    </div>
    <div class="col-8">
        <div class="card">
            <div class="card-header">
                <h4 class="header-title">Auto-merged PRs</h4>
            </div>
            <div class="card-body">
                <canvas id="manualVsAutoMergePrs"></canvas>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
  const manualVsAutoMergePrs = document.getElementById('manualVsAutoMergePrs');

  new Chart(manualVsAutoMergePrs, {
    type: 'doughnut',
    data: {
      labels: [${details.manualVsAutoMergedPrs.labels}],
      datasets: [{
        label: ${details.manualVsAutoMergedPrs.title},
        data: [${details.manualVsAutoMergedPrs.values}],
        backgroundColor: [${details.manualVsAutoMergedPrs.backgroundColors}],
        hoverOffset: 4
      }]
    }
  });
</script>
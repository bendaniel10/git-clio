<div class="row">
    <div class="col-4">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-git"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Commits</h6>
                <h2 class="mt-3 mb-3">${details.commits}</h2>
                <p class="mb-0 text-muted">
                    <span>${details.averageCommitPerPR}</span>
                    <span class="text-nowrap">per PR</span>
                </p>
            </div>
        </div>
    </div>
</div>
<div class="row mt-5">
    <div class="col">
        <div class="card">
            <div class="card-header">
                <h4 class="header-title">Commits distribution</h4>
            </div>
            <div class="card-body">
                <canvas id="commitByCountDist"></canvas>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
  const commitByCountDist = document.getElementById('commitByCountDist');

  new Chart(commitByCountDist, {
    type: 'scatter',
    data: {
      labels: [${details.commitByCountDist.labels}],
      datasets: [
          {
            label: ${details.commitByCountDist.title},
            data: [${details.commitByCountDist.values}],
            borderWidth: 1
          }
      ]
    },
    options: {
      scales: {
        y: {
          beginAtZero: true,
          title: {display: true, text: 'Total PRs'}
        },
        x: {
          beginAtZero: true,
          title: {display: true, text: 'Amount of commits'}
        }
      }
    }
  });

</script>
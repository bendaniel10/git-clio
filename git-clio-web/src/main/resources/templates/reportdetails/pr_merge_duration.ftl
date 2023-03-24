<div class="row">
    <div class="col">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-clock-history"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Average Duration</h6>
                <h2 class="mt-3 mb-3">${details.averageDurationHours}h</h2>
            </div>
        </div>
    </div>
    <div class="col">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-hourglass-bottom"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Shortest duration</h6>
                <h2 class="mt-3 mb-3">${details.shortestDurationHours}h</h2>
            </div>
        </div>
    </div>
    <div class="col">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-hourglass-top"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Longest Duration</h6>
                <h2 class="mt-3 mb-3">${details.longestDurationHours}h</h2>
            </div>
        </div>
    </div>
</div>
<div class="row mt-5">
    <div class="col">
        <div class="card">
            <div class="card-header">
                <h4 class="header-title">Duration Distribution</h4>
            </div>
            <div class="card-body">
                <canvas id="durationByCount"></canvas>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
  const durationByCount = document.getElementById('durationByCount');
  new Chart(durationByCount, {
    type: 'scatter',
    data: {
      labels: [${details.durationByCount.labels}],
      datasets: [
          {
            label: ${details.durationByCount.title},
            data: [${details.durationByCount.values}],
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
          title: {display: true, text: 'Hours taken'}
        }
      }
    }
  });
</script>
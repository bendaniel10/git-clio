<div class="row">
    <div class="col">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-file-earmark-plus"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Additions</h6>
                <h2 class="mt-3 mb-3">${details.additions}</h2>
            </div>
        </div>
    </div>
    <div class="col">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-file-minus"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Deletions</h6>
                <h2 class="mt-3 mb-3">${details.deletions}</h2>
            </div>
        </div>
    </div>
    <div class="col">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-file-code"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Files Changed</h6>
                <h2 class="mt-3 mb-3">${details.filesChanged}</h2>
            </div>
        </div>
    </div>
</div>
<div class="row mt-5">
    <div class="col">
        <div class="card">
            <div class="card-header">
                <h4 class="header-title">Changes By Month</h4>
            </div>
            <div class="card-body">
                <canvas id="changesByMonth"></canvas>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
  const changesByMonth = document.getElementById('changesByMonth');

  new Chart(changesByMonth, {
    type: 'bar',
    data: {
      labels: [${details.changesPerMonth.labels}],
      datasets: [
          {
            label: ${details.changesPerMonth.first.label},
            data: [${details.changesPerMonth.first.values}],
            borderWidth: 1
          },
          {
            label: ${details.changesPerMonth.second.label},
            data: [${details.changesPerMonth.second.values}],
            borderWidth: 1
          }
      ]
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
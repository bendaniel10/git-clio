<div class="row">
    <div class="col">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-brightness-alt-high"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Early</h6>
                <h2 class="mt-3 mb-3">${details.early}</h2>
                <p class="mb-0 text-muted">
                    <span>${details.earlyPercentage}%</span>
                    <span class="text-nowrap">before 07:00</span>
                </p>
            </div>
        </div>
    </div>
    <div class="col">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-moon-stars"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Late</h6>
                <h2 class="mt-3 mb-3">${details.late}</h2>
                <p class="mb-0 text-muted">
                    <span>${details.latePercentage}%</span>
                    <span class="text-nowrap">after 19:59</span>
                </p>
            </div>
        </div>
    </div>
    <div class="col">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-graph-up-arrow"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Popular Hour</h6>
                <h2 class="mt-3 mb-3">${details.popularHour}:00</h2>
                <p class="mb-0 text-muted">
                    <span>${details.popularHourCount}</span>
                    <span class="text-nowrap">created at this hour</span>
                </p>
            </div>
        </div>
    </div>
    <div class="col">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-graph-down-arrow"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Unpopular Hour</h6>
                <h2 class="mt-3 mb-3">${details.unpopularHour}:00</h2>
                <p class="mb-0 text-muted">
                    <span>${details.unpopularHourCount}</span>
                    <span class="text-nowrap">created at this hour</span>
                </p>
            </div>
        </div>
    </div>
</div>
<div class="row mt-5">
    <div class="col">
        <div class="card">
            <div class="card-header">
                <h4 class="header-title">Creation Distribution</h4>
            </div>
            <div class="card-body">
                <canvas id="countByHourDist"></canvas>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
  const countByHourDist = document.getElementById('countByHourDist');
  new Chart(countByHourDist, {
    type: 'bubble',
    data: {
      datasets: [
          {
            label: ${details.countByHourDist.title},
            data: [${details.countByHourDist.xyrValues}]
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
          title: {display: true, text: 'Hours of the day'}
        }
      }
    }
  });
</script>
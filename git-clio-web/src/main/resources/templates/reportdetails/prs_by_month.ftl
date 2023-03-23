<div class="card">
    <div class="card-header">
        <h4 class="header-title">PRs created by month</h4>
    </div>
    <div class="card-body">
        <canvas id="prByMonthChart"></canvas>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
  const prByMonthChart = document.getElementById('prByMonthChart');

  new Chart(prByMonthChart, {
    type: 'line',
    data: {
      labels: [${details.prsPerMonth.labels}],
      datasets: [{
        label: ${details.prsPerMonth.title},
        data: [${details.prsPerMonth.values}],
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
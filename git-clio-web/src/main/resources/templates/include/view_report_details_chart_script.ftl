<script>
  const prByMonthChart = document.getElementById('prByMonthChart');

  new Chart(prByMonthChart, {
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
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
<div class="row">
    <div class="col">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-chat-left-quote"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Comments</h6>
                <h2 class="mt-3 mb-3">${details.comments}</h2>
                <p class="mb-0 text-muted">
                    <span>${details.averageCommentPerPr}</span>
                    <span class="text-nowrap">per PR</span>
                </p>
            </div>
        </div>
    </div>
    <div class="col">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-chat-left-quote-fill"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Review Comments</h6>
                <h2 class="mt-3 mb-3">${details.reviewComments}</h2>
                <p class="mb-0 text-muted">
                    <span>${details.averageReviewCommentPerPr}</span>
                    <span class="text-nowrap">per PR</span>
                </p>
            </div>
        </div>
    </div>
    <div class="col">
    </div>
</div>
<div class="row mt-5">
    <div class="col">
        <div class="card">
            <div class="card-header">
                <h4 class="header-title">Comments distribution</h4>
            </div>
            <div class="card-body">
                <canvas id="commentsByCountDist"></canvas>
            </div>
        </div>
    </div>
</div>
<div class="row mt-5">
    <div class="col">
        <div class="card">
            <div class="card-header">
                <h4 class="header-title">Review distribution</h4>
            </div>
            <div class="card-body">
                <canvas id="reviewCommentsByCountDist"></canvas>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
  const commentsByCountDist = document.getElementById('commentsByCountDist');

  new Chart(commentsByCountDist, {
    type: 'scatter',
    data: {
      labels: [${details.commentsByCountDist.labels}],
      datasets: [
          {
            label: ${details.commentsByCountDist.title},
            data: [${details.commentsByCountDist.values}],
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
          title: {display: true, text: 'Amount of comments'}
        }
      }
    }
  });
  const reviewCommentsByCountDist = document.getElementById('reviewCommentsByCountDist');

  new Chart(reviewCommentsByCountDist, {
    type: 'scatter',
    data: {
      labels: [${details.reviewCommentsByCountDist.labels}],
      datasets: [
          {
            label: [${details.reviewCommentsByCountDist.title}],
            data: [${details.reviewCommentsByCountDist.values}],
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
          title: {display: true, text: 'Amount of comments'}
        }
      }
    }
  });
</script>
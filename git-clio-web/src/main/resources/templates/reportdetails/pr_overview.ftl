<div class="row">
    <div class="col">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-graph-up"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Total PRs</h6>
                <h2 class="mt-3 mb-3">${details.prs}</h2>
                <p class="mb-0 text-muted">
                    <span class="text-success">${details.averagePrPerDay}</span>
                    <span class="text-nowrap">per day</span>
                </p>
            </div>
        </div>
    </div>
    <div class="col">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-file-diff"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Open PRs</h6>
                <h2 class="mt-3 mb-3">${details.openPrs}</h2>
                <p class="mb-0 text-muted">
                    <span class="text-danger">${details.openPrsPercentage}%</span>
                    <span class="text-nowrap">of total</span>
                </p>
            </div>
        </div>
    </div>
    <div class="col">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-trash"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Dismissed PRs</h6>
                <h2 class="mt-3 mb-3">${details.dismissedPrs}</h2>
                <p class="mb-0 text-muted">
                    <span class="text-success">${details.dismissedPrsPercentage}%</span>
                    <span class="text-nowrap">of total</span>
                </p>
            </div>
        </div>
    </div>
</div>
<div class="row">
    <div class="col mt-5">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-check2"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Merged PRs</h6>
                <h2 class="mt-3 mb-3">${details.merged}</h2>
                <p class="mb-0 text-muted">
                    <span class="text-success">${details.mergedPrsPercentage}%</span>
                    <span class="text-nowrap">of total</span>
                </p>
            </div>
        </div>
    </div>
    <div class="col mt-5">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-check2-all"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Auto-Merged PRs</h6>
                <h2 class="mt-3 mb-3">${details.autoMerged}</h2>
                <p class="mb-0 text-muted">
                    <span class="text-success">${details.autoMergedPercentage}%</span>
                    <span class="text-nowrap">of merged</span>
                </p>
            </div>
        </div>
    </div>
    <div class="col mt-5">
        <div class="card">
            <div class="card-body">
                <div class="float-end">
                    <i class="bi bi-chat-left-quote"></i>
                </div>
                <h6 class="card-subtitle text-secondary">Total PR Comments</h6>
                <h2 class="mt-3 mb-3">${details.comments}</h2>
                <p class="mb-0 text-muted">
                    <span class="text-success">${details.averageCommentPerPR}</span>
                    <span class="text-nowrap">per PR</span>
                </p>
            </div>
        </div>
    </div>
</div>
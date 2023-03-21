<div class="card">
    <div class="card-header">
        <h4 class="header-title">Top creators</h4>
    </div>
    <div class="card-body">
        <div class="table-responsive">
            <table class="table table-striped table-hover">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Username</th>
                        <th>Total PRs</th>
                    </tr>
                </thead>
                <tbody>
                    <#list details.topPRCreators as prCreator>
                        <tr>
                            <td>${prCreator.position}</td>
                            <td>${prCreator.name}</td>
                            <td>${prCreator.value}</td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </div>
</div>
# git-clio

Pull fun stats from Github about your repository

## How to run

Define a `local.properties` file in the root of the project containing these properties:

```properties
githubUsername=<username>
githubPersonalAccessToken=<token_with_correct_read_scopes>
githubOrganization=<org_where_the_repo_resides>
githubRepository=<repo_to_analyze>
analyticsStartDate=<analytics_start_date> //yyyy-mm-dd
analyticsEndDate=<analytics_start_date> //yyyy-mm-dd
```

Execute the main function in `git-clio-parse` and grab a coffee. The location of the report file will be displayed at
the end of the analysis.

## Long analysis time
The issues/prs are processed in batches of a max of 100 (Github max items per page). Each item in processed concurrently. There could be possible ways to make the process faster but this improvement will come in a future update. 

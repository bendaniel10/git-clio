# git-clio
Pull fun stats from Github about your repository

## How to run
Define a `local.properties` file in the root of the project containing these properties:

```
githubUsername=<username>
githubPersonalAccessToken=<token_with_correct_read_scopes>
githubOrganization=<org_where_the_repo_resides>
githubRepository=<repo_to_analyze>
analyticsStartDate=<analytics_start_date> //yyyy-mm-dd
analyticsEndDate=<analytics_start_date> //yyyy-mm-dd
```
Execute the main function in `git-clio-parse` and grab a coffee. 

## Long analysis time
I tried this in a repo with over 2K PRs within a time range and it took 35 minutes. There are still some improvements to be made regarding parallel execution of some request. These will come later.

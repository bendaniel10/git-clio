# git-clio

Pull fun stats from Github about your repository

## How to run (Using Docker)
On the root project folder
```shell
docker-compose -f docker-compose.yml up -d --build
```

And to shut down the services:
```shell
docker-compose down
```

## Project Endpoints:
- GitClio Web interface: http://localhost:2546/
- Adminer (DB ui): http://localhost:8080/ (server: postgreSQL, server: database:5432, username: gitclio, password: gitclio, database: gitclio)

## Long analysis time
The issues/prs are processed in batches of a max of 100 (Github max items per page). Each item in processed concurrently. There could be possible ways to make the process faster but this improvement will come in a future update. 

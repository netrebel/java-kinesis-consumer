# java-kinesis-consumer

- [java-kinesis-consumer](#java-kinesis-consumer)
    - [Requirements](#requirements)
    - [How to run](#how-to-run)
    - [AWS cli commands](#aws-cli-commands)

## Requirements

1. Local `~/.aws/credential` profile with name: `local-testing`:

```properties
[local-testing]
output = json
region = us-east-1
aws_secret_access_key = fakeSecretAccessKey
aws_access_key_id = fakeMyKeyId
```

2. VM Runtime Option: `-DMINIKUBE_IP=localhost`
3. Localstack running on Minikube with port `32420`
4. Create Kinesis stream before starting the app:
```
AWS_PROFILE=local-testing aws --endpoint-url=http://localhost:32420 kinesis create-stream --stream-name DataChangeEventsStream --shard-count 1
```

## How to run

1. Run Application in IntelliJ
2. Run: `make test-kinesis`
3. Search logs for

## AWS cli commands

```
AWS_PROFILE=local-testing aws --endpoint-url=http://localhost:32420 kinesis list-streams
AWS_PROFILE=local-testing aws --endpoint-url=http://localhost:32420 kinesis delete-stream --stream-name DataChangeEventsStream
```

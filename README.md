# java-kinesis-consumer

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

## How to run

1. Run Application in IntelliJ
2. Run: `make test-kinesis`
3. Search logs for 

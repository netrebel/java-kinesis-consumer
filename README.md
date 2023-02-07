# java-kinesis-consumer

- [java-kinesis-consumer](#java-kinesis-consumer)
  - [Requirements](#requirements)
  - [How to run](#how-to-run)
  - [AWS cli commands](#aws-cli-commands)
  - [Services needed](#services-needed)
    - [Localstack](#localstack)
    - [Dynamodb](#dynamodb)
  - [Localstack Kubernetes (minikube) yaml](#localstack-kubernetes-minikube-yaml)

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
3. Environment Variable: `AWS_CBOR_DISABLE=true`. Specified in spring-cloud-stream-binder-aws-kinesis [docs](https://github.com/spring-cloud/spring-cloud-stream-binder-aws-kinesis/blob/v3.0.0/spring-cloud-stream-binder-kinesis-docs/src/main/asciidoc/overview.adoc#telling-the-binder-to-use-your-local-endpoint)
4. Localstack running on Minikube with port `32420`. M1s, make sure your port-forward is running: `kubectl port-forward service/dynamodb 30080:8000`
5. Create Kinesis stream before starting the app: `make create-stream`

## How to run

1. Run Application in IntelliJ
2. Run `make test-kinesis` to publish a message to the stream
3. Search application log for: "Event Payload"

## AWS cli commands

If you need to recreate the kinesis stream, you can use the following:
- `make list-kinesis-streams`
- `make delete-kinesis-stream`

## Services needed

### Localstack

`spring-cloud-stream-binder-kinesis` needs the following services enabled in Localstack:
- kinesis
- cloudwatch

Spring Beans for `AmazonCloudWatch` and `AmazonKinesisAsync` need to be instantiated so that they connect to the Localstack endpoint, instead of the AWS service. Otherwise, you will get an error such as:

> com.amazonaws.services.cloudwatch.model.AmazonCloudWatchException: The security token included in the request is invalid. (Service: AmazonCloudWatch; Status Code: 403; Error Code: InvalidClientTokenId; Request ID: c9e7e995-2256-4dc2-aed1-90608a5f8ae6; Proxy: null)

### Dynamodb

`spring-cloud-stream-binder-kinesis` also uses DynamoDB to keep track of the Kinesis reader offset.

DynamoDB Local is provided as an executable .jar file. The application runs on Windows, Linux, macOS, and other platforms that support Java.

Follow these steps to set up and run DynamoDB on your computer:
https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html

[dynamodb-admin](https://www.npmjs.com/package/dynamodb-admin) offers a UI to explore the tables

A Spring Bean for `AmazonDynamoDB` needs to be instantiated so that it connects to the Localstack endpoint instead of the AWS service.

## Localstack Kubernetes (minikube) yaml

Here's a sample (untested) Kubernetes yaml that you could `kubectl apply`

```yaml
---
apiVersion: v1
kind: Service
metadata:
  name: aws-localstack
  labels:
    app: aws-localstack
spec:
  ports:
    - port: 4566
      targetPort: 4566
      nodePort: 32420
      name: aws-localstack
  type: NodePort
  selector:
    app: aws-localstack
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: aws-localstack
  labels:
    app: aws-localstack
spec:
  replicas: 1
  selector:
    matchLabels:
      app: aws-localstack
  template:
    metadata:
      labels:
        app: aws-localstack
    spec:
      volumes:
        - name: dockersock
          hostPath:
            path: /var/run/docker.sock
        - name: localstack-storage
          persistentVolumeClaim:
            claimName: docker-pv-claim
      containers:
        - name: aws-localstack
          image: localstack/localstack:0.14.0
          imagePullPolicy: IfNotPresent
          resources:
            requests:
              memory: "100Mi"
              cpu: "100m"
          ports:
            - name: localstack-port
              containerPort: 4566
            - name: kinesis-port
              containerPort: 4568
          volumeMounts:
            - name: dockersock
              mountPath: /var/run/docker.sock
            - name: localstack-storage
              mountPath: /tmp
              subPath: localstack
          env:
            - name:  SERVICES
              value: "kinesis,cloudwatch"
            - name:  DATA_DIR
              value: "/tmp"
            - name:  AWS_DEFAULT_REGION
              value: "us-east-1"
            - name:  AWS_ACCESS_KEY_ID
              value: "test"
            - name:  AWS_SECRET_ACCESS_KEY
              value: "test"
            - name:  DEBUG
              value: "1"
              # By default, localstack will return SQS urls using localhost, this doesn't help when running the service on minikube(make kube-run).
              # This will make localstack return urls using aws-localstack as host, this will work when running services in minikube. 
              # In order to work on local environment(Ex make jar-run, run from IDE), we need to add an aws-localstack host entry in /etc/hosts file using minikube ip.
            - name: HOSTNAME_EXTERNAL
              value: "aws-localstack"
---
kind: StorageClass
apiVersion: storage.k8s.io/v1
metadata:
  name: local-storage
provisioner: kubernetes.io/no-provisioner
volumeBindingMode: WaitForFirstConsumer
---
apiVersion: v1
kind: PersistentVolume
metadata:
  name: docker-pv
spec:
  storageClassName: local-storage
  capacity:
    storage: 1Gi
  persistentVolumeReclaimPolicy: Retain
  accessModes:
    - ReadWriteOnce
  hostPath:
    path: "/tmp/data/docker"
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: docker-pv-claim
spec:
  accessModes:
    - ReadWriteOnce
  storageClassName: local-storage
  resources:
    requests:
      storage: 1Gi
```

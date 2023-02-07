# Copyright (c) 2022 Life360, Inc. - All Rights Reserved
# Unauthorized copying of this file via any medium is strictly prohibited
# Proprietary and confidential

# verify minikube running before grabbing its ip
SERVICE_PORT_IN_MINIKUBE = 32123
MINIKUBE_IP ?= localhost
ifneq ($(shell arch), arm64)
  ifeq ($(MINIKUBE_IP),localhost)
    MINIKUBE_STATUS=$(shell minikube status)
    ifneq (,$(findstring Running, $(MINIKUBE_STATUS)))
      MINIKUBE_IP=$(shell minikube ip)
    endif
  endif
endif

test-kinesis:
	AWS_PROFILE=local-testing aws --endpoint-url=http://${MINIKUBE_IP}:32420 kinesis put-record --stream-name DataChangeEventsStream --data 'eyJldmVudFR5cGUiOiJkZWxldGUtdXNlciIsInZlcnNpb24iOjEsInRpbWVzdGFtcCI6MTY3NDc3MzQyNDY1MSwiY29udGVudCI6eyJ1c2VyVXVpZCI6ImNkNWY5MjE4LTQ1YmMtNDQxYi04M2NiLWRjZWEyMWM1MTgzZCJ9fQ==' --partition-key 1 > /tmp/mosaic-kinesis-local-test

create-kinesis-stream:
	AWS_PROFILE=local-testing aws --endpoint-url=http://localhost:32420 kinesis create-stream --stream-name DataChangeEventsStream --shard-count 1

delete-kinesis-stream:
	AWS_PROFILE=local-testing aws --endpoint-url=http://localhost:32420 kinesis delete-stream --stream-name DataChangeEventsStream

list-kinesis-streams:
	AWS_PROFILE=local-testing aws --endpoint-url=http://localhost:32420 kinesis list-streams

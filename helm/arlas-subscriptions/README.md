This is a [Helm](https://helm.sh/) chart packaging [ARLAS-subscriptions](https://github.com/gisaia/ARLAS-subscriptions).

It comprises 3 components:

- arlas-subscriptions-manager
- arlas-subscriptions-matcher
- arlas-subscriptions-server (an instance of [ARLAS-server](https://github.com/gisaia/ARLAS-server))

The Helm chart deploys a Kubernetes service for the manager, so that its API can be used.

# Compatibility

This Helm chart has been tested with:

- Helm 2.12
- Kubernetes 1.12

# Prerequisites

ARLAS-subscriptions relies on an Elasticsearch index for subscriptions. The chart can take care of initializing the index, if you explicitly set `manager.init.elasticsearchIndex.enabled: true`. If you want to manage the index on your own, you have to create it prior to installing the chart. Then, upon installation you should pass the following values:

- `elasticsearch.index`: name of the index
- `elasticsearch.mappingType.name`:  name of the index's [mapping type](https://www.elastic.co/guide/en/elasticsearch/reference/6.4/getting-started-concepts.html#_type)

# Values

When [installing](https://v2-14-0.helm.sh/docs/helm/#helm-install) the chart, here are the configuration [values](https://v2-14-0.helm.sh/docs/developing_charts/#values-files) you can parameter:

Value | Type | Required | Default | Description
-|-|-|-|-
`elasticsearch.endpoints.http` | [sequence](https://yaml.org/spec/1.2/spec.html#sequence//) | required if either `manager.init.elasticsearchIndex.enabled` or `matcher.init.serverCollection.enabled` | | [HTTP endpoints](https://www.elastic.co/guide/en/elasticsearch/reference/6.4/modules-http.html) for the Elasticsearch cluster the components are going to connect to, under the form `<host>:<port>`
`elasticsearch.endpoints.transport` | [sequence](https://yaml.org/spec/1.2/spec.html#sequence//) | required | | [Transport endpoints](https://www.elastic.co/guide/en/elasticsearch/reference/6.4/modules-transport.html) for the Elasticsearch cluster the components are going to connect to, under the form `<host>:<port>`
`elasticsearch.index` | string |  | `subs` | [Elasticsearch index](https://www.elastic.co/guide/en/elasticsearch/reference/6.4/getting-started-concepts.html#_index) for subscriptions
`elasticsearch.mappingType.name` | string |  | `sub` | Name of the [Elasticsearch mapping type](https://www.elastic.co/guide/en/elasticsearch/reference/6.4/getting-started-concepts.html#_type) for subscriptions
`elasticsearch.mappingType.definition` | [Elasticsearch Mapping type](https://www.elastic.co/guide/en/elasticsearch/reference/6.4/mapping.html#mapping-type) in JSON or YAML | required if `manager.init.elasticsearchIndex.enabled` |  | Definition of the [Elasticsearch mapping type](https://www.elastic.co/guide/en/elasticsearch/reference/6.4/getting-started-concepts.html#_type) for subscriptions, see example [here](https://github.com/gisaia/ARLAS-subscriptions/blob/develop/docs/example.mapping.json)
`fullnameOverride` | string | | | When the chart gets installed, all created Kubernetes objects are named based on the release name, for example the ARLAS subscription manager's deployment is named `<release name>-arlas-sub-manager`. `fullnameOverride` allows using something else as the base name: `<fullnameOverride>-arlas-sub-manager`.
`manager.affinity` | [io.k8s.api.core.v1.Affinity](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#affinity-v1-core) | | `{}` | Allows assigning the Kubernetes deployment to certain Kubernetes nodes. Official docs [here](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/)
`manager.environmentVariables` | [mapping](https://yaml.org/spec/1.2/spec.html#mapping//) | required | | Allows defining environment variables for the container. Available values are documented [here](https://github.com/gisaia/ARLAS-subscriptions#manager-configuration)
`manager.environmentVariables.MONGO_AUTH_DATABASE` | string | | | MongoDB database against which to authenticate
`manager.environmentVariables.MONGO_HOST` | string | required | | See definition [here](https://github.com/gisaia/ARLAS-subscriptions#manager-configuration)
`manager.environmentVariables.MONGO_PASSWORD` | string | | | Password for authentication to MongoDB
`manager.environmentVariables.MONGO_USERNAME` | string | | | Username for authentication to MongoDB
`manager.image.repository` | string |  | [`gisaia/arlas-subscriptions-manager`](hub.docker.com/r/gisaia/arlas-subscriptions-manager) | Docker image's repository
`manager.image.tag` | string |  | `0.0.1-SNAPSHOT` | Docker image's tag
`manager.image.pullPolicy` | string |  | `IfNotPresent` | Docker image's pull policy. See field `imagePullPolicy` in [reference for Kubernetes object `container`](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#container-v1-core)
`manager.imagePullSecrets` | sequence of strings |  | | Names of [imagePullSecrets](https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/) to be used by pod to pull docker images
`manager.init.elasticsearchIndex.container.image.pullPolicy` | string |  | `IfNotPresent` | There is an [initContainer](https://kubernetes.io/docs/concepts/workloads/pods/init-containers/) in charge of initializing the subscriptions' index in elasticsearch. This value defines image pull policy for this container. See field `imagePullPolicy` in [reference for Kubernetes object `container`](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#container-v1-core)
`manager.init.elasticsearchIndex.container.image.repository` | string |  | [`centos`](https://hub.docker.com/_/centos) | There is an initContainer in charge of initializing the subscriptions' index in elasticsearch. This value defines image repository for this container.
`manager.init.elasticsearchIndex.container.image.tag` | string |  | `7` | There is an initContainer in charge of initializing the subscriptions' index in elasticsearch. This value defines image tag for this container.
`manager.init.elasticsearchIndex.container.image.resources.limits.cpu` | string |  | `1` | There is an initContainer in charge of initializing the subscriptions' index in elasticsearch. This value defines maximum amount of CPU it is allowed to use. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-limits-are-run)
`manager.init.elasticsearchIndex.container.image.resources.limits.memory` | string |  | `1G` | There is an initContainer in charge of initializing the subscriptions' index in elasticsearch. This value defines maximum amount of memory it is allowed to use. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-limits-are-run)
`manager.init.elasticsearchIndex.container.image.resources.requests.cpu` | string |  | `1` | There is an initContainer in charge of initializing the subscriptions' index in elasticsearch. This value defines the minimum amount of CPU required on a Kubernetes node for this container to be scheduled onto it. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-requests-are-scheduled)
`manager.init.elasticsearchIndex.container.image.resources.requests.memory` | string |  | `1G` | There is an initContainer in charge of initializing the subscriptions' index in elasticsearch. This value defines the minimum amount of memory required on a Kubernetes node for this container to be scheduled onto it. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-requests-are-scheduled)
`manager.init.elasticsearchIndex.enabled` | boolean |  | `false` | There is an initContainer in charge of initializing the subscriptions' index in elasticsearch. This value allows disabling/enabling it.
`manager.nodeSelector` | [io.k8s.api.core.v1.NodeSelector](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/#nodeselector) |  | `{}` | Allows selecting the nodes on which the pod is to run
`manager.resources.limits.cpu` | string |  | `1` | Maximum amount of CPU the container is allowed to use. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-limits-are-run)
`manager.resources.limits.memory` | string |  | `4G` | Maximum amount of memory the container is allowed to use. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-limits-are-run)
`manager.resources.requests.cpu` | string |  | `1` | Minimum amount of CPU required on a Kubernetes node for this container to be scheduled onto it. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-requests-are-scheduled)
`manager.resources.requests.memory` | string |  | `4G` | Minimum amount of memory required on a Kubernetes node for this container to be scheduled onto it. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-requests-are-scheduled)
`manager.service.type` | string |  | `ClusterIP` | Type for the [Kubernetes service](https://kubernetes.io/docs/concepts/services-networking/service), see [documentation](https://kubernetes.io/docs/concepts/services-networking/service/#publishing-services-service-types)
`manager.service.port` | integer |  | `80` | Port on which the [Kubernetes service](https://kubernetes.io/docs/concepts/services-networking/service) will expose the component's interface
`manager.tolerations` | [sequence](https://yaml.org/spec/1.2/spec.html#sequence//) of [io.k8s.api.core.v1.Toleration](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#toleration-v1-core) |  | `[]` | To prevent pod from running on certain nodes. See [documentation](https://kubernetes.io/docs/concepts/configuration/taint-and-toleration/)
`manager.triggerSchema.content` | [mapping](https://yaml.org/spec/1.2/spec.html#mapping//) | required |  | See example [this section](https://github.com/gisaia/ARLAS-subscriptions#pre-requisites) of the documentation
`matcher.affinity` | [io.k8s.api.core.v1.Affinity](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#affinity-v1-core) | | `{}` | Allows assigning the Kubernetes deployment to certain Kubernetes nodes. Official docs [here](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/)
`matcher.environmentVariables` | [mapping](https://yaml.org/spec/1.2/spec.html#mapping//) | required | | Allows defining environment variables for the container. Available values are documented [here](https://github.com/gisaia/ARLAS-subscriptions#matcher-configuration)
`manager.environmentVariables.ARLAS_SERVER_BASE_PATH` | string | required | | See definition [here](https://github.com/gisaia/ARLAS-subscriptions#matcher-configuration)
`manager.environmentVariables.KAFKA_BROKERS` | string | required | | See definition [here](https://github.com/gisaia/ARLAS-subscriptions#matcher-configuration)
`matcher.image.repository` | string |  | [`gisaia/arlas-subscriptions-matcher`](hub.docker.com/r/gisaia/arlas-subscriptions-matcher) | Docker image's repository
`matcher.image.tag` | string |  | `0.0.1-SNAPSHOT` | Docker image's tag
`matcher.image.pullPolicy` | string |  | `IfNotPresent` | Docker image's pull policy. See field `imagePullPolicy` in [reference for Kubernetes object `container`](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#container-v1-core)
`matcher.imagePullSecrets` | sequence of strings |  | | Names of [imagePullSecrets](https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/) to be used by pod to pull docker images
`matcher.init.serverCollection.container.image.pullPolicy` | string |  | `IfNotPresent` | There is an [initContainer](https://kubernetes.io/docs/concepts/workloads/pods/init-containers/) in charge of creating the subscriptions collection in the arlas-subscriptions-server. This value defines image pull policy for this container. See field `imagePullPolicy` in [reference for Kubernetes object `container`](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#container-v1-core)
`matcher.init.serverCollection.container.image.repository` | string |  | [`centos`](https://hub.docker.com/_/centos) | There is an initContainer in charge of creating the subscriptions collection in the arlas-subscriptions-server. This value defines image repository for this container.
`matcher.init.serverCollection.container.image.tag` | string |  | `7` | There is an initContainer in charge of creating the subscriptions collection in the arlas-subscriptions-server. This value defines image tag for this container.
`matcher.init.serverCollection.container.image.resources.limits.cpu` | string |  | `1` | There is an initContainer in charge of creating the subscriptions collection in the arlas-subscriptions-server. This value defines maximum amount of CPU it is allowed to use. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-limits-are-run)
`matcher.init.serverCollection.container.image.resources.limits.memory` | string |  | `1G` | There is an initContainer in charge of creating the subscriptions collection in the arlas-subscriptions-server. This value defines maximum amount of memory it is allowed to use. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-limits-are-run)
`matcher.init.serverCollection.container.image.resources.requests.cpu` | string |  | `1` | There is an initContainer in charge of creating the subscriptions collection in the arlas-subscriptions-server. This value defines the minimum amount of CPU required on a Kubernetes node for this container to be scheduled onto it. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-requests-are-scheduled)
`matcher.init.serverCollection.container.image.resources.requests.memory` | string |  | `1G` | There is an initContainer in charge of creating the subscriptions collection in the arlas-subscriptions-server. This value defines the minimum amount of memory required on a Kubernetes node for this container to be scheduled onto it. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-requests-are-scheduled)
`matcher.init.serverCollection.enabled` | boolean |  | `true` | There is an initContainer in charge of creating the subscriptions collection in the arlas-subscriptions-server. This value allows disabling/enabling it.
`matcher.nodeSelector` | [io.k8s.api.core.v1.NodeSelector](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/#nodeselector) |  | `{}` | Allows selecting the nodes on which the pod is to run
`matcher.resources.limits.cpu` | string |  | `1` | Maximum amount of CPU the container is allowed to use. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-limits-are-run)
`matcher.resources.limits.memory` | string |  | `4G` | Maximum amount of memory the container is allowed to use. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-limits-are-run)
`matcher.resources.requests.cpu` | string |  | `1` | Minimum amount of CPU required on a Kubernetes node for this container to be scheduled onto it. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-requests-are-scheduled)
`matcher.resources.requests.memory` | string |  | `4G` | Minimum amount of memory required on a Kubernetes node for this container to be scheduled onto it. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-requests-are-scheduled)
`matcher.tolerations` | [sequence](https://yaml.org/spec/1.2/spec.html#sequence//) of [io.k8s.api.core.v1.Toleration](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#toleration-v1-core) |  | `[]` | To prevent pod from running on certain nodes. See [documentation](https://kubernetes.io/docs/concepts/configuration/taint-and-toleration/)
`server.affinity` | [io.k8s.api.core.v1.Affinity](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#affinity-v1-core) | | `{}` | Allows assigning the Kubernetes deployment to certain Kubernetes nodes. Official docs [here](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/)
`server.collection.centroidPath` | string | required |  | Path, in the Elasticsearch mapping type for subscription, to the field representing the centroid
`server.collection.geometryPath` | string | required |  | Path, in the Elasticsearch mapping type for subscription, to the field representing the geometry
`server.collection.idPath` | string | required |  | Path, in the Elasticsearch mapping type for subscription, to the field representing the id
`server.collection.name` | string | | `subscriptions` | Name of the subscriptions' collection in arlas-subscriptions-server
`server.collection.timestampPath` | string | required |  | Path, in the Elasticsearch mapping type for subscription, to the field representing the timestamp
`server.environmentVariables` | [mapping](https://yaml.org/spec/1.2/spec.html#mapping//) |  | | Allows defining environment variables for the container. Available values are documented [here](http://docs.arlas.io/arlas-tech/current/arlas-server-configuration/)
`server.environmentVariables.ARLAS_PREFIX` | string | | `/arlas/`| See definition [here](http://docs.arlas.io/arlas-tech/current/arlas-server-configuration/)
`server.image.repository` | string |  | [`gisaia/arlas-subscriptions-server`](hub.docker.com/r/gisaia/arlas-server) | Docker image's repository
`server.image.tag` | string |  | `11.6.0` | Docker image's tag
`server.image.pullPolicy` | string |  | `IfNotPresent` | Docker image's pull policy. See field `imagePullPolicy` in [reference for Kubernetes object `container`](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#container-v1-core)
`matcher.imagePullSecrets` | sequence of strings |  | | Names of [imagePullSecrets](https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/) to be used by pod to pull docker images
`server.nodeSelector` | [io.k8s.api.core.v1.NodeSelector](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/#nodeselector) |  | `{}` | Allows selecting the nodes on which the pod is to run
`server.resources.limits.cpu` | string |  | `1` | Maximum amount of CPU the container is allowed to use. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-limits-are-run)
`server.resources.limits.memory` | string |  | `4G` | Maximum amount of memory the container is allowed to use. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-limits-are-run)
`server.resources.requests.cpu` | string |  | `1` | Minimum amount of CPU required on a Kubernetes node for this container to be scheduled onto it. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-requests-are-scheduled)
`server.resources.requests.memory` | string |  | `4G` | Minimum amount of memory required on a Kubernetes node for this container to be scheduled onto it. See [documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#how-pods-with-resource-requests-are-scheduled)
`server.service.type` | string |  | `ClusterIP` | Type for the [Kubernetes service](https://kubernetes.io/docs/concepts/services-networking/service), see [documentation](https://kubernetes.io/docs/concepts/services-networking/service/#publishing-services-service-types)
`server.service.port` | integer |  | `9999` | Port on which the [Kubernetes service](https://kubernetes.io/docs/concepts/services-networking/service) will expose the component's interface
`server.tolerations` | [sequence](https://yaml.org/spec/1.2/spec.html#sequence//) of [io.k8s.api.core.v1.Toleration](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.12/#toleration-v1-core) |  | `[]` | To prevent pod from running on certain nodes. See [documentation](https://kubernetes.io/docs/concepts/configuration/taint-and-toleration/)

# Docker images

- [centos](https://hub.docker.com/_/centos) (initContainers)
- [gisaia/arlas-server](https://hub.docker.com/r/gisaia/arlas-server)
- [gisaia/arlas-subscriptions-manager](https://hub.docker.com/r/gisaia/arlas-subscriptions-manager)
- [gisaia/arlas-subscriptions-matcher](https://hub.docker.com/r/gisaia/arlas-subscriptions-matcher)

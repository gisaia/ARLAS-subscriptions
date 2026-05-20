<!-- Generator: Widdershins v4.0.1 -->

<h1 id="arlas-subscriptions-manager-api">ARLAS Subscriptions Manager API v24.0.0-rc1</h1>

> Scroll down for example requests and responses.

Manage ARLAS subscriptions on ARLAS collections' events.

Base URLs:

* <a href="/arlas-subscriptions-manager">/arlas-subscriptions-manager</a>

Email: <a href="mailto:contact@gisaia.com">Gisaia</a> Web: <a href="http://www.gisaia.com/">Gisaia</a> 
License: <a href="https://www.apache.org/licenses/LICENSE-2.0.html">Apache 2.0</a>

<h1 id="arlas-subscriptions-manager-api-admin">admin</h1>

Optional endpoints to manage all subscriptions as an administrator of the service.

## Find subscription by ID

<a id="opIdget"></a>

`GET /admin/subscriptions/{id}`

Return a single subscription. Only creator can access their subscriptions.

<h3 id="find-subscription-by-id-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|id|path|string|true|ID of subscription to return|
|deleted|query|boolean|false|Filter subscriptions whether they are deleted or not.|
|pretty|query|boolean|false|Pretty print|

> Example responses

> 200 Response

```json
{
  "id": "string",
  "created_at": 0,
  "modified_at": 0,
  "created_by_admin": true,
  "deleted": true,
  "created_by": "string",
  "active": true,
  "starts_at": 0,
  "expires_at": 0,
  "title": "string",
  "subscription": {
    "trigger": {
      "property1": {},
      "property2": {}
    },
    "callback": "string",
    "hits": {
      "filter": "string",
      "projection": "string"
    }
  },
  "userMetadatas": {
    "property1": {},
    "property2": {}
  },
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  }
}
```

<h3 id="find-subscription-by-id-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[UserSubscriptionWithLinks](#schemausersubscriptionwithlinks)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized.|[Error](#schemaerror)|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Subscription not found.|[Error](#schemaerror)|
|503|[Service Unavailable](https://tools.ietf.org/html/rfc7231#section-6.6.4)|Arlas Subscriptions Manager Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Update an existing subscription

<a id="opIdput"></a>

`PUT /admin/subscriptions/{id}`

Update an existing subscription. 

> Body parameter

```json
{
  "created_by": "string",
  "active": true,
  "starts_at": 0,
  "expires_at": 0,
  "title": "string",
  "subscription": {
    "trigger": {
      "property1": {},
      "property2": {}
    },
    "callback": "string",
    "hits": {
      "filter": "string",
      "projection": "string"
    }
  },
  "userMetadatas": {
    "property1": {},
    "property2": {}
  }
}
```

<h3 id="update-an-existing-subscription-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|id|path|string|true|ID of subscription to return|
|pretty|query|boolean|false|Pretty print|
|body|body|[UserSubscription](#schemausersubscription)|true|Subscription description|

> Example responses

> 201 Response

```json
{
  "id": "string",
  "created_at": 0,
  "modified_at": 0,
  "created_by_admin": true,
  "deleted": true,
  "created_by": "string",
  "active": true,
  "starts_at": 0,
  "expires_at": 0,
  "title": "string",
  "subscription": {
    "trigger": {
      "property1": {},
      "property2": {}
    },
    "callback": "string",
    "hits": {
      "filter": "string",
      "projection": "string"
    }
  },
  "userMetadatas": {
    "property1": {},
    "property2": {}
  },
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  }
}
```

<h3 id="update-an-existing-subscription-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Successful operation|[UserSubscriptionWithLinks](#schemausersubscriptionwithlinks)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|JSON parameter malformed.|[Error](#schemaerror)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized.|[Error](#schemaerror)|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Not Found Error.|[Error](#schemaerror)|
|503|[Service Unavailable](https://tools.ietf.org/html/rfc7231#section-6.6.4)|Arlas Subscriptions Manager Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Delete a subscription

<a id="opIddelete"></a>

`DELETE /admin/subscriptions/{id}`

Mark a subscription as deleted.

<h3 id="delete-a-subscription-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|id|path|string|true|Subscription ID to delete|
|pretty|query|boolean|false|Pretty print|

> Example responses

> 202 Response

```json
{
  "id": "string",
  "created_at": 0,
  "modified_at": 0,
  "created_by_admin": true,
  "deleted": true,
  "created_by": "string",
  "active": true,
  "starts_at": 0,
  "expires_at": 0,
  "title": "string",
  "subscription": {
    "trigger": {
      "property1": {},
      "property2": {}
    },
    "callback": "string",
    "hits": {
      "filter": "string",
      "projection": "string"
    }
  },
  "userMetadatas": {
    "property1": {},
    "property2": {}
  },
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  }
}
```

<h3 id="delete-a-subscription-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|202|[Accepted](https://tools.ietf.org/html/rfc7231#section-6.3.3)|Subscription has been deleted.|[UserSubscriptionWithLinks](#schemausersubscriptionwithlinks)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized.|[Error](#schemaerror)|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Subscription not found.|[Error](#schemaerror)|
|503|[Service Unavailable](https://tools.ietf.org/html/rfc7231#section-6.6.4)|Arlas Subscriptions Manager Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## List all available subscriptions

<a id="opIdgetAll"></a>

`GET /admin/subscriptions`

Return the list of all registered subscriptions from the latest created to the earliest.

<h3 id="list-all-available-subscriptions-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|before|query|integer(int64)|false|Retrieve subscriptions created before given timestamp.|
|after|query|integer(int64)|false|Retrieve subscriptions created after given timestamp.|
|active|query|boolean|false|Filter subscriptions whether they are active or not (returns all if missing, 'active' if 'true', 'not active' if 'false').|
|started|query|boolean|false|Filter subscriptions whether they are started or not (returns all if missing, 'started' if 'true', 'not started' if 'false').|
|expired|query|boolean|false|Filter subscriptions whether they are expired or not (returns all if missing, 'expired' if 'true', 'not expired' if 'false').|
|created-by|query|string|false|Filter subscriptions by creator's identifier|
|deleted|query|boolean|false|Filter subscriptions whether they are deleted or not.|
|created-by-admin|query|boolean|false|Filter subscriptions whether they have been created by admin or not (returns all if missing, 'created_by_admin' if 'true', 'not created_by_admin' if 'false').|
|pretty|query|boolean|false|Pretty print|
|page|query|integer(int64)|false|Page ID|
|size|query|integer(int64)|false|Page Size|

> Example responses

> 200 Response

```json
{
  "count": 0,
  "total": 0,
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  },
  "subscriptions": [
    {
      "id": "string",
      "created_at": 0,
      "modified_at": 0,
      "created_by_admin": true,
      "deleted": true,
      "created_by": "string",
      "active": true,
      "starts_at": 0,
      "expires_at": 0,
      "title": "string",
      "subscription": {
        "trigger": {
          "property1": {},
          "property2": {}
        },
        "callback": "string",
        "hits": {
          "filter": "string",
          "projection": "string"
        }
      },
      "userMetadatas": {
        "property1": {},
        "property2": {}
      },
      "_links": {
        "property1": {
          "relation": "string",
          "href": "string",
          "type": "string",
          "method": "string"
        },
        "property2": {
          "relation": "string",
          "href": "string",
          "type": "string",
          "method": "string"
        }
      }
    }
  ]
}
```

<h3 id="list-all-available-subscriptions-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[SubscriptionListResource](#schemasubscriptionlistresource)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized.|[Error](#schemaerror)|
|503|[Service Unavailable](https://tools.ietf.org/html/rfc7231#section-6.6.4)|Arlas Subscriptions Manager Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Register a new subscription

<a id="opIdpost"></a>

`POST /admin/subscriptions`

Register a subscription for further notification.

> Body parameter

```json
{
  "created_by": "string",
  "active": true,
  "starts_at": 0,
  "expires_at": 0,
  "title": "string",
  "subscription": {
    "trigger": {
      "property1": {},
      "property2": {}
    },
    "callback": "string",
    "hits": {
      "filter": "string",
      "projection": "string"
    }
  },
  "userMetadatas": {
    "property1": {},
    "property2": {}
  }
}
```

<h3 id="register-a-new-subscription-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|pretty|query|boolean|false|Pretty print|
|body|body|[UserSubscription](#schemausersubscription)|true|Subscription description|

> Example responses

> 201 Response

```json
{
  "id": "string",
  "created_at": 0,
  "modified_at": 0,
  "created_by_admin": true,
  "deleted": true,
  "created_by": "string",
  "active": true,
  "starts_at": 0,
  "expires_at": 0,
  "title": "string",
  "subscription": {
    "trigger": {
      "property1": {},
      "property2": {}
    },
    "callback": "string",
    "hits": {
      "filter": "string",
      "projection": "string"
    }
  },
  "userMetadatas": {
    "property1": {},
    "property2": {}
  },
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  }
}
```

<h3 id="register-a-new-subscription-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Subscription has been registered|[UserSubscriptionWithLinks](#schemausersubscriptionwithlinks)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|JSON parameter malformed.|[Error](#schemaerror)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized.|[Error](#schemaerror)|
|503|[Service Unavailable](https://tools.ietf.org/html/rfc7231#section-6.6.4)|Arlas Subscriptions Manager Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

<h1 id="arlas-subscriptions-manager-api-end-user">end-user</h1>

Standard endpoints to manage one's subscriptions as an end-user.

## Find subscription by ID

<a id="opIdget_1"></a>

`GET /subscriptions/{id}`

Return a single subscription. Only creator can access their subscriptions.

<h3 id="find-subscription-by-id-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|id|path|string|true|ID of subscription to return|
|pretty|query|boolean|false|Pretty print|

> Example responses

> 200 Response

```json
{
  "id": "string",
  "created_at": 0,
  "modified_at": 0,
  "created_by_admin": true,
  "deleted": true,
  "created_by": "string",
  "active": true,
  "starts_at": 0,
  "expires_at": 0,
  "title": "string",
  "subscription": {
    "trigger": {
      "property1": {},
      "property2": {}
    },
    "callback": "string",
    "hits": {
      "filter": "string",
      "projection": "string"
    }
  },
  "userMetadatas": {
    "property1": {},
    "property2": {}
  },
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  }
}
```

<h3 id="find-subscription-by-id-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[UserSubscriptionWithLinks](#schemausersubscriptionwithlinks)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized.|[Error](#schemaerror)|
|403|[Forbidden](https://tools.ietf.org/html/rfc7231#section-6.5.3)|Forbidden.|[Error](#schemaerror)|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Subscription not found.|[Error](#schemaerror)|
|503|[Service Unavailable](https://tools.ietf.org/html/rfc7231#section-6.6.4)|Arlas Subscriptions Manager Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Update an existing subscription

<a id="opIdput_1"></a>

`PUT /subscriptions/{id}`

Update an existing subscription. Only creator can update their own subscriptions.

> Body parameter

```json
{
  "created_by": "string",
  "active": true,
  "starts_at": 0,
  "expires_at": 0,
  "title": "string",
  "subscription": {
    "trigger": {
      "property1": {},
      "property2": {}
    },
    "callback": "string",
    "hits": {
      "filter": "string",
      "projection": "string"
    }
  },
  "userMetadatas": {
    "property1": {},
    "property2": {}
  }
}
```

<h3 id="update-an-existing-subscription-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|id|path|string|true|ID of subscription to return|
|pretty|query|boolean|false|Pretty print|
|body|body|[UserSubscription](#schemausersubscription)|true|Subscription description|

> Example responses

> 201 Response

```json
{
  "id": "string",
  "created_at": 0,
  "modified_at": 0,
  "created_by_admin": true,
  "deleted": true,
  "created_by": "string",
  "active": true,
  "starts_at": 0,
  "expires_at": 0,
  "title": "string",
  "subscription": {
    "trigger": {
      "property1": {},
      "property2": {}
    },
    "callback": "string",
    "hits": {
      "filter": "string",
      "projection": "string"
    }
  },
  "userMetadatas": {
    "property1": {},
    "property2": {}
  },
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  }
}
```

<h3 id="update-an-existing-subscription-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Successful operation|[UserSubscriptionWithLinks](#schemausersubscriptionwithlinks)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|JSON parameter malformed.|[Error](#schemaerror)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized.|[Error](#schemaerror)|
|403|[Forbidden](https://tools.ietf.org/html/rfc7231#section-6.5.3)|Forbidden.|[Error](#schemaerror)|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Not Found Error.|[Error](#schemaerror)|
|503|[Service Unavailable](https://tools.ietf.org/html/rfc7231#section-6.6.4)|Arlas Subscriptions Manager Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Delete a subscription

<a id="opIddelete_1"></a>

`DELETE /subscriptions/{id}`

Mark a subscription as deleted. Only creator can delete their own subscriptions.

<h3 id="delete-a-subscription-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|id|path|string|true|Subscription ID to delete|
|pretty|query|boolean|false|Pretty print|

> Example responses

> 202 Response

```json
{
  "id": "string",
  "created_at": 0,
  "modified_at": 0,
  "created_by_admin": true,
  "deleted": true,
  "created_by": "string",
  "active": true,
  "starts_at": 0,
  "expires_at": 0,
  "title": "string",
  "subscription": {
    "trigger": {
      "property1": {},
      "property2": {}
    },
    "callback": "string",
    "hits": {
      "filter": "string",
      "projection": "string"
    }
  },
  "userMetadatas": {
    "property1": {},
    "property2": {}
  },
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  }
}
```

<h3 id="delete-a-subscription-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|202|[Accepted](https://tools.ietf.org/html/rfc7231#section-6.3.3)|Subscription has been deleted.|[UserSubscriptionWithLinks](#schemausersubscriptionwithlinks)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized.|[Error](#schemaerror)|
|403|[Forbidden](https://tools.ietf.org/html/rfc7231#section-6.5.3)|Forbidden.|[Error](#schemaerror)|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Subscription not found.|[Error](#schemaerror)|
|503|[Service Unavailable](https://tools.ietf.org/html/rfc7231#section-6.6.4)|Arlas Subscriptions Manager Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## List all available subscriptions

<a id="opIdgetAll_1"></a>

`GET /subscriptions`

Return the list of all registered subscriptions that are available for current user from the latest created to the earliest.
Only current user's subscriptions that are not deleted are listed.

<h3 id="list-all-available-subscriptions-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|before|query|integer(int64)|false|Retrieve subscriptions created before given timestamp.|
|active|query|boolean|false|Filter subscriptions whether they are active or not (returns all if missing, 'active' if 'true', 'inactive' if 'false').|
|expired|query|boolean|false|Filter subscriptions whether they are expired or not (returns all if missing, 'expired' if 'true', 'not expired' if 'false').|
|pretty|query|boolean|false|Pretty print|
|size|query|integer(int64)|false|Page Size|
|page|query|integer(int64)|false|Page ID|

> Example responses

> 200 Response

```json
{
  "count": 0,
  "total": 0,
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  },
  "subscriptions": [
    {
      "id": "string",
      "created_at": 0,
      "modified_at": 0,
      "created_by_admin": true,
      "deleted": true,
      "created_by": "string",
      "active": true,
      "starts_at": 0,
      "expires_at": 0,
      "title": "string",
      "subscription": {
        "trigger": {
          "property1": {},
          "property2": {}
        },
        "callback": "string",
        "hits": {
          "filter": "string",
          "projection": "string"
        }
      },
      "userMetadatas": {
        "property1": {},
        "property2": {}
      },
      "_links": {
        "property1": {
          "relation": "string",
          "href": "string",
          "type": "string",
          "method": "string"
        },
        "property2": {
          "relation": "string",
          "href": "string",
          "type": "string",
          "method": "string"
        }
      }
    }
  ]
}
```

<h3 id="list-all-available-subscriptions-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[SubscriptionListResource](#schemasubscriptionlistresource)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized.|[Error](#schemaerror)|
|403|[Forbidden](https://tools.ietf.org/html/rfc7231#section-6.5.3)|Forbidden.|[Error](#schemaerror)|
|503|[Service Unavailable](https://tools.ietf.org/html/rfc7231#section-6.6.4)|Arlas Subscriptions Manager Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Register a new subscription

<a id="opIdpost_1"></a>

`POST /subscriptions`

Register a subscription for further notification.

> Body parameter

```json
{
  "created_by": "string",
  "active": true,
  "starts_at": 0,
  "expires_at": 0,
  "title": "string",
  "subscription": {
    "trigger": {
      "property1": {},
      "property2": {}
    },
    "callback": "string",
    "hits": {
      "filter": "string",
      "projection": "string"
    }
  },
  "userMetadatas": {
    "property1": {},
    "property2": {}
  }
}
```

<h3 id="register-a-new-subscription-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|pretty|query|boolean|false|Pretty print|
|body|body|[UserSubscription](#schemausersubscription)|true|Subscription description|

> Example responses

> 201 Response

```json
{
  "id": "string",
  "created_at": 0,
  "modified_at": 0,
  "created_by_admin": true,
  "deleted": true,
  "created_by": "string",
  "active": true,
  "starts_at": 0,
  "expires_at": 0,
  "title": "string",
  "subscription": {
    "trigger": {
      "property1": {},
      "property2": {}
    },
    "callback": "string",
    "hits": {
      "filter": "string",
      "projection": "string"
    }
  },
  "userMetadatas": {
    "property1": {},
    "property2": {}
  },
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  }
}
```

<h3 id="register-a-new-subscription-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Subscription has been registered|[UserSubscriptionWithLinks](#schemausersubscriptionwithlinks)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|JSON parameter malformed.|[Error](#schemaerror)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized.|[Error](#schemaerror)|
|403|[Forbidden](https://tools.ietf.org/html/rfc7231#section-6.5.3)|Forbidden.|[Error](#schemaerror)|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Not Found Error.|[Error](#schemaerror)|
|503|[Service Unavailable](https://tools.ietf.org/html/rfc7231#section-6.6.4)|Arlas Subscriptions Manager Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

# Schemas

<h2 id="tocS_Hits">Hits</h2>
<!-- backwards compatibility -->
<a id="schemahits"></a>
<a id="schema_Hits"></a>
<a id="tocShits"></a>
<a id="tocshits"></a>

```json
{
  "filter": "string",
  "projection": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|filter|string|true|none|none|
|projection|string|true|none|none|

<h2 id="tocS_Link">Link</h2>
<!-- backwards compatibility -->
<a id="schemalink"></a>
<a id="schema_Link"></a>
<a id="tocSlink"></a>
<a id="tocslink"></a>

```json
{
  "relation": "string",
  "href": "string",
  "type": "string",
  "method": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|relation|string|true|none|none|
|href|string|true|none|none|
|type|string|true|none|none|
|method|string|true|none|none|

<h2 id="tocS_Subscription">Subscription</h2>
<!-- backwards compatibility -->
<a id="schemasubscription"></a>
<a id="schema_Subscription"></a>
<a id="tocSsubscription"></a>
<a id="tocssubscription"></a>

```json
{
  "trigger": {
    "property1": {},
    "property2": {}
  },
  "callback": "string",
  "hits": {
    "filter": "string",
    "projection": "string"
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|trigger|object|true|none|none|
|» **additionalProperties**|object|false|none|none|
|callback|string|true|none|none|
|hits|[Hits](#schemahits)|true|none|none|

<h2 id="tocS_UserSubscriptionWithLinks">UserSubscriptionWithLinks</h2>
<!-- backwards compatibility -->
<a id="schemausersubscriptionwithlinks"></a>
<a id="schema_UserSubscriptionWithLinks"></a>
<a id="tocSusersubscriptionwithlinks"></a>
<a id="tocsusersubscriptionwithlinks"></a>

```json
{
  "id": "string",
  "created_at": 0,
  "modified_at": 0,
  "created_by_admin": true,
  "deleted": true,
  "created_by": "string",
  "active": true,
  "starts_at": 0,
  "expires_at": 0,
  "title": "string",
  "subscription": {
    "trigger": {
      "property1": {},
      "property2": {}
    },
    "callback": "string",
    "hits": {
      "filter": "string",
      "projection": "string"
    }
  },
  "userMetadatas": {
    "property1": {},
    "property2": {}
  },
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|id|string|false|read-only|none|
|created_at|integer(int64)|false|read-only|none|
|modified_at|integer(int64)|false|read-only|none|
|created_by_admin|boolean|false|read-only|none|
|deleted|boolean|false|read-only|none|
|created_by|string|true|none|none|
|active|boolean|true|none|none|
|starts_at|integer(int64)|true|none|none|
|expires_at|integer(int64)|true|none|none|
|title|string|true|none|none|
|subscription|[Subscription](#schemasubscription)|true|none|none|
|userMetadatas|object|false|none|none|
|» **additionalProperties**|object|false|none|none|
|_links|object|false|none|none|
|» **additionalProperties**|[Link](#schemalink)|false|none|none|

<h2 id="tocS_Error">Error</h2>
<!-- backwards compatibility -->
<a id="schemaerror"></a>
<a id="schema_Error"></a>
<a id="tocSerror"></a>
<a id="tocserror"></a>

```json
{
  "status": 0,
  "message": "string",
  "error": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|status|integer(int32)|false|none|none|
|message|string|false|none|none|
|error|string|false|none|none|

<h2 id="tocS_SubscriptionListResource">SubscriptionListResource</h2>
<!-- backwards compatibility -->
<a id="schemasubscriptionlistresource"></a>
<a id="schema_SubscriptionListResource"></a>
<a id="tocSsubscriptionlistresource"></a>
<a id="tocssubscriptionlistresource"></a>

```json
{
  "count": 0,
  "total": 0,
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  },
  "subscriptions": [
    {
      "id": "string",
      "created_at": 0,
      "modified_at": 0,
      "created_by_admin": true,
      "deleted": true,
      "created_by": "string",
      "active": true,
      "starts_at": 0,
      "expires_at": 0,
      "title": "string",
      "subscription": {
        "trigger": {
          "property1": {},
          "property2": {}
        },
        "callback": "string",
        "hits": {
          "filter": "string",
          "projection": "string"
        }
      },
      "userMetadatas": {
        "property1": {},
        "property2": {}
      },
      "_links": {
        "property1": {
          "relation": "string",
          "href": "string",
          "type": "string",
          "method": "string"
        },
        "property2": {
          "relation": "string",
          "href": "string",
          "type": "string",
          "method": "string"
        }
      }
    }
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|count|integer(int32)|false|none|none|
|total|integer(int32)|false|none|none|
|_links|object|false|none|none|
|» **additionalProperties**|[Link](#schemalink)|false|none|none|
|subscriptions|[[UserSubscriptionWithLinks](#schemausersubscriptionwithlinks)]|false|none|none|

<h2 id="tocS_UserSubscription">UserSubscription</h2>
<!-- backwards compatibility -->
<a id="schemausersubscription"></a>
<a id="schema_UserSubscription"></a>
<a id="tocSusersubscription"></a>
<a id="tocsusersubscription"></a>

```json
{
  "id": "string",
  "created_at": 0,
  "modified_at": 0,
  "created_by_admin": true,
  "deleted": true,
  "created_by": "string",
  "active": true,
  "starts_at": 0,
  "expires_at": 0,
  "title": "string",
  "subscription": {
    "trigger": {
      "property1": {},
      "property2": {}
    },
    "callback": "string",
    "hits": {
      "filter": "string",
      "projection": "string"
    }
  },
  "userMetadatas": {
    "property1": {},
    "property2": {}
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|id|string|false|read-only|none|
|created_at|integer(int64)|false|read-only|none|
|modified_at|integer(int64)|false|read-only|none|
|created_by_admin|boolean|false|read-only|none|
|deleted|boolean|false|read-only|none|
|created_by|string|true|none|none|
|active|boolean|true|none|none|
|starts_at|integer(int64)|true|none|none|
|expires_at|integer(int64)|true|none|none|
|title|string|true|none|none|
|subscription|[Subscription](#schemasubscription)|true|none|none|
|userMetadatas|object|false|none|none|
|» **additionalProperties**|object|false|none|none|


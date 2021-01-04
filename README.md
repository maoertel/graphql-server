# GraphQL Test Project

## Technologies used

- Sangria
- http4s
- MongoDB
- Circe

## Preparation

Add an own `db.conf` ot `resources` file with the following properties:

```hocon
db {
  connectionString: "<your mongo connection string>"
  name: "<your db name>"
  user: "<user>"
  password: "<password>"
}
```

## Usage

- start the server (runs on localhost:8080)
- the server is implemented to work with `GET` (url parameters) as well as `POST` (parameters in the body) requests
  against the `/graphql` endpoint
- do some mutations & queries (here with httpie against the `POST` endpoint)

### Insert a Product

```shell
echo '{
  "query": "mutation Foo($draft: ProductInput!) { createProduct(productDraft: $draft) { id name description } }",
  "variables": {
    "draft": {
      "name": "Tenet - BluRay",
      "description": "sator arepo tenet opera rotas"
    }
  }
}' | http -f POST :8080/graphql
```

### Query Products

```shell
echo '{ "query" : "query { products { name id } }" }' | http -f POST :8080/graphql
```

### Update a product

```shell
echo '{
  "query": "mutation Foo($id: String!, $draft: ProductInput!) { updateProduct(id: $id, productDraft: $draft) { id name description } }",
  "variables": {
    "id": "<put in the ID of the product you wanna update>",
    "draft": {
      "name": "Tenet - BluRay",
      "description": "The story of inverted entropy."
    }
  }
}' | http -f POST :8080/graphql
```

### Delete a Product

- `not implemented yet`

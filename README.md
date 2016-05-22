Service Dependency Graph
====

This project is used to represent microservice dependency graph.


## Projects Description


### Service Stats


It's responsible for receive microservices data, and return that data in a graph format to be displayed by the Service Viewer project. All data is stored using the current time as an identification, so this way is possible to view data trough time.


This data in JSON format is send through a HTTP POST to /calling URI, and it's represented by:

* Caller Name (Microservice that perform the call)
* Called Name (Microservice that receive the call)
* Called Endpoint Name (Microservice's endpoint that receive the call)


Example:

```json
{
  "serviceCaller":"checkout",
  "serviceCalled":"users",
  "endpointCalled":"findById"
}
```


#### Test

```sh
$ sbt test
```


#### Run

```sh
$ sbt run
```


#### How it works internally


##### Storing data


All data is stored into Redis using HASH format to represent graph data.

The key for Node/Vertex is **nodes:HH.mm** (HH is hour and mm is minutes). Example: **nodes:17.41**

The key for Link/Edge is **edges:HH.mm** (HH is hour and mm is minutes). Example: **edges:17.41**

The node's index is generate by a Redis String using the **inc** command.


##### Displaying Data


Currently is only possible to display a graph for a specific time (hour:minute).


##### What is missing


Add TTL to all data inserted, so this there's no conflict among dates.


### Service Viewer


It's reponsible for displaying the graph in a browser.

* Grunt
* Zepto


## How do I execute it locally?

Just type:

```sh
$ grunt
```

Then use http://localhost:4000 to access the blog through your browser.


## How do I publish a new version?

Type:

```sh
$ grunt publish
```

It'll create a new build version, execute cssmin and uglify, after that the process will make a git commit and automatically publish it to github.
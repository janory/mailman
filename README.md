# mailman 📫
Mailman is an in memory mail service, which helps you to avoid spams by easily creating random email addresses.
The service can be reached via an idiomatic REST API. Check the endpoints below.
## REST API 
- `POST /mailboxes`: Create a new, random email address. 
- `POST /mailboxes/{email address}/messages`: Create a new message for a specific email address. 
- `GET /mailboxes/{email address}/messages`: Retrieve an index of messages sent to an email address, including sender, subject, and id, in recency order. Support cursor-based pagination through the index. 
- `GET /mailboxes/{email address}/messages/{message id}`: Retrieve a specific message by id. 
- `DELETE /mailboxes/{email address}`: Delete a specific email address and any associated messages. 
- `DELETE /mailboxes/{email address}/messages/{message id}`: Delete a specific message by id.

## Used technologies & frameworks
- Akka HTTP to create the client facing API
- Akka for mailbox creation and message distribution
- Ficus for configuration parsing
- Circe for JSON parsing
- Gatling for load testing 

## Postman support
You can find the postman collection and environment files [here](https://github.com/janory/mailman/tree/master/postman_collection).
These files can be directly imported in Postman and they give you all the support the play around with the REST API

## Benchmarking & Measures
Currently I've only tested this small service on a RPI2.
I was able to run the service with a good performance with a fairly low amount of memory.
![Indicators](https://raw.githubusercontent.com/janory/mailman/master/mailman-app-loadtest/results/indicators.png)
![Number of requests per second](https://raw.githubusercontent.com/janory/mailman/master/mailman-app-loadtest/results/number_of_requests_per_second.png)
![Number of responses per second](https://raw.githubusercontent.com/janory/mailman/master/mailman-app-loadtest/results/number_of_responses_per_second.png)


## TBD...
- Add proper tests
- Experiment with different memory configurations under different loads
- Add GralVM support for faster startup

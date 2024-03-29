


# tip of the spear '19
Welcome!
In this repository you will find the full implemention of a common use case pattern, one that comes as a replacement for another extensively proven pattern.

**abstract**
ALPAKKA TRANSACTION PROPOSAL
instead of communicating the pipeline processors via the database,
which was the '18 pattern of achieving full resilience.

**longer abstract**
When you have an ETL, ussually you don't want to lose messages.
To achieve this you use the hard disc.
In **2018** it became common use the Journal Poller pattern, which consisted on listening the database for changes, and reacting in consecuence.
In **2019**, since the release of Kafka 0.11.0. now it's becoming popular tu use Transactions instead.
In **2020** we may even see the Kafka Persistance project reach fruition, we'll see.


# Context

In this project I will show how Kafka Transactions can be used to fix some issues present in the actor model, namely the following:
- what happens when an important message receives no answer.
 You retry until it is answered successfully?
 Yes, that's exactly what you do. You retry until confirmation.
 You need a handshake! Remember TCP protocol?

You can do this with actors. Problem is you will fill your codebase of delivery-related guarantees, all of that mixed up with your bussiness logic. Not fun. Could a framework be made that could hide all this? Yes. Has it been made yet? No.

Second option is a physical handshake. Old typical solution for ETL pipelines, worked for the 90's, why wouldn't it work now?
This is what the Journal Poller pattern is all about.
Each pipeline processor node is listening for events in the database with Akka Persistence Query. Remember, no mention of inter-actor messaging here being made, because of Akka failures to confront this problem.

Third option, this one.

# One last thing

You can reliably send messages between Akka actors if they are inside the same node. That is, if they are physically located inside the same container in the cluster.
Do they need to communicate with another node? Then you need stronger guarantees, and you will use some of the solutions listed below to confront this problem.
*The chance of having a network failure make you lose a message.*




So, let's start.
I present you, an Actor that fails half the time. I call it, The Imperfect Actor.

# Introduction
##  The Imperfect Actor
[Imperfect Actor implementation](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/main/scala/introduction/ImperfectActor.scala)

[Imperfect Actor Specs](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/test/scala/introduction/IntroductionSpec.scala)


# Chapter I
##  Alpakka Transaction to the rescue
[Concrete Transaction implementation](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/main/scala/chapter_1/transaction/Transaction.scala)

[Transaction Specs](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/test/scala/chapter_1/chapter_1/transaction/TransactionSpec.scala)

[- - Link to example by the Alpakka Team Github](https://github.com/akka/alpakka-kafka/blob/master/tests/src/test/scala/akka/kafka/scaladsl/TransactionsSpec.scala)


# Chapter I revisited
##  Typeclasses and genericity
[Typeclass for Message serialization](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/main/scala/serialization/JsonFormats.scala)
Added a Typeclass for Message serialization.
  This adds a method to a class without modifying the source code. * Open-Close principle, **check!** *

[Generic Transaction implementation](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/main/scala/chapter_1/transaction/Transaction.scala)
Added the extra complexity needed to make Transaction generic. Visit the previous commit for a simpler, more concrete idea of Transaction.



# Chapter II
## Two actors on the same node
If your actors share the node they are located in, you can be sure that your messages are not going to experience network failure.
 #### II.I Creating an Actor
I worked on a Cabbify killer app so I am going to use the transportation bussiness domain as an example.
[Taxi main](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/main/scala/chapter_2/model/taxi/)
[Taxi test](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/test/scala/chapter_2/model/taxi/)
 #### II.II Testing survivability
By killing the actor and recovering its state from persistence we can prove its ready to survive crashes.
[Testing survivability](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/test/scala/chapter_2/model/taxi/TaxiAggregateSpec.scala)
 #### II.III Two actors, same node. No network failures!
If an actor creates a child actor and routes interactions to him, with no sharding, they will stay inside the same physical node.
Communication among them will work like method calls.
[TaxiDriver main](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/main/scala/chapter_2/model/taxi/taxiDriver)
[TaxiDriver test](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/main/scala/chapter_2/model/taxi/)

# Chapter III
## Different aggregates, different nodes.
Here is where fun starts.
In **Domain Driven Design** an Actor represents an Aggregate, and so, all communication between aggregates goes through network, with chance of failure.
This is where some teams use Journal Pollers to listen to each other Aggregates events on the database. This achieves rock-solid communication, because it is written in hard disc.
We are going to use Kafka instead. _Buckle up!_


 #### III.I The right questions.
 Given a taxi company.
 The company wants to know how to locate it's fleet in a way that avoids common sense and is actually data driven.
 Let's start talking domain language fist.
 We will define a zone as a small part of a city.
 - ##### Given a zone
[How many people are usually in the zone?](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/main/scala/chapter_3/model/zone/package.scala)

 [How many people are usually in the zone around this hour?](https://i.kym-cdn.com/photos/images/newsfeed/000/770/871/1a9.gif)

 [How many people are usually in the zone this day of week?](https://i.imgflip.com/jp9k8.jpg)

 [How many people are usually in the zone this day of week _around this hour_?](https://i.kym-cdn.com/photos/images/newsfeed/000/574/293/c22.png)

  #### III.II Designing the state of Zone
[Zone main](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/main/scala/chapter_3/model/zone/ZoneState.scala)

[Zone test](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/test/scala/chapter_3/model/zone/ZoneStateSpec.scala)


  #### III.III Designing the ZoneActor
[Zone main](https://github.com/miguelemosreverte/ReactiveExample/blob/7f50767d0c00d8d2963619d63e7283e0cea5fd6c/src/main/scala/chapter_3/model/zone/ZoneActor.scala)

[Zone test](https://github.com/miguelemosreverte/ReactiveExample/blob/7f50767d0c00d8d2963619d63e7283e0cea5fd6c/src/test/scala/chapter_3/model/zone/ZoneActorSpec.scala)

  #### III.IV Designing the ZoneActor to answer queries
[Zone main](https://github.com/miguelemosreverte/ReactiveExample/blob/6ce20c3cc9d43c5daa69eba335e7baf133d3474d/src/main/scala/chapter_3/model/zone/ZoneActor.scala)

[Zone test](https://github.com/miguelemosreverte/ReactiveExample/blob/6ce20c3cc9d43c5daa69eba335e7baf133d3474d/src/test/scala/chapter_3/model/zone/ZoneActorSpec.scala)

  #### III.V Custom Cluster Sharding
  By using GeoPoints as aggregateRoot we can say that all requests around an area should be processed by a specific node,
  and then increase the hardware in which that specific node is running.
  This allows a strategy in which 'Top Sellers' accounts can be served with extra hardware requirements.
  This also allows to avoid having the 'Top Sellers' accounts become a bottleneck to the rest of the smaller accounts.

[Custom Sharding](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/main/scala/akka/ShardedEntity.scala)

[Zone test with GeoPoint sharding](https://github.com/miguelemosreverte/ReactiveExample/blob/421cd09717b84604f133a4f8f9594a1c2877462a/src/test/scala/chapter_3/model/zone/ZoneActorSpec.scala)

  #### III.VI Transaction Flow
Given a **StartTrip** command the whole mechanism will come to life and a Taxi wil take the trip.
When taking the passenger the taxi will inform that the person has left the Zone in which it was, and is directing onto another Zone. By knowing this each Zone can perform the bussiness intelligence required to know how many people are inside them about to take trips each day, each hour, and each hour of each day. Zone is a ReadSide for complex queries, _queries that would perform very slowly if computed from the ground up, but are instantaneus_ thanks to the way Akka stores the intermediates results.

![representation](https://i.imgur.com/3KIHwgo.png)

[Transaction Flow](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/main/scala/chapter_3/transactionFlow/TransactionFlow.scala)

[Transaction Flow Spec](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/test/scala/chapter_3/transactionFlow/TransactionFlowSpec.scala)

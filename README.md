
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
  
You can relaiavely send messages between Akka actors if they are inside the same node. That is, if they are physically located inside the same container in the cluster.  
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
  
[Transaction Specs](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/test/scala/chapter_1/TransactionSpec.scala)

[- - Link to example by the Alpakka Team Github](https://github.com/akka/alpakka-kafka/blob/master/tests/src/test/scala/akka/kafka/scaladsl/TransactionsSpec.scala)


# Chapter I revisited
##  Typeclasses and genericity
[Typeclass for Message serialization](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/main/scala/serialization/JsonFormats.scala)
Added a Typeclass for Message serialization.
  This adds a method to a class without modifying the source code. * Open-Close principle, **check!** *

[Generic Transaction implementation](https://github.com/miguelemosreverte/ReactiveExample/blob/master/src/main/scala/chapter_1/transaction/Transaction.scala)
Added the extra complexity needed to make Transaction generic. Visit the previous commit for a simpler, more concrete idea of Transaction.


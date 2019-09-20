package utils

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import org.scalatest.{ AsyncWordSpecLike, BeforeAndAfter, BeforeAndAfterAll, Matchers, MustMatchers, WordSpecLike }

/** Presents the interface for all specs that requires an ActorSystem
  * @param _system the unique system for the whole test suite (ExamplePCSSystem.system)
  */
abstract class ExampleSystemSpec(_system: ActorSystem)
  extends TestKit(_system)
    with AsyncWordSpecLike
    with MustMatchers
    with BeforeAndAfter {

  def this() = this(ExampleSystem.system)

}

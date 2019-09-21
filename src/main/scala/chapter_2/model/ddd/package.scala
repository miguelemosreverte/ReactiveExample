package chapter_2.model

package object ddd {


  trait Event
  trait Command
  trait Response

  trait AbstractState {
    def +(event: Event) : AbstractState
  }
}

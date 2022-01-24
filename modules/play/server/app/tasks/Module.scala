package tasks

import actors._
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bindActor[Messenger]("messengerActor")
    //bindTypedActor(UserParentActor, "userParentActor")
   // bind(classOf[actors.Messenger.Factory]).toProvider(classOf[MessengerActorFactoryProvider])
  }
}

//@Singleton
//class MessengerActorFactoryProvider @Inject()(
//                                          stocksActor: ActorRef[Messenger.],
//                                          mat: Materializer,
//                                          ec: ExecutionContext,
//                                        ) extends Provider[UserActor.Factory] {
//  def get() = UserActor(_, stocksActor)(mat, ec)
//}
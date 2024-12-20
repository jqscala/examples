package commits_fs2 

import cats.effect.kernel.Async
import fs2.Pull
import fs2.Chunk
import fs2.Stream
import io.circe.Json
import org.typelevel.log4cats.LoggerFactory
import jq.std.{IsArray, IsObject}
import scala.concurrent.duration._

object Commits:

  // Utility function
  extension [F[_]: Async, A](st: Stream[F, A])
    def attempt(times: Int, delay: FiniteDuration): Stream[F, A] =
      st handleErrorWith: error =>
        if times == 0 then
          Stream.raiseError(new Exception("Number of attempts exceeded", error))
        else
          Stream.sleep(delay) >> attempt(times-1, delay)

  def from[F[_]: Async: LoggerFactory](repo: String, token: String): Stream[F, Json] =

    def newPage(i: Int): Stream[F, Json] = 
        Stream.fromUri(org.http4s.Uri.unsafeFromString(s"$repo/commits?page=$i"), token)
            .evalTap(_ => Async[F].delay(println(s"page $i")))
    
    def loop(next: Int): Stream[F, Json] =
        newPage(next)
          .attempt(3, 1000.millis)
          .flatMap:
            case IsObject(error) => Stream.raiseError(new Exception(error.toString))
            case IsArray(Vector()) => Stream.empty
            case array => Stream(array) ++ loop(next + 1)
            
    loop(1)

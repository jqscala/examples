package commits_fs2 

import cats.effect.kernel.Async
import fs2.Pull
import fs2.Stream
import io.circe.Json
import org.typelevel.log4cats.LoggerFactory
import jq.std.IsArray

object Commits:

  def from[F[_]: Async: LoggerFactory](repo: String, token: String): Stream[F, Json] =

    def newPage(i: Int): Stream[F, Json] = 
        Stream.fromUri(org.http4s.Uri.unsafeFromString(s"$repo/commits?page=$i"), token)
            .evalTap(_ => Async[F].delay(println(s"page $i")))
    
    def loop(next: Int): Stream[F, Json] =
        newPage(next) flatMap: 
            case IsArray(Vector()) => Stream.empty
            case array => Stream(array) ++ loop(next + 1)
            
    loop(1)

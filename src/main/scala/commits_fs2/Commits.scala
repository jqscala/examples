package commits_fs2 

import cats.effect.kernel.Async
import fs2.Pull
import fs2.Stream
import io.circe.Json
import org.typelevel.log4cats.LoggerFactory
import jq.IsArray

object Commits:

  def from[F[_]: Async: LoggerFactory](repo: String, token: String): Stream[F, Json] =

    def newPage(i: Int): Stream[F, Json] = 
        Stream.fromUri(org.http4s.Uri.unsafeFromString(s"$repo/commits?page=$i"), token)
    
    def go(i: Int, s: Stream[F,Json]): Pull[F, Json, Unit] =
        s.pull.uncons.flatMap:
            case Some((hd,tl)) =>
                hd(0) match
                    case IsArray(Vector()) => Pull.done
                    case _ => Pull.output(hd) >> go(i+1, tl ++ newPage(i))
            case None => Pull.done

    go(2, newPage(1)).stream
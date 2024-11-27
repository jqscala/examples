package commits_zio

import zio._
import zio.stream._
import zio.http._
import zio.json._, ast._

object Commits:

  object IsArray: 
      def unapply(json: Json): Option[Chunk[Json]] = 
          json.asArray
          
  def from(repo: String, token: String): ZStream[Client, Throwable, Json] =

    def newPage(i: Int): ZStream[Client, Throwable, Json] = 
        ZStream.fromUri(s"$repo/commits?page=$i", token)
            .mapZIO(_.body.asString)
            .map(_.fromJson[Json])
            .collectRight
    
    def go(page: ZStream[Client, Throwable, Json], next: Int): ZStream[Client, Throwable, Json] =
        page.flatMap: 
            case IsArray(Chunk()) => ZStream.empty
            case _ => page ++ go(newPage(next), next + 1)

    go(newPage(1), 2)
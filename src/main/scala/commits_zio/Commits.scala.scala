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
            .tap(_ => zio.Console.printLine(s"page: $i"))
    
    def loop(next: Int): ZStream[Client, Throwable, Json] =
        newPage(next) flatMap: 
            case IsArray(Chunk()) => ZStream.empty
            case array => ZStream(array) ++ loop(next + 1)
            
    loop(1)
package commits_zio

import zio._
import zio.stream._
import zio.http._
import zio.json._, ast._

object Commits:

  def from(repo: String, token: String): ZStream[Client, Throwable, Json] =

    def newPage(i: Int): ZStream[Client, Throwable, Json] = 
        ZStream.fromUri(s"$repo/commits?page=$i", token)
            .mapZIO(_.body.asString)
            .map(_.fromJson[Json])
            .collectRight
    
    def go(s: ZStream[Client, Throwable, Json], next: Int): ZStream[Client, Throwable, Json] =
        s.flatMap: page => 
            if page.asArray.map(_.isEmpty).getOrElse(true) then ZStream.empty
            else ZStream(page) ++ go(newPage(next), next + 1)

    go(newPage(0), 1)
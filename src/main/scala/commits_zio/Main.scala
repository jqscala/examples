package commits_zio

import zio._
import zio.stream._
import zio.json._
import zio.json.ast._, JsonCursor._
import zio.http._
import jq._
import jqzio._, jqzio.JQZioJZ

object Main extends ZIOAppDefault:

    def info[R, E]: ZPipeline[R, E, Json, Json] = 
        ZPipeline
            .collect(Function.unlift((json: Json) => json.asArray))
            .flattenChunks
            .map: commit => 
                for 
                    login <- commit.get(field("author") >>> isObject >>> field("login"))
                    msg <- commit.get(field("commit") >>> isObject >>> field("message"))
                yield Json.Arr(login, msg)
            .collectRight

    def infoWithJq[R, E]: ZPipeline[R, E, Json, Json | jqzio.TypeError] = 
        iterator | arr(i"author.login", i"commit.message")

    override val run = 
        // https://docs.github.com/en/rest/authentication/authenticating-to-the-rest-api?apiVersion=2022-11-28
        System.env("BEARER_GITHUB_TOKEN").flatMap: 
            case None => 
                Console.printLine("Environment variable BEARER_GITHUB_TOKEN is not set") *> 
                ZIO.succeed(1)
            case Some(token) => 
                ZIO.scoped:
                    Commits.from("https://api.github.com/repos/jserranohidalgo/urjc-pd",token)
                        .via(infoWithJq)
                        .take(10)
                        .foreach(Console.printLine(_))
                .provide(ZClient.default)
